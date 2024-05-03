/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.HttpVersionPolicy
 */
package org.apache.hc.client5.http.impl.async;

import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

class InternalHttpAsyncExecRuntime
implements AsyncExecRuntime {
    private final Logger log;
    private final AsyncClientConnectionManager manager;
    private final ConnectionInitiator connectionInitiator;
    private final HandlerFactory<AsyncPushConsumer> pushHandlerFactory;
    private final HttpVersionPolicy versionPolicy;
    private final AtomicReference<AsyncConnectionEndpoint> endpointRef;
    private volatile boolean reusable;
    private volatile Object state;
    private volatile TimeValue validDuration;

    InternalHttpAsyncExecRuntime(Logger log, AsyncClientConnectionManager manager, ConnectionInitiator connectionInitiator, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpVersionPolicy versionPolicy) {
        this.log = log;
        this.manager = manager;
        this.connectionInitiator = connectionInitiator;
        this.pushHandlerFactory = pushHandlerFactory;
        this.versionPolicy = versionPolicy;
        this.endpointRef = new AtomicReference<Object>(null);
        this.validDuration = TimeValue.NEG_ONE_MILLISECOND;
    }

    @Override
    public boolean isEndpointAcquired() {
        return this.endpointRef.get() != null;
    }

    @Override
    public Cancellable acquireEndpoint(final String id, HttpRoute route, Object object, HttpClientContext context, final FutureCallback<AsyncExecRuntime> callback) {
        if (this.endpointRef.get() == null) {
            this.state = object;
            RequestConfig requestConfig = context.getRequestConfig();
            Timeout connectionRequestTimeout = requestConfig.getConnectionRequestTimeout();
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: acquiring endpoint ({})", (Object)id, (Object)connectionRequestTimeout);
            }
            return Operations.cancellable(this.manager.lease(id, route, object, connectionRequestTimeout, new FutureCallback<AsyncConnectionEndpoint>(){

                @Override
                public void completed(AsyncConnectionEndpoint connectionEndpoint) {
                    InternalHttpAsyncExecRuntime.this.endpointRef.set(connectionEndpoint);
                    InternalHttpAsyncExecRuntime.this.reusable = connectionEndpoint.isConnected();
                    if (InternalHttpAsyncExecRuntime.this.log.isDebugEnabled()) {
                        InternalHttpAsyncExecRuntime.this.log.debug("{}: acquired endpoint {}", (Object)id, (Object)ConnPoolSupport.getId(connectionEndpoint));
                    }
                    callback.completed(InternalHttpAsyncExecRuntime.this);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void discardEndpoint(AsyncConnectionEndpoint endpoint) {
        try {
            endpoint.close(CloseMode.IMMEDIATE);
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: endpoint closed", (Object)ConnPoolSupport.getId(endpoint));
            }
        } finally {
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: discarding endpoint", (Object)ConnPoolSupport.getId(endpoint));
            }
            this.manager.release(endpoint, null, TimeValue.ZERO_MILLISECONDS);
        }
    }

    @Override
    public void releaseEndpoint() {
        AsyncConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
        if (endpoint != null) {
            if (this.reusable) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("{}: releasing valid endpoint", (Object)ConnPoolSupport.getId(endpoint));
                }
                this.manager.release(endpoint, this.state, this.validDuration);
            } else {
                this.discardEndpoint(endpoint);
            }
        }
    }

    @Override
    public void discardEndpoint() {
        AsyncConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
        if (endpoint != null) {
            this.discardEndpoint(endpoint);
        }
    }

    @Override
    public boolean validateConnection() {
        if (this.reusable) {
            AsyncConnectionEndpoint endpoint = this.endpointRef.get();
            return endpoint != null && endpoint.isConnected();
        }
        AsyncConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
        if (endpoint != null) {
            this.discardEndpoint(endpoint);
        }
        return false;
    }

    AsyncConnectionEndpoint ensureValid() {
        AsyncConnectionEndpoint endpoint = this.endpointRef.get();
        if (endpoint == null) {
            throw new IllegalStateException("Endpoint not acquired / already released");
        }
        return endpoint;
    }

    @Override
    public boolean isEndpointConnected() {
        AsyncConnectionEndpoint endpoint = this.endpointRef.get();
        return endpoint != null && endpoint.isConnected();
    }

    @Override
    public Cancellable connectEndpoint(HttpClientContext context, final FutureCallback<AsyncExecRuntime> callback) {
        AsyncConnectionEndpoint endpoint = this.ensureValid();
        if (endpoint.isConnected()) {
            callback.completed(this);
            return Operations.nonCancellable();
        }
        RequestConfig requestConfig = context.getRequestConfig();
        Timeout connectTimeout = requestConfig.getConnectTimeout();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: connecting endpoint ({})", (Object)ConnPoolSupport.getId(endpoint), (Object)connectTimeout);
        }
        return Operations.cancellable(this.manager.connect(endpoint, this.connectionInitiator, connectTimeout, this.versionPolicy, context, new FutureCallback<AsyncConnectionEndpoint>(){

            @Override
            public void completed(AsyncConnectionEndpoint endpoint) {
                if (InternalHttpAsyncExecRuntime.this.log.isDebugEnabled()) {
                    InternalHttpAsyncExecRuntime.this.log.debug("{}: endpoint connected", (Object)ConnPoolSupport.getId(endpoint));
                }
                callback.completed(InternalHttpAsyncExecRuntime.this);
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
        AsyncConnectionEndpoint endpoint = this.ensureValid();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: upgrading endpoint", (Object)ConnPoolSupport.getId(endpoint));
        }
        this.manager.upgrade(endpoint, this.versionPolicy, context);
    }

    @Override
    public Cancellable execute(final String id, final AsyncClientExchangeHandler exchangeHandler, final HttpClientContext context) {
        final AsyncConnectionEndpoint endpoint = this.ensureValid();
        if (endpoint.isConnected()) {
            RequestConfig requestConfig;
            Timeout responseTimeout;
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: start execution {}", (Object)ConnPoolSupport.getId(endpoint), (Object)id);
            }
            if ((responseTimeout = (requestConfig = context.getRequestConfig()).getResponseTimeout()) != null) {
                endpoint.setSocketTimeout(responseTimeout);
            }
            endpoint.execute(id, exchangeHandler, context);
            if (context.getRequestConfig().isHardCancellationEnabled()) {
                return new Cancellable(){

                    @Override
                    public boolean cancel() {
                        exchangeHandler.cancel();
                        return true;
                    }
                };
            }
        } else {
            this.connectEndpoint(context, new FutureCallback<AsyncExecRuntime>(){

                @Override
                public void completed(AsyncExecRuntime runtime) {
                    if (InternalHttpAsyncExecRuntime.this.log.isDebugEnabled()) {
                        InternalHttpAsyncExecRuntime.this.log.debug("{}: start execution {}", (Object)ConnPoolSupport.getId(endpoint), (Object)id);
                    }
                    try {
                        endpoint.execute(id, exchangeHandler, InternalHttpAsyncExecRuntime.this.pushHandlerFactory, context);
                    } catch (RuntimeException ex) {
                        this.failed(ex);
                    }
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
        return Operations.nonCancellable();
    }

    @Override
    public void markConnectionReusable(Object newState, TimeValue newValidDuration) {
        this.reusable = true;
        this.state = newState;
        this.validDuration = newValidDuration;
    }

    @Override
    public void markConnectionNonReusable() {
        this.reusable = false;
        this.state = null;
        this.validDuration = null;
    }

    @Override
    public AsyncExecRuntime fork() {
        return new InternalHttpAsyncExecRuntime(this.log, this.manager, this.connectionInitiator, this.pushHandlerFactory, this.versionPolicy);
    }
}

