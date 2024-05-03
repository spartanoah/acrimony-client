/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;

final class DnsAddressDecoder {
    private static final int INADDRSZ4 = 4;
    private static final int INADDRSZ6 = 16;

    static InetAddress decodeAddress(DnsRecord record, String name, boolean decodeIdn) {
        if (!(record instanceof DnsRawRecord)) {
            return null;
        }
        ByteBuf content = ((ByteBufHolder)((Object)record)).content();
        int contentLen = content.readableBytes();
        if (contentLen != 4 && contentLen != 16) {
            return null;
        }
        byte[] addrBytes = new byte[contentLen];
        content.getBytes(content.readerIndex(), addrBytes);
        try {
            return InetAddress.getByAddress(decodeIdn ? IDN.toUnicode(name) : name, addrBytes);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

    private DnsAddressDecoder() {
    }
}

