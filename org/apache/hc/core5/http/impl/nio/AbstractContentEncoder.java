/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.nio.ContentEncoder;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public abstract class AbstractContentEncoder
implements ContentEncoder {
    final WritableByteChannel channel;
    final SessionOutputBuffer buffer;
    final BasicHttpTransportMetrics metrics;
    boolean completed;

    public AbstractContentEncoder(WritableByteChannel channel, SessionOutputBuffer buffer, BasicHttpTransportMetrics metrics) {
        Args.notNull(channel, "Channel");
        Args.notNull(buffer, "Session input buffer");
        Args.notNull(metrics, "Transport metrics");
        this.buffer = buffer;
        this.channel = channel;
        this.metrics = metrics;
    }

    protected WritableByteChannel channel() {
        return this.channel;
    }

    protected SessionOutputBuffer buffer() {
        return this.buffer;
    }

    protected BasicHttpTransportMetrics metrics() {
        return this.metrics;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void complete(List<? extends Header> trailers) throws IOException {
        this.completed = true;
    }

    public final void complete() throws IOException {
        this.complete(null);
    }

    protected void assertNotCompleted() {
        Asserts.check(!this.completed, "Encoding process already completed");
    }

    protected int flushToChannel() throws IOException {
        if (!this.buffer.hasData()) {
            return 0;
        }
        int bytesWritten = this.buffer.flush(this.channel);
        if (bytesWritten > 0) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        return bytesWritten;
    }

    protected int writeToChannel(ByteBuffer src) throws IOException {
        if (!src.hasRemaining()) {
            return 0;
        }
        int bytesWritten = this.channel.write(src);
        if (bytesWritten > 0) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        return bytesWritten;
    }

    protected int writeToChannel(ByteBuffer src, int limit) throws IOException {
        return this.doWriteChunk(src, limit, true);
    }

    protected int writeToBuffer(ByteBuffer src, int limit) throws IOException {
        return this.doWriteChunk(src, limit, false);
    }

    private int doWriteChunk(ByteBuffer src, int chunk, boolean direct) throws IOException {
        int bytesWritten;
        if (src.remaining() > chunk) {
            int oldLimit = src.limit();
            int newLimit = oldLimit - (src.remaining() - chunk);
            src.limit(newLimit);
            bytesWritten = this.doWriteChunk(src, direct);
            src.limit(oldLimit);
        } else {
            bytesWritten = this.doWriteChunk(src, direct);
        }
        return bytesWritten;
    }

    private int doWriteChunk(ByteBuffer src, boolean direct) throws IOException {
        if (direct) {
            int bytesWritten = this.channel.write(src);
            if (bytesWritten > 0) {
                this.metrics.incrementBytesTransferred(bytesWritten);
            }
            return bytesWritten;
        }
        int chunk = src.remaining();
        this.buffer.write(src);
        return chunk;
    }
}

