/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import com.ibm.icu.impl.Utility;
import java.nio.ByteBuffer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ByteArrayWrapper
implements Comparable<ByteArrayWrapper> {
    public byte[] bytes;
    public int size;

    public ByteArrayWrapper() {
    }

    public ByteArrayWrapper(byte[] bytesToAdopt, int size) {
        if (bytesToAdopt == null && size != 0 || size < 0 || size > bytesToAdopt.length) {
            throw new IndexOutOfBoundsException("illegal size: " + size);
        }
        this.bytes = bytesToAdopt;
        this.size = size;
    }

    public ByteArrayWrapper(ByteBuffer source) {
        this.size = source.limit();
        this.bytes = new byte[this.size];
        source.get(this.bytes, 0, this.size);
    }

    public ByteArrayWrapper ensureCapacity(int capacity) {
        if (this.bytes == null || this.bytes.length < capacity) {
            byte[] newbytes = new byte[capacity];
            ByteArrayWrapper.copyBytes(this.bytes, 0, newbytes, 0, this.size);
            this.bytes = newbytes;
        }
        return this;
    }

    public final ByteArrayWrapper set(byte[] src, int start, int limit) {
        this.size = 0;
        this.append(src, start, limit);
        return this;
    }

    public final ByteArrayWrapper append(byte[] src, int start, int limit) {
        int len = limit - start;
        this.ensureCapacity(this.size + len);
        ByteArrayWrapper.copyBytes(src, start, this.bytes, this.size, len);
        this.size += len;
        return this;
    }

    public final byte[] releaseBytes() {
        byte[] result = this.bytes;
        this.bytes = null;
        this.size = 0;
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.size; ++i) {
            if (i != 0) {
                result.append(" ");
            }
            result.append(Utility.hex(this.bytes[i] & 0xFF, 2));
        }
        return result.toString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        try {
            ByteArrayWrapper that = (ByteArrayWrapper)other;
            if (this.size != that.size) {
                return false;
            }
            for (int i = 0; i < this.size; ++i) {
                if (this.bytes[i] == that.bytes[i]) continue;
                return false;
            }
            return true;
        } catch (ClassCastException classCastException) {
            return false;
        }
    }

    public int hashCode() {
        int result = this.bytes.length;
        for (int i = 0; i < this.size; ++i) {
            result = 37 * result + this.bytes[i];
        }
        return result;
    }

    @Override
    public int compareTo(ByteArrayWrapper other) {
        if (this == other) {
            return 0;
        }
        int minSize = this.size < other.size ? this.size : other.size;
        for (int i = 0; i < minSize; ++i) {
            if (this.bytes[i] == other.bytes[i]) continue;
            return (this.bytes[i] & 0xFF) - (other.bytes[i] & 0xFF);
        }
        return this.size - other.size;
    }

    private static final void copyBytes(byte[] src, int srcoff, byte[] tgt, int tgtoff, int length) {
        if (length < 64) {
            int i = srcoff;
            int n = tgtoff;
            while (--length >= 0) {
                tgt[n] = src[i];
                ++i;
                ++n;
            }
        } else {
            System.arraycopy(src, srcoff, tgt, tgtoff, length);
        }
    }
}

