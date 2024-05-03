/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.io.HttpMessageWriter;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public abstract class AbstractMessageWriter<T extends HttpMessage>
implements HttpMessageWriter<T> {
    private final CharArrayBuffer lineBuf;
    private final LineFormatter lineFormatter;

    public AbstractMessageWriter(LineFormatter formatter) {
        this.lineFormatter = formatter != null ? formatter : BasicLineFormatter.INSTANCE;
        this.lineBuf = new CharArrayBuffer(128);
    }

    LineFormatter getLineFormatter() {
        return this.lineFormatter;
    }

    protected abstract void writeHeadLine(T var1, CharArrayBuffer var2) throws IOException;

    @Override
    public void write(T message, SessionOutputBuffer buffer, OutputStream outputStream) throws IOException, HttpException {
        Args.notNull(message, "HTTP message");
        Args.notNull(buffer, "Session output buffer");
        Args.notNull(outputStream, "Output stream");
        this.writeHeadLine(message, this.lineBuf);
        buffer.writeLine(this.lineBuf, outputStream);
        Iterator<Header> it = message.headerIterator();
        while (it.hasNext()) {
            Header header = it.next();
            if (header instanceof FormattedHeader) {
                CharArrayBuffer chbuffer = ((FormattedHeader)header).getBuffer();
                buffer.writeLine(chbuffer, outputStream);
                continue;
            }
            this.lineBuf.clear();
            this.lineFormatter.formatHeader(this.lineBuf, header);
            buffer.writeLine(this.lineBuf, outputStream);
        }
        this.lineBuf.clear();
        buffer.writeLine(this.lineBuf, outputStream);
    }
}

