/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthStateCacheable;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.MalformedChallengeException;
import org.apache.hc.client5.http.impl.auth.AuthChallengeParser;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class HttpAuthenticator {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(HttpAuthenticator.class);
    private final Logger log;
    private final AuthChallengeParser parser;

    @Internal
    public HttpAuthenticator(Logger log) {
        this.log = log != null ? log : DEFAULT_LOGGER;
        this.parser = new AuthChallengeParser();
    }

    public HttpAuthenticator() {
        this(null);
    }

    public boolean isChallenged(HttpHost host, ChallengeType challengeType, HttpResponse response, AuthExchange authExchange, HttpContext context) {
        int challengeCode;
        switch (challengeType) {
            case TARGET: {
                challengeCode = 401;
                break;
            }
            case PROXY: {
                challengeCode = 407;
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected challenge type: " + (Object)((Object)challengeType));
            }
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        if (response.getCode() == challengeCode) {
            this.log.debug("Authentication required");
            if (authExchange.getState() == AuthExchange.State.SUCCESS) {
                this.clearCache(host, clientContext);
            }
            return true;
        }
        switch (authExchange.getState()) {
            case CHALLENGED: 
            case HANDSHAKE: {
                this.log.debug("Authentication succeeded");
                authExchange.setState(AuthExchange.State.SUCCESS);
                this.updateCache(host, authExchange.getAuthScheme(), clientContext);
                break;
            }
            case SUCCESS: {
                break;
            }
            default: {
                authExchange.setState(AuthExchange.State.UNCHALLENGED);
            }
        }
        return false;
    }

    public boolean updateAuthState(HttpHost host, ChallengeType challengeType, HttpResponse response, AuthenticationStrategy authStrategy, AuthExchange authExchange, HttpContext context) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} requested authentication", (Object)host.toHostString());
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Header[] headers = response.getHeaders(challengeType == ChallengeType.PROXY ? "Proxy-Authenticate" : "WWW-Authenticate");
        HashMap<String, AuthChallenge> challengeMap = new HashMap<String, AuthChallenge>();
        for (Header header : headers) {
            List<AuthChallenge> authChallenges;
            int pos;
            CharArrayBuffer buffer;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader)header).getBuffer();
                pos = ((FormattedHeader)header).getValuePos();
            } else {
                String s = header.getValue();
                if (s == null) continue;
                buffer = new CharArrayBuffer(s.length());
                buffer.append(s);
                pos = 0;
            }
            ParserCursor cursor = new ParserCursor(pos, buffer.length());
            try {
                authChallenges = this.parser.parse(challengeType, buffer, cursor);
            } catch (ParseException ex) {
                if (!this.log.isWarnEnabled()) continue;
                this.log.warn("Malformed challenge: {}", (Object)header.getValue());
                continue;
            }
            for (AuthChallenge authChallenge : authChallenges) {
                String schemeName = authChallenge.getSchemeName().toLowerCase(Locale.ROOT);
                if (challengeMap.containsKey(schemeName)) continue;
                challengeMap.put(schemeName, authChallenge);
            }
        }
        if (challengeMap.isEmpty()) {
            this.log.debug("Response contains no valid authentication challenges");
            this.clearCache(host, clientContext);
            authExchange.reset();
            return false;
        }
        switch (authExchange.getState()) {
            case FAILURE: {
                return false;
            }
            case SUCCESS: {
                authExchange.reset();
                break;
            }
            case CHALLENGED: 
            case HANDSHAKE: {
                Asserts.notNull(authExchange.getAuthScheme(), "AuthScheme");
            }
            case UNCHALLENGED: {
                AuthScheme authScheme = authExchange.getAuthScheme();
                if (authScheme == null) break;
                String schemeName = authScheme.getName();
                AuthChallenge challenge = (AuthChallenge)challengeMap.get(schemeName.toLowerCase(Locale.ROOT));
                if (challenge != null) {
                    this.log.debug("Authorization challenge processed");
                    try {
                        authScheme.processChallenge(challenge, context);
                    } catch (MalformedChallengeException ex) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn(ex.getMessage());
                        }
                        this.clearCache(host, clientContext);
                        authExchange.reset();
                        return false;
                    }
                    if (authScheme.isChallengeComplete()) {
                        this.log.debug("Authentication failed");
                        this.clearCache(host, clientContext);
                        authExchange.reset();
                        authExchange.setState(AuthExchange.State.FAILURE);
                        return false;
                    }
                    authExchange.setState(AuthExchange.State.HANDSHAKE);
                    return true;
                }
                authExchange.reset();
            }
        }
        List<AuthScheme> preferredSchemes = authStrategy.select(challengeType, challengeMap, context);
        CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
        if (credsProvider == null) {
            this.log.debug("Credentials provider not set in the context");
            return false;
        }
        LinkedList<AuthScheme> authOptions = new LinkedList<AuthScheme>();
        this.log.debug("Selecting authentication options");
        for (AuthScheme authScheme : preferredSchemes) {
            try {
                String schemeName = authScheme.getName();
                AuthChallenge challenge = (AuthChallenge)challengeMap.get(schemeName.toLowerCase(Locale.ROOT));
                authScheme.processChallenge(challenge, context);
                if (!authScheme.isResponseReady(host, credsProvider, context)) continue;
                authOptions.add(authScheme);
            } catch (AuthenticationException | MalformedChallengeException ex) {
                if (!this.log.isWarnEnabled()) continue;
                this.log.warn(ex.getMessage());
            }
        }
        if (!authOptions.isEmpty()) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Selected authentication options: {}", (Object)authOptions);
            }
            authExchange.reset();
            authExchange.setState(AuthExchange.State.CHALLENGED);
            authExchange.setOptions(authOptions);
            return true;
        }
        return false;
    }

    public void addAuthResponse(HttpHost host, ChallengeType challengeType, HttpRequest request, AuthExchange authExchange, HttpContext context) {
        block14: {
            AuthScheme authScheme = authExchange.getAuthScheme();
            switch (authExchange.getState()) {
                case FAILURE: {
                    return;
                }
                case SUCCESS: {
                    Asserts.notNull(authScheme, "AuthScheme");
                    if (!authScheme.isConnectionBased()) break;
                    return;
                }
                case HANDSHAKE: {
                    Asserts.notNull(authScheme, "AuthScheme");
                    break;
                }
                case CHALLENGED: {
                    Queue<AuthScheme> authOptions = authExchange.getAuthOptions();
                    if (authOptions != null) {
                        while (!authOptions.isEmpty()) {
                            authScheme = authOptions.remove();
                            authExchange.select(authScheme);
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("Generating response to an authentication challenge using {} scheme", (Object)authScheme.getName());
                            }
                            try {
                                String authResponse = authScheme.generateAuthResponse(host, request, context);
                                BasicHeader header = new BasicHeader(challengeType == ChallengeType.TARGET ? "Authorization" : "Proxy-Authorization", authResponse);
                                request.addHeader(header);
                                break;
                            } catch (AuthenticationException ex) {
                                if (!this.log.isWarnEnabled()) continue;
                                this.log.warn("{} authentication error: {}", (Object)authScheme, (Object)ex.getMessage());
                            }
                        }
                        return;
                    }
                    Asserts.notNull(authScheme, "AuthScheme");
                }
            }
            if (authScheme != null) {
                try {
                    String authResponse = authScheme.generateAuthResponse(host, request, context);
                    BasicHeader header = new BasicHeader(challengeType == ChallengeType.TARGET ? "Authorization" : "Proxy-Authorization", authResponse);
                    request.addHeader(header);
                } catch (AuthenticationException ex) {
                    if (!this.log.isErrorEnabled()) break block14;
                    this.log.error("{} authentication error: {}", (Object)authScheme, (Object)ex.getMessage());
                }
            }
        }
    }

    private void updateCache(HttpHost host, AuthScheme authScheme, HttpClientContext clientContext) {
        boolean cachable;
        boolean bl = cachable = authScheme.getClass().getAnnotation(AuthStateCacheable.class) != null;
        if (cachable) {
            AuthCache authCache = clientContext.getAuthCache();
            if (authCache == null) {
                authCache = new BasicAuthCache();
                clientContext.setAuthCache(authCache);
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Caching '{}' auth scheme for {}", (Object)authScheme.getName(), (Object)host);
            }
            authCache.put(host, authScheme);
        }
    }

    private void clearCache(HttpHost host, HttpClientContext clientContext) {
        AuthCache authCache = clientContext.getAuthCache();
        if (authCache != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Clearing cached auth scheme for {}", (Object)host);
            }
            authCache.remove(host);
        }
    }
}

