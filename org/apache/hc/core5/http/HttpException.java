/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

public class HttpException
extends Exception {
    private static final int FIRST_VALID_CHAR = 32;
    private static final long serialVersionUID = -5437299376222011036L;

    static String clean(String message) {
        int i;
        char[] chars = message.toCharArray();
        for (i = 0; i < chars.length && chars[i] >= ' '; ++i) {
        }
        if (i == chars.length) {
            return message;
        }
        StringBuilder builder = new StringBuilder(chars.length * 2);
        for (i = 0; i < chars.length; ++i) {
            char ch = chars[i];
            if (ch < ' ') {
                builder.append("[0x");
                String hexString = Integer.toHexString(i);
                if (hexString.length() == 1) {
                    builder.append("0");
                }
                builder.append(hexString);
                builder.append("]");
                continue;
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    public HttpException() {
    }

    public HttpException(String message) {
        super(HttpException.clean(message));
    }

    public HttpException(String format, Object ... args) {
        super(HttpException.clean(String.format(format, args)));
    }

    public HttpException(String message, Throwable cause) {
        super(HttpException.clean(message));
        this.initCause(cause);
    }
}

