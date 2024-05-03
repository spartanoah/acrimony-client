/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.UUID;

public class UUIDSerializer
extends StdScalarSerializer<UUID> {
    static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public UUIDSerializer() {
        super(UUID.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, UUID value) {
        return value.getLeastSignificantBits() == 0L && value.getMostSignificantBits() == 0L;
    }

    @Override
    public void serialize(UUID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.canWriteBinaryNatively() && !(gen instanceof TokenBuffer)) {
            gen.writeBinary(UUIDSerializer._asBytes(value));
            return;
        }
        char[] ch = new char[36];
        long msb = value.getMostSignificantBits();
        UUIDSerializer._appendInt((int)(msb >> 32), ch, 0);
        ch[8] = 45;
        int i = (int)msb;
        UUIDSerializer._appendShort(i >>> 16, ch, 9);
        ch[13] = 45;
        UUIDSerializer._appendShort(i, ch, 14);
        ch[18] = 45;
        long lsb = value.getLeastSignificantBits();
        UUIDSerializer._appendShort((int)(lsb >>> 48), ch, 19);
        ch[23] = 45;
        UUIDSerializer._appendShort((int)(lsb >>> 32), ch, 24);
        UUIDSerializer._appendInt((int)lsb, ch, 28);
        gen.writeString(ch, 0, 36);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        this.visitStringFormat(visitor, typeHint, JsonValueFormat.UUID);
    }

    private static void _appendInt(int bits, char[] ch, int offset) {
        UUIDSerializer._appendShort(bits >> 16, ch, offset);
        UUIDSerializer._appendShort(bits, ch, offset + 4);
    }

    private static void _appendShort(int bits, char[] ch, int offset) {
        ch[offset] = HEX_CHARS[bits >> 12 & 0xF];
        ch[++offset] = HEX_CHARS[bits >> 8 & 0xF];
        ch[++offset] = HEX_CHARS[bits >> 4 & 0xF];
        ch[++offset] = HEX_CHARS[bits & 0xF];
    }

    private static final byte[] _asBytes(UUID uuid) {
        byte[] buffer = new byte[16];
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        UUIDSerializer._appendInt((int)(hi >> 32), buffer, 0);
        UUIDSerializer._appendInt((int)hi, buffer, 4);
        UUIDSerializer._appendInt((int)(lo >> 32), buffer, 8);
        UUIDSerializer._appendInt((int)lo, buffer, 12);
        return buffer;
    }

    private static final void _appendInt(int value, byte[] buffer, int offset) {
        buffer[offset] = (byte)(value >> 24);
        buffer[++offset] = (byte)(value >> 16);
        buffer[++offset] = (byte)(value >> 8);
        buffer[++offset] = (byte)value;
    }
}

