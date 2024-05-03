/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.classic.RequestFailedException;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

class InternalExecRuntime
implements ExecRuntime,
Cancellable {
    private final Logger log;
    private final HttpClientConnectionManager manager;
    private final HttpRequestExecutor requestExecutor;
    private final CancellableDependency cancellableDependency;
    private final AtomicReference<ConnectionEndpoint> endpointRef;
    private volatile boolean reusable;
    private volatile Object state;
    private volatile TimeValue validDuration;

    InternalExecRuntime(Logger log, HttpClientConnectionManager manager, HttpRequestExecutor requestExecutor, CancellableDependency cancellableDependency) {
        this.log = log;
        this.manager = manager;
        this.requestExecutor = requestExecutor;
        this.cancellableDependency = cancellableDependency;
        this.endpointRef = new AtomicReference<Object>(null);
        this.validDuration = TimeValue.NEG_ONE_MILLISECOND;
    }

    @Override
    public boolean isExecutionAborted() {
        return this.cancellableDependency != null && this.cancellableDependency.isCancelled();
    }

    @Override
    public boolean isEndpointAcquired() {
        return this.endpointRef.get() != null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void acquireEndpoint(String id, HttpRoute route, Object object, HttpClientContext context) throws IOException {
        Args.notNull(route, "Route");
        if (this.endpointRef.get() != null) throw new IllegalStateException("Endpoint already acquired");
        RequestConfig requestConfig = context.getRequestConfig();
        Timeout connectionRequestTimeout = requestConfig.getConnectionRequestTimeout();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: acquiring endpoint ({})", (Object)id, (Object)connectionRequestTimeout);
        }
        LeaseRequest connRequest = this.manager.lease(id, route, connectionRequestTimeout, object);
        this.state = object;
        if (this.cancellableDependency != null) {
            if (this.cancellableDependency.isCancelled()) {
                connRequest.cancel();
                throw new RequestFailedException("Request aborted");
            }
            this.cancellableDependency.setDependency(connRequest);
        }
        try {
            ConnectionEndpoint connectionEndpoint = connRequest.get(connectionRequestTimeout);
            this.endpointRef.set(connectionEndpoint);
            this.reusable = connectionEndpoint.isConnected();
            if (this.cancellableDependency != null) {
                this.cancellableDependency.setDependency(this);
            }
            if (!this.log.isDebugEnabled()) return;
            this.log.debug("{}: acquired endpoint {}", (Object)id, (Object)ConnPoolSupport.getId(connectionEndpoint));
            return;
        } catch (TimeoutException ex) {
            throw new ConnectionRequestTimeoutException(ex.getMessage());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RequestFailedException("Request aborted", interrupted);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause != null) throw new RequestFailedException("Request execution failed", cause);
            cause = ex;
            throw new RequestFailedException("Request execution failed", cause);
        }
    }

    ConnectionEndpoint ensureValid() {
        ConnectionEndpoint endpoint = this.endpointRef.get();
        if (endpoint == null) {
            throw new IllegalStateException("Endpoint not acquired / already released");
        }
        return endpoint;
    }

    @Override
    public boolean isEndpointConnected() {
        ConnectionEndpoint endpoint = this.endpointRef.get();
        return endpoint != null && endpoint.isConnected();
    }

    private void connectEndpoint(ConnectionEndpoint endpoint, HttpClientContext context) throws IOException {
        if (this.cancellableDependency != null && this.cancellableDependency.isCancelled()) {
            throw new RequestFailedException("Request aborted");
        }
        RequestConfig requestConfig = context.getRequestConfig();
        Timeout connectTimeout = requestConfig.getConnectTimeout();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: connecting endpoint ({})", (Object)ConnPoolSupport.getId(endpoint), (Object)connectTimeout);
        }
        this.manager.connect(endpoint, connectTimeout, context);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: endpoint connected", (Object)ConnPoolSupport.getId(endpoint));
        }
    }

    @Override
    public void connectEndpoint(HttpClientContext context) throws IOException {
        ConnectionEndpoint endpoint = this.ensureValid();
        if (!endpoint.isConnected()) {
            this.connectEndpoint(endpoint, context);
        }
    }

    @Override
    public void disconnectEndpoint() throws IOException {
        ConnectionEndpoint endpoint = this.endpointRef.get();
        if (endpoint != null) {
            endpoint.close();
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: endpoint closed", (Object)ConnPoolSupport.getId(endpoint));
            }
        }
    }

    @Override
    public void upgradeTls(HttpClientContext context) throws IOException {
        ConnectionEndpoint endpoint = this.ensureValid();
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: upgrading endpoint", (Object)ConnPoolSupport.getId(endpoint));
        }
        this.manager.upgrade(endpoint, context);
    }

    @Override
    public ClassicHttpResponse execute(String id, ClassicHttpRequest request, HttpClientContext context) throws IOException, HttpException {
        RequestConfig requestConfig;
        Timeout responseTimeout;
        ConnectionEndpoint endpoint = this.ensureValid();
        if (!endpoint.isConnected()) {
            this.connectEndpoint(endpoint, context);
        }
        if ((responseTimeout = (requestConfig = context.getRequestConfig()).getResponseTimeout()) != null) {
            endpoint.setSocketTimeout(responseTimeout);
        }
        if (this.log.isDebugEnabled()) {
            this.log.debug("{}: start execution {}", (Object)ConnPoolSupport.getId(endpoint), (Object)id);
        }
        return endpoint.execute(id, request, this.requestExecutor, context);
    }

    @Override
    public boolean isConnectionReusable() {
        return this.reusable;
    }

    @Override
    public void markConnectionReusable(Object state, TimeValue validDuration) {
        this.reusable = true;
        this.state = state;
        this.validDuration = validDuration;
    }

    @Override
    public void markConnectionNonReusable() {
        this.reusable = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void discardEndpoint(ConnectionEndpoint endpoint) {
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
        ConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
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
        ConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
        if (endpoint != null) {
            this.discardEndpoint(endpoint);
        }
    }

    @Override
    public boolean cancel() {
        boolean alreadyReleased = this.endpointRef.get() == null;
        ConnectionEndpoint endpoint = this.endpointRef.getAndSet(null);
        if (endpoint != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("{}: cancel", (Object)ConnPoolSupport.getId(endpoint));
            }
            this.discardEndpoint(endpoint);
        }
        return !alreadyReleased;
    }

    @Override
    public ExecRuntime fork(CancellableDependency cancellableDependency) {
        return new InternalExecRuntime(this.log, this.manager, this.requestExecutor, cancellableDependency);
    }
}

