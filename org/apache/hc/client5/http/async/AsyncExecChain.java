/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async;

import java.io.IOException;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface AsyncExecChain {
    public void proceed(HttpRequest var1, AsyncEntityProducer var2, Scope var3, AsyncExecCallback var4) throws HttpException, IOException;

    public static final class Scope {
        public final String exchangeId;
        public final HttpRoute route;
        public final HttpRequest originalRequest;
        public final CancellableDependency cancellableDependency;
        public final HttpClientContext clientContext;
        public final AsyncExecRuntime execRuntime;

        public Scope(String exchangeId, HttpRoute route, HttpRequest originalRequest, CancellableDependency cancellableDependency, HttpClientContext clientContext, AsyncExecRuntime execRuntime) {
            this.exchangeId = Args.notBlank(exchangeId, "Exchange id");
            this.route = Args.notNull(route, "Route");
            this.originalRequest = Args.notNull(originalRequest, "Original request");
            this.cancellableDependency = Args.notNull(cancellableDependency, "Dependency");
            this.clientContext = clientContext != null ? clientContext : HttpClientContext.create();
            this.execRuntime = Args.notNull(execRuntime, "Exec runtime");
        }
    }
}

