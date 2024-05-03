/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.util.TextUtils;

public class HttpResponseException
extends ClientProtocolException {
    private static final long serialVersionUID = -7186627969477257933L;
    private final int statusCode;
    private final String reasonPhrase;

    public HttpResponseException(int statusCode, String reasonPhrase) {
        super(String.format("status code: %d" + (TextUtils.isBlank(reasonPhrase) ? "" : ", reason phrase: %s"), statusCode, reasonPhrase));
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }
}

