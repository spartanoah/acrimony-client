/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.exc;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.RequestPayload;

public abstract class StreamReadException
extends JsonProcessingException {
    static final long serialVersionUID = 1L;
    protected transient JsonParser _processor;
    protected RequestPayload _requestPayload;

    public StreamReadException(JsonParser p, String msg) {
        super(msg, p == null ? null : p.getCurrentLocation());
        this._processor = p;
    }

    public StreamReadException(JsonParser p, String msg, Throwable root) {
        super(msg, p == null ? null : p.getCurrentLocation(), root);
        this._processor = p;
    }

    public StreamReadException(JsonParser p, String msg, JsonLocation loc) {
        super(msg, loc, null);
        this._processor = p;
    }

    protected StreamReadException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg);
        if (rootCause != null) {
            this.initCause(rootCause);
        }
        this._location = loc;
    }

    public abstract StreamReadException withParser(JsonParser var1);

    public abstract StreamReadException withRequestPayload(RequestPayload var1);

    @Override
    public JsonParser getProcessor() {
        return this._processor;
    }

    public RequestPayload getRequestPayload() {
        return this._requestPayload;
    }

    public String getRequestPayloadAsString() {
        return this._requestPayload != null ? this._requestPayload.toString() : null;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (this._requestPayload != null) {
            msg = msg + "\nRequest payload : " + this._requestPayload.toString();
        }
        return msg;
    }
}

