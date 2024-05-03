/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import java.util.List;

public final class MultiDnsServerAddressStreamProvider
implements DnsServerAddressStreamProvider {
    private final DnsServerAddressStreamProvider[] providers;

    public MultiDnsServerAddressStreamProvider(List<DnsServerAddressStreamProvider> providers) {
        this.providers = providers.toArray(new DnsServerAddressStreamProvider[0]);
    }

    public MultiDnsServerAddressStreamProvider(DnsServerAddressStreamProvider ... providers) {
        this.providers = (DnsServerAddressStreamProvider[])providers.clone();
    }

    @Override
    public DnsServerAddressStream nameServerAddressStream(String hostname) {
        for (DnsServerAddressStreamProvider provider : this.providers) {
            DnsServerAddressStream stream = provider.nameServerAddressStream(hostname);
            if (stream == null) continue;
            return stream;
        }
        return null;
    }
}

