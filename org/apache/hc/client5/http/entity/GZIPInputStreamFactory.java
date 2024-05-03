/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.STATELESS)
public class GZIPInputStreamFactory
implements InputStreamFactory {
    private static final GZIPInputStreamFactory INSTANCE = new GZIPInputStreamFactory();

    public static GZIPInputStreamFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public InputStream create(InputStream inputStream) throws IOException {
        return new GZIPInputStream(inputStream);
    }
}

