/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.NamedElementChain;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.FilterEntry;
import org.apache.hc.core5.http.impl.bootstrap.HandlerEntry;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.StandardFilter;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.ssl.DefaultTlsSetupHandler;
import org.apache.hc.core5.http.io.support.BasicHttpServerExpectationDecorator;
import org.apache.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import org.apache.hc.core5.http.io.support.HttpServerExpectationFilter;
import org.apache.hc.core5.http.io.support.HttpServerFilterChainElement;
import org.apache.hc.core5.http.io.support.HttpServerFilterChainRequestHandler;
import org.apache.hc.core5.http.io.support.TerminalServerFilter;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http.protocol.UriPatternType;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.Args;

public class ServerBootstrap {
    private final List<HandlerEntry<HttpRequestHandler>> handlerList = new ArrayList<HandlerEntry<HttpRequestHandler>>();
    private final List<FilterEntry<HttpFilterHandler>> filters = new ArrayList<FilterEntry<HttpFilterHandler>>();
    private String canonicalHostName;
    private LookupRegistry<HttpRequestHandler> lookupRegistry;
    private int listenerPort;
    private InetAddress localAddress;
    private SocketConfig socketConfig;
    private Http1Config http1Config;
    private CharCodingConfig charCodingConfig;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private HttpResponseFactory<ClassicHttpResponse> responseFactory;
    private ServerSocketFactory serverSocketFactory;
    private SSLContext sslContext;
    private Callback<SSLParameters> sslSetupHandler;
    private HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory;
    private ExceptionListener exceptionListener;
    private Http1StreamListener streamListener;

    private ServerBootstrap() {
    }

    public static ServerBootstrap bootstrap() {
        return new ServerBootstrap();
    }

    public final ServerBootstrap setCanonicalHostName(String canonicalHostName) {
        this.canonicalHostName = canonicalHostName;
        return this;
    }

