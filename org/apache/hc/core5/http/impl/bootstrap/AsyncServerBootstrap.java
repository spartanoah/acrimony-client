/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.NamedElementChain;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.FilterEntry;
import org.apache.hc.core5.http.impl.bootstrap.HandlerEntry;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.impl.bootstrap.StandardFilter;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestParserFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseWriterFactory;
import org.apache.hc.core5.http.impl.nio.ServerHttp1IOEventHandlerFactory;
import org.apache.hc.core5.http.impl.nio.ServerHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.nio.AsyncFilterHandler;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.support.AsyncServerExpectationFilter;
import org.apache.hc.core5.http.nio.support.AsyncServerFilterChainElement;
import org.apache.hc.core5.http.nio.support.AsyncServerFilterChainExchangeHandlerFactory;
import org.apache.hc.core5.http.nio.support.BasicAsyncServerExpectationDecorator;
import org.apache.hc.core5.http.nio.support.BasicServerExchangeHandler;
import org.apache.hc.core5.http.nio.support.DefaultAsyncResponseExchangeHandlerFactory;
import org.apache.hc.core5.http.nio.support.TerminalAsyncServerFilter;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http.protocol.UriPatternType;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

public class AsyncServerBootstrap {
    private final List<HandlerEntry<Supplier<AsyncServerExchangeHandler>>> handlerList = new ArrayList<HandlerEntry<Supplier<AsyncServerExchangeHandler>>>();
    private final List<FilterEntry<AsyncFilterHandler>> filters = new ArrayList<FilterEntry<AsyncFilterHandler>>();
    private String canonicalHostName;
    private LookupRegistry<Supplier<AsyncServerExchangeHandler>> lookupRegistry;
    private IOReactorConfig ioReactorConfig;
    private Http1Config http1Config;
    private CharCodingConfig charCodingConfig;
    private HttpProcessor httpProcessor;
    private ConnectionReuseStrategy connStrategy;
    private TlsStrategy tlsStrategy;
    private Timeout handshakeTimeout;
    private Decorator<IOSession> ioSessionDecorator;
    private Callback<Exception> exceptionCallback;
    private IOSessionListener sessionListener;
    private Http1StreamListener streamListener;

    private AsyncServerBootstrap() {
    }

    public static AsyncServerBootstrap bootstrap() {
        return new AsyncServerBootstrap();
    }

    public final AsyncServerBootstrap setCanonicalHostName(String canonicalHostName) {
        this.canonicalHostName = canonicalHostName;
        return this;
    }

    public final AsyncServerBootstrap setIOReactorConfig(IOReactorConfig ioReactorConfig) {
        this.ioReactorConfig = ioReactorConfig;
        return this;
    }

    public final AsyncServerBootstrap setHttp1Config(Http1Config http1Config) {
        this.http1Config = http1Config;
        return this;
    }

    public final AsyncServerBootstrap setCharCodingConfig(CharCodingConfig charCodingConfig) {
        this.charCodingConfig = charCodingConfig;
        return this;
    }

    public final AsyncServerBootstrap setHttpProcessor(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
        return this;
    }

    public final AsyncServerBootstrap setConnectionReuseStrategy(ConnectionReuseStrategy connStrategy) {
        this.connStrategy = connStrategy;
        return this;
    }

    public final AsyncServerBootstrap setTlsStrategy(TlsStrategy tlsStrategy) {
        this.tlsStrategy = tlsStrategy;
        return this;
    }

    public final AsyncServerBootstrap setTlsHandshakeTimeout(Timeout handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
        return this;
    }

    public final AsyncServerBootstrap setIOSessionDecorator(Decorator<IOSession> ioSessionDecorator) {
        this.ioSessionDecorator = ioSessionDecorator;
        return this;
    }

    public final AsyncServerBootstrap setExceptionCallback(Callback<Exception> exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
        return this;
    }

    public final AsyncServerBootstrap setIOSessionListener(IOSessionListener sessionListener) {
        this.sessionListener = sessionListener;
        return this;
    }

    public final AsyncServerBootstrap setLookupRegistry(LookupRegistry<Supplier<AsyncServerExchangeHandler>> lookupRegistry) {
        this.lookupRegistry = lookupRegistry;
        return this;
    }

    public final AsyncServerBootstrap setStreamListener(Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }

    public final AsyncServerBootstrap register(String uriPattern, Supplier<AsyncServerExchangeHandler> supplier) {
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(supplier, "Supplier");
        this.handlerList.add(new HandlerEntry<Supplier<AsyncServerExchangeHandler>>(null, uriPattern, supplier));
        return this;
    }

    public final AsyncServerBootstrap registerVirtual(String hostname, String uriPattern, Supplier<AsyncServerExchangeHandler> supplier) {
        Args.notBlank(hostname, "Hostname");
        Args.notBlank(uriPattern, "URI pattern");
        Args.notNull(supplier, "Supplier");
        this.handlerList.add(new HandlerEntry<Supplier<AsyncServerExchangeHandler>>(hostname, uriPattern, supplier));
        return this;
    }

    public final <T> AsyncServerBootstrap register(String uriPattern, final AsyncServerRequestHandler<T> requestHandler) {
        this.register(uriPattern, new Supplier<AsyncServerExchangeHandler>(){

            @Override
            public AsyncServerExchangeHandler get() {
                return new BasicServerExchangeHandler(requestHandler);
            }
        });
        return this;
    }

    public final <T> AsyncServerBootstrap registerVirtual(String hostname, String uriPattern, final AsyncServerRequestHandler<T> requestHandler) {
        this.registerVirtual(hostname, uriPattern, new Supplier<AsyncServerExchangeHandler>(){

            @Override
            public AsyncServerExchangeHandler get() {
                return new BasicServerExchangeHandler(requestHandler);
            }
        });
        return this;
    }

