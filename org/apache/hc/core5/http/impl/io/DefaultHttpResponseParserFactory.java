/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParser;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.message.LazyLaxLineParser;
import org.apache.hc.core5.http.message.LineParser;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseParserFactory
implements HttpMessageParserFactory<ClassicHttpResponse> {
    public static final DefaultHttpResponseParserFactory INSTANCE = new DefaultHttpResponseParserFactory();
    private final LineParser lineParser;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public DefaultHttpResponseParserFactory(LineParser lineParser, HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.lineParser = lineParser != null ? lineParser : LazyLaxLineParser.INSTANCE;
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public DefaultHttpResponseParserFactory() {
        this(null, null);
    }

    @Override
    public HttpMessageParser<ClassicHttpResponse> create(Http1Config http1Config) {
        return new DefaultHttpResponseParser(this.lineParser, this.responseFactory, http1Config);
    }
}

