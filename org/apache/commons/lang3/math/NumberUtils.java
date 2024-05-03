/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.math;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;

public class NumberUtils {
    public static final Long LONG_ZERO = 0L;
    public static final Long LONG_ONE = 1L;
    public static final Long LONG_MINUS_ONE = -1L;
    public static final Integer INTEGER_ZERO = 0;
    public static final Integer INTEGER_ONE = 1;
    public static final Integer INTEGER_MINUS_ONE = -1;
    public static final Short SHORT_ZERO = 0;
    public static final Short SHORT_ONE = 1;
    public static final Short SHORT_MINUS_ONE = -1;
    public static final Byte BYTE_ZERO = 0;
    public static final Byte BYTE_ONE = 1;
    public static final Byte BYTE_MINUS_ONE = -1;
    public static final Double DOUBLE_ZERO = 0.0;
    public static final Double DOUBLE_ONE = 1.0;
    public static final Double DOUBLE_MINUS_ONE = -1.0;
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);

    public static int toInt(String str) {
        return NumberUtils.toInt(str, 0);
    }

    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static long toLong(String str) {
        return NumberUtils.toLong(str, 0L);
    }

    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static float toFloat(String str) {
        return NumberUtils.toFloat(str, 0.0f);
    }

    public static float toFloat(String str, float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static double toDouble(String str) {
        return NumberUtils.toDouble(str, 0.0);
    }

    public static double toDouble(String str, double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static byte toByte(String str) {
        return NumberUtils.toByte(str, (byte)0);
    }

    public static byte toByte(String str, byte defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static short toShort(String str) {
        return NumberUtils.toShort(str, (short)0);
    }

    public static short toShort(String str, short defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static Number createNumber(String str) throws NumberFormatException {
        String exp;
        String mant;
        String dec;
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        String[] hex_prefixes = new String[]{"0x", "0X", "-0x", "-0X", "#", "-#"};
        int pfxLen = 0;
        for (String pfx : hex_prefixes) {
            if (!str.startsWith(pfx)) continue;
            pfxLen += pfx.length();
            break;
        }
        if (pfxLen > 0) {
            char firstSigDigit = '\u0000';
            for (int i = pfxLen; i < str.length() && (firstSigDigit = str.charAt(i)) == '0'; ++i) {
                ++pfxLen;
            }
            int hexDigits = str.length() - pfxLen;
            if (hexDigits > 16 || hexDigits == 16 && firstSigDigit > '7') {
                return NumberUtils.createBigInteger(str);
            }
            if (hexDigits > 8 || hexDigits == 8 && firstSigDigit > '7') {
                return NumberUtils.createLong(str);
            }
            return NumberUtils.createInteger(str);
        }
        char lastChar = str.charAt(str.length() - 1);
        int decPos = str.indexOf(46);
        int expPos = str.indexOf(101) + str.indexOf(69) + 1;
        int numDecimals = 0;
        if (decPos > -1) {
            if (expPos > -1) {
                if (expPos < decPos || expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
            numDecimals = dec.length();
        } else {
            if (expPos > -1) {
                if (expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            exp = expPos > -1 && expPos < str.length() - 1 ? str.substring(expPos + 1, str.length() - 1) : null;
            String numeric = str.substring(0, str.length() - 1);
            boolean allZeros = NumberUtils.isAllZeros(mant) && NumberUtils.isAllZeros(exp);
            switch (lastChar) {
                case 'L': 
                case 'l': {
                    if (dec == null && exp == null && (numeric.charAt(0) == '-' && NumberUtils.isDigits(numeric.substring(1)) || NumberUtils.isDigits(numeric))) {
                        try {
                            return NumberUtils.createLong(numeric);
                        } catch (NumberFormatException nfe) {
                            return NumberUtils.createBigInteger(numeric);
                        }
                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                case 'F': 
                case 'f': {
                    try {
                        Float f = NumberUtils.createFloat(numeric);
                        if (!f.isInfinite() && (f.floatValue() != 0.0f || allZeros)) {
                            return f;
                        }
                    } catch (NumberFormatException nfe) {
                        // empty catch block
                    }
                }
                case 'D': 
                case 'd': {
                    try {
                        Double d = NumberUtils.createDouble(numeric);
                        if (!d.isInfinite() && ((double)d.floatValue() != 0.0 || allZeros)) {
                            return d;
                        }
                    } catch (NumberFormatException nfe) {
                        // empty catch block
                    }
                    try {
                        return NumberUtils.createBigDecimal(numeric);
                    } catch (NumberFormatException e) {
                        // empty catch block
                    }
                }
            }
            throw new NumberFormatException(str + " is not a valid number.");
        }
        exp = expPos > -1 && expPos < str.length() - 1 ? str.substring(expPos + 1, str.length()) : null;
        if (dec == null && exp == null) {
            try {
                return NumberUtils.createInteger(str);
            } catch (NumberFormatException nfe) {
                try {
                    return NumberUtils.createLong(str);
                } catch (NumberFormatException nfe2) {
                    return NumberUtils.createBigInteger(str);
                }
            }
        }
        boolean allZeros = NumberUtils.isAllZeros(mant) && NumberUtils.isAllZeros(exp);
        try {
            Float f;
            if (numDecimals <= 7 && !(f = NumberUtils.createFloat(str)).isInfinite() && (f.floatValue() != 0.0f || allZeros)) {
                return f;
            }
        } catch (NumberFormatException nfe) {
            // empty catch block
        }
        try {
            Double d;
            if (numDecimals <= 16 && !(d = NumberUtils.createDouble(str)).isInfinite() && (d != 0.0 || allZeros)) {
                return d;
            }
        } catch (NumberFormatException nfe) {
            // empty catch block
        }
        return NumberUtils.createBigDecimal(str);
    }

    private static boolean isAllZeros(String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; --i) {
            if (str.charAt(i) == '0') continue;
            return false;
        }
        return str.length() > 0;
    }

    public static Float createFloat(String str) {
        if (str == null) {
            return null;
        }
        return Float.valueOf(str);
    }

    public static Double createDouble(String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    public static Integer createInteger(String str) {
        if (str == null) {
            return null;
        }
        return Integer.decode(str);
    }

    public static Long createLong(String str) {
        if (str == null) {
            return null;
        }
        return Long.decode(str);
    }

    public static BigInteger createBigInteger(String str) {
        if (str == null) {
            return null;
        }
        int pos = 0;
        int radix = 10;
        boolean negate = false;
        if (str.startsWith("-")) {
            negate = true;
            pos = 1;
        }
        if (str.startsWith("0x", pos) || str.startsWith("0x", pos)) {
            radix = 16;
            pos += 2;
        } else if (str.startsWith("#", pos)) {
            radix = 16;
            ++pos;
        } else if (str.startsWith("0", pos) && str.length() > pos + 1) {
            radix = 8;
            ++pos;
        }
        BigInteger value = new BigInteger(str.substring(pos), radix);
        return negate ? value.negate() : value;
    }

    public static BigDecimal createBigDecimal(String str) {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if (str.trim().startsWith("--")) {
            throw new NumberFormatException(str + " is not a valid number.");
        }
        return new BigDecimal(str);
    }

    public static long min(long[] array) {
        NumberUtils.validateArray(array);
        long min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (array[i] >= min) continue;
            min = array[i];
        }
        return min;
    }

    public static int min(int[] array) {
        NumberUtils.validateArray(array);
        int min = array[0];
        for (int j = 1; j < array.length; ++j) {
            if (array[j] >= min) continue;
            min = array[j];
        }
        return min;
    }

    public static short min(short[] array) {
        NumberUtils.validateArray(array);
        short min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (array[i] >= min) continue;
            min = array[i];
        }
        return min;
    }

    public static byte min(byte[] array) {
        NumberUtils.validateArray(array);
        byte min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (array[i] >= min) continue;
            min = array[i];
        }
        return min;
    }

    public static double min(double[] array) {
        NumberUtils.validateArray(array);
        double min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }
            if (!(array[i] < min)) continue;
            min = array[i];
        }
        return min;
    }

    public static float min(float[] array) {
        NumberUtils.validateArray(array);
        float min = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }
            if (!(array[i] < min)) continue;
            min = array[i];
        }
        return min;
    }

    public static long max(long[] array) {
        NumberUtils.validateArray(array);
        long max = array[0];
        for (int j = 1; j < array.length; ++j) {
            if (array[j] <= max) continue;
            max = array[j];
        }
        return max;
    }

    public static int max(int[] array) {
        NumberUtils.validateArray(array);
        int max = array[0];
        for (int j = 1; j < array.length; ++j) {
            if (array[j] <= max) continue;
            max = array[j];
        }
        return max;
    }

    public static short max(short[] array) {
        NumberUtils.validateArray(array);
        short max = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (array[i] <= max) continue;
            max = array[i];
        }
        return max;
    }

    public static byte max(byte[] array) {
        NumberUtils.validateArray(array);
        byte max = array[0];
        for (int i = 1; i < array.length; ++i) {
            if (array[i] <= max) continue;
            max = array[i];
        }
        return max;
    }

    public static double max(double[] array) {
        NumberUtils.validateArray(array);
        double max = array[0];
        for (int j = 1; j < array.length; ++j) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (!(array[j] > max)) continue;
            max = array[j];
        }
        return max;
    }

    public static float max(float[] array) {
        NumberUtils.validateArray(array);
        float max = array[0];
        for (int j = 1; j < array.length; ++j) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }
            if (!(array[j] > max)) continue;
            max = array[j];
        }
        return max;
    }

    private static void validateArray(Object array) {
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (Array.getLength(array) == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    }

    public static long min(long a, long b, long c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    public static int min(int a, int b, int c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    public static short min(short a, short b, short c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    public static byte min(byte a, byte b, byte c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    public static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    public static long max(long a, long b, long c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    public static int max(int a, int b, int c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    public static short max(short a, short b, short c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    public static byte max(byte a, byte b, byte c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    public static boolean isDigits(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); ++i) {
            if (Character.isDigit(str.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isNumber(String str) {
        int i;
        int start;
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        int n = start = chars[0] == '-' ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0') {
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
                int i2 = start + 2;
                if (i2 == sz) {
                    return false;
                }
                while (i2 < chars.length) {
                    if (!(chars[i2] >= '0' && chars[i2] <= '9' || chars[i2] >= 'a' && chars[i2] <= 'f' || chars[i2] >= 'A' && chars[i2] <= 'F')) {
                        return false;
                    }
                    ++i2;
                }
                return true;
            }
            if (Character.isDigit(chars[start + 1])) {
                for (int i3 = start + 1; i3 < chars.length; ++i3) {
                    if (chars[i3] >= '0' && chars[i3] <= '7') continue;
                    return false;
                }
                return true;
            }
        }
        --sz;
        for (i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; ++i) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;
                continue;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    return false;
                }
                hasDecPoint = true;
                continue;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                if (hasExp) {
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
                continue;
            }
            if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false;
                continue;
            }
            return false;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    return false;
                }
                return foundDigit;
            }
            if (!(allowSigns || chars[i] != 'd' && chars[i] != 'D' && chars[i] != 'f' && chars[i] != 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                return foundDigit && !hasExp && !hasDecPoint;
            }
            return false;
        }
        return !allowSigns && foundDigit;
    }
}

