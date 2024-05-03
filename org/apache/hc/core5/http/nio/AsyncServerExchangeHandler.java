/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncDataExchangeHandler;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public interface AsyncServerExchangeHandler
extends AsyncDataExchangeHandler {
    public void handleRequest(HttpRequest var1, EntityDetails var2, ResponseChannel var3, HttpContext var4) throws HttpException, IOException;
}

