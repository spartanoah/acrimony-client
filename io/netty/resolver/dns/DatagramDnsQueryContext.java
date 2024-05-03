/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsQueryContext;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;

final class DatagramDnsQueryContext
extends DnsQueryContext {
    DatagramDnsQueryContext(DnsNameResolver parent, InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise) {
        super(parent, nameServerAddr, question, additionals, promise);
    }

    @Override
    protected DnsQuery newQuery(int id) {
        return new DatagramDnsQuery(null, this.nameServerAddr(), id);
    }

    @Override
    protected Channel channel() {
        return this.parent().ch;
    }

    @Override
    protected String protocol() {
        return "UDP";
    }
}

