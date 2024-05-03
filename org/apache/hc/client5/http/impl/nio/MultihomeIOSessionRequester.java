/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.ConnectExceptionSupport;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MultihomeIOSessionRequester {
    private static final Logger LOG = LoggerFactory.getLogger(MultihomeIOSessionRequester.class);
    private final DnsResolver dnsResolver;

    MultihomeIOSessionRequester(DnsResolver dnsResolver) {
        this.dnsResolver = dnsResolver != null ? dnsResolver : SystemDefaultDnsResolver.INSTANCE;
    }

    public Future<IOSession> connect(final ConnectionInitiator connectionInitiator, final NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, final SocketAddress localAddress, final Timeout connectTimeout, final Object attachment, FutureCallback<IOSession> callback) {
        InetAddress[] remoteAddresses;
        if (remoteAddress != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: connecting {} to {} ({})", remoteEndpoint, localAddress, remoteAddress, connectTimeout);
            }
            return connectionInitiator.connect(remoteEndpoint, remoteAddress, localAddress, connectTimeout, attachment, callback);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: resolving remote address", (Object)remoteEndpoint);
        }
        final ComplexFuture<IOSession> future = new ComplexFuture<IOSession>(callback);
        try {
            remoteAddresses = this.dnsResolver.resolve(remoteEndpoint.getHostName());
        } catch (UnknownHostException ex) {
            future.failed(ex);
            return future;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: resolved to {}", (Object)remoteEndpoint, (Object)Arrays.asList(remoteAddresses));
        }
        Runnable runnable = new Runnable(){
            private final AtomicInteger attempt = new AtomicInteger(0);

            void executeNext() {
                int index = this.attempt.getAndIncrement();
                final InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddresses[index], remoteEndpoint.getPort());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: connecting {} to {} ({})", remoteEndpoint, localAddress, remoteAddress, connectTimeout);
                }
                Future<IOSession> sessionFuture = connectionInitiator.connect(remoteEndpoint, remoteAddress, localAddress, connectTimeout, attachment, new FutureCallback<IOSession>(){

                    @Override
                    public void completed(IOSession session) {
                        if (LOG.isDebugEnabled() && LOG.isDebugEnabled()) {
                            LOG.debug("{}: connected {} {}->{}", remoteEndpoint, session.getId(), session.getLocalAddress(), session.getRemoteAddress());
                        }
                        future.completed(session);
                    }

                    @Override
                    public void failed(Exception cause) {
                        if (attempt.get() >= remoteAddresses.length) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: connection to {} failed ({}); terminating operation", remoteEndpoint, remoteAddress, cause.getClass());
                            }
                            if (cause instanceof IOException) {
                                future.failed(ConnectExceptionSupport.enhance((IOException)cause, remoteEndpoint, remoteAddresses));
                            } else {
                                future.failed(cause);
                            }
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: connection to {} failed ({}); retrying connection to the next address", remoteEndpoint, remoteAddress, cause.getClass());
                            }
                            this.executeNext();
                        }
                    }

                    @Override
                    public void cancelled() {
                        future.cancel();
                    }
                });
                future.setDependency(sessionFuture);
            }

            @Override
            public void run() {
                this.executeNext();
            }
        };
        runnable.run();
        return future;
    }

    public Future<IOSession> connect(ConnectionInitiator connectionInitiator, NamedEndpoint remoteEndpoint, SocketAddress localAddress, Timeout connectTimeout, Object attachment, FutureCallback<IOSession> callback) {
        return this.connect(connectionInitiator, remoteEndpoint, null, localAddress, connectTimeout, attachment, callback);
    }
}

