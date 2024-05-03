/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.net.URI;
import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RedirectException;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.util.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class AsyncRedirectExec
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncRedirectExec.class);
    private final HttpRoutePlanner routePlanner;
    private final RedirectStrategy redirectStrategy;

    AsyncRedirectExec(HttpRoutePlanner routePlanner, RedirectStrategy redirectStrategy) {
        this.routePlanner = routePlanner;
        this.redirectStrategy = redirectStrategy;
    }

    private void internalExecute(final State state, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        final HttpRequest request = state.currentRequest;
        AsyncEntityProducer entityProducer = state.currentEntityProducer;
        final AsyncExecChain.Scope scope = state.currentScope;
        final HttpClientContext clientContext = scope.clientContext;
        final String exchangeId = scope.exchangeId;
        final HttpRoute currentRoute = scope.route;
        chain.proceed(request, entityProducer, scope, new AsyncExecCallback(){

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                state.redirectURI = null;
                RequestConfig config = clientContext.getRequestConfig();
                if (config.isRedirectsEnabled() && AsyncRedirectExec.this.redirectStrategy.isRedirected(request, response, clientContext)) {
                    HttpRoute newRoute;
                    if (state.redirectCount >= state.maxRedirects) {
                        throw new RedirectException("Maximum redirects (" + state.maxRedirects + ") exceeded");
                    }
                    ++state.redirectCount;
                    URI redirectUri = AsyncRedirectExec.this.redirectStrategy.getLocationURI(request, response, clientContext);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: redirect requested to location '{}'", (Object)exchangeId, (Object)redirectUri);
                    }
                    if (!config.isCircularRedirectsAllowed() && state.redirectLocations.contains(redirectUri)) {
                        throw new CircularRedirectException("Circular redirect to '" + redirectUri + "'");
                    }
                    state.redirectLocations.add(redirectUri);
                    int statusCode = response.getCode();
                    state.currentRequest = null;
                    switch (statusCode) {
                        case 301: 
                        case 302: {
                            if (!Method.POST.isSame(request.getMethod())) break;
                            state.currentRequest = new BasicHttpRequest(Method.GET, redirectUri);
                            state.currentEntityProducer = null;
                            break;
                        }
                        case 303: {
                            if (Method.GET.isSame(request.getMethod()) || Method.HEAD.isSame(request.getMethod())) break;
                            state.currentRequest = new BasicHttpRequest(Method.GET, redirectUri);
                            state.currentEntityProducer = null;
                        }
                    }
                    if (state.currentRequest == null) {
                        state.currentRequest = new BasicHttpRequest(request.getMethod(), redirectUri);
                    }
                    state.currentRequest.setHeaders(scope.originalRequest.getHeaders());
                    HttpHost newTarget = URIUtils.extractHost(redirectUri);
                    if (newTarget == null) {
                        throw new ProtocolException("Redirect URI does not specify a valid host name: " + redirectUri);
                    }
                    state.reroute = false;
                    state.redirectURI = redirectUri;
                    if (!LangUtils.equals(currentRoute.getTargetHost(), newTarget) && !LangUtils.equals(currentRoute, newRoute = AsyncRedirectExec.this.routePlanner.determineRoute(newTarget, clientContext))) {
                        AuthExchange proxyAuthExchange;
                        state.reroute = true;
                        AuthExchange targetAuthExchange = clientContext.getAuthExchange(currentRoute.getTargetHost());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: resetting target auth state", (Object)exchangeId);
                        }
                        targetAuthExchange.reset();
                        if (currentRoute.getProxyHost() != null && (proxyAuthExchange = clientContext.getAuthExchange(currentRoute.getProxyHost())).isConnectionBased()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: resetting proxy auth state", (Object)exchangeId);
                            }
                            proxyAuthExchange.reset();
                        }
                        state.currentScope = new AsyncExecChain.Scope(scope.exchangeId, newRoute, scope.originalRequest, scope.cancellableDependency, clientContext, scope.execRuntime);
                    }
                }
                if (state.redirectURI != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: redirecting to '{}' via {}", exchangeId, state.redirectURI, currentRoute);
                    }
                    return null;
                }
                return asyncExecCallback.handleResponse(response, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                asyncExecCallback.handleInformationResponse(response);
            }

            @Override
            public void completed() {
                if (state.redirectURI == null) {
                    asyncExecCallback.completed();
                } else {
                    AsyncEntityProducer entityProducer = state.currentEntityProducer;
                    if (entityProducer != null) {
                        entityProducer.releaseResources();
                    }
                    if (entityProducer != null && !entityProducer.isRepeatable()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: cannot redirect non-repeatable request", (Object)exchangeId);
                        }
                        asyncExecCallback.completed();
                    } else {
                        try {
                            if (state.reroute) {
                                scope.execRuntime.releaseEndpoint();
                            }
                            AsyncRedirectExec.this.internalExecute(state, chain, asyncExecCallback);
                        } catch (IOException | HttpException ex) {
                            asyncExecCallback.failed(ex);
                        }
                    }
                }
            }

            @Override
            public void failed(Exception cause) {
                asyncExecCallback.failed(cause);
            }
        });
    }

    @Override
    public void execute(HttpRequest request, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        HttpClientContext clientContext = scope.clientContext;
        RedirectLocations redirectLocations = clientContext.getRedirectLocations();
        if (redirectLocations == null) {
            redirectLocations = new RedirectLocations();
            clientContext.setAttribute("http.protocol.redirect-locations", redirectLocations);
        }
        redirectLocations.clear();
        RequestConfig config = clientContext.getRequestConfig();
        State state = new State();
        state.maxRedirects = config.getMaxRedirects() > 0 ? config.getMaxRedirects() : 50;
        state.redirectCount = 0;
        state.currentRequest = request;
        state.currentEntityProducer = entityProducer;
        state.redirectLocations = redirectLocations;
        state.currentScope = scope;
        this.internalExecute(state, chain, asyncExecCallback);
    }

    private static class State {
        volatile URI redirectURI;
        volatile int maxRedirects;
        volatile int redirectCount;
        volatile HttpRequest currentRequest;
        volatile AsyncEntityProducer currentEntityProducer;
        volatile RedirectLocations redirectLocations;
        volatile AsyncExecChain.Scope currentScope;
        volatile boolean reroute;

        private State() {
        }
    }
}

