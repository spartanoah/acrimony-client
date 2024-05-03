/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.BackoffManager;
import org.apache.hc.client5.http.classic.ConnectionBackoffStrategy;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.InputStreamFactory;
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
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SPNegoSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.BackoffStrategyExec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.ConnectExec;
import org.apache.hc.client5.http.impl.classic.ContentCompressionExec;
import org.apache.hc.client5.http.impl.classic.ExecChainElement;
import org.apache.hc.client5.http.impl.classic.HttpRequestRetryExec;
import org.apache.hc.client5.http.impl.classic.InternalHttpClient;
import org.apache.hc.client5.http.impl.classic.MainClientExec;
import org.apache.hc.client5.http.impl.classic.ProtocolExec;
import org.apache.hc.client5.http.impl.classic.RedirectExec;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.protocol.RequestAddCookies;
import org.apache.hc.client5.http.protocol.RequestAuthCache;
import org.apache.hc.client5.http.protocol.RequestClientConnControl;
import org.apache.hc.client5.http.protocol.RequestDefaultHeaders;
import org.apache.hc.client5.http.protocol.RequestExpectContinue;
import org.apache.hc.client5.http.protocol.ResponseProcessCookies;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.NamedElementChain;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.protocol.RequestContent;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.VersionInfo;

public class HttpClientBuilder {
    private HttpRequestExecutor requestExec;
    private HttpClientConnectionManager connManager;
    private boolean connManagerShared;
    private SchemePortResolver schemePortResolver;
    private ConnectionReuseStrategy reuseStrategy;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private AuthenticationStrategy targetAuthStrategy;
    private AuthenticationStrategy proxyAuthStrategy;
    private UserTokenHandler userTokenHandler;
    private LinkedList<RequestInterceptorEntry> requestInterceptors;
    private LinkedList<ResponseInterceptorEntry> responseInterceptors;
    private LinkedList<ExecInterceptorEntry> execInterceptors;
    private HttpRequestRetryStrategy retryStrategy;
    private HttpRoutePlanner routePlanner;
    private RedirectStrategy redirectStrategy;
    private ConnectionBackoffStrategy connectionBackoffStrategy;
    private BackoffManager backoffManager;
    private Lookup<AuthSchemeFactory> authSchemeRegistry;
    private Lookup<CookieSpecFactory> cookieSpecRegistry;
    private LinkedHashMap<String, InputStreamFactory> contentDecoderMap;
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
    private boolean redirectHandlingDisabled;
    private boolean automaticRetriesDisabled;
    private boolean contentCompressionDisabled;
    private boolean cookieManagementDisabled;
    private boolean authCachingDisabled;
    private boolean connectionStateDisabled;
    private boolean defaultUserAgentDisabled;
    private List<Closeable> closeables;

    public static HttpClientBuilder create() {
        return new HttpClientBuilder();
    }

    protected HttpClientBuilder() {
    }

    public final HttpClientBuilder setRequestExecutor(HttpRequestExecutor requestExec) {
        this.requestExec = requestExec;
        return this;
    }

    public final HttpClientBuilder setConnectionManager(HttpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    public final HttpClientBuilder setConnectionManagerShared(boolean shared) {
        this.connManagerShared = shared;
        return this;
    }

    public final HttpClientBuilder setConnectionReuseStrategy(ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    public final HttpClientBuilder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    public final HttpClientBuilder setTargetAuthenticationStrategy(AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    public final HttpClientBuilder setProxyAuthenticationStrategy(AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    public final HttpClientBuilder setUserTokenHandler(UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    public final HttpClientBuilder disableConnectionState() {
        this.connectionStateDisabled = true;
        return this;
    }

    public final HttpClientBuilder setSchemePortResolver(SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    public final HttpClientBuilder setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public final HttpClientBuilder setDefaultHeaders(Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    public final HttpClientBuilder addResponseInterceptorFirst(HttpResponseInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.responseInterceptors == null) {
            this.responseInterceptors = new LinkedList();
        }
        this.responseInterceptors.add(new ResponseInterceptorEntry(ResponseInterceptorEntry.Postion.FIRST, interceptor));
        return this;
    }

    public final HttpClientBuilder addResponseInterceptorLast(HttpResponseInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.responseInterceptors == null) {
            this.responseInterceptors = new LinkedList();
        }
        this.responseInterceptors.add(new ResponseInterceptorEntry(ResponseInterceptorEntry.Postion.LAST, interceptor));
        return this;
    }

    public final HttpClientBuilder addRequestInterceptorFirst(HttpRequestInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.requestInterceptors == null) {
            this.requestInterceptors = new LinkedList();
        }
        this.requestInterceptors.add(new RequestInterceptorEntry(RequestInterceptorEntry.Postion.FIRST, interceptor));
        return this;
    }

    public final HttpClientBuilder addRequestInterceptorLast(HttpRequestInterceptor interceptor) {
        Args.notNull(interceptor, "Interceptor");
        if (this.requestInterceptors == null) {
            this.requestInterceptors = new LinkedList();
        }
        this.requestInterceptors.add(new RequestInterceptorEntry(RequestInterceptorEntry.Postion.LAST, interceptor));
        return this;
    }

    public final HttpClientBuilder addExecInterceptorBefore(String existing, String name, ExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.BEFORE, name, interceptor, existing));
        return this;
    }

    public final HttpClientBuilder addExecInterceptorAfter(String existing, String name, ExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.AFTER, name, interceptor, existing));
        return this;
    }

