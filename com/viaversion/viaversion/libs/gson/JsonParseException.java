/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.gson;

public class JsonParseException
extends RuntimeException {
    static final long serialVersionUID = -4086729973971783390L;

    public JsonParseException(String msg) {
        super(msg);
    }

    public JsonParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public JsonParseException(Throwable cause) {
        super(cause);
    }
}

