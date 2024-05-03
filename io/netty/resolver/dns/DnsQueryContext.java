/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.dns.AbstractDnsOptPseudoRrRecord;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

abstract class DnsQueryContext
implements FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsQueryContext.class);
    private final DnsNameResolver parent;
    private final Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise;
    private final int id;
    private final DnsQuestion question;
    private final DnsRecord[] additionals;
    private final DnsRecord optResource;
    private final InetSocketAddress nameServerAddr;
    private final boolean recursionDesired;
    private volatile ScheduledFuture<?> timeoutFuture;

    DnsQueryContext(DnsNameResolver parent, InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise) {
        this.parent = ObjectUtil.checkNotNull(parent, "parent");
        this.nameServerAddr = ObjectUtil.checkNotNull(nameServerAddr, "nameServerAddr");
        this.question = ObjectUtil.checkNotNull(question, "question");
        this.additionals = ObjectUtil.checkNotNull(additionals, "additionals");
        this.promise = ObjectUtil.checkNotNull(promise, "promise");
        this.recursionDesired = parent.isRecursionDesired();
        this.id = parent.queryContextManager.add(this);
        promise.addListener(this);
        this.optResource = parent.isOptResourceEnabled() ? new AbstractDnsOptPseudoRrRecord(parent.maxPayloadSize(), 0, 0){} : null;
    }

    InetSocketAddress nameServerAddr() {
        return this.nameServerAddr;
    }

    DnsQuestion question() {
        return this.question;
    }

    DnsNameResolver parent() {
        return this.parent;
    }

    protected abstract DnsQuery newQuery(int var1);

    protected abstract Channel channel();

    protected abstract String protocol();

    void query(boolean flush, ChannelPromise writePromise) {
        DnsQuestion question = this.question();
        InetSocketAddress nameServerAddr = this.nameServerAddr();
        DnsQuery query = this.newQuery(this.id);
        query.setRecursionDesired(this.recursionDesired);
        query.addRecord(DnsSection.QUESTION, question);
        for (DnsRecord record : this.additionals) {
            query.addRecord(DnsSection.ADDITIONAL, record);
        }
        if (this.optResource != null) {
            query.addRecord(DnsSection.ADDITIONAL, this.optResource);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} WRITE: {}, [{}: {}], {}", this.channel(), this.protocol(), this.id, nameServerAddr, question);
        }
        this.sendQuery(query, flush, writePromise);
    }

    private void sendQuery(final DnsQuery query, boolean flush, final ChannelPromise writePromise) {
        if (this.parent.channelFuture.isDone()) {
            this.writeQuery(query, flush, writePromise);
        } else {
            this.parent.channelFuture.addListener((GenericFutureListener<Future<Channel>>)new GenericFutureListener<Future<? super Channel>>(){

                @Override
                public void operationComplete(Future<? super Channel> future) {
                    if (future.isSuccess()) {
                        DnsQueryContext.this.writeQuery(query, true, writePromise);
                    } else {
                        Throwable cause = future.cause();
                        DnsQueryContext.this.promise.tryFailure(cause);
                        writePromise.setFailure(cause);
                    }
                }
            });
        }
    }

    private void writeQuery(DnsQuery query, boolean flush, ChannelPromise writePromise) {
        ChannelFuture writeFuture;
        ChannelFuture channelFuture = writeFuture = flush ? this.channel().writeAndFlush(query, writePromise) : this.channel().write(query, writePromise);
        if (writeFuture.isDone()) {
            this.onQueryWriteCompletion(writeFuture);
        } else {
            writeFuture.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) {
                    DnsQueryContext.this.onQueryWriteCompletion(writeFuture);
                }
            });
        }
    }

    private void onQueryWriteCompletion(ChannelFuture writeFuture) {
        if (!writeFuture.isSuccess()) {
            this.tryFailure("failed to send a query via " + this.protocol(), writeFuture.cause(), false);
            return;
        }
        final long queryTimeoutMillis = this.parent.queryTimeoutMillis();
        if (queryTimeoutMillis > 0L) {
            this.timeoutFuture = this.parent.ch.eventLoop().schedule(new Runnable(){

                @Override
                public void run() {
                    if (DnsQueryContext.this.promise.isDone()) {
                        return;
                    }
                    DnsQueryContext.this.tryFailure("query via " + DnsQueryContext.this.protocol() + " timed out after " + queryTimeoutMillis + " milliseconds", null, true);
                }
            }, queryTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    void finish(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
        DnsResponse res = envelope.content();
        if (res.count(DnsSection.QUESTION) != 1) {
            logger.warn("Received a DNS response with invalid number of questions: {}", (Object)envelope);
        } else if (!this.question().equals(res.recordAt(DnsSection.QUESTION))) {
            logger.warn("Received a mismatching DNS response: {}", (Object)envelope);
        } else if (this.trySuccess(envelope)) {
            return;
        }
        envelope.release();
    }

    private boolean trySuccess(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
        return this.promise.trySuccess(envelope);
    }

    boolean tryFailure(String message, Throwable cause, boolean timeout) {
        if (this.promise.isDone()) {
            return false;
        }
        InetSocketAddress nameServerAddr = this.nameServerAddr();
        StringBuilder buf = new StringBuilder(message.length() + 64);
        buf.append('[').append(nameServerAddr).append("] ").append(message).append(" (no stack trace available)");
        DnsNameResolverException e = timeout ? new DnsNameResolverTimeoutException(nameServerAddr, this.question(), buf.toString()) : new DnsNameResolverException(nameServerAddr, this.question(), buf.toString(), cause);
        return this.promise.tryFailure(e);
    }

    @Override
    public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
        ScheduledFuture<?> timeoutFuture = this.timeoutFuture;
        if (timeoutFuture != null) {
            this.timeoutFuture = null;
            timeoutFuture.cancel(false);
        }
        this.parent.queryContextManager.remove(this.nameServerAddr, this.id);
    }
}

