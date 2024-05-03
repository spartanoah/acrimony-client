/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;

public final class NumberInput {
    public static final String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";
    static final long L_BILLION = 1000000000L;
    static final String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
    static final String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);

    public static int parseInt(char[] ch, int off, int len) {
        int num = ch[off + len - 1] - 48;
        switch (len) {
            case 9: {
                num += (ch[off++] - 48) * 100000000;
            }
            case 8: {
                num += (ch[off++] - 48) * 10000000;
            }
            case 7: {
                num += (ch[off++] - 48) * 1000000;
            }
            case 6: {
                num += (ch[off++] - 48) * 100000;
            }
            case 5: {
                num += (ch[off++] - 48) * 10000;
            }
            case 4: {
                num += (ch[off++] - 48) * 1000;
            }
            case 3: {
                num += (ch[off++] - 48) * 100;
            }
            case 2: {
                num += (ch[off] - 48) * 10;
            }
        }
        return num;
    }

    public static int parseInt(String s) {
        char c = s.charAt(0);
        int len = s.length();
        boolean neg = c == '-';
        int offset = 1;
        if (neg) {
            if (len == 1 || len > 10) {
                return Integer.parseInt(s);
            }
            c = s.charAt(offset++);
        } else if (len > 9) {
            return Integer.parseInt(s);
        }
        if (c > '9' || c < '0') {
            return Integer.parseInt(s);
        }
        int num = c - 48;
        if (offset < len) {
            if ((c = s.charAt(offset++)) > '9' || c < '0') {
                return Integer.parseInt(s);
            }
            num = num * 10 + (c - 48);
            if (offset < len) {
                if ((c = s.charAt(offset++)) > '9' || c < '0') {
                    return Integer.parseInt(s);
                }
                num = num * 10 + (c - 48);
                if (offset < len) {
                    do {
                        if ((c = s.charAt(offset++)) > '9' || c < '0') {
                            return Integer.parseInt(s);
                        }
                        num = num * 10 + (c - 48);
                    } while (offset < len);
                }
            }
        }
        return neg ? -num : num;
    }

    public static long parseLong(char[] ch, int off, int len) {
        int len1 = len - 9;
        long val2 = (long)NumberInput.parseInt(ch, off, len1) * 1000000000L;
        return val2 + (long)NumberInput.parseInt(ch, off + len1, 9);
    }

    public static long parseLong(String s) {
        int length = s.length();
        if (length <= 9) {
            return NumberInput.parseInt(s);
        }
        return Long.parseLong(s);
    }

    public static boolean inLongRange(char[] ch, int off, int len, boolean negative) {
        String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if (len < cmpLen) {
            return true;
        }
        if (len > cmpLen) {
            return false;
        }
        for (int i = 0; i < cmpLen; ++i) {
            int diff = ch[off + i] - cmpStr.charAt(i);
            if (diff == 0) continue;
            return diff < 0;
        }
        return true;
    }

    public static boolean inLongRange(String s, boolean negative) {
        String cmp = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmp.length();
        int alen = s.length();
        if (alen < cmpLen) {
            return true;
        }
        if (alen > cmpLen) {
            return false;
        }
        for (int i = 0; i < cmpLen; ++i) {
            int diff = s.charAt(i) - cmp.charAt(i);
            if (diff == 0) continue;
            return diff < 0;
        }
        return true;
    }

    public static int parseAsInt(String s, int def) {
        char c;
        if (s == null) {
            return def;
        }
        int len = (s = s.trim()).length();
        if (len == 0) {
            return def;
        }
        int i = 0;
        if (i < len) {
            c = s.charAt(0);
            if (c == '+') {
                s = s.substring(1);
                len = s.length();
            } else if (c == '-') {
                ++i;
            }
        }
        while (i < len) {
            c = s.charAt(i);
            if (c > '9' || c < '0') {
                try {
                    return (int)NumberInput.parseDouble(s);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            ++i;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException numberFormatException) {
            return def;
        }
    }

    public static long parseAsLong(String s, long def) {
        char c;
        if (s == null) {
            return def;
        }
        int len = (s = s.trim()).length();
        if (len == 0) {
            return def;
        }
        int i = 0;
        if (i < len) {
            c = s.charAt(0);
            if (c == '+') {
                s = s.substring(1);
                len = s.length();
            } else if (c == '-') {
                ++i;
            }
        }
        while (i < len) {
            c = s.charAt(i);
            if (c > '9' || c < '0') {
                try {
                    return (long)NumberInput.parseDouble(s);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            ++i;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException numberFormatException) {
            return def;
        }
    }

    public static double parseAsDouble(String s, double def) {
        if (s == null) {
            return def;
        }
        int len = (s = s.trim()).length();
        if (len == 0) {
            return def;
        }
        try {
            return NumberInput.parseDouble(s);
        } catch (NumberFormatException numberFormatException) {
            return def;
        }
    }

    public static double parseDouble(String s) throws NumberFormatException {
        if (NASTY_SMALL_DOUBLE.equals(s)) {
            return Double.MIN_VALUE;
        }
        return Double.parseDouble(s);
    }

    public static BigDecimal parseBigDecimal(String s) throws NumberFormatException {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw NumberInput._badBD(s);
        }
    }

    public static BigDecimal parseBigDecimal(char[] b) throws NumberFormatException {
        return NumberInput.parseBigDecimal(b, 0, b.length);
    }

    public static BigDecimal parseBigDecimal(char[] b, int off, int len) throws NumberFormatException {
        try {
            return new BigDecimal(b, off, len);
        } catch (NumberFormatException e) {
            throw NumberInput._badBD(new String(b, off, len));
        }
    }

    private static NumberFormatException _badBD(String s) {
        return new NumberFormatException("Value \"" + s + "\" can not be represented as BigDecimal");
    }
}

