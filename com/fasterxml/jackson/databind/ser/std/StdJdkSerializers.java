/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.ClassSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlDateSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlTimeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class StdJdkSerializers {
    public static Collection<Map.Entry<Class<?>, Object>> all() {
        HashMap<Class<Object>, Object> sers = new HashMap<Class<Object>, Object>();
        sers.put(URL.class, new ToStringSerializer(URL.class));
        sers.put(URI.class, new ToStringSerializer(URI.class));
        sers.put(Currency.class, new ToStringSerializer(Currency.class));
        sers.put(UUID.class, new UUIDSerializer());
        sers.put(Pattern.class, new ToStringSerializer(Pattern.class));
        sers.put(Locale.class, new ToStringSerializer(Locale.class));
        sers.put(AtomicBoolean.class, AtomicBooleanSerializer.class);
        sers.put(AtomicInteger.class, AtomicIntegerSerializer.class);
        sers.put(AtomicLong.class, AtomicLongSerializer.class);
        sers.put(File.class, FileSerializer.class);
        sers.put(Class.class, ClassSerializer.class);
        sers.put(Void.class, NullSerializer.instance);
        sers.put(Void.TYPE, NullSerializer.instance);
        try {
            sers.put(Timestamp.class, DateSerializer.instance);
            sers.put(Date.class, SqlDateSerializer.class);
            sers.put(Time.class, SqlTimeSerializer.class);
        } catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        return sers.entrySet();
    }

    public static class AtomicLongSerializer
    extends StdScalarSerializer<AtomicLong> {
        public AtomicLongSerializer() {
            super(AtomicLong.class, false);
        }

        @Override
        public void serialize(AtomicLong value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonGenerationException {
            gen.writeNumber(value.get());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return this.createSchemaNode("integer", true);
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.LONG);
        }
    }

    public static class AtomicIntegerSerializer
    extends StdScalarSerializer<AtomicInteger> {
        public AtomicIntegerSerializer() {
            super(AtomicInteger.class, false);
        }

        @Override
        public void serialize(AtomicInteger value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonGenerationException {
            gen.writeNumber(value.get());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return this.createSchemaNode("integer", true);
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.INT);
        }
    }

    public static class AtomicBooleanSerializer
    extends StdScalarSerializer<AtomicBoolean> {
        public AtomicBooleanSerializer() {
            super(AtomicBoolean.class, false);
        }

        @Override
        public void serialize(AtomicBoolean value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonGenerationException {
            gen.writeBoolean(value.get());
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return this.createSchemaNode("boolean", true);
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            visitor.expectBooleanFormat(typeHint);
        }
    }
}

