/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

@JacksonStdImpl
public class NumberSerializer
extends StdScalarSerializer<Number>
implements ContextualSerializer {
    public static final NumberSerializer instance = new NumberSerializer((Class<? extends Number>)Number.class);
    protected static final int MAX_BIG_DECIMAL_SCALE = 9999;
    protected final boolean _isInt;

    public NumberSerializer(Class<? extends Number> rawType) {
        super(rawType, false);
        this._isInt = rawType == BigInteger.class;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        JsonFormat.Value format = this.findFormatOverrides(prov, property, this.handledType());
        if (format != null) {
            switch (format.getShape()) {
                case STRING: {
                    if (this.handledType() == BigDecimal.class) {
                        return NumberSerializer.bigDecimalAsStringSerializer();
                    }
                    return ToStringSerializer.instance;
                }
            }
        }
        return this;
    }

    @Override
    public void serialize(Number value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (value instanceof BigDecimal) {
            g.writeNumber((BigDecimal)value);
        } else if (value instanceof BigInteger) {
            g.writeNumber((BigInteger)value);
        } else if (value instanceof Long) {
            g.writeNumber(value.longValue());
        } else if (value instanceof Double) {
            g.writeNumber(value.doubleValue());
        } else if (value instanceof Float) {
            g.writeNumber(value.floatValue());
        } else if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
            g.writeNumber(value.intValue());
        } else {
            g.writeNumber(value.toString());
        }
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode(this._isInt ? "integer" : "number", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        if (this._isInt) {
            this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
        } else if (this.handledType() == BigDecimal.class) {
            this.visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
        } else {
            visitor.expectNumberFormat(typeHint);
        }
    }

    public static JsonSerializer<?> bigDecimalAsStringSerializer() {
        return BigDecimalAsStringSerializer.BD_INSTANCE;
    }

    static final class BigDecimalAsStringSerializer
    extends ToStringSerializerBase {
        static final BigDecimalAsStringSerializer BD_INSTANCE = new BigDecimalAsStringSerializer();

        public BigDecimalAsStringSerializer() {
            super(BigDecimal.class);
        }

        @Override
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return false;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            String text;
            if (gen.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
                BigDecimal bd = (BigDecimal)value;
                if (!this._verifyBigDecimalRange(gen, bd)) {
                    String errorMsg = String.format("Attempt to write plain `java.math.BigDecimal` (see JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) with illegal scale (%d): needs to be between [-%d, %d]", bd.scale(), 9999, 9999);
                    provider.reportMappingProblem(errorMsg, new Object[0]);
                }
                text = bd.toPlainString();
            } else {
                text = value.toString();
            }
            gen.writeString(text);
        }

        @Override
        public String valueToString(Object value) {
            throw new IllegalStateException();
        }

        protected boolean _verifyBigDecimalRange(JsonGenerator gen, BigDecimal value) throws IOException {
            int scale = value.scale();
            return scale >= -9999 && scale <= 9999;
        }
    }
}

