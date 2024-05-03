/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;

public final class BHSDCodec
extends Codec {
    private final int b;
    private final int d;
    private final int h;
    private final int l;
    private final int s;
    private long cardinality;
    private final long smallest;
    private final long largest;
    private final long[] powers;

    public BHSDCodec(int b, int h) {
        this(b, h, 0, 0);
    }

    public BHSDCodec(int b, int h, int s) {
        this(b, h, s, 0);
    }

    public BHSDCodec(int b, int h, int s, int d) {
        if (b < 1 || b > 5) {
            throw new IllegalArgumentException("1<=b<=5");
        }
        if (h < 1 || h > 256) {
            throw new IllegalArgumentException("1<=h<=256");
        }
        if (s < 0 || s > 2) {
            throw new IllegalArgumentException("0<=s<=2");
        }
        if (d < 0 || d > 1) {
            throw new IllegalArgumentException("0<=d<=1");
        }
        if (b == 1 && h != 256) {
            throw new IllegalArgumentException("b=1 -> h=256");
        }
        if (h == 256 && b == 5) {
            throw new IllegalArgumentException("h=256 -> b!=5");
        }
        this.b = b;
        this.h = h;
        this.s = s;
        this.d = d;
        this.l = 256 - h;
        this.cardinality = h == 1 ? (long)(b * 255 + 1) : (long)((double)((long)((double)this.l * (1.0 - Math.pow(h, b)) / (double)(1 - h))) + Math.pow(h, b));
        this.smallest = this.calculateSmallest();
        this.largest = this.calculateLargest();
        this.powers = new long[b];
        for (int c = 0; c < b; ++c) {
            this.powers[c] = (long)Math.pow(h, c);
        }
    }

    public long cardinality() {
        return this.cardinality;
    }

    @Override
    public int decode(InputStream in) throws IOException, Pack200Exception {
        if (this.d != 0) {
            throw new Pack200Exception("Delta encoding used without passing in last value; this is a coding error");
        }
        return this.decode(in, 0L);
    }

    @Override
    public int decode(InputStream in, long last) throws IOException, Pack200Exception {
        int n = 0;
        long z = 0L;
        long x = 0L;
        do {
            x = in.read();
            ++this.lastBandLength;
            z += x * this.powers[n];
        } while (x >= (long)this.l && ++n < this.b);
        if (x == -1L) {
            throw new EOFException("End of stream reached whilst decoding");
        }
        if (this.isSigned()) {
            int u = (1 << this.s) - 1;
            z = (z & (long)u) == (long)u ? z >>> this.s ^ 0xFFFFFFFFFFFFFFFFL : (z -= z >>> this.s);
        }
        if (this.isDelta()) {
            z += last;
        }
        return (int)z;
    }

    @Override
    public int[] decodeInts(int n, InputStream in) throws IOException, Pack200Exception {
        int[] band = super.decodeInts(n, in);
        if (this.isDelta()) {
            for (int i = 0; i < band.length; ++i) {
                while ((long)band[i] > this.largest) {
                    int n2 = i;
                    band[n2] = (int)((long)band[n2] - this.cardinality);
                }
                while ((long)band[i] < this.smallest) {
                    int n3 = i;
                    band[n3] = (int)((long)band[n3] + this.cardinality);
                }
            }
        }
        return band;
    }

    @Override
    public int[] decodeInts(int n, InputStream in, int firstValue) throws IOException, Pack200Exception {
        int[] band = super.decodeInts(n, in, firstValue);
        if (this.isDelta()) {
            for (int i = 0; i < band.length; ++i) {
                while ((long)band[i] > this.largest) {
                    int n2 = i;
                    band[n2] = (int)((long)band[n2] - this.cardinality);
                }
                while ((long)band[i] < this.smallest) {
                    int n3 = i;
                    band[n3] = (int)((long)band[n3] + this.cardinality);
                }
            }
        }
        return band;
    }

    public boolean encodes(long value) {
        return value >= this.smallest && value <= this.largest;
    }

    @Override
    public byte[] encode(int value, int last) throws Pack200Exception {
        if (!this.encodes(value)) {
            throw new Pack200Exception("The codec " + this.toString() + " does not encode the value " + value);
        }
        long z = value;
        if (this.isDelta()) {
            z -= (long)last;
        }
        if (this.isSigned()) {
            if (z < Integer.MIN_VALUE) {
                z += 0x100000000L;
            } else if (z > Integer.MAX_VALUE) {
                z -= 0x100000000L;
            }
            z = z < 0L ? (-z << this.s) - 1L : (this.s == 1 ? (z <<= this.s) : (z += (z - z % 3L) / 3L));
        } else if (z < 0L) {
            z = this.cardinality < 0x100000000L ? (z += this.cardinality) : (z += 0x100000000L);
        }
        if (z < 0L) {
            throw new Pack200Exception("unable to encode");
        }
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        for (int n = 0; n < this.b; ++n) {
            long byteN;
            if (z < (long)this.l) {
                byteN = z;
            } else {
                for (byteN = z % (long)this.h; byteN < (long)this.l; byteN += (long)this.h) {
                }
            }
            byteList.add((byte)byteN);
            if (byteN < (long)this.l) break;
            z -= byteN;
            z /= (long)this.h;
        }
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = (Byte)byteList.get(i);
        }
        return bytes;
    }

    @Override
    public byte[] encode(int value) throws Pack200Exception {
        return this.encode(value, 0);
    }

    public boolean isDelta() {
        return this.d != 0;
    }

    public boolean isSigned() {
        return this.s != 0;
    }

    public long largest() {
        return this.largest;
    }

    private long calculateLargest() {
        long result;
        if (this.d == 1) {
            BHSDCodec bh0 = new BHSDCodec(this.b, this.h);
            return bh0.largest();
        }
        if (this.s == 0) {
            result = this.cardinality() - 1L;
        } else if (this.s == 1) {
            result = this.cardinality() / 2L - 1L;
        } else if (this.s == 2) {
            result = 3L * this.cardinality() / 4L - 1L;
        } else {
            throw new Error("Unknown s value");
        }
        return Math.min((this.s == 0 ? 0xFFFFFFFEL : Integer.MAX_VALUE) - 1L, result);
    }

    public long smallest() {
        return this.smallest;
    }

    private long calculateSmallest() {
        long result = this.d == 1 || !this.isSigned() ? (this.cardinality >= 0x100000000L ? Integer.MIN_VALUE : 0L) : Math.max(Integer.MIN_VALUE, -this.cardinality() / (long)(1 << this.s));
        return result;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(11);
        buffer.append('(');
        buffer.append(this.b);
        buffer.append(',');
        buffer.append(this.h);
        if (this.s != 0 || this.d != 0) {
            buffer.append(',');
            buffer.append(this.s);
        }
        if (this.d != 0) {
            buffer.append(',');
            buffer.append(this.d);
        }
        buffer.append(')');
        return buffer.toString();
    }

    public int getB() {
        return this.b;
    }

    public int getH() {
        return this.h;
    }

    public int getS() {
        return this.s;
    }

    public int getL() {
        return this.l;
    }

    public boolean equals(Object o) {
        if (o instanceof BHSDCodec) {
            BHSDCodec codec = (BHSDCodec)o;
            return codec.b == this.b && codec.h == this.h && codec.s == this.s && codec.d == this.d;
        }
        return false;
    }

    public int hashCode() {
        return ((this.b * 37 + this.h) * 37 + this.s) * 37 + this.d;
    }
}

