/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DefaultDnsServerAddresses;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.SequentialDnsServerAddressStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

final class RotationalDnsServerAddresses
extends DefaultDnsServerAddresses {
    private static final AtomicIntegerFieldUpdater<RotationalDnsServerAddresses> startIdxUpdater = AtomicIntegerFieldUpdater.newUpdater(RotationalDnsServerAddresses.class, "startIdx");
    private volatile int startIdx;

    RotationalDnsServerAddresses(List<InetSocketAddress> addresses) {
        super("rotational", addresses);
    }

    @Override
    public DnsServerAddressStream stream() {
        int nextStartIdx;
        int curStartIdx;
        do {
            if ((nextStartIdx = (curStartIdx = this.startIdx) + 1) < this.addresses.size()) continue;
            nextStartIdx = 0;
        } while (!startIdxUpdater.compareAndSet(this, curStartIdx, nextStartIdx));
        return new SequentialDnsServerAddressStream(this.addresses, curStartIdx);
    }
}

