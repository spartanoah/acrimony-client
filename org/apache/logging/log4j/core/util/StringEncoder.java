/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.nio.charset.Charset;

public final class StringEncoder {
    private StringEncoder() {
    }

    public static byte[] toBytes(String str, Charset charset) {
        if (str != null) {
            return str.getBytes(charset != null ? charset : Charset.defaultCharset());
        }
        return null;
    }

    @Deprecated
    public static byte[] encodeSingleByteChars(CharSequence s) {
        int length = s.length();
        byte[] result = new byte[length];
        StringEncoder.encodeString(s, 0, length, result);
        return result;
    }

    @Deprecated
    public static int encodeIsoChars(CharSequence charArray, int charIndex, byte[] byteArray, int byteIndex, int length) {
        char c;
        int i;
        for (i = 0; i < length && (c = charArray.charAt(charIndex++)) <= '\u00ff'; ++i) {
            byteArray[byteIndex++] = (byte)c;
        }
        return i;
    }

    @Deprecated
    public static int encodeString(CharSequence charArray, int charOffset, int charLength, byte[] byteArray) {
        int byteOffset = 0;
        int length = Math.min(charLength, byteArray.length);
        int charDoneIndex = charOffset + length;
        while (charOffset < charDoneIndex) {
            char c;
            int done = StringEncoder.encodeIsoChars(charArray, charOffset, byteArray, byteOffset, length);
            charOffset += done;
            byteOffset += done;
            if (done == length) continue;
            if (Character.isHighSurrogate(c = charArray.charAt(charOffset++)) && charOffset < charDoneIndex && Character.isLowSurrogate(charArray.charAt(charOffset))) {
                if (charLength > byteArray.length) {
                    ++charDoneIndex;
                    --charLength;
                }
                ++charOffset;
            }
            byteArray[byteOffset++] = 63;
            length = Math.min(charDoneIndex - charOffset, byteArray.length - byteOffset);
        }
        return byteOffset;
    }
}

