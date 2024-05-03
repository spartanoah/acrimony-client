/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreCompleteExceptonFutureCallback<T>
implements FutureCallback<T> {
    private final FutureCallback<T> callback;
    private static final Logger LOG = LoggerFactory.getLogger(IgnoreCompleteExceptonFutureCallback.class);

    public IgnoreCompleteExceptonFutureCallback(FutureCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void completed(T result) {
        if (this.callback != null) {
            try {
                this.callback.completed(result);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void failed(Exception ex) {
        if (this.callback != null) {
            this.callback.failed(ex);
        }
    }

    @Override
    public void cancelled() {
        if (this.callback != null) {
            this.callback.cancelled();
        }
    }
}

