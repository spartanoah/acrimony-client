/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import java.io.IOException;
import java.io.Serializable;

public class RequestPayload
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected byte[] _payloadAsBytes;
    protected CharSequence _payloadAsText;
    protected String _charset;

    public RequestPayload(byte[] bytes, String charset) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        this._payloadAsBytes = bytes;
        this._charset = charset == null || charset.isEmpty() ? "UTF-8" : charset;
    }

    public RequestPayload(CharSequence str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        this._payloadAsText = str;
    }

    public Object getRawPayload() {
        if (this._payloadAsBytes != null) {
            return this._payloadAsBytes;
        }
        return this._payloadAsText;
    }

    public String toString() {
        if (this._payloadAsBytes != null) {
            try {
                return new String(this._payloadAsBytes, this._charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this._payloadAsText.toString();
    }
}

