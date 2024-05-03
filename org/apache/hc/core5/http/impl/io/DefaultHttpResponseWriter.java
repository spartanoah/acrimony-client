/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.io.AbstractMessageWriter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpResponseWriter
extends AbstractMessageWriter<ClassicHttpResponse> {
    public DefaultHttpResponseWriter(LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpResponseWriter() {
        super(null);
    }

    @Override
    protected void writeHeadLine(ClassicHttpResponse message, CharArrayBuffer lineBuf) throws IOException {
        ProtocolVersion transportVersion = message.getVersion();
        this.getLineFormatter().formatStatusLine(lineBuf, new StatusLine(transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1, message.getCode(), message.getReasonPhrase()));
    }
}

