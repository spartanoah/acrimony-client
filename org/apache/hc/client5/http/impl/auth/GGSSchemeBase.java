/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Locale;
import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.apache.hc.client5.http.auth.KerberosConfig;
import org.apache.hc.client5.http.auth.KerberosCredentials;
import org.apache.hc.client5.http.auth.MalformedChallengeException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GGSSchemeBase
implements AuthScheme {
    private static final Logger LOG = LoggerFactory.getLogger(GGSSchemeBase.class);
    private final KerberosConfig config;
    private final DnsResolver dnsResolver;
    private State state;
    private GSSCredential gssCredential;
    private String challenge;
    private byte[] token;

    GGSSchemeBase(KerberosConfig config, DnsResolver dnsResolver) {
        this.config = config != null ? config : KerberosConfig.DEFAULT;
        this.dnsResolver = dnsResolver != null ? dnsResolver : SystemDefaultDnsResolver.INSTANCE;
        this.state = State.UNINITIATED;
    }

    GGSSchemeBase(KerberosConfig config) {
        this(config, SystemDefaultDnsResolver.INSTANCE);
    }

    GGSSchemeBase() {
        this(KerberosConfig.DEFAULT, SystemDefaultDnsResolver.INSTANCE);
    }

    @Override
    public String getRealm() {
        return null;
    }

    @Override
    public void processChallenge(AuthChallenge authChallenge, HttpContext context) throws MalformedChallengeException {
        Args.notNull(authChallenge, "AuthChallenge");
        if (authChallenge.getValue() == null) {
            throw new MalformedChallengeException("Missing auth challenge");
        }
        this.challenge = authChallenge.getValue();
        if (this.state == State.UNINITIATED) {
            this.token = Base64.decodeBase64(this.challenge.getBytes());
            this.state = State.CHALLENGE_RECEIVED;
        } else {
            LOG.debug("Authentication already attempted");
            this.state = State.FAILED;
        }
    }

    protected GSSManager getManager() {
        return GSSManager.getInstance();
    }

    protected byte[] generateGSSToken(byte[] input, Oid oid, String serviceName, String authServer) throws GSSException {
        GSSManager manager = this.getManager();
        GSSName serverName = manager.createName(serviceName + "@" + authServer, GSSName.NT_HOSTBASED_SERVICE);
        GSSContext gssContext = this.createGSSContext(manager, oid, serverName, this.gssCredential);
        if (input != null) {
            return gssContext.initSecContext(input, 0, input.length);
        }
        return gssContext.initSecContext(new byte[0], 0, 0);
    }

    protected GSSContext createGSSContext(GSSManager manager, Oid oid, GSSName serverName, GSSCredential gssCredential) throws GSSException {
        GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, gssCredential, 0);
        gssContext.requestMutualAuth(true);
        if (this.config.getRequestDelegCreds() != KerberosConfig.Option.DEFAULT) {
            gssContext.requestCredDeleg(this.config.getRequestDelegCreds() == KerberosConfig.Option.ENABLE);
        }
        return gssContext;
    }

    protected abstract byte[] generateToken(byte[] var1, String var2, String var3) throws GSSException;

    @Override
    public boolean isChallengeComplete() {
        return this.state == State.TOKEN_GENERATED || this.state == State.FAILED;
    }

    @Override
    public boolean isResponseReady(HttpHost host, CredentialsProvider credentialsProvider, HttpContext context) throws AuthenticationException {
        Args.notNull(host, "Auth host");
        Args.notNull(credentialsProvider, "CredentialsProvider");
        Credentials credentials = credentialsProvider.getCredentials(new AuthScope(host, null, this.getName()), context);
        this.gssCredential = credentials instanceof KerberosCredentials ? ((KerberosCredentials)credentials).getGSSCredential() : null;
        return true;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public String generateAuthResponse(HttpHost host, HttpRequest request, HttpContext context) throws AuthenticationException {
        Args.notNull(host, "HTTP host");
        Args.notNull(request, "HTTP request");
        switch (this.state) {
            case UNINITIATED: {
                throw new AuthenticationException(this.getName() + " authentication has not been initiated");
            }
            case FAILED: {
                throw new AuthenticationException(this.getName() + " authentication has failed");
            }
            case CHALLENGE_RECEIVED: {
                try {
                    String hostname = host.getHostName();
                    if (this.config.getUseCanonicalHostname() != KerberosConfig.Option.DISABLE) {
                        try {
                            hostname = this.dnsResolver.resolveCanonicalHostname(host.getHostName());
                        } catch (UnknownHostException ignore) {
                            // empty catch block
                        }
                    }
                    String authServer = this.config.getStripPort() != KerberosConfig.Option.DISABLE ? hostname : hostname + ":" + host.getPort();
                    String serviceName = host.getSchemeName().toUpperCase(Locale.ROOT);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("init {}", (Object)authServer);
                    }
                    this.token = this.generateToken(this.token, serviceName, authServer);
                    this.state = State.TOKEN_GENERATED;
                } catch (GSSException gsse) {
                    this.state = State.FAILED;
                    if (gsse.getMajor() == 9 || gsse.getMajor() == 8) {
                        throw new InvalidCredentialsException(gsse.getMessage(), gsse);
                    }
                    if (gsse.getMajor() == 13) {
                        throw new InvalidCredentialsException(gsse.getMessage(), gsse);
                    }
                    if (gsse.getMajor() == 10 || gsse.getMajor() == 19 || gsse.getMajor() == 20) {
                        throw new AuthenticationException(gsse.getMessage(), gsse);
                    }
                    throw new AuthenticationException(gsse.getMessage());
                }
            }
            case TOKEN_GENERATED: {
                Base64 codec = new Base64(0);
                String tokenstr = new String(codec.encode(this.token));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending response '{}' back to the auth server", (Object)tokenstr);
                }
                return "Negotiate " + tokenstr;
            }
        }
        throw new IllegalStateException("Illegal state: " + (Object)((Object)this.state));
    }

    public String toString() {
        return this.getName() + "{" + (Object)((Object)this.state) + " " + this.challenge + '}';
    }

    static enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        TOKEN_GENERATED,
        FAILED;

    }
}

