/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic.methods;

import java.net.URI;
import java.util.concurrent.atomic.AtomicMarkableReference;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

public class HttpUriRequestBase
extends BasicClassicHttpRequest
implements HttpUriRequest,
CancellableDependency {
    private static final long serialVersionUID = 1L;
    private final AtomicMarkableReference<Cancellable> cancellableRef = new AtomicMarkableReference<Object>(null, false);
    private RequestConfig requestConfig;

    public HttpUriRequestBase(String method, URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public boolean cancel() {
        while (!this.cancellableRef.isMarked()) {
            Cancellable actualCancellable = this.cancellableRef.getReference();
            if (!this.cancellableRef.compareAndSet(actualCancellable, actualCancellable, false, true)) continue;
            if (actualCancellable != null) {
                actualCancellable.cancel();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancellableRef.isMarked();
    }

    @Override
    public void setDependency(Cancellable cancellable) {
        Cancellable actualCancellable = this.cancellableRef.getReference();
        if (!this.cancellableRef.compareAndSet(actualCancellable, cancellable, false, false)) {
            cancellable.cancel();
        }
    }

    public void reset() {
        boolean marked;
        Cancellable actualCancellable;
        do {
            marked = this.cancellableRef.isMarked();
            actualCancellable = this.cancellableRef.getReference();
            if (actualCancellable == null) continue;
            actualCancellable.cancel();
        } while (!this.cancellableRef.compareAndSet(actualCancellable, null, marked, false));
    }

    @Override
    public void abort() throws UnsupportedOperationException {
        this.cancel();
    }

    @Override
    public boolean isAborted() {
        return this.isCancelled();
    }

    public void setConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    @Override
    public RequestConfig getConfig() {
        return this.requestConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getMethod()).append(" ").append(this.getRequestUri());
        return sb.toString();
    }
}

