/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Set;

public class BeanAsArraySerializer
extends BeanSerializerBase {
    private static final long serialVersionUID = 1L;
    protected final BeanSerializerBase _defaultSerializer;

    public BeanAsArraySerializer(BeanSerializerBase src) {
        super(src, (ObjectIdWriter)null);
        this._defaultSerializer = src;
    }

    protected BeanAsArraySerializer(BeanSerializerBase src, Set<String> toIgnore) {
        super(src, toIgnore);
        this._defaultSerializer = src;
    }

    protected BeanAsArraySerializer(BeanSerializerBase src, ObjectIdWriter oiw, Object filterId) {
        super(src, oiw, filterId);
        this._defaultSerializer = src;
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
        return this._defaultSerializer.unwrappingSerializer(transformer);
    }

    @Override
    public boolean isUnwrappingSerializer() {
        return false;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return this._defaultSerializer.withObjectIdWriter(objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new BeanAsArraySerializer((BeanSerializerBase)this, this._objectIdWriter, filterId);
    }

    @Override
    protected BeanAsArraySerializer withIgnorals(Set<String> toIgnore) {
        return new BeanAsArraySerializer((BeanSerializerBase)this, toIgnore);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return this;
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        if (this._objectIdWriter != null) {
            this._serializeWithObjectId(bean, gen, provider, typeSer);
            return;
        }
        WritableTypeId typeIdDef = this._typeIdDef(typeSer, bean, JsonToken.START_ARRAY);
        typeSer.writeTypePrefix(gen, typeIdDef);
        gen.setCurrentValue(bean);
        this.serializeAsArray(bean, gen, provider);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    @Override
    public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) && this.hasSingleElement(provider)) {
            this.serializeAsArray(bean, gen, provider);
            return;
        }
        gen.writeStartArray(bean);
        this.serializeAsArray(bean, gen, provider);
        gen.writeEndArray();
    }

    private boolean hasSingleElement(SerializerProvider provider) {
        BeanPropertyWriter[] props = this._filteredProps != null && provider.getActiveView() != null ? this._filteredProps : this._props;
        return props.length == 1;
    }

    protected final void serializeAsArray(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int i;
        BeanPropertyWriter[] props = this._filteredProps != null && provider.getActiveView() != null ? this._filteredProps : this._props;
        try {
            for (BeanPropertyWriter prop : props) {
                if (prop == null) {
                    gen.writeNull();
                    continue;
                }
                prop.serializeAsElement(bean, gen, provider);
            }
        } catch (Exception e) {
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            this.wrapAndThrow(provider, (Throwable)e, bean, name);
        } catch (StackOverflowError e) {
            JsonMappingException mapE = JsonMappingException.from(gen, "Infinite recursion (StackOverflowError)", (Throwable)e);
            String name = i == props.length ? "[anySetter]" : props[i].getName();
            mapE.prependPath(new JsonMappingException.Reference(bean, name));
            throw mapE;
        }
    }

    public String toString() {
        return "BeanAsArraySerializer for " + this.handledType().getName();
    }
}

