/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import org.apache.commons.compress.utils.BoundedArchiveInputStream;

public class BoundedSeekableByteChannelInputStream
extends BoundedArchiveInputStream {
    private final SeekableByteChannel channel;

    public BoundedSeekableByteChannelInputStream(long start, long remaining, SeekableByteChannel channel) {
        super(start, remaining);
        this.channel = channel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected int read(long pos, ByteBuffer buf) throws IOException {
        int read;
        SeekableByteChannel seekableByteChannel = this.channel;
        synchronized (seekableByteChannel) {
            this.channel.position(pos);
            read = this.channel.read(buf);
        }
        buf.flip();
        return read;
    }
}

