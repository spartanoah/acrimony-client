/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.ReasonPhraseCatalog;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseFactory
implements HttpResponseFactory<HttpResponse> {
    public static final DefaultHttpResponseFactory INSTANCE = new DefaultHttpResponseFactory();
    private final ReasonPhraseCatalog reasonCatalog;

    public DefaultHttpResponseFactory(ReasonPhraseCatalog catalog) {
        this.reasonCatalog = Args.notNull(catalog, "Reason phrase catalog");
    }

    public DefaultHttpResponseFactory() {
        this(EnglishReasonPhraseCatalog.INSTANCE);
    }

    @Override
    public HttpResponse newHttpResponse(int status, String reasonPhrase) {
        return new BasicHttpResponse(status, reasonPhrase);
    }

    @Override
    public HttpResponse newHttpResponse(int status) {
        return new BasicHttpResponse(status, this.reasonCatalog, null);
    }
}

