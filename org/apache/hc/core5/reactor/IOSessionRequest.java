/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Timeout;

final class IOSessionRequest
implements Future<IOSession> {
    final NamedEndpoint remoteEndpoint;
    final SocketAddress remoteAddress;
    final SocketAddress localAddress;
    final Timeout timeout;
    final Object attachment;
    final BasicFuture<IOSession> future;
    private final AtomicReference<ModalCloseable> closeableRef;

    public IOSessionRequest(NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, SocketAddress localAddress, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) {
        this.remoteEndpoint = remoteEndpoint;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.timeout = timeout;
        this.attachment = attachment;
        this.future = new BasicFuture<IOSession>(callback);
        this.closeableRef = new AtomicReference<Object>(null);
    }

    public void completed(ProtocolIOSession ioSession) {
        this.future.completed(ioSession);
        this.closeableRef.set(null);
    }

    public void failed(Exception cause) {
        this.future.failed(cause);
        this.closeableRef.set(null);
    }

    public boolean cancel() {
        boolean cancelled = this.future.cancel();
        ModalCloseable closeable = this.closeableRef.getAndSet(null);
        if (cancelled && closeable != null) {
            closeable.close(CloseMode.IMMEDIATE);
        }
        return cancelled;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.future.isCancelled();
    }

    public void assign(ModalCloseable closeable) {
        this.closeableRef.set(closeable);
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    @Override
    public IOSession get() throws InterruptedException, ExecutionException {
        return this.future.get();
    }

    @Override
    public IOSession get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.future.get(timeout, unit);
    }

    public String toString() {
        return "[remoteEndpoint=" + this.remoteEndpoint + ", remoteAddress=" + this.remoteAddress + ", localAddress=" + this.localAddress + ", attachment=" + this.attachment + ']';
    }
}

