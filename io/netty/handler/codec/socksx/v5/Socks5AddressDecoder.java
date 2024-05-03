/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

public interface Socks5AddressDecoder {
    public static final Socks5AddressDecoder DEFAULT = new Socks5AddressDecoder(){
        private static final int IPv6_LEN = 16;

        @Override
        public String decodeAddress(Socks5AddressType addrType, ByteBuf in) throws Exception {
            if (addrType == Socks5AddressType.IPv4) {
                return NetUtil.intToIpAddress((int)in.readInt());
            }
            if (addrType == Socks5AddressType.DOMAIN) {
                short length = in.readUnsignedByte();
                String domain = in.toString(in.readerIndex(), length, CharsetUtil.US_ASCII);
                in.skipBytes(length);
                return domain;
            }
            if (addrType == Socks5AddressType.IPv6) {
                if (in.hasArray()) {
                    int readerIdx = in.readerIndex();
                    in.readerIndex(readerIdx + 16);
                    return NetUtil.bytesToIpAddress((byte[])in.array(), (int)(in.arrayOffset() + readerIdx), (int)16);
                }
                byte[] tmp = new byte[16];
                in.readBytes(tmp);
                return NetUtil.bytesToIpAddress((byte[])tmp);
            }
            throw new DecoderException("unsupported address type: " + (addrType.byteValue() & 0xFF));
        }
    };

    public String decodeAddress(Socks5AddressType var1, ByteBuf var2) throws Exception;
}

