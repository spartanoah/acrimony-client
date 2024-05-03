/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socks;

public enum SocksProtocolVersion {
    SOCKS4a(4),
    SOCKS5(5),
    UNKNOWN(-1);

    private final byte b;

    private SocksProtocolVersion(byte b) {
        this.b = b;
    }

    @Deprecated
    public static SocksProtocolVersion fromByte(byte b) {
        return SocksProtocolVersion.valueOf(b);
    }

    public static SocksProtocolVersion valueOf(byte b) {
        for (SocksProtocolVersion code : SocksProtocolVersion.values()) {
            if (code.b != b) continue;
            return code;
        }
        return UNKNOWN;
    }

    public byte byteValue() {
        return this.b;
    }
}

