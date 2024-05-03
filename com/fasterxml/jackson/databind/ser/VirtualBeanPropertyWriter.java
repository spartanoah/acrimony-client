/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.util.Annotations;
import java.io.Serializable;

public abstract class VirtualBeanPropertyWriter
extends BeanPropertyWriter
implements Serializable {
    private static final long serialVersionUID = 1L;

    protected VirtualBeanPropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
        this(propDef, contextAnnotations, declaredType, null, null, null, propDef.findInclusion());
    }

    protected VirtualBeanPropertyWriter() {
    }

    protected VirtualBeanPropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType, JsonSerializer<?> ser, TypeSerializer typeSer, JavaType serType, JsonInclude.Value inclusion, Class<?>[] includeInViews) {
        super(propDef, propDef.getPrimaryMember(), contextAnnotations, declaredType, ser, typeSer, serType, VirtualBeanPropertyWriter._suppressNulls(inclusion), VirtualBeanPropertyWriter._suppressableValue(inclusion), includeInViews);
    }

    @Deprecated
    protected VirtualBeanPropertyWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType, JsonSerializer<?> ser, TypeSerializer typeSer, JavaType serType, JsonInclude.Value inclusion) {
        this(propDef, contextAnnotations, declaredType, ser, typeSer, serType, inclusion, null);
    }

    protected VirtualBeanPropertyWriter(VirtualBeanPropertyWriter base) {
        super(base);
    }

    protected VirtualBeanPropertyWriter(VirtualBeanPropertyWriter base, PropertyName name) {
        super((BeanPropertyWriter)base, name);
    }

    protected static boolean _suppressNulls(JsonInclude.Value inclusion) {
        if (inclusion == null) {
            return false;
        }
        JsonInclude.Include incl = inclusion.getValueInclusion();
        return incl != JsonInclude.Include.ALWAYS && incl != JsonInclude.Include.USE_DEFAULTS;
    }

    protected static Object _suppressableValue(JsonInclude.Value inclusion) {
        if (inclusion == null) {
            return false;
        }
        JsonInclude.Include incl = inclusion.getValueInclusion();
        if (incl == JsonInclude.Include.ALWAYS || incl == JsonInclude.Include.NON_NULL || incl == JsonInclude.Include.USE_DEFAULTS) {
            return null;
        }
        return MARKER_FOR_EMPTY;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    protected abstract Object value(Object var1, JsonGenerator var2, SerializerProvider var3) throws Exception;

    public abstract VirtualBeanPropertyWriter withConfig(MapperConfig<?> var1, AnnotatedClass var2, BeanPropertyDefinition var3, JavaType var4);

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        Class<?> cls;
        PropertySerializerMap m;
        Object value = this.value(bean, gen, prov);
        if (value == null) {
            if (this._nullSerializer != null) {
                gen.writeFieldName(this._name);
                this._nullSerializer.serialize(null, gen, prov);
            }
            return;
        }
        JsonSerializer<Object> ser = this._serializer;
        if (ser == null && (ser = (m = this._dynamicSerializers).serializerFor(cls = value.getClass())) == null) {
            ser = this._findAndAddDynamic(m, cls, prov);
        }
        if (this._suppressableValue != null && (MARKER_FOR_EMPTY == this._suppressableValue ? ser.isEmpty(prov, value) : this._suppressableValue.equals(value))) {
            return;
        }
        if (value == bean && this._handleSelfReference(bean, gen, prov, ser)) {
            return;
        }
        gen.writeFieldName(this._name);
        if (this._typeSerializer == null) {
            ser.serialize(value, gen, prov);
        } else {
            ser.serializeWithType(value, gen, prov, this._typeSerializer);
        }
    }

    @Override
    public void serializeAsElement(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        Class<?> cls;
        PropertySerializerMap map;
        Object value = this.value(bean, gen, prov);
        if (value == null) {
            if (this._nullSerializer != null) {
                this._nullSerializer.serialize(null, gen, prov);
            } else {
                gen.writeNull();
            }
            return;
        }
        JsonSerializer<Object> ser = this._serializer;
        if (ser == null && (ser = (map = this._dynamicSerializers).serializerFor(cls = value.getClass())) == null) {
            ser = this._findAndAddDynamic(map, cls, prov);
        }
        if (this._suppressableValue != null) {
            if (MARKER_FOR_EMPTY == this._suppressableValue) {
                if (ser.isEmpty(prov, value)) {
                    this.serializeAsPlaceholder(bean, gen, prov);
                    return;
                }
            } else if (this._suppressableValue.equals(value)) {
                this.serializeAsPlaceholder(bean, gen, prov);
                return;
            }
        }
        if (value == bean && this._handleSelfReference(bean, gen, prov, ser)) {
            return;
        }
        if (this._typeSerializer == null) {
            ser.serialize(value, gen, prov);
        } else {
            ser.serializeWithType(value, gen, prov, this._typeSerializer);
        }
    }
}

