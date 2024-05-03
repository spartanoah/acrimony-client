/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import org.apache.hc.core5.reactor.AbstractSingleCoreIOReactor;

final class IOReactorWorker
implements Runnable {
    private final AbstractSingleCoreIOReactor ioReactor;
    private volatile Throwable throwable;

    public IOReactorWorker(AbstractSingleCoreIOReactor ioReactor) {
        this.ioReactor = ioReactor;
    }

    @Override
    public void run() {
        try {
            this.ioReactor.execute();
        } catch (Error ex) {
            this.throwable = ex;
            throw ex;
        } catch (Exception ex) {
            this.throwable = ex;
        }
    }

    public Throwable getThrowable() {
        return this.throwable;
    }
}

