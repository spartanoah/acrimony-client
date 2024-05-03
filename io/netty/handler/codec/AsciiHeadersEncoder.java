/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.util.Map;

public final class AsciiHeadersEncoder {
    private final ByteBuf buf;
    private final SeparatorType separatorType;
    private final NewlineType newlineType;

    public AsciiHeadersEncoder(ByteBuf buf) {
        this(buf, SeparatorType.COLON_SPACE, NewlineType.CRLF);
    }

    public AsciiHeadersEncoder(ByteBuf buf, SeparatorType separatorType, NewlineType newlineType) {
        this.buf = ObjectUtil.checkNotNull(buf, "buf");
        this.separatorType = ObjectUtil.checkNotNull(separatorType, "separatorType");
        this.newlineType = ObjectUtil.checkNotNull(newlineType, "newlineType");
    }

    public void encode(Map.Entry<CharSequence, CharSequence> entry) {
        CharSequence name = entry.getKey();
        CharSequence value = entry.getValue();
        ByteBuf buf = this.buf;
        int nameLen = name.length();
        int valueLen = value.length();
        int entryLen = nameLen + valueLen + 4;
        int offset = buf.writerIndex();
        buf.ensureWritable(entryLen);
        AsciiHeadersEncoder.writeAscii(buf, offset, name);
        offset += nameLen;
        switch (this.separatorType) {
            case COLON: {
                buf.setByte(offset++, 58);
                break;
            }
            case COLON_SPACE: {
                buf.setByte(offset++, 58);
                buf.setByte(offset++, 32);
                break;
            }
            default: {
                throw new Error();
            }
        }
        AsciiHeadersEncoder.writeAscii(buf, offset, value);
        offset += valueLen;
        switch (this.newlineType) {
            case LF: {
                buf.setByte(offset++, 10);
                break;
            }
            case CRLF: {
                buf.setByte(offset++, 13);
                buf.setByte(offset++, 10);
                break;
            }
            default: {
                throw new Error();
            }
        }
        buf.writerIndex(offset);
    }

    private static void writeAscii(ByteBuf buf, int offset, CharSequence value) {
        if (value instanceof AsciiString) {
            ByteBufUtil.copy((AsciiString)((AsciiString)value), (int)0, (ByteBuf)buf, (int)offset, (int)value.length());
        } else {
            buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
        }
    }

    public static enum NewlineType {
        LF,
        CRLF;

    }

    public static enum SeparatorType {
        COLON,
        COLON_SPACE;

    }
}

