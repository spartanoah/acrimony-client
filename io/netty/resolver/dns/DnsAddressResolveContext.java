/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.DnsAddressDecoder;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsResolveContext;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.PreferredAddressTypeComparator;
import io.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

final class DnsAddressResolveContext
extends DnsResolveContext<InetAddress> {
    private final DnsCache resolveCache;
    private final AuthoritativeDnsServerCache authoritativeDnsServerCache;
    private final boolean completeEarlyIfPossible;

    DnsAddressResolveContext(DnsNameResolver parent, Promise<?> originalPromise, String hostname, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries, DnsCache resolveCache, AuthoritativeDnsServerCache authoritativeDnsServerCache, boolean completeEarlyIfPossible) {
        super(parent, originalPromise, hostname, 1, parent.resolveRecordTypes(), additionals, nameServerAddrs, allowedQueries);
        this.resolveCache = resolveCache;
        this.authoritativeDnsServerCache = authoritativeDnsServerCache;
        this.completeEarlyIfPossible = completeEarlyIfPossible;
    }

    @Override
    DnsResolveContext<InetAddress> newResolverContext(DnsNameResolver parent, Promise<?> originalPromise, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        return new DnsAddressResolveContext(parent, originalPromise, hostname, additionals, nameServerAddrs, allowedQueries, this.resolveCache, this.authoritativeDnsServerCache, this.completeEarlyIfPossible);
    }

    @Override
    InetAddress convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop) {
        return DnsAddressDecoder.decodeAddress(record, hostname, this.parent.isDecodeIdn());
    }

    @Override
    List<InetAddress> filterResults(List<InetAddress> unfiltered) {
        Collections.sort(unfiltered, PreferredAddressTypeComparator.comparator(this.parent.preferredAddressType()));
        return unfiltered;
    }

    @Override
    boolean isCompleteEarly(InetAddress resolved) {
        return this.completeEarlyIfPossible && this.parent.preferredAddressType().addressType() == resolved.getClass();
    }

    @Override
    boolean isDuplicateAllowed() {
        return false;
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals, DnsRecord result, InetAddress convertedResult) {
        this.resolveCache.cache(hostname, additionals, convertedResult, result.timeToLive(), this.parent.ch.eventLoop());
    }

    @Override
    void cache(String hostname, DnsRecord[] additionals, UnknownHostException cause) {
        this.resolveCache.cache(hostname, additionals, cause, this.parent.ch.eventLoop());
    }

    @Override
    void doSearchDomainQuery(String hostname, Promise<List<InetAddress>> nextPromise) {
        if (!DnsNameResolver.doResolveAllCached(hostname, this.additionals, nextPromise, this.resolveCache, this.parent.resolvedInternetProtocolFamiliesUnsafe())) {
            super.doSearchDomainQuery(hostname, nextPromise);
        }
    }

    @Override
    DnsCache resolveCache() {
        return this.resolveCache;
    }

    @Override
    AuthoritativeDnsServerCache authoritativeDnsServerCache() {
        return this.authoritativeDnsServerCache;
    }
}