    public final AsyncServerBootstrap addFilterBefore(String existing, String name, AsyncFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<AsyncFilterHandler>(FilterEntry.Postion.BEFORE, name, filterHandler, existing));
        return this;
    }

    public final AsyncServerBootstrap addFilterAfter(String existing, String name, AsyncFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notBlank(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<AsyncFilterHandler>(FilterEntry.Postion.AFTER, name, filterHandler, existing));
        return this;
    }

    public final AsyncServerBootstrap replaceFilter(String existing, AsyncFilterHandler filterHandler) {
        Args.notBlank(existing, "Existing");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<AsyncFilterHandler>(FilterEntry.Postion.REPLACE, existing, filterHandler, existing));
        return this;
    }

    public final AsyncServerBootstrap addFilterFirst(String name, AsyncFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<AsyncFilterHandler>(FilterEntry.Postion.FIRST, name, filterHandler, null));
        return this;
    }

    public final AsyncServerBootstrap addFilterLast(String name, AsyncFilterHandler filterHandler) {
        Args.notNull(name, "Name");
        Args.notNull(filterHandler, "Filter handler");
        this.filters.add(new FilterEntry<AsyncFilterHandler>(FilterEntry.Postion.LAST, name, filterHandler, null));
        return this;
    }

    public HttpAsyncServer create() {
        HandlerFactory<AsyncServerExchangeHandler> handlerFactory;
        RequestHandlerRegistry<Supplier<AsyncServerExchangeHandler>> registry = new RequestHandlerRegistry<Supplier<AsyncServerExchangeHandler>>(this.canonicalHostName != null ? this.canonicalHostName : InetAddressUtils.getCanonicalLocalHostName(), new Supplier<LookupRegistry<Supplier<AsyncServerExchangeHandler>>>(){

            @Override
            public LookupRegistry<Supplier<AsyncServerExchangeHandler>> get() {
                return AsyncServerBootstrap.this.lookupRegistry != null ? AsyncServerBootstrap.this.lookupRegistry : UriPatternType.newMatcher(UriPatternType.URI_PATTERN);
            }
        });
        for (HandlerEntry<Supplier<AsyncServerExchangeHandler>> entry : this.handlerList) {
            registry.register(entry.hostname, entry.uriPattern, (Supplier<AsyncServerExchangeHandler>)entry.handler);
        }
        if (!this.filters.isEmpty()) {
            NamedElementChain<AsyncFilterHandler> filterChainDefinition = new NamedElementChain<AsyncFilterHandler>();
            filterChainDefinition.addLast(new TerminalAsyncServerFilter(new DefaultAsyncResponseExchangeHandlerFactory(registry)), StandardFilter.MAIN_HANDLER.name());
            filterChainDefinition.addFirst(new AsyncServerExpectationFilter(), StandardFilter.EXPECT_CONTINUE.name());
            for (FilterEntry<AsyncFilterHandler> entry : this.filters) {
                switch (entry.postion) {
                    case AFTER: {
                        filterChainDefinition.addAfter(entry.existing, (AsyncFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case BEFORE: {
                        filterChainDefinition.addBefore(entry.existing, (AsyncFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case REPLACE: {
                        filterChainDefinition.replace(entry.existing, (AsyncFilterHandler)entry.filterHandler);
                        break;
                    }
                    case FIRST: {
                        filterChainDefinition.addFirst((AsyncFilterHandler)entry.filterHandler, entry.name);
                        break;
                    }
                    case LAST: {
                        filterChainDefinition.addLast((AsyncFilterHandler)entry.filterHandler, entry.name);
                    }
                }
            }
            AsyncServerFilterChainElement execChain = null;
            for (NamedElementChain.Node current = filterChainDefinition.getLast(); current != null; current = current.getPrevious()) {
                execChain = new AsyncServerFilterChainElement((AsyncFilterHandler)current.getValue(), execChain);
            }
            handlerFactory = new AsyncServerFilterChainExchangeHandlerFactory(execChain, this.exceptionCallback);
        } else {
            handlerFactory = new DefaultAsyncResponseExchangeHandlerFactory(registry, new Decorator<AsyncServerExchangeHandler>(){

                @Override
                public AsyncServerExchangeHandler decorate(AsyncServerExchangeHandler handler) {
                    return new BasicAsyncServerExpectationDecorator(handler, AsyncServerBootstrap.this.exceptionCallback);
                }
            });
        }
        ServerHttp1StreamDuplexerFactory streamHandlerFactory = new ServerHttp1StreamDuplexerFactory(this.httpProcessor != null ? this.httpProcessor : HttpProcessors.server(), handlerFactory, this.http1Config != null ? this.http1Config : Http1Config.DEFAULT, this.charCodingConfig != null ? this.charCodingConfig : CharCodingConfig.DEFAULT, this.connStrategy != null ? this.connStrategy : DefaultConnectionReuseStrategy.INSTANCE, DefaultHttpRequestParserFactory.INSTANCE, DefaultHttpResponseWriterFactory.INSTANCE, DefaultContentLengthStrategy.INSTANCE, DefaultContentLengthStrategy.INSTANCE, this.streamListener);
        ServerHttp1IOEventHandlerFactory ioEventHandlerFactory = new ServerHttp1IOEventHandlerFactory(streamHandlerFactory, this.tlsStrategy, this.handshakeTimeout);
        return new HttpAsyncServer(ioEventHandlerFactory, this.ioReactorConfig, this.ioSessionDecorator, this.exceptionCallback, this.sessionListener);
    }
}

