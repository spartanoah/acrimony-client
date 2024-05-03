/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.io.InterruptedIOException;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteTracker;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.TunnelRefusedException;
import org.apache.hc.client5.http.impl.auth.HttpAuthenticator;
import org.apache.hc.client5.http.impl.routing.BasicRouteDirector;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRouteDirector;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class AsyncConnectExec
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncConnectExec.class);
    private final HttpProcessor proxyHttpProcessor;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;
    private final HttpRouteDirector routeDirector;

    public AsyncConnectExec(HttpProcessor proxyHttpProcessor, AuthenticationStrategy proxyAuthStrategy) {
        Args.notNull(proxyHttpProcessor, "Proxy HTTP processor");
        Args.notNull(proxyAuthStrategy, "Proxy authentication strategy");
        this.proxyHttpProcessor = proxyHttpProcessor;
        this.proxyAuthStrategy = proxyAuthStrategy;
        this.authenticator = new HttpAuthenticator(LOG);
        this.routeDirector = new BasicRouteDirector();
    }

    @Override
    public void execute(final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        String exchangeId = scope.exchangeId;
        HttpRoute route = scope.route;
        CancellableDependency cancellableDependency = scope.cancellableDependency;
        HttpClientContext clientContext = scope.clientContext;
        AsyncExecRuntime execRuntime = scope.execRuntime;
        final State state = new State(route);
        if (!execRuntime.isEndpointAcquired()) {
            Object userToken = clientContext.getUserToken();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: acquiring connection with route {}", (Object)exchangeId, (Object)route);
            }
            cancellableDependency.setDependency(execRuntime.acquireEndpoint(exchangeId, route, userToken, clientContext, new FutureCallback<AsyncExecRuntime>(){

                @Override
                public void completed(AsyncExecRuntime execRuntime) {
                    if (execRuntime.isEndpointConnected()) {
                        try {
                            chain.proceed(request, entityProducer, scope, asyncExecCallback);
                        } catch (IOException | HttpException ex) {
                            asyncExecCallback.failed(ex);
                        }
                    } else {
                        AsyncConnectExec.this.proceedToNextHop(state, request, entityProducer, scope, chain, asyncExecCallback);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    asyncExecCallback.failed(ex);
                }

                @Override
                public void cancelled() {
                    asyncExecCallback.failed(new InterruptedIOException());
                }
            }));
        } else if (execRuntime.isEndpointConnected()) {
            try {
                chain.proceed(request, entityProducer, scope, asyncExecCallback);
            } catch (IOException | HttpException ex) {
                asyncExecCallback.failed(ex);
            }
        } else {
            this.proceedToNextHop(state, request, entityProducer, scope, chain, asyncExecCallback);
        }
    }

    private void proceedToNextHop(final State state, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) {
        int step;
        final RouteTracker tracker = state.tracker;
        final String exchangeId = scope.exchangeId;
        final HttpRoute route = scope.route;
        AsyncExecRuntime execRuntime = scope.execRuntime;
        CancellableDependency operation = scope.cancellableDependency;
        HttpClientContext clientContext = scope.clientContext;
        block13: do {
            HttpRoute fact = tracker.toRoute();
            step = this.routeDirector.nextStep(route, fact);
            switch (step) {
                case 1: {
                    operation.setDependency(execRuntime.connectEndpoint(clientContext, new FutureCallback<AsyncExecRuntime>(){

                        @Override
                        public void completed(AsyncExecRuntime execRuntime) {
                            tracker.connectTarget(route.isSecure());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: connected to target", (Object)exchangeId);
                            }
                            AsyncConnectExec.this.proceedToNextHop(state, request, entityProducer, scope, chain, asyncExecCallback);
                        }

                        @Override
                        public void failed(Exception ex) {
                            asyncExecCallback.failed(ex);
                        }

                        @Override
                        public void cancelled() {
                            asyncExecCallback.failed(new InterruptedIOException());
                        }
                    }));
                    return;
                }
                case 2: {
                    operation.setDependency(execRuntime.connectEndpoint(clientContext, new FutureCallback<AsyncExecRuntime>(){

                        @Override
                        public void completed(AsyncExecRuntime execRuntime) {
                            HttpHost proxy = route.getProxyHost();
                            tracker.connectProxy(proxy, route.isSecure() && !route.isTunnelled());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: connected to proxy", (Object)exchangeId);
                            }
                            AsyncConnectExec.this.proceedToNextHop(state, request, entityProducer, scope, chain, asyncExecCallback);
                        }

                        @Override
                        public void failed(Exception ex) {
                            asyncExecCallback.failed(ex);
                        }

                        @Override
                        public void cancelled() {
                            asyncExecCallback.failed(new InterruptedIOException());
                        }
                    }));
                    return;
                }
                case 3: {
                    try {
                        HttpHost proxy = route.getProxyHost();
                        HttpHost target = route.getTargetHost();
                        this.createTunnel(state, proxy, target, scope, chain, new AsyncExecCallback(){

                            @Override
                            public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                                return asyncExecCallback.handleResponse(response, entityDetails);
                            }

                            @Override
                            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                                asyncExecCallback.handleInformationResponse(response);
                            }

                            @Override
                            public void completed() {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("{}: tunnel to target created", (Object)exchangeId);
                                }
                                tracker.tunnelTarget(false);
                                AsyncConnectExec.this.proceedToNextHop(state, request, entityProducer, scope, chain, asyncExecCallback);
                            }

                            @Override
                            public void failed(Exception cause) {
                                asyncExecCallback.failed(cause);
                            }
                        });
                    } catch (IOException | HttpException ex) {
                        asyncExecCallback.failed(ex);
                    }
                    return;
                }
                case 4: {
                    asyncExecCallback.failed(new HttpException("Proxy chains are not supported"));
                    return;
                }
                case 5: {
                    execRuntime.upgradeTls(clientContext);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: upgraded to TLS", (Object)exchangeId);
                    }
                    tracker.layerProtocol(route.isSecure());
                    break;
                }
                case -1: {
                    asyncExecCallback.failed(new HttpException("Unable to establish route: planned = " + route + "; current = " + fact));
                    return;
                }
                case 0: {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: route fully established", (Object)exchangeId);
                    }
                    try {
                        chain.proceed(request, entityProducer, scope, asyncExecCallback);
                    } catch (IOException | HttpException ex) {
                        asyncExecCallback.failed(ex);
                    }
                    continue block13;
                }
                default: {
                    throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
                }
            }
        } while (step > 0);
    }

    private void createTunnel(final State state, final HttpHost proxy, final HttpHost nextHop, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        final AsyncExecRuntime execRuntime = scope.execRuntime;
        final HttpClientContext clientContext = scope.clientContext;
        final AuthExchange proxyAuthExchange = proxy != null ? clientContext.getAuthExchange(proxy) : new AuthExchange();
        BasicHttpRequest connect = new BasicHttpRequest("CONNECT", nextHop, nextHop.toHostString());
        connect.setVersion(HttpVersion.HTTP_1_1);
        this.proxyHttpProcessor.process(connect, null, (HttpContext)clientContext);
        this.authenticator.addAuthResponse(proxy, ChallengeType.PROXY, connect, proxyAuthExchange, clientContext);
        chain.proceed(connect, null, scope, new AsyncExecCallback(){

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                clientContext.setAttribute("http.response", response);
                AsyncConnectExec.this.proxyHttpProcessor.process(response, entityDetails, (HttpContext)clientContext);
                int status = response.getCode();
                if (status < 200) {
                    throw new HttpException("Unexpected response to CONNECT request: " + new StatusLine(response));
                }
                if (AsyncConnectExec.this.needAuthentication(proxyAuthExchange, proxy, response, clientContext)) {
                    state.challenged = true;
                    return null;
                }
                state.challenged = false;
                if (status >= 300) {
                    state.tunnelRefused = true;
                    return asyncExecCallback.handleResponse(response, entityDetails);
                }
                return null;
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
            }

            @Override
            public void completed() {
                if (!execRuntime.isEndpointConnected()) {
                    state.tracker.reset();
                }
                if (state.challenged) {
                    try {
                        AsyncConnectExec.this.createTunnel(state, proxy, nextHop, scope, chain, asyncExecCallback);
                    } catch (IOException | HttpException ex) {
                        asyncExecCallback.failed(ex);
                    }
                } else if (state.tunnelRefused) {
                    asyncExecCallback.failed(new TunnelRefusedException("Tunnel refused", null));
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                asyncExecCallback.failed(cause);
            }
        });
    }

    private boolean needAuthentication(AuthExchange proxyAuthExchange, HttpHost proxy, HttpResponse response, HttpClientContext context) {
        boolean proxyAuthRequested;
        RequestConfig config = context.getRequestConfig();
        if (config.isAuthenticationEnabled() && (proxyAuthRequested = this.authenticator.isChallenged(proxy, ChallengeType.PROXY, response, proxyAuthExchange, context))) {
            return this.authenticator.updateAuthState(proxy, ChallengeType.PROXY, response, this.proxyAuthStrategy, proxyAuthExchange, context);
        }
        return false;
    }

    static class State {
        final RouteTracker tracker;
        volatile boolean challenged;
        volatile boolean tunnelRefused;

        State(HttpRoute route) {
            this.tracker = new RouteTracker(route);
        }
    }
}