    public final HttpClientBuilder replaceExecInterceptor(String existing, ExecChainHandler interceptor) {
        Args.notBlank(existing, "Existing");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.REPLACE, existing, interceptor, existing));
        return this;
    }

    public final HttpClientBuilder addExecInterceptorFirst(String name, ExecChainHandler interceptor) {
        Args.notNull(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.FIRST, name, interceptor, null));
        return this;
    }

    public final HttpClientBuilder addExecInterceptorLast(String name, ExecChainHandler interceptor) {
        Args.notNull(name, "Name");
        Args.notNull(interceptor, "Interceptor");
        if (this.execInterceptors == null) {
            this.execInterceptors = new LinkedList();
        }
        this.execInterceptors.add(new ExecInterceptorEntry(ExecInterceptorEntry.Postion.LAST, name, interceptor, null));
        return this;
    }

    public final HttpClientBuilder disableCookieManagement() {
        this.cookieManagementDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableContentCompression() {
        this.contentCompressionDisabled = true;
        return this;
    }

    public final HttpClientBuilder disableAuthCaching() {
        this.authCachingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setRetryStrategy(HttpRequestRetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
        return this;
    }

    public final HttpClientBuilder disableAutomaticRetries() {
        this.automaticRetriesDisabled = true;
        return this;
    }

    public final HttpClientBuilder setProxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public final HttpClientBuilder setRoutePlanner(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    public final HttpClientBuilder setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    public final HttpClientBuilder disableRedirectHandling() {
        this.redirectHandlingDisabled = true;
        return this;
    }

    public final HttpClientBuilder setConnectionBackoffStrategy(ConnectionBackoffStrategy connectionBackoffStrategy) {
        this.connectionBackoffStrategy = connectionBackoffStrategy;
        return this;
    }

    public final HttpClientBuilder setBackoffManager(BackoffManager backoffManager) {
        this.backoffManager = backoffManager;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    public final HttpClientBuilder setDefaultCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public final HttpClientBuilder setDefaultAuthSchemeRegistry(Lookup<AuthSchemeFactory> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    public final HttpClientBuilder setDefaultCookieSpecRegistry(Lookup<CookieSpecFactory> cookieSpecRegistry) {
        this.cookieSpecRegistry = cookieSpecRegistry;
        return this;
    }

    public final HttpClientBuilder setContentDecoderRegistry(LinkedHashMap<String, InputStreamFactory> contentDecoderMap) {
        this.contentDecoderMap = contentDecoderMap;
        return this;
    }

    public final HttpClientBuilder setDefaultRequestConfig(RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    public final HttpClientBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public final HttpClientBuilder evictExpiredConnections() {
        this.evictExpiredConnections = true;
        return this;
    }

    public final HttpClientBuilder evictIdleConnections(TimeValue maxIdleTime) {
        this.evictIdleConnections = true;
        this.maxIdleTime = maxIdleTime;
        return this;
    }

    public final HttpClientBuilder disableDefaultUserAgent() {
        this.defaultUserAgentDisabled = true;
        return this;
    }

    @Internal
    protected void customizeExecChain(NamedElementChain<ExecChainHandler> execChainDefinition) {
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
    public CloseableHttpClient build() {
        ArrayList<Closeable> closeablesCopy;
        CredentialsProvider defaultCredentialsProvider;
        CookieStore defaultCookieStore;
        Lookup<CookieSpecFactory> cookieSpecRegistryCopy;
        void var12_27;
        HttpRoutePlanner httpRoutePlanner;
        String userAgentCopy;
        UserTokenHandler userTokenHandlerCopy;
        AuthenticationStrategy proxyAuthStrategyCopy;
        AuthenticationStrategy targetAuthStrategyCopy;
        ConnectionKeepAliveStrategy keepAliveStrategyCopy;
        ConnectionReuseStrategy reuseStrategyCopy;
        HttpClientConnectionManager connManagerCopy;
        HttpRequestExecutor requestExecCopy = this.requestExec;
        if (requestExecCopy == null) {
            requestExecCopy = new HttpRequestExecutor();
        }
        if ((connManagerCopy = this.connManager) == null) {
            connManagerCopy = PoolingHttpClientConnectionManagerBuilder.create().build();
        }
        if ((reuseStrategyCopy = this.reuseStrategy) == null) {
            String s;
            reuseStrategyCopy = this.systemProperties ? ("true".equalsIgnoreCase(s = System.getProperty("http.keepAlive", "true")) ? DefaultConnectionReuseStrategy.INSTANCE : new ConnectionReuseStrategy(){

                @Override
                public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
                    return false;
                }
            }) : DefaultConnectionReuseStrategy.INSTANCE;
        }
        if ((keepAliveStrategyCopy = this.keepAliveStrategy) == null) {
            keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        if ((targetAuthStrategyCopy = this.targetAuthStrategy) == null) {
            targetAuthStrategyCopy = DefaultAuthenticationStrategy.INSTANCE;
        }
        if ((proxyAuthStrategyCopy = this.proxyAuthStrategy) == null) {
            proxyAuthStrategyCopy = DefaultAuthenticationStrategy.INSTANCE;
        }
        if ((userTokenHandlerCopy = this.userTokenHandler) == null) {
            userTokenHandlerCopy = !this.connectionStateDisabled ? DefaultUserTokenHandler.INSTANCE : NoopUserTokenHandler.INSTANCE;
        }
        if ((userAgentCopy = this.userAgent) == null) {
            if (this.systemProperties) {
                userAgentCopy = System.getProperty("http.agent");
            }
            if (userAgentCopy == null && !this.defaultUserAgentDisabled) {
                userAgentCopy = VersionInfo.getSoftwareInfo("Apache-HttpClient", "org.apache.hc.client5", this.getClass());
            }
        }
        NamedElementChain<ExecChainHandler> execChainDefinition = new NamedElementChain<ExecChainHandler>();
        execChainDefinition.addLast(new MainClientExec(connManagerCopy, reuseStrategyCopy, keepAliveStrategyCopy, userTokenHandlerCopy), ChainElement.MAIN_TRANSPORT.name());
        execChainDefinition.addFirst(new ConnectExec(reuseStrategyCopy, new DefaultHttpProcessor(new RequestTargetHost(), new RequestUserAgent(userAgentCopy)), proxyAuthStrategyCopy), ChainElement.CONNECT.name());
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
        b.addAll(new RequestDefaultHeaders(this.defaultHeaders), new RequestContent(), new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent(userAgentCopy), new RequestExpectContinue());
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
        execChainDefinition.addFirst(new ProtocolExec(httpProcessor, targetAuthStrategyCopy, proxyAuthStrategyCopy), ChainElement.PROTOCOL.name());
        if (!this.automaticRetriesDisabled) {
            void var12_21;
            HttpRequestRetryStrategy httpRequestRetryStrategy = this.retryStrategy;
            if (httpRequestRetryStrategy == null) {
                DefaultHttpRequestRetryStrategy defaultHttpRequestRetryStrategy = DefaultHttpRequestRetryStrategy.INSTANCE;
            }
            execChainDefinition.addFirst(new HttpRequestRetryExec((HttpRequestRetryStrategy)var12_21), ChainElement.RETRY.name());
        }
        if ((httpRoutePlanner = this.routePlanner) == null) {
            SchemePortResolver schemePortResolverCopy = this.schemePortResolver;
            if (schemePortResolverCopy == null) {
                schemePortResolverCopy = DefaultSchemePortResolver.INSTANCE;
            }
            if (this.proxy != null) {
                DefaultProxyRoutePlanner defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(this.proxy, schemePortResolverCopy);
            } else if (this.systemProperties) {
                SystemDefaultRoutePlanner systemDefaultRoutePlanner = new SystemDefaultRoutePlanner(schemePortResolverCopy, ProxySelector.getDefault());
            } else {
                DefaultRoutePlanner defaultRoutePlanner = new DefaultRoutePlanner(schemePortResolverCopy);
            }
        }
        if (!this.contentCompressionDisabled) {
            if (this.contentDecoderMap != null) {
                ArrayList<String> encodings = new ArrayList<String>(this.contentDecoderMap.keySet());
                RegistryBuilder<InputStreamFactory> b2 = RegistryBuilder.create();
                for (Map.Entry<String, InputStreamFactory> entry : this.contentDecoderMap.entrySet()) {
                    b2.register(entry.getKey(), entry.getValue());
                }
                Registry<InputStreamFactory> decoderRegistry = b2.build();
                execChainDefinition.addFirst(new ContentCompressionExec(encodings, decoderRegistry, true), ChainElement.COMPRESS.name());
            } else {
                execChainDefinition.addFirst(new ContentCompressionExec(true), ChainElement.COMPRESS.name());
            }
        }
        if (!this.redirectHandlingDisabled) {
            RedirectStrategy redirectStrategyCopy = this.redirectStrategy;
            if (redirectStrategyCopy == null) {
                redirectStrategyCopy = DefaultRedirectStrategy.INSTANCE;
            }
            execChainDefinition.addFirst(new RedirectExec((HttpRoutePlanner)var12_27, redirectStrategyCopy), ChainElement.REDIRECT.name());
        }
        if (this.backoffManager != null && this.connectionBackoffStrategy != null) {
            execChainDefinition.addFirst(new BackoffStrategyExec(this.connectionBackoffStrategy, this.backoffManager), ChainElement.BACK_OFF.name());
        }
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
        ExecChainElement execChain = null;
        for (NamedElementChain.Node current = execChainDefinition.getLast(); current != null; current = current.getPrevious()) {
            execChain = new ExecChainElement((ExecChainHandler)current.getValue(), execChain);
        }
        Lookup<AuthSchemeFactory> authSchemeRegistryCopy = this.authSchemeRegistry;
        if (authSchemeRegistryCopy == null) {
            authSchemeRegistryCopy = RegistryBuilder.create().register("Basic", BasicSchemeFactory.INSTANCE).register("Digest", (BasicSchemeFactory)((Object)DigestSchemeFactory.INSTANCE)).register("NTLM", (BasicSchemeFactory)((Object)NTLMSchemeFactory.INSTANCE)).register("Negotiate", (BasicSchemeFactory)((Object)SPNegoSchemeFactory.DEFAULT)).register("Kerberos", (BasicSchemeFactory)((Object)KerberosSchemeFactory.DEFAULT)).build();
        }
        if ((cookieSpecRegistryCopy = this.cookieSpecRegistry) == null) {
            cookieSpecRegistryCopy = CookieSpecSupport.createDefault();
        }
        if ((defaultCookieStore = this.cookieStore) == null) {
            defaultCookieStore = new BasicCookieStore();
        }
        if ((defaultCredentialsProvider = this.credentialsProvider) == null) {
            defaultCredentialsProvider = this.systemProperties ? new SystemDefaultCredentialsProvider() : new BasicCredentialsProvider();
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
                        try {
                            connectionEvictor.awaitTermination(Timeout.ofSeconds(1L));
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                connectionEvictor.start();
            }
            closeablesCopy.add(connManagerCopy);
        }
        return new InternalHttpClient(connManagerCopy, requestExecCopy, execChain, (HttpRoutePlanner)var12_27, cookieSpecRegistryCopy, authSchemeRegistryCopy, defaultCookieStore, defaultCredentialsProvider, this.defaultRequestConfig != null ? this.defaultRequestConfig : RequestConfig.DEFAULT, closeablesCopy);
    }

    private static class ExecInterceptorEntry {
        final Postion postion;
        final String name;
        final ExecChainHandler interceptor;
        final String existing;

        private ExecInterceptorEntry(Postion postion, String name, ExecChainHandler interceptor, String existing) {
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

