/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.ByteBufChecksum;
import io.netty.handler.codec.compression.CompressionUtil;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

public final class Lz4XXHash32
extends ByteBufChecksum {
    private static final XXHash32 XXHASH32 = XXHashFactory.fastestInstance().hash32();
    private final int seed;
    private boolean used;
    private int value;

    public Lz4XXHash32(int seed) {
        this.seed = seed;
    }

    @Override
    public void update(int b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(byte[] b, int off, int len) {
        if (this.used) {
            throw new IllegalStateException();
        }
        this.value = XXHASH32.hash(b, off, len, this.seed);
        this.used = true;
    }

    @Override
    public void update(ByteBuf b, int off, int len) {
        if (this.used) {
            throw new IllegalStateException();
        }
        this.value = b.hasArray() ? XXHASH32.hash(b.array(), b.arrayOffset() + off, len, this.seed) : XXHASH32.hash(CompressionUtil.safeNioBuffer(b, off, len), this.seed);
        this.used = true;
    }

    @Override
    public long getValue() {
        if (!this.used) {
            throw new IllegalStateException();
        }
        return (long)this.value & 0xFFFFFFFL;
    }

    @Override
    public void reset() {
        this.used = false;
    }
}

