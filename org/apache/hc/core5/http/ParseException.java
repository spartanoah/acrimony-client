/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.ProtocolException;

public class ParseException
extends ProtocolException {
    private static final long serialVersionUID = -7288819855864183578L;
    private final int errorOffset;

    public ParseException() {
        this.errorOffset = -1;
    }

    public ParseException(String message) {
        super(message);
        this.errorOffset = -1;
    }

    public ParseException(String description, CharSequence text, int off, int len, int errorOffset) {
        super(description + (errorOffset >= 0 ? "; error at offset " + errorOffset : "") + (text != null && len < 1024 ? ": <" + text.subSequence(off, off + len) + ">" : ""));
        this.errorOffset = errorOffset;
    }

    public ParseException(String description, CharSequence text, int off, int len) {
        this(description, text, off, len, -1);
    }

    public int getErrorOffset() {
        return this.errorOffset;
    }
}

