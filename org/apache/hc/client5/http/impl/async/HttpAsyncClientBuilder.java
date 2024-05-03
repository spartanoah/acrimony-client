/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.HttpVersionPolicy
 *  org.apache.hc.core5.http2.config.H2Config
 *  org.apache.hc.core5.http2.protocol.H2RequestConnControl
 *  org.apache.hc.core5.http2.protocol.H2RequestContent
 *  org.apache.hc.core5.http2.protocol.H2RequestTargetHost
 */
package org.apache.hc.client5.http.impl.async;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.CookieSpecSupport;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.DefaultUserTokenHandler;
import org.apache.hc.client5.http.impl.IdleConnectionEvictor;
import org.apache.hc.client5.http.impl.NoopUserTokenHandler;
import org.apache.hc.client5.http.impl.async.AsyncConnectExec;
import org.apache.hc.client5.http.impl.async.AsyncExecChainElement;
import org.apache.hc.client5.http.impl.async.AsyncHttpRequestRetryExec;
import org.apache.hc.client5.http.impl.async.AsyncProtocolExec;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.impl.async.AsyncRedirectExec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientEventHandlerFactory;
import org.apache.hc.client5.http.impl.async.HttpAsyncMainClientExec;
import org.apache.hc.client5.http.impl.async.InternalHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.LoggingExceptionCallback;
import org.apache.hc.client5.http.impl.async.LoggingIOSessionDecorator;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SPNegoSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.protocol.RequestAddCookies;
import org.apache.hc.client5.http.protocol.RequestAuthCache;
import org.apache.hc.client5.http.protocol.RequestDefaultHeaders;
import org.apache.hc.client5.http.protocol.RequestExpectContinue;
import org.apache.hc.client5.http.protocol.ResponseProcessCookies;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.NamedElementChain;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.protocol.H2RequestConnControl;
import org.apache.hc.core5.http2.protocol.H2RequestContent;
import org.apache.hc.core5.http2.protocol.H2RequestTargetHost;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.VersionInfo;

public class HttpAsyncClientBuilder {
    private HttpVersionPolicy versionPolicy;
    private AsyncClientConnectionManager connManager;
    private boolean connManagerShared;
    private IOReactorConfig ioReactorConfig;
    private Http1Config h1Config;
    private H2Config h2Config;
    private CharCodingConfig charCodingConfig;
    private SchemePortResolver schemePortResolver;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private UserTokenHandler userTokenHandler;
    private AuthenticationStrategy targetAuthStrategy;
    private AuthenticationStrategy proxyAuthStrategy;
    private LinkedList<RequestInterceptorEntry> requestInterceptors;
    private LinkedList<ResponseInterceptorEntry> responseInterceptors;
    private LinkedList<ExecInterceptorEntry> execInterceptors;
    private HttpRoutePlanner routePlanner;
    private RedirectStrategy redirectStrategy;
    private HttpRequestRetryStrategy retryStrategy;
    private ConnectionReuseStrategy reuseStrategy;
    private Lookup<AuthSchemeFactory> authSchemeRegistry;
    private Lookup<CookieSpecFactory> cookieSpecRegistry;
    private CookieStore cookieStore;
    private CredentialsProvider credentialsProvider;
    private String userAgent;
    private HttpHost proxy;
    private Collection<? extends Header> defaultHeaders;
    private RequestConfig defaultRequestConfig;
    private boolean evictExpiredConnections;
    private boolean evictIdleConnections;
    private TimeValue maxIdleTime;
    private boolean systemProperties;
    private boolean automaticRetriesDisabled;
    private boolean redirectHandlingDisabled;
    private boolean cookieManagementDisabled;
    private boolean authCachingDisabled;
    private boolean connectionStateDisabled;
    private ThreadFactory threadFactory;
    private List<Closeable> closeables;

    public static HttpAsyncClientBuilder create() {
        return new HttpAsyncClientBuilder();
    }

    protected HttpAsyncClientBuilder() {
    }

    public final HttpAsyncClientBuilder setVersionPolicy(HttpVersionPolicy versionPolicy) {
        this.versionPolicy = versionPolicy;
        return this;
    }

    public final HttpAsyncClientBuilder setHttp1Config(Http1Config h1Config) {
        this.h1Config = h1Config;
        return this;
    }

    public final HttpAsyncClientBuilder setH2Config(H2Config h2Config) {
        this.h2Config = h2Config;
        return this;
    }

