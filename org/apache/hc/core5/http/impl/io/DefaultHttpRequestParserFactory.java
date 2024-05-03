/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpRequestFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpRequestFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestParser;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.message.LazyLineParser;
import org.apache.hc.core5.http.message.LineParser;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpRequestParserFactory
implements HttpMessageParserFactory<ClassicHttpRequest> {
    public static final DefaultHttpRequestParserFactory INSTANCE = new DefaultHttpRequestParserFactory();
    private final LineParser lineParser;
    private final HttpRequestFactory<ClassicHttpRequest> requestFactory;

    public DefaultHttpRequestParserFactory(LineParser lineParser, HttpRequestFactory<ClassicHttpRequest> requestFactory) {
        this.lineParser = lineParser != null ? lineParser : LazyLineParser.INSTANCE;
        this.requestFactory = requestFactory != null ? requestFactory : DefaultClassicHttpRequestFactory.INSTANCE;
    }

    public DefaultHttpRequestParserFactory() {
        this(null, null);
    }

    @Override
    public HttpMessageParser<ClassicHttpRequest> create(Http1Config http1Config) {
        return new DefaultHttpRequestParser(this.lineParser, this.requestFactory, http1Config);
    }
}

