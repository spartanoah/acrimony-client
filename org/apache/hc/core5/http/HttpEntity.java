/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;

public interface HttpEntity
extends EntityDetails,
Closeable {
    public boolean isRepeatable();

    public InputStream getContent() throws IOException, UnsupportedOperationException;

    public void writeTo(OutputStream var1) throws IOException;

    public boolean isStreaming();

    public Supplier<List<? extends Header>> getTrailers();
}

