/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncPushConsumer
extends AsyncDataConsumer {
    public void consumePromise(HttpRequest var1, HttpResponse var2, EntityDetails var3, HttpContext var4) throws HttpException, IOException;

    public void failed(Exception var1);
}

