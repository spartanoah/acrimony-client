/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.net.URI;
import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RedirectException;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.RequestEntityProxy;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class RedirectExec
implements ExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RedirectExec.class);
    private final RedirectStrategy redirectStrategy;
    private final HttpRoutePlanner routePlanner;

    public RedirectExec(HttpRoutePlanner routePlanner, RedirectStrategy redirectStrategy) {
        Args.notNull(routePlanner, "HTTP route planner");
        Args.notNull(redirectStrategy, "HTTP redirect strategy");
        this.routePlanner = routePlanner;
        this.redirectStrategy = redirectStrategy;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        HttpClientContext context = scope.clientContext;
        RedirectLocations redirectLocations = context.getRedirectLocations();
        if (redirectLocations == null) {
            redirectLocations = new RedirectLocations();
            context.setAttribute("http.protocol.redirect-locations", redirectLocations);
        }
        redirectLocations.clear();
        RequestConfig config = context.getRequestConfig();
        int maxRedirects = config.getMaxRedirects() > 0 ? config.getMaxRedirects() : 50;
        HttpMessage currentRequest = request;
        ExecChain.Scope currentScope = scope;
        int redirectCount = 0;
        while (true) {
            String exchangeId = currentScope.exchangeId;
            ClassicHttpResponse response = chain.proceed((ClassicHttpRequest)currentRequest, currentScope);
            try {
                HttpRoute newRoute;
                if (!config.isRedirectsEnabled()) return response;
                if (!this.redirectStrategy.isRedirected(request, response, context)) return response;
                HttpEntity requestEntity = request.getEntity();
                if (requestEntity != null && !requestEntity.isRepeatable()) {
                    if (!LOG.isDebugEnabled()) return response;
                    LOG.debug("{}: cannot redirect non-repeatable request", (Object)exchangeId);
                    return response;
                }
                if (redirectCount >= maxRedirects) {
                    throw new RedirectException("Maximum redirects (" + maxRedirects + ") exceeded");
                }
                ++redirectCount;
                URI redirectUri = this.redirectStrategy.getLocationURI((HttpRequest)currentRequest, response, context);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: redirect requested to location '{}'", (Object)exchangeId, (Object)redirectUri);
                }
                if (!config.isCircularRedirectsAllowed() && redirectLocations.contains(redirectUri)) {
                    throw new CircularRedirectException("Circular redirect to '" + redirectUri + "'");
                }
                redirectLocations.add(redirectUri);
                ClassicHttpRequest originalRequest = scope.originalRequest;
                HttpMessage redirect = null;
                int statusCode = response.getCode();
                switch (statusCode) {
                    case 301: 
                    case 302: {
                        if (!Method.POST.isSame(request.getMethod())) break;
                        redirect = new HttpGet(redirectUri);
                        break;
                    }
                    case 303: {
                        if (Method.GET.isSame(request.getMethod()) || Method.HEAD.isSame(request.getMethod())) break;
                        redirect = new HttpGet(redirectUri);
                        break;
                    }
                }
                if (redirect == null) {
                    redirect = new BasicClassicHttpRequest(originalRequest.getMethod(), redirectUri);
                    redirect.setEntity(originalRequest.getEntity());
                }
                redirect.setHeaders(originalRequest.getHeaders());
                HttpHost newTarget = URIUtils.extractHost(redirectUri);
                if (newTarget == null) {
                    throw new ProtocolException("Redirect URI does not specify a valid host name: " + redirectUri);
                }
                HttpRoute currentRoute = currentScope.route;
                if (!LangUtils.equals(currentRoute.getTargetHost(), newTarget) && !LangUtils.equals(currentRoute, newRoute = this.routePlanner.determineRoute(newTarget, context))) {
                    AuthExchange proxyAuthExchange;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: new route required", (Object)exchangeId);
                    }
                    AuthExchange targetAuthExchange = context.getAuthExchange(currentRoute.getTargetHost());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: resetting target auth state", (Object)exchangeId);
                    }
                    targetAuthExchange.reset();
                    if (currentRoute.getProxyHost() != null && (proxyAuthExchange = context.getAuthExchange(currentRoute.getProxyHost())).isConnectionBased()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: resetting proxy auth state", (Object)exchangeId);
                        }
                        proxyAuthExchange.reset();
                    }
                    currentScope = new ExecChain.Scope(currentScope.exchangeId, newRoute, currentScope.originalRequest, currentScope.execRuntime, currentScope.clientContext);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: redirecting to '{}' via {}", exchangeId, redirectUri, currentRoute);
                }
                currentRequest = redirect;
                RequestEntityProxy.enhance(currentRequest);
                EntityUtils.consume(response.getEntity());
                response.close();
            } catch (IOException | RuntimeException ex) {
                response.close();
                throw ex;
            } catch (HttpException ex) {
                try {
                    EntityUtils.consume(response.getEntity());
                    throw ex;
                } catch (IOException ioex) {
                    if (!LOG.isDebugEnabled()) throw ex;
                    LOG.debug("{}: I/O error while releasing connection", (Object)exchangeId, (Object)ioex);
                    throw ex;
                } finally {
                    response.close();
                }
            }
        }
    }
}

