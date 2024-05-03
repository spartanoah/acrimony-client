/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Locale;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.AbstractMessageWrapper;

public class HttpResponseWrapper
extends AbstractMessageWrapper
implements HttpResponse {
    private final HttpResponse message;

    public HttpResponseWrapper(HttpResponse message) {
        super(message);
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.message.getCode();
    }

    @Override
    public void setCode(int code) {
        this.message.setCode(code);
    }

    @Override
    public String getReasonPhrase() {
        return this.message.getReasonPhrase();
    }

    @Override
    public void setReasonPhrase(String reason) {
        this.message.setReasonPhrase(reason);
    }

    @Override
    public Locale getLocale() {
        return this.message.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        this.message.setLocale(loc);
    }
}

