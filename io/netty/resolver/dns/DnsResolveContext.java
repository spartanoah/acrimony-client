/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DefaultDnsRecordDecoder;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.DnsAddressDecoder;
import io.netty.resolver.dns.DnsAddressResolveContext;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.resolver.dns.DnsCnameCache;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsQueryLifecycleObserver;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.NoopDnsQueryLifecycleObserver;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

abstract class DnsResolveContext<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsResolveContext.class);
    private static final RuntimeException NXDOMAIN_QUERY_FAILED_EXCEPTION = DnsResolveContextException.newStatic("No answer found and NXDOMAIN response code returned", DnsResolveContext.class, "onResponse(..)");
    private static final RuntimeException CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION = DnsResolveContextException.newStatic("No matching CNAME record found", DnsResolveContext.class, "onResponseCNAME(..)");
    private static final RuntimeException NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION = DnsResolveContextException.newStatic("No matching record type found", DnsResolveContext.class, "onResponseAorAAAA(..)");
    private static final RuntimeException UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION = DnsResolveContextException.newStatic("Response type was unrecognized", DnsResolveContext.class, "onResponse(..)");
    private static final RuntimeException NAME_SERVERS_EXHAUSTED_EXCEPTION = DnsResolveContextException.newStatic("No name servers returned an answer", DnsResolveContext.class, "tryToFinishResolve(..)");
    final DnsNameResolver parent;
    private final Promise<?> originalPromise;
    private final DnsServerAddressStream nameServerAddrs;
    private final String hostname;
    private final int dnsClass;
    private final DnsRecordType[] expectedTypes;
    final DnsRecord[] additionals;
    private final Set<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> queriesInProgress = Collections.newSetFromMap(new IdentityHashMap());
    private List<T> finalResult;
    private int allowedQueries;
    private boolean triedCNAME;
    private boolean completeEarly;

    DnsResolveContext(DnsNameResolver parent, Promise<?> originalPromise, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, int allowedQueries) {
        assert (expectedTypes.length > 0);
        this.parent = parent;
        this.originalPromise = originalPromise;
        this.hostname = hostname;
        this.dnsClass = dnsClass;
        this.expectedTypes = expectedTypes;
        this.additionals = additionals;
        this.nameServerAddrs = ObjectUtil.checkNotNull(nameServerAddrs, "nameServerAddrs");
        this.allowedQueries = allowedQueries;
    }

    DnsCache resolveCache() {
        return this.parent.resolveCache();
    }

    DnsCnameCache cnameCache() {
        return this.parent.cnameCache();
    }

    AuthoritativeDnsServerCache authoritativeDnsServerCache() {
        return this.parent.authoritativeDnsServerCache();
    }

    abstract DnsResolveContext<T> newResolverContext(DnsNameResolver var1, Promise<?> var2, String var3, int var4, DnsRecordType[] var5, DnsRecord[] var6, DnsServerAddressStream var7, int var8);

    abstract T convertRecord(DnsRecord var1, String var2, DnsRecord[] var3, EventLoop var4);

    abstract List<T> filterResults(List<T> var1);

    abstract boolean isCompleteEarly(T var1);

    abstract boolean isDuplicateAllowed();

    abstract void cache(String var1, DnsRecord[] var2, DnsRecord var3, T var4);

    abstract void cache(String var1, DnsRecord[] var2, UnknownHostException var3);

    void resolve(final Promise<List<T>> promise) {
        final String[] searchDomains = this.parent.searchDomains();
        if (searchDomains.length == 0 || this.parent.ndots() == 0 || StringUtil.endsWith((CharSequence)this.hostname, (char)'.')) {
            this.internalResolve(this.hostname, promise);
        } else {
            final boolean startWithoutSearchDomain = this.hasNDots();
            String initialHostname = startWithoutSearchDomain ? this.hostname : this.hostname + '.' + searchDomains[0];
            final int initialSearchDomainIdx = startWithoutSearchDomain ? 0 : 1;
            Promise<List<T>> searchDomainPromise = this.parent.executor().newPromise();
            searchDomainPromise.addListener(new FutureListener<List<T>>(){
                private int searchDomainIdx;
                {
                    this.searchDomainIdx = initialSearchDomainIdx;
                }

                @Override
                public void operationComplete(Future<List<T>> future) {
                    Throwable cause = future.cause();
                    if (cause == null) {
                        List result = future.getNow();
                        if (!promise.trySuccess(result)) {
                            for (Object item : result) {
                                ReferenceCountUtil.safeRelease(item);
                            }
                        }
                    } else if (DnsNameResolver.isTransportOrTimeoutError(cause)) {
                        promise.tryFailure(new SearchDomainUnknownHostException(cause, DnsResolveContext.this.hostname));
                    } else if (this.searchDomainIdx < searchDomains.length) {
                        Promise newPromise = DnsResolveContext.this.parent.executor().newPromise();
                        newPromise.addListener(this);
                        DnsResolveContext.this.doSearchDomainQuery(DnsResolveContext.this.hostname + '.' + searchDomains[this.searchDomainIdx++], newPromise);
                    } else if (!startWithoutSearchDomain) {
                        DnsResolveContext.this.internalResolve(DnsResolveContext.this.hostname, promise);
                    } else {
                        promise.tryFailure(new SearchDomainUnknownHostException(cause, DnsResolveContext.this.hostname));
                    }
                }
            });
            this.doSearchDomainQuery(initialHostname, searchDomainPromise);
        }
    }

    private boolean hasNDots() {
        int dots = 0;
        for (int idx = this.hostname.length() - 1; idx >= 0; --idx) {
            if (this.hostname.charAt(idx) != '.' || ++dots < this.parent.ndots()) continue;
            return true;
        }
        return false;
    }

    void doSearchDomainQuery(String hostname, Promise<List<T>> nextPromise) {
        DnsResolveContext<T> nextContext = this.newResolverContext(this.parent, this.originalPromise, hostname, this.dnsClass, this.expectedTypes, this.additionals, this.nameServerAddrs, this.parent.maxQueriesPerResolve());
        super.internalResolve(hostname, nextPromise);
    }

    private static String hostnameWithDot(String name) {
        if (StringUtil.endsWith((CharSequence)name, (char)'.')) {
            return name;
        }
        return name + '.';
    }

    static String cnameResolveFromCache(DnsCnameCache cnameCache, String name) throws UnknownHostException {
        String first = cnameCache.get(DnsResolveContext.hostnameWithDot(name));
        if (first == null) {
            return name;
        }
        String second = cnameCache.get(DnsResolveContext.hostnameWithDot(first));
        if (second == null) {
            return first;
        }
        DnsResolveContext.checkCnameLoop(name, first, second);
        return DnsResolveContext.cnameResolveFromCacheLoop(cnameCache, name, first, second);
    }

    private static String cnameResolveFromCacheLoop(DnsCnameCache cnameCache, String hostname, String first, String mapping) throws UnknownHostException {
        boolean advance = false;
        String name = mapping;
        while ((mapping = cnameCache.get(DnsResolveContext.hostnameWithDot(name))) != null) {
            DnsResolveContext.checkCnameLoop(hostname, first, mapping);
            name = mapping;
            if (advance) {
                first = cnameCache.get(first);
            }
            advance = !advance;
        }
        return name;
    }

    private static void checkCnameLoop(String hostname, String first, String second) throws UnknownHostException {
        if (first.equals(second)) {
            throw new UnknownHostException("CNAME loop detected for '" + hostname + '\'');
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void internalResolve(String name, Promise<List<T>> promise) {
        try {
            name = DnsResolveContext.cnameResolveFromCache(this.cnameCache(), name);
        } catch (Throwable cause) {
            promise.tryFailure(cause);
            return;
        }
        try {
            DnsServerAddressStream nameServerAddressStream = this.getNameServers(name);
            int end = this.expectedTypes.length - 1;
            for (int i = 0; i < end; ++i) {
                if (this.query(name, this.expectedTypes[i], nameServerAddressStream.duplicate(), false, promise)) continue;
                return;
            }
            this.query(name, this.expectedTypes[end], nameServerAddressStream, false, promise);
        } finally {
            this.parent.flushQueries();
        }
    }

    private DnsServerAddressStream getNameServersFromCache(String hostname) {
        DnsServerAddressStream entries;
        int idx;
        int len = hostname.length();
        if (len == 0) {
            return null;
        }
        if (hostname.charAt(len - 1) != '.') {
            hostname = hostname + ".";
        }
        if ((idx = hostname.indexOf(46)) == hostname.length() - 1) {
            return null;
        }
        do {
            int idx2;
            if ((idx2 = (hostname = hostname.substring(idx + 1)).indexOf(46)) <= 0 || idx2 == hostname.length() - 1) {
                return null;
            }
            idx = idx2;
        } while ((entries = this.authoritativeDnsServerCache().get(hostname)) == null);
        return entries;
    }

    private void query(final DnsServerAddressStream nameServerAddrStream, final int nameServerAddrStreamIndex, final DnsQuestion question, final DnsQueryLifecycleObserver queryLifecycleObserver, boolean flush, final Promise<List<T>> promise, Throwable cause) {
        if (this.completeEarly || nameServerAddrStreamIndex >= nameServerAddrStream.size() || this.allowedQueries == 0 || this.originalPromise.isCancelled() || promise.isCancelled()) {
            this.tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question, queryLifecycleObserver, promise, cause);
            return;
        }
        --this.allowedQueries;
        InetSocketAddress nameServerAddr = nameServerAddrStream.next();
        if (nameServerAddr.isUnresolved()) {
            this.queryUnresolvedNameServer(nameServerAddr, nameServerAddrStream, nameServerAddrStreamIndex, question, queryLifecycleObserver, promise, cause);
            return;
        }
        ChannelPromise writePromise = this.parent.ch.newPromise();
        Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> queryPromise = this.parent.ch.eventLoop().newPromise();
        Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f = this.parent.query0(nameServerAddr, question, this.additionals, flush, writePromise, queryPromise);
        this.queriesInProgress.add(f);
        queryLifecycleObserver.queryWritten(nameServerAddr, writePromise);
        f.addListener((GenericFutureListener<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>>)new FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>(){

            @Override
            public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
                DnsResolveContext.this.queriesInProgress.remove(future);
                if (promise.isDone() || future.isCancelled()) {
                    queryLifecycleObserver.queryCancelled(DnsResolveContext.this.allowedQueries);
                    AddressedEnvelope<DnsResponse, InetSocketAddress> result = future.getNow();
                    if (result != null) {
                        result.release();
                    }
                    return;
                }
                Throwable queryCause = future.cause();
                try {
                    if (queryCause == null) {
                        DnsResolveContext.this.onResponse(nameServerAddrStream, nameServerAddrStreamIndex, question, future.getNow(), queryLifecycleObserver, promise);
                    } else {
                        queryLifecycleObserver.queryFailed(queryCause);
                        DnsResolveContext.this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, DnsResolveContext.this.newDnsQueryLifecycleObserver(question), true, promise, queryCause);
                    }
                } finally {
                    DnsResolveContext.this.tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question, NoopDnsQueryLifecycleObserver.INSTANCE, promise, queryCause);
                }
            }
        });
    }

    private void queryUnresolvedNameServer(final InetSocketAddress nameServerAddr, final DnsServerAddressStream nameServerAddrStream, final int nameServerAddrStreamIndex, final DnsQuestion question, final DnsQueryLifecycleObserver queryLifecycleObserver, final Promise<List<T>> promise, final Throwable cause) {
        String nameServerName;
        String string = nameServerName = PlatformDependent.javaVersion() >= 7 ? nameServerAddr.getHostString() : nameServerAddr.getHostName();
        assert (nameServerName != null);
        final Future<Object> resolveFuture = this.parent.executor().newSucceededFuture(null);
        this.queriesInProgress.add(resolveFuture);
        Promise<List<T>> resolverPromise = this.parent.executor().newPromise();
        resolverPromise.addListener(new FutureListener<List<InetAddress>>(){

            @Override
            public void operationComplete(Future<List<InetAddress>> future) {
                DnsResolveContext.this.queriesInProgress.remove(resolveFuture);
                if (future.isSuccess()) {
                    List<InetAddress> resolvedAddresses = future.getNow();
                    CombinedDnsServerAddressStream addressStream = new CombinedDnsServerAddressStream(nameServerAddr, resolvedAddresses, nameServerAddrStream);
                    DnsResolveContext.this.query(addressStream, nameServerAddrStreamIndex, question, queryLifecycleObserver, true, promise, cause);
                } else {
                    DnsResolveContext.this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver, true, promise, cause);
                }
            }
        });
        DnsCache resolveCache = this.resolveCache();
        if (!DnsNameResolver.doResolveAllCached(nameServerName, this.additionals, resolverPromise, resolveCache, this.parent.resolvedInternetProtocolFamiliesUnsafe())) {
            new DnsAddressResolveContext(this.parent, this.originalPromise, nameServerName, this.additionals, this.parent.newNameServerAddressStream(nameServerName), this.allowedQueries, resolveCache, DnsResolveContext.redirectAuthoritativeDnsServerCache(this.authoritativeDnsServerCache()), false).resolve(resolverPromise);
        }
    }

    private static AuthoritativeDnsServerCache redirectAuthoritativeDnsServerCache(AuthoritativeDnsServerCache authoritativeDnsServerCache) {
        if (authoritativeDnsServerCache instanceof RedirectAuthoritativeDnsServerCache) {
            return authoritativeDnsServerCache;
        }
        return new RedirectAuthoritativeDnsServerCache(authoritativeDnsServerCache);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void onResponse(DnsServerAddressStream nameServerAddrStream, int nameServerAddrStreamIndex, DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        try {
            DnsResponse res = envelope.content();
            DnsResponseCode code = res.code();
            if (code == DnsResponseCode.NOERROR) {
                if (this.handleRedirect(question, envelope, queryLifecycleObserver, promise)) {
                    return;
                }
                DnsRecordType type = question.type();
                if (type == DnsRecordType.CNAME) {
                    this.onResponseCNAME(question, DnsResolveContext.buildAliasMap(envelope.content(), this.cnameCache(), this.parent.executor()), queryLifecycleObserver, promise);
                    return;
                }
                for (DnsRecordType expectedType : this.expectedTypes) {
                    if (type != expectedType) continue;
                    this.onExpectedResponse(question, envelope, queryLifecycleObserver, promise);
                    return;
                }
                queryLifecycleObserver.queryFailed(UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION);
                return;
            }
            if (code != DnsResponseCode.NXDOMAIN) {
                this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver.queryNoAnswer(code), true, promise, null);
            } else {
                queryLifecycleObserver.queryFailed(NXDOMAIN_QUERY_FAILED_EXCEPTION);
                if (!res.isAuthoritativeAnswer()) {
                    this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, this.newDnsQueryLifecycleObserver(question), true, promise, null);
                }
            }
        } finally {
            ReferenceCountUtil.safeRelease(envelope);
        }
    }

    private boolean handleRedirect(DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        AuthoritativeNameServerList serverNames;
        DnsResponse res = envelope.content();
        if (res.count(DnsSection.ANSWER) == 0 && (serverNames = DnsResolveContext.extractAuthoritativeNameServers(question.name(), res)) != null) {
            int additionalCount = res.count(DnsSection.ADDITIONAL);
            AuthoritativeDnsServerCache authoritativeDnsServerCache = this.authoritativeDnsServerCache();
            for (int i = 0; i < additionalCount; ++i) {
                Object r = res.recordAt(DnsSection.ADDITIONAL, i);
                if (r.type() == DnsRecordType.A && !this.parent.supportsARecords() || r.type() == DnsRecordType.AAAA && !this.parent.supportsAAAARecords()) continue;
                serverNames.handleWithAdditional(this.parent, (DnsRecord)r, authoritativeDnsServerCache);
            }
            serverNames.handleWithoutAdditionals(this.parent, this.resolveCache(), authoritativeDnsServerCache);
            List<InetSocketAddress> addresses = serverNames.addressList();
            DnsServerAddressStream serverStream = this.parent.newRedirectDnsServerStream(question.name(), addresses);
            if (serverStream != null) {
                this.query(serverStream, 0, question, queryLifecycleObserver.queryRedirected(new DnsAddressStreamList(serverStream)), true, promise, null);
                return true;
            }
        }
        return false;
    }

    private static AuthoritativeNameServerList extractAuthoritativeNameServers(String questionName, DnsResponse res) {
        int authorityCount = res.count(DnsSection.AUTHORITY);
        if (authorityCount == 0) {
            return null;
        }
        AuthoritativeNameServerList serverNames = new AuthoritativeNameServerList(questionName);
        for (int i = 0; i < authorityCount; ++i) {
            serverNames.add((DnsRecord)res.recordAt(DnsSection.AUTHORITY, i));
        }
        return serverNames.isEmpty() ? null : serverNames;
    }

    private void onExpectedResponse(DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        DnsResponse response = envelope.content();
        Map<String, String> cnames = DnsResolveContext.buildAliasMap(response, this.cnameCache(), this.parent.executor());
        int answerCount = response.count(DnsSection.ANSWER);
        boolean found = false;
        boolean completeEarly = this.completeEarly;
        for (int i = 0; i < answerCount; ++i) {
            T converted;
            Object r = response.recordAt(DnsSection.ANSWER, i);
            DnsRecordType type = r.type();
            boolean matches = false;
            for (DnsRecordType expectedType : this.expectedTypes) {
                if (type != expectedType) continue;
                matches = true;
                break;
            }
            if (!matches) continue;
            String questionName = question.name().toLowerCase(Locale.US);
            String recordName = r.name().toLowerCase(Locale.US);
            if (!recordName.equals(questionName)) {
                HashMap<String, String> cnamesCopy = new HashMap<String, String>(cnames);
                String resolved = questionName;
                while (!recordName.equals(resolved = (String)cnamesCopy.remove(resolved)) && resolved != null) {
                }
                if (resolved == null) {
                    assert (questionName.isEmpty() || questionName.charAt(questionName.length() - 1) == '.');
                    for (String searchDomain : this.parent.searchDomains()) {
                        String fqdn;
                        if (searchDomain.isEmpty() || !recordName.equals(fqdn = searchDomain.charAt(searchDomain.length() - 1) == '.' ? questionName + searchDomain : questionName + searchDomain + '.')) continue;
                        resolved = recordName;
                        break;
                    }
                    if (resolved == null) {
                        if (!logger.isDebugEnabled()) continue;
                        logger.debug("Ignoring record {} as it contains a different name than the question name [{}]. Cnames: {}, Search domains: {}", r.toString(), questionName, cnames, this.parent.searchDomains());
                        continue;
                    }
                }
            }
            if ((converted = this.convertRecord((DnsRecord)r, this.hostname, this.additionals, this.parent.executor())) == null) {
                if (!logger.isDebugEnabled()) continue;
                logger.debug("Ignoring record {} as the converted record is null. hostname [{}], Additionals: {}", r.toString(), this.hostname, this.additionals);
                continue;
            }
            boolean shouldRelease = false;
            if (!completeEarly) {
                completeEarly = this.isCompleteEarly(converted);
            }
            if (this.finalResult == null) {
                this.finalResult = new ArrayList<T>(8);
                this.finalResult.add(converted);
            } else if (this.isDuplicateAllowed() || !this.finalResult.contains(converted)) {
                this.finalResult.add(converted);
            } else {
                shouldRelease = true;
            }
            this.cache(this.hostname, this.additionals, (DnsRecord)r, converted);
            found = true;
            if (!shouldRelease) continue;
            ReferenceCountUtil.release(converted);
        }
        if (cnames.isEmpty()) {
            if (found) {
                if (completeEarly) {
                    this.completeEarly = true;
                }
                queryLifecycleObserver.querySucceed();
                return;
            }
            queryLifecycleObserver.queryFailed(NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION);
        } else {
            queryLifecycleObserver.querySucceed();
            this.onResponseCNAME(question, cnames, this.newDnsQueryLifecycleObserver(question), promise);
        }
    }

    private void onResponseCNAME(DnsQuestion question, Map<String, String> cnames, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        String next;
        String resolved = question.name().toLowerCase(Locale.US);
        boolean found = false;
        while (!cnames.isEmpty() && (next = cnames.remove(resolved)) != null) {
            found = true;
            resolved = next;
        }
        if (found) {
            this.followCname(question, resolved, queryLifecycleObserver, promise);
        } else {
            queryLifecycleObserver.queryFailed(CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION);
        }
    }

    private static Map<String, String> buildAliasMap(DnsResponse response, DnsCnameCache cache, EventLoop loop) {
        int answerCount = response.count(DnsSection.ANSWER);
        Map<String, String> cnames = null;
        for (int i = 0; i < answerCount; ++i) {
            String mappingWithDot;
            ByteBuf recordContent;
            String domainName;
            Object r = response.recordAt(DnsSection.ANSWER, i);
            DnsRecordType type = r.type();
            if (type != DnsRecordType.CNAME || !(r instanceof DnsRawRecord) || (domainName = DnsResolveContext.decodeDomainName(recordContent = ((ByteBufHolder)r).content())) == null) continue;
            if (cnames == null) {
                cnames = new HashMap<String, String>(Math.min(8, answerCount));
            }
            String name = r.name().toLowerCase(Locale.US);
            String mapping = domainName.toLowerCase(Locale.US);
            String nameWithDot = DnsResolveContext.hostnameWithDot(name);
            if (nameWithDot.equalsIgnoreCase(mappingWithDot = DnsResolveContext.hostnameWithDot(mapping))) continue;
            cache.cache(nameWithDot, mappingWithDot, r.timeToLive(), loop);
            cnames.put(name, mapping);
        }
        return cnames != null ? cnames : Collections.emptyMap();
    }

    private void tryToFinishResolve(DnsServerAddressStream nameServerAddrStream, int nameServerAddrStreamIndex, DnsQuestion question, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise, Throwable cause) {
        if (!this.completeEarly && !this.queriesInProgress.isEmpty()) {
            queryLifecycleObserver.queryCancelled(this.allowedQueries);
            return;
        }
        if (this.finalResult == null) {
            if (nameServerAddrStreamIndex < nameServerAddrStream.size()) {
                if (queryLifecycleObserver == NoopDnsQueryLifecycleObserver.INSTANCE) {
                    this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, this.newDnsQueryLifecycleObserver(question), true, promise, cause);
                } else {
                    this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver, true, promise, cause);
                }
                return;
            }
            queryLifecycleObserver.queryFailed(NAME_SERVERS_EXHAUSTED_EXCEPTION);
            if (!(cause != null || this.triedCNAME || question.type() != DnsRecordType.A && question.type() != DnsRecordType.AAAA)) {
                this.triedCNAME = true;
                this.query(this.hostname, DnsRecordType.CNAME, this.getNameServers(this.hostname), true, promise);
                return;
            }
        } else {
            queryLifecycleObserver.queryCancelled(this.allowedQueries);
        }
        this.finishResolve(promise, cause);
    }

    private void finishResolve(Promise<List<T>> promise, Throwable cause) {
        if (!this.completeEarly && !this.queriesInProgress.isEmpty()) {
            Iterator<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> i = this.queriesInProgress.iterator();
            while (i.hasNext()) {
                Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f = i.next();
                i.remove();
                f.cancel(false);
            }
        }
        if (this.finalResult != null) {
            List<T> result;
            if (!promise.isDone() && !DnsNameResolver.trySuccess(promise, result = this.filterResults(this.finalResult))) {
                for (Object item : result) {
                    ReferenceCountUtil.safeRelease(item);
                }
            }
            return;
        }
        int maxAllowedQueries = this.parent.maxQueriesPerResolve();
        int tries = maxAllowedQueries - this.allowedQueries;
        StringBuilder buf = new StringBuilder(64);
        buf.append("failed to resolve '").append(this.hostname).append('\'');
        if (tries > 1) {
            if (tries < maxAllowedQueries) {
                buf.append(" after ").append(tries).append(" queries ");
            } else {
                buf.append(". Exceeded max queries per resolve ").append(maxAllowedQueries).append(' ');
            }
        }
        UnknownHostException unknownHostException = new UnknownHostException(buf.toString());
        if (cause == null) {
            this.cache(this.hostname, this.additionals, unknownHostException);
        } else {
            unknownHostException.initCause(cause);
        }
        promise.tryFailure(unknownHostException);
    }

    static String decodeDomainName(ByteBuf in) {
        in.markReaderIndex();
        try {
            String string = DefaultDnsRecordDecoder.decodeName(in);
            return string;
        } catch (CorruptedFrameException e) {
            String string = null;
            return string;
        } finally {
            in.resetReaderIndex();
        }
    }

    private DnsServerAddressStream getNameServers(String name) {
        DnsServerAddressStream stream = this.getNameServersFromCache(name);
        if (stream == null) {
            if (name.equals(this.hostname)) {
                return this.nameServerAddrs.duplicate();
            }
            return this.parent.newNameServerAddressStream(name);
        }
        return stream;
    }

    private void followCname(DnsQuestion question, String cname, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
        DefaultDnsQuestion cnameQuestion;
        DnsServerAddressStream stream;
        try {
            cname = DnsResolveContext.cnameResolveFromCache(this.cnameCache(), cname);
            stream = this.getNameServers(cname);
            cnameQuestion = new DefaultDnsQuestion(cname, question.type(), this.dnsClass);
        } catch (Throwable cause) {
            queryLifecycleObserver.queryFailed(cause);
            PlatformDependent.throwException(cause);
            return;
        }
        this.query(stream, 0, cnameQuestion, queryLifecycleObserver.queryCNAMEd(cnameQuestion), true, promise, null);
    }

    private boolean query(String hostname, DnsRecordType type, DnsServerAddressStream dnsServerAddressStream, boolean flush, Promise<List<T>> promise) {
        DefaultDnsQuestion question;
        try {
            question = new DefaultDnsQuestion(hostname, type, this.dnsClass);
        } catch (Throwable cause) {
            promise.tryFailure(new IllegalArgumentException("Unable to create DNS Question for: [" + hostname + ", " + type + ']', cause));
            return false;
        }
        this.query(dnsServerAddressStream, 0, question, this.newDnsQueryLifecycleObserver(question), flush, promise, null);
        return true;
    }

    private DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
        return this.parent.dnsQueryLifecycleObserverFactory().newDnsQueryLifecycleObserver(question);
    }

    private static final class AuthoritativeNameServer {
        private final int dots;
        private final String domainName;
        final boolean isCopy;
        final String nsName;
        private long ttl;
        private InetSocketAddress address;
        AuthoritativeNameServer next;

        AuthoritativeNameServer(int dots, long ttl, String domainName, String nsName) {
            this.dots = dots;
            this.ttl = ttl;
            this.nsName = nsName;
            this.domainName = domainName;
            this.isCopy = false;
        }

        AuthoritativeNameServer(AuthoritativeNameServer server) {
            this.dots = server.dots;
            this.ttl = server.ttl;
            this.nsName = server.nsName;
            this.domainName = server.domainName;
            this.isCopy = true;
        }

        boolean isRootServer() {
            return this.dots == 1;
        }

        void update(InetSocketAddress address, long ttl) {
            assert (this.address == null || this.address.isUnresolved());
            this.address = address;
            this.ttl = Math.min(this.ttl, ttl);
        }

        void update(InetSocketAddress address) {
            this.update(address, Long.MAX_VALUE);
        }
    }

    private static final class AuthoritativeNameServerList {
        private final String questionName;
        private AuthoritativeNameServer head;
        private int nameServerCount;

        AuthoritativeNameServerList(String questionName) {
            this.questionName = questionName.toLowerCase(Locale.US);
        }

        void add(DnsRecord r) {
            if (r.type() != DnsRecordType.NS || !(r instanceof DnsRawRecord)) {
                return;
            }
            if (this.questionName.length() < r.name().length()) {
                return;
            }
            String recordName = r.name().toLowerCase(Locale.US);
            int dots = 0;
            int a = recordName.length() - 1;
            int b = this.questionName.length() - 1;
            while (a >= 0) {
                char c = recordName.charAt(a);
                if (this.questionName.charAt(b) != c) {
                    return;
                }
                if (c == '.') {
                    ++dots;
                }
                --a;
                --b;
            }
            if (this.head != null && this.head.dots > dots) {
                return;
            }
            ByteBuf recordContent = ((ByteBufHolder)((Object)r)).content();
            String domainName = DnsResolveContext.decodeDomainName(recordContent);
            if (domainName == null) {
                return;
            }
            if (this.head == null || this.head.dots < dots) {
                this.nameServerCount = 1;
                this.head = new AuthoritativeNameServer(dots, r.timeToLive(), recordName, domainName);
            } else if (this.head.dots == dots) {
                AuthoritativeNameServer serverName = this.head;
                while (serverName.next != null) {
                    serverName = serverName.next;
                }
                serverName.next = new AuthoritativeNameServer(dots, r.timeToLive(), recordName, domainName);
                ++this.nameServerCount;
            }
        }

        void handleWithAdditional(DnsNameResolver parent, DnsRecord r, AuthoritativeDnsServerCache authoritativeCache) {
            AuthoritativeNameServer serverName = this.head;
            String nsName = r.name();
            InetAddress resolved = DnsAddressDecoder.decodeAddress(r, nsName, parent.isDecodeIdn());
            if (resolved == null) {
                return;
            }
            while (serverName != null) {
                if (serverName.nsName.equalsIgnoreCase(nsName)) {
                    if (serverName.address != null) {
                        while (serverName.next != null && serverName.next.isCopy) {
                            serverName = serverName.next;
                        }
                        AuthoritativeNameServer server = new AuthoritativeNameServer(serverName);
                        server.next = serverName.next;
                        serverName.next = server;
                        serverName = server;
                        ++this.nameServerCount;
                    }
                    serverName.update(parent.newRedirectServerAddress(resolved), r.timeToLive());
                    AuthoritativeNameServerList.cache(serverName, authoritativeCache, parent.executor());
                    return;
                }
                serverName = serverName.next;
            }
        }

        void handleWithoutAdditionals(DnsNameResolver parent, DnsCache cache, AuthoritativeDnsServerCache authoritativeCache) {
            AuthoritativeNameServer serverName = this.head;
            while (serverName != null) {
                if (serverName.address == null) {
                    InetAddress address;
                    AuthoritativeNameServerList.cacheUnresolved(serverName, authoritativeCache, parent.executor());
                    List<? extends DnsCacheEntry> entries = cache.get(serverName.nsName, null);
                    if (entries != null && !entries.isEmpty() && (address = entries.get(0).address()) != null) {
                        serverName.update(parent.newRedirectServerAddress(address));
                        for (int i = 1; i < entries.size(); ++i) {
                            address = entries.get(i).address();
                            assert (address != null) : "Cache returned a cached failure, should never return anything else";
                            AuthoritativeNameServer server = new AuthoritativeNameServer(serverName);
                            server.next = serverName.next;
                            serverName.next = server;
                            serverName = server;
                            serverName.update(parent.newRedirectServerAddress(address));
                            ++this.nameServerCount;
                        }
                    }
                }
                serverName = serverName.next;
            }
        }

        private static void cacheUnresolved(AuthoritativeNameServer server, AuthoritativeDnsServerCache authoritativeCache, EventLoop loop) {
            server.address = InetSocketAddress.createUnresolved(server.nsName, 53);
            AuthoritativeNameServerList.cache(server, authoritativeCache, loop);
        }

        private static void cache(AuthoritativeNameServer server, AuthoritativeDnsServerCache cache, EventLoop loop) {
            if (!server.isRootServer()) {
                cache.cache(server.domainName, server.address, server.ttl, loop);
            }
        }

        boolean isEmpty() {
            return this.nameServerCount == 0;
        }

        List<InetSocketAddress> addressList() {
            ArrayList<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>(this.nameServerCount);
            AuthoritativeNameServer server = this.head;
            while (server != null) {
                if (server.address != null) {
                    addressList.add(server.address);
                }
                server = server.next;
            }
            return addressList;
        }
    }

    private final class CombinedDnsServerAddressStream
    implements DnsServerAddressStream {
        private final InetSocketAddress replaced;
        private final DnsServerAddressStream originalStream;
        private final List<InetAddress> resolvedAddresses;
        private Iterator<InetAddress> resolved;

        CombinedDnsServerAddressStream(InetSocketAddress replaced, List<InetAddress> resolvedAddresses, DnsServerAddressStream originalStream) {
            this.replaced = replaced;
            this.resolvedAddresses = resolvedAddresses;
            this.originalStream = originalStream;
            this.resolved = resolvedAddresses.iterator();
        }

        @Override
        public InetSocketAddress next() {
            if (this.resolved.hasNext()) {
                return this.nextResolved0();
            }
            InetSocketAddress address = this.originalStream.next();
            if (address.equals(this.replaced)) {
                this.resolved = this.resolvedAddresses.iterator();
                return this.nextResolved0();
            }
            return address;
        }

        private InetSocketAddress nextResolved0() {
            return DnsResolveContext.this.parent.newRedirectServerAddress(this.resolved.next());
        }

        @Override
        public int size() {
            return this.originalStream.size() + this.resolvedAddresses.size() - 1;
        }

        @Override
        public DnsServerAddressStream duplicate() {
            return new CombinedDnsServerAddressStream(this.replaced, this.resolvedAddresses, this.originalStream.duplicate());
        }
    }

    private static final class DnsAddressStreamList
    extends AbstractList<InetSocketAddress> {
        private final DnsServerAddressStream duplicate;
        private List<InetSocketAddress> addresses;

        DnsAddressStreamList(DnsServerAddressStream stream) {
            this.duplicate = stream.duplicate();
        }

        @Override
        public InetSocketAddress get(int index) {
            if (this.addresses == null) {
                DnsServerAddressStream stream = this.duplicate.duplicate();
                this.addresses = new ArrayList<InetSocketAddress>(this.size());
                for (int i = 0; i < stream.size(); ++i) {
                    this.addresses.add(stream.next());
                }
            }
            return this.addresses.get(index);
        }

        @Override
        public int size() {
            return this.duplicate.size();
        }

        @Override
        public Iterator<InetSocketAddress> iterator() {
            return new Iterator<InetSocketAddress>(){
                private final DnsServerAddressStream stream;
                private int i;
                {
                    this.stream = DnsAddressStreamList.this.duplicate.duplicate();
                }

                @Override
                public boolean hasNext() {
                    return this.i < this.stream.size();
                }

                @Override
                public InetSocketAddress next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    ++this.i;
                    return this.stream.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final class RedirectAuthoritativeDnsServerCache
    implements AuthoritativeDnsServerCache {
        private final AuthoritativeDnsServerCache wrapped;

        RedirectAuthoritativeDnsServerCache(AuthoritativeDnsServerCache authoritativeDnsServerCache) {
            this.wrapped = authoritativeDnsServerCache;
        }

        @Override
        public DnsServerAddressStream get(String hostname) {
            return null;
        }

        @Override
        public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
            this.wrapped.cache(hostname, address, originalTtl, loop);
        }

        @Override
        public void clear() {
            this.wrapped.clear();
        }

        @Override
        public boolean clear(String hostname) {
            return this.wrapped.clear(hostname);
        }
    }

    private static final class SearchDomainUnknownHostException
    extends UnknownHostException {
        private static final long serialVersionUID = -8573510133644997085L;

        SearchDomainUnknownHostException(Throwable cause, String originalHostname) {
            super("Search domain query failed. Original hostname: '" + originalHostname + "' " + cause.getMessage());
            this.setStackTrace(cause.getStackTrace());
            this.initCause(cause.getCause());
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    static final class DnsResolveContextException
    extends RuntimeException {
        private static final long serialVersionUID = 1209303419266433003L;

        private DnsResolveContextException(String message) {
            super(message);
        }

        @SuppressJava6Requirement(reason="uses Java 7+ Exception.<init>(String, Throwable, boolean, boolean) but is guarded by version checks")
        private DnsResolveContextException(String message, boolean shared) {
            super(message, null, false, true);
            assert (shared);
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        static DnsResolveContextException newStatic(String message, Class<?> clazz, String method) {
            DnsResolveContextException exception = PlatformDependent.javaVersion() >= 7 ? new DnsResolveContextException(message, true) : new DnsResolveContextException(message);
            return ThrowableUtil.unknownStackTrace(exception, clazz, method);
        }
    }
}

