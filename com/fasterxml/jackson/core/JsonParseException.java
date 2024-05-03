/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.util.RequestPayload;

public class JsonParseException
extends StreamReadException {
    private static final long serialVersionUID = 2L;

    @Deprecated
    public JsonParseException(String msg, JsonLocation loc) {
        super(msg, loc, null);
    }

    @Deprecated
    public JsonParseException(String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    public JsonParseException(JsonParser p, String msg) {
        super(p, msg);
    }

    public JsonParseException(JsonParser p, String msg, Throwable root) {
        super(p, msg, root);
    }

    public JsonParseException(JsonParser p, String msg, JsonLocation loc) {
        super(p, msg, loc);
    }

    public JsonParseException(JsonParser p, String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    @Override
    public JsonParseException withParser(JsonParser p) {
        this._processor = p;
        return this;
    }

    @Override
    public JsonParseException withRequestPayload(RequestPayload p) {
        this._requestPayload = p;
        return this;
    }

    @Override
    public JsonParser getProcessor() {
        return super.getProcessor();
    }

    @Override
    public RequestPayload getRequestPayload() {
        return super.getRequestPayload();
    }

    @Override
    public String getRequestPayloadAsString() {
        return super.getRequestPayloadAsString();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

