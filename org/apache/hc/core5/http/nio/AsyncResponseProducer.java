/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncDataProducer;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncResponseProducer
extends AsyncDataProducer {
    public void sendResponse(ResponseChannel var1, HttpContext var2) throws HttpException, IOException;

    public void failed(Exception var1);
}

