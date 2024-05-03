/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseParser;
import org.apache.hc.core5.http.message.LazyLaxLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.nio.NHttpMessageParser;
import org.apache.hc.core5.http.nio.NHttpMessageParserFactory;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseParserFactory
implements NHttpMessageParserFactory<HttpResponse> {
    public static final DefaultHttpResponseParserFactory INSTANCE = new DefaultHttpResponseParserFactory();
    private final Http1Config http1Config;
    private final HttpResponseFactory<HttpResponse> responseFactory;
    private final LineParser lineParser;

    public DefaultHttpResponseParserFactory(Http1Config http1Config, HttpResponseFactory<HttpResponse> responseFactory, LineParser lineParser) {
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.responseFactory = responseFactory != null ? responseFactory : DefaultHttpResponseFactory.INSTANCE;
        this.lineParser = lineParser != null ? lineParser : LazyLaxLineParser.INSTANCE;
    }

    public DefaultHttpResponseParserFactory(Http1Config http1Config) {
        this(http1Config, null, null);
    }

    public DefaultHttpResponseParserFactory() {
        this(null);
    }

    @Override
    public NHttpMessageParser<HttpResponse> create() {
        return new DefaultHttpResponseParser<HttpResponse>(this.responseFactory, this.lineParser, this.http1Config);
    }
}

