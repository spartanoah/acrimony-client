/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;

public interface AsyncEntityConsumer<T>
extends AsyncDataConsumer {
    public void streamStart(EntityDetails var1, FutureCallback<T> var2) throws HttpException, IOException;

    public void failed(Exception var1);

    public T getContent();
}

