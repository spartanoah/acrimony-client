/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.ResourceHolder;

public interface AsyncDataConsumer
extends ResourceHolder {
    public void updateCapacity(CapacityChannel var1) throws IOException;

    public void consume(ByteBuffer var1) throws IOException;

    public void streamEnd(List<? extends Header> var1) throws HttpException, IOException;
}

