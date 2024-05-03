/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.hc.client5.http.DnsResolver;

public class SystemDefaultDnsResolver
implements DnsResolver {
    public static final SystemDefaultDnsResolver INSTANCE = new SystemDefaultDnsResolver();

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        if (host == null) {
            return null;
        }
        InetAddress in = InetAddress.getByName(host);
        String canonicalServer = in.getCanonicalHostName();
        if (in.getHostAddress().contentEquals(canonicalServer)) {
            return host;
        }
        return canonicalServer;
    }
}

