/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteTracker;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.TunnelRefusedException;
import org.apache.hc.client5.http.impl.auth.HttpAuthenticator;
import org.apache.hc.client5.http.impl.routing.BasicRouteDirector;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRouteDirector;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class ConnectExec
implements ExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectExec.class);
    private final ConnectionReuseStrategy reuseStrategy;
    private final HttpProcessor proxyHttpProcessor;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;
    private final HttpRouteDirector routeDirector;

    public ConnectExec(ConnectionReuseStrategy reuseStrategy, HttpProcessor proxyHttpProcessor, AuthenticationStrategy proxyAuthStrategy) {
        Args.notNull(reuseStrategy, "Connection reuse strategy");
        Args.notNull(proxyHttpProcessor, "Proxy HTTP processor");
        Args.notNull(proxyAuthStrategy, "Proxy authentication strategy");
        this.reuseStrategy = reuseStrategy;
        this.proxyHttpProcessor = proxyHttpProcessor;
        this.proxyAuthStrategy = proxyAuthStrategy;
        this.authenticator = new HttpAuthenticator(LOG);
        this.routeDirector = new BasicRouteDirector();
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        String exchangeId = scope.exchangeId;
        HttpRoute route = scope.route;
        HttpClientContext context = scope.clientContext;
        ExecRuntime execRuntime = scope.execRuntime;
        if (!execRuntime.isEndpointAcquired()) {
            Object userToken = context.getUserToken();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: acquiring connection with route {}", (Object)exchangeId, (Object)route);
            }
            execRuntime.acquireEndpoint(exchangeId, route, userToken, context);
        }
        try {
            if (!execRuntime.isEndpointConnected()) {
                int step;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: opening connection {}", (Object)exchangeId, (Object)route);
                }
                RouteTracker tracker = new RouteTracker(route);
                do {
                    HttpRoute fact = tracker.toRoute();
                    step = this.routeDirector.nextStep(route, fact);
                    switch (step) {
                        case 1: {
                            execRuntime.connectEndpoint(context);
                            tracker.connectTarget(route.isSecure());
                            break;
                        }
                        case 2: {
                            execRuntime.connectEndpoint(context);
                            HttpHost proxy = route.getProxyHost();
                            tracker.connectProxy(proxy, route.isSecure() && !route.isTunnelled());
                            break;
                        }
                        case 3: {
                            boolean secure = this.createTunnelToTarget(exchangeId, route, request, execRuntime, context);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: tunnel to target created.", (Object)exchangeId);
                            }
                            tracker.tunnelTarget(secure);
                            break;
                        }
                        case 4: {
                            int hop = fact.getHopCount() - 1;
                            boolean secure = this.createTunnelToProxy(route, hop, context);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: tunnel to proxy created.", (Object)exchangeId);
                            }
                            tracker.tunnelProxy(route.getHopTarget(hop), secure);
                            break;
                        }
                        case 5: {
                            execRuntime.upgradeTls(context);
                            tracker.layerProtocol(route.isSecure());
                            break;
                        }
                        case -1: {
                            throw new HttpException("Unable to establish route: planned = " + route + "; current = " + fact);
                        }
                        case 0: {
                            break;
                        }
                        default: {
                            throw new IllegalStateException("Unknown step indicator " + step + " from RouteDirector.");
                        }
                    }
                } while (step > 0);
            }
            return chain.proceed(request, scope);
        } catch (IOException | RuntimeException | HttpException ex) {
            execRuntime.discardEndpoint();
            throw ex;
        }
    }

    private boolean createTunnelToTarget(String exchangeId, HttpRoute route, HttpRequest request, ExecRuntime execRuntime, HttpClientContext context) throws HttpException, IOException {
        HttpEntity entity;
        int status;
        RequestConfig config = context.getRequestConfig();
        HttpHost target = route.getTargetHost();
        HttpHost proxy = route.getProxyHost();
        AuthExchange proxyAuthExchange = context.getAuthExchange(proxy);
        HttpResponse response = null;
        String authority = target.toHostString();
        BasicClassicHttpRequest connect = new BasicClassicHttpRequest("CONNECT", target, authority);
        connect.setVersion(HttpVersion.HTTP_1_1);
        this.proxyHttpProcessor.process(connect, null, (HttpContext)context);
        while (response == null) {
            connect.removeHeaders("Proxy-Authorization");
            this.authenticator.addAuthResponse(proxy, ChallengeType.PROXY, connect, proxyAuthExchange, context);
            response = execRuntime.execute(exchangeId, connect, context);
            this.proxyHttpProcessor.process(response, (EntityDetails)response.getEntity(), (HttpContext)context);
            status = response.getCode();
            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " + new StatusLine(response));
            }
            if (!config.isAuthenticationEnabled() || !this.authenticator.isChallenged(proxy, ChallengeType.PROXY, response, proxyAuthExchange, context) || !this.authenticator.updateAuthState(proxy, ChallengeType.PROXY, response, this.proxyAuthStrategy, proxyAuthExchange, context)) continue;
            if (this.reuseStrategy.keepAlive(request, response, context)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: connection kept alive", (Object)exchangeId);
                }
                entity = response.getEntity();
                EntityUtils.consume(entity);
            } else {
                execRuntime.disconnectEndpoint();
            }
            response = null;
        }
        status = response.getCode();
        if (status >= 300) {
            entity = response.getEntity();
            String responseMessage = entity != null ? EntityUtils.toString(entity) : null;
            execRuntime.disconnectEndpoint();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + new StatusLine(response), responseMessage);
        }
        return false;
    }

    private boolean createTunnelToProxy(HttpRoute route, int hop, HttpClientContext context) throws HttpException {
        throw new HttpException("Proxy chains are not supported.");
    }
}

