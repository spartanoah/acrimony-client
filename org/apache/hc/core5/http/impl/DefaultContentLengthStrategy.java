/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.NotImplementedException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class DefaultContentLengthStrategy
implements ContentLengthStrategy {
    public static final DefaultContentLengthStrategy INSTANCE = new DefaultContentLengthStrategy();

    @Override
    public long determineLength(HttpMessage message) throws HttpException {
        Args.notNull(message, "HTTP message");
        Header transferEncodingHeader = message.getFirstHeader("Transfer-Encoding");
        if (transferEncodingHeader != null) {
            String headerValue = transferEncodingHeader.getValue();
            if ("chunked".equalsIgnoreCase(headerValue)) {
                return -1L;
            }
            throw new NotImplementedException("Unsupported transfer encoding: " + headerValue);
        }
        if (message.countHeaders("Content-Length") > 1) {
            throw new ProtocolException("Multiple Content-Length headers");
        }
        Header contentLengthHeader = message.getFirstHeader("Content-Length");
        if (contentLengthHeader != null) {
            String s = contentLengthHeader.getValue();
            try {
                long len = Long.parseLong(s);
                if (len < 0L) {
                    throw new ProtocolException("Negative content length: " + s);
                }
                return len;
            } catch (NumberFormatException e) {
                throw new ProtocolException("Invalid content length: " + s);
            }
        }
        return -9223372036854775807L;
    }
}

