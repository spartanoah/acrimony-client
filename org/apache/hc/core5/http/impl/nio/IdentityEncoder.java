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

public class IdentityEncoder
extends AbstractContentEncoder
implements FileContentEncoder {
    private final int fragHint;

    public IdentityEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics, int chunkSizeHint) {
        super(channel, buffer, metrics);
        this.fragHint = chunkSizeHint > 0 ? chunkSizeHint : 0;
    }

    public IdentityEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics) {
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
            int capacity;
            if ((this.buffer.hasData() || this.fragHint > 0) && src.remaining() <= this.fragHint && (capacity = this.fragHint - this.buffer.length()) > 0) {
                int limit = Math.min(capacity, src.remaining());
                int bytesWritten2 = this.writeToBuffer(src, limit);
                total += bytesWritten2;
            }
            if (this.buffer.hasData() && (this.buffer.length() >= this.fragHint || src.hasRemaining()) && (bytesWritten = this.flushToChannel()) == 0) break;
            if (this.buffer.hasData() || src.remaining() <= this.fragHint) continue;
            bytesWritten = this.writeToChannel(src);
            total += bytesWritten;
            if (bytesWritten != 0) continue;
            break;
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
        long bytesWritten = src.transferTo(position, count, this.channel);
        if (bytesWritten > 0L) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        return bytesWritten;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[identity; completed: ");
        sb.append(this.isCompleted());
        sb.append("]");
        return sb.toString();
    }
}

