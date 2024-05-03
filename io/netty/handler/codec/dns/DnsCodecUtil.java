/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.CharsetUtil;

final class DnsCodecUtil {
    private DnsCodecUtil() {
    }

    static void encodeDomainName(String name, ByteBuf buf) {
        String label;
        int labelLen;
        String[] labels;
        if (".".equals(name)) {
            buf.writeByte(0);
            return;
        }
        String[] stringArray = labels = name.split("\\.");
        int n = stringArray.length;
        for (int i = 0; i < n && (labelLen = (label = stringArray[i]).length()) != 0; ++i) {
            buf.writeByte(labelLen);
            ByteBufUtil.writeAscii((ByteBuf)buf, (CharSequence)label);
        }
        buf.writeByte(0);
    }

    static String decodeDomainName(ByteBuf in) {
        int position = -1;
        int checked = 0;
        int end = in.writerIndex();
        int readable = in.readableBytes();
        if (readable == 0) {
            return ".";
        }
        StringBuilder name = new StringBuilder(readable << 1);
        while (in.isReadable()) {
            boolean pointer;
            short len = in.readUnsignedByte();
            boolean bl = pointer = (len & 0xC0) == 192;
            if (pointer) {
                if (position == -1) {
                    position = in.readerIndex() + 1;
                }
                if (!in.isReadable()) {
                    throw new CorruptedFrameException("truncated pointer in a name");
                }
                int next = (len & 0x3F) << 8 | in.readUnsignedByte();
                if (next >= end) {
                    throw new CorruptedFrameException("name has an out-of-range pointer");
                }
                in.readerIndex(next);
                if ((checked += 2) < end) continue;
                throw new CorruptedFrameException("name contains a loop.");
            }
            if (len == 0) break;
            if (!in.isReadable(len)) {
                throw new CorruptedFrameException("truncated label in a name");
            }
            name.append(in.toString(in.readerIndex(), len, CharsetUtil.UTF_8)).append('.');
            in.skipBytes(len);
        }
        if (position != -1) {
            in.readerIndex(position);
        }
        if (name.length() == 0) {
            return ".";
        }
        if (name.charAt(name.length() - 1) != '.') {
            name.append('.');
        }
        return name.toString();
    }

    static ByteBuf decompressDomainName(ByteBuf compression) {
        String domainName = DnsCodecUtil.decodeDomainName(compression);
        ByteBuf result = compression.alloc().buffer(domainName.length() << 1);
        DnsCodecUtil.encodeDomainName(domainName, result);
        return result;
    }
}

