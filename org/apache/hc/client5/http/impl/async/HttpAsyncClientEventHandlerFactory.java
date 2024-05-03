/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.HttpVersionPolicy
 *  org.apache.hc.core5.http2.config.H2Config
 *  org.apache.hc.core5.http2.frame.FramePrinter
 *  org.apache.hc.core5.http2.frame.RawFrame
 *  org.apache.hc.core5.http2.impl.nio.ClientH2StreamMultiplexerFactory
 *  org.apache.hc.core5.http2.impl.nio.ClientHttpProtocolNegotiator
 *  org.apache.hc.core5.http2.impl.nio.H2StreamListener
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.hc.client5.http.impl.async.InternalHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.LogAppendable;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseParserFactory;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.NHttpMessageParserFactory;
import org.apache.hc.core5.http.nio.NHttpMessageWriterFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.frame.FramePrinter;
import org.apache.hc.core5.http2.frame.RawFrame;
import org.apache.hc.core5.http2.impl.nio.ClientH2StreamMultiplexerFactory;
import org.apache.hc.core5.http2.impl.nio.ClientHttpProtocolNegotiator;
import org.apache.hc.core5.http2.impl.nio.H2StreamListener;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HttpAsyncClientEventHandlerFactory
implements IOEventHandlerFactory {
    private static final Logger STREAM_LOG = LoggerFactory.getLogger(InternalHttpAsyncClient.class);
    private static final Logger HEADER_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http.headers");
    private static final Logger FRAME_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http2.frame");
    private static final Logger FRAME_PAYLOAD_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http2.frame.payload");
    private static final Logger FLOW_CTRL_LOG = LoggerFactory.getLogger("org.apache.hc.client5.http2.flow");
    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncPushConsumer> exchangeHandlerFactory;
    private final HttpVersionPolicy versionPolicy;
    private final H2Config h2Config;
    private final Http1Config h1Config;
    private final CharCodingConfig charCodingConfig;
    private final ConnectionReuseStrategy http1ConnectionReuseStrategy;
    private final NHttpMessageParserFactory<HttpResponse> http1ResponseParserFactory;
    private final NHttpMessageWriterFactory<HttpRequest> http1RequestWriterFactory;

    HttpAsyncClientEventHandlerFactory(HttpProcessor httpProcessor, HandlerFactory<AsyncPushConsumer> exchangeHandlerFactory, HttpVersionPolicy versionPolicy, H2Config h2Config, Http1Config h1Config, CharCodingConfig charCodingConfig, ConnectionReuseStrategy connectionReuseStrategy) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.exchangeHandlerFactory = exchangeHandlerFactory;
        this.versionPolicy = versionPolicy != null ? versionPolicy : HttpVersionPolicy.NEGOTIATE;
        this.h2Config = h2Config != null ? h2Config : H2Config.DEFAULT;
        this.h1Config = h1Config != null ? h1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.http1ConnectionReuseStrategy = connectionReuseStrategy != null ? connectionReuseStrategy : DefaultConnectionReuseStrategy.INSTANCE;
        this.http1ResponseParserFactory = new DefaultHttpResponseParserFactory(h1Config);
        this.http1RequestWriterFactory = DefaultHttpRequestWriterFactory.INSTANCE;
    }

    @Override
    public IOEventHandler createHandler(ProtocolIOSession ioSession, Object attachment) {
        if (STREAM_LOG.isDebugEnabled() || HEADER_LOG.isDebugEnabled() || FRAME_LOG.isDebugEnabled() || FRAME_PAYLOAD_LOG.isDebugEnabled() || FLOW_CTRL_LOG.isDebugEnabled()) {
            final String id = ioSession.getId();
            ClientHttp1StreamDuplexerFactory http1StreamHandlerFactory = new ClientHttp1StreamDuplexerFactory(this.httpProcessor, this.h1Config, this.charCodingConfig, this.http1ConnectionReuseStrategy, this.http1ResponseParserFactory, this.http1RequestWriterFactory, new Http1StreamListener(){

                @Override
                public void onRequestHead(HttpConnection connection, HttpRequest request) {
                    if (HEADER_LOG.isDebugEnabled()) {
                        HEADER_LOG.debug("{} >> {}", (Object)id, (Object)new RequestLine(request));
                        Iterator<Header> it = request.headerIterator();
                        while (it.hasNext()) {
                            HEADER_LOG.debug("{} >> {}", (Object)id, (Object)it.next());
                        }
                    }
                }

                @Override
                public void onResponseHead(HttpConnection connection, HttpResponse response) {
                    if (HEADER_LOG.isDebugEnabled()) {
                        HEADER_LOG.debug("{} << {}", (Object)id, (Object)new StatusLine(response));
                        Iterator<Header> it = response.headerIterator();
                        while (it.hasNext()) {
                            HEADER_LOG.debug("{} << {}", (Object)id, (Object)it.next());
                        }
                    }
                }

                @Override
                public void onExchangeComplete(HttpConnection connection, boolean keepAlive) {
                    if (STREAM_LOG.isDebugEnabled()) {
                        if (keepAlive) {
                            STREAM_LOG.debug("{} Connection is kept alive", (Object)id);
                        } else {
                            STREAM_LOG.debug("{} Connection is not kept alive", (Object)id);
                        }
                    }
                }
            });
            ClientH2StreamMultiplexerFactory http2StreamHandlerFactory = new ClientH2StreamMultiplexerFactory(this.httpProcessor, this.exchangeHandlerFactory, this.h2Config, this.charCodingConfig, new H2StreamListener(){
                final FramePrinter framePrinter = new FramePrinter();

                private void logFrameInfo(String prefix, RawFrame frame) {
                    try {
                        LogAppendable logAppendable = new LogAppendable(FRAME_LOG, prefix);
                        this.framePrinter.printFrameInfo(frame, (Appendable)logAppendable);
                        logAppendable.flush();
                    } catch (IOException iOException) {
                        // empty catch block
                    }
                }

                private void logFramePayload(String prefix, RawFrame frame) {
                    try {
                        LogAppendable logAppendable = new LogAppendable(FRAME_PAYLOAD_LOG, prefix);
                        this.framePrinter.printPayload(frame, (Appendable)logAppendable);
                        logAppendable.flush();
                    } catch (IOException iOException) {
                        // empty catch block
                    }
                }

                private void logFlowControl(String prefix, int streamId, int delta, int actualSize) {
                    FLOW_CTRL_LOG.debug("{} stream {} flow control {} -> {}", prefix, streamId, delta, actualSize);
                }

                public void onHeaderInput(HttpConnection connection, int streamId, List<? extends Header> headers) {
                    if (HEADER_LOG.isDebugEnabled()) {
                        for (int i = 0; i < headers.size(); ++i) {
                            HEADER_LOG.debug("{} << {}", (Object)id, (Object)headers.get(i));
                        }
                    }
                }

                public void onHeaderOutput(HttpConnection connection, int streamId, List<? extends Header> headers) {
                    if (HEADER_LOG.isDebugEnabled()) {
                        for (int i = 0; i < headers.size(); ++i) {
                            HEADER_LOG.debug("{} >> {}", (Object)id, (Object)headers.get(i));
                        }
                    }
                }

                public void onFrameInput(HttpConnection connection, int streamId, RawFrame frame) {
                    if (FRAME_LOG.isDebugEnabled()) {
                        this.logFrameInfo(id + " <<", frame);
                    }
                    if (FRAME_PAYLOAD_LOG.isDebugEnabled()) {
                        this.logFramePayload(id + " <<", frame);
                    }
                }

                public void onFrameOutput(HttpConnection connection, int streamId, RawFrame frame) {
                    if (FRAME_LOG.isDebugEnabled()) {
                        this.logFrameInfo(id + " >>", frame);
                    }
                    if (FRAME_PAYLOAD_LOG.isDebugEnabled()) {
                        this.logFramePayload(id + " >>", frame);
                    }
                }

                public void onInputFlowControl(HttpConnection connection, int streamId, int delta, int actualSize) {
                    if (FLOW_CTRL_LOG.isDebugEnabled()) {
                        this.logFlowControl(id + " <<", streamId, delta, actualSize);
                    }
                }

                public void onOutputFlowControl(HttpConnection connection, int streamId, int delta, int actualSize) {
                    if (FLOW_CTRL_LOG.isDebugEnabled()) {
                        this.logFlowControl(id + " >>", streamId, delta, actualSize);
                    }
                }
            });
            return new ClientHttpProtocolNegotiator(ioSession, http1StreamHandlerFactory, http2StreamHandlerFactory, attachment instanceof HttpVersionPolicy ? (HttpVersionPolicy)attachment : this.versionPolicy);
        }
        ClientHttp1StreamDuplexerFactory http1StreamHandlerFactory = new ClientHttp1StreamDuplexerFactory(this.httpProcessor, this.h1Config, this.charCodingConfig, this.http1ConnectionReuseStrategy, this.http1ResponseParserFactory, this.http1RequestWriterFactory, null);
        ClientH2StreamMultiplexerFactory http2StreamHandlerFactory = new ClientH2StreamMultiplexerFactory(this.httpProcessor, this.exchangeHandlerFactory, this.h2Config, this.charCodingConfig, null);
        return new ClientHttpProtocolNegotiator(ioSession, http1StreamHandlerFactory, http2StreamHandlerFactory, attachment instanceof HttpVersionPolicy ? (HttpVersionPolicy)attachment : this.versionPolicy);
    }
}

