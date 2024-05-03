/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io;

import java.io.Serializable;
import org.apache.commons.io.FilenameUtils;

public final class IOCase
implements Serializable {
    public static final IOCase SENSITIVE = new IOCase("Sensitive", true);
    public static final IOCase INSENSITIVE = new IOCase("Insensitive", false);
    public static final IOCase SYSTEM = new IOCase("System", !FilenameUtils.isSystemWindows());
    private static final long serialVersionUID = -6343169151696340687L;
    private final String name;
    private final transient boolean sensitive;

    public static IOCase forName(String name) {
        if (IOCase.SENSITIVE.name.equals(name)) {
            return SENSITIVE;
        }
        if (IOCase.INSENSITIVE.name.equals(name)) {
            return INSENSITIVE;
        }
        if (IOCase.SYSTEM.name.equals(name)) {
            return SYSTEM;
        }
        throw new IllegalArgumentException("Invalid IOCase name: " + name);
    }

    private IOCase(String name, boolean sensitive) {
        this.name = name;
        this.sensitive = sensitive;
    }

    private Object readResolve() {
        return IOCase.forName(this.name);
    }

    public String getName() {
        return this.name;
    }

    public boolean isCaseSensitive() {
        return this.sensitive;
    }

    public int checkCompareTo(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("The strings must not be null");
        }
        return this.sensitive ? str1.compareTo(str2) : str1.compareToIgnoreCase(str2);
    }

    public boolean checkEquals(String str1, String str2) {
        if (str1 == null || str2 == null) {
            throw new NullPointerException("The strings must not be null");
        }
        return this.sensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
    }

    public boolean checkStartsWith(String str, String start) {
        return str.regionMatches(!this.sensitive, 0, start, 0, start.length());
    }

    public boolean checkEndsWith(String str, String end) {
        int endLen = end.length();
        return str.regionMatches(!this.sensitive, str.length() - endLen, end, 0, endLen);
    }

    public int checkIndexOf(String str, int strStartIndex, String search) {
        int endIndex = str.length() - search.length();
        if (endIndex >= strStartIndex) {
            for (int i = strStartIndex; i <= endIndex; ++i) {
                if (!this.checkRegionMatches(str, i, search)) continue;
                return i;
            }
        }
        return -1;
    }

    public boolean checkRegionMatches(String str, int strStartIndex, String search) {
        return str.regionMatches(!this.sensitive, strStartIndex, search, 0, search.length());
    }

    public String toString() {
        return this.name;
    }
}

