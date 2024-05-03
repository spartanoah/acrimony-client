/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import java.io.IOException;
import java.util.List;

@JacksonStdImpl
public final class IndexedListSerializer
extends AsArraySerializerBase<List<?>> {
    private static final long serialVersionUID = 1L;

    public IndexedListSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> valueSerializer) {
        super(List.class, elemType, staticTyping, vts, valueSerializer);
    }

    public IndexedListSerializer(IndexedListSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
    }

    public IndexedListSerializer withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new IndexedListSerializer(this, property, vts, elementSerializer, unwrapSingle);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, List<?> value) {
        return value.isEmpty();
    }

    @Override
    public boolean hasSingleElement(List<?> value) {
        return value.size() == 1;
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new IndexedListSerializer(this, this._property, vts, this._elementSerializer, this._unwrapSingle);
    }

    @Override
    public final void serialize(List<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int len = value.size();
        if (len == 1 && (this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE)) {
            this.serializeContents(value, gen, provider);
            return;
        }
        gen.writeStartArray(value, len);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(List<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        int i;
        if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, g, provider, this._elementSerializer);
            return;
        }
        if (this._valueTypeSerializer != null) {
            this.serializeTypedContents(value, g, provider);
            return;
        }
        int len = value.size();
        if (len == 0) {
            return;
        }
        try {
            PropertySerializerMap serializers = this._dynamicSerializers;
            for (i = 0; i < len; ++i) {
                Object elem = value.get(i);
                if (elem == null) {
                    provider.defaultSerializeNull(g);
                    continue;
                }
                Class<?> cc = elem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    serializer = this._elementType.hasGenericTypes() ? this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider) : this._findAndAddDynamic(serializers, cc, provider);
                    serializers = this._dynamicSerializers;
                }
                serializer.serialize(elem, g, provider);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, value, i);
        }
    }

    public void serializeContentsUsing(List<?> value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException {
        int len = value.size();
        if (len == 0) {
            return;
        }
        TypeSerializer typeSer = this._valueTypeSerializer;
        for (int i = 0; i < len; ++i) {
            Object elem = value.get(i);
            try {
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                    continue;
                }
                if (typeSer == null) {
                    ser.serialize(elem, jgen, provider);
                    continue;
                }
                ser.serializeWithType(elem, jgen, provider, typeSer);
                continue;
            } catch (Exception e) {
                this.wrapAndThrow(provider, (Throwable)e, value, i);
            }
        }
    }

    public void serializeTypedContents(List<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        int i;
        int len = value.size();
        if (len == 0) {
            return;
        }
        try {
            TypeSerializer typeSer = this._valueTypeSerializer;
            PropertySerializerMap serializers = this._dynamicSerializers;
            for (i = 0; i < len; ++i) {
                Object elem = value.get(i);
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                    continue;
                }
                Class<?> cc = elem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    serializer = this._elementType.hasGenericTypes() ? this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider) : this._findAndAddDynamic(serializers, cc, provider);
                    serializers = this._dynamicSerializers;
                }
                serializer.serializeWithType(elem, jgen, provider, typeSer);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, value, i);
        }
    }
}

