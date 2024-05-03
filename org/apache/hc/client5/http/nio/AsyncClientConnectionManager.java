/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.nio;

import java.util.concurrent.Future;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE)
public interface AsyncClientConnectionManager
extends ModalCloseable {
    public Future<AsyncConnectionEndpoint> lease(String var1, HttpRoute var2, Object var3, Timeout var4, FutureCallback<AsyncConnectionEndpoint> var5);

    public void release(AsyncConnectionEndpoint var1, Object var2, TimeValue var3);

    public Future<AsyncConnectionEndpoint> connect(AsyncConnectionEndpoint var1, ConnectionInitiator var2, Timeout var3, Object var4, HttpContext var5, FutureCallback<AsyncConnectionEndpoint> var6);

    public void upgrade(AsyncConnectionEndpoint var1, Object var2, HttpContext var3);
}

