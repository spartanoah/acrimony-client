/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

import org.json.JSONException;

public class JSONPointerException
extends JSONException {
    private static final long serialVersionUID = 8872944667561856751L;

    public JSONPointerException(String message) {
        super(message);
    }

    public JSONPointerException(String message, Throwable cause) {
        super(message, cause);
    }
}

