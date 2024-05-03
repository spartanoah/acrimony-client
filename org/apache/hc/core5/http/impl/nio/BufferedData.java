/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.hc.core5.http.impl.nio.ExpandableBuffer;
import org.apache.hc.core5.util.Args;

public class BufferedData
extends ExpandableBuffer {
    public static BufferedData allocate(int bufferSize) {
        return new BufferedData(bufferSize);
    }

    protected BufferedData(int bufferSize) {
        super(bufferSize);
    }

    @Override
    public final boolean hasData() {
        return super.hasData();
    }

    @Override
    public final int length() {
        return super.length();
    }

    @Override
    public final int capacity() {
        return super.capacity();
    }

    @Override
    public final void clear() {
        super.clear();
    }

    public final void put(ByteBuffer src) {
        Args.notNull(src, "Data source");
        this.setInputMode();
        int requiredCapacity = this.buffer().position() + src.remaining();
        this.ensureAdjustedCapacity(requiredCapacity);
        this.buffer().put(src);
    }

    public final int readFrom(ReadableByteChannel channel) throws IOException {
        Args.notNull(channel, "Channel");
        this.setInputMode();
        if (!this.buffer().hasRemaining()) {
            this.expand();
        }
        return channel.read(this.buffer());
    }

    public final int writeTo(WritableByteChannel dst) throws IOException {
        if (dst == null) {
            return 0;
        }
        this.setOutputMode();
        return dst.write(this.buffer());
    }

    public final ByteBuffer data() {
        this.setOutputMode();
        return this.buffer();
    }
}

