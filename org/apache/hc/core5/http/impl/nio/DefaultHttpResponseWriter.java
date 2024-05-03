/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.nio.AbstractMessageWriter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpResponseWriter<T extends HttpResponse>
extends AbstractMessageWriter<T> {
    public DefaultHttpResponseWriter(LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpResponseWriter() {
        super(null);
    }

    @Override
    protected void writeHeadLine(T message, CharArrayBuffer lineBuf) throws IOException {
        lineBuf.clear();
        ProtocolVersion transportVersion = message.getVersion();
        this.getLineFormatter().formatStatusLine(lineBuf, new StatusLine(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1, message.getCode(), message.getReasonPhrase()));
    }
}

