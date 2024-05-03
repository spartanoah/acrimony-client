/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.io.InterruptedIOException;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.client5.http.impl.ConnectionShutdownException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.RequestEntityProxy;
import org.apache.hc.client5.http.impl.classic.ResponseEntityProxy;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class MainClientExec
implements ExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MainClientExec.class);
    private final HttpClientConnectionManager connectionManager;
    private final ConnectionReuseStrategy reuseStrategy;
    private final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final UserTokenHandler userTokenHandler;

    public MainClientExec(HttpClientConnectionManager connectionManager, ConnectionReuseStrategy reuseStrategy, ConnectionKeepAliveStrategy keepAliveStrategy, UserTokenHandler userTokenHandler) {
        this.connectionManager = Args.notNull(connectionManager, "Connection manager");
        this.reuseStrategy = Args.notNull(reuseStrategy, "Connection reuse strategy");
        this.keepAliveStrategy = Args.notNull(keepAliveStrategy, "Connection keep alive strategy");
        this.userTokenHandler = Args.notNull(userTokenHandler, "User token handler");
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        String exchangeId = scope.exchangeId;
        HttpRoute route = scope.route;
        HttpClientContext context = scope.clientContext;
        ExecRuntime execRuntime = scope.execRuntime;
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: executing {}", (Object)exchangeId, (Object)new RequestLine(request));
        }
        try {
            RequestEntityProxy.enhance(request);
            ClassicHttpResponse response = execRuntime.execute(exchangeId, request, context);
            Object userToken = context.getUserToken();
            if (userToken == null) {
                userToken = this.userTokenHandler.getUserToken(route, context);
                context.setAttribute("http.user-token", userToken);
            }
            if (this.reuseStrategy.keepAlive(request, response, context)) {
                TimeValue duration = this.keepAliveStrategy.getKeepAliveDuration(response, context);
                if (LOG.isDebugEnabled()) {
                    String s = duration != null ? "for " + duration : "indefinitely";
                    LOG.debug("{}: connection can be kept alive {}", (Object)exchangeId, (Object)s);
                }
                execRuntime.markConnectionReusable(userToken, duration);
            } else {
                execRuntime.markConnectionNonReusable();
            }
            HttpEntity entity = response.getEntity();
            if (entity == null || !entity.isStreaming()) {
                execRuntime.releaseEndpoint();
                return new CloseableHttpResponse(response, null);
            }
            ResponseEntityProxy.enhance(response, execRuntime);
            return new CloseableHttpResponse(response, execRuntime);
        } catch (ConnectionShutdownException ex) {
            InterruptedIOException ioex = new InterruptedIOException("Connection has been shut down");
            ioex.initCause(ex);
            execRuntime.discardEndpoint();
            throw ioex;
        } catch (IOException | RuntimeException | HttpException ex) {
            execRuntime.discardEndpoint();
            throw ex;
        } catch (Error error) {
            this.connectionManager.close(CloseMode.IMMEDIATE);
            throw error;
        }
    }
}

