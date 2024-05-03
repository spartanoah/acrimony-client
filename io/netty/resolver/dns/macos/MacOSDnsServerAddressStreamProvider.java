/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns.macos;

import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.DnsServerAddresses;
import io.netty.resolver.dns.macos.DnsResolver;
import io.netty.util.internal.ClassInitializerUtil;
import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class MacOSDnsServerAddressStreamProvider
implements DnsServerAddressStreamProvider {
    private static final Comparator<DnsResolver> RESOLVER_COMPARATOR = new Comparator<DnsResolver>(){

        @Override
        public int compare(DnsResolver r1, DnsResolver r2) {
            return r1.searchOrder() < r2.searchOrder() ? 1 : (r1.searchOrder() == r2.searchOrder() ? 0 : -1);
        }
    };
    private static final Throwable UNAVAILABILITY_CAUSE;
    private static final InternalLogger logger;
    private static final long REFRESH_INTERVAL;
    private volatile Map<String, DnsServerAddresses> currentMappings = MacOSDnsServerAddressStreamProvider.retrieveCurrentMappings();
    private final AtomicLong lastRefresh = new AtomicLong(System.nanoTime());

    private static void loadNativeLibrary() {
        if (!PlatformDependent.isOsx()) {
            throw new IllegalStateException("Only supported on MacOS/OSX");
        }
        String staticLibName = "netty_resolver_dns_native_macos";
        String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
        ClassLoader cl = PlatformDependent.getClassLoader(MacOSDnsServerAddressStreamProvider.class);
        try {
            NativeLibraryLoader.load(sharedLibName, cl);
        } catch (UnsatisfiedLinkError e1) {
            try {
                NativeLibraryLoader.load(staticLibName, cl);
                logger.debug("Failed to load {}", (Object)sharedLibName, (Object)e1);
            } catch (UnsatisfiedLinkError e2) {
                ThrowableUtil.addSuppressed((Throwable)e1, e2);
                throw e1;
            }
        }
    }

    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    public MacOSDnsServerAddressStreamProvider() {
        MacOSDnsServerAddressStreamProvider.ensureAvailability();
    }

    private static Map<String, DnsServerAddresses> retrieveCurrentMappings() {
        DnsResolver[] resolvers = MacOSDnsServerAddressStreamProvider.resolvers();
        if (resolvers == null || resolvers.length == 0) {
            return Collections.emptyMap();
        }
        Arrays.sort(resolvers, RESOLVER_COMPARATOR);
        HashMap<String, DnsServerAddresses> resolverMap = new HashMap<String, DnsServerAddresses>(resolvers.length);
        for (DnsResolver resolver : resolvers) {
            InetSocketAddress[] nameservers;
            if ("mdns".equalsIgnoreCase(resolver.options()) || (nameservers = resolver.nameservers()) == null || nameservers.length == 0) continue;
            String domain = resolver.domain();
            if (domain == null) {
                domain = "";
            }
            InetSocketAddress[] servers = resolver.nameservers();
            for (int a = 0; a < servers.length; ++a) {
                InetSocketAddress address = servers[a];
                if (address.getPort() != 0) continue;
                int port = resolver.port();
                if (port == 0) {
                    port = 53;
                }
                servers[a] = new InetSocketAddress(address.getAddress(), port);
            }
            resolverMap.put(domain, DnsServerAddresses.sequential(servers));
        }
        return resolverMap;
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        long last = this.lastRefresh.get();
        Map<String, DnsServerAddresses> resolverMap = this.currentMappings;
        if (System.nanoTime() - last > REFRESH_INTERVAL && this.lastRefresh.compareAndSet(last, System.nanoTime())) {
            resolverMap = this.currentMappings = MacOSDnsServerAddressStreamProvider.retrieveCurrentMappings();
        }
        String originalHostname = hostname;
        while (true) {
            DnsServerAddresses addresses;
            int i;
            if ((i = hostname.indexOf(46, 1)) < 0 || i == hostname.length() - 1) {
                addresses = resolverMap.get("");
                if (addresses != null) {
                    return addresses.stream();
                }
                return DnsServerAddressStreamProviders.unixDefault().nameServerAddressStream(originalHostname);
            }
            addresses = resolverMap.get(hostname);
            if (addresses != null) {
                return addresses.stream();
            }
            hostname = hostname.substring(i + 1);
        }
    }

    private static native DnsResolver[] resolvers();

    static {
        logger = InternalLoggerFactory.getInstance(MacOSDnsServerAddressStreamProvider.class);
        REFRESH_INTERVAL = TimeUnit.SECONDS.toNanos(10L);
        ClassInitializerUtil.tryLoadClasses(MacOSDnsServerAddressStreamProvider.class, byte[].class, String.class);
        Throwable cause = null;
        try {
            MacOSDnsServerAddressStreamProvider.loadNativeLibrary();
        } catch (Throwable error) {
            cause = error;
        }
        UNAVAILABILITY_CAUSE = cause;
    }
}

