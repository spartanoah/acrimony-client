/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import java.io.IOException;
import java.util.Iterator;

@JacksonStdImpl
public class IterableSerializer
extends AsArraySerializerBase<Iterable<?>> {
    public IterableSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts) {
        super(Iterable.class, elemType, staticTyping, vts, null);
    }

    public IterableSerializer(IterableSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new IterableSerializer(this, this._property, vts, this._elementSerializer, this._unwrapSingle);
    }

    public IterableSerializer withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new IterableSerializer(this, property, vts, elementSerializer, unwrapSingle);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Iterable<?> value) {
        return !value.iterator().hasNext();
    }

    @Override
    public boolean hasSingleElement(Iterable<?> value) {
        Iterator<?> it;
        if (value != null && (it = value.iterator()).hasNext()) {
            it.next();
            if (!it.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final void serialize(Iterable<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if ((this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE) && this.hasSingleElement(value)) {
            this.serializeContents(value, gen, provider);
            return;
        }
        gen.writeStartArray(value);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(Iterable<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        Iterator<?> it = value.iterator();
        if (it.hasNext()) {
            TypeSerializer typeSer = this._valueTypeSerializer;
            JsonSerializer<Object> prevSerializer = null;
            Class<?> prevClass = null;
            do {
                Object elem;
                if ((elem = it.next()) == null) {
                    provider.defaultSerializeNull(jgen);
                    continue;
                }
                JsonSerializer<Object> currSerializer = this._elementSerializer;
                if (currSerializer == null) {
                    Class<?> cc = elem.getClass();
                    if (cc == prevClass) {
                        currSerializer = prevSerializer;
                    } else {
                        prevSerializer = currSerializer = provider.findValueSerializer(cc, this._property);
                        prevClass = cc;
                    }
                }
                if (typeSer == null) {
                    currSerializer.serialize(elem, jgen, provider);
                    continue;
                }
                currSerializer.serializeWithType(elem, jgen, provider, typeSer);
            } while (it.hasNext());
        }
    }
}

