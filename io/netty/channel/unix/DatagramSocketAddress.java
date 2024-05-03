/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public final class DatagramSocketAddress
extends InetSocketAddress {
    private static final long serialVersionUID = 3094819287843178401L;
    private final int receivedAmount;
    private final DatagramSocketAddress localAddress;

    DatagramSocketAddress(byte[] addr, int scopeId, int port, int receivedAmount, DatagramSocketAddress local) throws UnknownHostException {
        super(DatagramSocketAddress.newAddress(addr, scopeId), port);
        this.receivedAmount = receivedAmount;
        this.localAddress = local;
    }

    public DatagramSocketAddress localAddress() {
        return this.localAddress;
    }

    public int receivedAmount() {
        return this.receivedAmount;
    }

    private static InetAddress newAddress(byte[] bytes, int scopeId) throws UnknownHostException {
        if (bytes.length == 4) {
            return InetAddress.getByAddress(bytes);
        }
        return Inet6Address.getByAddress(null, bytes, scopeId);
    }
}

