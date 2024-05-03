/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

public class JSONException
extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

