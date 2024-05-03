/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

public final class Assert {
    private Assert() {
    }

    public static <T> T isNotNull(T checkMe, String name) {
        if (checkMe == null) {
            throw new NullPointerException(name + " is null");
        }
        return checkMe;
    }
}

