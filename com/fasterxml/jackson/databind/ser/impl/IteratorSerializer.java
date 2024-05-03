/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import java.io.IOException;
import java.util.Iterator;

@JacksonStdImpl
public class IteratorSerializer
extends AsArraySerializerBase<Iterator<?>> {
    public IteratorSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts) {
        super(Iterator.class, elemType, staticTyping, vts, null);
    }

    public IteratorSerializer(IteratorSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Iterator<?> value) {
        return !value.hasNext();
    }

    @Override
    public boolean hasSingleElement(Iterator<?> value) {
        return false;
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new IteratorSerializer(this, this._property, vts, this._elementSerializer, this._unwrapSingle);
    }

    public IteratorSerializer withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new IteratorSerializer(this, property, vts, elementSerializer, unwrapSingle);
    }

    @Override
    public final void serialize(Iterator<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray(value);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(Iterator<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (!value.hasNext()) {
            return;
        }
        JsonSerializer serializer = this._elementSerializer;
        if (serializer == null) {
            this._serializeDynamicContents(value, g, provider);
            return;
        }
        TypeSerializer typeSer = this._valueTypeSerializer;
        do {
            Object elem;
            if ((elem = value.next()) == null) {
                provider.defaultSerializeNull(g);
                continue;
            }
            if (typeSer == null) {
                serializer.serialize(elem, g, provider);
                continue;
            }
            serializer.serializeWithType(elem, g, provider, typeSer);
        } while (value.hasNext());
    }

    protected void _serializeDynamicContents(Iterator<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        TypeSerializer typeSer = this._valueTypeSerializer;
        PropertySerializerMap serializers = this._dynamicSerializers;
        do {
            Object elem;
            if ((elem = value.next()) == null) {
                provider.defaultSerializeNull(g);
                continue;
            }
            Class<?> cc = elem.getClass();
            JsonSerializer<Object> serializer = serializers.serializerFor(cc);
            if (serializer == null) {
                serializer = this._elementType.hasGenericTypes() ? this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider) : this._findAndAddDynamic(serializers, cc, provider);
                serializers = this._dynamicSerializers;
            }
            if (typeSer == null) {
                serializer.serialize(elem, g, provider);
                continue;
            }
            serializer.serializeWithType(elem, g, provider, typeSer);
        } while (value.hasNext());
    }
}

