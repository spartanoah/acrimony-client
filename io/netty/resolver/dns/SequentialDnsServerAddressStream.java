/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DnsServerAddressStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;

final class SequentialDnsServerAddressStream
implements DnsServerAddressStream {
    private final List<? extends InetSocketAddress> addresses;
    private int i;

    SequentialDnsServerAddressStream(List<? extends InetSocketAddress> addresses, int startIdx) {
        this.addresses = addresses;
        this.i = startIdx;
    }

    @Override
    public InetSocketAddress next() {
        int i = this.i;
        InetSocketAddress next = this.addresses.get(i);
        this.i = ++i < this.addresses.size() ? i : 0;
        return next;
    }

    @Override
    public int size() {
        return this.addresses.size();
    }

    @Override
    public SequentialDnsServerAddressStream duplicate() {
        return new SequentialDnsServerAddressStream(this.addresses, this.i);
    }

    public String toString() {
        return SequentialDnsServerAddressStream.toString("sequential", this.i, this.addresses);
    }

    static String toString(String type, int index, Collection<? extends InetSocketAddress> addresses) {
        StringBuilder buf = new StringBuilder(type.length() + 2 + addresses.size() * 16);
        buf.append(type).append("(index: ").append(index);
        buf.append(", addrs: (");
        for (InetSocketAddress inetSocketAddress : addresses) {
            buf.append(inetSocketAddress).append(", ");
        }
        buf.setLength(buf.length() - 2);
        buf.append("))");
        return buf.toString();
    }
}

