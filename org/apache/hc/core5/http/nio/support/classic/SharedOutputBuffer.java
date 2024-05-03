/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.support.classic.AbstractSharedBuffer;
import org.apache.hc.core5.http.nio.support.classic.ContentOutputBuffer;

@Contract(threading=ThreadingBehavior.SAFE)
public final class SharedOutputBuffer
extends AbstractSharedBuffer
implements ContentOutputBuffer {
    private volatile DataStreamChannel dataStreamChannel;
    private volatile boolean hasCapacity = false;
    private volatile boolean endStreamPropagated = false;

    public SharedOutputBuffer(ReentrantLock lock, int initialBufferSize) {
        super(lock, initialBufferSize);
    }

    public SharedOutputBuffer(int bufferSize) {
        this(new ReentrantLock(), bufferSize);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void flush(DataStreamChannel channel) throws IOException {
        this.lock.lock();
        try {
            this.dataStreamChannel = channel;
            this.hasCapacity = true;
            this.setOutputMode();
            if (this.buffer().hasRemaining()) {
                this.dataStreamChannel.write(this.buffer());
            }
            if (!this.buffer().hasRemaining() && this.endStream) {
                this.propagateEndStream();
            }
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    private void ensureNotAborted() throws InterruptedIOException {
        if (this.aborted) {
            throw new InterruptedIOException("Operation aborted");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ByteBuffer src = ByteBuffer.wrap(b, off, len);
        this.lock.lock();
        try {
            this.ensureNotAborted();
            this.setInputMode();
            while (src.hasRemaining()) {
                int bytesWritten;
                if (src.remaining() < 1024 && this.buffer().remaining() > src.remaining()) {
                    this.buffer().put(src);
                    continue;
                }
                if (this.buffer().position() > 0 || this.dataStreamChannel == null) {
                    this.waitFlush();
                }
                if (this.buffer().position() != 0 || this.dataStreamChannel == null || (bytesWritten = this.dataStreamChannel.write(src)) != 0) continue;
                this.hasCapacity = false;
                this.waitFlush();
            }
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(int b) throws IOException {
        this.lock.lock();
        try {
            this.ensureNotAborted();
            this.setInputMode();
            if (!this.buffer().hasRemaining()) {
                this.waitFlush();
            }
            this.buffer().put((byte)b);
        } finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeCompleted() throws IOException {
        if (this.endStream) {
            return;
        }
        this.lock.lock();
        try {
            if (!this.endStream) {
                this.endStream = true;
                if (this.dataStreamChannel != null) {
                    this.setOutputMode();
                    if (this.buffer().hasRemaining()) {
                        this.dataStreamChannel.requestOutput();
                    } else {
                        this.propagateEndStream();
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    private void waitFlush() throws InterruptedIOException {
        this.setOutputMode();
        if (this.dataStreamChannel != null) {
            this.dataStreamChannel.requestOutput();
        }
        this.ensureNotAborted();
        while (this.buffer().hasRemaining() || !this.hasCapacity) {
            try {
                this.condition.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException(ex.getMessage());
            }
            this.ensureNotAborted();
        }
        this.setInputMode();
    }

    private void propagateEndStream() throws IOException {
        if (!this.endStreamPropagated) {
            this.dataStreamChannel.endStream();
            this.endStreamPropagated = true;
        }
    }
}

