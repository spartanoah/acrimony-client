/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.StreamChannel;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public abstract class AbstractCharAsyncEntityProducer
implements AsyncEntityProducer {
    private static final CharBuffer EMPTY = CharBuffer.wrap(new char[0]);
    private final ByteBuffer bytebuf;
    private final int fragmentSizeHint;
    private final ContentType contentType;
    private final CharsetEncoder charsetEncoder;
    private volatile State state;

    public AbstractCharAsyncEntityProducer(int bufferSize, int fragmentSizeHint, ContentType contentType) {
        Charset charset;
        Args.positive(bufferSize, "Buffer size");
        this.fragmentSizeHint = fragmentSizeHint >= 0 ? fragmentSizeHint : 0;
        this.bytebuf = ByteBuffer.allocate(bufferSize);
        this.contentType = contentType;
        Charset charset2 = charset = contentType != null ? contentType.getCharset() : null;
        if (charset == null) {
            charset = StandardCharsets.US_ASCII;
        }
        this.charsetEncoder = charset.newEncoder();
        this.state = State.ACTIVE;
    }

    private void flush(StreamChannel<ByteBuffer> channel) throws IOException {
        if (this.bytebuf.position() > 0) {
            this.bytebuf.flip();
            channel.write(this.bytebuf);
            this.bytebuf.compact();
        }
    }

    final int writeData(StreamChannel<ByteBuffer> channel, CharBuffer src) throws IOException {
        int chunk = src.remaining();
        if (chunk == 0) {
            return 0;
        }
        int p = src.position();
        CoderResult result = this.charsetEncoder.encode(src, this.bytebuf, false);
        if (result.isError()) {
            result.throwException();
        }
        if (!this.bytebuf.hasRemaining() || this.bytebuf.position() >= this.fragmentSizeHint) {
            this.flush(channel);
        }
        return src.position() - p;
    }

    final void streamEnd(StreamChannel<ByteBuffer> channel) throws IOException {
        if (this.state == State.ACTIVE) {
            CoderResult result2;
            CoderResult result;
            this.state = State.FLUSHING;
            if (!this.bytebuf.hasRemaining()) {
                this.flush(channel);
            }
            if ((result = this.charsetEncoder.encode(EMPTY, this.bytebuf, true)).isError()) {
                result.throwException();
            }
            if ((result2 = this.charsetEncoder.flush(this.bytebuf)).isError()) {
                result.throwException();
            } else if (result.isUnderflow()) {
                this.flush(channel);
                if (this.bytebuf.position() == 0) {
                    this.state = State.END_STREAM;
                    channel.endStream();
                }
            }
        }
    }

    protected abstract int availableData();

    protected abstract void produceData(StreamChannel<CharBuffer> var1) throws IOException;

    @Override
    public final String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public long getContentLength() {
        return -1L;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
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
                this.produceData(new StreamChannel<CharBuffer>(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public int write(CharBuffer src) throws IOException {
                        Args.notNull(src, "Buffer");
                        ByteBuffer byteBuffer = AbstractCharAsyncEntityProducer.this.bytebuf;
                        synchronized (byteBuffer) {
                            return AbstractCharAsyncEntityProducer.this.writeData(channel, src);
                        }
                    }

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void endStream() throws IOException {
                        ByteBuffer byteBuffer = AbstractCharAsyncEntityProducer.this.bytebuf;
                        synchronized (byteBuffer) {
                            AbstractCharAsyncEntityProducer.this.streamEnd(channel);
                        }
                    }
                });
            }
            if (this.state == State.FLUSHING) {
                CoderResult result = this.charsetEncoder.flush(this.bytebuf);
                if (result.isError()) {
                    result.throwException();
                } else if (result.isOverflow()) {
                    this.flush(channel);
                } else if (result.isUnderflow()) {
                    this.flush(channel);
                    if (this.bytebuf.position() == 0) {
                        this.state = State.END_STREAM;
                        channel.endStream();
                    }
                }
            }
        }
    }

    @Override
    public void releaseResources() {
        this.state = State.ACTIVE;
        this.charsetEncoder.reset();
    }

    static enum State {
        ACTIVE,
        FLUSHING,
        END_STREAM;

    }
}

