/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.IOException;
import java.util.Locale;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ReasonPhraseCatalog;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.io.Closer;

public class BasicClassicHttpResponse
extends BasicHttpResponse
implements ClassicHttpResponse {
    private static final long serialVersionUID = 1L;
    private HttpEntity entity;

    public BasicClassicHttpResponse(int code, ReasonPhraseCatalog catalog, Locale locale) {
        super(code, catalog, locale);
    }

    public BasicClassicHttpResponse(int code, String reasonPhrase) {
        super(code, reasonPhrase);
    }

    public BasicClassicHttpResponse(int code) {
        super(code);
    }

    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public void close() throws IOException {
        Closer.close(this.entity);
    }
}

