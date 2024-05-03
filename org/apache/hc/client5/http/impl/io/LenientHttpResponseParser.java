/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LenientHttpResponseParser
extends DefaultHttpResponseParser {
    private static final Logger LOG = LoggerFactory.getLogger(LenientHttpResponseParser.class);

    public LenientHttpResponseParser(LineParser lineParser, HttpResponseFactory<ClassicHttpResponse> responseFactory, Http1Config h1Config) {
        super(lineParser, responseFactory, h1Config);
    }

    public LenientHttpResponseParser(Http1Config h1Config) {
        this(null, null, h1Config);
    }

    @Override
    protected ClassicHttpResponse createMessage(CharArrayBuffer buffer) throws IOException {
        try {
            return super.createMessage(buffer);
        } catch (HttpException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Garbage in response: {}", (Object)buffer);
            }
            return null;
        }
    }
}

