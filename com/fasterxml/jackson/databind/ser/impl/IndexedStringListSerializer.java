/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StaticListSerializerBase;
import java.io.IOException;
import java.util.List;

@JacksonStdImpl
public final class IndexedStringListSerializer
extends StaticListSerializerBase<List<String>> {
    private static final long serialVersionUID = 1L;
    public static final IndexedStringListSerializer instance = new IndexedStringListSerializer();

    protected IndexedStringListSerializer() {
        super(List.class);
    }

    public IndexedStringListSerializer(IndexedStringListSerializer src, Boolean unwrapSingle) {
        super(src, unwrapSingle);
    }

    @Override
    public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
        return new IndexedStringListSerializer(this, unwrapSingle);
    }

    @Override
    protected JsonNode contentSchema() {
        return this.createSchemaNode("string", true);
    }

    @Override
    protected void acceptContentVisitor(JsonArrayFormatVisitor visitor) throws JsonMappingException {
        visitor.itemsFormat(JsonFormatTypes.STRING);
    }

    @Override
    public void serialize(List<String> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        int len = value.size();
        if (len == 1 && (this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE)) {
            this.serializeContents(value, g, provider, 1);
            return;
        }
        g.writeStartArray(value, len);
        this.serializeContents(value, g, provider, len);
        g.writeEndArray();
    }

    @Override
    public void serializeWithType(List<String> value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.START_ARRAY));
        g.setCurrentValue(value);
        this.serializeContents(value, g, provider, value.size());
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    private final void serializeContents(List<String> value, JsonGenerator g, SerializerProvider provider, int len) throws IOException {
        int i;
        try {
            for (i = 0; i < len; ++i) {
                String str = value.get(i);
                if (str == null) {
                    provider.defaultSerializeNull(g);
                    continue;
                }
                g.writeString(str);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, value, i);
        }
    }
}

