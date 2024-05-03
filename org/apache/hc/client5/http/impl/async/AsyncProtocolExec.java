/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.AuthSupport;
import org.apache.hc.client5.http.impl.async.HttpProxyRequest;
import org.apache.hc.client5.http.impl.auth.HttpAuthenticator;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class AsyncProtocolExec
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncProtocolExec.class);
    private final HttpProcessor httpProcessor;
    private final AuthenticationStrategy targetAuthStrategy;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;

    AsyncProtocolExec(HttpProcessor httpProcessor, AuthenticationStrategy targetAuthStrategy, AuthenticationStrategy proxyAuthStrategy) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP protocol processor");
        this.targetAuthStrategy = Args.notNull(targetAuthStrategy, "Target authentication strategy");
        this.proxyAuthStrategy = Args.notNull(proxyAuthStrategy, "Proxy authentication strategy");
        this.authenticator = new HttpAuthenticator(LOG);
    }

    @Override
    public void execute(HttpRequest userRequest, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        CredentialsProvider credsProvider;
        HttpRequest request;
        if (Method.CONNECT.isSame(userRequest.getMethod())) {
            throw new ProtocolException("Direct execution of CONNECT is not allowed");
        }
        HttpRoute route = scope.route;
        HttpClientContext clientContext = scope.clientContext;
        if (route.getProxyHost() != null && !route.isTunnelled()) {
            try {
                URI uri = userRequest.getUri();
                uri = !uri.isAbsolute() ? URIUtils.rewriteURI(uri, route.getTargetHost(), true) : URIUtils.rewriteURI(uri);
                request = HttpProxyRequest.rewrite(userRequest, uri);
            } catch (URISyntaxException ex) {
                throw new ProtocolException("Invalid request URI: " + userRequest.getRequestUri(), ex);
            }
        } else {
            request = userRequest;
        }
        URIAuthority authority = request.getAuthority();
        if (authority != null && (credsProvider = clientContext.getCredentialsProvider()) instanceof CredentialsStore) {
            AuthSupport.extractFromAuthority(request.getScheme(), authority, (CredentialsStore)credsProvider);
        }
        AtomicBoolean challenged = new AtomicBoolean(false);
        this.internalExecute(challenged, request, entityProducer, scope, chain, asyncExecCallback);
    }

    private void internalExecute(final AtomicBoolean challenged, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        final String exchangeId = scope.exchangeId;
        final HttpRoute route = scope.route;
        final HttpClientContext clientContext = scope.clientContext;
        final AsyncExecRuntime execRuntime = scope.execRuntime;
        HttpHost target = route.getTargetHost();
        HttpHost proxy = route.getProxyHost();
        final AuthExchange targetAuthExchange = clientContext.getAuthExchange(target);
        final AuthExchange proxyAuthExchange = proxy != null ? clientContext.getAuthExchange(proxy) : new AuthExchange();
        clientContext.setAttribute("http.route", route);
        clientContext.setAttribute("http.request", request);
        this.httpProcessor.process(request, (EntityDetails)entityProducer, (HttpContext)clientContext);
        if (!request.containsHeader("Authorization")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: target auth state: {}", (Object)exchangeId, (Object)targetAuthExchange.getState());
            }
            this.authenticator.addAuthResponse(target, ChallengeType.TARGET, request, targetAuthExchange, clientContext);
        }
        if (!request.containsHeader("Proxy-Authorization") && !route.isTunnelled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: proxy auth state: {}", (Object)exchangeId, (Object)proxyAuthExchange.getState());
            }
            this.authenticator.addAuthResponse(proxy, ChallengeType.PROXY, request, proxyAuthExchange, clientContext);
        }
        chain.proceed(request, entityProducer, scope, new AsyncExecCallback(){

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                clientContext.setAttribute("http.response", response);
                AsyncProtocolExec.this.httpProcessor.process(response, entityDetails, (HttpContext)clientContext);
                if (Method.TRACE.isSame(request.getMethod())) {
                    return asyncExecCallback.handleResponse(response, entityDetails);
                }
                if (AsyncProtocolExec.this.needAuthentication(targetAuthExchange, proxyAuthExchange, route, request, response, clientContext)) {
                    challenged.set(true);
                    return null;
                }
                challenged.set(false);
                return asyncExecCallback.handleResponse(response, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                asyncExecCallback.handleInformationResponse(response);
            }

            @Override
            public void completed() {
                if (!execRuntime.isEndpointConnected()) {
                    if (proxyAuthExchange.getState() == AuthExchange.State.SUCCESS && proxyAuthExchange.isConnectionBased()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: resetting proxy auth state", (Object)exchangeId);
                        }
                        proxyAuthExchange.reset();
                    }
                    if (targetAuthExchange.getState() == AuthExchange.State.SUCCESS && targetAuthExchange.isConnectionBased()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: resetting target auth state", (Object)exchangeId);
                        }
                        targetAuthExchange.reset();
                    }
                }
                if (challenged.get()) {
                    if (entityProducer != null && !entityProducer.isRepeatable()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: cannot retry non-repeatable request", (Object)exchangeId);
                        }
                        asyncExecCallback.completed();
                    } else {
                        HttpRequest original = scope.originalRequest;
                        request.setHeaders(new Header[0]);
                        Iterator<Header> it = original.headerIterator();
                        while (it.hasNext()) {
                            request.addHeader(it.next());
                        }
                        try {
                            if (entityProducer != null) {
                                entityProducer.releaseResources();
                            }
                            AsyncProtocolExec.this.internalExecute(challenged, request, entityProducer, scope, chain, asyncExecCallback);
                        } catch (IOException | HttpException ex) {
                            asyncExecCallback.failed(ex);
                        }
                    }
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                if (cause instanceof IOException || cause instanceof RuntimeException) {
                    if (proxyAuthExchange.isConnectionBased()) {
                        proxyAuthExchange.reset();
                    }
                    if (targetAuthExchange.isConnectionBased()) {
                        targetAuthExchange.reset();
                    }
                }
                asyncExecCallback.failed(cause);
            }
        });
    }

    private boolean needAuthentication(AuthExchange targetAuthExchange, AuthExchange proxyAuthExchange, HttpRoute route, HttpRequest request, HttpResponse response, HttpClientContext context) {
        RequestConfig config = context.getRequestConfig();
        if (config.isAuthenticationEnabled()) {
            HttpHost target = AuthSupport.resolveAuthTarget(request, route);
            boolean targetAuthRequested = this.authenticator.isChallenged(target, ChallengeType.TARGET, response, targetAuthExchange, context);
            HttpHost proxy = route.getProxyHost();
            if (proxy == null) {
                proxy = route.getTargetHost();
            }
            boolean proxyAuthRequested = this.authenticator.isChallenged(proxy, ChallengeType.PROXY, response, proxyAuthExchange, context);
            if (targetAuthRequested) {
                return this.authenticator.updateAuthState(target, ChallengeType.TARGET, response, this.targetAuthStrategy, targetAuthExchange, context);
            }
            if (proxyAuthRequested) {
                return this.authenticator.updateAuthState(proxy, ChallengeType.PROXY, response, this.proxyAuthStrategy, proxyAuthExchange, context);
            }
        }
        return false;
    }
}

