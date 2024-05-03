/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.nio.ContentDecoder;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;

public abstract class AbstractContentDecoder
implements ContentDecoder {
    final ReadableByteChannel channel;
    final SessionInputBuffer buffer;
    final BasicHttpTransportMetrics metrics;
    protected boolean completed;

    public AbstractContentDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, BasicHttpTransportMetrics metrics) {
        Args.notNull(channel, "Channel");
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(metrics, "Transport metrics");
        this.buffer = buffer;
        this.channel = channel;
        this.metrics = metrics;
    }

    protected ReadableByteChannel channel() {
        return this.channel;
    }

    protected SessionInputBuffer buffer() {
        return this.buffer;
    }

    protected BasicHttpTransportMetrics metrics() {
        return this.metrics;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    protected void setCompleted() {
        this.completed = true;
    }

    protected int readFromChannel(ByteBuffer dst) throws IOException {
        int bytesRead = this.channel.read(dst);
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }

    protected int fillBufferFromChannel() throws IOException {
        int bytesRead = this.buffer.fill(this.channel);
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }

    protected int readFromChannel(ByteBuffer dst, int limit) throws IOException {
        int bytesRead;
        if (dst.remaining() > limit) {
            int oldLimit = dst.limit();
            int newLimit = oldLimit - (dst.remaining() - limit);
            dst.limit(newLimit);
            bytesRead = this.channel.read(dst);
            dst.limit(oldLimit);
        } else {
            bytesRead = this.channel.read(dst);
        }
        if (bytesRead > 0) {
            this.metrics.incrementBytesTransferred(bytesRead);
        }
        return bytesRead;
    }

    @Override
    public List<? extends Header> getTrailers() {
        return null;
    }
}

