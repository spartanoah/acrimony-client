/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.StreamChannel;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public abstract class AbstractBinAsyncEntityProducer
implements AsyncEntityProducer {
    private final int fragmentSizeHint;
    private final ByteBuffer bytebuf;
    private final ContentType contentType;
    private volatile State state;

    public AbstractBinAsyncEntityProducer(int fragmentSizeHint, ContentType contentType) {
        this.fragmentSizeHint = fragmentSizeHint >= 0 ? fragmentSizeHint : 0;
        this.bytebuf = ByteBuffer.allocate(this.fragmentSizeHint);
        this.contentType = contentType;
        this.state = State.ACTIVE;
    }

    private void flush(StreamChannel<ByteBuffer> channel) throws IOException {
        if (this.bytebuf.position() > 0) {
            this.bytebuf.flip();
            channel.write(this.bytebuf);
            this.bytebuf.compact();
        }
    }

    final int writeData(StreamChannel<ByteBuffer> channel, ByteBuffer src) throws IOException {
        int chunk = src.remaining();
        if (chunk == 0) {
            return 0;
        }
        if (chunk > this.fragmentSizeHint) {
            this.flush(channel);
            if (this.bytebuf.position() == 0) {
                return channel.write(src);
            }
        } else {
            if (this.bytebuf.remaining() < chunk) {
                this.flush(channel);
            }
            if (this.bytebuf.remaining() >= chunk) {
                this.bytebuf.put(src);
                if (!this.bytebuf.hasRemaining()) {
                    this.flush(channel);
                }
                return chunk;
            }
        }
        return 0;
    }

    final void streamEnd(StreamChannel<ByteBuffer> channel) throws IOException {
        if (this.state == State.ACTIVE) {
            this.state = State.FLUSHING;
            this.flush(channel);
            if (this.bytebuf.position() == 0) {
                this.state = State.END_STREAM;
                channel.endStream();
            }
        }
    }

    protected abstract int availableData();

    protected abstract void produceData(StreamChannel<ByteBuffer> var1) throws IOException;

    @Override
    public final String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public long getContentLength() {
        return -1L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final int available() {
        if (this.state == State.ACTIVE) {
            return this.availableData();
        }
        ByteBuffer byteBuffer = this.bytebuf;
        synchronized (byteBuffer) {
            return this.bytebuf.position();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void produce(final DataStreamChannel channel) throws IOException {
        ByteBuffer byteBuffer = this.bytebuf;
        synchronized (byteBuffer) {
            if (this.state == State.ACTIVE) {
                this.produceData(new StreamChannel<ByteBuffer>(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public int write(ByteBuffer src) throws IOException {
                        Args.notNull(src, "Buffer");
                        ByteBuffer byteBuffer = AbstractBinAsyncEntityProducer.this.bytebuf;
                        synchronized (byteBuffer) {
                            return AbstractBinAsyncEntityProducer.this.writeData(channel, src);
                        }
                    }

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void endStream() throws IOException {
                        ByteBuffer byteBuffer = AbstractBinAsyncEntityProducer.this.bytebuf;
                        synchronized (byteBuffer) {
                            AbstractBinAsyncEntityProducer.this.streamEnd(channel);
                        }
                    }
                });
            }
            if (this.state == State.FLUSHING) {
                this.flush(channel);
                if (this.bytebuf.position() == 0) {
                    this.state = State.END_STREAM;
                    channel.endStream();
                }
            }
        }
    }

    @Override
    public void releaseResources() {
        this.state = State.ACTIVE;
    }

    static enum State {
        ACTIVE,
        FLUSHING,
        END_STREAM;

    }
}

