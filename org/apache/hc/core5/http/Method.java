/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.util.Locale;
import org.apache.hc.core5.util.Args;

public enum Method {
    GET(true, true),
    HEAD(true, true),
    POST(false, false),
    PUT(false, true),
    DELETE(false, true),
    CONNECT(false, false),
    TRACE(true, true),
    OPTIONS(true, true),
    PATCH(false, false);

    private final boolean safe;
    private final boolean idempotent;

    private Method(boolean safe, boolean idempotent) {
        this.safe = safe;
        this.idempotent = idempotent;
    }

    public boolean isSafe() {
        return this.safe;
    }

    public boolean isIdempotent() {
        return this.idempotent;
    }

    public static boolean isSafe(String value) {
        if (value == null) {
            return false;
        }
        try {
            return Method.normalizedValueOf((String)value).safe;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isIdempotent(String value) {
        if (value == null) {
            return false;
        }
        try {
            return Method.normalizedValueOf((String)value).idempotent;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static Method normalizedValueOf(String method) {
        return Method.valueOf(Args.notNull(method, "method").toUpperCase(Locale.ROOT));
    }

    public boolean isSame(String value) {
        if (value == null) {
            return false;
        }
        return this.name().equalsIgnoreCase(value);
    }
}

