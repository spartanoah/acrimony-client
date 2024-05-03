/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import java.io.IOException;

@JacksonStdImpl
public class ObjectArraySerializer
extends ArraySerializerBase<Object[]>
implements ContextualSerializer {
    protected final boolean _staticTyping;
    protected final JavaType _elementType;
    protected final TypeSerializer _valueTypeSerializer;
    protected JsonSerializer<Object> _elementSerializer;
    protected PropertySerializerMap _dynamicSerializers;

    public ObjectArraySerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> elementSerializer) {
        super(Object[].class);
        this._elementType = elemType;
        this._staticTyping = staticTyping;
        this._valueTypeSerializer = vts;
        this._dynamicSerializers = PropertySerializerMap.emptyForProperties();
        this._elementSerializer = elementSerializer;
    }

    public ObjectArraySerializer(ObjectArraySerializer src, TypeSerializer vts) {
        super(src);
        this._elementType = src._elementType;
        this._valueTypeSerializer = vts;
        this._staticTyping = src._staticTyping;
        this._dynamicSerializers = src._dynamicSerializers;
        this._elementSerializer = src._elementSerializer;
    }

    public ObjectArraySerializer(ObjectArraySerializer src, BeanProperty property, TypeSerializer vts, JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        super(src, property, unwrapSingle);
        this._elementType = src._elementType;
        this._valueTypeSerializer = vts;
        this._staticTyping = src._staticTyping;
        this._dynamicSerializers = PropertySerializerMap.emptyForProperties();
        this._elementSerializer = elementSerializer;
    }

    @Override
    public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
        return new ObjectArraySerializer(this, prop, this._valueTypeSerializer, this._elementSerializer, unwrapSingle);
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new ObjectArraySerializer(this._elementType, this._staticTyping, vts, this._elementSerializer);
    }

    public ObjectArraySerializer withResolved(BeanProperty prop, TypeSerializer vts, JsonSerializer<?> ser, Boolean unwrapSingle) {
        if (this._property == prop && ser == this._elementSerializer && this._valueTypeSerializer == vts && this._unwrapSingle == unwrapSingle) {
            return this;
        }
        return new ObjectArraySerializer(this, prop, vts, ser, unwrapSingle);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
        JsonFormat.Value format;
        TypeSerializer vts = this._valueTypeSerializer;
        if (vts != null) {
            vts = vts.forProperty(property);
        }
        JsonSerializer<Object> ser = null;
        Boolean unwrapSingle = null;
        if (property != null) {
            Object serDef;
            AnnotatedMember m = property.getMember();
            AnnotationIntrospector intr = serializers.getAnnotationIntrospector();
            if (m != null && (serDef = intr.findContentSerializer(m)) != null) {
                ser = serializers.serializerInstance(m, serDef);
            }
        }
        if ((format = this.findFormatOverrides(serializers, property, this.handledType())) != null) {
            unwrapSingle = format.getFeature(JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        }
        if (ser == null) {
            ser = this._elementSerializer;
        }
        if ((ser = this.findContextualConvertingSerializer(serializers, property, ser)) == null && this._elementType != null && this._staticTyping && !this._elementType.isJavaLangObject()) {
            ser = serializers.findContentValueSerializer(this._elementType, property);
        }
        return this.withResolved(property, vts, ser, unwrapSingle);
    }

    @Override
    public JavaType getContentType() {
        return this._elementType;
    }

    @Override
    public JsonSerializer<?> getContentSerializer() {
        return this._elementSerializer;
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object[] value) {
        return value.length == 0;
    }

    @Override
    public boolean hasSingleElement(Object[] value) {
        return value.length == 1;
    }

    @Override
    public final void serialize(Object[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int len = value.length;
        if (len == 1 && (this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE)) {
            this.serializeContents(value, gen, provider);
            return;
        }
        gen.writeStartArray(value, len);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(Object[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int i;
        int len = value.length;
        if (len == 0) {
            return;
        }
        if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, gen, provider, this._elementSerializer);
            return;
        }
        if (this._valueTypeSerializer != null) {
            this.serializeTypedContents(value, gen, provider);
            return;
        }
        Object elem = null;
        try {
            PropertySerializerMap serializers = this._dynamicSerializers;
            for (i = 0; i < len; ++i) {
                elem = value[i];
                if (elem == null) {
                    provider.defaultSerializeNull(gen);
                    continue;
                }
                Class<?> cc = elem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    serializer = this._elementType.hasGenericTypes() ? this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider) : this._findAndAddDynamic(serializers, cc, provider);
                }
                serializer.serialize(elem, gen, provider);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, elem, i);
        }
    }

    public void serializeContentsUsing(Object[] value, JsonGenerator jgen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException {
        int i;
        int len = value.length;
        TypeSerializer typeSer = this._valueTypeSerializer;
        Object elem = null;
        try {
            for (i = 0; i < len; ++i) {
                elem = value[i];
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                    continue;
                }
                if (typeSer == null) {
                    ser.serialize(elem, jgen, provider);
                    continue;
                }
                ser.serializeWithType(elem, jgen, provider, typeSer);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, elem, i);
        }
    }

    public void serializeTypedContents(Object[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        int i;
        int len = value.length;
        TypeSerializer typeSer = this._valueTypeSerializer;
        Object elem = null;
        try {
            PropertySerializerMap serializers = this._dynamicSerializers;
            for (i = 0; i < len; ++i) {
                elem = value[i];
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                    continue;
                }
                Class<?> cc = elem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    serializer = this._findAndAddDynamic(serializers, cc, provider);
                }
                serializer.serializeWithType(elem, jgen, provider, typeSer);
            }
        } catch (Exception e) {
            this.wrapAndThrow(provider, (Throwable)e, elem, i);
        }
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        JsonArrayFormatVisitor arrayVisitor = visitor.expectArrayFormat(typeHint);
        if (arrayVisitor != null) {
            JavaType contentType = this._elementType;
            JsonSerializer<Object> valueSer = this._elementSerializer;
            if (valueSer == null) {
                valueSer = visitor.getProvider().findContentValueSerializer(contentType, this._property);
            }
            arrayVisitor.itemsFormat(valueSer, contentType);
        }
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, Class<?> type, SerializerProvider provider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicSerializers = result.map;
        }
        return result.serializer;
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map, JavaType type, SerializerProvider provider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSecondarySerializer(type, provider, this._property);
        if (map != result.map) {
            this._dynamicSerializers = result.map;
        }
        return result.serializer;
    }
}

