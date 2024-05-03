/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import java.math.BigDecimal;
import java.math.BigInteger;

final class DigitList {
    public static final int MAX_LONG_DIGITS = 19;
    public static final int DBL_DIG = 17;
    public int decimalAt = 0;
    public int count = 0;
    public byte[] digits = new byte[19];
    private static byte[] LONG_MIN_REP;

    DigitList() {
    }

    private final void ensureCapacity(int digitCapacity, int digitsToCopy) {
        if (digitCapacity > this.digits.length) {
            byte[] newDigits = new byte[digitCapacity * 2];
            System.arraycopy(this.digits, 0, newDigits, 0, digitsToCopy);
            this.digits = newDigits;
        }
    }

    boolean isZero() {
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == 48) continue;
            return false;
        }
        return true;
    }

    public void append(int digit) {
        this.ensureCapacity(this.count + 1, this.count);
        this.digits[this.count++] = (byte)digit;
    }

    public byte getDigitValue(int i) {
        return (byte)(this.digits[i] - 48);
    }

    public final double getDouble() {
        if (this.count == 0) {
            return 0.0;
        }
        StringBuilder temp = new StringBuilder(this.count);
        temp.append('.');
        for (int i = 0; i < this.count; ++i) {
            temp.append((char)this.digits[i]);
        }
        temp.append('E');
        temp.append(Integer.toString(this.decimalAt));
        return Double.valueOf(temp.toString());
    }

    public final long getLong() {
        if (this.count == 0) {
            return 0L;
        }
        if (this.isLongMIN_VALUE()) {
            return Long.MIN_VALUE;
        }
        StringBuilder temp = new StringBuilder(this.count);
        for (int i = 0; i < this.decimalAt; ++i) {
            temp.append(i < this.count ? (char)this.digits[i] : (char)'0');
        }
        return Long.parseLong(temp.toString());
    }

    public BigInteger getBigInteger(boolean isPositive) {
        int i;
        int len;
        if (this.isZero()) {
            return BigInteger.valueOf(0L);
        }
        int n = len = this.decimalAt > this.count ? this.decimalAt : this.count;
        if (!isPositive) {
            ++len;
        }
        char[] text = new char[len];
        int n2 = 0;
        if (!isPositive) {
            text[0] = 45;
            for (i = 0; i < this.count; ++i) {
                text[i + 1] = (char)this.digits[i];
            }
            n2 = this.count + 1;
        } else {
            for (i = 0; i < this.count; ++i) {
                text[i] = (char)this.digits[i];
            }
            n2 = this.count;
        }
        for (i = n2; i < text.length; ++i) {
            text[i] = 48;
        }
        return new BigInteger(new String(text));
    }

    private String getStringRep(boolean isPositive) {
        int d;
        if (this.isZero()) {
            return "0";
        }
        StringBuilder stringRep = new StringBuilder(this.count + 1);
        if (!isPositive) {
            stringRep.append('-');
        }
        if ((d = this.decimalAt) < 0) {
            stringRep.append('.');
            while (d < 0) {
                stringRep.append('0');
                ++d;
            }
            d = -1;
        }
        for (int i = 0; i < this.count; ++i) {
            if (d == i) {
                stringRep.append('.');
            }
            stringRep.append((char)this.digits[i]);
        }
        while (d-- > this.count) {
            stringRep.append('0');
        }
        return stringRep.toString();
    }

    public BigDecimal getBigDecimal(boolean isPositive) {
        if (this.isZero()) {
            return BigDecimal.valueOf(0L);
        }
        long scale = (long)this.count - (long)this.decimalAt;
        if (scale > 0L) {
            int numDigits = this.count;
            if (scale > Integer.MAX_VALUE) {
                long numShift = scale - Integer.MAX_VALUE;
                if (numShift < (long)this.count) {
                    numDigits = (int)((long)numDigits - numShift);
                } else {
                    return new BigDecimal(0);
                }
            }
            StringBuilder significantDigits = new StringBuilder(numDigits + 1);
            if (!isPositive) {
                significantDigits.append('-');
            }
            for (int i = 0; i < numDigits; ++i) {
                significantDigits.append((char)this.digits[i]);
            }
            BigInteger unscaledVal = new BigInteger(significantDigits.toString());
            return new BigDecimal(unscaledVal, (int)scale);
        }
        return new BigDecimal(this.getStringRep(isPositive));
    }

    public com.ibm.icu.math.BigDecimal getBigDecimalICU(boolean isPositive) {
        if (this.isZero()) {
            return com.ibm.icu.math.BigDecimal.valueOf(0L);
        }
        long scale = (long)this.count - (long)this.decimalAt;
        if (scale > 0L) {
            int numDigits = this.count;
            if (scale > Integer.MAX_VALUE) {
                long numShift = scale - Integer.MAX_VALUE;
                if (numShift < (long)this.count) {
                    numDigits = (int)((long)numDigits - numShift);
                } else {
                    return new com.ibm.icu.math.BigDecimal(0);
                }
            }
            StringBuilder significantDigits = new StringBuilder(numDigits + 1);
            if (!isPositive) {
                significantDigits.append('-');
            }
            for (int i = 0; i < numDigits; ++i) {
                significantDigits.append((char)this.digits[i]);
            }
            BigInteger unscaledVal = new BigInteger(significantDigits.toString());
            return new com.ibm.icu.math.BigDecimal(unscaledVal, (int)scale);
        }
        return new com.ibm.icu.math.BigDecimal(this.getStringRep(isPositive));
    }

    boolean isIntegral() {
        while (this.count > 0 && this.digits[this.count - 1] == 48) {
            --this.count;
        }
        return this.count == 0 || this.decimalAt >= this.count;
    }

    final void set(double source, int maximumDigits, boolean fixedPoint) {
        if (source == 0.0) {
            source = 0.0;
        }
        String rep = Double.toString(source);
        this.set(rep, 19);
        if (fixedPoint) {
            if (-this.decimalAt > maximumDigits) {
                this.count = 0;
                return;
            }
            if (-this.decimalAt == maximumDigits) {
                if (this.shouldRoundUp(0)) {
                    this.count = 1;
                    ++this.decimalAt;
                    this.digits[0] = 49;
                } else {
                    this.count = 0;
                }
                return;
            }
        }
        while (this.count > 1 && this.digits[this.count - 1] == 48) {
            --this.count;
        }
        this.round(fixedPoint ? maximumDigits + this.decimalAt : (maximumDigits == 0 ? -1 : maximumDigits));
    }

    private void set(String rep, int maxCount) {
        this.decimalAt = -1;
        this.count = 0;
        int exponent = 0;
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        int i = 0;
        if (rep.charAt(i) == '-') {
            ++i;
        }
        while (i < rep.length()) {
            char c = rep.charAt(i);
            if (c == '.') {
                this.decimalAt = this.count;
            } else {
                if (c == 'e' || c == 'E') {
                    if (rep.charAt(++i) == '+') {
                        ++i;
                    }
                    exponent = Integer.valueOf(rep.substring(i));
                    break;
                }
                if (this.count < maxCount) {
                    if (!nonZeroDigitSeen) {
                        boolean bl = nonZeroDigitSeen = c != '0';
                        if (!nonZeroDigitSeen && this.decimalAt != -1) {
                            ++leadingZerosAfterDecimal;
                        }
                    }
                    if (nonZeroDigitSeen) {
                        this.ensureCapacity(this.count + 1, this.count);
                        this.digits[this.count++] = (byte)c;
                    }
                }
            }
            ++i;
        }
        if (this.decimalAt == -1) {
            this.decimalAt = this.count;
        }
        this.decimalAt += exponent - leadingZerosAfterDecimal;
    }

    private boolean shouldRoundUp(int maximumDigits) {
        if (maximumDigits < this.count) {
            if (this.digits[maximumDigits] > 53) {
                return true;
            }
            if (this.digits[maximumDigits] == 53) {
                for (int i = maximumDigits + 1; i < this.count; ++i) {
                    if (this.digits[i] == 48) continue;
                    return true;
                }
                return maximumDigits > 0 && this.digits[maximumDigits - 1] % 2 != 0;
            }
        }
        return false;
    }

    public final void round(int maximumDigits) {
        if (maximumDigits >= 0 && maximumDigits < this.count) {
            if (this.shouldRoundUp(maximumDigits)) {
                do {
                    if (--maximumDigits < 0) {
                        this.digits[0] = 49;
                        ++this.decimalAt;
                        maximumDigits = 0;
                        break;
                    }
                    int n = maximumDigits;
                    this.digits[n] = (byte)(this.digits[n] + 1);
                } while (this.digits[maximumDigits] > 57);
                ++maximumDigits;
            }
            this.count = maximumDigits;
        }
        while (this.count > 1 && this.digits[this.count - 1] == 48) {
            --this.count;
        }
    }

    public final void set(long source) {
        this.set(source, 0);
    }

    public final void set(long source, int maximumDigits) {
        if (source <= 0L) {
            if (source == Long.MIN_VALUE) {
                this.count = 19;
                this.decimalAt = 19;
                System.arraycopy(LONG_MIN_REP, 0, this.digits, 0, this.count);
            } else {
                this.count = 0;
                this.decimalAt = 0;
            }
        } else {
            int left = 19;
            while (source > 0L) {
                this.digits[--left] = (byte)(48L + source % 10L);
                source /= 10L;
            }
            this.decimalAt = 19 - left;
            int right = 18;
            while (this.digits[right] == 48) {
                --right;
            }
            this.count = right - left + 1;
            System.arraycopy(this.digits, left, this.digits, 0, this.count);
        }
        if (maximumDigits > 0) {
            this.round(maximumDigits);
        }
    }

    public final void set(BigInteger source, int maximumDigits) {
        String stringDigits = source.toString();
        this.count = this.decimalAt = stringDigits.length();
        while (this.count > 1 && stringDigits.charAt(this.count - 1) == '0') {
            --this.count;
        }
        int offset = 0;
        if (stringDigits.charAt(0) == '-') {
            ++offset;
            --this.count;
            --this.decimalAt;
        }
        this.ensureCapacity(this.count, 0);
        for (int i = 0; i < this.count; ++i) {
            this.digits[i] = (byte)stringDigits.charAt(i + offset);
        }
        if (maximumDigits > 0) {
            this.round(maximumDigits);
        }
    }

    private void setBigDecimalDigits(String stringDigits, int maximumDigits, boolean fixedPoint) {
        this.set(stringDigits, stringDigits.length());
        this.round(fixedPoint ? maximumDigits + this.decimalAt : (maximumDigits == 0 ? -1 : maximumDigits));
    }

    public final void set(BigDecimal source, int maximumDigits, boolean fixedPoint) {
        this.setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    public final void set(com.ibm.icu.math.BigDecimal source, int maximumDigits, boolean fixedPoint) {
        this.setBigDecimalDigits(source.toString(), maximumDigits, fixedPoint);
    }

    private boolean isLongMIN_VALUE() {
        if (this.decimalAt != this.count || this.count != 19) {
            return false;
        }
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == LONG_MIN_REP[i]) continue;
            return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DigitList)) {
            return false;
        }
        DigitList other = (DigitList)obj;
        if (this.count != other.count || this.decimalAt != other.decimalAt) {
            return false;
        }
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == other.digits[i]) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashcode = this.decimalAt;
        for (int i = 0; i < this.count; ++i) {
            hashcode = hashcode * 37 + this.digits[i];
        }
        return hashcode;
    }

    public String toString() {
        if (this.isZero()) {
            return "0";
        }
        StringBuilder buf = new StringBuilder("0.");
        for (int i = 0; i < this.count; ++i) {
            buf.append((char)this.digits[i]);
        }
        buf.append("x10^");
        buf.append(this.decimalAt);
        return buf.toString();
    }

    static {
        String s = Long.toString(Long.MIN_VALUE);
        LONG_MIN_REP = new byte[19];
        for (int i = 0; i < 19; ++i) {
            DigitList.LONG_MIN_REP[i] = (byte)s.charAt(i + 1);
        }
    }
}

