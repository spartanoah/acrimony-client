/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

public final class TextUtils {
    private TextUtils() {
    }

    public static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
    }

    public static boolean isBlank(CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isWhitespace(s.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean containsBlanks(CharSequence s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) continue;
            return true;
        }
        return false;
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            if (b < 16) {
                buffer.append('0');
            }
            buffer.append(Integer.toHexString(b & 0xFF));
        }
        return buffer.toString();
    }
}

