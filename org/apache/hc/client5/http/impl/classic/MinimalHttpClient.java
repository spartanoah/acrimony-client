/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.io.InterruptedIOException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnectionShutdownException;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.InternalExecRuntime;
import org.apache.hc.client5.http.impl.classic.ResponseEntityProxy;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RequestClientConnControl;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.RequestContent;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class MinimalHttpClient
extends CloseableHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(MinimalHttpClient.class);
    private final HttpClientConnectionManager connManager;
    private final ConnectionReuseStrategy reuseStrategy;
    private final SchemePortResolver schemePortResolver;
    private final HttpRequestExecutor requestExecutor;
    private final HttpProcessor httpProcessor;

    MinimalHttpClient(HttpClientConnectionManager connManager) {
        this.connManager = Args.notNull(connManager, "HTTP connection manager");
        this.reuseStrategy = DefaultConnectionReuseStrategy.INSTANCE;
        this.schemePortResolver = DefaultSchemePortResolver.INSTANCE;
        this.requestExecutor = new HttpRequestExecutor(this.reuseStrategy);
        this.httpProcessor = new DefaultHttpProcessor(new RequestContent(), new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent(VersionInfo.getSoftwareInfo("Apache-HttpClient", "org.apache.hc.client5", this.getClass())));
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
        Args.notNull(target, "Target host");
        Args.notNull(request, "HTTP request");
        if (request.getScheme() == null) {
            request.setScheme(target.getSchemeName());
        }
        if (request.getAuthority() == null) {
            request.setAuthority(new URIAuthority(target));
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context != null ? context : new BasicHttpContext());
        RequestConfig config = null;
        if (request instanceof Configurable) {
            config = ((Configurable)((Object)request)).getConfig();
        }
        if (config != null) {
            clientContext.setRequestConfig(config);
        }
        HttpRoute route = new HttpRoute(RoutingSupport.normalize(target, this.schemePortResolver));
        String exchangeId = ExecSupport.getNextExchangeId();
        InternalExecRuntime execRuntime = new InternalExecRuntime(LOG, this.connManager, this.requestExecutor, request instanceof CancellableDependency ? (CancellableDependency)((Object)request) : null);
        try {
            if (!execRuntime.isEndpointAcquired()) {
                execRuntime.acquireEndpoint(exchangeId, route, null, clientContext);
            }
            if (!execRuntime.isEndpointConnected()) {
                execRuntime.connectEndpoint(clientContext);
            }
            context.setAttribute("http.request", request);
            context.setAttribute("http.route", route);
            this.httpProcessor.process(request, (EntityDetails)request.getEntity(), context);
            ClassicHttpResponse response = execRuntime.execute(exchangeId, request, clientContext);
            this.httpProcessor.process(response, (EntityDetails)response.getEntity(), context);
            if (this.reuseStrategy.keepAlive(request, response, context)) {
                execRuntime.markConnectionReusable(null, TimeValue.NEG_ONE_MILLISECOND);
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
        } catch (HttpException httpException) {
            execRuntime.discardEndpoint();
            throw new ClientProtocolException(httpException);
        } catch (IOException | RuntimeException ex) {
            execRuntime.discardEndpoint();
            throw ex;
        } catch (Error error) {
            this.connManager.close(CloseMode.IMMEDIATE);
            throw error;
        }
    }

    @Override
    public void close() throws IOException {
        this.connManager.close();
    }

    @Override
    public void close(CloseMode closeMode) {
        this.connManager.close(closeMode);
    }
}

