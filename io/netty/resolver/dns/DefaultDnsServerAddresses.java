/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DnsServerAddresses;
import java.net.InetSocketAddress;
import java.util.List;

abstract class DefaultDnsServerAddresses
extends DnsServerAddresses {
    protected final List<InetSocketAddress> addresses;
    private final String strVal;

    DefaultDnsServerAddresses(String type, List<InetSocketAddress> addresses) {
        this.addresses = addresses;
        StringBuilder buf = new StringBuilder(type.length() + 2 + addresses.size() * 16);
        buf.append(type).append('(');
        for (InetSocketAddress a : addresses) {
            buf.append(a).append(", ");
        }
        buf.setLength(buf.length() - 2);
        buf.append(')');
        this.strVal = buf.toString();
    }

    public String toString() {
        return this.strVal;
    }
}

