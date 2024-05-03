/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncDataProducer;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncRequestProducer
extends AsyncDataProducer {
    public void sendRequest(RequestChannel var1, HttpContext var2) throws HttpException, IOException;

    public boolean isRepeatable();

    public void failed(Exception var1);
}