    public final ServerBootstrap setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
        return this;
    }

    public final ServerBootstrap setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public final ServerBootstrap setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
        return this;
    }

    public final ServerBootstrap setHttp1Config(Http1Config http1Config) {
        this.http1Config = http1Config;
        return this;
    }

    public final ServerBootstrap setCharCodingConfig(CharCodingConfig charCodingConfig) {
        this.charCodingConfig = charCodingConfig;
        return this;
    }

    public final ServerBootstrap setHttpProcessor(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final ServerBootstrap setConnectionReuseStrategy(ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final ServerBootstrap setResponseFactory(HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.responseFactory = responseFactory;
        return this;
    }

    public final ServerBootstrap setLookupRegistry(LookupRegistry<HttpRequestHandler> lookupRegistry) {
        this.lookupRegistry = lookupRegistry;
        return this;
    }

    public final ServerBootstrap register(String uriPattern, HttpRequestHandler requestHandler) {
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(requestHandler, "Supplier");
        this.handlerList.add(new HandlerEntry<HttpRequestHandler>(null, uriPattern, requestHandler));
        return this;
    }

    public final ServerBootstrap registerVirtual(String hostname, String uriPattern, HttpRequestHandler requestHandler) {
        Args.notBlank(hostname, "Hostname");
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(requestHandler, "Supplier");
        this.handlerList.add(new HandlerEntry<HttpRequestHandler>(hostname, uriPattern, requestHandler));
        return this;
    }

    public final ServerBootstrap setConnectionFactory(HttpConnectionFactory<? extends DefaultBHttpServerConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public final ServerBootstrap setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
        return this;
    }

    public final ServerBootstrap setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public final ServerBootstrap setSslSetupHandler(Callback<SSLParameters> sslSetupHandler) {
        this.sslSetupHandler = sslSetupHandler;
        return this;
    }

    public final ServerBootstrap setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
        return this;
    }

    public final ServerBootstrap setStreamListener(Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final ServerBootstrap addFilterBefore(String existing, String name, HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<HttpFilterHandler>(FilterEntry.Postion.BEFORE, name, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap addFilterAfter(String existing, String name, HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<HttpFilterHandler>(FilterEntry.Postion.AFTER, name, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap replaceFilter(String existing, HttpFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<HttpFilterHandler>(FilterEntry.Postion.REPLACE, existing, filterHandler, existing));
        return this;
    }

    public final ServerBootstrap addFilterFirst(String name, HttpFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<HttpFilterHandler>(FilterEntry.Postion.FIRST, name, filterHandler, null));
        return this;
    }

    public final ServerBootstrap addFilterLast(String name, HttpFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<HttpFilterHandler>(FilterEntry.Postion.LAST, name, filterHandler, null));
        return this;
    }

    public HttpServer create() {
        DefaultBHttpServerConnectionFactory connectionFactoryCopy;
        HttpServerRequestHandler requestHandler;
        RequestHandlerRegistry<HttpRequestHandler> handlerRegistry = new RequestHandlerRegistry<HttpRequestHandler>(this.canonicalHostName != null ? this.canonicalHostName : InetAddressUtils.getCanonicalLocalHostName(), new Supplier<LookupRegistry<HttpRequestHandler>>(){

            @Override
            public LookupRegistry<HttpRequestHandler> get() {
                return ServerBootstrap.this.lookupRegistry != null ? ServerBootstrap.this.lookupRegistry : UriPatternType.newMatcher(UriPatternType.URI_PATTERN);
            }
        });
        for (HandlerEntry<HttpRequestHandler> entry : this.handlerList) {
            handlerRegistry.register(entry.hostname, entry.uriPattern, (HttpRequestHandler)entry.handler);
        }
        if (!this.filters.isEmpty()) {
            NamedElementChain<HttpFilterHandler> filterChainDefinition = new NamedElementChain<HttpFilterHandler>();
            filterChainDefinition.addLast(new TerminalServerFilter(handlerRegistry, this.responseFactory != null ? this.responseFactory : DefaultClassicHttpResponseFactory.INSTANCE), StandardFilter.MAIN_HANDLER.name());
            filterChainDefinition.addFirst(new HttpServerExpectationFilter(), StandardFilter.EXPECT_CONTINUE.name());
            for (FilterEntry<HttpFilterHandler> entry : this.filters) {
                switch (entry.postion) {
                    case AFTER: {
                        filterChainDefinition.addAfter(entry.existing, (HttpFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case BEFORE: {
                        filterChainDefinition.addBefore(entry.existing, (HttpFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case REPLACE: {
                        filterChainDefinition.replace(entry.existing, (HttpFilterHandler)entry.filterHandler);
                        break;
                    }
                    case FIRST: {
                        filterChainDefinition.addFirst((HttpFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case LAST: {
                        filterChainDefinition.addLast((HttpFilterHandler)entry.filterHandler, entry.name);
                    }
                }
            }
            HttpServerFilterChainElement filterChain = null;
            for (NamedElementChain.Node current = filterChainDefinition.getLast(); current != null; current = current.getPrevious()) {
                filterChain = new HttpServerFilterChainElement((HttpFilterHandler)current.getValue(), filterChain);
            }
            requestHandler = new HttpServerFilterChainRequestHandler(filterChain);
        } else {
            requestHandler = new BasicHttpServerExpectationDecorator(new BasicHttpServerRequestHandler(handlerRegistry, this.responseFactory != null ? this.responseFactory : DefaultClassicHttpResponseFactory.INSTANCE));
        }
        HttpService httpService = new HttpService(this.httpProcessor != null ? this.httpProcessor : HttpProcessors.server(), requestHandler, this.connStrategy != null ? this.connStrategy : DefaultConnectionReuseStrategy.INSTANCE, this.streamListener);
        ServerSocketFactory serverSocketFactoryCopy = this.serverSocketFactory;
        if (serverSocketFactoryCopy == null) {
            serverSocketFactoryCopy = this.sslContext != null ? this.sslContext.getServerSocketFactory() : ServerSocketFactory.getDefault();
        }
        if ((connectionFactoryCopy = this.connectionFactory) == null) {
            String scheme = serverSocketFactoryCopy instanceof SSLServerSocketFactory ? URIScheme.HTTPS.id : URIScheme.HTTP.id;
            connectionFactoryCopy = new DefaultBHttpServerConnectionFactory(scheme, this.http1Config, this.charCodingConfig);
        }
        return new HttpServer(this.listenerPort > 0 ? this.listenerPort : 0, httpService, this.localAddress, this.socketConfig != null ? this.socketConfig : SocketConfig.DEFAULT, serverSocketFactoryCopy, connectionFactoryCopy, this.sslSetupHandler != null ? this.sslSetupHandler : new DefaultTlsSetupHandler(), this.exceptionListener != null ? this.exceptionListener : ExceptionListener.NO_OP);
    }
}

