/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.codec.digest;

import org.apache.commons.codec.binary.StringUtils;

public final class MurmurHash3 {
    @Deprecated
    public static final long NULL_HASHCODE = 2862933555777941757L;
    public static final int DEFAULT_SEED = 104729;
    static final int LONG_BYTES = 8;
    static final int INTEGER_BYTES = 4;
    static final int SHORT_BYTES = 2;
    private static final int C1_32 = -862048943;
    private static final int C2_32 = 461845907;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;
    private static final int M_32 = 5;
    private static final int N_32 = -430675100;
    private static final long C1 = -8663945395140668459L;
    private static final long C2 = 5545529020109919103L;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 1390208809;
    private static final int N2 = 944331445;

    private MurmurHash3() {
    }

    public static int hash32(long data1, long data2) {
        return MurmurHash3.hash32(data1, data2, 104729);
    }

    public static int hash32(long data1, long data2, int seed) {
        int hash = seed;
        long r0 = Long.reverseBytes(data1);
        long r1 = Long.reverseBytes(data2);
        hash = MurmurHash3.mix32((int)r0, hash);
        hash = MurmurHash3.mix32((int)(r0 >>> 32), hash);
        hash = MurmurHash3.mix32((int)r1, hash);
        hash = MurmurHash3.mix32((int)(r1 >>> 32), hash);
        return MurmurHash3.fmix32(hash ^= 0x10);
    }

    public static int hash32(long data) {
        return MurmurHash3.hash32(data, 104729);
    }

    public static int hash32(long data, int seed) {
        int hash = seed;
        long r0 = Long.reverseBytes(data);
        hash = MurmurHash3.mix32((int)r0, hash);
        hash = MurmurHash3.mix32((int)(r0 >>> 32), hash);
        return MurmurHash3.fmix32(hash ^= 8);
    }

    @Deprecated
    public static int hash32(byte[] data) {
        return MurmurHash3.hash32(data, 0, data.length, 104729);
    }

    @Deprecated
    public static int hash32(String data) {
        byte[] bytes = StringUtils.getBytesUtf8(data);
        return MurmurHash3.hash32(bytes, 0, bytes.length, 104729);
    }

    @Deprecated
    public static int hash32(byte[] data, int length) {
        return MurmurHash3.hash32(data, length, 104729);
    }

    @Deprecated
    public static int hash32(byte[] data, int length, int seed) {
        return MurmurHash3.hash32(data, 0, length, seed);
    }

