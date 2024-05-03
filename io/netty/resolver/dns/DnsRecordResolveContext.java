/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.resolver.dns.DnsCnameCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsResolveContext;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.NoopDnsCnameCache;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import java.net.UnknownHostException;
import java.util.List;

final class DnsRecordResolveContext
extends DnsResolveContext<DnsRecord> {
    DnsRecordResolveContext(DnsNameResolver parent, Promise<?> originalPromise, DnsQuestion question, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        this(parent, originalPromise, question.name(), question.dnsClass(), new DnsRecordType[]{question.type()}, additionals, nameServerAddrs, allowedQueries);
    }

    private DnsRecordResolveContext(DnsNameResolver parent, Promise<?> originalPromise, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        super(parent, originalPromise, hostname, dnsClass, expectedTypes, additionals, nameServerAddrs, allowedQueries);
    }

    @Override
    DnsResolveContext<DnsRecord> newResolverContext(DnsNameResolver parent, Promise<?> originalPromise, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        return new DnsRecordResolveContext(parent, originalPromise, hostname, dnsClass, expectedTypes, additionals, nameServerAddrs, allowedQueries);
    }

    @Override
    DnsRecord convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop) {
        return ReferenceCountUtil.retain(record);
    }

    @Override
    List<DnsRecord> filterResults(List<DnsRecord> unfiltered) {
        return unfiltered;
    }

    @Override
    boolean isCompleteEarly(DnsRecord resolved) {
        return false;
    }

    @Override
    boolean isDuplicateAllowed() {
        return true;
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals, DnsRecord result, DnsRecord convertedResult) {
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals, UnknownHostException cause) {
    }

    @Override
    DnsCnameCache cnameCache() {
        return NoopDnsCnameCache.INSTANCE;
    }
}

