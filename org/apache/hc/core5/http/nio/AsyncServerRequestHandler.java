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
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface AsyncServerRequestHandler<T> {
    public AsyncRequestConsumer<T> prepare(HttpRequest var1, EntityDetails var2, HttpContext var3) throws HttpException;

    public void handle(T var1, ResponseTrigger var2, HttpContext var3) throws HttpException, IOException;

    public static interface ResponseTrigger {
        public void sendInformation(HttpResponse var1, HttpContext var2) throws HttpException, IOException;

        public void submitResponse(AsyncResponseProducer var1, HttpContext var2) throws HttpException, IOException;

        public void pushPromise(HttpRequest var1, HttpContext var2, AsyncPushProducer var3) throws HttpException, IOException;
    }
}

