/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonLocation;
import java.io.IOException;

public class JsonProcessingException
extends IOException {
    static final long serialVersionUID = 123L;
    protected JsonLocation _location;

    protected JsonProcessingException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg);
        if (rootCause != null) {
            this.initCause(rootCause);
        }
        this._location = loc;
    }

    protected JsonProcessingException(String msg) {
        super(msg);
    }

    protected JsonProcessingException(String msg, JsonLocation loc) {
        this(msg, loc, null);
    }

    protected JsonProcessingException(String msg, Throwable rootCause) {
        this(msg, null, rootCause);
    }

    protected JsonProcessingException(Throwable rootCause) {
        this(null, null, rootCause);
    }

    public JsonLocation getLocation() {
        return this._location;
    }

    public void clearLocation() {
        this._location = null;
    }

    public String getOriginalMessage() {
        return super.getMessage();
    }

    public Object getProcessor() {
        return null;
    }

    protected String getMessageSuffix() {
        return null;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        JsonLocation loc = this.getLocation();
        String suffix = this.getMessageSuffix();
        if (loc != null || suffix != null) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(msg);
            if (suffix != null) {
                sb.append(suffix);
            }
            if (loc != null) {
                sb.append('\n');
                sb.append(" at ");
                sb.append(loc.toString());
            }
            msg = sb.toString();
        }
        return msg;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + this.getMessage();
    }
}

