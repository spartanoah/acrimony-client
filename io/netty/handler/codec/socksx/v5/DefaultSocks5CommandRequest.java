/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.v5.AbstractSocks5Message;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.net.IDN;

public final class DefaultSocks5CommandRequest
extends AbstractSocks5Message
implements Socks5CommandRequest {
    private final Socks5CommandType type;
    private final Socks5AddressType dstAddrType;
    private final String dstAddr;
    private final int dstPort;

    public DefaultSocks5CommandRequest(Socks5CommandType type, Socks5AddressType dstAddrType, String dstAddr, int dstPort) {
        this.type = ObjectUtil.checkNotNull(type, "type");
        ObjectUtil.checkNotNull(dstAddrType, "dstAddrType");
        ObjectUtil.checkNotNull(dstAddr, "dstAddr");
        if (dstAddrType == Socks5AddressType.IPv4) {
            if (!NetUtil.isValidIpV4Address(dstAddr)) {
                throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: a valid IPv4 address)");
            }
        } else if (dstAddrType == Socks5AddressType.DOMAIN) {
            if ((dstAddr = IDN.toASCII(dstAddr)).length() > 255) {
                throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: less than 256 chars)");
            }
        } else if (dstAddrType == Socks5AddressType.IPv6 && !NetUtil.isValidIpV6Address(dstAddr)) {
            throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: a valid IPv6 address");
        }
        if (dstPort < 0 || dstPort > 65535) {
            throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 0~65535)");
        }
        this.dstAddrType = dstAddrType;
        this.dstAddr = dstAddr;
        this.dstPort = dstPort;
    }

    @Override
    public Socks5CommandType type() {
        return this.type;
    }

    @Override
    public Socks5AddressType dstAddrType() {
        return this.dstAddrType;
    }

    @Override
    public String dstAddr() {
        return this.dstAddr;
    }

    @Override
    public int dstPort() {
        return this.dstPort;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append(StringUtil.simpleClassName(this));
        DecoderResult decoderResult = this.decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", type: ");
        } else {
            buf.append("(type: ");
        }
        buf.append(this.type());
        buf.append(", dstAddrType: ");
        buf.append(this.dstAddrType());
        buf.append(", dstAddr: ");
        buf.append(this.dstAddr());
        buf.append(", dstPort: ");
        buf.append(this.dstPort());
        buf.append(')');
        return buf.toString();
    }
}

