/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.lang;

import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.impl.Norm2AllModes;
import com.ibm.icu.impl.Trie2;
import com.ibm.icu.impl.UBiDiProps;
import com.ibm.icu.impl.UCaseProps;
import com.ibm.icu.impl.UCharacterName;
import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.impl.UCharacterUtility;
import com.ibm.icu.impl.UPropertyAliases;
import com.ibm.icu.lang.UCharacterEnums;
import com.ibm.icu.lang.UCharacterNameIterator;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.RangeValueIterator;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ValueIterator;
import com.ibm.icu.util.VersionInfo;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public final class UCharacter
implements UCharacterEnums.ECharacterCategory,
UCharacterEnums.ECharacterDirection {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 0x10FFFF;
    public static final int SUPPLEMENTARY_MIN_VALUE = 65536;
    public static final int REPLACEMENT_CHAR = 65533;
    public static final double NO_NUMERIC_VALUE = -1.23456789E8;
    public static final int MIN_RADIX = 2;
    public static final int MAX_RADIX = 36;
    public static final int TITLECASE_NO_LOWERCASE = 256;
    public static final int TITLECASE_NO_BREAK_ADJUSTMENT = 512;
    public static final int FOLD_CASE_DEFAULT = 0;
    public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
    public static final char MIN_HIGH_SURROGATE = '\ud800';
    public static final char MAX_HIGH_SURROGATE = '\udbff';
    public static final char MIN_LOW_SURROGATE = '\udc00';
    public static final char MAX_LOW_SURROGATE = '\udfff';
    public static final char MIN_SURROGATE = '\ud800';
    public static final char MAX_SURROGATE = '\udfff';
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 65536;
    public static final int MAX_CODE_POINT = 0x10FFFF;
    public static final int MIN_CODE_POINT = 0;
    private static final int LAST_CHAR_MASK_ = 65535;
    private static final int NO_BREAK_SPACE_ = 160;
    private static final int FIGURE_SPACE_ = 8199;
    private static final int NARROW_NO_BREAK_SPACE_ = 8239;
    private static final int IDEOGRAPHIC_NUMBER_ZERO_ = 12295;
    private static final int CJK_IDEOGRAPH_FIRST_ = 19968;
    private static final int CJK_IDEOGRAPH_SECOND_ = 20108;
    private static final int CJK_IDEOGRAPH_THIRD_ = 19977;
    private static final int CJK_IDEOGRAPH_FOURTH_ = 22235;
    private static final int CJK_IDEOGRAPH_FIFTH_ = 20116;
    private static final int CJK_IDEOGRAPH_SIXTH_ = 20845;
    private static final int CJK_IDEOGRAPH_SEVENTH_ = 19971;
    private static final int CJK_IDEOGRAPH_EIGHTH_ = 20843;
    private static final int CJK_IDEOGRAPH_NINETH_ = 20061;
    private static final int APPLICATION_PROGRAM_COMMAND_ = 159;
    private static final int UNIT_SEPARATOR_ = 31;
    private static final int DELETE_ = 127;
    private static final int CJK_IDEOGRAPH_COMPLEX_ZERO_ = 38646;
    private static final int CJK_IDEOGRAPH_COMPLEX_ONE_ = 22777;
    private static final int CJK_IDEOGRAPH_COMPLEX_TWO_ = 36019;
    private static final int CJK_IDEOGRAPH_COMPLEX_THREE_ = 21443;
    private static final int CJK_IDEOGRAPH_COMPLEX_FOUR_ = 32902;
    private static final int CJK_IDEOGRAPH_COMPLEX_FIVE_ = 20237;
    private static final int CJK_IDEOGRAPH_COMPLEX_SIX_ = 38520;
    private static final int CJK_IDEOGRAPH_COMPLEX_SEVEN_ = 26578;
    private static final int CJK_IDEOGRAPH_COMPLEX_EIGHT_ = 25420;
    private static final int CJK_IDEOGRAPH_COMPLEX_NINE_ = 29590;
    private static final int CJK_IDEOGRAPH_TEN_ = 21313;
    private static final int CJK_IDEOGRAPH_COMPLEX_TEN_ = 25342;
    private static final int CJK_IDEOGRAPH_HUNDRED_ = 30334;
    private static final int CJK_IDEOGRAPH_COMPLEX_HUNDRED_ = 20336;
    private static final int CJK_IDEOGRAPH_THOUSAND_ = 21315;
    private static final int CJK_IDEOGRAPH_COMPLEX_THOUSAND_ = 20191;
    private static final int CJK_IDEOGRAPH_TEN_THOUSAND_ = 33356;
    private static final int CJK_IDEOGRAPH_HUNDRED_MILLION_ = 20740;

    public static int digit(int ch, int radix) {
        if (2 <= radix && radix <= 36) {
            int value = UCharacter.digit(ch);
            if (value < 0) {
                value = UCharacterProperty.getEuropeanDigit(ch);
            }
            return value < radix ? value : -1;
        }
        return -1;
    }

    public static int digit(int ch) {
        return UCharacterProperty.INSTANCE.digit(ch);
    }

    public static int getNumericValue(int ch) {
        return UCharacterProperty.INSTANCE.getNumericValue(ch);
    }

    public static double getUnicodeNumericValue(int ch) {
        return UCharacterProperty.INSTANCE.getUnicodeNumericValue(ch);
    }

    public static boolean isSpace(int ch) {
        return ch <= 32 && (ch == 32 || ch == 9 || ch == 10 || ch == 12 || ch == 13);
    }

    public static int getType(int ch) {
        return UCharacterProperty.INSTANCE.getType(ch);
    }

    public static boolean isDefined(int ch) {
        return UCharacter.getType(ch) != 0;
    }

    public static boolean isDigit(int ch) {
        return UCharacter.getType(ch) == 9;
    }

    public static boolean isISOControl(int ch) {
        return ch >= 0 && ch <= 159 && (ch <= 31 || ch >= 127);
    }

    public static boolean isLetter(int ch) {
        return (1 << UCharacter.getType(ch) & 0x3E) != 0;
    }

    public static boolean isLetterOrDigit(int ch) {
        return (1 << UCharacter.getType(ch) & 0x23E) != 0;
    }

    public static boolean isJavaLetter(int cp) {
        return UCharacter.isJavaIdentifierStart(cp);
    }

    public static boolean isJavaLetterOrDigit(int cp) {
        return UCharacter.isJavaIdentifierPart(cp);
    }

    public static boolean isJavaIdentifierStart(int cp) {
        return Character.isJavaIdentifierStart((char)cp);
    }

    public static boolean isJavaIdentifierPart(int cp) {
        return Character.isJavaIdentifierPart((char)cp);
    }

    public static boolean isLowerCase(int ch) {
        return UCharacter.getType(ch) == 2;
    }

    public static boolean isWhitespace(int ch) {
        return (1 << UCharacter.getType(ch) & 0x7000) != 0 && ch != 160 && ch != 8199 && ch != 8239 || ch >= 9 && ch <= 13 || ch >= 28 && ch <= 31;
    }

    public static boolean isSpaceChar(int ch) {
        return (1 << UCharacter.getType(ch) & 0x7000) != 0;
    }

    public static boolean isTitleCase(int ch) {
        return UCharacter.getType(ch) == 3;
    }

    public static boolean isUnicodeIdentifierPart(int ch) {
        return (1 << UCharacter.getType(ch) & 0x40077E) != 0 || UCharacter.isIdentifierIgnorable(ch);
    }

    public static boolean isUnicodeIdentifierStart(int ch) {
        return (1 << UCharacter.getType(ch) & 0x43E) != 0;
    }

    public static boolean isIdentifierIgnorable(int ch) {
        if (ch <= 159) {
            return !(!UCharacter.isISOControl(ch) || ch >= 9 && ch <= 13 || ch >= 28 && ch <= 31);
        }
        return UCharacter.getType(ch) == 16;
    }

    public static boolean isUpperCase(int ch) {
        return UCharacter.getType(ch) == 1;
    }

    public static int toLowerCase(int ch) {
        return UCaseProps.INSTANCE.tolower(ch);
    }

    public static String toString(int ch) {
        if (ch < 0 || ch > 0x10FFFF) {
            return null;
        }
        if (ch < 65536) {
            return String.valueOf((char)ch);
        }
        StringBuilder result = new StringBuilder();
        result.append(UTF16.getLeadSurrogate(ch));
        result.append(UTF16.getTrailSurrogate(ch));
        return result.toString();
    }

    public static int toTitleCase(int ch) {
        return UCaseProps.INSTANCE.totitle(ch);
    }

    public static int toUpperCase(int ch) {
        return UCaseProps.INSTANCE.toupper(ch);
    }

    public static boolean isSupplementary(int ch) {
        return ch >= 65536 && ch <= 0x10FFFF;
    }

    public static boolean isBMP(int ch) {
        return ch >= 0 && ch <= 65535;
    }

    public static boolean isPrintable(int ch) {
        int cat = UCharacter.getType(ch);
        return cat != 0 && cat != 15 && cat != 16 && cat != 17 && cat != 18 && cat != 0;
    }

    public static boolean isBaseForm(int ch) {
        int cat = UCharacter.getType(ch);
        return cat == 9 || cat == 11 || cat == 10 || cat == 1 || cat == 2 || cat == 3 || cat == 4 || cat == 5 || cat == 6 || cat == 7 || cat == 8;
    }

    public static int getDirection(int ch) {
        return UBiDiProps.INSTANCE.getClass(ch);
    }

    public static boolean isMirrored(int ch) {
        return UBiDiProps.INSTANCE.isMirrored(ch);
    }

    public static int getMirror(int ch) {
        return UBiDiProps.INSTANCE.getMirror(ch);
    }

    public static int getCombiningClass(int ch) {
        return Norm2AllModes.getNFCInstance().decomp.getCombiningClass(ch);
    }

    public static boolean isLegal(int ch) {
        if (ch < 0) {
            return false;
        }
        if (ch < 55296) {
            return true;
        }
        if (ch <= 57343) {
            return false;
        }
        if (UCharacterUtility.isNonCharacter(ch)) {
            return false;
        }
        return ch <= 0x10FFFF;
    }

    public static boolean isLegal(String str) {
        int size = str.length();
        for (int i = 0; i < size; ++i) {
            int codepoint = UTF16.charAt(str, i);
            if (!UCharacter.isLegal(codepoint)) {
                return false;
            }
            if (!UCharacter.isSupplementary(codepoint)) continue;
            ++i;
        }
        return true;
    }

    public static VersionInfo getUnicodeVersion() {
        return UCharacterProperty.INSTANCE.m_unicodeVersion_;
    }

    public static String getName(int ch) {
        return UCharacterName.INSTANCE.getName(ch, 0);
    }

    public static String getName(String s, String separator) {
        int cp;
        if (s.length() == 1) {
            return UCharacter.getName(s.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(s, i);
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(UCharacter.getName(cp));
        }
        return sb.toString();
    }

    public static String getName1_0(int ch) {
        return null;
    }

    public static String getExtendedName(int ch) {
        return UCharacterName.INSTANCE.getName(ch, 2);
    }

    public static String getNameAlias(int ch) {
        return UCharacterName.INSTANCE.getName(ch, 3);
    }

    public static String getISOComment(int ch) {
        return null;
    }

    public static int getCharFromName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(0, name);
    }

    public static int getCharFromName1_0(String name) {
        return -1;
    }

    public static int getCharFromExtendedName(String name) {
        return UCharacterName.INSTANCE.getCharFromName(2, name);
    }

    public static int getCharFromNameAlias(String name) {
        return UCharacterName.INSTANCE.getCharFromName(3, name);
    }

    public static String getPropertyName(int property, int nameChoice) {
        return UPropertyAliases.INSTANCE.getPropertyName(property, nameChoice);
    }

    public static int getPropertyEnum(CharSequence propertyAlias) {
        int propEnum = UPropertyAliases.INSTANCE.getPropertyEnum(propertyAlias);
        if (propEnum == -1) {
            throw new IllegalIcuArgumentException("Invalid name: " + propertyAlias);
        }
        return propEnum;
    }

    public static String getPropertyValueName(int property, int value, int nameChoice) {
        if ((property == 4098 || property == 4112 || property == 4113) && value >= UCharacter.getIntPropertyMinValue(4098) && value <= UCharacter.getIntPropertyMaxValue(4098) && nameChoice >= 0 && nameChoice < 2) {
            try {
                return UPropertyAliases.INSTANCE.getPropertyValueName(property, value, nameChoice);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return UPropertyAliases.INSTANCE.getPropertyValueName(property, value, nameChoice);
    }

    public static int getPropertyValueEnum(int property, CharSequence valueAlias) {
        int propEnum = UPropertyAliases.INSTANCE.getPropertyValueEnum(property, valueAlias);
        if (propEnum == -1) {
            throw new IllegalIcuArgumentException("Invalid name: " + valueAlias);
        }
        return propEnum;
    }

    public static int getCodePoint(char lead, char trail) {
        if (UTF16.isLeadSurrogate(lead) && UTF16.isTrailSurrogate(trail)) {
            return UCharacterProperty.getRawSupplementary(lead, trail);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }

    public static int getCodePoint(char char16) {
        if (UCharacter.isLegal(char16)) {
            return char16;
        }
        throw new IllegalArgumentException("Illegal codepoint");
    }

    public static String toUpperCase(String str) {
        return UCharacter.toUpperCase(ULocale.getDefault(), str);
    }

    public static String toLowerCase(String str) {
        return UCharacter.toLowerCase(ULocale.getDefault(), str);
    }

    public static String toTitleCase(String str, BreakIterator breakiter) {
        return UCharacter.toTitleCase(ULocale.getDefault(), str, breakiter);
    }

    public static String toUpperCase(Locale locale, String str) {
        return UCharacter.toUpperCase(ULocale.forLocale(locale), str);
    }

    public static String toUpperCase(ULocale locale, String str) {
        int c;
        StringContextIterator iter = new StringContextIterator(str);
        StringBuilder result = new StringBuilder(str.length());
        int[] locCache = new int[1];
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        locCache[0] = 0;
        while ((c = iter.nextCaseMapCP()) >= 0) {
            if ((c = UCaseProps.INSTANCE.toFullUpper(c, iter, result, locale, locCache)) < 0) {
                c ^= 0xFFFFFFFF;
            } else if (c <= 31) continue;
            result.appendCodePoint(c);
        }
        return result.toString();
    }

    public static String toLowerCase(Locale locale, String str) {
        return UCharacter.toLowerCase(ULocale.forLocale(locale), str);
    }

    public static String toLowerCase(ULocale locale, String str) {
        int c;
        StringContextIterator iter = new StringContextIterator(str);
        StringBuilder result = new StringBuilder(str.length());
        int[] locCache = new int[1];
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        locCache[0] = 0;
        while ((c = iter.nextCaseMapCP()) >= 0) {
            if ((c = UCaseProps.INSTANCE.toFullLower(c, iter, result, locale, locCache)) < 0) {
                c ^= 0xFFFFFFFF;
            } else if (c <= 31) continue;
            result.appendCodePoint(c);
        }
        return result.toString();
    }

    public static String toTitleCase(Locale locale, String str, BreakIterator breakiter) {
        return UCharacter.toTitleCase(ULocale.forLocale(locale), str, breakiter);
    }

    public static String toTitleCase(ULocale locale, String str, BreakIterator titleIter) {
        return UCharacter.toTitleCase(locale, str, titleIter, 0);
    }

    public static String toTitleCase(ULocale locale, String str, BreakIterator titleIter, int options) {
        StringContextIterator iter = new StringContextIterator(str);
        StringBuilder result = new StringBuilder(str.length());
        int[] locCache = new int[1];
        int srcLength = str.length();
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        locCache[0] = 0;
        if (titleIter == null) {
            titleIter = BreakIterator.getWordInstance(locale);
        }
        titleIter.setText(str);
        boolean isDutch = locale.getLanguage().equals("nl");
        boolean FirstIJ = true;
        int prev = 0;
        boolean isFirstIndex = true;
        while (prev < srcLength) {
            int index;
            if (isFirstIndex) {
                isFirstIndex = false;
                index = titleIter.first();
            } else {
                index = titleIter.next();
            }
            if (index == -1 || index > srcLength) {
                index = srcLength;
            }
            if (prev < index) {
                int titleStart;
                iter.setLimit(index);
                int c = iter.nextCaseMapCP();
                if ((options & 0x200) == 0 && 0 == UCaseProps.INSTANCE.getType(c)) {
                    while ((c = iter.nextCaseMapCP()) >= 0 && 0 == UCaseProps.INSTANCE.getType(c)) {
                    }
                    titleStart = iter.getCPStart();
                    if (prev < titleStart) {
                        result.append(str, prev, titleStart);
                    }
                } else {
                    titleStart = prev;
                }
                if (titleStart < index) {
                    FirstIJ = true;
                    c = UCaseProps.INSTANCE.toFullTitle(c, iter, result, locale, locCache);
                    while (true) {
                        if (c < 0) {
                            result.appendCodePoint(c ^= 0xFFFFFFFF);
                        } else if (c > 31) {
                            result.appendCodePoint(c);
                        }
                        if ((options & 0x100) != 0) {
                            int titleLimit = iter.getCPLimit();
                            if (titleLimit < index) {
                                String appendStr = str.substring(titleLimit, index);
                                if (isDutch && c == 73 && appendStr.startsWith("j")) {
                                    appendStr = "J" + appendStr.substring(1);
                                }
                                result.append(appendStr);
                            }
                            iter.moveToLimit();
                            break;
                        }
                        int nc = iter.nextCaseMapCP();
                        if (nc < 0) break;
                        if (isDutch && (nc == 74 || nc == 106) && c == 73 && FirstIJ) {
                            c = 74;
                            FirstIJ = false;
                            continue;
                        }
                        c = UCaseProps.INSTANCE.toFullLower(nc, iter, result, locale, locCache);
                    }
                }
            }
            prev = index;
        }
        return result.toString();
    }

    public static int foldCase(int ch, boolean defaultmapping) {
        return UCharacter.foldCase(ch, defaultmapping ? 0 : 1);
    }

    public static String foldCase(String str, boolean defaultmapping) {
        return UCharacter.foldCase(str, defaultmapping ? 0 : 1);
    }

    public static int foldCase(int ch, int options) {
        return UCaseProps.INSTANCE.fold(ch, options);
    }

    public static final String foldCase(String str, int options) {
        StringBuilder result = new StringBuilder(str.length());
        int length = str.length();
        int i = 0;
        while (i < length) {
            int c = UTF16.charAt(str, i);
            i += UTF16.getCharCount(c);
            if ((c = UCaseProps.INSTANCE.toFullFolding(c, result, options)) < 0) {
                c ^= 0xFFFFFFFF;
            } else if (c <= 31) continue;
            result.appendCodePoint(c);
        }
        return result.toString();
    }

    public static int getHanNumericValue(int ch) {
        switch (ch) {
            case 12295: 
            case 38646: {
                return 0;
            }
            case 19968: 
            case 22777: {
                return 1;
            }
            case 20108: 
            case 36019: {
                return 2;
            }
            case 19977: 
            case 21443: {
                return 3;
            }
            case 22235: 
            case 32902: {
                return 4;
            }
            case 20116: 
            case 20237: {
                return 5;
            }
            case 20845: 
            case 38520: {
                return 6;
            }
            case 19971: 
            case 26578: {
                return 7;
            }
            case 20843: 
            case 25420: {
                return 8;
            }
            case 20061: 
            case 29590: {
                return 9;
            }
            case 21313: 
            case 25342: {
                return 10;
            }
            case 20336: 
            case 30334: {
                return 100;
            }
            case 20191: 
            case 21315: {
                return 1000;
            }
            case 33356: {
                return 10000;
            }
            case 20740: {
                return 100000000;
            }
        }
        return -1;
    }

    public static RangeValueIterator getTypeIterator() {
        return new UCharacterTypeIterator();
    }

    public static ValueIterator getNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 0);
    }

    public static ValueIterator getName1_0Iterator() {
        return new DummyValueIterator();
    }

    public static ValueIterator getExtendedNameIterator() {
        return new UCharacterNameIterator(UCharacterName.INSTANCE, 2);
    }

    public static VersionInfo getAge(int ch) {
        if (ch < 0 || ch > 0x10FFFF) {
            throw new IllegalArgumentException("Codepoint out of bounds");
        }
        return UCharacterProperty.INSTANCE.getAge(ch);
    }

    public static boolean hasBinaryProperty(int ch, int property) {
        return UCharacterProperty.INSTANCE.hasBinaryProperty(ch, property);
    }

    public static boolean isUAlphabetic(int ch) {
        return UCharacter.hasBinaryProperty(ch, 0);
    }

    public static boolean isULowercase(int ch) {
        return UCharacter.hasBinaryProperty(ch, 22);
    }

    public static boolean isUUppercase(int ch) {
        return UCharacter.hasBinaryProperty(ch, 30);
    }

    public static boolean isUWhiteSpace(int ch) {
        return UCharacter.hasBinaryProperty(ch, 31);
    }

    public static int getIntPropertyValue(int ch, int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyValue(ch, type);
    }

    public static String getStringPropertyValue(int propertyEnum, int codepoint, int nameChoice) {
        if (propertyEnum >= 0 && propertyEnum < 57 || propertyEnum >= 4096 && propertyEnum < 4117) {
            return UCharacter.getPropertyValueName(propertyEnum, UCharacter.getIntPropertyValue(codepoint, propertyEnum), nameChoice);
        }
        if (propertyEnum == 12288) {
            return String.valueOf(UCharacter.getUnicodeNumericValue(codepoint));
        }
        switch (propertyEnum) {
            case 16384: {
                return UCharacter.getAge(codepoint).toString();
            }
            case 16387: {
                return UCharacter.getISOComment(codepoint);
            }
            case 16385: {
                return UTF16.valueOf(UCharacter.getMirror(codepoint));
            }
            case 16386: {
                return UCharacter.foldCase(UTF16.valueOf(codepoint), true);
            }
            case 16388: {
                return UCharacter.toLowerCase(UTF16.valueOf(codepoint));
            }
            case 16389: {
                return UCharacter.getName(codepoint);
            }
            case 16390: {
                return UTF16.valueOf(UCharacter.foldCase(codepoint, true));
            }
            case 16391: {
                return UTF16.valueOf(UCharacter.toLowerCase(codepoint));
            }
            case 16392: {
                return UTF16.valueOf(UCharacter.toTitleCase(codepoint));
            }
            case 16393: {
                return UTF16.valueOf(UCharacter.toUpperCase(codepoint));
            }
            case 16394: {
                return UCharacter.toTitleCase(UTF16.valueOf(codepoint), null);
            }
            case 16395: {
                return UCharacter.getName1_0(codepoint);
            }
            case 16396: {
                return UCharacter.toUpperCase(UTF16.valueOf(codepoint));
            }
        }
        throw new IllegalArgumentException("Illegal Property Enum");
    }

    public static int getIntPropertyMinValue(int type) {
        return 0;
    }

    public static int getIntPropertyMaxValue(int type) {
        return UCharacterProperty.INSTANCE.getIntPropertyMaxValue(type);
    }

    public static char forDigit(int digit, int radix) {
        return Character.forDigit(digit, radix);
    }

    public static final boolean isValidCodePoint(int cp) {
        return cp >= 0 && cp <= 0x10FFFF;
    }

    public static final boolean isSupplementaryCodePoint(int cp) {
        return cp >= 65536 && cp <= 0x10FFFF;
    }

    public static boolean isHighSurrogate(char ch) {
        return ch >= '\ud800' && ch <= '\udbff';
    }

    public static boolean isLowSurrogate(char ch) {
        return ch >= '\udc00' && ch <= '\udfff';
    }

    public static final boolean isSurrogatePair(char high, char low) {
        return UCharacter.isHighSurrogate(high) && UCharacter.isLowSurrogate(low);
    }

    public static int charCount(int cp) {
        return UTF16.getCharCount(cp);
    }

    public static final int toCodePoint(char high, char low) {
        return UCharacterProperty.getRawSupplementary(high, low);
    }

    public static final int codePointAt(CharSequence seq, int index) {
        char c2;
        char c1;
        if (UCharacter.isHighSurrogate(c1 = seq.charAt(index++)) && index < seq.length() && UCharacter.isLowSurrogate(c2 = seq.charAt(index))) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c1;
    }

    public static final int codePointAt(char[] text, int index) {
        char c2;
        char c1;
        if (UCharacter.isHighSurrogate(c1 = text[index++]) && index < text.length && UCharacter.isLowSurrogate(c2 = text[index])) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c1;
    }

    public static final int codePointAt(char[] text, int index, int limit) {
        char c2;
        char c1;
        if (index >= limit || limit > text.length) {
            throw new IndexOutOfBoundsException();
        }
        if (UCharacter.isHighSurrogate(c1 = text[index++]) && index < limit && UCharacter.isLowSurrogate(c2 = text[index])) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c1;
    }

    public static final int codePointBefore(CharSequence seq, int index) {
        char c1;
        char c2;
        if (UCharacter.isLowSurrogate(c2 = seq.charAt(--index)) && index > 0 && UCharacter.isHighSurrogate(c1 = seq.charAt(--index))) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c2;
    }

    public static final int codePointBefore(char[] text, int index) {
        char c1;
        char c2;
        if (UCharacter.isLowSurrogate(c2 = text[--index]) && index > 0 && UCharacter.isHighSurrogate(c1 = text[--index])) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c2;
    }

    public static final int codePointBefore(char[] text, int index, int limit) {
        char c1;
        char c2;
        if (index <= limit || limit < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (UCharacter.isLowSurrogate(c2 = text[--index]) && index > limit && UCharacter.isHighSurrogate(c1 = text[--index])) {
            return UCharacter.toCodePoint(c1, c2);
        }
        return c2;
    }

    public static final int toChars(int cp, char[] dst, int dstIndex) {
        if (cp >= 0) {
            if (cp < 65536) {
                dst[dstIndex] = (char)cp;
                return 1;
            }
            if (cp <= 0x10FFFF) {
                dst[dstIndex] = UTF16.getLeadSurrogate(cp);
                dst[dstIndex + 1] = UTF16.getTrailSurrogate(cp);
                return 2;
            }
        }
        throw new IllegalArgumentException();
    }

    public static final char[] toChars(int cp) {
        if (cp >= 0) {
            if (cp < 65536) {
                return new char[]{(char)cp};
            }
            if (cp <= 0x10FFFF) {
                return new char[]{UTF16.getLeadSurrogate(cp), UTF16.getTrailSurrogate(cp)};
            }
        }
        throw new IllegalArgumentException();
    }

    public static byte getDirectionality(int cp) {
        return (byte)UCharacter.getDirection(cp);
    }

    public static int codePointCount(CharSequence text, int start, int limit) {
        if (start < 0 || limit < start || limit > text.length()) {
            throw new IndexOutOfBoundsException("start (" + start + ") or limit (" + limit + ") invalid or out of range 0, " + text.length());
        }
        int len = limit - start;
        block0: while (limit > start) {
            char ch = text.charAt(--limit);
            while (ch >= '\udc00' && ch <= '\udfff' && limit > start) {
                if ((ch = text.charAt(--limit)) < '\ud800' || ch > '\udbff') continue;
                --len;
                continue block0;
            }
        }
        return len;
    }

    public static int codePointCount(char[] text, int start, int limit) {
        if (start < 0 || limit < start || limit > text.length) {
            throw new IndexOutOfBoundsException("start (" + start + ") or limit (" + limit + ") invalid or out of range 0, " + text.length);
        }
        int len = limit - start;
        block0: while (limit > start) {
            char ch = text[--limit];
            while (ch >= '\udc00' && ch <= '\udfff' && limit > start) {
                if ((ch = text[--limit]) < '\ud800' || ch > '\udbff') continue;
                --len;
                continue block0;
            }
        }
        return len;
    }

    public static int offsetByCodePoints(CharSequence text, int index, int codePointOffset) {
        if (index < 0 || index > text.length()) {
            throw new IndexOutOfBoundsException("index ( " + index + ") out of range 0, " + text.length());
        }
        if (codePointOffset < 0) {
            while (++codePointOffset <= 0) {
                char ch = text.charAt(--index);
                while (ch >= '\udc00' && ch <= '\udfff' && index > 0) {
                    if ((ch = text.charAt(--index)) >= '\ud800' && ch <= '\udbff' || ++codePointOffset <= 0) continue;
                    return index + 1;
                }
            }
        } else {
            int limit = text.length();
            while (--codePointOffset >= 0) {
                char ch = text.charAt(index++);
                while (ch >= '\ud800' && ch <= '\udbff' && index < limit) {
                    if ((ch = text.charAt(index++)) >= '\udc00' && ch <= '\udfff' || --codePointOffset >= 0) continue;
                    return index - 1;
                }
            }
        }
        return index;
    }

    public static int offsetByCodePoints(char[] text, int start, int count, int index, int codePointOffset) {
        int limit = start + count;
        if (start < 0 || limit < start || limit > text.length || index < start || index > limit) {
            throw new IndexOutOfBoundsException("index ( " + index + ") out of range " + start + ", " + limit + " in array 0, " + text.length);
        }
        if (codePointOffset < 0) {
            while (++codePointOffset <= 0) {
                char ch = text[--index];
                if (index < start) {
                    throw new IndexOutOfBoundsException("index ( " + index + ") < start (" + start + ")");
                }
                while (ch >= '\udc00' && ch <= '\udfff' && index > start) {
                    if ((ch = text[--index]) >= '\ud800' && ch <= '\udbff' || ++codePointOffset <= 0) continue;
                    return index + 1;
                }
            }
        } else {
            while (--codePointOffset >= 0) {
                char ch = text[index++];
                if (index > limit) {
                    throw new IndexOutOfBoundsException("index ( " + index + ") > limit (" + limit + ")");
                }
                while (ch >= '\ud800' && ch <= '\udbff' && index < limit) {
                    if ((ch = text[index++]) >= '\udc00' && ch <= '\udfff' || --codePointOffset >= 0) continue;
                    return index - 1;
                }
            }
        }
        return index;
    }

    private UCharacter() {
    }

    private static final class DummyValueIterator
    implements ValueIterator {
        private DummyValueIterator() {
        }

        public boolean next(ValueIterator.Element element) {
            return false;
        }

        public void reset() {
        }

        public void setRange(int start, int limit) {
        }
    }

    private static final class UCharacterTypeIterator
    implements RangeValueIterator {
        private Iterator<Trie2.Range> trieIterator;
        private Trie2.Range range;
        private static final MaskType MASK_TYPE = new MaskType();

        UCharacterTypeIterator() {
            this.reset();
        }

        public boolean next(RangeValueIterator.Element element) {
            if (this.trieIterator.hasNext()) {
                this.range = this.trieIterator.next();
                if (!this.range.leadSurrogate) {
                    element.start = this.range.startCodePoint;
                    element.limit = this.range.endCodePoint + 1;
                    element.value = this.range.value;
                    return true;
                }
            }
            return false;
        }

        public void reset() {
            this.trieIterator = UCharacterProperty.INSTANCE.m_trie_.iterator(MASK_TYPE);
        }

        private static final class MaskType
        implements Trie2.ValueMapper {
            private MaskType() {
            }

            public int map(int value) {
                return value & 0x1F;
            }
        }
    }

    private static class StringContextIterator
    implements UCaseProps.ContextIterator {
        protected String s;
        protected int index;
        protected int limit;
        protected int cpStart;
        protected int cpLimit;
        protected int dir;

        StringContextIterator(String s) {
            this.s = s;
            this.limit = s.length();
            this.index = 0;
            this.cpLimit = 0;
            this.cpStart = 0;
            this.dir = 0;
        }

        public void setLimit(int lim) {
            this.limit = 0 <= lim && lim <= this.s.length() ? lim : this.s.length();
        }

        public void moveToLimit() {
            this.cpStart = this.cpLimit = this.limit;
        }

        public int nextCaseMapCP() {
            this.cpStart = this.cpLimit;
            if (this.cpLimit < this.limit) {
                char c2;
                int c;
                if ((55296 <= (c = this.s.charAt(this.cpLimit++)) || c <= 57343) && c <= 56319 && this.cpLimit < this.limit && '\udc00' <= (c2 = this.s.charAt(this.cpLimit)) && c2 <= '\udfff') {
                    ++this.cpLimit;
                    c = UCharacterProperty.getRawSupplementary((char)c, c2);
                }
                return c;
            }
            return -1;
        }

        public int getCPStart() {
            return this.cpStart;
        }

        public int getCPLimit() {
            return this.cpLimit;
        }

        public void reset(int direction) {
            if (direction > 0) {
                this.dir = 1;
                this.index = this.cpLimit;
            } else if (direction < 0) {
                this.dir = -1;
                this.index = this.cpStart;
            } else {
                this.dir = 0;
                this.index = 0;
            }
        }

        public int next() {
            if (this.dir > 0 && this.index < this.s.length()) {
                int c = UTF16.charAt(this.s, this.index);
                this.index += UTF16.getCharCount(c);
                return c;
            }
            if (this.dir < 0 && this.index > 0) {
                int c = UTF16.charAt(this.s, this.index - 1);
                this.index -= UTF16.getCharCount(c);
                return c;
            }
            return -1;
        }
    }

    public static interface HangulSyllableType {
        public static final int NOT_APPLICABLE = 0;
        public static final int LEADING_JAMO = 1;
        public static final int VOWEL_JAMO = 2;
        public static final int TRAILING_JAMO = 3;
        public static final int LV_SYLLABLE = 4;
        public static final int LVT_SYLLABLE = 5;
        public static final int COUNT = 6;
    }

    public static interface NumericType {
        public static final int NONE = 0;
        public static final int DECIMAL = 1;
        public static final int DIGIT = 2;
        public static final int NUMERIC = 3;
        public static final int COUNT = 4;
    }

    public static interface LineBreak {
        public static final int UNKNOWN = 0;
        public static final int AMBIGUOUS = 1;
        public static final int ALPHABETIC = 2;
        public static final int BREAK_BOTH = 3;
        public static final int BREAK_AFTER = 4;
        public static final int BREAK_BEFORE = 5;
        public static final int MANDATORY_BREAK = 6;
        public static final int CONTINGENT_BREAK = 7;
        public static final int CLOSE_PUNCTUATION = 8;
        public static final int COMBINING_MARK = 9;
        public static final int CARRIAGE_RETURN = 10;
        public static final int EXCLAMATION = 11;
        public static final int GLUE = 12;
        public static final int HYPHEN = 13;
        public static final int IDEOGRAPHIC = 14;
        public static final int INSEPERABLE = 15;
        public static final int INSEPARABLE = 15;
        public static final int INFIX_NUMERIC = 16;
        public static final int LINE_FEED = 17;
        public static final int NONSTARTER = 18;
        public static final int NUMERIC = 19;
        public static final int OPEN_PUNCTUATION = 20;
        public static final int POSTFIX_NUMERIC = 21;
        public static final int PREFIX_NUMERIC = 22;
        public static final int QUOTATION = 23;
        public static final int COMPLEX_CONTEXT = 24;
        public static final int SURROGATE = 25;
        public static final int SPACE = 26;
        public static final int BREAK_SYMBOLS = 27;
        public static final int ZWSPACE = 28;
        public static final int NEXT_LINE = 29;
        public static final int WORD_JOINER = 30;
        public static final int H2 = 31;
        public static final int H3 = 32;
        public static final int JL = 33;
        public static final int JT = 34;
        public static final int JV = 35;
        public static final int CLOSE_PARENTHESIS = 36;
        public static final int CONDITIONAL_JAPANESE_STARTER = 37;
        public static final int HEBREW_LETTER = 38;
        public static final int REGIONAL_INDICATOR = 39;
        public static final int COUNT = 40;
    }

    public static interface SentenceBreak {
        public static final int OTHER = 0;
        public static final int ATERM = 1;
        public static final int CLOSE = 2;
        public static final int FORMAT = 3;
        public static final int LOWER = 4;
        public static final int NUMERIC = 5;
        public static final int OLETTER = 6;
        public static final int SEP = 7;
        public static final int SP = 8;
        public static final int STERM = 9;
        public static final int UPPER = 10;
        public static final int CR = 11;
        public static final int EXTEND = 12;
        public static final int LF = 13;
        public static final int SCONTINUE = 14;
        public static final int COUNT = 15;
    }

    public static interface WordBreak {
        public static final int OTHER = 0;
        public static final int ALETTER = 1;
        public static final int FORMAT = 2;
        public static final int KATAKANA = 3;
        public static final int MIDLETTER = 4;
        public static final int MIDNUM = 5;
        public static final int NUMERIC = 6;
        public static final int EXTENDNUMLET = 7;
        public static final int CR = 8;
        public static final int EXTEND = 9;
        public static final int LF = 10;
        public static final int MIDNUMLET = 11;
        public static final int NEWLINE = 12;
        public static final int REGIONAL_INDICATOR = 13;
        public static final int COUNT = 14;
    }

    public static interface GraphemeClusterBreak {
        public static final int OTHER = 0;
        public static final int CONTROL = 1;
        public static final int CR = 2;
        public static final int EXTEND = 3;
        public static final int L = 4;
        public static final int LF = 5;
        public static final int LV = 6;
        public static final int LVT = 7;
        public static final int T = 8;
        public static final int V = 9;
        public static final int SPACING_MARK = 10;
        public static final int PREPEND = 11;
        public static final int REGIONAL_INDICATOR = 12;
        public static final int COUNT = 13;
    }

    public static interface JoiningGroup {
        public static final int NO_JOINING_GROUP = 0;
        public static final int AIN = 1;
        public static final int ALAPH = 2;
        public static final int ALEF = 3;
        public static final int BEH = 4;
        public static final int BETH = 5;
        public static final int DAL = 6;
        public static final int DALATH_RISH = 7;
        public static final int E = 8;
        public static final int FEH = 9;
        public static final int FINAL_SEMKATH = 10;
        public static final int GAF = 11;
        public static final int GAMAL = 12;
        public static final int HAH = 13;
        public static final int TEH_MARBUTA_GOAL = 14;
        public static final int HAMZA_ON_HEH_GOAL = 14;
        public static final int HE = 15;
        public static final int HEH = 16;
        public static final int HEH_GOAL = 17;
        public static final int HETH = 18;
        public static final int KAF = 19;
        public static final int KAPH = 20;
        public static final int KNOTTED_HEH = 21;
        public static final int LAM = 22;
        public static final int LAMADH = 23;
        public static final int MEEM = 24;
        public static final int MIM = 25;
        public static final int NOON = 26;
        public static final int NUN = 27;
        public static final int PE = 28;
        public static final int QAF = 29;
        public static final int QAPH = 30;
        public static final int REH = 31;
        public static final int REVERSED_PE = 32;
        public static final int SAD = 33;
        public static final int SADHE = 34;
        public static final int SEEN = 35;
        public static final int SEMKATH = 36;
        public static final int SHIN = 37;
        public static final int SWASH_KAF = 38;
        public static final int SYRIAC_WAW = 39;
        public static final int TAH = 40;
        public static final int TAW = 41;
        public static final int TEH_MARBUTA = 42;
        public static final int TETH = 43;
        public static final int WAW = 44;
        public static final int YEH = 45;
        public static final int YEH_BARREE = 46;
        public static final int YEH_WITH_TAIL = 47;
        public static final int YUDH = 48;
        public static final int YUDH_HE = 49;
        public static final int ZAIN = 50;
        public static final int FE = 51;
        public static final int KHAPH = 52;
        public static final int ZHAIN = 53;
        public static final int BURUSHASKI_YEH_BARREE = 54;
        public static final int FARSI_YEH = 55;
        public static final int NYA = 56;
        public static final int ROHINGYA_YEH = 57;
        public static final int COUNT = 58;
    }

    public static interface JoiningType {
        public static final int NON_JOINING = 0;
        public static final int JOIN_CAUSING = 1;
        public static final int DUAL_JOINING = 2;
        public static final int LEFT_JOINING = 3;
        public static final int RIGHT_JOINING = 4;
        public static final int TRANSPARENT = 5;
        public static final int COUNT = 6;
    }

    public static interface DecompositionType {
        public static final int NONE = 0;
        public static final int CANONICAL = 1;
        public static final int COMPAT = 2;
        public static final int CIRCLE = 3;
        public static final int FINAL = 4;
        public static final int FONT = 5;
        public static final int FRACTION = 6;
        public static final int INITIAL = 7;
        public static final int ISOLATED = 8;
        public static final int MEDIAL = 9;
        public static final int NARROW = 10;
        public static final int NOBREAK = 11;
        public static final int SMALL = 12;
        public static final int SQUARE = 13;
        public static final int SUB = 14;
        public static final int SUPER = 15;
        public static final int VERTICAL = 16;
        public static final int WIDE = 17;
        public static final int COUNT = 18;
    }

    public static interface EastAsianWidth {
        public static final int NEUTRAL = 0;
        public static final int AMBIGUOUS = 1;
        public static final int HALFWIDTH = 2;
        public static final int FULLWIDTH = 3;
        public static final int NARROW = 4;
        public static final int WIDE = 5;
        public static final int COUNT = 6;
    }

    public static final class UnicodeBlock
    extends Character.Subset {
        public static final int INVALID_CODE_ID = -1;
        public static final int BASIC_LATIN_ID = 1;
        public static final int LATIN_1_SUPPLEMENT_ID = 2;
        public static final int LATIN_EXTENDED_A_ID = 3;
        public static final int LATIN_EXTENDED_B_ID = 4;
        public static final int IPA_EXTENSIONS_ID = 5;
        public static final int SPACING_MODIFIER_LETTERS_ID = 6;
        public static final int COMBINING_DIACRITICAL_MARKS_ID = 7;
        public static final int GREEK_ID = 8;
        public static final int CYRILLIC_ID = 9;
        public static final int ARMENIAN_ID = 10;
        public static final int HEBREW_ID = 11;
        public static final int ARABIC_ID = 12;
        public static final int SYRIAC_ID = 13;
        public static final int THAANA_ID = 14;
        public static final int DEVANAGARI_ID = 15;
        public static final int BENGALI_ID = 16;
        public static final int GURMUKHI_ID = 17;
        public static final int GUJARATI_ID = 18;
        public static final int ORIYA_ID = 19;
        public static final int TAMIL_ID = 20;
        public static final int TELUGU_ID = 21;
        public static final int KANNADA_ID = 22;
        public static final int MALAYALAM_ID = 23;
        public static final int SINHALA_ID = 24;
        public static final int THAI_ID = 25;
        public static final int LAO_ID = 26;
        public static final int TIBETAN_ID = 27;
        public static final int MYANMAR_ID = 28;
        public static final int GEORGIAN_ID = 29;
        public static final int HANGUL_JAMO_ID = 30;
        public static final int ETHIOPIC_ID = 31;
        public static final int CHEROKEE_ID = 32;
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_ID = 33;
        public static final int OGHAM_ID = 34;
        public static final int RUNIC_ID = 35;
        public static final int KHMER_ID = 36;
        public static final int MONGOLIAN_ID = 37;
        public static final int LATIN_EXTENDED_ADDITIONAL_ID = 38;
        public static final int GREEK_EXTENDED_ID = 39;
        public static final int GENERAL_PUNCTUATION_ID = 40;
        public static final int SUPERSCRIPTS_AND_SUBSCRIPTS_ID = 41;
        public static final int CURRENCY_SYMBOLS_ID = 42;
        public static final int COMBINING_MARKS_FOR_SYMBOLS_ID = 43;
        public static final int LETTERLIKE_SYMBOLS_ID = 44;
        public static final int NUMBER_FORMS_ID = 45;
        public static final int ARROWS_ID = 46;
        public static final int MATHEMATICAL_OPERATORS_ID = 47;
        public static final int MISCELLANEOUS_TECHNICAL_ID = 48;
        public static final int CONTROL_PICTURES_ID = 49;
        public static final int OPTICAL_CHARACTER_RECOGNITION_ID = 50;
        public static final int ENCLOSED_ALPHANUMERICS_ID = 51;
        public static final int BOX_DRAWING_ID = 52;
        public static final int BLOCK_ELEMENTS_ID = 53;
        public static final int GEOMETRIC_SHAPES_ID = 54;
        public static final int MISCELLANEOUS_SYMBOLS_ID = 55;
        public static final int DINGBATS_ID = 56;
        public static final int BRAILLE_PATTERNS_ID = 57;
        public static final int CJK_RADICALS_SUPPLEMENT_ID = 58;
        public static final int KANGXI_RADICALS_ID = 59;
        public static final int IDEOGRAPHIC_DESCRIPTION_CHARACTERS_ID = 60;
        public static final int CJK_SYMBOLS_AND_PUNCTUATION_ID = 61;
        public static final int HIRAGANA_ID = 62;
        public static final int KATAKANA_ID = 63;
        public static final int BOPOMOFO_ID = 64;
        public static final int HANGUL_COMPATIBILITY_JAMO_ID = 65;
        public static final int KANBUN_ID = 66;
        public static final int BOPOMOFO_EXTENDED_ID = 67;
        public static final int ENCLOSED_CJK_LETTERS_AND_MONTHS_ID = 68;
        public static final int CJK_COMPATIBILITY_ID = 69;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A_ID = 70;
        public static final int CJK_UNIFIED_IDEOGRAPHS_ID = 71;
        public static final int YI_SYLLABLES_ID = 72;
        public static final int YI_RADICALS_ID = 73;
        public static final int HANGUL_SYLLABLES_ID = 74;
        public static final int HIGH_SURROGATES_ID = 75;
        public static final int HIGH_PRIVATE_USE_SURROGATES_ID = 76;
        public static final int LOW_SURROGATES_ID = 77;
        public static final int PRIVATE_USE_AREA_ID = 78;
        public static final int PRIVATE_USE_ID = 78;
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_ID = 79;
        public static final int ALPHABETIC_PRESENTATION_FORMS_ID = 80;
        public static final int ARABIC_PRESENTATION_FORMS_A_ID = 81;
        public static final int COMBINING_HALF_MARKS_ID = 82;
        public static final int CJK_COMPATIBILITY_FORMS_ID = 83;
        public static final int SMALL_FORM_VARIANTS_ID = 84;
        public static final int ARABIC_PRESENTATION_FORMS_B_ID = 85;
        public static final int SPECIALS_ID = 86;
        public static final int HALFWIDTH_AND_FULLWIDTH_FORMS_ID = 87;
        public static final int OLD_ITALIC_ID = 88;
        public static final int GOTHIC_ID = 89;
        public static final int DESERET_ID = 90;
        public static final int BYZANTINE_MUSICAL_SYMBOLS_ID = 91;
        public static final int MUSICAL_SYMBOLS_ID = 92;
        public static final int MATHEMATICAL_ALPHANUMERIC_SYMBOLS_ID = 93;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B_ID = 94;
        public static final int CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT_ID = 95;
        public static final int TAGS_ID = 96;
        public static final int CYRILLIC_SUPPLEMENTARY_ID = 97;
        public static final int CYRILLIC_SUPPLEMENT_ID = 97;
        public static final int TAGALOG_ID = 98;
        public static final int HANUNOO_ID = 99;
        public static final int BUHID_ID = 100;
        public static final int TAGBANWA_ID = 101;
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A_ID = 102;
        public static final int SUPPLEMENTAL_ARROWS_A_ID = 103;
        public static final int SUPPLEMENTAL_ARROWS_B_ID = 104;
        public static final int MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B_ID = 105;
        public static final int SUPPLEMENTAL_MATHEMATICAL_OPERATORS_ID = 106;
        public static final int KATAKANA_PHONETIC_EXTENSIONS_ID = 107;
        public static final int VARIATION_SELECTORS_ID = 108;
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_A_ID = 109;
        public static final int SUPPLEMENTARY_PRIVATE_USE_AREA_B_ID = 110;
        public static final int LIMBU_ID = 111;
        public static final int TAI_LE_ID = 112;
        public static final int KHMER_SYMBOLS_ID = 113;
        public static final int PHONETIC_EXTENSIONS_ID = 114;
        public static final int MISCELLANEOUS_SYMBOLS_AND_ARROWS_ID = 115;
        public static final int YIJING_HEXAGRAM_SYMBOLS_ID = 116;
        public static final int LINEAR_B_SYLLABARY_ID = 117;
        public static final int LINEAR_B_IDEOGRAMS_ID = 118;
        public static final int AEGEAN_NUMBERS_ID = 119;
        public static final int UGARITIC_ID = 120;
        public static final int SHAVIAN_ID = 121;
        public static final int OSMANYA_ID = 122;
        public static final int CYPRIOT_SYLLABARY_ID = 123;
        public static final int TAI_XUAN_JING_SYMBOLS_ID = 124;
        public static final int VARIATION_SELECTORS_SUPPLEMENT_ID = 125;
        public static final int ANCIENT_GREEK_MUSICAL_NOTATION_ID = 126;
        public static final int ANCIENT_GREEK_NUMBERS_ID = 127;
        public static final int ARABIC_SUPPLEMENT_ID = 128;
        public static final int BUGINESE_ID = 129;
        public static final int CJK_STROKES_ID = 130;
        public static final int COMBINING_DIACRITICAL_MARKS_SUPPLEMENT_ID = 131;
        public static final int COPTIC_ID = 132;
        public static final int ETHIOPIC_EXTENDED_ID = 133;
        public static final int ETHIOPIC_SUPPLEMENT_ID = 134;
        public static final int GEORGIAN_SUPPLEMENT_ID = 135;
        public static final int GLAGOLITIC_ID = 136;
        public static final int KHAROSHTHI_ID = 137;
        public static final int MODIFIER_TONE_LETTERS_ID = 138;
        public static final int NEW_TAI_LUE_ID = 139;
        public static final int OLD_PERSIAN_ID = 140;
        public static final int PHONETIC_EXTENSIONS_SUPPLEMENT_ID = 141;
        public static final int SUPPLEMENTAL_PUNCTUATION_ID = 142;
        public static final int SYLOTI_NAGRI_ID = 143;
        public static final int TIFINAGH_ID = 144;
        public static final int VERTICAL_FORMS_ID = 145;
        public static final int NKO_ID = 146;
        public static final int BALINESE_ID = 147;
        public static final int LATIN_EXTENDED_C_ID = 148;
        public static final int LATIN_EXTENDED_D_ID = 149;
        public static final int PHAGS_PA_ID = 150;
        public static final int PHOENICIAN_ID = 151;
        public static final int CUNEIFORM_ID = 152;
        public static final int CUNEIFORM_NUMBERS_AND_PUNCTUATION_ID = 153;
        public static final int COUNTING_ROD_NUMERALS_ID = 154;
        public static final int SUNDANESE_ID = 155;
        public static final int LEPCHA_ID = 156;
        public static final int OL_CHIKI_ID = 157;
        public static final int CYRILLIC_EXTENDED_A_ID = 158;
        public static final int VAI_ID = 159;
        public static final int CYRILLIC_EXTENDED_B_ID = 160;
        public static final int SAURASHTRA_ID = 161;
        public static final int KAYAH_LI_ID = 162;
        public static final int REJANG_ID = 163;
        public static final int CHAM_ID = 164;
        public static final int ANCIENT_SYMBOLS_ID = 165;
        public static final int PHAISTOS_DISC_ID = 166;
        public static final int LYCIAN_ID = 167;
        public static final int CARIAN_ID = 168;
        public static final int LYDIAN_ID = 169;
        public static final int MAHJONG_TILES_ID = 170;
        public static final int DOMINO_TILES_ID = 171;
        public static final int SAMARITAN_ID = 172;
        public static final int UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_ID = 173;
        public static final int TAI_THAM_ID = 174;
        public static final int VEDIC_EXTENSIONS_ID = 175;
        public static final int LISU_ID = 176;
        public static final int BAMUM_ID = 177;
        public static final int COMMON_INDIC_NUMBER_FORMS_ID = 178;
        public static final int DEVANAGARI_EXTENDED_ID = 179;
        public static final int HANGUL_JAMO_EXTENDED_A_ID = 180;
        public static final int JAVANESE_ID = 181;
        public static final int MYANMAR_EXTENDED_A_ID = 182;
        public static final int TAI_VIET_ID = 183;
        public static final int MEETEI_MAYEK_ID = 184;
        public static final int HANGUL_JAMO_EXTENDED_B_ID = 185;
        public static final int IMPERIAL_ARAMAIC_ID = 186;
        public static final int OLD_SOUTH_ARABIAN_ID = 187;
        public static final int AVESTAN_ID = 188;
        public static final int INSCRIPTIONAL_PARTHIAN_ID = 189;
        public static final int INSCRIPTIONAL_PAHLAVI_ID = 190;
        public static final int OLD_TURKIC_ID = 191;
        public static final int RUMI_NUMERAL_SYMBOLS_ID = 192;
        public static final int KAITHI_ID = 193;
        public static final int EGYPTIAN_HIEROGLYPHS_ID = 194;
        public static final int ENCLOSED_ALPHANUMERIC_SUPPLEMENT_ID = 195;
        public static final int ENCLOSED_IDEOGRAPHIC_SUPPLEMENT_ID = 196;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C_ID = 197;
        public static final int MANDAIC_ID = 198;
        public static final int BATAK_ID = 199;
        public static final int ETHIOPIC_EXTENDED_A_ID = 200;
        public static final int BRAHMI_ID = 201;
        public static final int BAMUM_SUPPLEMENT_ID = 202;
        public static final int KANA_SUPPLEMENT_ID = 203;
        public static final int PLAYING_CARDS_ID = 204;
        public static final int MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS_ID = 205;
        public static final int EMOTICONS_ID = 206;
        public static final int TRANSPORT_AND_MAP_SYMBOLS_ID = 207;
        public static final int ALCHEMICAL_SYMBOLS_ID = 208;
        public static final int CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D_ID = 209;
        public static final int ARABIC_EXTENDED_A_ID = 210;
        public static final int ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS_ID = 211;
        public static final int CHAKMA_ID = 212;
        public static final int MEETEI_MAYEK_EXTENSIONS_ID = 213;
        public static final int MEROITIC_CURSIVE_ID = 214;
        public static final int MEROITIC_HIEROGLYPHS_ID = 215;
        public static final int MIAO_ID = 216;
        public static final int SHARADA_ID = 217;
        public static final int SORA_SOMPENG_ID = 218;
        public static final int SUNDANESE_SUPPLEMENT_ID = 219;
        public static final int TAKRI_ID = 220;
        public static final int COUNT = 221;
        private static final UnicodeBlock[] BLOCKS_ = new UnicodeBlock[221];
        public static final UnicodeBlock NO_BLOCK = new UnicodeBlock("NO_BLOCK", 0);
        public static final UnicodeBlock BASIC_LATIN = new UnicodeBlock("BASIC_LATIN", 1);
        public static final UnicodeBlock LATIN_1_SUPPLEMENT = new UnicodeBlock("LATIN_1_SUPPLEMENT", 2);
        public static final UnicodeBlock LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", 3);
        public static final UnicodeBlock LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", 4);
        public static final UnicodeBlock IPA_EXTENSIONS = new UnicodeBlock("IPA_EXTENSIONS", 5);
        public static final UnicodeBlock SPACING_MODIFIER_LETTERS = new UnicodeBlock("SPACING_MODIFIER_LETTERS", 6);
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 7);
        public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", 8);
        public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC", 9);
        public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN", 10);
        public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW", 11);
        public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC", 12);
        public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC", 13);
        public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA", 14);
        public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI", 15);
        public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI", 16);
        public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI", 17);
        public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI", 18);
        public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA", 19);
        public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL", 20);
        public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU", 21);
        public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA", 22);
        public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM", 23);
        public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA", 24);
        public static final UnicodeBlock THAI = new UnicodeBlock("THAI", 25);
        public static final UnicodeBlock LAO = new UnicodeBlock("LAO", 26);
        public static final UnicodeBlock TIBETAN = new UnicodeBlock("TIBETAN", 27);
        public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR", 28);
        public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN", 29);
        public static final UnicodeBlock HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", 30);
        public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC", 31);
        public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE", 32);
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 33);
        public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 34);
        public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC", 35);
        public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER", 36);
        public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN", 37);
        public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 38);
        public static final UnicodeBlock GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", 39);
        public static final UnicodeBlock GENERAL_PUNCTUATION = new UnicodeBlock("GENERAL_PUNCTUATION", 40);
        public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 41);
        public static final UnicodeBlock CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", 42);
        public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 43);
        public static final UnicodeBlock LETTERLIKE_SYMBOLS = new UnicodeBlock("LETTERLIKE_SYMBOLS", 44);
        public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", 45);
        public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS", 46);
        public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS", 47);
        public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 48);
        public static final UnicodeBlock CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", 49);
        public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 50);
        public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 51);
        public static final UnicodeBlock BOX_DRAWING = new UnicodeBlock("BOX_DRAWING", 52);
        public static final UnicodeBlock BLOCK_ELEMENTS = new UnicodeBlock("BLOCK_ELEMENTS", 53);
        public static final UnicodeBlock GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", 54);
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 55);
        public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS", 56);
        public static final UnicodeBlock BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", 57);
        public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 58);
        public static final UnicodeBlock KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", 59);
        public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 60);
        public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 61);
        public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA", 62);
        public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA", 63);
        public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO", 64);
        public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 65);
        public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN", 66);
        public static final UnicodeBlock BOPOMOFO_EXTENDED = new UnicodeBlock("BOPOMOFO_EXTENDED", 67);
        public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 68);
        public static final UnicodeBlock CJK_COMPATIBILITY = new UnicodeBlock("CJK_COMPATIBILITY", 69);
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 70);
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 71);
        public static final UnicodeBlock YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", 72);
        public static final UnicodeBlock YI_RADICALS = new UnicodeBlock("YI_RADICALS", 73);
        public static final UnicodeBlock HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", 74);
        public static final UnicodeBlock HIGH_SURROGATES = new UnicodeBlock("HIGH_SURROGATES", 75);
        public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES", 76);
        public static final UnicodeBlock LOW_SURROGATES = new UnicodeBlock("LOW_SURROGATES", 77);
        public static final UnicodeBlock PRIVATE_USE_AREA;
        public static final UnicodeBlock PRIVATE_USE;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS;
        public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A;
        public static final UnicodeBlock COMBINING_HALF_MARKS;
        public static final UnicodeBlock CJK_COMPATIBILITY_FORMS;
        public static final UnicodeBlock SMALL_FORM_VARIANTS;
        public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B;
        public static final UnicodeBlock SPECIALS;
        public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS;
        public static final UnicodeBlock OLD_ITALIC;
        public static final UnicodeBlock GOTHIC;
        public static final UnicodeBlock DESERET;
        public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS;
        public static final UnicodeBlock MUSICAL_SYMBOLS;
        public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B;
        public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT;
        public static final UnicodeBlock TAGS;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY;
        public static final UnicodeBlock CYRILLIC_SUPPLEMENT;
        public static final UnicodeBlock TAGALOG;
        public static final UnicodeBlock HANUNOO;
        public static final UnicodeBlock BUHID;
        public static final UnicodeBlock TAGBANWA;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A;
        public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B;
        public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B;
        public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS;
        public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS;
        public static final UnicodeBlock VARIATION_SELECTORS;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A;
        public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B;
        public static final UnicodeBlock LIMBU;
        public static final UnicodeBlock TAI_LE;
        public static final UnicodeBlock KHMER_SYMBOLS;
        public static final UnicodeBlock PHONETIC_EXTENSIONS;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS;
        public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS;
        public static final UnicodeBlock LINEAR_B_SYLLABARY;
        public static final UnicodeBlock LINEAR_B_IDEOGRAMS;
        public static final UnicodeBlock AEGEAN_NUMBERS;
        public static final UnicodeBlock UGARITIC;
        public static final UnicodeBlock SHAVIAN;
        public static final UnicodeBlock OSMANYA;
        public static final UnicodeBlock CYPRIOT_SYLLABARY;
        public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS;
        public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT;
        public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION;
        public static final UnicodeBlock ANCIENT_GREEK_NUMBERS;
        public static final UnicodeBlock ARABIC_SUPPLEMENT;
        public static final UnicodeBlock BUGINESE;
        public static final UnicodeBlock CJK_STROKES;
        public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT;
        public static final UnicodeBlock COPTIC;
        public static final UnicodeBlock ETHIOPIC_EXTENDED;
        public static final UnicodeBlock ETHIOPIC_SUPPLEMENT;
        public static final UnicodeBlock GEORGIAN_SUPPLEMENT;
        public static final UnicodeBlock GLAGOLITIC;
        public static final UnicodeBlock KHAROSHTHI;
        public static final UnicodeBlock MODIFIER_TONE_LETTERS;
        public static final UnicodeBlock NEW_TAI_LUE;
        public static final UnicodeBlock OLD_PERSIAN;
        public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT;
        public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION;
        public static final UnicodeBlock SYLOTI_NAGRI;
        public static final UnicodeBlock TIFINAGH;
        public static final UnicodeBlock VERTICAL_FORMS;
        public static final UnicodeBlock NKO;
        public static final UnicodeBlock BALINESE;
        public static final UnicodeBlock LATIN_EXTENDED_C;
        public static final UnicodeBlock LATIN_EXTENDED_D;
        public static final UnicodeBlock PHAGS_PA;
        public static final UnicodeBlock PHOENICIAN;
        public static final UnicodeBlock CUNEIFORM;
        public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION;
        public static final UnicodeBlock COUNTING_ROD_NUMERALS;
        public static final UnicodeBlock SUNDANESE;
        public static final UnicodeBlock LEPCHA;
        public static final UnicodeBlock OL_CHIKI;
        public static final UnicodeBlock CYRILLIC_EXTENDED_A;
        public static final UnicodeBlock VAI;
        public static final UnicodeBlock CYRILLIC_EXTENDED_B;
        public static final UnicodeBlock SAURASHTRA;
        public static final UnicodeBlock KAYAH_LI;
        public static final UnicodeBlock REJANG;
        public static final UnicodeBlock CHAM;
        public static final UnicodeBlock ANCIENT_SYMBOLS;
        public static final UnicodeBlock PHAISTOS_DISC;
        public static final UnicodeBlock LYCIAN;
        public static final UnicodeBlock CARIAN;
        public static final UnicodeBlock LYDIAN;
        public static final UnicodeBlock MAHJONG_TILES;
        public static final UnicodeBlock DOMINO_TILES;
        public static final UnicodeBlock SAMARITAN;
        public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED;
        public static final UnicodeBlock TAI_THAM;
        public static final UnicodeBlock VEDIC_EXTENSIONS;
        public static final UnicodeBlock LISU;
        public static final UnicodeBlock BAMUM;
        public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS;
        public static final UnicodeBlock DEVANAGARI_EXTENDED;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A;
        public static final UnicodeBlock JAVANESE;
        public static final UnicodeBlock MYANMAR_EXTENDED_A;
        public static final UnicodeBlock TAI_VIET;
        public static final UnicodeBlock MEETEI_MAYEK;
        public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B;
        public static final UnicodeBlock IMPERIAL_ARAMAIC;
        public static final UnicodeBlock OLD_SOUTH_ARABIAN;
        public static final UnicodeBlock AVESTAN;
        public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN;
        public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI;
        public static final UnicodeBlock OLD_TURKIC;
        public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS;
        public static final UnicodeBlock KAITHI;
        public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS;
        public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT;
        public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C;
        public static final UnicodeBlock MANDAIC;
        public static final UnicodeBlock BATAK;
        public static final UnicodeBlock ETHIOPIC_EXTENDED_A;
        public static final UnicodeBlock BRAHMI;
        public static final UnicodeBlock BAMUM_SUPPLEMENT;
        public static final UnicodeBlock KANA_SUPPLEMENT;
        public static final UnicodeBlock PLAYING_CARDS;
        public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS;
        public static final UnicodeBlock EMOTICONS;
        public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS;
        public static final UnicodeBlock ALCHEMICAL_SYMBOLS;
        public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D;
        public static final UnicodeBlock ARABIC_EXTENDED_A;
        public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS;
        public static final UnicodeBlock CHAKMA;
        public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS;
        public static final UnicodeBlock MEROITIC_CURSIVE;
        public static final UnicodeBlock MEROITIC_HIEROGLYPHS;
        public static final UnicodeBlock MIAO;
        public static final UnicodeBlock SHARADA;
        public static final UnicodeBlock SORA_SOMPENG;
        public static final UnicodeBlock SUNDANESE_SUPPLEMENT;
        public static final UnicodeBlock TAKRI;
        public static final UnicodeBlock INVALID_CODE;
        private static SoftReference<Map<String, UnicodeBlock>> mref;
        private int m_id_;

        public static UnicodeBlock getInstance(int id) {
            if (id >= 0 && id < BLOCKS_.length) {
                return BLOCKS_[id];
            }
            return INVALID_CODE;
        }

        public static UnicodeBlock of(int ch) {
            if (ch > 0x10FFFF) {
                return INVALID_CODE;
            }
            return UnicodeBlock.getInstance(UCharacterProperty.INSTANCE.getIntPropertyValue(ch, 4097));
        }

        public static final UnicodeBlock forName(String blockName) {
            UnicodeBlock b;
            Map<String, UnicodeBlock> m = null;
            if (mref != null) {
                m = mref.get();
            }
            if (m == null) {
                m = new HashMap<String, UnicodeBlock>(BLOCKS_.length);
                for (int i = 0; i < BLOCKS_.length; ++i) {
                    UnicodeBlock b2 = BLOCKS_[i];
                    String name = UnicodeBlock.trimBlockName(UCharacter.getPropertyValueName(4097, b2.getID(), 1));
                    m.put(name, b2);
                }
                mref = new SoftReference<Map<String, UnicodeBlock>>(m);
            }
            if ((b = m.get(UnicodeBlock.trimBlockName(blockName))) == null) {
                throw new IllegalArgumentException();
            }
            return b;
        }

        private static String trimBlockName(String name) {
            String upper = name.toUpperCase(Locale.ENGLISH);
            StringBuilder result = new StringBuilder(upper.length());
            for (int i = 0; i < upper.length(); ++i) {
                char c = upper.charAt(i);
                if (c == ' ' || c == '_' || c == '-') continue;
                result.append(c);
            }
            return result.toString();
        }

        public int getID() {
            return this.m_id_;
        }

        private UnicodeBlock(String name, int id) {
            super(name);
            this.m_id_ = id;
            if (id >= 0) {
                UnicodeBlock.BLOCKS_[id] = this;
            }
        }

        static {
            PRIVATE_USE = PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", 78);
            CJK_COMPATIBILITY_IDEOGRAPHS = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 79);
            ALPHABETIC_PRESENTATION_FORMS = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 80);
            ARABIC_PRESENTATION_FORMS_A = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 81);
            COMBINING_HALF_MARKS = new UnicodeBlock("COMBINING_HALF_MARKS", 82);
            CJK_COMPATIBILITY_FORMS = new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 83);
            SMALL_FORM_VARIANTS = new UnicodeBlock("SMALL_FORM_VARIANTS", 84);
            ARABIC_PRESENTATION_FORMS_B = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 85);
            SPECIALS = new UnicodeBlock("SPECIALS", 86);
            HALFWIDTH_AND_FULLWIDTH_FORMS = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 87);
            OLD_ITALIC = new UnicodeBlock("OLD_ITALIC", 88);
            GOTHIC = new UnicodeBlock("GOTHIC", 89);
            DESERET = new UnicodeBlock("DESERET", 90);
            BYZANTINE_MUSICAL_SYMBOLS = new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS", 91);
            MUSICAL_SYMBOLS = new UnicodeBlock("MUSICAL_SYMBOLS", 92);
            MATHEMATICAL_ALPHANUMERIC_SYMBOLS = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", 93);
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", 94);
            CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", 95);
            TAGS = new UnicodeBlock("TAGS", 96);
            CYRILLIC_SUPPLEMENTARY = new UnicodeBlock("CYRILLIC_SUPPLEMENTARY", 97);
            CYRILLIC_SUPPLEMENT = new UnicodeBlock("CYRILLIC_SUPPLEMENT", 97);
            TAGALOG = new UnicodeBlock("TAGALOG", 98);
            HANUNOO = new UnicodeBlock("HANUNOO", 99);
            BUHID = new UnicodeBlock("BUHID", 100);
            TAGBANWA = new UnicodeBlock("TAGBANWA", 101);
            MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", 102);
            SUPPLEMENTAL_ARROWS_A = new UnicodeBlock("SUPPLEMENTAL_ARROWS_A", 103);
            SUPPLEMENTAL_ARROWS_B = new UnicodeBlock("SUPPLEMENTAL_ARROWS_B", 104);
            MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", 105);
            SUPPLEMENTAL_MATHEMATICAL_OPERATORS = new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", 106);
            KATAKANA_PHONETIC_EXTENSIONS = new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS", 107);
            VARIATION_SELECTORS = new UnicodeBlock("VARIATION_SELECTORS", 108);
            SUPPLEMENTARY_PRIVATE_USE_AREA_A = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", 109);
            SUPPLEMENTARY_PRIVATE_USE_AREA_B = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", 110);
            LIMBU = new UnicodeBlock("LIMBU", 111);
            TAI_LE = new UnicodeBlock("TAI_LE", 112);
            KHMER_SYMBOLS = new UnicodeBlock("KHMER_SYMBOLS", 113);
            PHONETIC_EXTENSIONS = new UnicodeBlock("PHONETIC_EXTENSIONS", 114);
            MISCELLANEOUS_SYMBOLS_AND_ARROWS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", 115);
            YIJING_HEXAGRAM_SYMBOLS = new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS", 116);
            LINEAR_B_SYLLABARY = new UnicodeBlock("LINEAR_B_SYLLABARY", 117);
            LINEAR_B_IDEOGRAMS = new UnicodeBlock("LINEAR_B_IDEOGRAMS", 118);
            AEGEAN_NUMBERS = new UnicodeBlock("AEGEAN_NUMBERS", 119);
            UGARITIC = new UnicodeBlock("UGARITIC", 120);
            SHAVIAN = new UnicodeBlock("SHAVIAN", 121);
            OSMANYA = new UnicodeBlock("OSMANYA", 122);
            CYPRIOT_SYLLABARY = new UnicodeBlock("CYPRIOT_SYLLABARY", 123);
            TAI_XUAN_JING_SYMBOLS = new UnicodeBlock("TAI_XUAN_JING_SYMBOLS", 124);
            VARIATION_SELECTORS_SUPPLEMENT = new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", 125);
            ANCIENT_GREEK_MUSICAL_NOTATION = new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", 126);
            ANCIENT_GREEK_NUMBERS = new UnicodeBlock("ANCIENT_GREEK_NUMBERS", 127);
            ARABIC_SUPPLEMENT = new UnicodeBlock("ARABIC_SUPPLEMENT", 128);
            BUGINESE = new UnicodeBlock("BUGINESE", 129);
            CJK_STROKES = new UnicodeBlock("CJK_STROKES", 130);
            COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", 131);
            COPTIC = new UnicodeBlock("COPTIC", 132);
            ETHIOPIC_EXTENDED = new UnicodeBlock("ETHIOPIC_EXTENDED", 133);
            ETHIOPIC_SUPPLEMENT = new UnicodeBlock("ETHIOPIC_SUPPLEMENT", 134);
            GEORGIAN_SUPPLEMENT = new UnicodeBlock("GEORGIAN_SUPPLEMENT", 135);
            GLAGOLITIC = new UnicodeBlock("GLAGOLITIC", 136);
            KHAROSHTHI = new UnicodeBlock("KHAROSHTHI", 137);
            MODIFIER_TONE_LETTERS = new UnicodeBlock("MODIFIER_TONE_LETTERS", 138);
            NEW_TAI_LUE = new UnicodeBlock("NEW_TAI_LUE", 139);
            OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", 140);
            PHONETIC_EXTENSIONS_SUPPLEMENT = new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", 141);
            SUPPLEMENTAL_PUNCTUATION = new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION", 142);
            SYLOTI_NAGRI = new UnicodeBlock("SYLOTI_NAGRI", 143);
            TIFINAGH = new UnicodeBlock("TIFINAGH", 144);
            VERTICAL_FORMS = new UnicodeBlock("VERTICAL_FORMS", 145);
            NKO = new UnicodeBlock("NKO", 146);
            BALINESE = new UnicodeBlock("BALINESE", 147);
            LATIN_EXTENDED_C = new UnicodeBlock("LATIN_EXTENDED_C", 148);
            LATIN_EXTENDED_D = new UnicodeBlock("LATIN_EXTENDED_D", 149);
            PHAGS_PA = new UnicodeBlock("PHAGS_PA", 150);
            PHOENICIAN = new UnicodeBlock("PHOENICIAN", 151);
            CUNEIFORM = new UnicodeBlock("CUNEIFORM", 152);
            CUNEIFORM_NUMBERS_AND_PUNCTUATION = new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", 153);
            COUNTING_ROD_NUMERALS = new UnicodeBlock("COUNTING_ROD_NUMERALS", 154);
            SUNDANESE = new UnicodeBlock("SUNDANESE", 155);
            LEPCHA = new UnicodeBlock("LEPCHA", 156);
            OL_CHIKI = new UnicodeBlock("OL_CHIKI", 157);
            CYRILLIC_EXTENDED_A = new UnicodeBlock("CYRILLIC_EXTENDED_A", 158);
            VAI = new UnicodeBlock("VAI", 159);
            CYRILLIC_EXTENDED_B = new UnicodeBlock("CYRILLIC_EXTENDED_B", 160);
            SAURASHTRA = new UnicodeBlock("SAURASHTRA", 161);
            KAYAH_LI = new UnicodeBlock("KAYAH_LI", 162);
            REJANG = new UnicodeBlock("REJANG", 163);
            CHAM = new UnicodeBlock("CHAM", 164);
            ANCIENT_SYMBOLS = new UnicodeBlock("ANCIENT_SYMBOLS", 165);
            PHAISTOS_DISC = new UnicodeBlock("PHAISTOS_DISC", 166);
            LYCIAN = new UnicodeBlock("LYCIAN", 167);
            CARIAN = new UnicodeBlock("CARIAN", 168);
            LYDIAN = new UnicodeBlock("LYDIAN", 169);
            MAHJONG_TILES = new UnicodeBlock("MAHJONG_TILES", 170);
            DOMINO_TILES = new UnicodeBlock("DOMINO_TILES", 171);
            SAMARITAN = new UnicodeBlock("SAMARITAN", 172);
            UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED", 173);
            TAI_THAM = new UnicodeBlock("TAI_THAM", 174);
            VEDIC_EXTENSIONS = new UnicodeBlock("VEDIC_EXTENSIONS", 175);
            LISU = new UnicodeBlock("LISU", 176);
            BAMUM = new UnicodeBlock("BAMUM", 177);
            COMMON_INDIC_NUMBER_FORMS = new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS", 178);
            DEVANAGARI_EXTENDED = new UnicodeBlock("DEVANAGARI_EXTENDED", 179);
            HANGUL_JAMO_EXTENDED_A = new UnicodeBlock("HANGUL_JAMO_EXTENDED_A", 180);
            JAVANESE = new UnicodeBlock("JAVANESE", 181);
            MYANMAR_EXTENDED_A = new UnicodeBlock("MYANMAR_EXTENDED_A", 182);
            TAI_VIET = new UnicodeBlock("TAI_VIET", 183);
            MEETEI_MAYEK = new UnicodeBlock("MEETEI_MAYEK", 184);
            HANGUL_JAMO_EXTENDED_B = new UnicodeBlock("HANGUL_JAMO_EXTENDED_B", 185);
            IMPERIAL_ARAMAIC = new UnicodeBlock("IMPERIAL_ARAMAIC", 186);
            OLD_SOUTH_ARABIAN = new UnicodeBlock("OLD_SOUTH_ARABIAN", 187);
            AVESTAN = new UnicodeBlock("AVESTAN", 188);
            INSCRIPTIONAL_PARTHIAN = new UnicodeBlock("INSCRIPTIONAL_PARTHIAN", 189);
            INSCRIPTIONAL_PAHLAVI = new UnicodeBlock("INSCRIPTIONAL_PAHLAVI", 190);
            OLD_TURKIC = new UnicodeBlock("OLD_TURKIC", 191);
            RUMI_NUMERAL_SYMBOLS = new UnicodeBlock("RUMI_NUMERAL_SYMBOLS", 192);
            KAITHI = new UnicodeBlock("KAITHI", 193);
            EGYPTIAN_HIEROGLYPHS = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS", 194);
            ENCLOSED_ALPHANUMERIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", 195);
            ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", 196);
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", 197);
            MANDAIC = new UnicodeBlock("MANDAIC", 198);
            BATAK = new UnicodeBlock("BATAK", 199);
            ETHIOPIC_EXTENDED_A = new UnicodeBlock("ETHIOPIC_EXTENDED_A", 200);
            BRAHMI = new UnicodeBlock("BRAHMI", 201);
            BAMUM_SUPPLEMENT = new UnicodeBlock("BAMUM_SUPPLEMENT", 202);
            KANA_SUPPLEMENT = new UnicodeBlock("KANA_SUPPLEMENT", 203);
            PLAYING_CARDS = new UnicodeBlock("PLAYING_CARDS", 204);
            MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS", 205);
            EMOTICONS = new UnicodeBlock("EMOTICONS", 206);
            TRANSPORT_AND_MAP_SYMBOLS = new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS", 207);
            ALCHEMICAL_SYMBOLS = new UnicodeBlock("ALCHEMICAL_SYMBOLS", 208);
            CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", 209);
            ARABIC_EXTENDED_A = new UnicodeBlock("ARABIC_EXTENDED_A", 210);
            ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = new UnicodeBlock("ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS", 211);
            CHAKMA = new UnicodeBlock("CHAKMA", 212);
            MEETEI_MAYEK_EXTENSIONS = new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS", 213);
            MEROITIC_CURSIVE = new UnicodeBlock("MEROITIC_CURSIVE", 214);
            MEROITIC_HIEROGLYPHS = new UnicodeBlock("MEROITIC_HIEROGLYPHS", 215);
            MIAO = new UnicodeBlock("MIAO", 216);
            SHARADA = new UnicodeBlock("SHARADA", 217);
            SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", 218);
            SUNDANESE_SUPPLEMENT = new UnicodeBlock("SUNDANESE_SUPPLEMENT", 219);
            TAKRI = new UnicodeBlock("TAKRI", 220);
            INVALID_CODE = new UnicodeBlock("INVALID_CODE", -1);
            for (int blockId = 0; blockId < 221; ++blockId) {
                if (BLOCKS_[blockId] != null) continue;
                throw new IllegalStateException("UnicodeBlock.BLOCKS_[" + blockId + "] not initialized");
            }
        }
    }
}