    public final HttpAsyncClientBuilder setConnectionManager(AsyncClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final HttpAsyncClientBuilder setConnectionManagerShared(boolean shared) {
        this.connManagerShared = shared;
        return this;
    }

    public final HttpAsyncClientBuilder setIOReactorConfig(IOReactorConfig ioReactorConfig) {
        this.ioReactorConfig = ioReactorConfig;
        return this;
    }

    public final HttpAsyncClientBuilder setCharCodingConfig(CharCodingConfig charCodingConfig) {
        this.charCodingConfig = charCodingConfig;
        return this;
    }

    public final HttpAsyncClientBuilder setConnectionReuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    public final HttpAsyncClientBuilder setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder addResponseInterceptorFirst(HttpResponseInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.responseInterceptors == null) {
            this.responseInterceptors = new LinkedList();
        }
        this.responseInterceptors.add(new ResponseInterceptorEntry(ResponseInterceptorEntry.Postion.FIRST, interceptor));
        return this;
    }

    public final HttpAsyncClientBuilder addResponseInterceptorLast(HttpResponseInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.responseInterceptors == null) {
            this.responseInterceptors = new LinkedList();
        }
        this.responseInterceptors.add(new ResponseInterceptorEntry(ResponseInterceptorEntry.Postion.LAST, interceptor));
        return this;
    }

    public final HttpAsyncClientBuilder addExecInterceptorBefore(String existing, String name, AsyncExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.BEFORE, name, interceptor, existing));
        return this;
    }

    public final HttpAsyncClientBuilder addExecInterceptorAfter(String existing, String name, AsyncExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.AFTER, name, interceptor, existing));
        return this;
    }

    public final HttpAsyncClientBuilder replaceExecInterceptor(String existing, AsyncExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.REPLACE, existing, interceptor, existing));
        return this;
    }

    public final HttpAsyncClientBuilder addExecInterceptorFirst(String name, AsyncExecChainHandler interceptor) {
        Args.notNull(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.FIRST, name, interceptor, null));
        return this;
    }

    public final HttpAsyncClientBuilder addExecInterceptorLast(String name, AsyncExecChainHandler interceptor) {
        Args.notNull(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.LAST, name, interceptor, null));
        return this;
    }

    public final HttpAsyncClientBuilder addRequestInterceptorFirst(HttpRequestInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.requestInterceptors == null) {
            this.requestInterceptors = new LinkedList();
        }
        this.requestInterceptors.add(new RequestInterceptorEntry(RequestInterceptorEntry.Postion.FIRST, interceptor));
        return this;
    }

    public final HttpAsyncClientBuilder addRequestInterceptorLast(HttpRequestInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.requestInterceptors == null) {
            this.requestInterceptors = new LinkedList();
        }
        this.requestInterceptors.add(new RequestInterceptorEntry(RequestInterceptorEntry.Postion.LAST, interceptor));
        return this;
    }

    public final HttpAsyncClientBuilder setRetryStrategy(HttpRequestRetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
        return this;
    }

    public HttpAsyncClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    public final HttpAsyncClientBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final HttpAsyncClientBuilder setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public final HttpAsyncClientBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public final HttpAsyncClientBuilder setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public final HttpAsyncClientBuilder setRoutePlanner(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultAuthSchemeRegistry(Lookup<AuthSchemeFactory> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCookieSpecRegistry(Lookup<CookieSpecFactory> cookieSpecRegistry) {
        this.cookieSpecRegistry = cookieSpecRegistry;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public final HttpAsyncClientBuilder setDefaultRequestConfig(RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    public final HttpAsyncClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableConnectionState() {
        this.connectionStateDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableRedirectHandling() {
        this.redirectHandlingDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableAutomaticRetries() {
        this.automaticRetriesDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableCookieManagement() {
        this.cookieManagementDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    public final HttpAsyncClientBuilder evictExpiredConnections() {
        this.evictExpiredConnections = true;
        return this;
    }

    public final HttpAsyncClientBuilder evictIdleConnections(TimeValue maxIdleTime) {
        this.evictIdleConnections = true;
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    @Internal
    protected void customizeExecChain(NamedElementChain<AsyncExecChainHandler> execChainDefinition) {
    }

    @Internal
    protected void addCloseable(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (this.closeables == null) {
            this.closeables = new ArrayList<Closeable>();
        }
        this.closeables.add(closeable);
    }

    /*
     * WARNING - void declaration
     */
    public CloseableHttpAsyncClient build() {
        CredentialsProvider credentialsProviderCopy;
        CookieStore cookieStoreCopy;
        Lookup<CookieSpecFactory> cookieSpecRegistryCopy;
        ConnectionReuseStrategy reuseStrategyCopy;
        ArrayList<Closeable> closeablesCopy;
        void var10_25;
        HttpRoutePlanner httpRoutePlanner;
        String userAgentCopy;
        AuthenticationStrategy proxyAuthStrategyCopy;
        UserTokenHandler userTokenHandlerCopy;
        ConnectionKeepAliveStrategy keepAliveStrategyCopy;
        AsyncClientConnectionManager connManagerCopy = this.connManager;
        if (connManagerCopy == null) {
            connManagerCopy = PoolingAsyncClientConnectionManagerBuilder.create().build();
        }
        if ((keepAliveStrategyCopy = this.keepAliveStrategy) == null) {
            keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        if ((userTokenHandlerCopy = this.userTokenHandler) == null) {
            userTokenHandlerCopy = !this.connectionStateDisabled ? DefaultUserTokenHandler.INSTANCE : NoopUserTokenHandler.INSTANCE;
        }
        NamedElementChain<AsyncExecChainHandler> execChainDefinition = new NamedElementChain<AsyncExecChainHandler>();
        execChainDefinition.addLast(new HttpAsyncMainClientExec(keepAliveStrategyCopy, userTokenHandlerCopy), ChainElement.MAIN_TRANSPORT.name());
        AuthenticationStrategy targetAuthStrategyCopy = this.targetAuthStrategy;
        if (targetAuthStrategyCopy == null) {
            targetAuthStrategyCopy = DefaultAuthenticationStrategy.INSTANCE;
        }
        if ((proxyAuthStrategyCopy = this.proxyAuthStrategy) == null) {
            proxyAuthStrategyCopy = DefaultAuthenticationStrategy.INSTANCE;
        }
        if ((userAgentCopy = this.userAgent) == null) {
            if (this.systemProperties) {
                userAgentCopy = this.getProperty("http.agent", null);
            }
            if (userAgentCopy == null) {
                userAgentCopy = VersionInfo.getSoftwareInfo("Apache-HttpAsyncClient", "org.apache.hc.client5", this.getClass());
            }
        }
        execChainDefinition.addFirst(new AsyncConnectExec(new DefaultHttpProcessor(new RequestTargetHost(), new RequestUserAgent(userAgentCopy)), proxyAuthStrategyCopy), ChainElement.CONNECT.name());
        HttpProcessorBuilder b = HttpProcessorBuilder.create();
        if (this.requestInterceptors != null) {
            for (RequestInterceptorEntry requestInterceptorEntry : this.requestInterceptors) {
                if (requestInterceptorEntry.postion != RequestInterceptorEntry.Postion.FIRST) continue;
                b.addFirst(requestInterceptorEntry.interceptor);
            }
        }
        if (this.responseInterceptors != null) {
            for (ResponseInterceptorEntry responseInterceptorEntry : this.responseInterceptors) {
                if (responseInterceptorEntry.postion != ResponseInterceptorEntry.Postion.FIRST) continue;
                b.addFirst(responseInterceptorEntry.interceptor);
            }
        }
        b.addAll(new RequestDefaultHeaders(this.defaultHeaders), new RequestUserAgent(userAgentCopy), new RequestExpectContinue());
        if (!this.cookieManagementDisabled) {
            b.add(new RequestAddCookies());
        }
        if (!this.authCachingDisabled) {
            b.add(new RequestAuthCache());
        }
        if (!this.cookieManagementDisabled) {
            b.add(new ResponseProcessCookies());
        }
        if (this.requestInterceptors != null) {
            for (RequestInterceptorEntry requestInterceptorEntry : this.requestInterceptors) {
                if (requestInterceptorEntry.postion != RequestInterceptorEntry.Postion.LAST) continue;
                b.addLast(requestInterceptorEntry.interceptor);
            }
        }
        if (this.responseInterceptors != null) {
            for (ResponseInterceptorEntry responseInterceptorEntry : this.responseInterceptors) {
                if (responseInterceptorEntry.postion != ResponseInterceptorEntry.Postion.LAST) continue;
                b.addLast(responseInterceptorEntry.interceptor);
            }
        }
        HttpProcessor httpProcessor = b.build();
        execChainDefinition.addFirst(new AsyncProtocolExec(httpProcessor, targetAuthStrategyCopy, proxyAuthStrategyCopy), ChainElement.PROTOCOL.name());
        if (!this.automaticRetriesDisabled) {
            void var10_19;
            HttpRequestRetryStrategy httpRequestRetryStrategy = this.retryStrategy;
            if (httpRequestRetryStrategy == null) {
                DefaultHttpRequestRetryStrategy defaultHttpRequestRetryStrategy = DefaultHttpRequestRetryStrategy.INSTANCE;
            }
            execChainDefinition.addFirst(new AsyncHttpRequestRetryExec((HttpRequestRetryStrategy)var10_19), ChainElement.RETRY.name());
        }
        if ((httpRoutePlanner = this.routePlanner) == null) {
            SchemePortResolver schemePortResolverCopy = this.schemePortResolver;
            if (schemePortResolverCopy == null) {
                schemePortResolverCopy = DefaultSchemePortResolver.INSTANCE;
            }
            if (this.proxy != null) {
                DefaultProxyRoutePlanner defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(this.proxy, schemePortResolverCopy);
            } else if (this.systemProperties) {
                ProxySelector defaultProxySelector = AccessController.doPrivileged(new PrivilegedAction<ProxySelector>(){

                    @Override
                    public ProxySelector run() {
                        return ProxySelector.getDefault();
                    }
                });
                SystemDefaultRoutePlanner systemDefaultRoutePlanner = new SystemDefaultRoutePlanner(schemePortResolverCopy, defaultProxySelector);
            } else {
                DefaultRoutePlanner defaultRoutePlanner = new DefaultRoutePlanner(schemePortResolverCopy);
            }
        }
        if (!this.redirectHandlingDisabled) {
            RedirectStrategy redirectStrategyCopy = this.redirectStrategy;
            if (redirectStrategyCopy == null) {
                redirectStrategyCopy = DefaultRedirectStrategy.INSTANCE;
            }
            execChainDefinition.addFirst(new AsyncRedirectExec((HttpRoutePlanner)var10_25, redirectStrategyCopy), ChainElement.REDIRECT.name());
        }
        ArrayList<Closeable> arrayList = closeablesCopy = this.closeables != null ? new ArrayList<Closeable>(this.closeables) : null;
        if (!this.connManagerShared) {
            if (closeablesCopy == null) {
                closeablesCopy = new ArrayList(1);
            }
            if ((this.evictExpiredConnections || this.evictIdleConnections) && connManagerCopy instanceof ConnPoolControl) {
                final IdleConnectionEvictor connectionEvictor = new IdleConnectionEvictor((ConnPoolControl)((Object)connManagerCopy), this.maxIdleTime, this.maxIdleTime);
                closeablesCopy.add(new Closeable(){

                    @Override
                    public void close() throws IOException {
                        connectionEvictor.shutdown();
                    }
                });
                connectionEvictor.start();
            }
            closeablesCopy.add(connManagerCopy);
        }
        if ((reuseStrategyCopy = this.reuseStrategy) == null) {
            String s;
            reuseStrategyCopy = this.systemProperties ? ("true".equalsIgnoreCase(s = this.getProperty("http.keepAlive", "true")) ? DefaultConnectionReuseStrategy.INSTANCE : new ConnectionReuseStrategy(){

                @Override
                public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
                    return false;
                }
            }) : DefaultConnectionReuseStrategy.INSTANCE;
        }
        final AsyncPushConsumerRegistry pushConsumerRegistry = new AsyncPushConsumerRegistry();
        HttpAsyncClientEventHandlerFactory ioEventHandlerFactory = new HttpAsyncClientEventHandlerFactory(new DefaultHttpProcessor(new HttpRequestInterceptor[]{new H2RequestContent(), new H2RequestTargetHost(), new H2RequestConnControl()}), new HandlerFactory<AsyncPushConsumer>(){

            @Override
            public AsyncPushConsumer create(HttpRequest request, HttpContext context) throws HttpException {
                return pushConsumerRegistry.get(request);
            }
        }, this.versionPolicy != null ? this.versionPolicy : HttpVersionPolicy.NEGOTIATE, this.h2Config != null ? this.h2Config : H2Config.DEFAULT, this.h1Config != null ? this.h1Config : Http1Config.DEFAULT, this.charCodingConfig != null ? this.charCodingConfig : CharCodingConfig.DEFAULT, reuseStrategyCopy);
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioEventHandlerFactory, this.ioReactorConfig != null ? this.ioReactorConfig : IOReactorConfig.DEFAULT, this.threadFactory != null ? this.threadFactory : new DefaultThreadFactory("httpclient-dispatch", true), LoggingIOSessionDecorator.INSTANCE, LoggingExceptionCallback.INSTANCE, null, new Callback<IOSession>(){

            @Override
            public void execute(IOSession ioSession) {
                ioSession.enqueue(new ShutdownCommand(CloseMode.GRACEFUL), Command.Priority.IMMEDIATE);
            }
        });
        if (this.execInterceptors != null) {
            for (ExecInterceptorEntry entry : this.execInterceptors) {
                switch (entry.postion) {
                    case AFTER: {
                        execChainDefinition.addAfter(entry.existing, entry.interceptor, entry.name);
                        break;
                    }
                    case BEFORE: {
                        execChainDefinition.addBefore(entry.existing, entry.interceptor, entry.name);
                        break;
                    }
                    case REPLACE: {
                        execChainDefinition.replace(entry.existing, entry.interceptor);
                        break;
                    }
                    case FIRST: {
                        execChainDefinition.addFirst(entry.interceptor, entry.name);
                        break;
                    }
                    case LAST: {
                        execChainDefinition.addLast(entry.interceptor, entry.name);
                    }
                }
            }
        }
        this.customizeExecChain(execChainDefinition);
        AsyncExecChainElement execChain = null;
        for (NamedElementChain.Node current = execChainDefinition.getLast(); current != null; current = current.getPrevious()) {
            execChain = new AsyncExecChainElement((AsyncExecChainHandler)current.getValue(), execChain);
        }
        Lookup<AuthSchemeFactory> authSchemeRegistryCopy = this.authSchemeRegistry;
        if (authSchemeRegistryCopy == null) {
            authSchemeRegistryCopy = RegistryBuilder.create().register("Basic", BasicSchemeFactory.INSTANCE).register("Digest", (BasicSchemeFactory)((Object)DigestSchemeFactory.INSTANCE)).register("NTLM", (BasicSchemeFactory)((Object)NTLMSchemeFactory.INSTANCE)).register("Negotiate", (BasicSchemeFactory)((Object)SPNegoSchemeFactory.DEFAULT)).register("Kerberos", (BasicSchemeFactory)((Object)KerberosSchemeFactory.DEFAULT)).build();
        }
        if ((cookieSpecRegistryCopy = this.cookieSpecRegistry) == null) {
            cookieSpecRegistryCopy = CookieSpecSupport.createDefault();
        }
        if ((cookieStoreCopy = this.cookieStore) == null) {
            cookieStoreCopy = new BasicCookieStore();
        }
        if ((credentialsProviderCopy = this.credentialsProvider) == null) {
            credentialsProviderCopy = this.systemProperties ? new SystemDefaultCredentialsProvider() : new BasicCredentialsProvider();
        }
        return new InternalHttpAsyncClient(ioReactor, execChain, pushConsumerRegistry, this.threadFactory != null ? this.threadFactory : new DefaultThreadFactory("httpclient-main", true), connManagerCopy, (HttpRoutePlanner)var10_25, this.versionPolicy != null ? this.versionPolicy : HttpVersionPolicy.NEGOTIATE, cookieSpecRegistryCopy, authSchemeRegistryCopy, cookieStoreCopy, credentialsProviderCopy, this.defaultRequestConfig, closeablesCopy);
    }

    private String getProperty(final String key, final String defaultValue) {
        return AccessController.doPrivileged(new PrivilegedAction<String>(){

            @Override
            public String run() {
                return System.getProperty(key, defaultValue);
            }
        });
    }

    private static class ExecInterceptorEntry {
        final Postion postion;
        final String name;
        final AsyncExecChainHandler interceptor;
        final String existing;

        private ExecInterceptorEntry(Postion postion, String name, AsyncExecChainHandler interceptor, String existing) {
            this.postion = postion;
            this.name = name;
            this.interceptor = interceptor;
            this.existing = existing;
        }

        static enum Postion {
            BEFORE,
            AFTER,
            REPLACE,
            FIRST,
            LAST;

        }
    }

    private static class ResponseInterceptorEntry {
        final Postion postion;
        final HttpResponseInterceptor interceptor;

        private ResponseInterceptorEntry(Postion postion, HttpResponseInterceptor interceptor) {
            this.postion = postion;
            this.interceptor = interceptor;
        }

        static enum Postion {
            FIRST,
            LAST;

        }
    }

    private static class RequestInterceptorEntry {
        final Postion postion;
        final HttpRequestInterceptor interceptor;

        private RequestInterceptorEntry(Postion postion, HttpRequestInterceptor interceptor) {
            this.postion = postion;
            this.interceptor = interceptor;
        }

        static enum Postion {
            FIRST,
            LAST;

        }
    }
}

