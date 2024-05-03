/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.handler.codec.dns.DefaultDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsQueryContext;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;

final class TcpDnsQueryContext
extends DnsQueryContext {
    private final Channel channel;

    TcpDnsQueryContext(DnsNameResolver parent, Channel channel, InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise) {
        super(parent, nameServerAddr, question, additionals, promise);
        this.channel = channel;
    }

    @Override
    protected DnsQuery newQuery(int id) {
        return new DefaultDnsQuery(id);
    }

    @Override
    protected Channel channel() {
        return this.channel;
    }

    @Override
    protected String protocol() {
        return "TCP";
    }
}

