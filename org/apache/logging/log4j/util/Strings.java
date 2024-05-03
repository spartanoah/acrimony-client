/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Strings {
    private static final ThreadLocal<StringBuilder> tempStr = ThreadLocal.withInitial(StringBuilder::new);
    public static final String EMPTY = "";
    private static final String COMMA_DELIMITED_RE = "\\s*,\\s*";
    public static final String[] EMPTY_ARRAY = new String[0];
    public static final String LINE_SEPARATOR = PropertiesUtil.getProperties().getStringProperty("line.separator", "\n");

    public static String dquote(String str) {
        return '\"' + str + '\"';
    }

    public static boolean isBlank(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) continue;
            return false;
        }
        return true;
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotBlank(String s) {
        return !Strings.isBlank(s);
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !Strings.isEmpty(cs);
    }

    public static String join(Iterable<?> iterable, char separator) {
        if (iterable == null) {
            return null;
        }
        return Strings.join(iterable.iterator(), separator);
    }

    public static String join(Iterator<?> iterator, char separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, EMPTY);
        }
        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj == null) continue;
            buf.append(obj);
        }
        return buf.toString();
    }

    public static String[] splitList(String string) {
        return string != null ? string.split(COMMA_DELIMITED_RE) : new String[]{};
    }

    public static String left(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    public static String quote(String str) {
        return '\'' + str + '\'';
    }

    public static String trimToNull(String str) {
        String ts = str == null ? null : str.trim();
        return Strings.isEmpty(ts) ? null : ts;
    }

    private Strings() {
    }

    public static String toRootLowerCase(String str) {
        return str.toLowerCase(Locale.ROOT);
    }

    public static String toRootUpperCase(String str) {
        return str.toUpperCase(Locale.ROOT);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String concat(String str1, String str2) {
        if (Strings.isEmpty(str1)) {
            return str2;
        }
        if (Strings.isEmpty(str2)) {
            return str1;
        }
        StringBuilder sb = tempStr.get();
        try {
            String string = sb.append(str1).append(str2).toString();
            return string;
        } finally {
            sb.setLength(0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String repeat(String str, int count) {
        Objects.requireNonNull(str, "str");
        if (count < 0) {
            throw new IllegalArgumentException("count");
        }
        StringBuilder sb = tempStr.get();
        try {
            for (int index = 0; index < count; ++index) {
                sb.append(str);
            }
            String string = sb.toString();
            return string;
        } finally {
            sb.setLength(0);
        }
    }
}

