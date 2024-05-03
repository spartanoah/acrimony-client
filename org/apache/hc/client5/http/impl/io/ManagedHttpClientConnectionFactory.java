/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.hc.client5.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.hc.client5.http.impl.io.DefaultManagedHttpClientConnection;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class ManagedHttpClientConnectionFactory
implements HttpConnectionFactory<ManagedHttpClientConnection> {
    private static final AtomicLong COUNTER = new AtomicLong();
    public static final ManagedHttpClientConnectionFactory INSTANCE = new ManagedHttpClientConnectionFactory();
    private final Http1Config h1Config;
    private final CharCodingConfig charCodingConfig;
    private final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory;
    private final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;

    public ManagedHttpClientConnectionFactory(Http1Config h1Config, CharCodingConfig charCodingConfig, HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory, HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory, ContentLengthStrategy incomingContentStrategy, ContentLengthStrategy outgoingContentStrategy) {
        this.h1Config = h1Config != null ? h1Config : Http1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.requestWriterFactory = requestWriterFactory != null ? requestWriterFactory : DefaultHttpRequestWriterFactory.INSTANCE;
        this.responseParserFactory = responseParserFactory != null ? responseParserFactory : DefaultHttpResponseParserFactory.INSTANCE;
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : DefaultContentLengthStrategy.INSTANCE;
    }

    public ManagedHttpClientConnectionFactory(Http1Config h1Config, CharCodingConfig charCodingConfig, HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory, HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this(h1Config, charCodingConfig, requestWriterFactory, responseParserFactory, null, null);
    }

    public ManagedHttpClientConnectionFactory(Http1Config h1Config, CharCodingConfig charCodingConfig, HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory) {
        this(h1Config, charCodingConfig, null, responseParserFactory);
    }

    public ManagedHttpClientConnectionFactory() {
        this(null, null, null);
    }

    @Override
    public ManagedHttpClientConnection createConnection(Socket socket) throws IOException {
        CodingErrorAction unmappableInputAction;
        CharsetDecoder charDecoder = null;
        CharsetEncoder charEncoder = null;
        Charset charset = this.charCodingConfig.getCharset();
        CodingErrorAction malformedInputAction = this.charCodingConfig.getMalformedInputAction() != null ? this.charCodingConfig.getMalformedInputAction() : CodingErrorAction.REPORT;
        CodingErrorAction codingErrorAction = unmappableInputAction = this.charCodingConfig.getUnmappableInputAction() != null ? this.charCodingConfig.getUnmappableInputAction() : CodingErrorAction.REPORT;
        if (charset != null) {
            charDecoder = charset.newDecoder();
            charDecoder.onMalformedInput(malformedInputAction);
            charDecoder.onUnmappableCharacter(unmappableInputAction);
            charEncoder = charset.newEncoder();
            charEncoder.onMalformedInput(malformedInputAction);
            charEncoder.onUnmappableCharacter(unmappableInputAction);
        }
        String id = "http-outgoing-" + Long.toString(COUNTER.getAndIncrement());
        DefaultManagedHttpClientConnection conn = new DefaultManagedHttpClientConnection(id, charDecoder, charEncoder, this.h1Config, this.incomingContentStrategy, this.outgoingContentStrategy, this.requestWriterFactory, this.responseParserFactory);
        if (socket != null) {
            conn.bind(socket);
        }
        return conn;
    }
}

