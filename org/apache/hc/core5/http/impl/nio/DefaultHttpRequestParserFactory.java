/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestFactory;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestParser;
import org.apache.hc.core5.http.message.LazyLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.nio.NHttpMessageParser;
import org.apache.hc.core5.http.nio.NHttpMessageParserFactory;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpRequestParserFactory
implements NHttpMessageParserFactory<HttpRequest> {
    public static final DefaultHttpRequestParserFactory INSTANCE = new DefaultHttpRequestParserFactory();
    private final Http1Config http1Config;
    private final LineParser lineParser;
    private final HttpRequestFactory<HttpRequest> requestFactory;

    public DefaultHttpRequestParserFactory(Http1Config http1Config, HttpRequestFactory<HttpRequest> requestFactory, LineParser lineParser) {
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.requestFactory = requestFactory != null ? requestFactory : DefaultHttpRequestFactory.INSTANCE;
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
    }

    public DefaultHttpRequestParserFactory(Http1Config http1Config) {
        this(http1Config, null, null);
    }

    public DefaultHttpRequestParserFactory() {
        this(null);
    }

    @Override
    public NHttpMessageParser<HttpRequest> create() {
        return new DefaultHttpRequestParser<HttpRequest>(this.requestFactory, this.lineParser, this.http1Config);
    }
}

