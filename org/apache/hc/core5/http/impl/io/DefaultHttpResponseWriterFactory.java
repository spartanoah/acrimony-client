/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseWriter;
import org.apache.hc.core5.http.io.HttpMessageWriter;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;
import org.apache.hc.core5.http.message.BasicLineFormatter;
import org.apache.hc.core5.http.message.LineFormatter;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpResponseWriterFactory
implements HttpMessageWriterFactory<ClassicHttpResponse> {
    public static final DefaultHttpResponseWriterFactory INSTANCE = new DefaultHttpResponseWriterFactory();
    private final LineFormatter lineFormatter;

    public DefaultHttpResponseWriterFactory(LineFormatter lineFormatter) {
        this.lineFormatter = lineFormatter != null ? lineFormatter : BasicLineFormatter.INSTANCE;
    }

    public DefaultHttpResponseWriterFactory() {
        this(null);
    }

    @Override
    public HttpMessageWriter<ClassicHttpResponse> create() {
        return new DefaultHttpResponseWriter(this.lineFormatter);
    }
}

