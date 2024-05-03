/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.conn.util;

public class DnsUtils {
    private DnsUtils() {
    }

    private static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static String normalize(String s) {
        int remaining;
        if (s == null) {
            return null;
        }
        int pos = 0;
        for (remaining = s.length(); remaining > 0 && !DnsUtils.isUpper(s.charAt(pos)); --remaining) {
            ++pos;
        }
        if (remaining > 0) {
            StringBuilder buf = new StringBuilder(s.length());
            buf.append(s, 0, pos);
            while (remaining > 0) {
                char c = s.charAt(pos);
                if (DnsUtils.isUpper(c)) {
                    buf.append((char)(c + 32));
                } else {
                    buf.append(c);
                }
                ++pos;
                --remaining;
            }
            return buf.toString();
        }
        return s;
    }
}

