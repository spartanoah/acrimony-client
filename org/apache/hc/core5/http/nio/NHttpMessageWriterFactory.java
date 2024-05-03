/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.nio.NHttpMessageWriter;

public interface NHttpMessageWriterFactory<T extends MessageHeaders> {
    public NHttpMessageWriter<T> create();
}

