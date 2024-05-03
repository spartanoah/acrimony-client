/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.ReasonPhraseCatalog;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultClassicHttpResponseFactory
implements HttpResponseFactory<ClassicHttpResponse> {
    public static final DefaultClassicHttpResponseFactory INSTANCE = new DefaultClassicHttpResponseFactory();
    private final ReasonPhraseCatalog reasonCatalog;

    public DefaultClassicHttpResponseFactory(ReasonPhraseCatalog catalog) {
        this.reasonCatalog = Args.notNull(catalog, "Reason phrase catalog");
    }

    public DefaultClassicHttpResponseFactory() {
        this(EnglishReasonPhraseCatalog.INSTANCE);
    }

    @Override
    public ClassicHttpResponse newHttpResponse(int status, String reasonPhrase) {
        return new BasicClassicHttpResponse(status, reasonPhrase);
    }

    @Override
    public ClassicHttpResponse newHttpResponse(int status) {
        return new BasicClassicHttpResponse(status, this.reasonCatalog, null);
    }
}

