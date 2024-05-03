/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns.macos;

import io.netty.channel.unix.NativeInetAddress;
import java.net.InetSocketAddress;

final class DnsResolver {
    private final String domain;
    private final InetSocketAddress[] nameservers;
    private final int port;
    private final String[] searches;
    private final String options;
    private final int timeout;
    private final int searchOrder;

    DnsResolver(String domain, byte[][] nameservers, int port, String[] searches, String options, int timeout, int searchOrder) {
        this.domain = domain;
        if (nameservers == null) {
            this.nameservers = new InetSocketAddress[0];
        } else {
            this.nameservers = new InetSocketAddress[nameservers.length];
            for (int i = 0; i < nameservers.length; ++i) {
                byte[] addr = nameservers[i];
                this.nameservers[i] = NativeInetAddress.address(addr, 0, addr.length);
            }
        }
        this.port = port;
        this.searches = searches;
        this.options = options;
        this.timeout = timeout;
        this.searchOrder = searchOrder;
    }

    String domain() {
        return this.domain;
    }

    InetSocketAddress[] nameservers() {
        return this.nameservers;
    }

    int port() {
        return this.port;
    }

    String[] searches() {
        return this.searches;
    }

    String options() {
        return this.options;
    }

    int timeout() {
        return this.timeout;
    }

    int searchOrder() {
        return this.searchOrder;
    }
}

