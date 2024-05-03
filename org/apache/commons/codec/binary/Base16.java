/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.codec.binary;

import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.BaseNCodec;

public class Base16
extends BaseNCodec {
    private static final int BITS_PER_ENCODED_BYTE = 4;
    private static final int BYTES_PER_ENCODED_BLOCK = 2;
    private static final int BYTES_PER_UNENCODED_BLOCK = 1;
    private static final byte[] UPPER_CASE_DECODE_TABLE = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};
    private static final byte[] UPPER_CASE_ENCODE_TABLE = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
    private static final byte[] LOWER_CASE_DECODE_TABLE = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};
    private static final byte[] LOWER_CASE_ENCODE_TABLE = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    private static final int MASK_4BITS = 15;
    private final byte[] decodeTable;
    private final byte[] encodeTable;

    public Base16() {
        this(false);
    }

    public Base16(boolean lowerCase) {
        this(lowerCase, DECODING_POLICY_DEFAULT);
    }

    public Base16(boolean lowerCase, CodecPolicy decodingPolicy) {
        super(1, 2, 0, 0, (byte)61, decodingPolicy);
        if (lowerCase) {
            this.encodeTable = LOWER_CASE_ENCODE_TABLE;
            this.decodeTable = LOWER_CASE_DECODE_TABLE;
        } else {
            this.encodeTable = UPPER_CASE_ENCODE_TABLE;
            this.decodeTable = UPPER_CASE_DECODE_TABLE;
        }
    }

    @Override
    void decode(byte[] data, int offset, int length, BaseNCodec.Context context) {
        int result;
        int dataLen;
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                this.validateTrailingCharacter();
            }
            return;
        }
        int availableChars = (context.ibitWorkArea != 0 ? 1 : 0) + (dataLen = Math.min(data.length - offset, length));
        if (availableChars == 1 && availableChars == dataLen) {
            context.ibitWorkArea = this.decodeOctet(data[offset]) + 1;
            return;
        }
        int charsToProcess = availableChars % 2 == 0 ? availableChars : availableChars - 1;
        byte[] buffer = this.ensureBufferSize(charsToProcess / 2, context);
        int i = 0;
        if (dataLen < availableChars) {
            result = context.ibitWorkArea - 1 << 4;
            int n = offset++;
            i = 2;
            buffer[context.pos++] = (byte)(result |= this.decodeOctet(data[n]));
            context.ibitWorkArea = 0;
        }
        while (i < charsToProcess) {
            result = this.decodeOctet(data[offset++]) << 4;
            int n = offset++;
            i += 2;
            buffer[context.pos++] = (byte)(result |= this.decodeOctet(data[n]));
        }
        if (i < dataLen) {
            context.ibitWorkArea = this.decodeOctet(data[i]) + 1;
        }
    }

    private int decodeOctet(byte octet) {
        int decoded = -1;
        if ((octet & 0xFF) < this.decodeTable.length) {
            decoded = this.decodeTable[octet];
        }
        if (decoded == -1) {
            throw new IllegalArgumentException("Invalid octet in encoded value: " + octet);
        }
        return decoded;
    }

    @Override
    void encode(byte[] data, int offset, int length, BaseNCodec.Context context) {
        if (context.eof) {
            return;
        }
        if (length < 0) {
            context.eof = true;
            return;
        }
        int size = length * 2;
        if (size < 0) {
            throw new IllegalArgumentException("Input length exceeds maximum size for encoded data: " + length);
        }
        byte[] buffer = this.ensureBufferSize(size, context);
        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            byte value = data[i];
            int high = value >> 4 & 0xF;
            int low = value & 0xF;
            buffer[context.pos++] = this.encodeTable[high];
            buffer[context.pos++] = this.encodeTable[low];
        }
    }

    @Override
    public boolean isInAlphabet(byte octet) {
        return (octet & 0xFF) < this.decodeTable.length && this.decodeTable[octet] != -1;
    }

    private void validateTrailingCharacter() {
        if (this.isStrictDecoding()) {
            throw new IllegalArgumentException("Strict decoding: Last encoded character is a valid base 16 alphabetcharacter but not a possible encoding. Decoding requires at least two characters to create one byte.");
        }
    }
}

