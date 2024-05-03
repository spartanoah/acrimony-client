/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.util.internal.InternalThreadLocalMap;

final class CookieEncoderUtil {
    static StringBuilder stringBuilder() {
        return InternalThreadLocalMap.get().stringBuilder();
    }

    static String stripTrailingSeparator(StringBuilder buf) {
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

    static void add(StringBuilder sb, String name, String val2) {
        if (val2 == null) {
            CookieEncoderUtil.addQuoted(sb, name, "");
            return;
        }
        for (int i = 0; i < val2.length(); ++i) {
            char c = val2.charAt(i);
            switch (c) {
                case '\t': 
                case ' ': 
                case '\"': 
                case '(': 
                case ')': 
                case ',': 
                case '/': 
                case ':': 
                case ';': 
                case '<': 
                case '=': 
                case '>': 
                case '?': 
                case '@': 
                case '[': 
                case '\\': 
                case ']': 
                case '{': 
                case '}': {
                    CookieEncoderUtil.addQuoted(sb, name, val2);
                    return;
                }
            }
        }
        CookieEncoderUtil.addUnquoted(sb, name, val2);
    }

    static void addUnquoted(StringBuilder sb, String name, String val2) {
        sb.append(name);
        sb.append('=');
        sb.append(val2);
        sb.append(';');
        sb.append(' ');
    }

    static void addQuoted(StringBuilder sb, String name, String val2) {
        if (val2 == null) {
            val2 = "";
        }
        sb.append(name);
        sb.append('=');
        sb.append('\"');
        sb.append(val2.replace("\\", "\\\\").replace("\"", "\\\""));
        sb.append('\"');
        sb.append(';');
        sb.append(' ');
    }

    static void add(StringBuilder sb, String name, long val2) {
        sb.append(name);
        sb.append('=');
        sb.append(val2);
        sb.append(';');
        sb.append(' ');
    }

    private CookieEncoderUtil() {
    }
}

