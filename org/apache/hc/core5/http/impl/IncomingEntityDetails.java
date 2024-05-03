/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.util.Collections;
import java.util.Set;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.Args;

@Internal
public class IncomingEntityDetails
implements EntityDetails {
    private final MessageHeaders message;
    private final long contentLength;

    public IncomingEntityDetails(MessageHeaders message, long contentLength) {
        this.message = Args.notNull(message, "Message");
        this.contentLength = contentLength;
    }

    public IncomingEntityDetails(MessageHeaders message) {
        this(message, -1L);
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public String getContentType() {
        Header h = this.message.getFirstHeader("Content-Type");
        return h != null ? h.getValue() : null;
    }

    @Override
    public String getContentEncoding() {
        Header h = this.message.getFirstHeader("Content-Encoding");
        return h != null ? h.getValue() : null;
    }

    @Override
    public boolean isChunked() {
        return this.contentLength < 0L;
    }

    @Override
    public Set<String> getTrailerNames() {
        Header h = this.message.getFirstHeader("Trailer");
        if (h == null) {
            return Collections.emptySet();
        }
        return MessageSupport.parseTokens(h);
    }
}

