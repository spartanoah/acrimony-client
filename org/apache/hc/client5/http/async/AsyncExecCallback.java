/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;

public interface AsyncExecCallback {
    public AsyncDataConsumer handleResponse(HttpResponse var1, EntityDetails var2) throws HttpException, IOException;

    public void handleInformationResponse(HttpResponse var1) throws HttpException, IOException;

    public void completed();

    public void failed(Exception var1);
}

