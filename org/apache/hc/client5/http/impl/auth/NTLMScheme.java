/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.security.Principal;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.MalformedChallengeException;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.impl.auth.NTLMEngine;
import org.apache.hc.client5.http.impl.auth.NTLMEngineImpl;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NTLMScheme
implements AuthScheme {
    private static final Logger LOG = LoggerFactory.getLogger(NTLMScheme.class);
    private final NTLMEngine engine;
    private State state;
    private String challenge;
    private NTCredentials credentials;

    public NTLMScheme(NTLMEngine engine) {
        Args.notNull(engine, "NTLM engine");
        this.engine = engine;
        this.state = State.UNINITIATED;
    }

    public NTLMScheme() {
        this(new NTLMEngineImpl());
    }

    @Override
    public String getName() {
        return "NTLM";
    }

    @Override
    public boolean isConnectionBased() {
        return true;
    }

    @Override
    public String getRealm() {
        return null;
    }

    @Override
    public void processChallenge(AuthChallenge authChallenge, HttpContext context) throws MalformedChallengeException {
        Args.notNull(authChallenge, "AuthChallenge");
        this.challenge = authChallenge.getValue();
        if (this.challenge == null || this.challenge.isEmpty()) {
            this.state = this.state == State.UNINITIATED ? State.CHALLENGE_RECEIVED : State.FAILED;
        } else {
            if (this.state.compareTo(State.MSG_TYPE1_GENERATED) < 0) {
                this.state = State.FAILED;
                throw new MalformedChallengeException("Out of sequence NTLM response message");
            }
            if (this.state == State.MSG_TYPE1_GENERATED) {
                this.state = State.MSG_TYPE2_RECEVIED;
            }
        }
    }

    @Override
    public boolean isResponseReady(HttpHost host, CredentialsProvider credentialsProvider, HttpContext context) throws AuthenticationException {
        Args.notNull(host, "Auth host");
        Args.notNull(credentialsProvider, "CredentialsProvider");
        AuthScope authScope = new AuthScope(host, null, this.getName());
        Credentials credentials = credentialsProvider.getCredentials(authScope, context);
        if (credentials instanceof NTCredentials) {
            this.credentials = (NTCredentials)credentials;
            return true;
        }
        LOG.debug("No credentials found for auth scope [{}]", (Object)authScope);
        return false;
    }

    @Override
    public Principal getPrincipal() {
        return this.credentials != null ? this.credentials.getUserPrincipal() : null;
    }

    @Override
    public String generateAuthResponse(HttpHost host, HttpRequest request, HttpContext context) throws AuthenticationException {
        String response;
        if (this.credentials == null) {
            throw new AuthenticationException("NT credentials not available");
        }
        if (this.state == State.FAILED) {
            throw new AuthenticationException("NTLM authentication failed");
        }
        if (this.state == State.CHALLENGE_RECEIVED) {
            response = this.engine.generateType1Msg(this.credentials.getNetbiosDomain(), this.credentials.getWorkstation());
            this.state = State.MSG_TYPE1_GENERATED;
        } else if (this.state == State.MSG_TYPE2_RECEVIED) {
            response = this.engine.generateType3Msg(this.credentials.getUserName(), this.credentials.getPassword(), this.credentials.getNetbiosDomain(), this.credentials.getWorkstation(), this.challenge);
            this.state = State.MSG_TYPE3_GENERATED;
        } else {
            throw new AuthenticationException("Unexpected state: " + (Object)((Object)this.state));
        }
        return "NTLM " + response;
    }

    @Override
    public boolean isChallengeComplete() {
        return this.state == State.MSG_TYPE3_GENERATED || this.state == State.FAILED;
    }

    public String toString() {
        return this.getName() + "{" + (Object)((Object)this.state) + " " + this.challenge + '}';
    }

    static enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        MSG_TYPE1_GENERATED,
        MSG_TYPE2_RECEVIED,
        MSG_TYPE3_GENERATED,
        FAILED;

    }
}

