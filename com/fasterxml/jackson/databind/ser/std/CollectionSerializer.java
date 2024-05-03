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
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class CollectionSerializer
extends AsArraySerializerBase<Collection<?>> {
    private static final long serialVersionUID = 1L;

    public CollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> valueSerializer) {
        super(Collection.class, elemType, staticTyping, vts, valueSerializer);
    }

    @Deprecated
    public CollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, BeanProperty property, JsonSerializer<Object> valueSerializer) {
        this(elemType, staticTyping, vts, valueSerializer);
    }

    public CollectionSerializer(CollectionSerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new CollectionSerializer(this, this._property, vts, this._elementSerializer, this._unwrapSingle);
    }

    public CollectionSerializer withResolved(BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new CollectionSerializer(this, property, vts, elementSerializer, unwrapSingle);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Collection<?> value) {
        return value.isEmpty();
    }

    @Override
    public boolean hasSingleElement(Collection<?> value) {
        return value.size() == 1;
    }

    @Override
    public final void serialize(Collection<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        int len = value.size();
        if (len == 1 && (this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE)) {
            this.serializeContents(value, g, provider);
            return;
        }
        g.writeStartArray(value, len);
        this.serializeContents(value, g, provider);
        g.writeEndArray();
    }

    @Override
    public void serializeContents(Collection<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.setCurrentValue(value);
        if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, g, provider, this._elementSerializer);
            return;
        }
        Iterator<?> it = value.iterator();
        if (!it.hasNext()) {
            return;
        }
        PropertySerializerMap serializers = this._dynamicSerializers;
        TypeSerializer typeSer = this._valueTypeSerializer;
        int i = 0;
        try {
            do {
                Object elem;
                if ((elem = it.next()) == null) {
                    provider.defaultSerializeNull(g);
                } else {
                    Class<?> cc = elem.getClass();
                    JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                    if (serializer == null) {
                        serializer = this._elementType.hasGenericTypes() ? this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider) : this._findAndAddDynamic(serializers, cc, provider);
                        serializers = this._dynamicSerializers;
                    }
                    if (typeSer == null) {
                        serializer.serialize(elem, g, provider);
                    } else {
                        serializer.serializeWithType(elem, g, provider, typeSer);
                    }
                }
                ++i;
            } while (it.hasNext());
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, value, i);
        }
    }

    public void serializeContentsUsing(Collection<?> value, JsonGenerator g, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException {
        Iterator<?> it = value.iterator();
        if (it.hasNext()) {
            TypeSerializer typeSer = this._valueTypeSerializer;
            int i = 0;
            do {
                Object elem = it.next();
                try {
                    if (elem == null) {
                        provider.defaultSerializeNull(g);
                    } else if (typeSer == null) {
                        ser.serialize(elem, g, provider);
                    } else {
                        ser.serializeWithType(elem, g, provider, typeSer);
                    }
                    ++i;
                } catch (Exception e) {
                    this.wrapAndThrow(provider, (Throwable)e, value, i);
                }
            } while (it.hasNext());
        }
    }
}

