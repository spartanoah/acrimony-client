/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.impl.RequestCopier;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.NoopEntityConsumer;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public final class AsyncHttpRequestRetryExec
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncHttpRequestRetryExec.class);
    private final HttpRequestRetryStrategy retryStrategy;

    public AsyncHttpRequestRetryExec(HttpRequestRetryStrategy retryStrategy) {
        Args.notNull(retryStrategy, "retryStrategy");
        this.retryStrategy = retryStrategy;
    }

    private void internalExecute(final State state, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        final String exchangeId = scope.exchangeId;
        chain.proceed(RequestCopier.INSTANCE.copy(request), entityProducer, scope, new AsyncExecCallback(){

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                HttpClientContext clientContext = scope.clientContext;
                if (entityProducer != null && !entityProducer.isRepeatable()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: cannot retry non-repeatable request", (Object)exchangeId);
                    }
                    return asyncExecCallback.handleResponse(response, entityDetails);
                }
                state.retrying = AsyncHttpRequestRetryExec.this.retryStrategy.retryRequest(response, state.execCount, clientContext);
                if (state.retrying) {
                    return new NoopEntityConsumer();
                }
                return asyncExecCallback.handleResponse(response, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                asyncExecCallback.handleInformationResponse(response);
            }

            @Override
            public void completed() {
                if (state.retrying) {
                    ++state.execCount;
                    try {
                        AsyncHttpRequestRetryExec.this.internalExecute(state, request, entityProducer, scope, chain, asyncExecCallback);
                    } catch (IOException | HttpException ex) {
                        asyncExecCallback.failed(ex);
                    }
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                if (cause instanceof IOException) {
                    HttpRoute route = scope.route;
                    HttpClientContext clientContext = scope.clientContext;
                    if (entityProducer != null && !entityProducer.isRepeatable()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: cannot retry non-repeatable request", (Object)exchangeId);
                        }
                    } else if (AsyncHttpRequestRetryExec.this.retryStrategy.retryRequest(request, (IOException)cause, state.execCount, clientContext)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("{}: {}", exchangeId, cause.getMessage(), cause);
                        }
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Recoverable I/O exception ({}) caught when processing request to {}", (Object)cause.getClass().getName(), (Object)route);
                        }
                        scope.execRuntime.discardEndpoint();
                        if (entityProducer != null) {
                            entityProducer.releaseResources();
                        }
                        state.retrying = true;
                        ++state.execCount;
                        try {
                            AsyncHttpRequestRetryExec.this.internalExecute(state, request, entityProducer, scope, chain, asyncExecCallback);
                        } catch (IOException | HttpException ex) {
                            asyncExecCallback.failed(ex);
                        }
                        return;
                    }
                }
                asyncExecCallback.failed(cause);
            }
        });
    }

    @Override
    public void execute(HttpRequest request, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        State state = new State();
        state.execCount = 1;
        state.retrying = false;
        this.internalExecute(state, request, entityProducer, scope, chain, asyncExecCallback);
    }

    private static class State {
        volatile int execCount;
        volatile boolean retrying;

        private State() {
        }
    }
}

