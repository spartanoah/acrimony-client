/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResourceHolder;

public interface AsyncDataProducer
extends ResourceHolder {
    public int available();

    public void produce(DataStreamChannel var1) throws IOException;
}

