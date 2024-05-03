/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver;

import io.netty.resolver.HostsFileEntriesProvider;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DefaultHostsFileEntriesResolver
implements HostsFileEntriesResolver {
    private final Map<String, List<InetAddress>> inet4Entries;
    private final Map<String, List<InetAddress>> inet6Entries;

    public DefaultHostsFileEntriesResolver() {
        this(DefaultHostsFileEntriesResolver.parseEntries());
    }

    DefaultHostsFileEntriesResolver(HostsFileEntriesProvider entries) {
        this.inet4Entries = entries.ipv4Entries();
        this.inet6Entries = entries.ipv6Entries();
    }

    @Override
    public InetAddress address(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
        String normalized = this.normalize(inetHost);
        switch (resolvedAddressTypes) {
            case IPV4_ONLY: {
                return DefaultHostsFileEntriesResolver.firstAddress(this.inet4Entries.get(normalized));
            }
            case IPV6_ONLY: {
                return DefaultHostsFileEntriesResolver.firstAddress(this.inet6Entries.get(normalized));
            }
            case IPV4_PREFERRED: {
                InetAddress inet4Address = DefaultHostsFileEntriesResolver.firstAddress(this.inet4Entries.get(normalized));
                return inet4Address != null ? inet4Address : DefaultHostsFileEntriesResolver.firstAddress(this.inet6Entries.get(normalized));
            }
            case IPV6_PREFERRED: {
                InetAddress inet6Address = DefaultHostsFileEntriesResolver.firstAddress(this.inet6Entries.get(normalized));
                return inet6Address != null ? inet6Address : DefaultHostsFileEntriesResolver.firstAddress(this.inet4Entries.get(normalized));
            }
        }
        throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + (Object)((Object)resolvedAddressTypes));
    }

    public List<InetAddress> addresses(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
        String normalized = this.normalize(inetHost);
        switch (resolvedAddressTypes) {
            case IPV4_ONLY: {
                return this.inet4Entries.get(normalized);
            }
            case IPV6_ONLY: {
                return this.inet6Entries.get(normalized);
            }
            case IPV4_PREFERRED: {
                List<InetAddress> allInet4Addresses = this.inet4Entries.get(normalized);
                return allInet4Addresses != null ? DefaultHostsFileEntriesResolver.allAddresses(allInet4Addresses, this.inet6Entries.get(normalized)) : this.inet6Entries.get(normalized);
            }
            case IPV6_PREFERRED: {
                List<InetAddress> allInet6Addresses = this.inet6Entries.get(normalized);
                return allInet6Addresses != null ? DefaultHostsFileEntriesResolver.allAddresses(allInet6Addresses, this.inet4Entries.get(normalized)) : this.inet4Entries.get(normalized);
            }
        }
        throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + (Object)((Object)resolvedAddressTypes));
    }

    String normalize(String inetHost) {
        return inetHost.toLowerCase(Locale.ENGLISH);
    }

    private static List<InetAddress> allAddresses(List<InetAddress> a, List<InetAddress> b) {
        ArrayList<InetAddress> result = new ArrayList<InetAddress>(a.size() + (b == null ? 0 : b.size()));
        result.addAll(a);
        if (b != null) {
            result.addAll(b);
        }
        return result;
    }

    private static InetAddress firstAddress(List<InetAddress> addresses) {
        return addresses != null && !addresses.isEmpty() ? addresses.get(0) : null;
    }

    private static HostsFileEntriesProvider parseEntries() {
        if (PlatformDependent.isWindows()) {
            return HostsFileEntriesProvider.parser().parseSilently(Charset.defaultCharset(), CharsetUtil.UTF_16, CharsetUtil.UTF_8);
        }
        return HostsFileEntriesProvider.parser().parseSilently();
    }
}

