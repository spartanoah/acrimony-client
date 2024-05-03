/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.nio.AbstractMessageWriter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpRequestWriter<T extends HttpRequest>
extends AbstractMessageWriter<T> {
    public DefaultHttpRequestWriter(LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpRequestWriter() {
        super(null);
    }

    @Override
    protected void writeHeadLine(T message, CharArrayBuffer lineBuf) throws IOException {
        lineBuf.clear();
        ProtocolVersion transportVersion = message.getVersion();
        this.getLineFormatter().formatRequestLine(lineBuf, new RequestLine(message.getMethod(), message.getRequestUri(), transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1));
    }
}

