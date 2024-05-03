/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.IOException;
import java.util.Calendar;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class CoreXMLSerializers
extends Serializers.Base {
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> raw = type.getRawClass();
        if (Duration.class.isAssignableFrom(raw) || QName.class.isAssignableFrom(raw)) {
            return ToStringSerializer.instance;
        }
        if (XMLGregorianCalendar.class.isAssignableFrom(raw)) {
            return XMLGregorianCalendarSerializer.instance;
        }
        return null;
    }

    public static class XMLGregorianCalendarSerializer
    extends StdSerializer<XMLGregorianCalendar>
    implements ContextualSerializer {
        static final XMLGregorianCalendarSerializer instance = new XMLGregorianCalendarSerializer();
        final JsonSerializer<Object> _delegate;

        public XMLGregorianCalendarSerializer() {
            this(CalendarSerializer.instance);
        }

        protected XMLGregorianCalendarSerializer(JsonSerializer<?> del) {
            super(XMLGregorianCalendar.class);
            this._delegate = del;
        }

        @Override
        public JsonSerializer<?> getDelegatee() {
            return this._delegate;
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, XMLGregorianCalendar value) {
            return this._delegate.isEmpty(provider, this._convert(value));
        }

        @Override
        public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            this._delegate.serialize(this._convert(value), gen, provider);
        }

        @Override
        public void serializeWithType(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
            this._delegate.serializeWithType(this._convert(value), gen, provider, typeSer);
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            this._delegate.acceptJsonFormatVisitor(visitor, null);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            JsonSerializer<?> ser = prov.handlePrimaryContextualization(this._delegate, property);
            if (ser != this._delegate) {
                return new XMLGregorianCalendarSerializer(ser);
            }
            return this;
        }

        protected Calendar _convert(XMLGregorianCalendar input) {
            return input == null ? null : input.toGregorianCalendar();
        }
    }
}

