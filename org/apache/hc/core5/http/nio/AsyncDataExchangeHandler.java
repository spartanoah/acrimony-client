/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncDataProducer;

public interface AsyncDataExchangeHandler
extends AsyncDataConsumer,
AsyncDataProducer {
    public void failed(Exception var1);
}

