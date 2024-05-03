/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.classic.ClassicRequestCopier;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.ExecChainElement;
import org.apache.hc.client5.http.impl.classic.InternalExecRuntime;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
@Internal
class InternalHttpClient
extends CloseableHttpClient
implements Configurable {
    private static final Logger LOG = LoggerFactory.getLogger(InternalHttpClient.class);
    private final HttpClientConnectionManager connManager;
    private final HttpRequestExecutor requestExecutor;
    private final ExecChainElement execChain;
    private final HttpRoutePlanner routePlanner;
    private final Lookup<CookieSpecFactory> cookieSpecRegistry;
    private final Lookup<AuthSchemeFactory> authSchemeRegistry;
    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;
    private final RequestConfig defaultConfig;
    private final ConcurrentLinkedQueue<Closeable> closeables;

    public InternalHttpClient(HttpClientConnectionManager connManager, HttpRequestExecutor requestExecutor, ExecChainElement execChain, HttpRoutePlanner routePlanner, Lookup<CookieSpecFactory> cookieSpecRegistry, Lookup<AuthSchemeFactory> authSchemeRegistry, CookieStore cookieStore, CredentialsProvider credentialsProvider, RequestConfig defaultConfig, List<Closeable> closeables) {
        this.connManager = Args.notNull(connManager, "Connection manager");
        this.requestExecutor = Args.notNull(requestExecutor, "Request executor");
        this.execChain = Args.notNull(execChain, "Execution chain");
        this.routePlanner = Args.notNull(routePlanner, "Route planner");
        this.cookieSpecRegistry = cookieSpecRegistry;
        this.authSchemeRegistry = authSchemeRegistry;
        this.cookieStore = cookieStore;
        this.credentialsProvider = credentialsProvider;
        this.defaultConfig = defaultConfig;
        this.closeables = closeables != null ? new ConcurrentLinkedQueue<Closeable>(closeables) : null;
    }

    private HttpRoute determineRoute(HttpHost host, HttpRequest request, HttpContext context) throws HttpException {
        HttpHost target = host != null ? host : RoutingSupport.determineHost(request);
        return this.routePlanner.determineRoute(target, context);
    }

    private void setupContext(HttpClientContext context) {
        if (context.getAttribute("http.authscheme-registry") == null) {
            context.setAttribute("http.authscheme-registry", this.authSchemeRegistry);
        }
        if (context.getAttribute("http.cookiespec-registry") == null) {
            context.setAttribute("http.cookiespec-registry", this.cookieSpecRegistry);
        }
        if (context.getAttribute("http.cookie-store") == null) {
            context.setAttribute("http.cookie-store", this.cookieStore);
        }
        if (context.getAttribute("http.auth.credentials-provider") == null) {
            context.setAttribute("http.auth.credentials-provider", this.credentialsProvider);
        }
        if (context.getAttribute("http.request-config") == null) {
            context.setAttribute("http.request-config", this.defaultConfig);
        }
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
        Args.notNull(request, "HTTP request");
        try {
            if (request.getScheme() == null && target != null) {
                request.setScheme(target.getSchemeName());
            }
            if (request.getAuthority() == null && target != null) {
                request.setAuthority(new URIAuthority(target));
            }
            HttpClientContext localcontext = HttpClientContext.adapt(context != null ? context : new BasicHttpContext());
            RequestConfig config = null;
            if (request instanceof Configurable) {
                config = ((Configurable)((Object)request)).getConfig();
            }
            if (config != null) {
                localcontext.setRequestConfig(config);
            }
            this.setupContext(localcontext);
            HttpRoute route = this.determineRoute(target, request, localcontext);
            String exchangeId = ExecSupport.getNextExchangeId();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: preparing request execution", (Object)exchangeId);
            }
            InternalExecRuntime execRuntime = new InternalExecRuntime(LOG, this.connManager, this.requestExecutor, request instanceof CancellableDependency ? (CancellableDependency)((Object)request) : null);
            ExecChain.Scope scope = new ExecChain.Scope(exchangeId, route, request, execRuntime, localcontext);
            ClassicHttpResponse response = this.execChain.execute(ClassicRequestCopier.INSTANCE.copy(request), scope);
            return CloseableHttpResponse.adapt(response);
        } catch (HttpException httpException) {
            throw new ClientProtocolException(httpException.getMessage(), httpException);
        }
    }

    @Override
    public RequestConfig getConfig() {
        return this.defaultConfig;
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.closeables != null) {
            Closeable closeable;
            while ((closeable = this.closeables.poll()) != null) {
                try {
                    if (closeable instanceof ModalCloseable) {
                        ((ModalCloseable)closeable).close(closeMode);
                        continue;
                    }
                    closeable.close();
                } catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
    }
}

