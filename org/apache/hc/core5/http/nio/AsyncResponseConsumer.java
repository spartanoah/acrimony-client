/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncResponseConsumer<T>
extends AsyncDataConsumer {
    public void consumeResponse(HttpResponse var1, EntityDetails var2, HttpContext var3, FutureCallback<T> var4) throws HttpException, IOException;

    public void informationResponse(HttpResponse var1, HttpContext var2) throws HttpException, IOException;

    public void failed(Exception var1);
}

