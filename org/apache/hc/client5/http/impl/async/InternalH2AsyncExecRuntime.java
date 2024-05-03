/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.nio.pool.H2ConnPool
 */
package org.apache.hc.client5.http.impl.async;

import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexCancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http2.nio.pool.H2ConnPool;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

class InternalH2AsyncExecRuntime
implements AsyncExecRuntime {
    private final Logger log;
    private final H2ConnPool connPool;
    private final HandlerFactory<AsyncPushConsumer> pushHandlerFactory;
    private final AtomicReference<Endpoint> sessionRef;
    private volatile boolean reusable;

    InternalH2AsyncExecRuntime(Logger log, H2ConnPool connPool, HandlerFactory<AsyncPushConsumer> pushHandlerFactory) {
        this.log = log;
        this.connPool = connPool;
        this.pushHandlerFactory = pushHandlerFactory;
        this.sessionRef = new AtomicReference<Object>(null);
    }

    @Override
    public boolean isEndpointAcquired() {
        return this.sessionRef.get() != null;
    }

    @Override
    public Cancellable acquireEndpoint(final String id, HttpRoute route, Object object, HttpClientContext context, final FutureCallback<AsyncExecRuntime> callback) {
        if (this.sessionRef.get() == null) {
            final HttpHost target = route.getTargetHost();
            RequestConfig requestConfig = context.getRequestConfig();
            Timeout connectTimeout = requestConfig.getConnectTimeout();
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: acquiring endpoint ({})", (Object)id, (Object)connectTimeout);
            }
            return Operations.cancellable(this.connPool.getSession((Object)target, connectTimeout, (FutureCallback)new FutureCallback<IOSession>(){

                @Override
                public void completed(IOSession ioSession) {
                    InternalH2AsyncExecRuntime.this.sessionRef.set(new Endpoint(target, ioSession));
                    InternalH2AsyncExecRuntime.this.reusable = true;
                    if (InternalH2AsyncExecRuntime.this.log.isDebugEnabled()) {
                        InternalH2AsyncExecRuntime.this.log.debug("{}: acquired endpoint", (Object)id);
                    }
                    callback.completed(InternalH2AsyncExecRuntime.this);
                }

                @Override
                public void failed(Exception ex) {
                    callback.failed(ex);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }
            }));
        }
        callback.completed(this);
        return Operations.nonCancellable();
    }

    private void closeEndpoint(Endpoint endpoint) {
        endpoint.session.close(CloseMode.GRACEFUL);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: endpoint closed", (Object)ConnPoolSupport.getId(endpoint));
        }
    }

    @Override
    public void releaseEndpoint() {
        Endpoint endpoint = this.sessionRef.getAndSet(null);
        if (endpoint != null && !this.reusable) {
            this.closeEndpoint(endpoint);
        }
    }

    @Override
    public void discardEndpoint() {
        Endpoint endpoint = this.sessionRef.getAndSet(null);
        if (endpoint != null) {
            this.closeEndpoint(endpoint);
        }
    }

    @Override
    public boolean validateConnection() {
        if (this.reusable) {
            Endpoint endpoint = this.sessionRef.get();
            return endpoint != null && endpoint.session.isOpen();
        }
        Endpoint endpoint = this.sessionRef.getAndSet(null);
        if (endpoint != null) {
            this.closeEndpoint(endpoint);
        }
        return false;
    }

    @Override
    public boolean isEndpointConnected() {
        Endpoint endpoint = this.sessionRef.get();
        return endpoint != null && endpoint.session.isOpen();
    }

    Endpoint ensureValid() {
        Endpoint endpoint = this.sessionRef.get();
        if (endpoint == null) {
            throw new IllegalStateException("I/O session not acquired / already released");
        }
        return endpoint;
    }

    @Override
    public Cancellable connectEndpoint(HttpClientContext context, final FutureCallback<AsyncExecRuntime> callback) {
        final Endpoint endpoint = this.ensureValid();
        if (endpoint.session.isOpen()) {
            callback.completed(this);
            return Operations.nonCancellable();
        }
        final HttpHost target = endpoint.target;
        RequestConfig requestConfig = context.getRequestConfig();
        Timeout connectTimeout = requestConfig.getConnectTimeout();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: connecting endpoint ({})", (Object)ConnPoolSupport.getId(endpoint), (Object)connectTimeout);
        }
        return Operations.cancellable(this.connPool.getSession((Object)target, connectTimeout, (FutureCallback)new FutureCallback<IOSession>(){

            @Override
            public void completed(IOSession ioSession) {
                InternalH2AsyncExecRuntime.this.sessionRef.set(new Endpoint(target, ioSession));
                InternalH2AsyncExecRuntime.this.reusable = true;
                if (InternalH2AsyncExecRuntime.this.log.isDebugEnabled()) {
                    InternalH2AsyncExecRuntime.this.log.debug("{}: endpoint connected", (Object)ConnPoolSupport.getId(endpoint));
                }
                callback.completed(InternalH2AsyncExecRuntime.this);
            }

            @Override
            public void failed(Exception ex) {
                callback.failed(ex);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        }));
    }

    @Override
    public void upgradeTls(HttpClientContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cancellable execute(final String id, final AsyncClientExchangeHandler exchangeHandler, final HttpClientContext context) {
        final ComplexCancellable complexCancellable = new ComplexCancellable();
        final Endpoint endpoint = this.ensureValid();
        final IOSession session = endpoint.session;
        if (session.isOpen()) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: start execution {}", (Object)ConnPoolSupport.getId(endpoint), (Object)id);
            }
            session.enqueue(new RequestExecutionCommand(exchangeHandler, this.pushHandlerFactory, complexCancellable, context), Command.Priority.NORMAL);
        } else {
            final HttpHost target = endpoint.target;
            RequestConfig requestConfig = context.getRequestConfig();
            Timeout connectTimeout = requestConfig.getConnectTimeout();
            this.connPool.getSession((Object)target, connectTimeout, (FutureCallback)new FutureCallback<IOSession>(){

                @Override
                public void completed(IOSession ioSession) {
                    InternalH2AsyncExecRuntime.this.sessionRef.set(new Endpoint(target, ioSession));
                    InternalH2AsyncExecRuntime.this.reusable = true;
                    if (InternalH2AsyncExecRuntime.this.log.isDebugEnabled()) {
                        InternalH2AsyncExecRuntime.this.log.debug("{}: start execution {}", (Object)ConnPoolSupport.getId(endpoint), (Object)id);
                    }
                    session.enqueue(new RequestExecutionCommand(exchangeHandler, InternalH2AsyncExecRuntime.this.pushHandlerFactory, complexCancellable, context), Command.Priority.NORMAL);
                }

                @Override
                public void failed(Exception ex) {
                    exchangeHandler.failed(ex);
                }

                @Override
                public void cancelled() {
                    exchangeHandler.failed(new InterruptedIOException());
                }
            });
        }
        return complexCancellable;
    }

    @Override
    public void markConnectionReusable(Object newState, TimeValue newValidDuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markConnectionNonReusable() {
        this.reusable = false;
    }

    @Override
    public AsyncExecRuntime fork() {
        return new InternalH2AsyncExecRuntime(this.log, this.connPool, this.pushHandlerFactory);
    }

    static class Endpoint
    implements Identifiable {
        final HttpHost target;
        final IOSession session;

        Endpoint(HttpHost target, IOSession session) {
            this.target = target;
            this.session = session;
        }

        @Override
        public String getId() {
            return this.session.getId();
        }
    }
}

