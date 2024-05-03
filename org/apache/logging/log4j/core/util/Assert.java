/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.util.Collection;
import java.util.Map;

public final class Assert {
    private Assert() {
    }

    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof CharSequence) {
            return ((CharSequence)o).length() == 0;
        }
        if (o.getClass().isArray()) {
            return ((Object[])o).length == 0;
        }
        if (o instanceof Collection) {
            return ((Collection)o).isEmpty();
        }
        if (o instanceof Map) {
            return ((Map)o).isEmpty();
        }
        return false;
    }

    public static boolean isNonEmpty(Object o) {
        return !Assert.isEmpty(o);
    }

    public static <T> T requireNonEmpty(T value) {
        return Assert.requireNonEmpty(value, "");
    }

    public static <T> T requireNonEmpty(T value, String message) {
        if (Assert.isEmpty(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static int valueIsAtLeast(int value, int minValue) {
        if (value < minValue) {
            throw new IllegalArgumentException("Value should be at least " + minValue + " but was " + value);
        }
        return value;
    }
}

