/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.nio.pool.H2ConnPool
 */
package org.apache.hc.client5.http.impl.async;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.async.AsyncExecChainElement;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.impl.async.InternalAbstractHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.InternalH2AsyncExecRuntime;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http2.nio.pool.H2ConnPool;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
@Internal
public final class InternalH2AsyncClient
extends InternalAbstractHttpAsyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(InternalH2AsyncClient.class);
    private final HttpRoutePlanner routePlanner;
    private final H2ConnPool connPool;

    InternalH2AsyncClient(DefaultConnectingIOReactor ioReactor, AsyncExecChainElement execChain, AsyncPushConsumerRegistry pushConsumerRegistry, ThreadFactory threadFactory, H2ConnPool connPool, HttpRoutePlanner routePlanner, Lookup<CookieSpecFactory> cookieSpecRegistry, Lookup<AuthSchemeFactory> authSchemeRegistry, CookieStore cookieStore, CredentialsProvider credentialsProvider, RequestConfig defaultConfig, List<Closeable> closeables) {
        super(ioReactor, pushConsumerRegistry, threadFactory, execChain, cookieSpecRegistry, authSchemeRegistry, cookieStore, credentialsProvider, defaultConfig, closeables);
        this.connPool = connPool;
        this.routePlanner = routePlanner;
    }

    @Override
    AsyncExecRuntime createAsyncExecRuntime(HandlerFactory<AsyncPushConsumer> pushHandlerFactory) {
        return new InternalH2AsyncExecRuntime(LOG, this.connPool, pushHandlerFactory);
    }

    @Override
    HttpRoute determineRoute(HttpHost httpHost, HttpClientContext clientContext) throws HttpException {
        HttpRoute route = this.routePlanner.determineRoute(httpHost, clientContext);
        if (route.isTunnelled()) {
            throw new HttpException("HTTP/2 tunneling not supported");
        }
        return route;
    }
}

