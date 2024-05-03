/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.DecompressionException;

final class FastLz {
    private static final int MAX_DISTANCE = 8191;
    private static final int MAX_FARDISTANCE = 73725;
    private static final int HASH_LOG = 13;
    private static final int HASH_SIZE = 8192;
    private static final int HASH_MASK = 8191;
    private static final int MAX_COPY = 32;
    private static final int MAX_LEN = 264;
    private static final int MIN_RECOMENDED_LENGTH_FOR_LEVEL_2 = 65536;
    static final int MAGIC_NUMBER = 4607066;
    static final byte BLOCK_TYPE_NON_COMPRESSED = 0;
    static final byte BLOCK_TYPE_COMPRESSED = 1;
    static final byte BLOCK_WITHOUT_CHECKSUM = 0;
    static final byte BLOCK_WITH_CHECKSUM = 16;
    static final int OPTIONS_OFFSET = 3;
    static final int CHECKSUM_OFFSET = 4;
    static final int MAX_CHUNK_LENGTH = 65535;
    static final int MIN_LENGTH_TO_COMPRESSION = 32;
    static final int LEVEL_AUTO = 0;
    static final int LEVEL_1 = 1;
    static final int LEVEL_2 = 2;

    static int calculateOutputBufferLength(int inputLength) {
        int outputLength = (int)((double)inputLength * 1.06);
        return Math.max(outputLength, 66);
    }

    static int compress(ByteBuf input, int inOffset, int inLength, ByteBuf output, int outOffset, int proposedLevel) {
        int hslot;
        int level = proposedLevel == 0 ? (inLength < 65536 ? 1 : 2) : proposedLevel;
        int ip = 0;
        int ipBound = ip + inLength - 2;
        int ipLimit = ip + inLength - 12;
        int op = 0;
        int[] htab = new int[8192];
        if (inLength < 4) {
            if (inLength != 0) {
                output.setByte(outOffset + op++, (byte)(inLength - 1));
                ++ipBound;
                while (ip <= ipBound) {
                    output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
                }
                return inLength + 1;
            }
            return 0;
        }
        for (hslot = 0; hslot < 8192; ++hslot) {
            htab[hslot] = ip;
        }
        int copy = 2;
        output.setByte(outOffset + op++, 31);
        output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
        output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
        while (ip < ipLimit) {
            int hval;
            int len;
            long distance;
            int ref;
            block38: {
                int anchor;
                block40: {
                    block39: {
                        ref = 0;
                        distance = 0L;
                        len = 3;
                        anchor = ip;
                        boolean matchLabel = false;
                        if (level == 2 && input.getByte(inOffset + ip) == input.getByte(inOffset + ip - 1) && FastLz.readU16(input, inOffset + ip - 1) == FastLz.readU16(input, inOffset + ip + 1)) {
                            distance = 1L;
                            ip += 3;
                            ref = anchor + 2;
                            matchLabel = true;
                        }
                        if (matchLabel) break block38;
                        hslot = hval = FastLz.hashFunction(input, inOffset + ip);
                        ref = htab[hval];
                        distance = anchor - ref;
                        htab[hslot] = anchor;
                        if (distance == 0L || (level != 1 ? distance >= 73725L : distance >= 8191L)) break block39;
                        if (input.getByte(inOffset + ref++) == input.getByte(inOffset + ip++) && input.getByte(inOffset + ref++) == input.getByte(inOffset + ip++) && input.getByte(inOffset + ref++) == input.getByte(inOffset + ip++)) break block40;
                    }
                    output.setByte(outOffset + op++, input.getByte(inOffset + anchor++));
                    ip = anchor;
                    if (++copy != 32) continue;
                    copy = 0;
                    output.setByte(outOffset + op++, 31);
                    continue;
                }
                if (level == 2 && distance >= 8191L) {
                    if (input.getByte(inOffset + ip++) != input.getByte(inOffset + ref++) || input.getByte(inOffset + ip++) != input.getByte(inOffset + ref++)) {
                        output.setByte(outOffset + op++, input.getByte(inOffset + anchor++));
                        ip = anchor;
                        if (++copy != 32) continue;
                        copy = 0;
                        output.setByte(outOffset + op++, 31);
                        continue;
                    }
                    len += 2;
                }
            }
            if (--distance == 0L) {
                byte x = input.getByte(inOffset + ip - 1);
                for (ip = anchor + len; ip < ipBound && input.getByte(inOffset + ref++) == x; ++ip) {
                }
            } else {
                boolean missMatch = false;
                for (int i = 0; i < 8; ++i) {
                    if (input.getByte(inOffset + ref++) == input.getByte(inOffset + ip++)) continue;
                    missMatch = true;
                    break;
                }
                if (!missMatch) {
                    while (ip < ipBound && input.getByte(inOffset + ref++) == input.getByte(inOffset + ip++)) {
                    }
                }
            }
            if (copy != 0) {
                output.setByte(outOffset + op - copy - 1, (byte)(copy - 1));
            } else {
                --op;
            }
            copy = 0;
            if (level == 2) {
                if (distance < 8191L) {
                    if (len < 7) {
                        output.setByte(outOffset + op++, (byte)((long)(len << 5) + (distance >>> 8)));
                        output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                    } else {
                        output.setByte(outOffset + op++, (byte)(224L + (distance >>> 8)));
                        len -= 7;
                        while (len >= 255) {
                            output.setByte(outOffset + op++, -1);
                            len -= 255;
                        }
                        output.setByte(outOffset + op++, (byte)len);
                        output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                    }
                } else if (len < 7) {
                    output.setByte(outOffset + op++, (byte)((len << 5) + 31));
                    output.setByte(outOffset + op++, -1);
                    output.setByte(outOffset + op++, (byte)((distance -= 8191L) >>> 8));
                    output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                } else {
                    distance -= 8191L;
                    output.setByte(outOffset + op++, -1);
                    len -= 7;
                    while (len >= 255) {
                        output.setByte(outOffset + op++, -1);
                        len -= 255;
                    }
                    output.setByte(outOffset + op++, (byte)len);
                    output.setByte(outOffset + op++, -1);
                    output.setByte(outOffset + op++, (byte)(distance >>> 8));
                    output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                }
            } else {
                if (len > 262) {
                    for (len = (ip -= 3) - anchor; len > 262; len -= 262) {
                        output.setByte(outOffset + op++, (byte)(224L + (distance >>> 8)));
                        output.setByte(outOffset + op++, -3);
                        output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                    }
                }
                if (len < 7) {
                    output.setByte(outOffset + op++, (byte)((long)(len << 5) + (distance >>> 8)));
                    output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                } else {
                    output.setByte(outOffset + op++, (byte)(224L + (distance >>> 8)));
                    output.setByte(outOffset + op++, (byte)(len - 7));
                    output.setByte(outOffset + op++, (byte)(distance & 0xFFL));
                }
            }
            hval = FastLz.hashFunction(input, inOffset + ip);
            htab[hval] = ip++;
            hval = FastLz.hashFunction(input, inOffset + ip);
            htab[hval] = ip++;
            output.setByte(outOffset + op++, 31);
        }
        ++ipBound;
        while (ip <= ipBound) {
            output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
            if (++copy != 32) continue;
            copy = 0;
            output.setByte(outOffset + op++, 31);
        }
        if (copy != 0) {
            output.setByte(outOffset + op - copy - 1, (byte)(copy - 1));
        } else {
            --op;
        }
        if (level == 2) {
            output.setByte(outOffset, output.getByte(outOffset) | 0x20);
        }
        return op;
    }

