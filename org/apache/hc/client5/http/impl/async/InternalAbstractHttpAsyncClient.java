/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.RequestCopier;
import org.apache.hc.client5.http.impl.async.AbstractHttpAsyncClientBase;
import org.apache.hc.client5.http.impl.async.AsyncExecChainElement;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class InternalAbstractHttpAsyncClient
extends AbstractHttpAsyncClientBase {
    private static final Logger LOG = LoggerFactory.getLogger(InternalAbstractHttpAsyncClient.class);
    private final AsyncExecChainElement execChain;
    private final Lookup<CookieSpecFactory> cookieSpecRegistry;
    private final Lookup<AuthSchemeFactory> authSchemeRegistry;
    private final CookieStore cookieStore;
    private final CredentialsProvider credentialsProvider;
    private final RequestConfig defaultConfig;
    private final ConcurrentLinkedQueue<Closeable> closeables;

    InternalAbstractHttpAsyncClient(DefaultConnectingIOReactor ioReactor, AsyncPushConsumerRegistry pushConsumerRegistry, ThreadFactory threadFactory, AsyncExecChainElement execChain, Lookup<CookieSpecFactory> cookieSpecRegistry, Lookup<AuthSchemeFactory> authSchemeRegistry, CookieStore cookieStore, CredentialsProvider credentialsProvider, RequestConfig defaultConfig, List<Closeable> closeables) {
        super(ioReactor, pushConsumerRegistry, threadFactory);
        this.execChain = execChain;
        this.cookieSpecRegistry = cookieSpecRegistry;
        this.authSchemeRegistry = authSchemeRegistry;
        this.cookieStore = cookieStore;
        this.credentialsProvider = credentialsProvider;
        this.defaultConfig = defaultConfig;
        this.closeables = closeables != null ? new ConcurrentLinkedQueue<Closeable>(closeables) : null;
    }

    @Override
    void internalClose(CloseMode closeMode) {
        if (this.closeables != null) {
            Closeable closeable;
            while ((closeable = this.closeables.poll()) != null) {
                try {
                    if (closeable instanceof ModalCloseable) {
                        ((ModalCloseable)closeable).close(closeMode);
                        continue;
                    }
                    closeable.close();
                } catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private void setupContext(HttpClientContext context) {
        if (context.getAttribute("http.authscheme-registry") == null) {
            context.setAttribute("http.authscheme-registry", this.authSchemeRegistry);
        }
        if (context.getAttribute("http.cookiespec-registry") == null) {
            context.setAttribute("http.cookiespec-registry", this.cookieSpecRegistry);
        }
        if (context.getAttribute("http.cookie-store") == null) {
            context.setAttribute("http.cookie-store", this.cookieStore);
        }
        if (context.getAttribute("http.auth.credentials-provider") == null) {
            context.setAttribute("http.auth.credentials-provider", this.credentialsProvider);
        }
        if (context.getAttribute("http.request-config") == null) {
            context.setAttribute("http.request-config", this.defaultConfig);
        }
    }

    abstract AsyncExecRuntime createAsyncExecRuntime(HandlerFactory<AsyncPushConsumer> var1);

    abstract HttpRoute determineRoute(HttpHost var1, HttpClientContext var2) throws HttpException;

    @Override
    protected <T> Future<T> doExecute(final HttpHost httpHost, final AsyncRequestProducer requestProducer, final AsyncResponseConsumer<T> responseConsumer, final HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context, FutureCallback<T> callback) {
        final ComplexFuture<T> future = new ComplexFuture<T>(callback);
        try {
            if (!this.isRunning()) {
                throw new CancellationException("Request execution cancelled");
            }
            final HttpClientContext clientContext = context != null ? HttpClientContext.adapt(context) : HttpClientContext.create();
            requestProducer.sendRequest(new RequestChannel(){

                @Override
                public void sendRequest(HttpRequest request, final EntityDetails entityDetails, final HttpContext context) throws HttpException, IOException {
                    RequestConfig requestConfig = null;
                    if (request instanceof Configurable) {
                        requestConfig = ((Configurable)((Object)request)).getConfig();
                    }
                    if (requestConfig != null) {
                        clientContext.setRequestConfig(requestConfig);
                    }
                    HttpRoute route = InternalAbstractHttpAsyncClient.this.determineRoute(httpHost != null ? httpHost : RoutingSupport.determineHost(request), clientContext);
                    final String exchangeId = ExecSupport.getNextExchangeId();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: preparing request execution", (Object)exchangeId);
                    }
                    final AsyncExecRuntime execRuntime = InternalAbstractHttpAsyncClient.this.createAsyncExecRuntime(pushHandlerFactory);
                    InternalAbstractHttpAsyncClient.this.setupContext(clientContext);
                    AsyncExecChain.Scope scope = new AsyncExecChain.Scope(exchangeId, route, request, future, clientContext, execRuntime);
                    final AtomicBoolean outputTerminated = new AtomicBoolean(false);
                    InternalAbstractHttpAsyncClient.this.execChain.execute(RequestCopier.INSTANCE.copy(request), entityDetails != null ? new AsyncEntityProducer(){

                        @Override
                        public void releaseResources() {
                            requestProducer.releaseResources();
                        }

                        @Override
                        public void failed(Exception cause) {
                            requestProducer.failed(cause);
                        }

                        @Override
                        public boolean isRepeatable() {
                            return requestProducer.isRepeatable();
                        }

                        @Override
                        public long getContentLength() {
                            return entityDetails.getContentLength();
                        }

                        @Override
                        public String getContentType() {
                            return entityDetails.getContentType();
                        }

                        @Override
                        public String getContentEncoding() {
                            return entityDetails.getContentEncoding();
                        }

                        @Override
                        public boolean isChunked() {
                            return entityDetails.isChunked();
                        }

                        @Override
                        public Set<String> getTrailerNames() {
                            return entityDetails.getTrailerNames();
                        }

                        @Override
                        public int available() {
                            return requestProducer.available();
                        }

                        @Override
                        public void produce(DataStreamChannel channel) throws IOException {
                            if (outputTerminated.get()) {
                                channel.endStream();
                                return;
                            }
                            requestProducer.produce(channel);
                        }
                    } : null, scope, new AsyncExecCallback(){

                        @Override
                        public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                            if (response.getCode() >= 400) {
                                outputTerminated.set(true);
                                requestProducer.releaseResources();
                            }
                            responseConsumer.consumeResponse(response, entityDetails, context, new FutureCallback<T>(){

                                @Override
                                public void completed(T result) {
                                    future.completed(result);
                                }

                                @Override
                                public void failed(Exception ex) {
                                    future.failed(ex);
                                }

                                @Override
                                public void cancelled() {
                                    future.cancel();
                                }
                            });
                            return responseConsumer;
                        }

                        @Override
                        public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                            responseConsumer.informationResponse(response, context);
                        }

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void completed() {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: message exchange successfully completed", (Object)exchangeId);
                            }
                            try {
                                execRuntime.releaseEndpoint();
                            } finally {
                                responseConsumer.releaseResources();
                                requestProducer.releaseResources();
                            }
                        }

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void failed(Exception cause) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: request failed: {}", (Object)exchangeId, (Object)cause.getMessage());
                            }
                            try {
                                execRuntime.discardEndpoint();
                                responseConsumer.failed(cause);
                            } finally {
                                try {
                                    future.failed(cause);
                                } finally {
                                    responseConsumer.releaseResources();
                                    requestProducer.releaseResources();
                                }
                            }
                        }
                    });
                }
            }, context);
        } catch (IOException | IllegalStateException | HttpException ex) {
            future.failed(ex);
        }
        return future;
    }
}

