/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

import org.apache.logging.log4j.core.helpers.Strings;

public class Integers {
    public static int parseInt(String s, int defaultValue) {
        return Strings.isEmpty(s) ? defaultValue : Integer.parseInt(s);
    }

    public static int parseInt(String s) {
        return Integers.parseInt(s, 0);
    }
}

