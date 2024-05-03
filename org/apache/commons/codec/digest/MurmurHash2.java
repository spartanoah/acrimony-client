/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.codec.digest;

import org.apache.commons.codec.binary.StringUtils;

public final class MurmurHash2 {
    private static final int M32 = 1540483477;
    private static final int R32 = 24;
    private static final long M64 = -4132994306676758123L;
    private static final int R64 = 47;

    private MurmurHash2() {
    }

    public static int hash32(byte[] data, int length, int seed) {
        int h = seed ^ length;
        int nblocks = length >> 2;
        for (int i = 0; i < nblocks; ++i) {
            int index = i << 2;
            int k = MurmurHash2.getLittleEndianInt(data, index);
            k *= 1540483477;
            k ^= k >>> 24;
            h *= 1540483477;
            h ^= (k *= 1540483477);
        }
        int index = nblocks << 2;
        switch (length - index) {
            case 3: {
                h ^= (data[index + 2] & 0xFF) << 16;
            }
            case 2: {
                h ^= (data[index + 1] & 0xFF) << 8;
            }
            case 1: {
                h ^= data[index] & 0xFF;
                h *= 1540483477;
            }
        }
        h ^= h >>> 13;
        h *= 1540483477;
        h ^= h >>> 15;
        return h;
    }

    public static int hash32(byte[] data, int length) {
        return MurmurHash2.hash32(data, length, -1756908916);
    }

    public static int hash32(String text) {
        byte[] bytes = StringUtils.getBytesUtf8(text);
        return MurmurHash2.hash32(bytes, bytes.length);
    }

    public static int hash32(String text, int from, int length) {
        return MurmurHash2.hash32(text.substring(from, from + length));
    }

    public static long hash64(byte[] data, int length, int seed) {
        long h = (long)seed & 0xFFFFFFFFL ^ (long)length * -4132994306676758123L;
        int nblocks = length >> 3;
        for (int i = 0; i < nblocks; ++i) {
            int index = i << 3;
            long k = MurmurHash2.getLittleEndianLong(data, index);
            k *= -4132994306676758123L;
            k ^= k >>> 47;
            h ^= (k *= -4132994306676758123L);
            h *= -4132994306676758123L;
        }
        int index = nblocks << 3;
        switch (length - index) {
            case 7: {
                h ^= ((long)data[index + 6] & 0xFFL) << 48;
            }
            case 6: {
                h ^= ((long)data[index + 5] & 0xFFL) << 40;
            }
            case 5: {
                h ^= ((long)data[index + 4] & 0xFFL) << 32;
            }
            case 4: {
                h ^= ((long)data[index + 3] & 0xFFL) << 24;
            }
            case 3: {
                h ^= ((long)data[index + 2] & 0xFFL) << 16;
            }
            case 2: {
                h ^= ((long)data[index + 1] & 0xFFL) << 8;
            }
            case 1: {
                h ^= (long)data[index] & 0xFFL;
                h *= -4132994306676758123L;
            }
        }
        h ^= h >>> 47;
        h *= -4132994306676758123L;
        h ^= h >>> 47;
        return h;
    }

    public static long hash64(byte[] data, int length) {
        return MurmurHash2.hash64(data, length, -512093083);
    }

    public static long hash64(String text) {
        byte[] bytes = StringUtils.getBytesUtf8(text);
        return MurmurHash2.hash64(bytes, bytes.length);
    }

    public static long hash64(String text, int from, int length) {
        return MurmurHash2.hash64(text.substring(from, from + length));
    }

    private static int getLittleEndianInt(byte[] data, int index) {
        return data[index] & 0xFF | (data[index + 1] & 0xFF) << 8 | (data[index + 2] & 0xFF) << 16 | (data[index + 3] & 0xFF) << 24;
    }

    private static long getLittleEndianLong(byte[] data, int index) {
        return (long)data[index] & 0xFFL | ((long)data[index + 1] & 0xFFL) << 8 | ((long)data[index + 2] & 0xFFL) << 16 | ((long)data[index + 3] & 0xFFL) << 24 | ((long)data[index + 4] & 0xFFL) << 32 | ((long)data[index + 5] & 0xFFL) << 40 | ((long)data[index + 6] & 0xFFL) << 48 | ((long)data[index + 7] & 0xFFL) << 56;
    }
}

