/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import java.math.BigDecimal;
import java.math.BigInteger;

class NumberConversionUtil {
    NumberConversionUtil() {
    }

    static Number stringToNumber(String input) throws NumberFormatException {
        char initial;
        String val2 = input;
        if (val2.startsWith(".")) {
            val2 = "0" + val2;
        }
        if (val2.startsWith("-.")) {
            val2 = "-0." + val2.substring(2);
        }
        if (NumberConversionUtil.isNumericChar(initial = val2.charAt(0)) || initial == '-') {
            BigInteger bi;
            char at1;
            if (NumberConversionUtil.isDecimalNotation(val2)) {
                try {
                    BigDecimal bd = new BigDecimal(val2);
                    if (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0) {
                        return -0.0;
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    try {
                        Double d = Double.valueOf(val2);
                        if (d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val [" + input + "] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val [" + input + "] is not a valid number.");
                    }
                }
            }
            val2 = NumberConversionUtil.removeLeadingZerosOfNumber(input);
            initial = val2.charAt(0);
            if (initial == '0' && val2.length() > 1) {
                at1 = val2.charAt(1);
                if (NumberConversionUtil.isNumericChar(at1)) {
                    throw new NumberFormatException("val [" + input + "] is not a valid number.");
                }
            } else if (initial == '-' && val2.length() > 2) {
                at1 = val2.charAt(1);
                char at2 = val2.charAt(2);
                if (at1 == '0' && NumberConversionUtil.isNumericChar(at2)) {
                    throw new NumberFormatException("val [" + input + "] is not a valid number.");
                }
            }
            if ((bi = new BigInteger(val2)).bitLength() <= 31) {
                return bi.intValue();
            }
            if (bi.bitLength() <= 63) {
                return bi.longValue();
            }
            return bi;
        }
        throw new NumberFormatException("val [" + input + "] is not a valid number.");
    }

    private static boolean isNumericChar(char c) {
        return c <= '9' && c >= '0';
    }

    static boolean potentialNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return NumberConversionUtil.potentialPositiveNumberStartingAtIndex(value, value.charAt(0) == '-' ? 1 : 0);
    }

    private static boolean isDecimalNotation(String val2) {
        return val2.indexOf(46) > -1 || val2.indexOf(101) > -1 || val2.indexOf(69) > -1 || "-0".equals(val2);
    }

    private static boolean potentialPositiveNumberStartingAtIndex(String value, int index) {
        if (index >= value.length()) {
            return false;
        }
        return NumberConversionUtil.digitAtIndex(value, value.charAt(index) == '.' ? index + 1 : index);
    }

    private static boolean digitAtIndex(String value, int index) {
        if (index >= value.length()) {
            return false;
        }
        return value.charAt(index) >= '0' && value.charAt(index) <= '9';
    }

    private static String removeLeadingZerosOfNumber(String value) {
        int counter;
        if (value.equals("-")) {
            return value;
        }
        boolean negativeFirstChar = value.charAt(0) == '-';
        int n = counter = negativeFirstChar ? 1 : 0;
        while (counter < value.length()) {
            if (value.charAt(counter) != '0') {
                if (negativeFirstChar) {
                    return "-".concat(value.substring(counter));
                }
                return value.substring(counter);
            }
            ++counter;
        }
        if (negativeFirstChar) {
            return "-0";
        }
        return "0";
    }
}

