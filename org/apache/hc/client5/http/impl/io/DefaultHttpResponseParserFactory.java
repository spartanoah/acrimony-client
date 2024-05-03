/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import org.apache.hc.client5.http.impl.io.LenientHttpResponseParser;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.LineParser;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultHttpResponseParserFactory
implements HttpMessageParserFactory<ClassicHttpResponse> {
    public static final DefaultHttpResponseParserFactory INSTANCE = new DefaultHttpResponseParserFactory();
    private final LineParser lineParser;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public DefaultHttpResponseParserFactory(LineParser lineParser, HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.lineParser = lineParser != null ? lineParser : BasicLineParser.INSTANCE;
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public DefaultHttpResponseParserFactory(HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this(null, responseFactory);
    }

    public DefaultHttpResponseParserFactory() {
        this(null, null);
    }

    @Override
    public HttpMessageParser<ClassicHttpResponse> create(Http1Config h1Config) {
        return new LenientHttpResponseParser(this.lineParser, this.responseFactory, h1Config);
    }
}

