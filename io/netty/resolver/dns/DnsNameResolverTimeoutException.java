/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.resolver.dns.DnsNameResolverException;
import java.net.InetSocketAddress;

public final class DnsNameResolverTimeoutException
extends DnsNameResolverException {
    private static final long serialVersionUID = -8826717969627131854L;

    public DnsNameResolverTimeoutException(InetSocketAddress remoteAddress, DnsQuestion question, String message) {
        super(remoteAddress, question, message);
    }
}

