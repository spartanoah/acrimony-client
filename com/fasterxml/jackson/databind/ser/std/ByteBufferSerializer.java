/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferSerializer
extends StdScalarSerializer<ByteBuffer> {
    public ByteBufferSerializer() {
        super(ByteBuffer.class);
    }

    @Override
    public void serialize(ByteBuffer bbuf, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (bbuf.hasArray()) {
            int pos = bbuf.position();
            gen.writeBinary(bbuf.array(), bbuf.arrayOffset() + pos, bbuf.limit() - pos);
            return;
        }
        ByteBuffer copy = bbuf.asReadOnlyBuffer();
        if (copy.position() > 0) {
            copy.rewind();
        }
        ByteBufferBackedInputStream in = new ByteBufferBackedInputStream(copy);
        gen.writeBinary(in, copy.remaining());
        in.close();
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
        if (v2 != null) {
            v2.itemsFormat(JsonFormatTypes.INTEGER);
        }
    }
}

