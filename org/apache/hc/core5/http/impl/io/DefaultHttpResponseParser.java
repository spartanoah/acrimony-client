/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpResponseParser
extends AbstractMessageParser<ClassicHttpResponse> {
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public DefaultHttpResponseParser(LineParser lineParser, HttpResponseFactory<ClassicHttpResponse> responseFactory, Http1Config http1Config) {
        super(lineParser, http1Config);
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public DefaultHttpResponseParser(Http1Config http1Config) {
        this(null, null, http1Config);
    }

    public DefaultHttpResponseParser() {
        this(Http1Config.DEFAULT);
    }

    @Override
    protected IOException createConnectionClosedException() {
        return new NoHttpResponseException("The target server failed to respond");
    }

    @Override
    protected ClassicHttpResponse createMessage(CharArrayBuffer buffer) throws IOException, HttpException {
        StatusLine statusline = this.getLineParser().parseStatusLine(buffer);
        ClassicHttpResponse response = this.responseFactory.newHttpResponse(statusline.getStatusCode(), statusline.getReasonPhrase());
        response.setVersion(statusline.getProtocolVersion());
        return response;
    }
}

