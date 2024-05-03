/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client;

import org.apache.http.annotation.Immutable;
import org.apache.http.client.ClientProtocolException;

@Immutable
public class HttpResponseException
extends ClientProtocolException {
    private static final long serialVersionUID = -7186627969477257933L;
    private final int statusCode;

    public HttpResponseException(int statusCode, String s) {
        super(s);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}