    @Deprecated
    public static int hash32(byte[] data, int offset, int length, int seed) {
        int hash = seed;
        int nblocks = length >> 2;
        for (int i = 0; i < nblocks; ++i) {
            int index = offset + (i << 2);
            int k = MurmurHash3.getLittleEndianInt(data, index);
            hash = MurmurHash3.mix32(k, hash);
        }
        int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3: {
                k1 ^= data[index + 2] << 16;
            }
            case 2: {
                k1 ^= data[index + 1] << 8;
            }
            case 1: {
                k1 ^= data[index];
                k1 *= -862048943;
                k1 = Integer.rotateLeft(k1, 15);
                hash ^= (k1 *= 461845907);
            }
        }
        return MurmurHash3.fmix32(hash ^= length);
    }

    public static int hash32x86(byte[] data) {
        return MurmurHash3.hash32x86(data, 0, data.length, 0);
    }

    public static int hash32x86(byte[] data, int offset, int length, int seed) {
        int hash = seed;
        int nblocks = length >> 2;
        for (int i = 0; i < nblocks; ++i) {
            int index = offset + (i << 2);
            int k = MurmurHash3.getLittleEndianInt(data, index);
            hash = MurmurHash3.mix32(k, hash);
        }
        int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3: {
                k1 ^= (data[index + 2] & 0xFF) << 16;
            }
            case 2: {
                k1 ^= (data[index + 1] & 0xFF) << 8;
            }
            case 1: {
                k1 ^= data[index] & 0xFF;
                k1 *= -862048943;
                k1 = Integer.rotateLeft(k1, 15);
                hash ^= (k1 *= 461845907);
            }
        }
        return MurmurHash3.fmix32(hash ^= length);
    }

    @Deprecated
    public static long hash64(long data) {
        long hash = 104729L;
        long k = Long.reverseBytes(data);
        int length = 8;
        k *= -8663945395140668459L;
        k = Long.rotateLeft(k, 31);
        hash ^= (k *= 5545529020109919103L);
        hash = Long.rotateLeft(hash, 27) * 5L + 1390208809L;
        hash ^= 8L;
        hash = MurmurHash3.fmix64(hash);
        return hash;
    }

    @Deprecated
    public static long hash64(int data) {
        long k1 = (long)Integer.reverseBytes(data) & 0xFFFFFFFFL;
        int length = 4;
        long hash = 104729L;
        k1 *= -8663945395140668459L;
        k1 = Long.rotateLeft(k1, 31);
        hash ^= (k1 *= 5545529020109919103L);
        hash ^= 4L;
        hash = MurmurHash3.fmix64(hash);
        return hash;
    }

    @Deprecated
    public static long hash64(short data) {
        long hash = 104729L;
        long k1 = 0L;
        k1 ^= ((long)data & 0xFFL) << 8;
        k1 ^= (long)((data & 0xFF00) >> 8) & 0xFFL;
        k1 *= -8663945395140668459L;
        k1 = Long.rotateLeft(k1, 31);
        hash ^= (k1 *= 5545529020109919103L);
        hash ^= 2L;
        hash = MurmurHash3.fmix64(hash);
        return hash;
    }

    @Deprecated
    public static long hash64(byte[] data) {
        return MurmurHash3.hash64(data, 0, data.length, 104729);
    }

    @Deprecated
    public static long hash64(byte[] data, int offset, int length) {
        return MurmurHash3.hash64(data, offset, length, 104729);
    }

    @Deprecated
    public static long hash64(byte[] data, int offset, int length, int seed) {
        long hash = seed;
        int nblocks = length >> 3;
        for (int i = 0; i < nblocks; ++i) {
            int index = offset + (i << 3);
            long k = MurmurHash3.getLittleEndianLong(data, index);
            k *= -8663945395140668459L;
            k = Long.rotateLeft(k, 31);
            hash ^= (k *= 5545529020109919103L);
            hash = Long.rotateLeft(hash, 27) * 5L + 1390208809L;
        }
        long k1 = 0L;
        int index = offset + (nblocks << 3);
        switch (offset + length - index) {
            case 7: {
                k1 ^= ((long)data[index + 6] & 0xFFL) << 48;
            }
            case 6: {
                k1 ^= ((long)data[index + 5] & 0xFFL) << 40;
            }
            case 5: {
                k1 ^= ((long)data[index + 4] & 0xFFL) << 32;
            }
            case 4: {
                k1 ^= ((long)data[index + 3] & 0xFFL) << 24;
            }
            case 3: {
                k1 ^= ((long)data[index + 2] & 0xFFL) << 16;
            }
            case 2: {
                k1 ^= ((long)data[index + 1] & 0xFFL) << 8;
            }
            case 1: {
                k1 ^= (long)data[index] & 0xFFL;
                k1 *= -8663945395140668459L;
                k1 = Long.rotateLeft(k1, 31);
                hash ^= (k1 *= 5545529020109919103L);
            }
        }
        hash ^= (long)length;
        hash = MurmurHash3.fmix64(hash);
        return hash;
    }

    public static long[] hash128(byte[] data) {
        return MurmurHash3.hash128(data, 0, data.length, 104729);
    }

    public static long[] hash128x64(byte[] data) {
        return MurmurHash3.hash128x64(data, 0, data.length, 0);
    }

    @Deprecated
    public static long[] hash128(String data) {
        byte[] bytes = StringUtils.getBytesUtf8(data);
        return MurmurHash3.hash128(bytes, 0, bytes.length, 104729);
    }

    @Deprecated
    public static long[] hash128(byte[] data, int offset, int length, int seed) {
        return MurmurHash3.hash128x64Internal(data, offset, length, seed);
    }

    public static long[] hash128x64(byte[] data, int offset, int length, int seed) {
        return MurmurHash3.hash128x64Internal(data, offset, length, (long)seed & 0xFFFFFFFFL);
    }

    private static long[] hash128x64Internal(byte[] data, int offset, int length, long seed) {
        long h1 = seed;
        long h2 = seed;
        int nblocks = length >> 4;
        for (int i = 0; i < nblocks; ++i) {
            int index = offset + (i << 4);
            long k1 = MurmurHash3.getLittleEndianLong(data, index);
            long k2 = MurmurHash3.getLittleEndianLong(data, index + 8);
            k1 *= -8663945395140668459L;
            k1 = Long.rotateLeft(k1, 31);
            h1 ^= (k1 *= 5545529020109919103L);
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5L + 1390208809L;
            k2 *= 5545529020109919103L;
            k2 = Long.rotateLeft(k2, 33);
            h2 ^= (k2 *= -8663945395140668459L);
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5L + 944331445L;
        }
        long k1 = 0L;
        long k2 = 0L;
        int index = offset + (nblocks << 4);
        switch (offset + length - index) {
            case 15: {
                k2 ^= ((long)data[index + 14] & 0xFFL) << 48;
            }
            case 14: {
                k2 ^= ((long)data[index + 13] & 0xFFL) << 40;
            }
            case 13: {
                k2 ^= ((long)data[index + 12] & 0xFFL) << 32;
            }
            case 12: {
                k2 ^= ((long)data[index + 11] & 0xFFL) << 24;
            }
            case 11: {
                k2 ^= ((long)data[index + 10] & 0xFFL) << 16;
            }
            case 10: {
                k2 ^= ((long)data[index + 9] & 0xFFL) << 8;
            }
            case 9: {
                k2 ^= (long)(data[index + 8] & 0xFF);
                k2 *= 5545529020109919103L;
                k2 = Long.rotateLeft(k2, 33);
                h2 ^= (k2 *= -8663945395140668459L);
            }
            case 8: {
                k1 ^= ((long)data[index + 7] & 0xFFL) << 56;
            }
            case 7: {
                k1 ^= ((long)data[index + 6] & 0xFFL) << 48;
            }
            case 6: {
                k1 ^= ((long)data[index + 5] & 0xFFL) << 40;
            }
            case 5: {
                k1 ^= ((long)data[index + 4] & 0xFFL) << 32;
            }
            case 4: {
                k1 ^= ((long)data[index + 3] & 0xFFL) << 24;
            }
            case 3: {
                k1 ^= ((long)data[index + 2] & 0xFFL) << 16;
            }
            case 2: {
                k1 ^= ((long)data[index + 1] & 0xFFL) << 8;
            }
            case 1: {
                k1 ^= (long)(data[index] & 0xFF);
                k1 *= -8663945395140668459L;
                k1 = Long.rotateLeft(k1, 31);
                h1 ^= (k1 *= 5545529020109919103L);
            }
        }
        h1 ^= (long)length;
        h1 += (h2 ^= (long)length);
        h2 += h1;
        h1 = MurmurHash3.fmix64(h1);
        h2 = MurmurHash3.fmix64(h2);
        h1 += h2;
        return new long[]{h1, h2 += h1};
    }

    private static long getLittleEndianLong(byte[] data, int index) {
        return (long)data[index] & 0xFFL | ((long)data[index + 1] & 0xFFL) << 8 | ((long)data[index + 2] & 0xFFL) << 16 | ((long)data[index + 3] & 0xFFL) << 24 | ((long)data[index + 4] & 0xFFL) << 32 | ((long)data[index + 5] & 0xFFL) << 40 | ((long)data[index + 6] & 0xFFL) << 48 | ((long)data[index + 7] & 0xFFL) << 56;
    }

    private static int getLittleEndianInt(byte[] data, int index) {
        return data[index] & 0xFF | (data[index + 1] & 0xFF) << 8 | (data[index + 2] & 0xFF) << 16 | (data[index + 3] & 0xFF) << 24;
    }

    private static int mix32(int k, int hash) {
        k *= -862048943;
        k = Integer.rotateLeft(k, 15);
        return Integer.rotateLeft(hash ^= (k *= 461845907), 13) * 5 + -430675100;
    }

    private static int fmix32(int hash) {
        hash ^= hash >>> 16;
        hash *= -2048144789;
        hash ^= hash >>> 13;
        hash *= -1028477387;
        hash ^= hash >>> 16;
        return hash;
    }

    private static long fmix64(long hash) {
        hash ^= hash >>> 33;
        hash *= -49064778989728563L;
        hash ^= hash >>> 33;
        hash *= -4265267296055464877L;
        hash ^= hash >>> 33;
        return hash;
    }

    @Deprecated
    public static class IncrementalHash32
    extends IncrementalHash32x86 {
        @Override
        @Deprecated
        int finalise(int hash, int unprocessedLength, byte[] unprocessed, int totalLen) {
            int result = hash;
            int k1 = 0;
            switch (unprocessedLength) {
                case 3: {
                    k1 ^= unprocessed[2] << 16;
                }
                case 2: {
                    k1 ^= unprocessed[1] << 8;
                }
                case 1: {
                    k1 ^= unprocessed[0];
                    k1 *= -862048943;
                    k1 = Integer.rotateLeft(k1, 15);
                    result ^= (k1 *= 461845907);
                }
            }
            return MurmurHash3.fmix32(result ^= totalLen);
        }
    }

    public static class IncrementalHash32x86 {
        private static final int BLOCK_SIZE = 4;
        private final byte[] unprocessed = new byte[3];
        private int unprocessedLength;
        private int totalLen;
        private int hash;

        public final void start(int seed) {
            this.totalLen = 0;
            this.unprocessedLength = 0;
            this.hash = seed;
        }

        public final void add(byte[] data, int offset, int length) {
            int newLength;
            int newOffset;
            int consumed;
            if (length <= 0) {
                return;
            }
            this.totalLen += length;
            if (this.unprocessedLength + length - 4 < 0) {
                System.arraycopy(data, offset, this.unprocessed, this.unprocessedLength, length);
                this.unprocessedLength += length;
                return;
            }
            if (this.unprocessedLength > 0) {
                int k = -1;
                switch (this.unprocessedLength) {
                    case 1: {
                        k = IncrementalHash32x86.orBytes(this.unprocessed[0], data[offset], data[offset + 1], data[offset + 2]);
                        break;
                    }
                    case 2: {
                        k = IncrementalHash32x86.orBytes(this.unprocessed[0], this.unprocessed[1], data[offset], data[offset + 1]);
                        break;
                    }
                    case 3: {
                        k = IncrementalHash32x86.orBytes(this.unprocessed[0], this.unprocessed[1], this.unprocessed[2], data[offset]);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unprocessed length should be 1, 2, or 3: " + this.unprocessedLength);
                    }
                }
                this.hash = MurmurHash3.mix32(k, this.hash);
                consumed = 4 - this.unprocessedLength;
                newOffset = offset + consumed;
                newLength = length - consumed;
            } else {
                newOffset = offset;
                newLength = length;
            }
            int nblocks = newLength >> 2;
            for (int i = 0; i < nblocks; ++i) {
                int index = newOffset + (i << 2);
                int k = MurmurHash3.getLittleEndianInt(data, index);
                this.hash = MurmurHash3.mix32(k, this.hash);
            }
            consumed = nblocks << 2;
            this.unprocessedLength = newLength - consumed;
            if (this.unprocessedLength != 0) {
                System.arraycopy(data, newOffset + consumed, this.unprocessed, 0, this.unprocessedLength);
            }
        }

        public final int end() {
            return this.finalise(this.hash, this.unprocessedLength, this.unprocessed, this.totalLen);
        }

        int finalise(int hash, int unprocessedLength, byte[] unprocessed, int totalLen) {
            int result = hash;
            int k1 = 0;
            switch (unprocessedLength) {
                case 3: {
                    k1 ^= (unprocessed[2] & 0xFF) << 16;
                }
                case 2: {
                    k1 ^= (unprocessed[1] & 0xFF) << 8;
                }
                case 1: {
                    k1 ^= unprocessed[0] & 0xFF;
                    k1 *= -862048943;
                    k1 = Integer.rotateLeft(k1, 15);
                    result ^= (k1 *= 461845907);
                }
            }
            return MurmurHash3.fmix32(result ^= totalLen);
        }

        private static int orBytes(byte b1, byte b2, byte b3, byte b4) {
            return b1 & 0xFF | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24;
        }
    }
}

