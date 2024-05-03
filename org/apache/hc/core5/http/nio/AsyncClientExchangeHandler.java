/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataExchangeHandler;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncClientExchangeHandler
extends AsyncDataExchangeHandler {
    public void produceRequest(RequestChannel var1, HttpContext var2) throws HttpException, IOException;

    public void consumeResponse(HttpResponse var1, EntityDetails var2, HttpContext var3) throws HttpException, IOException;

    public void consumeInformation(HttpResponse var1, HttpContext var2) throws HttpException, IOException;

    public void cancel();
}

