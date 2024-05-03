/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public abstract class StdDeserializer<T>
extends JsonDeserializer<T>
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static final int F_MASK_INT_COERCIONS = DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.getMask() | DeserializationFeature.USE_LONG_FOR_INTS.getMask();
    protected static final int F_MASK_ACCEPT_ARRAYS = DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS.getMask() | DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.getMask();
    protected final Class<?> _valueClass;
    protected final JavaType _valueType;

    protected StdDeserializer(Class<?> vc) {
        this._valueClass = vc;
        this._valueType = null;
    }

    protected StdDeserializer(JavaType valueType) {
        this._valueClass = valueType == null ? Object.class : valueType.getRawClass();
        this._valueType = valueType;
    }

    protected StdDeserializer(StdDeserializer<?> src) {
        this._valueClass = src._valueClass;
        this._valueType = src._valueType;
    }

    @Override
    public Class<?> handledType() {
        return this._valueClass;
    }

    @Deprecated
    public final Class<?> getValueClass() {
        return this._valueClass;
    }

    public JavaType getValueType() {
        return this._valueType;
    }

    public JavaType getValueType(DeserializationContext ctxt) {
        if (this._valueType != null) {
            return this._valueType;
        }
        return ctxt.constructType(this._valueClass);
    }

    protected boolean isDefaultDeserializer(JsonDeserializer<?> deserializer) {
        return ClassUtil.isJacksonStdImpl(deserializer);
    }

    protected boolean isDefaultKeyDeserializer(KeyDeserializer keyDeser) {
        return ClassUtil.isJacksonStdImpl(keyDeser);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }

    @Deprecated
    protected final boolean _parseBooleanPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this._parseBooleanPrimitive(ctxt, p, Boolean.TYPE);
    }

    protected final boolean _parseBooleanPrimitive(DeserializationContext ctxt, JsonParser p, Class<?> targetType) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return false;
        }
        if (t == JsonToken.VALUE_NULL) {
            this._verifyNullForPrimitive(ctxt);
            return false;
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return this._parseBooleanFromInt(p, ctxt);
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if ("true".equals(text) || "True".equals(text)) {
                return true;
            }
            if ("false".equals(text) || "False".equals(text)) {
                return false;
            }
            if (this._isEmptyOrTextualNull(text)) {
                this._verifyNullForPrimitiveCoercion(ctxt, text);
                return false;
            }
            Boolean b = (Boolean)ctxt.handleWeirdStringValue(targetType, text, "only \"true\" or \"false\" recognized", new Object[0]);
            return Boolean.TRUE.equals(b);
        }
        if (t == JsonToken.START_ARRAY && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            p.nextToken();
            boolean parsed = this._parseBooleanPrimitive(ctxt, p, targetType);
            this._verifyEndArrayForSingle(p, ctxt);
            return parsed;
        }
        return (Boolean)ctxt.handleUnexpectedToken(targetType, p);
    }

    protected boolean _parseBooleanFromInt(JsonParser p, DeserializationContext ctxt) throws IOException {
        this._verifyNumberForScalarCoercion(ctxt, p);
        return !"0".equals(p.getText());
    }

    protected final byte _parseBytePrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        int value = this._parseIntPrimitive(p, ctxt);
        if (this._byteOverflow(value)) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, String.valueOf(value), "overflow, value cannot be represented as 8-bit value", new Object[0]);
            return this._nonNullNumber(v).byteValue();
        }
        return (byte)value;
    }

    protected final short _parseShortPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        int value = this._parseIntPrimitive(p, ctxt);
        if (this._shortOverflow(value)) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, String.valueOf(value), "overflow, value cannot be represented as 16-bit value", new Object[0]);
            return this._nonNullNumber(v).shortValue();
        }
        return (short)value;
    }

    protected final int _parseIntPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return p.getIntValue();
        }
        switch (p.getCurrentTokenId()) {
            case 6: {
                String text = p.getText().trim();
                if (this._isEmptyOrTextualNull(text)) {
                    this._verifyNullForPrimitiveCoercion(ctxt, text);
                    return 0;
                }
                return this._parseIntPrimitive(ctxt, text);
            }
            case 8: {
                if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                    this._failDoubleToIntCoercion(p, ctxt, "int");
                }
                return p.getValueAsInt();
            }
            case 11: {
                this._verifyNullForPrimitive(ctxt);
                return 0;
            }
            case 3: {
                if (!ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) break;
                p.nextToken();
                int parsed = this._parseIntPrimitive(p, ctxt);
                this._verifyEndArrayForSingle(p, ctxt);
                return parsed;
            }
        }
        return ((Number)ctxt.handleUnexpectedToken(this._valueClass, p)).intValue();
    }

    protected final int _parseIntPrimitive(DeserializationContext ctxt, String text) throws IOException {
        try {
            if (text.length() > 9) {
                long l = Long.parseLong(text);
                if (this._intOverflow(l)) {
                    Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, text, "Overflow: numeric value (%s) out of range of int (%d -%d)", text, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    return this._nonNullNumber(v).intValue();
                }
                return (int)l;
            }
            return NumberInput.parseInt(text);
        } catch (IllegalArgumentException iae) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid int value", new Object[0]);
            return this._nonNullNumber(v).intValue();
        }
    }

    protected final long _parseLongPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return p.getLongValue();
        }
        switch (p.getCurrentTokenId()) {
            case 6: {
                String text = p.getText().trim();
                if (this._isEmptyOrTextualNull(text)) {
                    this._verifyNullForPrimitiveCoercion(ctxt, text);
                    return 0L;
                }
                return this._parseLongPrimitive(ctxt, text);
            }
            case 8: {
                if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                    this._failDoubleToIntCoercion(p, ctxt, "long");
                }
                return p.getValueAsLong();
            }
            case 11: {
                this._verifyNullForPrimitive(ctxt);
                return 0L;
            }
            case 3: {
                if (!ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) break;
                p.nextToken();
                long parsed = this._parseLongPrimitive(p, ctxt);
                this._verifyEndArrayForSingle(p, ctxt);
                return parsed;
            }
        }
        return ((Number)ctxt.handleUnexpectedToken(this._valueClass, p)).longValue();
    }

    protected final long _parseLongPrimitive(DeserializationContext ctxt, String text) throws IOException {
        try {
            return NumberInput.parseLong(text);
        } catch (IllegalArgumentException illegalArgumentException) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid long value", new Object[0]);
            return this._nonNullNumber(v).longValue();
        }
    }

    protected final float _parseFloatPrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            return p.getFloatValue();
        }
        switch (p.getCurrentTokenId()) {
            case 6: {
                String text = p.getText().trim();
                if (this._isEmptyOrTextualNull(text)) {
                    this._verifyNullForPrimitiveCoercion(ctxt, text);
                    return 0.0f;
                }
                return this._parseFloatPrimitive(ctxt, text);
            }
            case 7: {
                return p.getFloatValue();
            }
            case 11: {
                this._verifyNullForPrimitive(ctxt);
                return 0.0f;
            }
            case 3: {
                if (!ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) break;
                p.nextToken();
                float parsed = this._parseFloatPrimitive(p, ctxt);
                this._verifyEndArrayForSingle(p, ctxt);
                return parsed;
            }
        }
        return ((Number)ctxt.handleUnexpectedToken(this._valueClass, p)).floatValue();
    }

    protected final float _parseFloatPrimitive(DeserializationContext ctxt, String text) throws IOException {
        switch (text.charAt(0)) {
            case 'I': {
                if (!this._isPosInf(text)) break;
                return Float.POSITIVE_INFINITY;
            }
            case 'N': {
                if (!this._isNaN(text)) break;
                return Float.NaN;
            }
            case '-': {
                if (!this._isNegInf(text)) break;
                return Float.NEGATIVE_INFINITY;
            }
        }
        try {
            return Float.parseFloat(text);
        } catch (IllegalArgumentException illegalArgumentException) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid float value", new Object[0]);
            return this._nonNullNumber(v).floatValue();
        }
    }

    protected final double _parseDoublePrimitive(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            return p.getDoubleValue();
        }
        switch (p.getCurrentTokenId()) {
            case 6: {
                String text = p.getText().trim();
                if (this._isEmptyOrTextualNull(text)) {
                    this._verifyNullForPrimitiveCoercion(ctxt, text);
                    return 0.0;
                }
                return this._parseDoublePrimitive(ctxt, text);
            }
            case 7: {
                return p.getDoubleValue();
            }
            case 11: {
                this._verifyNullForPrimitive(ctxt);
                return 0.0;
            }
            case 3: {
                if (!ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) break;
                p.nextToken();
                double parsed = this._parseDoublePrimitive(p, ctxt);
                this._verifyEndArrayForSingle(p, ctxt);
                return parsed;
            }
        }
        return ((Number)ctxt.handleUnexpectedToken(this._valueClass, p)).doubleValue();
    }

    protected final double _parseDoublePrimitive(DeserializationContext ctxt, String text) throws IOException {
        switch (text.charAt(0)) {
            case 'I': {
                if (!this._isPosInf(text)) break;
                return Double.POSITIVE_INFINITY;
            }
            case 'N': {
                if (!this._isNaN(text)) break;
                return Double.NaN;
            }
            case '-': {
                if (!this._isNegInf(text)) break;
                return Double.NEGATIVE_INFINITY;
            }
        }
        try {
            return StdDeserializer.parseDouble(text);
        } catch (IllegalArgumentException illegalArgumentException) {
            Number v = (Number)ctxt.handleWeirdStringValue(this._valueClass, text, "not a valid double value (as String to convert)", new Object[0]);
            return this._nonNullNumber(v).doubleValue();
        }
    }

    protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.getCurrentTokenId()) {
            case 6: {
                return this._parseDate(p.getText().trim(), ctxt);
            }
            case 7: {
                long ts;
                try {
                    ts = p.getLongValue();
                } catch (JsonParseException | InputCoercionException e) {
                    Number v = (Number)ctxt.handleWeirdNumberValue(this._valueClass, p.getNumberValue(), "not a valid 64-bit long for creating `java.util.Date`", new Object[0]);
                    ts = v.longValue();
                }
                return new Date(ts);
            }
            case 11: {
                return (Date)this.getNullValue(ctxt);
            }
            case 3: {
                return this._parseDateFromArray(p, ctxt);
            }
        }
        return (Date)ctxt.handleUnexpectedToken(this._valueClass, p);
    }

    protected Date _parseDateFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t;
        if (ctxt.hasSomeOfFeatures(F_MASK_ACCEPT_ARRAYS)) {
            t = p.nextToken();
            if (t == JsonToken.END_ARRAY && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                return (Date)this.getNullValue(ctxt);
            }
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                Date parsed = this._parseDate(p, ctxt);
                this._verifyEndArrayForSingle(p, ctxt);
                return parsed;
            }
        } else {
            t = p.getCurrentToken();
        }
        return (Date)ctxt.handleUnexpectedToken(this._valueClass, t, p, null, new Object[0]);
    }

    protected Date _parseDate(String value, DeserializationContext ctxt) throws IOException {
        try {
            if (this._isEmptyOrTextualNull(value)) {
                return (Date)this.getNullValue(ctxt);
            }
            return ctxt.parseDate(value);
        } catch (IllegalArgumentException iae) {
            return (Date)ctxt.handleWeirdStringValue(this._valueClass, value, "not a valid representation (error: %s)", ClassUtil.exceptionMessage(iae));
        }
    }

    protected static final double parseDouble(String numStr) throws NumberFormatException {
        if ("2.2250738585072012e-308".equals(numStr)) {
            return Double.MIN_NORMAL;
        }
        return Double.parseDouble(numStr);
    }

    protected final String _parseString(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            return p.getText();
        }
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob instanceof byte[]) {
                return ctxt.getBase64Variant().encode((byte[])ob, false);
            }
            if (ob == null) {
                return null;
            }
            return ob.toString();
        }
        String value = p.getValueAsString();
        if (value != null) {
            return value;
        }
        return (String)ctxt.handleUnexpectedToken(String.class, p);
    }

    protected T _deserializeFromEmpty(JsonParser p, DeserializationContext ctxt) throws IOException {
        String str;
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_ARRAY) {
            if (ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                t = p.nextToken();
                if (t == JsonToken.END_ARRAY) {
                    return null;
                }
                return (T)ctxt.handleUnexpectedToken(this.handledType(), p);
            }
        } else if (t == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && (str = p.getText().trim()).isEmpty()) {
            return null;
        }
        return (T)ctxt.handleUnexpectedToken(this.handledType(), p);
    }

    protected boolean _hasTextualNull(String value) {
        return "null".equals(value);
    }

    protected boolean _isEmptyOrTextualNull(String value) {
        return value.isEmpty() || "null".equals(value);
    }

    protected final boolean _isNegInf(String text) {
        return "-Infinity".equals(text) || "-INF".equals(text);
    }

    protected final boolean _isPosInf(String text) {
        return "Infinity".equals(text) || "INF".equals(text);
    }

    protected final boolean _isNaN(String text) {
        return "NaN".equals(text);
    }

    protected T _deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (ctxt.hasSomeOfFeatures(F_MASK_ACCEPT_ARRAYS)) {
            JsonToken t = p.nextToken();
            if (t == JsonToken.END_ARRAY && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                return this.getNullValue(ctxt);
            }
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                Object parsed = this.deserialize(p, ctxt);
                if (p.nextToken() != JsonToken.END_ARRAY) {
                    this.handleMissingEndArrayForSingle(p, ctxt);
                }
                return parsed;
            }
        } else {
            JsonToken t = p.getCurrentToken();
        }
        Object result = ctxt.handleUnexpectedToken(this.getValueType(ctxt), p.getCurrentToken(), p, null, new Object[0]);
        return (T)result;
    }

    protected T _deserializeWrappedValue(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.START_ARRAY)) {
            String msg = String.format("Cannot deserialize instance of %s out of %s token: nested Arrays not allowed with %s", new Object[]{ClassUtil.nameOf(this._valueClass), JsonToken.START_ARRAY, "DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS"});
            Object result = ctxt.handleUnexpectedToken(this.getValueType(ctxt), p.getCurrentToken(), p, msg, new Object[0]);
            return (T)result;
        }
        return this.deserialize(p, ctxt);
    }

    protected void _failDoubleToIntCoercion(JsonParser p, DeserializationContext ctxt, String type) throws IOException {
        ctxt.reportInputMismatch(this.handledType(), "Cannot coerce a floating-point value ('%s') into %s (enable `DeserializationFeature.ACCEPT_FLOAT_AS_INT` to allow)", p.getValueAsString(), type);
    }

    protected Object _coerceIntegral(JsonParser p, DeserializationContext ctxt) throws IOException {
        int feats = ctxt.getDeserializationFeatures();
        if (DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.enabledIn(feats)) {
            return p.getBigIntegerValue();
        }
        if (DeserializationFeature.USE_LONG_FOR_INTS.enabledIn(feats)) {
            return p.getLongValue();
        }
        return p.getBigIntegerValue();
    }

    protected Object _coerceNullToken(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
        if (isPrimitive) {
            this._verifyNullForPrimitive(ctxt);
        }
        return this.getNullValue(ctxt);
    }

    protected Object _coerceTextualNull(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
        boolean enable;
        Enum feat;
        if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
            enable = true;
        } else if (isPrimitive && ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            feat = DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
            enable = false;
        } else {
            return this.getNullValue(ctxt);
        }
        this._reportFailedNullCoerce(ctxt, enable, feat, "String \"null\"");
        return null;
    }

    protected Object _coerceEmptyString(DeserializationContext ctxt, boolean isPrimitive) throws JsonMappingException {
        boolean enable;
        Enum feat;
        if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
            enable = true;
        } else if (isPrimitive && ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            feat = DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
            enable = false;
        } else {
            return this.getNullValue(ctxt);
        }
        this._reportFailedNullCoerce(ctxt, enable, feat, "empty String (\"\")");
        return null;
    }

    protected final void _verifyNullForPrimitive(DeserializationContext ctxt) throws JsonMappingException {
        if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            ctxt.reportInputMismatch(this, "Cannot coerce `null` %s (disable `DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES` to allow)", this._coercedTypeDesc());
        }
    }

    protected final void _verifyNullForPrimitiveCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
        boolean enable;
        Enum feat;
        if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
            enable = true;
        } else if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)) {
            feat = DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
            enable = false;
        } else {
            return;
        }
        String strDesc = str.isEmpty() ? "empty String (\"\")" : String.format("String \"%s\"", str);
        this._reportFailedNullCoerce(ctxt, enable, feat, strDesc);
    }

    protected final void _verifyNullForScalarCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
        if (!ctxt.isEnabled(MapperFeature.ALLOW_COERCION_OF_SCALARS)) {
            String strDesc = str.isEmpty() ? "empty String (\"\")" : String.format("String \"%s\"", str);
            this._reportFailedNullCoerce(ctxt, true, MapperFeature.ALLOW_COERCION_OF_SCALARS, strDesc);
        }
    }

    protected void _verifyStringForScalarCoercion(DeserializationContext ctxt, String str) throws JsonMappingException {
        MapperFeature feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
        if (!ctxt.isEnabled(feat)) {
            ctxt.reportInputMismatch(this, "Cannot coerce String \"%s\" %s (enable `%s.%s` to allow)", str, this._coercedTypeDesc(), feat.getClass().getSimpleName(), feat.name());
        }
    }

    protected void _verifyNumberForScalarCoercion(DeserializationContext ctxt, JsonParser p) throws IOException {
        MapperFeature feat = MapperFeature.ALLOW_COERCION_OF_SCALARS;
        if (!ctxt.isEnabled(feat)) {
            String valueDesc = p.getText();
            ctxt.reportInputMismatch(this, "Cannot coerce Number (%s) %s (enable `%s.%s` to allow)", valueDesc, this._coercedTypeDesc(), feat.getClass().getSimpleName(), feat.name());
        }
    }

    protected void _reportFailedNullCoerce(DeserializationContext ctxt, boolean state, Enum<?> feature, String inputDesc) throws JsonMappingException {
        String enableDesc = state ? "enable" : "disable";
        ctxt.reportInputMismatch(this, "Cannot coerce %s to Null value %s (%s `%s.%s` to allow)", inputDesc, this._coercedTypeDesc(), enableDesc, feature.getClass().getSimpleName(), feature.name());
    }

    protected String _coercedTypeDesc() {
        String typeDesc;
        boolean structured;
        JavaType t = this.getValueType();
        if (t != null && !t.isPrimitive()) {
            structured = t.isContainerType() || t.isReferenceType();
            typeDesc = "'" + t.toString() + "'";
        } else {
            Class<?> cls = this.handledType();
            structured = cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls);
            typeDesc = ClassUtil.nameOf(cls);
        }
        if (structured) {
            return "as content of type " + typeDesc;
        }
        return "for type " + typeDesc;
    }

    protected JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, JavaType type, BeanProperty property) throws JsonMappingException {
        return ctxt.findContextualValueDeserializer(type, property);
    }

    protected final boolean _isIntNumber(String text) {
        int len = text.length();
        if (len > 0) {
            int i;
            char c = text.charAt(0);
            int n = i = c == '-' || c == '+' ? 1 : 0;
            while (i < len) {
                char ch = text.charAt(i);
                if (ch > '9' || ch < '0') {
                    return false;
                }
                ++i;
            }
            return true;
        }
        return false;
    }

    protected JsonDeserializer<?> findConvertingContentDeserializer(DeserializationContext ctxt, BeanProperty prop, JsonDeserializer<?> existingDeserializer) throws JsonMappingException {
        Object convDef;
        AnnotatedMember member;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (StdDeserializer._neitherNull(intr, prop) && (member = prop.getMember()) != null && (convDef = intr.findDeserializationContentConverter(member)) != null) {
            Converter<Object, Object> conv = ctxt.converterInstance(prop.getMember(), convDef);
            JavaType delegateType = conv.getInputType(ctxt.getTypeFactory());
            if (existingDeserializer == null) {
                existingDeserializer = ctxt.findContextualValueDeserializer(delegateType, prop);
            }
            return new StdDelegatingDeserializer<Object>(conv, delegateType, existingDeserializer);
        }
        return existingDeserializer;
    }

    protected JsonFormat.Value findFormatOverrides(DeserializationContext ctxt, BeanProperty prop, Class<?> typeForDefaults) {
        if (prop != null) {
            return prop.findPropertyFormat(ctxt.getConfig(), typeForDefaults);
        }
        return ctxt.getDefaultPropertyFormat(typeForDefaults);
    }

    protected Boolean findFormatFeature(DeserializationContext ctxt, BeanProperty prop, Class<?> typeForDefaults, JsonFormat.Feature feat) {
        JsonFormat.Value format = this.findFormatOverrides(ctxt, prop, typeForDefaults);
        if (format != null) {
            return format.getFeature(feat);
        }
        return null;
    }

    protected final NullValueProvider findValueNullProvider(DeserializationContext ctxt, SettableBeanProperty prop, PropertyMetadata propMetadata) throws JsonMappingException {
        if (prop != null) {
            return this._findNullProvider(ctxt, prop, propMetadata.getValueNulls(), prop.getValueDeserializer());
        }
        return null;
    }

    protected NullValueProvider findContentNullProvider(DeserializationContext ctxt, BeanProperty prop, JsonDeserializer<?> valueDeser) throws JsonMappingException {
        Nulls nulls = this.findContentNullStyle(ctxt, prop);
        if (nulls == Nulls.SKIP) {
            return NullsConstantProvider.skipper();
        }
        if (nulls == Nulls.FAIL) {
            if (prop == null) {
                JavaType type = ctxt.constructType(valueDeser.handledType());
                if (type.isContainerType()) {
                    type = type.getContentType();
                }
                return NullsFailProvider.constructForRootValue(type);
            }
            return NullsFailProvider.constructForProperty(prop, prop.getType().getContentType());
        }
        NullValueProvider prov = this._findNullProvider(ctxt, prop, nulls, valueDeser);
        if (prov != null) {
            return prov;
        }
        return valueDeser;
    }

    protected Nulls findContentNullStyle(DeserializationContext ctxt, BeanProperty prop) throws JsonMappingException {
        if (prop != null) {
            return prop.getMetadata().getContentNulls();
        }
        return null;
    }

    protected final NullValueProvider _findNullProvider(DeserializationContext ctxt, BeanProperty prop, Nulls nulls, JsonDeserializer<?> valueDeser) throws JsonMappingException {
        if (nulls == Nulls.FAIL) {
            if (prop == null) {
                return NullsFailProvider.constructForRootValue(ctxt.constructType(valueDeser.handledType()));
            }
            return NullsFailProvider.constructForProperty(prop);
        }
        if (nulls == Nulls.AS_EMPTY) {
            AccessPattern access;
            ValueInstantiator vi;
            if (valueDeser == null) {
                return null;
            }
            if (valueDeser instanceof BeanDeserializerBase && !(vi = ((BeanDeserializerBase)valueDeser).getValueInstantiator()).canCreateUsingDefault()) {
                JavaType type = prop.getType();
                ctxt.reportBadDefinition(type, String.format("Cannot create empty instance of %s, no default Creator", type));
            }
            if ((access = valueDeser.getEmptyAccessPattern()) == AccessPattern.ALWAYS_NULL) {
                return NullsConstantProvider.nuller();
            }
            if (access == AccessPattern.CONSTANT) {
                return NullsConstantProvider.forValue(valueDeser.getEmptyValue(ctxt));
            }
            return new NullsAsEmptyProvider(valueDeser);
        }
        if (nulls == Nulls.SKIP) {
            return NullsConstantProvider.skipper();
        }
        return null;
    }

    protected void handleUnknownProperty(JsonParser p, DeserializationContext ctxt, Object instanceOrClass, String propName) throws IOException {
        if (instanceOrClass == null) {
            instanceOrClass = this.handledType();
        }
        if (ctxt.handleUnknownProperty(p, this, instanceOrClass, propName)) {
            return;
        }
        p.skipChildren();
    }

    protected void handleMissingEndArrayForSingle(JsonParser p, DeserializationContext ctxt) throws IOException {
        ctxt.reportWrongTokenException(this, JsonToken.END_ARRAY, "Attempted to unwrap '%s' value from an array (with `DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS`) but it contains more than one value", this.handledType().getName());
    }

    protected void _verifyEndArrayForSingle(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.nextToken();
        if (t != JsonToken.END_ARRAY) {
            this.handleMissingEndArrayForSingle(p, ctxt);
        }
    }

    protected static final boolean _neitherNull(Object a, Object b) {
        return a != null && b != null;
    }

    protected final boolean _byteOverflow(int value) {
        return value < -128 || value > 255;
    }

    protected final boolean _shortOverflow(int value) {
        return value < Short.MIN_VALUE || value > Short.MAX_VALUE;
    }

    protected final boolean _intOverflow(long value) {
        return value < Integer.MIN_VALUE || value > Integer.MAX_VALUE;
    }

    protected Number _nonNullNumber(Number n) {
        if (n == null) {
            n = 0;
        }
        return n;
    }
}

