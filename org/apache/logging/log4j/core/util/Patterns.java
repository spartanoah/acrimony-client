/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

public final class Patterns {
    public static final String COMMA_SEPARATOR = Patterns.toWhitespaceSeparator(",");
    public static final String WHITESPACE = "\\s*";

    private Patterns() {
    }

    public static String toWhitespaceSeparator(String separator) {
        return WHITESPACE + separator + WHITESPACE;
    }
}

