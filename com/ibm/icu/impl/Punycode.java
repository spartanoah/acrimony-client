/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.UTF16;

public final class Punycode {
    private static final int BASE = 36;
    private static final int TMIN = 1;
    private static final int TMAX = 26;
    private static final int SKEW = 38;
    private static final int DAMP = 700;
    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 128;
    private static final int HYPHEN = 45;
    private static final int DELIMITER = 45;
    private static final int ZERO = 48;
    private static final int SMALL_A = 97;
    private static final int SMALL_Z = 122;
    private static final int CAPITAL_A = 65;
    private static final int CAPITAL_Z = 90;
    private static final int MAX_CP_COUNT = 200;
    static final int[] basicToDigit = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private static int adaptBias(int delta, int length, boolean firstTime) {
        delta = firstTime ? (delta /= 700) : (delta /= 2);
        delta += delta / length;
        int count = 0;
        while (delta > 455) {
            delta /= 35;
            count += 36;
        }
        return count + 36 * delta / (delta + 38);
    }

    private static char asciiCaseMap(char b, boolean uppercase) {
        if (uppercase) {
            if ('a' <= b && b <= 'z') {
                b = (char)(b - 32);
            }
        } else if ('A' <= b && b <= 'Z') {
            b = (char)(b + 32);
        }
        return b;
    }

    private static char digitToBasic(int digit, boolean uppercase) {
        if (digit < 26) {
            if (uppercase) {
                return (char)(65 + digit);
            }
            return (char)(97 + digit);
        }
        return (char)(22 + digit);
    }

