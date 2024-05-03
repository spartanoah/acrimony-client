/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import org.apache.hc.core5.annotation.Internal;

@Internal
public class ExpandableBuffer {
    private Mode mode;
    private ByteBuffer buffer;

    protected ExpandableBuffer(int bufferSize) {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.mode = Mode.INPUT;
    }

    protected Mode mode() {
        return this.mode;
    }

    protected ByteBuffer buffer() {
        return this.buffer;
    }

    protected void setOutputMode() {
        if (this.mode != Mode.OUTPUT) {
            this.buffer.flip();
            this.mode = Mode.OUTPUT;
        }
    }

    protected void setInputMode() {
        if (this.mode != Mode.INPUT) {
            if (this.buffer.hasRemaining()) {
                this.buffer.compact();
            } else {
                this.buffer.clear();
            }
            this.mode = Mode.INPUT;
        }
    }

    private void expandCapacity(int capacity) {
        ByteBuffer oldBuffer = this.buffer;
        this.buffer = ByteBuffer.allocate(capacity);
        oldBuffer.flip();
        this.buffer.put(oldBuffer);
    }

    protected void expand() throws BufferOverflowException {
        int newcapacity = this.buffer.capacity() + 1 << 1;
        if (newcapacity < 0) {
            int vmBytes = 8;
            int javaBytes = 8;
            int headRoom = 8;
            newcapacity = 0x7FFFFFF7;
            if (newcapacity <= this.buffer.capacity()) {
                throw new BufferOverflowException();
            }
        }
        this.expandCapacity(newcapacity);
    }

    protected void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity > this.buffer.capacity()) {
            this.expandCapacity(requiredCapacity);
        }
    }

    protected void ensureAdjustedCapacity(int requiredCapacity) {
        if (requiredCapacity > this.buffer.capacity()) {
            int adjustedCapacity = (requiredCapacity >> 10) + 1 << 10;
            this.expandCapacity(adjustedCapacity);
        }
    }

    protected boolean hasData() {
        this.setOutputMode();
        return this.buffer.hasRemaining();
    }

    protected int length() {
        this.setOutputMode();
        return this.buffer.remaining();
    }

    protected int capacity() {
        this.setInputMode();
        return this.buffer.remaining();
    }

    protected void clear() {
        this.buffer.clear();
        this.mode = Mode.INPUT;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[mode=");
        sb.append((Object)this.mode);
        sb.append(" pos=");
        sb.append(this.buffer.position());
        sb.append(" lim=");
        sb.append(this.buffer.limit());
        sb.append(" cap=");
        sb.append(this.buffer.capacity());
        sb.append("]");
        return sb.toString();
    }

    public static enum Mode {
        INPUT,
        OUTPUT;

    }
}

