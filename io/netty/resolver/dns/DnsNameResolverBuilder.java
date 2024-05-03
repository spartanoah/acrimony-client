/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.SocketChannel;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.AuthoritativeDnsServerCacheAdapter;
import io.netty.resolver.dns.DefaultAuthoritativeDnsServerCache;
import io.netty.resolver.dns.DefaultDnsCache;
import io.netty.resolver.dns.DefaultDnsCnameCache;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCnameCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.NameServerComparator;
import io.netty.resolver.dns.NoopDnsQueryLifecycleObserverFactory;
import io.netty.util.internal.ObjectUtil;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public final class DnsNameResolverBuilder {
    volatile EventLoop eventLoop;
    private ChannelFactory<? extends DatagramChannel> channelFactory;
    private ChannelFactory<? extends SocketChannel> socketChannelFactory;
    private DnsCache resolveCache;
    private DnsCnameCache cnameCache;
    private AuthoritativeDnsServerCache authoritativeDnsServerCache;
    private SocketAddress localAddress;
    private Integer minTtl;
    private Integer maxTtl;
    private Integer negativeTtl;
    private long queryTimeoutMillis = -1L;
    private ResolvedAddressTypes resolvedAddressTypes = DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
    private boolean completeOncePreferredResolved;
    private boolean recursionDesired = true;
    private int maxQueriesPerResolve = -1;
    private boolean traceEnabled;
    private int maxPayloadSize = 4096;
    private boolean optResourceEnabled = true;
    private HostsFileEntriesResolver hostsFileEntriesResolver = HostsFileEntriesResolver.DEFAULT;
    private DnsServerAddressStreamProvider dnsServerAddressStreamProvider = DnsServerAddressStreamProviders.platformDefault();
    private DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory = NoopDnsQueryLifecycleObserverFactory.INSTANCE;
    private String[] searchDomains;
    private int ndots = -1;
    private boolean decodeIdn = true;

    public DnsNameResolverBuilder() {
    }

    public DnsNameResolverBuilder(EventLoop eventLoop) {
        this.eventLoop(eventLoop);
    }

    public DnsNameResolverBuilder eventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        return this;
    }

    protected ChannelFactory<? extends DatagramChannel> channelFactory() {
        return this.channelFactory;
    }

    public DnsNameResolverBuilder channelFactory(ChannelFactory<? extends DatagramChannel> channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    public DnsNameResolverBuilder channelType(Class<? extends DatagramChannel> channelType) {
        return this.channelFactory(new ReflectiveChannelFactory<DatagramChannel>(channelType));
    }

    public DnsNameResolverBuilder socketChannelFactory(ChannelFactory<? extends SocketChannel> channelFactory) {
        this.socketChannelFactory = channelFactory;
        return this;
    }

    public DnsNameResolverBuilder socketChannelType(Class<? extends SocketChannel> channelType) {
        if (channelType == null) {
            return this.socketChannelFactory(null);
        }
        return this.socketChannelFactory(new ReflectiveChannelFactory<SocketChannel>(channelType));
    }

    public DnsNameResolverBuilder resolveCache(DnsCache resolveCache) {
        this.resolveCache = resolveCache;
        return this;
    }

    public DnsNameResolverBuilder cnameCache(DnsCnameCache cnameCache) {
        this.cnameCache = cnameCache;
        return this;
    }

    public DnsNameResolverBuilder dnsQueryLifecycleObserverFactory(DnsQueryLifecycleObserverFactory lifecycleObserverFactory) {
        this.dnsQueryLifecycleObserverFactory = ObjectUtil.checkNotNull(lifecycleObserverFactory, "lifecycleObserverFactory");
        return this;
    }

    @Deprecated
    public DnsNameResolverBuilder authoritativeDnsServerCache(DnsCache authoritativeDnsServerCache) {
        this.authoritativeDnsServerCache = new AuthoritativeDnsServerCacheAdapter(authoritativeDnsServerCache);
        return this;
    }

    public DnsNameResolverBuilder authoritativeDnsServerCache(AuthoritativeDnsServerCache authoritativeDnsServerCache) {
        this.authoritativeDnsServerCache = authoritativeDnsServerCache;
        return this;
    }

    public DnsNameResolverBuilder localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return this;
    }

    public DnsNameResolverBuilder ttl(int minTtl, int maxTtl) {
        this.maxTtl = maxTtl;
        this.minTtl = minTtl;
        return this;
    }

    public DnsNameResolverBuilder negativeTtl(int negativeTtl) {
        this.negativeTtl = negativeTtl;
        return this;
    }

    public DnsNameResolverBuilder queryTimeoutMillis(long queryTimeoutMillis) {
        this.queryTimeoutMillis = queryTimeoutMillis;
        return this;
    }

    public static ResolvedAddressTypes computeResolvedAddressTypes(InternetProtocolFamily ... internetProtocolFamilies) {
        if (internetProtocolFamilies == null || internetProtocolFamilies.length == 0) {
            return DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        }
        if (internetProtocolFamilies.length > 2) {
            throw new IllegalArgumentException("No more than 2 InternetProtocolFamilies");
        }
        switch (internetProtocolFamilies[0]) {
            case IPv4: {
                return internetProtocolFamilies.length >= 2 && internetProtocolFamilies[1] == InternetProtocolFamily.IPv6 ? ResolvedAddressTypes.IPV4_PREFERRED : ResolvedAddressTypes.IPV4_ONLY;
            }
            case IPv6: {
                return internetProtocolFamilies.length >= 2 && internetProtocolFamilies[1] == InternetProtocolFamily.IPv4 ? ResolvedAddressTypes.IPV6_PREFERRED : ResolvedAddressTypes.IPV6_ONLY;
            }
        }
        throw new IllegalArgumentException("Couldn't resolve ResolvedAddressTypes from InternetProtocolFamily array");
    }

    public DnsNameResolverBuilder resolvedAddressTypes(ResolvedAddressTypes resolvedAddressTypes) {
        this.resolvedAddressTypes = resolvedAddressTypes;
        return this;
    }

    public DnsNameResolverBuilder completeOncePreferredResolved(boolean completeOncePreferredResolved) {
        this.completeOncePreferredResolved = completeOncePreferredResolved;
        return this;
    }

    public DnsNameResolverBuilder recursionDesired(boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
        return this;
    }

    public DnsNameResolverBuilder maxQueriesPerResolve(int maxQueriesPerResolve) {
        this.maxQueriesPerResolve = maxQueriesPerResolve;
        return this;
    }

    @Deprecated
    public DnsNameResolverBuilder traceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        return this;
    }

    public DnsNameResolverBuilder maxPayloadSize(int maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
        return this;
    }

    public DnsNameResolverBuilder optResourceEnabled(boolean optResourceEnabled) {
        this.optResourceEnabled = optResourceEnabled;
        return this;
    }

    public DnsNameResolverBuilder hostsFileEntriesResolver(HostsFileEntriesResolver hostsFileEntriesResolver) {
        this.hostsFileEntriesResolver = hostsFileEntriesResolver;
        return this;
    }

    protected DnsServerAddressStreamProvider nameServerProvider() {
        return this.dnsServerAddressStreamProvider;
    }

    public DnsNameResolverBuilder nameServerProvider(DnsServerAddressStreamProvider dnsServerAddressStreamProvider) {
        this.dnsServerAddressStreamProvider = ObjectUtil.checkNotNull(dnsServerAddressStreamProvider, "dnsServerAddressStreamProvider");
        return this;
    }

    public DnsNameResolverBuilder searchDomains(Iterable<String> searchDomains) {
        ObjectUtil.checkNotNull(searchDomains, "searchDomains");
        ArrayList<String> list = new ArrayList<String>(4);
        for (String f : searchDomains) {
            if (f == null) break;
            if (list.contains(f)) continue;
            list.add(f);
        }
        this.searchDomains = list.toArray(new String[0]);
        return this;
    }

    public DnsNameResolverBuilder ndots(int ndots) {
        this.ndots = ndots;
        return this;
    }

    private DnsCache newCache() {
        return new DefaultDnsCache(ObjectUtil.intValue(this.minTtl, 0), ObjectUtil.intValue(this.maxTtl, Integer.MAX_VALUE), ObjectUtil.intValue(this.negativeTtl, 0));
    }

    private AuthoritativeDnsServerCache newAuthoritativeDnsServerCache() {
        return new DefaultAuthoritativeDnsServerCache(ObjectUtil.intValue(this.minTtl, 0), ObjectUtil.intValue(this.maxTtl, Integer.MAX_VALUE), new NameServerComparator(DnsNameResolver.preferredAddressType(this.resolvedAddressTypes).addressType()));
    }

    private DnsCnameCache newCnameCache() {
        return new DefaultDnsCnameCache(ObjectUtil.intValue(this.minTtl, 0), ObjectUtil.intValue(this.maxTtl, Integer.MAX_VALUE));
    }

    public DnsNameResolverBuilder decodeIdn(boolean decodeIdn) {
        this.decodeIdn = decodeIdn;
        return this;
    }

    public DnsNameResolver build() {
        if (this.eventLoop == null) {
            throw new IllegalStateException("eventLoop should be specified to build a DnsNameResolver.");
        }
        if (this.resolveCache != null && (this.minTtl != null || this.maxTtl != null || this.negativeTtl != null)) {
            throw new IllegalStateException("resolveCache and TTLs are mutually exclusive");
        }
        if (this.authoritativeDnsServerCache != null && (this.minTtl != null || this.maxTtl != null || this.negativeTtl != null)) {
            throw new IllegalStateException("authoritativeDnsServerCache and TTLs are mutually exclusive");
        }
        DnsCache resolveCache = this.resolveCache != null ? this.resolveCache : this.newCache();
        DnsCnameCache cnameCache = this.cnameCache != null ? this.cnameCache : this.newCnameCache();
        AuthoritativeDnsServerCache authoritativeDnsServerCache = this.authoritativeDnsServerCache != null ? this.authoritativeDnsServerCache : this.newAuthoritativeDnsServerCache();
        return new DnsNameResolver(this.eventLoop, this.channelFactory, this.socketChannelFactory, resolveCache, cnameCache, authoritativeDnsServerCache, this.localAddress, this.dnsQueryLifecycleObserverFactory, this.queryTimeoutMillis, this.resolvedAddressTypes, this.recursionDesired, this.maxQueriesPerResolve, this.traceEnabled, this.maxPayloadSize, this.optResourceEnabled, this.hostsFileEntriesResolver, this.dnsServerAddressStreamProvider, this.searchDomains, this.ndots, this.decodeIdn, this.completeOncePreferredResolved);
    }

    public DnsNameResolverBuilder copy() {
        DnsNameResolverBuilder copiedBuilder = new DnsNameResolverBuilder();
        if (this.eventLoop != null) {
            copiedBuilder.eventLoop(this.eventLoop);
        }
        if (this.channelFactory != null) {
            copiedBuilder.channelFactory(this.channelFactory);
        }
        if (this.socketChannelFactory != null) {
            copiedBuilder.socketChannelFactory(this.socketChannelFactory);
        }
        if (this.resolveCache != null) {
            copiedBuilder.resolveCache(this.resolveCache);
        }
        if (this.cnameCache != null) {
            copiedBuilder.cnameCache(this.cnameCache);
        }
        if (this.maxTtl != null && this.minTtl != null) {
            copiedBuilder.ttl(this.minTtl, this.maxTtl);
        }
        if (this.negativeTtl != null) {
            copiedBuilder.negativeTtl(this.negativeTtl);
        }
        if (this.authoritativeDnsServerCache != null) {
            copiedBuilder.authoritativeDnsServerCache(this.authoritativeDnsServerCache);
        }
        if (this.dnsQueryLifecycleObserverFactory != null) {
            copiedBuilder.dnsQueryLifecycleObserverFactory(this.dnsQueryLifecycleObserverFactory);
        }
        copiedBuilder.queryTimeoutMillis(this.queryTimeoutMillis);
        copiedBuilder.resolvedAddressTypes(this.resolvedAddressTypes);
        copiedBuilder.recursionDesired(this.recursionDesired);
        copiedBuilder.maxQueriesPerResolve(this.maxQueriesPerResolve);
        copiedBuilder.traceEnabled(this.traceEnabled);
        copiedBuilder.maxPayloadSize(this.maxPayloadSize);
        copiedBuilder.optResourceEnabled(this.optResourceEnabled);
        copiedBuilder.hostsFileEntriesResolver(this.hostsFileEntriesResolver);
        if (this.dnsServerAddressStreamProvider != null) {
            copiedBuilder.nameServerProvider(this.dnsServerAddressStreamProvider);
        }
        if (this.searchDomains != null) {
            copiedBuilder.searchDomains(Arrays.asList(this.searchDomains));
        }
        copiedBuilder.ndots(this.ndots);
        copiedBuilder.decodeIdn(this.decodeIdn);
        copiedBuilder.completeOncePreferredResolved(this.completeOncePreferredResolved);
        return copiedBuilder;
    }
}

