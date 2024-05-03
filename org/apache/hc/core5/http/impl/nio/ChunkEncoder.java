/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.nio.AbstractContentEncoder;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.util.CharArrayBuffer;

public class ChunkEncoder
extends AbstractContentEncoder {
    private final int chunkSizeHint;
    private final CharArrayBuffer lineBuffer;

    public ChunkEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics, int chunkSizeHint) {
        super(channel, buffer, metrics);
        this.chunkSizeHint = chunkSizeHint > 0 ? chunkSizeHint : 0;
        this.lineBuffer = new CharArrayBuffer(16);
    }

    public ChunkEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics) {
        this(channel, buffer, metrics, 0);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (src == null) {
            return 0;
        }
        this.assertNotCompleted();
        int total = 0;
        while (src.hasRemaining()) {
            int bytesWritten;
            int chunk = src.remaining();
            int avail = this.buffer.capacity();
            if ((avail -= 12) > 0) {
                if (avail < chunk) {
                    chunk = avail;
                    this.lineBuffer.clear();
                    this.lineBuffer.append(Integer.toHexString(chunk));
                    this.buffer.writeLine(this.lineBuffer);
                    int oldlimit = src.limit();
                    src.limit(src.position() + chunk);
                    this.buffer.write(src);
                    src.limit(oldlimit);
                } else {
                    this.lineBuffer.clear();
                    this.lineBuffer.append(Integer.toHexString(chunk));
                    this.buffer.writeLine(this.lineBuffer);
                    this.buffer.write(src);
                }
                this.lineBuffer.clear();
                this.buffer.writeLine(this.lineBuffer);
                total += chunk;
            }
            if (this.buffer.length() < this.chunkSizeHint && !src.hasRemaining() || (bytesWritten = this.flushToChannel()) != 0) continue;
            break;
        }
        return total;
    }

    @Override
    public void complete(List<? extends Header> trailers) throws IOException {
        this.assertNotCompleted();
        this.lineBuffer.clear();
        this.lineBuffer.append("0");
        this.buffer.writeLine(this.lineBuffer);
        this.writeTrailers(trailers);
        this.lineBuffer.clear();
        this.buffer.writeLine(this.lineBuffer);
        super.complete(trailers);
    }

    private void writeTrailers(List<? extends Header> trailers) throws IOException {
        if (trailers != null) {
            for (int i = 0; i < trailers.size(); ++i) {
                Header header = trailers.get(i);
                if (header instanceof FormattedHeader) {
                    CharArrayBuffer chbuffer = ((FormattedHeader)header).getBuffer();
                    this.buffer.writeLine(chbuffer);
                    continue;
                }
                this.lineBuffer.clear();
                BasicLineFormatter.INSTANCE.formatHeader(this.lineBuffer, header);
                this.buffer.writeLine(this.lineBuffer);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[chunk-coded; completed: ");
        sb.append(this.isCompleted());
        sb.append("]");
        return sb.toString();
    }
}

