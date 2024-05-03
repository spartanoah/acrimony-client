/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.impl.io.AbstractMessageWriter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.util.CharArrayBuffer;

public class DefaultHttpRequestWriter
extends AbstractMessageWriter<ClassicHttpRequest> {
    public DefaultHttpRequestWriter(LineFormatter formatter) {
        super(formatter);
    }

    public DefaultHttpRequestWriter() {
        this(null);
    }

    @Override
    protected void writeHeadLine(ClassicHttpRequest message, CharArrayBuffer lineBuf) throws IOException {
        ProtocolVersion transportVersion = message.getVersion();
        this.getLineFormatter().formatRequestLine(lineBuf, new RequestLine(message.getMethod(), message.getRequestUri(), transportVersion != null ? transportVersion : HttpVersion.HTTP_1_1));
    }
}

