/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.util.TimeValue;

@Internal
public interface AsyncExecRuntime {
    public boolean isEndpointAcquired();

    public Cancellable acquireEndpoint(String var1, HttpRoute var2, Object var3, HttpClientContext var4, FutureCallback<AsyncExecRuntime> var5);

    public void releaseEndpoint();

    public void discardEndpoint();

    public boolean isEndpointConnected();

    public Cancellable connectEndpoint(HttpClientContext var1, FutureCallback<AsyncExecRuntime> var2);

    public void upgradeTls(HttpClientContext var1);

    public boolean validateConnection();

    public Cancellable execute(String var1, AsyncClientExchangeHandler var2, HttpClientContext var3);

    public void markConnectionReusable(Object var1, TimeValue var2);

    public void markConnectionNonReusable();

    public AsyncExecRuntime fork();
}

