/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import com.ibm.icu.util.BytesTrie;
import com.ibm.icu.util.StringTrieBuilder;
import java.nio.ByteBuffer;

public final class BytesTrieBuilder
extends StringTrieBuilder {
    private final byte[] intBytes = new byte[5];
    private byte[] bytes;
    private int bytesLength;

    public BytesTrieBuilder add(byte[] sequence, int length, int value) {
        this.addImpl(new BytesAsCharSequence(sequence, length), value);
        return this;
    }

    public BytesTrie build(StringTrieBuilder.Option buildOption) {
        this.buildBytes(buildOption);
        return new BytesTrie(this.bytes, this.bytes.length - this.bytesLength);
    }

    public ByteBuffer buildByteBuffer(StringTrieBuilder.Option buildOption) {
        this.buildBytes(buildOption);
        return ByteBuffer.wrap(this.bytes, this.bytes.length - this.bytesLength, this.bytesLength);
    }

    private void buildBytes(StringTrieBuilder.Option buildOption) {
        if (this.bytes == null) {
            this.bytes = new byte[1024];
        }
        this.buildImpl(buildOption);
    }

    public BytesTrieBuilder clear() {
        this.clearImpl();
        this.bytes = null;
        this.bytesLength = 0;
        return this;
    }

    protected boolean matchNodesCanHaveValues() {
        return false;
    }

    protected int getMaxBranchLinearSubNodeLength() {
        return 5;
    }

    protected int getMinLinearMatch() {
        return 16;
    }

    protected int getMaxLinearMatchLength() {
        return 16;
    }

    private void ensureCapacity(int length) {
        if (length > this.bytes.length) {
            int newCapacity = this.bytes.length;
            while ((newCapacity *= 2) <= length) {
            }
            byte[] newBytes = new byte[newCapacity];
            System.arraycopy(this.bytes, this.bytes.length - this.bytesLength, newBytes, newBytes.length - this.bytesLength, this.bytesLength);
            this.bytes = newBytes;
        }
    }

    protected int write(int b) {
        int newLength = this.bytesLength + 1;
        this.ensureCapacity(newLength);
        this.bytesLength = newLength;
        this.bytes[this.bytes.length - this.bytesLength] = (byte)b;
        return this.bytesLength;
    }

    protected int write(int offset, int length) {
        int newLength = this.bytesLength + length;
        this.ensureCapacity(newLength);
        this.bytesLength = newLength;
        int bytesOffset = this.bytes.length - this.bytesLength;
        while (length > 0) {
            this.bytes[bytesOffset++] = (byte)this.strings.charAt(offset++);
            --length;
        }
        return this.bytesLength;
    }

    private int write(byte[] b, int length) {
        int newLength = this.bytesLength + length;
        this.ensureCapacity(newLength);
        this.bytesLength = newLength;
        System.arraycopy(b, 0, this.bytes, this.bytes.length - this.bytesLength, length);
        return this.bytesLength;
    }

    protected int writeValueAndFinal(int i, boolean isFinal) {
        if (0 <= i && i <= 64) {
            return this.write(16 + i << 1 | (isFinal ? 1 : 0));
        }
        int length = 1;
        if (i < 0 || i > 0xFFFFFF) {
            this.intBytes[0] = 127;
            this.intBytes[1] = (byte)(i >> 24);
            this.intBytes[2] = (byte)(i >> 16);
            this.intBytes[3] = (byte)(i >> 8);
            this.intBytes[4] = (byte)i;
            length = 5;
        } else {
            if (i <= 6911) {
                this.intBytes[0] = (byte)(81 + (i >> 8));
            } else {
                if (i <= 0x11FFFF) {
                    this.intBytes[0] = (byte)(108 + (i >> 16));
                } else {
                    this.intBytes[0] = 126;
                    this.intBytes[1] = (byte)(i >> 16);
                    length = 2;
                }
                this.intBytes[length++] = (byte)(i >> 8);
            }
            this.intBytes[length++] = (byte)i;
        }
        this.intBytes[0] = (byte)(this.intBytes[0] << 1 | (isFinal ? 1 : 0));
        return this.write(this.intBytes, length);
    }

    protected int writeValueAndType(boolean hasValue, int value, int node) {
        int offset = this.write(node);
        if (hasValue) {
            offset = this.writeValueAndFinal(value, false);
        }
        return offset;
    }

    protected int writeDeltaTo(int jumpTarget) {
        int length;
        int i = this.bytesLength - jumpTarget;
        assert (i >= 0);
        if (i <= 191) {
            return this.write(i);
        }
        if (i <= 12287) {
            this.intBytes[0] = (byte)(192 + (i >> 8));
            length = 1;
        } else {
            if (i <= 917503) {
                this.intBytes[0] = (byte)(240 + (i >> 16));
                length = 2;
            } else {
                if (i <= 0xFFFFFF) {
                    this.intBytes[0] = -2;
                    length = 3;
                } else {
                    this.intBytes[0] = -1;
                    this.intBytes[1] = (byte)(i >> 24);
                    length = 4;
                }
                this.intBytes[1] = (byte)(i >> 16);
            }
            this.intBytes[1] = (byte)(i >> 8);
        }
        this.intBytes[length++] = (byte)i;
        return this.write(this.intBytes, length);
    }

    private static final class BytesAsCharSequence
    implements CharSequence {
        private byte[] s;
        private int len;

        public BytesAsCharSequence(byte[] sequence, int length) {
            this.s = sequence;
            this.len = length;
        }

        public char charAt(int i) {
            return (char)(this.s[i] & 0xFF);
        }

        public int length() {
            return this.len;
        }

        public CharSequence subSequence(int start, int end) {
            return null;
        }
    }
}

