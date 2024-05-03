/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

public final class NumberOutput {
    private static int MILLION = 1000000;
    private static int BILLION = 1000000000;
    private static long BILLION_L = 1000000000L;
    private static long MIN_INT_AS_LONG = Integer.MIN_VALUE;
    private static long MAX_INT_AS_LONG = Integer.MAX_VALUE;
    static final String SMALLEST_INT = String.valueOf(Integer.MIN_VALUE);
    static final String SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);
    private static final int[] TRIPLET_TO_CHARS = new int[1000];
    private static final String[] sSmallIntStrs;
    private static final String[] sSmallIntStrs2;

    public static int outputInt(int v, char[] b, int off) {
        if (v < 0) {
            if (v == Integer.MIN_VALUE) {
                return NumberOutput._outputSmallestI(b, off);
            }
            b[off++] = 45;
            v = -v;
        }
        if (v < MILLION) {
            if (v < 1000) {
                if (v < 10) {
                    b[off] = (char)(48 + v);
                    return off + 1;
                }
                return NumberOutput._leading3(v, b, off);
            }
            int thousands = v / 1000;
            off = NumberOutput._leading3(thousands, b, off);
            off = NumberOutput._full3(v -= thousands * 1000, b, off);
            return off;
        }
        if (v >= BILLION) {
            if ((v -= BILLION) >= BILLION) {
                v -= BILLION;
                b[off++] = 50;
            } else {
                b[off++] = 49;
            }
            return NumberOutput._outputFullBillion(v, b, off);
        }
        int newValue = v / 1000;
        int ones = v - newValue * 1000;
        v = newValue;
        int thousands = v - (newValue /= 1000) * 1000;
        off = NumberOutput._leading3(newValue, b, off);
        off = NumberOutput._full3(thousands, b, off);
        return NumberOutput._full3(ones, b, off);
    }

    public static int outputInt(int v, byte[] b, int off) {
        if (v < 0) {
            if (v == Integer.MIN_VALUE) {
                return NumberOutput._outputSmallestI(b, off);
            }
            b[off++] = 45;
            v = -v;
        }
        if (v < MILLION) {
            if (v < 1000) {
                if (v < 10) {
                    b[off++] = (byte)(48 + v);
                } else {
                    off = NumberOutput._leading3(v, b, off);
                }
            } else {
                int thousands = v / 1000;
                off = NumberOutput._leading3(thousands, b, off);
                off = NumberOutput._full3(v -= thousands * 1000, b, off);
            }
            return off;
        }
        if (v >= BILLION) {
            if ((v -= BILLION) >= BILLION) {
                v -= BILLION;
                b[off++] = 50;
            } else {
                b[off++] = 49;
            }
            return NumberOutput._outputFullBillion(v, b, off);
        }
        int newValue = v / 1000;
        int ones = v - newValue * 1000;
        v = newValue;
        int thousands = v - (newValue /= 1000) * 1000;
        off = NumberOutput._leading3(newValue, b, off);
        off = NumberOutput._full3(thousands, b, off);
        return NumberOutput._full3(ones, b, off);
    }

    public static int outputLong(long v, char[] b, int off) {
        if (v < 0L) {
            if (v > MIN_INT_AS_LONG) {
                return NumberOutput.outputInt((int)v, b, off);
            }
            if (v == Long.MIN_VALUE) {
                return NumberOutput._outputSmallestL(b, off);
            }
            b[off++] = 45;
            v = -v;
        } else if (v <= MAX_INT_AS_LONG) {
            return NumberOutput.outputInt((int)v, b, off);
        }
        long upper = v / BILLION_L;
        v -= upper * BILLION_L;
        if (upper < BILLION_L) {
            off = NumberOutput._outputUptoBillion((int)upper, b, off);
        } else {
            long hi = upper / BILLION_L;
            off = NumberOutput._leading3((int)hi, b, off);
            off = NumberOutput._outputFullBillion((int)(upper -= hi * BILLION_L), b, off);
        }
        return NumberOutput._outputFullBillion((int)v, b, off);
    }

    public static int outputLong(long v, byte[] b, int off) {
        if (v < 0L) {
            if (v > MIN_INT_AS_LONG) {
                return NumberOutput.outputInt((int)v, b, off);
            }
            if (v == Long.MIN_VALUE) {
                return NumberOutput._outputSmallestL(b, off);
            }
            b[off++] = 45;
            v = -v;
        } else if (v <= MAX_INT_AS_LONG) {
            return NumberOutput.outputInt((int)v, b, off);
        }
        long upper = v / BILLION_L;
        v -= upper * BILLION_L;
        if (upper < BILLION_L) {
            off = NumberOutput._outputUptoBillion((int)upper, b, off);
        } else {
            long hi = upper / BILLION_L;
            off = NumberOutput._leading3((int)hi, b, off);
            off = NumberOutput._outputFullBillion((int)(upper -= hi * BILLION_L), b, off);
        }
        return NumberOutput._outputFullBillion((int)v, b, off);
    }

    public static String toString(int v) {
        if (v < sSmallIntStrs.length) {
            if (v >= 0) {
                return sSmallIntStrs[v];
            }
            int v2 = -v - 1;
            if (v2 < sSmallIntStrs2.length) {
                return sSmallIntStrs2[v2];
            }
        }
        return Integer.toString(v);
    }

    public static String toString(long v) {
        if (v <= Integer.MAX_VALUE && v >= Integer.MIN_VALUE) {
            return NumberOutput.toString((int)v);
        }
        return Long.toString(v);
    }

    public static String toString(double v) {
        return Double.toString(v);
    }

    public static String toString(float v) {
        return Float.toString(v);
    }

    public static boolean notFinite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value);
    }

    public static boolean notFinite(float value) {
        return Float.isNaN(value) || Float.isInfinite(value);
    }

    private static int _outputUptoBillion(int v, char[] b, int off) {
        if (v < MILLION) {
            if (v < 1000) {
                return NumberOutput._leading3(v, b, off);
            }
            int thousands = v / 1000;
            int ones = v - thousands * 1000;
            return NumberOutput._outputUptoMillion(b, off, thousands, ones);
        }
        int thousands = v / 1000;
        int ones = v - thousands * 1000;
        int millions = thousands / 1000;
        off = NumberOutput._leading3(millions, b, off);
        int enc = TRIPLET_TO_CHARS[thousands -= millions * 1000];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        return off;
    }

    private static int _outputFullBillion(int v, char[] b, int off) {
        int thousands = v / 1000;
        int ones = v - thousands * 1000;
        int millions = thousands / 1000;
        int enc = TRIPLET_TO_CHARS[millions];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        enc = TRIPLET_TO_CHARS[thousands -= millions * 1000];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        return off;
    }

    private static int _outputUptoBillion(int v, byte[] b, int off) {
        if (v < MILLION) {
            if (v < 1000) {
                return NumberOutput._leading3(v, b, off);
            }
            int thousands = v / 1000;
            int ones = v - thousands * 1000;
            return NumberOutput._outputUptoMillion(b, off, thousands, ones);
        }
        int thousands = v / 1000;
        int ones = v - thousands * 1000;
        int millions = thousands / 1000;
        off = NumberOutput._leading3(millions, b, off);
        int enc = TRIPLET_TO_CHARS[thousands -= millions * 1000];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        return off;
    }

    private static int _outputFullBillion(int v, byte[] b, int off) {
        int thousands = v / 1000;
        int ones = v - thousands * 1000;
        int millions = thousands / 1000;
        int enc = TRIPLET_TO_CHARS[millions];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        enc = TRIPLET_TO_CHARS[thousands -= millions * 1000];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        return off;
    }

    private static int _outputUptoMillion(char[] b, int off, int thousands, int ones) {
        int enc = TRIPLET_TO_CHARS[thousands];
        if (thousands > 9) {
            if (thousands > 99) {
                b[off++] = (char)(enc >> 16);
            }
            b[off++] = (char)(enc >> 8 & 0x7F);
        }
        b[off++] = (char)(enc & 0x7F);
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        return off;
    }

    private static int _outputUptoMillion(byte[] b, int off, int thousands, int ones) {
        int enc = TRIPLET_TO_CHARS[thousands];
        if (thousands > 9) {
            if (thousands > 99) {
                b[off++] = (byte)(enc >> 16);
            }
            b[off++] = (byte)(enc >> 8);
        }
        b[off++] = (byte)enc;
        enc = TRIPLET_TO_CHARS[ones];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        return off;
    }

    private static int _leading3(int t, char[] b, int off) {
        int enc = TRIPLET_TO_CHARS[t];
        if (t > 9) {
            if (t > 99) {
                b[off++] = (char)(enc >> 16);
            }
            b[off++] = (char)(enc >> 8 & 0x7F);
        }
        b[off++] = (char)(enc & 0x7F);
        return off;
    }

    private static int _leading3(int t, byte[] b, int off) {
        int enc = TRIPLET_TO_CHARS[t];
        if (t > 9) {
            if (t > 99) {
                b[off++] = (byte)(enc >> 16);
            }
            b[off++] = (byte)(enc >> 8);
        }
        b[off++] = (byte)enc;
        return off;
    }

    private static int _full3(int t, char[] b, int off) {
        int enc = TRIPLET_TO_CHARS[t];
        b[off++] = (char)(enc >> 16);
        b[off++] = (char)(enc >> 8 & 0x7F);
        b[off++] = (char)(enc & 0x7F);
        return off;
    }

    private static int _full3(int t, byte[] b, int off) {
        int enc = TRIPLET_TO_CHARS[t];
        b[off++] = (byte)(enc >> 16);
        b[off++] = (byte)(enc >> 8);
        b[off++] = (byte)enc;
        return off;
    }

    private static int _outputSmallestL(char[] b, int off) {
        int len = SMALLEST_LONG.length();
        SMALLEST_LONG.getChars(0, len, b, off);
        return off + len;
    }

    private static int _outputSmallestL(byte[] b, int off) {
        int len = SMALLEST_LONG.length();
        for (int i = 0; i < len; ++i) {
            b[off++] = (byte)SMALLEST_LONG.charAt(i);
        }
        return off;
    }

    private static int _outputSmallestI(char[] b, int off) {
        int len = SMALLEST_INT.length();
        SMALLEST_INT.getChars(0, len, b, off);
        return off + len;
    }

    private static int _outputSmallestI(byte[] b, int off) {
        int len = SMALLEST_INT.length();
        for (int i = 0; i < len; ++i) {
            b[off++] = (byte)SMALLEST_INT.charAt(i);
        }
        return off;
    }

    static {
        int fullIx = 0;
        for (int i1 = 0; i1 < 10; ++i1) {
            for (int i2 = 0; i2 < 10; ++i2) {
                for (int i3 = 0; i3 < 10; ++i3) {
                    int enc = i1 + 48 << 16 | i2 + 48 << 8 | i3 + 48;
                    NumberOutput.TRIPLET_TO_CHARS[fullIx++] = enc;
                }
            }
        }
        sSmallIntStrs = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        sSmallIntStrs2 = new String[]{"-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10"};
    }
}