    static int decompress(ByteBuf input, int inOffset, int inLength, ByteBuf output, int outOffset, int outLength) {
        int level = (input.getByte(inOffset) >> 5) + 1;
        if (level != 1 && level != 2) {
            throw new DecompressionException(String.format("invalid level: %d (expected: %d or %d)", level, 1, 2));
        }
        int ip = 0;
        int op = 0;
        long ctrl = input.getByte(inOffset + ip++) & 0x1F;
        boolean loop = true;
        do {
            int ref = op;
            long len = ctrl >> 5;
            long ofs = (ctrl & 0x1FL) << 8;
            if (ctrl >= 32L) {
                short code;
                ref = (int)((long)ref - ofs);
                if (--len == 6L) {
                    if (level == 1) {
                        len += (long)input.getUnsignedByte(inOffset + ip++);
                    } else {
                        do {
                            code = input.getUnsignedByte(inOffset + ip++);
                            len += (long)code;
                        } while (code == 255);
                    }
                }
                if (level == 1) {
                    ref -= input.getUnsignedByte(inOffset + ip++);
                } else {
                    code = input.getUnsignedByte(inOffset + ip++);
                    ref -= code;
                    if (code == 255 && ofs == 7936L) {
                        ofs = input.getUnsignedByte(inOffset + ip++) << 8;
                        ref = (int)((long)op - (ofs += (long)input.getUnsignedByte(inOffset + ip++)) - 8191L);
                    }
                }
                if ((long)op + len + 3L > (long)outLength) {
                    return 0;
                }
                if (ref - 1 < 0) {
                    return 0;
                }
                if (ip < inLength) {
                    ctrl = input.getUnsignedByte(inOffset + ip++);
                } else {
                    loop = false;
                }
                if (ref == op) {
                    byte b = output.getByte(outOffset + ref - 1);
                    output.setByte(outOffset + op++, b);
                    output.setByte(outOffset + op++, b);
                    output.setByte(outOffset + op++, b);
                    while (len != 0L) {
                        output.setByte(outOffset + op++, b);
                        --len;
                    }
                } else {
                    int n = op++;
                    int n2 = --ref;
                    output.setByte(outOffset + n, output.getByte(outOffset + n2));
                    int n3 = op++;
                    int n4 = ++ref;
                    output.setByte(outOffset + n3, output.getByte(outOffset + n4));
                    int n5 = op++;
                    int n6 = ++ref;
                    ++ref;
                    output.setByte(outOffset + n5, output.getByte(outOffset + n6));
                    while (len != 0L) {
                        output.setByte(outOffset + op++, output.getByte(outOffset + ref++));
                        --len;
                    }
                }
            } else {
                if ((long)op + ++ctrl > (long)outLength) {
                    return 0;
                }
                if ((long)ip + ctrl > (long)inLength) {
                    return 0;
                }
                output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
                --ctrl;
                while (ctrl != 0L) {
                    output.setByte(outOffset + op++, input.getByte(inOffset + ip++));
                    --ctrl;
                }
                boolean bl = loop = ip < inLength;
                if (!loop) continue;
                ctrl = input.getUnsignedByte(inOffset + ip++);
            }
        } while (loop);
        return op;
    }

    private static int hashFunction(ByteBuf p, int offset) {
        int v = FastLz.readU16(p, offset);
        v ^= FastLz.readU16(p, offset + 1) ^ v >> 3;
        return v &= 0x1FFF;
    }

    private static int readU16(ByteBuf data, int offset) {
        if (offset + 1 >= data.readableBytes()) {
            return data.getUnsignedByte(offset);
        }
        return data.getUnsignedByte(offset + 1) << 8 | data.getUnsignedByte(offset);
    }

    private FastLz() {
    }
}

