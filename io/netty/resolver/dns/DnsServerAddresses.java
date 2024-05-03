/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import io.netty.resolver.dns.DefaultDnsServerAddresses;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.RotationalDnsServerAddresses;
import io.netty.resolver.dns.SequentialDnsServerAddressStream;
import io.netty.resolver.dns.ShuffledDnsServerAddressStream;
import io.netty.resolver.dns.SingletonDnsServerAddresses;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DnsServerAddresses {
    @Deprecated
    public static List<InetSocketAddress> defaultAddressList() {
        return DefaultDnsServerAddressStreamProvider.defaultAddressList();
    }

    @Deprecated
    public static DnsServerAddresses defaultAddresses() {
        return DefaultDnsServerAddressStreamProvider.defaultAddresses();
    }

    public static DnsServerAddresses sequential(Iterable<? extends InetSocketAddress> addresses) {
        return DnsServerAddresses.sequential0(DnsServerAddresses.sanitize(addresses));
    }

    public static DnsServerAddresses sequential(InetSocketAddress ... addresses) {
        return DnsServerAddresses.sequential0(DnsServerAddresses.sanitize(addresses));
    }

    private static DnsServerAddresses sequential0(List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return DnsServerAddresses.singleton(addresses.get(0));
        }
        return new DefaultDnsServerAddresses("sequential", (List)addresses){

            @Override
            public DnsServerAddressStream stream() {
                return new SequentialDnsServerAddressStream(this.addresses, 0);
            }
        };
    }

    public static DnsServerAddresses shuffled(Iterable<? extends InetSocketAddress> addresses) {
        return DnsServerAddresses.shuffled0(DnsServerAddresses.sanitize(addresses));
    }

    public static DnsServerAddresses shuffled(InetSocketAddress ... addresses) {
        return DnsServerAddresses.shuffled0(DnsServerAddresses.sanitize(addresses));
    }

    private static DnsServerAddresses shuffled0(List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return DnsServerAddresses.singleton(addresses.get(0));
        }
        return new DefaultDnsServerAddresses("shuffled", (List)addresses){

            @Override
            public DnsServerAddressStream stream() {
                return new ShuffledDnsServerAddressStream(this.addresses);
            }
        };
    }

    public static DnsServerAddresses rotational(Iterable<? extends InetSocketAddress> addresses) {
        return DnsServerAddresses.rotational0(DnsServerAddresses.sanitize(addresses));
    }

    public static DnsServerAddresses rotational(InetSocketAddress ... addresses) {
        return DnsServerAddresses.rotational0(DnsServerAddresses.sanitize(addresses));
    }

    private static DnsServerAddresses rotational0(List<InetSocketAddress> addresses) {
        if (addresses.size() == 1) {
            return DnsServerAddresses.singleton(addresses.get(0));
        }
        return new RotationalDnsServerAddresses(addresses);
    }

    public static DnsServerAddresses singleton(InetSocketAddress address) {
        ObjectUtil.checkNotNull(address, "address");
        if (address.isUnresolved()) {
            throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + address);
        }
        return new SingletonDnsServerAddresses(address);
    }

    private static List<InetSocketAddress> sanitize(Iterable<? extends InetSocketAddress> addresses) {
        ObjectUtil.checkNotNull(addresses, "addresses");
        ArrayList<InetSocketAddress> list = addresses instanceof Collection ? new ArrayList(((Collection)addresses).size()) : new ArrayList<InetSocketAddress>(4);
        for (InetSocketAddress inetSocketAddress : addresses) {
            if (inetSocketAddress == null) break;
            if (inetSocketAddress.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + inetSocketAddress);
            }
            list.add(inetSocketAddress);
        }
        return ObjectUtil.checkNonEmpty(list, "list");
    }

    private static List<InetSocketAddress> sanitize(InetSocketAddress[] addresses) {
        ObjectUtil.checkNotNull(addresses, "addresses");
        ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>(addresses.length);
        for (InetSocketAddress a : addresses) {
            if (a == null) break;
            if (a.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a);
            }
            list.add(a);
        }
        if (list.isEmpty()) {
            return DefaultDnsServerAddressStreamProvider.defaultAddressList();
        }
        return list;
    }

    public abstract DnsServerAddressStream stream();
}

