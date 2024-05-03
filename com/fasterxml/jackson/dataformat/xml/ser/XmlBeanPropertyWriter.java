/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import javax.xml.namespace.QName;

public class XmlBeanPropertyWriter
extends BeanPropertyWriter {
    private static final long serialVersionUID = 1L;
    protected final QName _wrapperQName;
    protected final QName _wrappedQName;

    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped, PropertyName wrapperName, PropertyName wrappedName) {
        this(wrapped, wrapperName, wrappedName, null);
    }

    public XmlBeanPropertyWriter(BeanPropertyWriter wrapped, PropertyName wrapperName, PropertyName wrappedName, JsonSerializer<Object> serializer) {
        super(wrapped);
        this._wrapperQName = this._qname(wrapperName);
        this._wrappedQName = this._qname(wrappedName);
        if (serializer != null) {
            this.assignSerializer(serializer);
        }
    }

    private QName _qname(PropertyName n) {
        String ns = n.getNamespace();
        if (ns == null) {
            ns = "";
        }
        return new QName(ns, n.getSimpleName());
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception {
        ToXmlGenerator xmlGen;
        Class<?> cls;
        PropertySerializerMap map;
        Object value = this.get(bean);
        if (value == null) {
            return;
        }
        JsonSerializer<Object> ser = this._serializer;
        if (ser == null && (ser = (map = this._dynamicSerializers).serializerFor(cls = value.getClass())) == null) {
            ser = this._findAndAddDynamic(map, cls, prov);
        }
        if (this._suppressableValue != null && (MARKER_FOR_EMPTY == this._suppressableValue ? ser.isEmpty(prov, value) : this._suppressableValue.equals(value))) {
            return;
        }
        if (value == bean && this._handleSelfReference(bean, jgen, prov, ser)) {
            return;
        }
        ToXmlGenerator toXmlGenerator = xmlGen = jgen instanceof ToXmlGenerator ? (ToXmlGenerator)jgen : null;
        if (xmlGen != null) {
            xmlGen.startWrappedValue(this._wrapperQName, this._wrappedQName);
        }
        jgen.writeFieldName(this._name);
        if (this._typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        } else {
            ser.serializeWithType(value, jgen, prov, this._typeSerializer);
        }
        if (xmlGen != null) {
            xmlGen.finishWrappedValue(this._wrapperQName, this._wrappedQName);
        }
    }
}

