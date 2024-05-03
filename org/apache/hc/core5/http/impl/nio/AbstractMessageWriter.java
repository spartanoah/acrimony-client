/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.nio.NHttpMessageWriter;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public abstract class AbstractMessageWriter<T extends HttpMessage>
implements NHttpMessageWriter<T> {
    private final CharArrayBuffer lineBuf;
    private final LineFormatter lineFormatter;

    public AbstractMessageWriter(LineFormatter formatter) {
        this.lineFormatter = formatter != null ? formatter : BasicLineFormatter.INSTANCE;
        this.lineBuf = new CharArrayBuffer(64);
    }

    LineFormatter getLineFormatter() {
        return this.lineFormatter;
    }

    @Override
    public void reset() {
    }

    protected abstract void writeHeadLine(T var1, CharArrayBuffer var2) throws IOException;

    @Override
    public void write(T message, SessionOutputBuffer sessionBuffer) throws IOException, HttpException {
        Args.notNull(message, "HTTP message");
        Args.notNull(sessionBuffer, "Session output buffer");
        this.writeHeadLine(message, this.lineBuf);
        sessionBuffer.writeLine(this.lineBuf);
        Iterator<Header> it = message.headerIterator();
        while (it.hasNext()) {
            Header header = it.next();
            if (header instanceof FormattedHeader) {
                CharArrayBuffer buffer = ((FormattedHeader)header).getBuffer();
                sessionBuffer.writeLine(buffer);
                continue;
            }
            this.lineBuf.clear();
            this.lineFormatter.formatHeader(this.lineBuf, header);
            sessionBuffer.writeLine(this.lineBuf);
        }
        this.lineBuf.clear();
        sessionBuffer.writeLine(this.lineBuf);
    }
}

