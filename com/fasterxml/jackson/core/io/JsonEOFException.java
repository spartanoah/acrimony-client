/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonEOFException
extends JsonParseException {
    private static final long serialVersionUID = 1L;
    protected final JsonToken _token;

    public JsonEOFException(JsonParser p, JsonToken token, String msg) {
        super(p, msg);
        this._token = token;
    }

    public JsonToken getTokenBeingDecoded() {
        return this._token;
    }
}

