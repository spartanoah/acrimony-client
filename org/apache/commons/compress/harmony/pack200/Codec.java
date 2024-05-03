/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;

public abstract class Codec {
    public static final BHSDCodec BCI5 = new BHSDCodec(5, 4);
    public static final BHSDCodec BRANCH5 = new BHSDCodec(5, 4, 2);
    public static final BHSDCodec BYTE1 = new BHSDCodec(1, 256);
    public static final BHSDCodec CHAR3 = new BHSDCodec(3, 128);
    public static final BHSDCodec DELTA5 = new BHSDCodec(5, 64, 1, 1);
    public static final BHSDCodec MDELTA5 = new BHSDCodec(5, 64, 2, 1);
    public static final BHSDCodec SIGNED5 = new BHSDCodec(5, 64, 1);
    public static final BHSDCodec UDELTA5 = new BHSDCodec(5, 64, 0, 1);
    public static final BHSDCodec UNSIGNED5 = new BHSDCodec(5, 64);
    public int lastBandLength;

    public abstract int decode(InputStream var1) throws IOException, Pack200Exception;

    public abstract byte[] encode(int var1, int var2) throws Pack200Exception;

    public abstract byte[] encode(int var1) throws Pack200Exception;

    public abstract int decode(InputStream var1, long var2) throws IOException, Pack200Exception;

    public int[] decodeInts(int n, InputStream in) throws IOException, Pack200Exception {
        this.lastBandLength = 0;
        int[] result = new int[n];
        int last = 0;
        for (int i = 0; i < n; ++i) {
            result[i] = last = this.decode(in, last);
        }
        return result;
    }

    public int[] decodeInts(int n, InputStream in, int firstValue) throws IOException, Pack200Exception {
        int[] result = new int[n + 1];
        result[0] = firstValue;
        int last = firstValue;
        for (int i = 1; i < n + 1; ++i) {
            result[i] = last = this.decode(in, last);
        }
        return result;
    }

    public byte[] encode(int[] ints) throws Pack200Exception {
        int total = 0;
        byte[][] bytes = new byte[ints.length][];
        for (int i = 0; i < ints.length; ++i) {
            bytes[i] = this.encode(ints[i], i > 0 ? ints[i - 1] : 0);
            total += bytes[i].length;
        }
        byte[] encoded = new byte[total];
        int index = 0;
        for (int i = 0; i < bytes.length; ++i) {
            System.arraycopy(bytes[i], 0, encoded, index, bytes[i].length);
            index += bytes[i].length;
        }
        return encoded;
    }
}

