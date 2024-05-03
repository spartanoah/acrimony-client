/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.nio.AbstractContentEncoder;
import org.apache.hc.core5.http.nio.FileContentEncoder;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;

public class LengthDelimitedEncoder
extends AbstractContentEncoder
implements FileContentEncoder {
    private final long contentLength;
    private final int fragHint;
    private long remaining;

    public LengthDelimitedEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics, long contentLength, int chunkSizeHint) {
        super(channel, buffer, metrics);
        Args.notNegative(contentLength, "Content length");
        this.contentLength = contentLength;
        this.fragHint = chunkSizeHint > 0 ? chunkSizeHint : 0;
        this.remaining = contentLength;
    }

    public LengthDelimitedEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics, long contentLength) {
        this(channel, buffer, metrics, contentLength, 0);
    }

    private int nextChunk(ByteBuffer src) {
        return (int)Math.min(Math.min(this.remaining, Integer.MAX_VALUE), (long)src.remaining());
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (src == null) {
            return 0;
        }
        this.assertNotCompleted();
        int total = 0;
        while (src.hasRemaining() && this.remaining > 0L) {
            int bytesWritten;
            int capacity;
            int chunk;
            if ((this.buffer.hasData() || this.fragHint > 0) && (chunk = this.nextChunk(src)) <= this.fragHint && (capacity = this.fragHint - this.buffer.length()) > 0) {
                int limit = Math.min(capacity, chunk);
                int bytesWritten2 = this.writeToBuffer(src, limit);
                this.remaining -= (long)bytesWritten2;
                total += bytesWritten2;
            }
            if (this.buffer.hasData()) {
                chunk = this.nextChunk(src);
                if ((this.buffer.length() >= this.fragHint || chunk > 0) && (bytesWritten = this.flushToChannel()) == 0) break;
            }
            if (this.buffer.hasData() || (chunk = this.nextChunk(src)) <= this.fragHint) continue;
            bytesWritten = this.writeToChannel(src, chunk);
            this.remaining -= (long)bytesWritten;
            total += bytesWritten;
            if (bytesWritten != 0) continue;
            break;
        }
        if (this.remaining <= 0L) {
            super.complete(null);
        }
        return total;
    }

    @Override
    public long transfer(FileChannel src, long position, long count) throws IOException {
        if (src == null) {
            return 0L;
        }
        this.assertNotCompleted();
        this.flushToChannel();
        if (this.buffer.hasData()) {
            return 0L;
        }
        long chunk = Math.min(this.remaining, count);
        long bytesWritten = src.transferTo(position, chunk, this.channel);
        if (bytesWritten > 0L) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        this.remaining -= bytesWritten;
        if (this.remaining <= 0L) {
            super.complete(null);
        }
        return bytesWritten;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[content length: ");
        sb.append(this.contentLength);
        sb.append("; pos: ");
        sb.append(this.contentLength - this.remaining);
        sb.append("; completed: ");
        sb.append(this.isCompleted());
        sb.append("]");
        return sb.toString();
    }
}