    public static StringBuilder encode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int n;
        int j;
        int[] cpBuffer = new int[200];
        int srcLength = src.length();
        int destCapacity = 200;
        char[] dest = new char[destCapacity];
        StringBuilder result = new StringBuilder();
        int destLength = 0;
        int srcCPCount = 0;
        for (j = 0; j < srcLength; ++j) {
            char c2;
            if (srcCPCount == 200) {
                throw new IndexOutOfBoundsException();
            }
            char c = src.charAt(j);
            if (Punycode.isBasic(c)) {
                if (destLength < destCapacity) {
                    cpBuffer[srcCPCount++] = 0;
                    dest[destLength] = caseFlags != null ? Punycode.asciiCaseMap(c, caseFlags[j]) : c;
                }
                ++destLength;
                continue;
            }
            n = (caseFlags != null && caseFlags[j] ? 1 : 0) << 31;
            if (!UTF16.isSurrogate(c)) {
                n |= c;
            } else if (UTF16.isLeadSurrogate(c) && j + 1 < srcLength && UTF16.isTrailSurrogate(c2 = src.charAt(j + 1))) {
                ++j;
                n |= UCharacter.getCodePoint(c, c2);
            } else {
                throw new StringPrepParseException("Illegal char found", 1);
            }
            cpBuffer[srcCPCount++] = n;
        }
        int basicLength = destLength;
        if (basicLength > 0) {
            if (destLength < destCapacity) {
                dest[destLength] = 45;
            }
            ++destLength;
        }
        n = 128;
        int delta = 0;
        int bias = 72;
        int handledCPCount = basicLength;
        while (handledCPCount < srcCPCount) {
            int q;
            int m = Integer.MAX_VALUE;
            for (j = 0; j < srcCPCount; ++j) {
                q = cpBuffer[j] & Integer.MAX_VALUE;
                if (n > q || q >= m) continue;
                m = q;
            }
            if (m - n > (0x7FFFFF37 - delta) / (handledCPCount + 1)) {
                throw new IllegalStateException("Internal program error");
            }
            delta += (m - n) * (handledCPCount + 1);
            n = m;
            for (j = 0; j < srcCPCount; ++j) {
                q = cpBuffer[j] & Integer.MAX_VALUE;
                if (q < n) {
                    ++delta;
                    continue;
                }
                if (q != n) continue;
                q = delta;
                int k = 36;
                while (true) {
                    int t;
                    if ((t = k - bias) < 1) {
                        t = 1;
                    } else if (k >= bias + 26) {
                        t = 26;
                    }
                    if (q < t) break;
                    if (destLength < destCapacity) {
                        dest[destLength++] = Punycode.digitToBasic(t + (q - t) % (36 - t), false);
                    }
                    q = (q - t) / (36 - t);
                    k += 36;
                }
                if (destLength < destCapacity) {
                    dest[destLength++] = Punycode.digitToBasic(q, cpBuffer[j] < 0);
                }
                bias = Punycode.adaptBias(delta, handledCPCount + 1, handledCPCount == basicLength);
                delta = 0;
                ++handledCPCount;
            }
            ++delta;
            ++n;
        }
        return result.append(dest, 0, destLength);
    }

    private static boolean isBasic(int ch) {
        return ch < 128;
    }

    private static boolean isBasicUpperCase(int ch) {
        return 65 <= ch && ch >= 90;
    }

    private static boolean isSurrogate(int ch) {
        return (ch & 0xFFFFF800) == 55296;
    }

    public static StringBuilder decode(CharSequence src, boolean[] caseFlags) throws StringPrepParseException {
        int in;
        int destCPCount;
        int srcLength = src.length();
        StringBuilder result = new StringBuilder();
        int destCapacity = 200;
        char[] dest = new char[destCapacity];
        int j = srcLength;
        while (j > 0 && src.charAt(--j) != '-') {
        }
        int basicLength = destCPCount = j;
        int destLength = destCPCount;
        while (j > 0) {
            char b;
            if (!Punycode.isBasic(b = src.charAt(--j))) {
                throw new StringPrepParseException("Illegal char found", 0);
            }
            if (j >= destCapacity) continue;
            dest[j] = b;
            if (caseFlags == null) continue;
            caseFlags[j] = Punycode.isBasicUpperCase(b);
        }
        int n = 128;
        int i = 0;
        int bias = 72;
        int firstSupplementaryIndex = 1000000000;
        int n2 = in = basicLength > 0 ? basicLength + 1 : 0;
        while (in < srcLength) {
            int oldi = i;
            int w = 1;
            int k = 36;
            while (true) {
                int digit;
                if (in >= srcLength) {
                    throw new StringPrepParseException("Illegal char found", 1);
                }
                if ((digit = basicToDigit[src.charAt(in++) & 0xFF]) < 0) {
                    throw new StringPrepParseException("Invalid char found", 0);
                }
                if (digit > (Integer.MAX_VALUE - i) / w) {
                    throw new StringPrepParseException("Illegal char found", 1);
                }
                i += digit * w;
                int t = k - bias;
                if (t < 1) {
                    t = 1;
                } else if (k >= bias + 26) {
                    t = 26;
                }
                if (digit < t) break;
                if (w > Integer.MAX_VALUE / (36 - t)) {
                    throw new StringPrepParseException("Illegal char found", 1);
                }
                w *= 36 - t;
                k += 36;
            }
            bias = Punycode.adaptBias(i - oldi, ++destCPCount, oldi == 0);
            if (i / destCPCount > Integer.MAX_VALUE - n) {
                throw new StringPrepParseException("Illegal char found", 1);
            }
            n += i / destCPCount;
            i %= destCPCount;
            if (n > 0x10FFFF || Punycode.isSurrogate(n)) {
                throw new StringPrepParseException("Illegal char found", 1);
            }
            int cpLength = UTF16.getCharCount(n);
            if (destLength + cpLength < destCapacity) {
                int codeUnitIndex;
                if (i <= firstSupplementaryIndex) {
                    codeUnitIndex = i;
                    firstSupplementaryIndex = cpLength > 1 ? codeUnitIndex : ++firstSupplementaryIndex;
                } else {
                    codeUnitIndex = firstSupplementaryIndex;
                    codeUnitIndex = UTF16.moveCodePointOffset(dest, 0, destLength, codeUnitIndex, i - codeUnitIndex);
                }
                if (codeUnitIndex < destLength) {
                    System.arraycopy(dest, codeUnitIndex, dest, codeUnitIndex + cpLength, destLength - codeUnitIndex);
                    if (caseFlags != null) {
                        System.arraycopy(caseFlags, codeUnitIndex, caseFlags, codeUnitIndex + cpLength, destLength - codeUnitIndex);
                    }
                }
                if (cpLength == 1) {
                    dest[codeUnitIndex] = (char)n;
                } else {
                    dest[codeUnitIndex] = UTF16.getLeadSurrogate(n);
                    dest[codeUnitIndex + 1] = UTF16.getTrailSurrogate(n);
                }
                if (caseFlags != null) {
                    caseFlags[codeUnitIndex] = Punycode.isBasicUpperCase(src.charAt(in - 1));
                    if (cpLength == 2) {
                        caseFlags[codeUnitIndex + 1] = false;
                    }
                }
            }
            destLength += cpLength;
            ++i;
        }
        result.append(dest, 0, destLength);
        return result;
    }
}

