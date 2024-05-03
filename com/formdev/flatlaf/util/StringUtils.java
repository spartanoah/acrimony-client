/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringUtils {
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String removeLeading(String string, String leading) {
        return string.startsWith(leading) ? string.substring(leading.length()) : string;
    }

    public static String removeTrailing(String string, String trailing) {
        return string.endsWith(trailing) ? string.substring(0, string.length() - trailing.length()) : string;
    }

    public static List<String> split(String str, char delim) {
        return StringUtils.split(str, delim, false, false);
    }

    public static List<String> split(String str, char delim, boolean trim, boolean excludeEmpty) {
        int delimIndex = str.indexOf(delim);
        if (delimIndex < 0) {
            if (trim) {
                str = str.trim();
            }
            return !excludeEmpty || !str.isEmpty() ? Collections.singletonList(str) : Collections.emptyList();
        }
        ArrayList<String> strs = new ArrayList<String>();
        int index = 0;
        while (delimIndex >= 0) {
            StringUtils.add(strs, str, index, delimIndex, trim, excludeEmpty);
            index = delimIndex + 1;
            delimIndex = str.indexOf(delim, index);
        }
        StringUtils.add(strs, str, index, str.length(), trim, excludeEmpty);
        return strs;
    }

    private static void add(List<String> strs, String str, int beginIndex, int endIndex, boolean trim, boolean excludeEmpty) {
        if (trim) {
            beginIndex = StringUtils.trimBegin(str, beginIndex, endIndex);
            endIndex = StringUtils.trimEnd(str, beginIndex, endIndex);
        }
        if (!excludeEmpty || endIndex > beginIndex) {
            strs.add(str.substring(beginIndex, endIndex));
        }
    }

    public static String substringTrimmed(String str, int beginIndex) {
        return StringUtils.substringTrimmed(str, beginIndex, str.length());
    }

    public static String substringTrimmed(String str, int beginIndex, int endIndex) {
        return (endIndex = StringUtils.trimEnd(str, beginIndex = StringUtils.trimBegin(str, beginIndex, endIndex), endIndex)) > beginIndex ? str.substring(beginIndex, endIndex) : "";
    }

    public static boolean isTrimmedEmpty(String str) {
        int endIndex;
        int length = str.length();
        int beginIndex = StringUtils.trimBegin(str, 0, length);
        return beginIndex >= (endIndex = StringUtils.trimEnd(str, beginIndex, length));
    }

    private static int trimBegin(String str, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && str.charAt(beginIndex) <= ' ') {
            ++beginIndex;
        }
        return beginIndex;
    }

    private static int trimEnd(String str, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && str.charAt(endIndex - 1) <= ' ') {
            --endIndex;
        }
        return endIndex;
    }
}

