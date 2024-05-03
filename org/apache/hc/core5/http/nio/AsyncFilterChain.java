/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface AsyncFilterChain {
    public AsyncDataConsumer proceed(HttpRequest var1, EntityDetails var2, HttpContext var3, ResponseTrigger var4) throws HttpException, IOException;

    public static interface ResponseTrigger {
        public void sendInformation(HttpResponse var1) throws HttpException, IOException;

        public void submitResponse(HttpResponse var1, AsyncEntityProducer var2) throws HttpException, IOException;

        public void pushPromise(HttpRequest var1, AsyncPushProducer var2) throws HttpException, IOException;
    }
}

