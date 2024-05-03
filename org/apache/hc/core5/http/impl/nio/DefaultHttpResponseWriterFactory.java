/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.nio.DefaultHttpResponseWriter;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.LineFormatter;
import org.apache.hc.core5.http.nio.NHttpMessageWriter;
import org.apache.hc.core5.http.nio.NHttpMessageWriterFactory;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseWriterFactory
implements NHttpMessageWriterFactory<HttpResponse> {
    public static final DefaultHttpResponseWriterFactory INSTANCE = new DefaultHttpResponseWriterFactory();
    private final LineFormatter lineFormatter;

    public DefaultHttpResponseWriterFactory(LineFormatter lineFormatter) {
        this.lineFormatter = lineFormatter != null ? lineFormatter : BasicLineFormatter.INSTANCE;
    }

    public DefaultHttpResponseWriterFactory() {
        this(null);
    }

    @Override
    public NHttpMessageWriter<HttpResponse> create() {
        return new DefaultHttpResponseWriter<HttpResponse>(this.lineFormatter);
    }
}

