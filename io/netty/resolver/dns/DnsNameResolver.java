/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.codec.dns.DefaultDnsRawRecord;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.TcpDnsQueryEncoder;
import io.netty.handler.codec.dns.TcpDnsResponseDecoder;
import io.netty.resolver.DefaultHostsFileEntriesResolver;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.InetNameResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.AuthoritativeDnsServerCacheAdapter;
import io.netty.resolver.dns.BiDnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.DatagramDnsQueryContext;
import io.netty.resolver.dns.DnsAddressResolveContext;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.resolver.dns.DnsCnameCache;
import io.netty.resolver.dns.DnsNameResolverException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.netty.resolver.dns.DnsQueryContext;
import io.netty.resolver.dns.DnsQueryContextManager;
import io.netty.resolver.dns.DnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.DnsRecordResolveContext;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.LoggingDnsQueryLifeCycleObserverFactory;
import io.netty.resolver.dns.NameServerComparator;
import io.netty.resolver.dns.NoopDnsCnameCache;
import io.netty.resolver.dns.NoopDnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.SequentialDnsServerAddressStream;
import io.netty.resolver.dns.TcpDnsQueryContext;
import io.netty.resolver.dns.UnixResolverDnsServerAddressStreamProvider;
import io.netty.resolver.dns.UnixResolverOptions;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DnsNameResolver
extends InetNameResolver {
    private static final InternalLogger logger;
    private static final String LOCALHOST = "localhost";
    private static final String WINDOWS_HOST_NAME;
    private static final InetAddress LOCALHOST_ADDRESS;
    private static final DnsRecord[] EMPTY_ADDITIONALS;
    private static final DnsRecordType[] IPV4_ONLY_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV6_ONLY_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
    static final ResolvedAddressTypes DEFAULT_RESOLVE_ADDRESS_TYPES;
    static final String[] DEFAULT_SEARCH_DOMAINS;
    private static final UnixResolverOptions DEFAULT_OPTIONS;
    private static final DatagramDnsResponseDecoder DATAGRAM_DECODER;
    private static final DatagramDnsQueryEncoder DATAGRAM_ENCODER;
    private static final TcpDnsQueryEncoder TCP_ENCODER;
    final Future<Channel> channelFuture;
    final Channel ch;
    private final Comparator<InetSocketAddress> nameServerComparator;
    final DnsQueryContextManager queryContextManager = new DnsQueryContextManager();
    private final DnsCache resolveCache;
    private final AuthoritativeDnsServerCache authoritativeDnsServerCache;
    private final DnsCnameCache cnameCache;
    private final FastThreadLocal<DnsServerAddressStream> nameServerAddrStream = new FastThreadLocal<DnsServerAddressStream>(){

        @Override
        protected DnsServerAddressStream initialValue() {
            return DnsNameResolver.this.dnsServerAddressStreamProvider.nameServerAddressStream("");
        }
    };
    private final long queryTimeoutMillis;
    private final int maxQueriesPerResolve;
    private final ResolvedAddressTypes resolvedAddressTypes;
    private final InternetProtocolFamily[] resolvedInternetProtocolFamilies;
    private final boolean recursionDesired;
    private final int maxPayloadSize;
    private final boolean optResourceEnabled;
    private final HostsFileEntriesResolver hostsFileEntriesResolver;
    private final DnsServerAddressStreamProvider dnsServerAddressStreamProvider;
    private final String[] searchDomains;
    private final int ndots;
    private final boolean supportsAAAARecords;
    private final boolean supportsARecords;
    private final InternetProtocolFamily preferredAddressType;
    private final DnsRecordType[] resolveRecordTypes;
    private final boolean decodeIdn;
    private final DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory;
    private final boolean completeOncePreferredResolved;
    private final ChannelFactory<? extends SocketChannel> socketChannelFactory;

    private static boolean anyInterfaceSupportsIpV6() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!(inetAddress instanceof Inet6Address) || inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) continue;
                    return true;
                }
            }
        } catch (SocketException e) {
            logger.debug("Unable to detect if any interface supports IPv6, assuming IPv4-only", e);
        }
        return false;
    }

    private static List<String> getSearchDomainsHack() throws Exception {
        if (PlatformDependent.javaVersion() < 9) {
            Class<?> configClass = Class.forName("sun.net.dns.ResolverConfiguration");
            Method open = configClass.getMethod("open", new Class[0]);
            Method nameservers = configClass.getMethod("searchlist", new Class[0]);
            Object instance = open.invoke(null, new Object[0]);
            return (List)nameservers.invoke(instance, new Object[0]);
        }
        return Collections.emptyList();
    }

    @Deprecated
    public DnsNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, DnsCache resolveCache, DnsCache authoritativeDnsServerCache, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn) {
        this(eventLoop, channelFactory, resolveCache, new AuthoritativeDnsServerCacheAdapter(authoritativeDnsServerCache), dnsQueryLifecycleObserverFactory, queryTimeoutMillis, resolvedAddressTypes, recursionDesired, maxQueriesPerResolve, traceEnabled, maxPayloadSize, optResourceEnabled, hostsFileEntriesResolver, dnsServerAddressStreamProvider, searchDomains, ndots, decodeIdn);
    }

    @Deprecated
    public DnsNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, DnsCache resolveCache, AuthoritativeDnsServerCache authoritativeDnsServerCache, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn) {
        this(eventLoop, channelFactory, null, resolveCache, NoopDnsCnameCache.INSTANCE, authoritativeDnsServerCache, dnsQueryLifecycleObserverFactory, queryTimeoutMillis, resolvedAddressTypes, recursionDesired, maxQueriesPerResolve, traceEnabled, maxPayloadSize, optResourceEnabled, hostsFileEntriesResolver, dnsServerAddressStreamProvider, searchDomains, ndots, decodeIdn, false);
    }

    DnsNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, ChannelFactory<? extends SocketChannel> socketChannelFactory, DnsCache resolveCache, DnsCnameCache cnameCache, AuthoritativeDnsServerCache authoritativeDnsServerCache, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn, boolean completeOncePreferredResolved) {
        this(eventLoop, channelFactory, socketChannelFactory, resolveCache, cnameCache, authoritativeDnsServerCache, null, dnsQueryLifecycleObserverFactory, queryTimeoutMillis, resolvedAddressTypes, recursionDesired, maxQueriesPerResolve, traceEnabled, maxPayloadSize, optResourceEnabled, hostsFileEntriesResolver, dnsServerAddressStreamProvider, searchDomains, ndots, decodeIdn, completeOncePreferredResolved);
    }

    DnsNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, ChannelFactory<? extends SocketChannel> socketChannelFactory, final DnsCache resolveCache, final DnsCnameCache cnameCache, final AuthoritativeDnsServerCache authoritativeDnsServerCache, SocketAddress localAddress, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn, boolean completeOncePreferredResolved) {
        super(eventLoop);
        this.queryTimeoutMillis = queryTimeoutMillis > 0L ? queryTimeoutMillis : TimeUnit.SECONDS.toMillis(DEFAULT_OPTIONS.timeout());
        this.resolvedAddressTypes = resolvedAddressTypes != null ? resolvedAddressTypes : DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = recursionDesired;
        this.maxQueriesPerResolve = maxQueriesPerResolve > 0 ? maxQueriesPerResolve : DEFAULT_OPTIONS.attempts();
        this.maxPayloadSize = ObjectUtil.checkPositive(maxPayloadSize, "maxPayloadSize");
        this.optResourceEnabled = optResourceEnabled;
        this.hostsFileEntriesResolver = ObjectUtil.checkNotNull(hostsFileEntriesResolver, "hostsFileEntriesResolver");
        this.dnsServerAddressStreamProvider = ObjectUtil.checkNotNull(dnsServerAddressStreamProvider, "dnsServerAddressStreamProvider");
        this.resolveCache = ObjectUtil.checkNotNull(resolveCache, "resolveCache");
        this.cnameCache = ObjectUtil.checkNotNull(cnameCache, "cnameCache");
        this.dnsQueryLifecycleObserverFactory = traceEnabled ? (dnsQueryLifecycleObserverFactory instanceof NoopDnsQueryLifecycleObserverFactory ? new LoggingDnsQueryLifeCycleObserverFactory() : new BiDnsQueryLifecycleObserverFactory(new LoggingDnsQueryLifeCycleObserverFactory(), dnsQueryLifecycleObserverFactory)) : ObjectUtil.checkNotNull(dnsQueryLifecycleObserverFactory, "dnsQueryLifecycleObserverFactory");
        this.searchDomains = searchDomains != null ? (String[])searchDomains.clone() : DEFAULT_SEARCH_DOMAINS;
        this.ndots = ndots >= 0 ? ndots : DEFAULT_OPTIONS.ndots();
        this.decodeIdn = decodeIdn;
        this.completeOncePreferredResolved = completeOncePreferredResolved;
        this.socketChannelFactory = socketChannelFactory;
        switch (this.resolvedAddressTypes) {
            case IPV4_ONLY: {
                this.supportsAAAARecords = false;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV4_ONLY_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
                break;
            }
            case IPV4_PREFERRED: {
                this.supportsAAAARecords = true;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
                break;
            }
            case IPV6_ONLY: {
                this.supportsAAAARecords = true;
                this.supportsARecords = false;
                this.resolveRecordTypes = IPV6_ONLY_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
                break;
            }
            case IPV6_PREFERRED: {
                this.supportsAAAARecords = true;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + (Object)((Object)resolvedAddressTypes));
            }
        }
        this.preferredAddressType = DnsNameResolver.preferredAddressType(this.resolvedAddressTypes);
        this.authoritativeDnsServerCache = ObjectUtil.checkNotNull(authoritativeDnsServerCache, "authoritativeDnsServerCache");
        this.nameServerComparator = new NameServerComparator(this.preferredAddressType.addressType());
        Bootstrap b = new Bootstrap();
        b.group(this.executor());
        b.channelFactory(channelFactory);
        b.option(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, true);
        final DnsResponseHandler responseHandler = new DnsResponseHandler(this.executor().newPromise());
        b.handler(new ChannelInitializer<DatagramChannel>(){

            @Override
            protected void initChannel(DatagramChannel ch) {
                ch.pipeline().addLast(DATAGRAM_ENCODER, DATAGRAM_DECODER, responseHandler);
            }
        });
        this.channelFuture = responseHandler.channelActivePromise;
        ChannelFuture future = localAddress == null ? b.register() : b.bind(localAddress);
        Throwable cause = future.cause();
        if (cause != null) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            throw new IllegalStateException("Unable to create / register Channel", cause);
        }
        this.ch = future.channel();
        this.ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(maxPayloadSize));
        this.ch.closeFuture().addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) {
                resolveCache.clear();
                cnameCache.clear();
                authoritativeDnsServerCache.clear();
            }
        });
    }

    static InternetProtocolFamily preferredAddressType(ResolvedAddressTypes resolvedAddressTypes) {
        switch (resolvedAddressTypes) {
            case IPV4_ONLY: 
            case IPV4_PREFERRED: {
                return InternetProtocolFamily.IPv4;
            }
            case IPV6_ONLY: 
            case IPV6_PREFERRED: {
                return InternetProtocolFamily.IPv6;
            }
        }
        throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + (Object)((Object)resolvedAddressTypes));
    }

    InetSocketAddress newRedirectServerAddress(InetAddress server) {
        return new InetSocketAddress(server, 53);
    }

    final DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory() {
        return this.dnsQueryLifecycleObserverFactory;
    }

    protected DnsServerAddressStream newRedirectDnsServerStream(String hostname, List<InetSocketAddress> nameservers) {
        DnsServerAddressStream cached = this.authoritativeDnsServerCache().get(hostname);
        if (cached == null || cached.size() == 0) {
            Collections.sort(nameservers, this.nameServerComparator);
            return new SequentialDnsServerAddressStream(nameservers, 0);
        }
        return cached;
    }

    public DnsCache resolveCache() {
        return this.resolveCache;
    }

    public DnsCnameCache cnameCache() {
        return this.cnameCache;
    }

    public AuthoritativeDnsServerCache authoritativeDnsServerCache() {
        return this.authoritativeDnsServerCache;
    }

    public long queryTimeoutMillis() {
        return this.queryTimeoutMillis;
    }

    public ResolvedAddressTypes resolvedAddressTypes() {
        return this.resolvedAddressTypes;
    }

    InternetProtocolFamily[] resolvedInternetProtocolFamiliesUnsafe() {
        return this.resolvedInternetProtocolFamilies;
    }

    final String[] searchDomains() {
        return this.searchDomains;
    }

    final int ndots() {
        return this.ndots;
    }

    final boolean supportsAAAARecords() {
        return this.supportsAAAARecords;
    }

    final boolean supportsARecords() {
        return this.supportsARecords;
    }

    final InternetProtocolFamily preferredAddressType() {
        return this.preferredAddressType;
    }

    final DnsRecordType[] resolveRecordTypes() {
        return this.resolveRecordTypes;
    }

    final boolean isDecodeIdn() {
        return this.decodeIdn;
    }

    public boolean isRecursionDesired() {
        return this.recursionDesired;
    }

    public int maxQueriesPerResolve() {
        return this.maxQueriesPerResolve;
    }

    public int maxPayloadSize() {
        return this.maxPayloadSize;
    }

    public boolean isOptResourceEnabled() {
        return this.optResourceEnabled;
    }

    public HostsFileEntriesResolver hostsFileEntriesResolver() {
        return this.hostsFileEntriesResolver;
    }

    @Override
    public void close() {
        if (this.ch.isOpen()) {
            this.ch.close();
        }
    }

    @Override
    protected EventLoop executor() {
        return (EventLoop)super.executor();
    }

    private InetAddress resolveHostsFileEntry(String hostname) {
        if (this.hostsFileEntriesResolver == null) {
            return null;
        }
        InetAddress address = this.hostsFileEntriesResolver.address(hostname, this.resolvedAddressTypes);
        return address == null && DnsNameResolver.isLocalWindowsHost(hostname) ? LOCALHOST_ADDRESS : address;
    }

    private List<InetAddress> resolveHostsFileEntries(String hostname) {
        InetAddress address;
        if (this.hostsFileEntriesResolver == null) {
            return null;
        }
        List<InetAddress> addresses = this.hostsFileEntriesResolver instanceof DefaultHostsFileEntriesResolver ? ((DefaultHostsFileEntriesResolver)this.hostsFileEntriesResolver).addresses(hostname, this.resolvedAddressTypes) : ((address = this.hostsFileEntriesResolver.address(hostname, this.resolvedAddressTypes)) != null ? Collections.singletonList(address) : null);
        return addresses == null && DnsNameResolver.isLocalWindowsHost(hostname) ? Collections.singletonList(LOCALHOST_ADDRESS) : addresses;
    }

    private static boolean isLocalWindowsHost(String hostname) {
        return PlatformDependent.isWindows() && (LOCALHOST.equalsIgnoreCase(hostname) || WINDOWS_HOST_NAME != null && WINDOWS_HOST_NAME.equalsIgnoreCase(hostname));
    }

    @Override
    public final Future<InetAddress> resolve(String inetHost, Iterable<DnsRecord> additionals) {
        return this.resolve(inetHost, additionals, this.executor().newPromise());
    }

    public final Future<InetAddress> resolve(String inetHost, Iterable<DnsRecord> additionals, Promise<InetAddress> promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        DnsRecord[] additionalsArray = DnsNameResolver.toArray(additionals, true);
        try {
            this.doResolve(inetHost, additionalsArray, promise, this.resolveCache);
            return promise;
        } catch (Exception e) {
            return promise.setFailure(e);
        }
    }

    public final Future<List<InetAddress>> resolveAll(String inetHost, Iterable<DnsRecord> additionals) {
        return this.resolveAll(inetHost, additionals, this.executor().newPromise());
    }

    public final Future<List<InetAddress>> resolveAll(String inetHost, Iterable<DnsRecord> additionals, Promise<List<InetAddress>> promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        DnsRecord[] additionalsArray = DnsNameResolver.toArray(additionals, true);
        try {
            this.doResolveAll(inetHost, additionalsArray, promise, this.resolveCache);
            return promise;
        } catch (Exception e) {
            return promise.setFailure(e);
        }
    }

    @Override
    protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
        this.doResolve(inetHost, EMPTY_ADDITIONALS, promise, this.resolveCache);
    }

    public final Future<List<DnsRecord>> resolveAll(DnsQuestion question) {
        return this.resolveAll(question, EMPTY_ADDITIONALS, this.executor().newPromise());
    }

    public final Future<List<DnsRecord>> resolveAll(DnsQuestion question, Iterable<DnsRecord> additionals) {
        return this.resolveAll(question, additionals, this.executor().newPromise());
    }

    public final Future<List<DnsRecord>> resolveAll(DnsQuestion question, Iterable<DnsRecord> additionals, Promise<List<DnsRecord>> promise) {
        DnsRecord[] additionalsArray = DnsNameResolver.toArray(additionals, true);
        return this.resolveAll(question, additionalsArray, promise);
    }

    private Future<List<DnsRecord>> resolveAll(DnsQuestion question, DnsRecord[] additionals, Promise<List<DnsRecord>> promise) {
        List<InetAddress> hostsFileEntries;
        ObjectUtil.checkNotNull(question, "question");
        ObjectUtil.checkNotNull(promise, "promise");
        DnsRecordType type = question.type();
        String hostname = question.name();
        if ((type == DnsRecordType.A || type == DnsRecordType.AAAA) && (hostsFileEntries = this.resolveHostsFileEntries(hostname)) != null) {
            ArrayList<DefaultDnsRawRecord> result = new ArrayList<DefaultDnsRawRecord>();
            for (InetAddress hostsFileEntry : hostsFileEntries) {
                ByteBuf content = null;
                if (hostsFileEntry instanceof Inet4Address) {
                    if (type == DnsRecordType.A) {
                        content = Unpooled.wrappedBuffer(hostsFileEntry.getAddress());
                    }
                } else if (hostsFileEntry instanceof Inet6Address && type == DnsRecordType.AAAA) {
                    content = Unpooled.wrappedBuffer(hostsFileEntry.getAddress());
                }
                if (content == null) continue;
                result.add(new DefaultDnsRawRecord(hostname, type, 86400L, content));
            }
            if (!result.isEmpty()) {
                DnsNameResolver.trySuccess(promise, result);
                return promise;
            }
        }
        DnsServerAddressStream nameServerAddrs = this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
        new DnsRecordResolveContext(this, promise, question, additionals, nameServerAddrs, this.maxQueriesPerResolve).resolve(promise);
        return promise;
    }

    private static DnsRecord[] toArray(Iterable<DnsRecord> additionals, boolean validateType) {
        ObjectUtil.checkNotNull(additionals, "additionals");
        if (additionals instanceof Collection) {
            Collection records = (Collection)additionals;
            for (DnsRecord r : additionals) {
                DnsNameResolver.validateAdditional(r, validateType);
            }
            return records.toArray(new DnsRecord[records.size()]);
        }
        Iterator<DnsRecord> additionalsIt = additionals.iterator();
        if (!additionalsIt.hasNext()) {
            return EMPTY_ADDITIONALS;
        }
        ArrayList<DnsRecord> records = new ArrayList<DnsRecord>();
        do {
            DnsRecord r = additionalsIt.next();
            DnsNameResolver.validateAdditional(r, validateType);
            records.add(r);
        } while (additionalsIt.hasNext());
        return records.toArray(new DnsRecord[records.size()]);
    }

    private static void validateAdditional(DnsRecord record, boolean validateType) {
        ObjectUtil.checkNotNull(record, "record");
        if (validateType && record instanceof DnsRawRecord) {
            throw new IllegalArgumentException("DnsRawRecord implementations not allowed: " + record);
        }
    }

    private InetAddress loopbackAddress() {
        return this.preferredAddressType().localhost();
    }

    protected void doResolve(String inetHost, DnsRecord[] additionals, Promise<InetAddress> promise, DnsCache resolveCache) throws Exception {
        if (inetHost == null || inetHost.isEmpty()) {
            promise.setSuccess(this.loopbackAddress());
            return;
        }
        byte[] bytes = NetUtil.createByteArrayFromIpAddressString(inetHost);
        if (bytes != null) {
            promise.setSuccess(InetAddress.getByAddress(bytes));
            return;
        }
        String hostname = DnsNameResolver.hostname(inetHost);
        InetAddress hostsFileEntry = this.resolveHostsFileEntry(hostname);
        if (hostsFileEntry != null) {
            promise.setSuccess(hostsFileEntry);
            return;
        }
        if (!this.doResolveCached(hostname, additionals, promise, resolveCache)) {
            this.doResolveUncached(hostname, additionals, promise, resolveCache, true);
        }
    }

    private boolean doResolveCached(String hostname, DnsRecord[] additionals, Promise<InetAddress> promise, DnsCache resolveCache) {
        List<? extends DnsCacheEntry> cachedEntries = resolveCache.get(hostname, additionals);
        if (cachedEntries == null || cachedEntries.isEmpty()) {
            return false;
        }
        Throwable cause = cachedEntries.get(0).cause();
        if (cause == null) {
            int numEntries = cachedEntries.size();
            for (InternetProtocolFamily f : this.resolvedInternetProtocolFamilies) {
                for (int i = 0; i < numEntries; ++i) {
                    DnsCacheEntry e = cachedEntries.get(i);
                    if (!f.addressType().isInstance(e.address())) continue;
                    DnsNameResolver.trySuccess(promise, e.address());
                    return true;
                }
            }
            return false;
        }
        DnsNameResolver.tryFailure(promise, cause);
        return true;
    }

    static <T> boolean trySuccess(Promise<T> promise, T result) {
        boolean notifiedRecords = promise.trySuccess(result);
        if (!notifiedRecords) {
            logger.trace("Failed to notify success ({}) to a promise: {}", (Object)result, (Object)promise);
        }
        return notifiedRecords;
    }

    private static void tryFailure(Promise<?> promise, Throwable cause) {
        if (!promise.tryFailure(cause)) {
            logger.trace("Failed to notify failure to a promise: {}", (Object)promise, (Object)cause);
        }
    }

    private void doResolveUncached(String hostname, DnsRecord[] additionals, final Promise<InetAddress> promise, DnsCache resolveCache, boolean completeEarlyIfPossible) {
        Promise<List<InetAddress>> allPromise = this.executor().newPromise();
        this.doResolveAllUncached(hostname, additionals, promise, allPromise, resolveCache, true);
        allPromise.addListener((GenericFutureListener<Future<List<InetAddress>>>)new FutureListener<List<InetAddress>>(){

            @Override
            public void operationComplete(Future<List<InetAddress>> future) {
                if (future.isSuccess()) {
                    DnsNameResolver.trySuccess(promise, future.getNow().get(0));
                } else {
                    DnsNameResolver.tryFailure(promise, future.cause());
                }
            }
        });
    }

    @Override
    protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
        this.doResolveAll(inetHost, EMPTY_ADDITIONALS, promise, this.resolveCache);
    }

    protected void doResolveAll(String inetHost, DnsRecord[] additionals, Promise<List<InetAddress>> promise, DnsCache resolveCache) throws Exception {
        if (inetHost == null || inetHost.isEmpty()) {
            promise.setSuccess(Collections.singletonList(this.loopbackAddress()));
            return;
        }
        byte[] bytes = NetUtil.createByteArrayFromIpAddressString(inetHost);
        if (bytes != null) {
            promise.setSuccess(Collections.singletonList(InetAddress.getByAddress(bytes)));
            return;
        }
        String hostname = DnsNameResolver.hostname(inetHost);
        List<InetAddress> hostsFileEntries = this.resolveHostsFileEntries(hostname);
        if (hostsFileEntries != null) {
            promise.setSuccess(hostsFileEntries);
            return;
        }
        if (!DnsNameResolver.doResolveAllCached(hostname, additionals, promise, resolveCache, this.resolvedInternetProtocolFamilies)) {
            this.doResolveAllUncached(hostname, additionals, promise, promise, resolveCache, this.completeOncePreferredResolved);
        }
    }

    static boolean doResolveAllCached(String hostname, DnsRecord[] additionals, Promise<List<InetAddress>> promise, DnsCache resolveCache, InternetProtocolFamily[] resolvedInternetProtocolFamilies) {
        List<? extends DnsCacheEntry> cachedEntries = resolveCache.get(hostname, additionals);
        if (cachedEntries == null || cachedEntries.isEmpty()) {
            return false;
        }
        Throwable cause = cachedEntries.get(0).cause();
        if (cause == null) {
            ArrayList<InetAddress> result = null;
            int numEntries = cachedEntries.size();
            for (InternetProtocolFamily f : resolvedInternetProtocolFamilies) {
                for (int i = 0; i < numEntries; ++i) {
                    DnsCacheEntry e = cachedEntries.get(i);
                    if (!f.addressType().isInstance(e.address())) continue;
                    if (result == null) {
                        result = new ArrayList<InetAddress>(numEntries);
                    }
                    result.add(e.address());
                }
            }
            if (result != null) {
                DnsNameResolver.trySuccess(promise, result);
                return true;
            }
            return false;
        }
        DnsNameResolver.tryFailure(promise, cause);
        return true;
    }

    private void doResolveAllUncached(final String hostname, final DnsRecord[] additionals, final Promise<?> originalPromise, final Promise<List<InetAddress>> promise, final DnsCache resolveCache, final boolean completeEarlyIfPossible) {
        EventLoop executor = this.executor();
        if (executor.inEventLoop()) {
            this.doResolveAllUncached0(hostname, additionals, originalPromise, promise, resolveCache, completeEarlyIfPossible);
        } else {
            executor.execute(new Runnable(){

                @Override
                public void run() {
                    DnsNameResolver.this.doResolveAllUncached0(hostname, additionals, originalPromise, promise, resolveCache, completeEarlyIfPossible);
                }
            });
        }
    }

    private void doResolveAllUncached0(String hostname, DnsRecord[] additionals, Promise<?> originalPromise, Promise<List<InetAddress>> promise, DnsCache resolveCache, boolean completeEarlyIfPossible) {
        assert (this.executor().inEventLoop());
        DnsServerAddressStream nameServerAddrs = this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
        new DnsAddressResolveContext(this, originalPromise, hostname, additionals, nameServerAddrs, this.maxQueriesPerResolve, resolveCache, this.authoritativeDnsServerCache, completeEarlyIfPossible).resolve(promise);
    }

    private static String hostname(String inetHost) {
        String hostname = IDN.toASCII(inetHost);
        if (StringUtil.endsWith((CharSequence)inetHost, (char)'.') && !StringUtil.endsWith((CharSequence)hostname, (char)'.')) {
            hostname = hostname + ".";
        }
        return hostname;
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question) {
        return this.query(this.nextNameServerAddress(), question);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Iterable<DnsRecord> additionals) {
        return this.query(this.nextNameServerAddress(), question, additionals);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query(this.nextNameServerAddress(), question, Collections.<DnsRecord>emptyList(), promise);
    }

    private InetSocketAddress nextNameServerAddress() {
        return this.nameServerAddrStream.get().next();
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question) {
        return this.query0(nameServerAddr, question, EMPTY_ADDITIONALS, true, this.ch.newPromise(), this.ch.eventLoop().newPromise());
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals) {
        return this.query0(nameServerAddr, question, DnsNameResolver.toArray(additionals, false), true, this.ch.newPromise(), this.ch.eventLoop().newPromise());
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query0(nameServerAddr, question, EMPTY_ADDITIONALS, true, this.ch.newPromise(), promise);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query0(nameServerAddr, question, DnsNameResolver.toArray(additionals, false), true, this.ch.newPromise(), promise);
    }

    public static boolean isTransportOrTimeoutError(Throwable cause) {
        return cause != null && cause.getCause() instanceof DnsNameResolverException;
    }

    public static boolean isTimeoutError(Throwable cause) {
        return cause != null && cause.getCause() instanceof DnsNameResolverTimeoutException;
    }

    final void flushQueries() {
        this.ch.flush();
    }

    final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query0(InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, boolean flush, ChannelPromise writePromise, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        assert (!writePromise.isVoid());
        Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> castPromise = DnsNameResolver.cast(ObjectUtil.checkNotNull(promise, "promise"));
        try {
            new DatagramDnsQueryContext(this, nameServerAddr, question, additionals, castPromise).query(flush, writePromise);
            return castPromise;
        } catch (Exception e) {
            return castPromise.setFailure(e);
        }
    }

    private static Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> cast(Promise<?> promise) {
        return promise;
    }

    final DnsServerAddressStream newNameServerAddressStream(String hostname) {
        return this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
    }

    static {
        UnixResolverOptions options;
        String[] searchDomains;
        String hostName;
        logger = InternalLoggerFactory.getInstance(DnsNameResolver.class);
        EMPTY_ADDITIONALS = new DnsRecord[0];
        IPV4_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.A};
        IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv4};
        IPV4_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.A, DnsRecordType.AAAA};
        IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv4, InternetProtocolFamily.IPv6};
        IPV6_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.AAAA};
        IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv6};
        IPV6_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.AAAA, DnsRecordType.A};
        IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv6, InternetProtocolFamily.IPv4};
        if (NetUtil.isIpV4StackPreferred() || !DnsNameResolver.anyInterfaceSupportsIpV6()) {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_ONLY;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
        } else if (NetUtil.isIpV6AddressesPreferred()) {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV6_PREFERRED;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST6;
        } else {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_PREFERRED;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
        }
        try {
            hostName = PlatformDependent.isWindows() ? InetAddress.getLocalHost().getHostName() : null;
        } catch (Exception ignore) {
            hostName = null;
        }
        WINDOWS_HOST_NAME = hostName;
        try {
            List<String> list = PlatformDependent.isWindows() ? DnsNameResolver.getSearchDomainsHack() : UnixResolverDnsServerAddressStreamProvider.parseEtcResolverSearchDomains();
            searchDomains = list.toArray(new String[0]);
        } catch (Exception ignore) {
            searchDomains = EmptyArrays.EMPTY_STRINGS;
        }
        DEFAULT_SEARCH_DOMAINS = searchDomains;
        try {
            options = UnixResolverDnsServerAddressStreamProvider.parseEtcResolverOptions();
        } catch (Exception ignore) {
            options = UnixResolverOptions.newBuilder().build();
        }
        DEFAULT_OPTIONS = options;
        DATAGRAM_DECODER = new DatagramDnsResponseDecoder(){

            @Override
            protected DnsResponse decodeResponse(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
                DnsResponse response = super.decodeResponse(ctx, packet);
                if (((ByteBuf)packet.content()).isReadable()) {
                    response.setTruncated(true);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} RECEIVED: UDP truncated packet received, consider adjusting maxPayloadSize for the {}.", (Object)ctx.channel(), (Object)StringUtil.simpleClassName(DnsNameResolver.class));
                    }
                }
                return response;
            }
        };
        DATAGRAM_ENCODER = new DatagramDnsQueryEncoder();
        TCP_ENCODER = new TcpDnsQueryEncoder();
    }

    private static final class AddressedEnvelopeAdapter
    implements AddressedEnvelope<DnsResponse, InetSocketAddress> {
        private final InetSocketAddress sender;
        private final InetSocketAddress recipient;
        private final DnsResponse response;

        AddressedEnvelopeAdapter(InetSocketAddress sender, InetSocketAddress recipient, DnsResponse response) {
            this.sender = sender;
            this.recipient = recipient;
            this.response = response;
        }

        @Override
        public DnsResponse content() {
            return this.response;
        }

        @Override
        public InetSocketAddress sender() {
            return this.sender;
        }

        @Override
        public InetSocketAddress recipient() {
            return this.recipient;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> retain() {
            this.response.retain();
            return this;
        }

        @Override
        public AddressedEnvelope<DnsResponse, InetSocketAddress> retain(int increment) {
            this.response.retain(increment);
            return this;
        }

        public AddressedEnvelope<DnsResponse, InetSocketAddress> touch() {
            this.response.touch();
            return this;
        }

        public AddressedEnvelope<DnsResponse, InetSocketAddress> touch(Object hint) {
            this.response.touch(hint);
            return this;
        }

        @Override
        public int refCnt() {
            return this.response.refCnt();
        }

        @Override
        public boolean release() {
            return this.response.release();
        }

        @Override
        public boolean release(int decrement) {
            return this.response.release(decrement);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AddressedEnvelope)) {
                return false;
            }
            AddressedEnvelope that = (AddressedEnvelope)obj;
            if (this.sender() == null ? that.sender() != null : !this.sender().equals(that.sender())) {
                return false;
            }
            if (this.recipient() == null ? that.recipient() != null : !this.recipient().equals(that.recipient())) {
                return false;
            }
            return this.response.equals(obj);
        }

        public int hashCode() {
            int hashCode = this.response.hashCode();
            if (this.sender() != null) {
                hashCode = hashCode * 31 + this.sender().hashCode();
            }
            if (this.recipient() != null) {
                hashCode = hashCode * 31 + this.recipient().hashCode();
            }
            return hashCode;
        }
    }

    private final class DnsResponseHandler
    extends ChannelInboundHandlerAdapter {
        private final Promise<Channel> channelActivePromise;

        DnsResponseHandler(Promise<Channel> channelActivePromise) {
            this.channelActivePromise = channelActivePromise;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            DnsQueryContext qCtx;
            final DatagramDnsResponse res = (DatagramDnsResponse)msg;
            final int queryId = res.id();
            if (logger.isDebugEnabled()) {
                logger.debug("{} RECEIVED: UDP [{}: {}], {}", DnsNameResolver.this.ch, queryId, res.sender(), res);
            }
            if ((qCtx = DnsNameResolver.this.queryContextManager.get(res.sender(), queryId)) == null) {
                logger.debug("Received a DNS response with an unknown ID: UDP [{}: {}]", (Object)DnsNameResolver.this.ch, (Object)queryId);
                res.release();
                return;
            }
            if (!res.isTruncated() || DnsNameResolver.this.socketChannelFactory == null) {
                qCtx.finish(res);
                return;
            }
            Bootstrap bs = new Bootstrap();
            ((Bootstrap)((Bootstrap)((Bootstrap)bs.option(ChannelOption.SO_REUSEADDR, true)).group(DnsNameResolver.this.executor())).channelFactory(DnsNameResolver.this.socketChannelFactory)).handler(TCP_ENCODER);
            bs.connect(res.sender()).addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!future.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Unable to fallback to TCP [{}]", (Object)queryId, (Object)future.cause());
                        }
                        qCtx.finish(res);
                        return;
                    }
                    final Channel channel = future.channel();
                    Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise = channel.eventLoop().newPromise();
                    final TcpDnsQueryContext tcpCtx = new TcpDnsQueryContext(DnsNameResolver.this, channel, (InetSocketAddress)channel.remoteAddress(), qCtx.question(), EMPTY_ADDITIONALS, promise);
                    channel.pipeline().addLast(new TcpDnsResponseDecoder());
                    channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            DnsQueryContext foundCtx;
                            Channel channel = ctx.channel();
                            DnsResponse response = (DnsResponse)msg;
                            int queryId = response.id();
                            if (logger.isDebugEnabled()) {
                                logger.debug("{} RECEIVED: TCP [{}: {}], {}", channel, queryId, channel.remoteAddress(), response);
                            }
                            if ((foundCtx = DnsNameResolver.this.queryContextManager.get(res.sender(), queryId)) == tcpCtx) {
                                tcpCtx.finish(new AddressedEnvelopeAdapter((InetSocketAddress)ctx.channel().remoteAddress(), (InetSocketAddress)ctx.channel().localAddress(), response));
                            } else {
                                response.release();
                                tcpCtx.tryFailure("Received TCP DNS response with unexpected ID", null, false);
                                logger.debug("Received a DNS response with an unexpected ID: TCP [{}: {}]", (Object)channel, (Object)queryId);
                            }
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            if (tcpCtx.tryFailure("TCP fallback error", cause, false) && logger.isDebugEnabled()) {
                                logger.debug("{} Error during processing response: TCP [{}: {}]", ctx.channel(), queryId, ctx.channel().remoteAddress(), cause);
                            }
                        }
                    });
                    promise.addListener((GenericFutureListener<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>>)new FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>(){

                        @Override
                        public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
                            channel.close();
                            if (future.isSuccess()) {
                                qCtx.finish(future.getNow());
                                res.release();
                            } else {
                                qCtx.finish(res);
                            }
                        }
                    });
                    tcpCtx.query(true, future.channel().newPromise());
                }
            });
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            this.channelActivePromise.setSuccess(ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof CorruptedFrameException) {
                logger.debug("Unable to decode DNS response: UDP [{}]", (Object)ctx.channel(), (Object)cause);
            } else {
                logger.warn("Unexpected exception: UDP [{}]", (Object)ctx.channel(), (Object)cause);
            }
        }
    }
}

