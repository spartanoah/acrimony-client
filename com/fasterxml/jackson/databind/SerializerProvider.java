/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.FailingSerializer;
import com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap;
import com.fasterxml.jackson.databind.ser.impl.TypeWrappedSerializer;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class SerializerProvider
extends DatabindContext {
    protected static final boolean CACHE_UNKNOWN_MAPPINGS = false;
    public static final JsonSerializer<Object> DEFAULT_NULL_KEY_SERIALIZER = new FailingSerializer("Null key for a Map not allowed in JSON (use a converting NullKeySerializer?)");
    protected static final JsonSerializer<Object> DEFAULT_UNKNOWN_SERIALIZER = new UnknownSerializer();
    protected final SerializationConfig _config;
    protected final Class<?> _serializationView;
    protected final SerializerFactory _serializerFactory;
    protected final SerializerCache _serializerCache;
    protected transient ContextAttributes _attributes;
    protected JsonSerializer<Object> _unknownTypeSerializer = DEFAULT_UNKNOWN_SERIALIZER;
    protected JsonSerializer<Object> _keySerializer;
    protected JsonSerializer<Object> _nullValueSerializer = NullSerializer.instance;
    protected JsonSerializer<Object> _nullKeySerializer = DEFAULT_NULL_KEY_SERIALIZER;
    protected final ReadOnlyClassToSerializerMap _knownSerializers;
    protected DateFormat _dateFormat;
    protected final boolean _stdNullValueSerializer;

    public SerializerProvider() {
        this._config = null;
        this._serializerFactory = null;
        this._serializerCache = new SerializerCache();
        this._knownSerializers = null;
        this._serializationView = null;
        this._attributes = null;
        this._stdNullValueSerializer = true;
    }

    protected SerializerProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
        this._serializerFactory = f;
        this._config = config;
        this._serializerCache = src._serializerCache;
        this._unknownTypeSerializer = src._unknownTypeSerializer;
        this._keySerializer = src._keySerializer;
        this._nullValueSerializer = src._nullValueSerializer;
        this._nullKeySerializer = src._nullKeySerializer;
        this._stdNullValueSerializer = this._nullValueSerializer == DEFAULT_NULL_KEY_SERIALIZER;
        this._serializationView = config.getActiveView();
        this._attributes = config.getAttributes();
        this._knownSerializers = this._serializerCache.getReadOnlyLookupMap();
    }

    protected SerializerProvider(SerializerProvider src) {
        this._config = null;
        this._serializationView = null;
        this._serializerFactory = null;
        this._knownSerializers = null;
        this._serializerCache = new SerializerCache();
        this._unknownTypeSerializer = src._unknownTypeSerializer;
        this._keySerializer = src._keySerializer;
        this._nullValueSerializer = src._nullValueSerializer;
        this._nullKeySerializer = src._nullKeySerializer;
        this._stdNullValueSerializer = src._stdNullValueSerializer;
    }

    public void setDefaultKeySerializer(JsonSerializer<Object> ks) {
        if (ks == null) {
            throw new IllegalArgumentException("Cannot pass null JsonSerializer");
        }
        this._keySerializer = ks;
    }

    public void setNullValueSerializer(JsonSerializer<Object> nvs) {
        if (nvs == null) {
            throw new IllegalArgumentException("Cannot pass null JsonSerializer");
        }
        this._nullValueSerializer = nvs;
    }

    public void setNullKeySerializer(JsonSerializer<Object> nks) {
        if (nks == null) {
            throw new IllegalArgumentException("Cannot pass null JsonSerializer");
        }
        this._nullKeySerializer = nks;
    }

    public final SerializationConfig getConfig() {
        return this._config;
    }

    @Override
    public final AnnotationIntrospector getAnnotationIntrospector() {
        return this._config.getAnnotationIntrospector();
    }

    @Override
    public final TypeFactory getTypeFactory() {
        return this._config.getTypeFactory();
    }

    @Override
    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) throws IllegalArgumentException {
        if (baseType.hasRawClass(subclass)) {
            return baseType;
        }
        return this.getConfig().getTypeFactory().constructSpecializedType(baseType, subclass, true);
    }

    @Override
    public final Class<?> getActiveView() {
        return this._serializationView;
    }

    @Deprecated
    public final Class<?> getSerializationView() {
        return this._serializationView;
    }

    @Override
    public final boolean canOverrideAccessModifiers() {
        return this._config.canOverrideAccessModifiers();
    }

    @Override
    public final boolean isEnabled(MapperFeature feature) {
        return this._config.isEnabled(feature);
    }

    @Override
    public final JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) {
        return this._config.getDefaultPropertyFormat(baseType);
    }

    public final JsonInclude.Value getDefaultPropertyInclusion(Class<?> baseType) {
        return this._config.getDefaultPropertyInclusion(baseType);
    }

    @Override
    public Locale getLocale() {
        return this._config.getLocale();
    }

    @Override
    public TimeZone getTimeZone() {
        return this._config.getTimeZone();
    }

    @Override
    public Object getAttribute(Object key) {
        return this._attributes.getAttribute(key);
    }

    @Override
    public SerializerProvider setAttribute(Object key, Object value) {
        this._attributes = this._attributes.withPerCallAttribute(key, value);
        return this;
    }

    public final boolean isEnabled(SerializationFeature feature) {
        return this._config.isEnabled(feature);
    }

    public final boolean hasSerializationFeatures(int featureMask) {
        return this._config.hasSerializationFeatures(featureMask);
    }

    public final FilterProvider getFilterProvider() {
        return this._config.getFilterProvider();
    }

    public JsonGenerator getGenerator() {
        return null;
    }

    public abstract WritableObjectId findObjectId(Object var1, ObjectIdGenerator<?> var2);

    public JsonSerializer<Object> findValueSerializer(Class<?> valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType))) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType);
            return ser;
        }
        return this.handleSecondaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findValueSerializer(JavaType valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser;
        if (valueType == null) {
            this.reportMappingProblem("Null passed for `valueType` of `findValueSerializer()`", new Object[0]);
        }
        if ((ser = this._knownSerializers.untypedValueSerializer(valueType)) == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType.getRawClass());
            return ser;
        }
        return this.handleSecondaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findValueSerializer(Class<?> valueType) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType))) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType);
        }
        return ser;
    }

    public JsonSerializer<Object> findValueSerializer(JavaType valueType) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType.getRawClass());
        }
        return ser;
    }

    public JsonSerializer<Object> findPrimaryPropertySerializer(JavaType valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType.getRawClass());
            return ser;
        }
        return this.handlePrimaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findPrimaryPropertySerializer(Class<?> valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType))) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType);
            return ser;
        }
        return this.handlePrimaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findContentValueSerializer(JavaType valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType.getRawClass());
            return ser;
        }
        return this.handleSecondaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findContentValueSerializer(Class<?> valueType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(valueType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(valueType)) == null && (ser = this._serializerCache.untypedValueSerializer(this._config.constructType(valueType))) == null && (ser = this._createAndCacheUntypedSerializer(valueType)) == null) {
            ser = this.getUnknownTypeSerializer(valueType);
            return ser;
        }
        return this.handleSecondaryContextualization(ser, property);
    }

    public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache, BeanProperty property) throws JsonMappingException {
        JsonSerializer ser = this._knownSerializers.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this._serializerCache.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this.findValueSerializer(valueType, property);
        TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, this._config.constructType(valueType));
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
            ser = new TypeWrappedSerializer(typeSer, ser);
        }
        if (cache) {
            this._serializerCache.addTypedSerializer(valueType, ser);
        }
        return ser;
    }

    public JsonSerializer<Object> findTypedValueSerializer(JavaType valueType, boolean cache, BeanProperty property) throws JsonMappingException {
        JsonSerializer ser = this._knownSerializers.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this._serializerCache.typedValueSerializer(valueType);
        if (ser != null) {
            return ser;
        }
        ser = this.findValueSerializer(valueType, property);
        TypeSerializer typeSer = this._serializerFactory.createTypeSerializer(this._config, valueType);
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
            ser = new TypeWrappedSerializer(typeSer, ser);
        }
        if (cache) {
            this._serializerCache.addTypedSerializer(valueType, ser);
        }
        return ser;
    }

    public TypeSerializer findTypeSerializer(JavaType javaType) throws JsonMappingException {
        return this._serializerFactory.createTypeSerializer(this._config, javaType);
    }

    public JsonSerializer<Object> findKeySerializer(JavaType keyType, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._serializerFactory.createKeySerializer(this, keyType, this._keySerializer);
        return this._handleContextualResolvable(ser, property);
    }

    public JsonSerializer<Object> findKeySerializer(Class<?> rawKeyType, BeanProperty property) throws JsonMappingException {
        return this.findKeySerializer(this._config.constructType(rawKeyType), property);
    }

    public JsonSerializer<Object> getDefaultNullKeySerializer() {
        return this._nullKeySerializer;
    }

    public JsonSerializer<Object> getDefaultNullValueSerializer() {
        return this._nullValueSerializer;
    }

    public JsonSerializer<Object> findNullKeySerializer(JavaType serializationType, BeanProperty property) throws JsonMappingException {
        return this._nullKeySerializer;
    }

    public JsonSerializer<Object> findNullValueSerializer(BeanProperty property) throws JsonMappingException {
        return this._nullValueSerializer;
    }

    public JsonSerializer<Object> getUnknownTypeSerializer(Class<?> unknownType) {
        if (unknownType == Object.class) {
            return this._unknownTypeSerializer;
        }
        return new UnknownSerializer(unknownType);
    }

    public boolean isUnknownTypeSerializer(JsonSerializer<?> ser) {
        if (ser == this._unknownTypeSerializer || ser == null) {
            return true;
        }
        return this.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS) && ser.getClass() == UnknownSerializer.class;
    }

    public abstract JsonSerializer<Object> serializerInstance(Annotated var1, Object var2) throws JsonMappingException;

    public abstract Object includeFilterInstance(BeanPropertyDefinition var1, Class<?> var2) throws JsonMappingException;

    public abstract boolean includeFilterSuppressNulls(Object var1) throws JsonMappingException;

    public JsonSerializer<?> handlePrimaryContextualization(JsonSerializer<?> ser, BeanProperty property) throws JsonMappingException {
        if (ser != null && ser instanceof ContextualSerializer) {
            ser = ((ContextualSerializer)((Object)ser)).createContextual(this, property);
        }
        return ser;
    }

    public JsonSerializer<?> handleSecondaryContextualization(JsonSerializer<?> ser, BeanProperty property) throws JsonMappingException {
        if (ser != null && ser instanceof ContextualSerializer) {
            ser = ((ContextualSerializer)((Object)ser)).createContextual(this, property);
        }
        return ser;
    }

    public final void defaultSerializeValue(Object value, JsonGenerator gen) throws IOException {
        if (value == null) {
            if (this._stdNullValueSerializer) {
                gen.writeNull();
            } else {
                this._nullValueSerializer.serialize(null, gen, this);
            }
        } else {
            Class<?> cls = value.getClass();
            this.findTypedValueSerializer(cls, true, null).serialize(value, gen, this);
        }
    }

    public final void defaultSerializeField(String fieldName, Object value, JsonGenerator gen) throws IOException {
        gen.writeFieldName(fieldName);
        if (value == null) {
            if (this._stdNullValueSerializer) {
                gen.writeNull();
            } else {
                this._nullValueSerializer.serialize(null, gen, this);
            }
        } else {
            Class<?> cls = value.getClass();
            this.findTypedValueSerializer(cls, true, null).serialize(value, gen, this);
        }
    }

    public final void defaultSerializeDateValue(long timestamp, JsonGenerator gen) throws IOException {
        if (this.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            gen.writeNumber(timestamp);
        } else {
            gen.writeString(this._dateFormat().format(new Date(timestamp)));
        }
    }

    public final void defaultSerializeDateValue(Date date, JsonGenerator gen) throws IOException {
        if (this.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            gen.writeNumber(date.getTime());
        } else {
            gen.writeString(this._dateFormat().format(date));
        }
    }

    public void defaultSerializeDateKey(long timestamp, JsonGenerator gen) throws IOException {
        if (this.isEnabled(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)) {
            gen.writeFieldName(String.valueOf(timestamp));
        } else {
            gen.writeFieldName(this._dateFormat().format(new Date(timestamp)));
        }
    }

    public void defaultSerializeDateKey(Date date, JsonGenerator gen) throws IOException {
        if (this.isEnabled(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)) {
            gen.writeFieldName(String.valueOf(date.getTime()));
        } else {
            gen.writeFieldName(this._dateFormat().format(date));
        }
    }

    public final void defaultSerializeNull(JsonGenerator gen) throws IOException {
        if (this._stdNullValueSerializer) {
            gen.writeNull();
        } else {
            this._nullValueSerializer.serialize(null, gen, this);
        }
    }

    public void reportMappingProblem(String message, Object ... args) throws JsonMappingException {
        throw this.mappingException(message, args);
    }

    public <T> T reportBadTypeDefinition(BeanDescription bean, String msg, Object ... msgArgs) throws JsonMappingException {
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        }
        msg = String.format("Invalid type definition for type %s: %s", beanDesc, this._format(msg, msgArgs));
        throw InvalidDefinitionException.from(this.getGenerator(), msg, bean, null);
    }

    public <T> T reportBadPropertyDefinition(BeanDescription bean, BeanPropertyDefinition prop, String message, Object ... msgArgs) throws JsonMappingException {
        message = this._format(message, msgArgs);
        String propName = "N/A";
        if (prop != null) {
            propName = this._quotedString(prop.getName());
        }
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        }
        message = String.format("Invalid definition for property %s (of type %s): %s", propName, beanDesc, message);
        throw InvalidDefinitionException.from(this.getGenerator(), message, bean, prop);
    }

    @Override
    public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
        throw InvalidDefinitionException.from(this.getGenerator(), msg, type);
    }

    public <T> T reportBadDefinition(JavaType type, String msg, Throwable cause) throws JsonMappingException {
        InvalidDefinitionException e = InvalidDefinitionException.from(this.getGenerator(), msg, type);
        e.initCause(cause);
        throw e;
    }

    public <T> T reportBadDefinition(Class<?> raw, String msg, Throwable cause) throws JsonMappingException {
        InvalidDefinitionException e = InvalidDefinitionException.from(this.getGenerator(), msg, this.constructType(raw));
        e.initCause(cause);
        throw e;
    }

    public void reportMappingProblem(Throwable t, String message, Object ... msgArgs) throws JsonMappingException {
        message = this._format(message, msgArgs);
        throw JsonMappingException.from(this.getGenerator(), message, t);
    }

    @Override
    public JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
        String msg = String.format("Could not resolve type id '%s' as a subtype of %s", typeId, ClassUtil.getTypeDescription(baseType));
        return InvalidTypeIdException.from(null, this._colonConcat(msg, extraDesc), baseType, typeId);
    }

    @Deprecated
    public JsonMappingException mappingException(String message, Object ... msgArgs) {
        return JsonMappingException.from(this.getGenerator(), this._format(message, msgArgs));
    }

    @Deprecated
    protected JsonMappingException mappingException(Throwable t, String message, Object ... msgArgs) {
        return JsonMappingException.from(this.getGenerator(), this._format(message, msgArgs), t);
    }

    protected void _reportIncompatibleRootType(Object value, JavaType rootType) throws IOException {
        Class<?> wrapperType;
        if (rootType.isPrimitive() && (wrapperType = ClassUtil.wrapperType(rootType.getRawClass())).isAssignableFrom(value.getClass())) {
            return;
        }
        this.reportBadDefinition(rootType, String.format("Incompatible types: declared root type (%s) vs %s", rootType, ClassUtil.classNameOf(value)));
    }

    protected JsonSerializer<Object> _findExplicitUntypedSerializer(Class<?> runtimeType) throws JsonMappingException {
        JsonSerializer<Object> ser = this._knownSerializers.untypedValueSerializer(runtimeType);
        if (ser == null && (ser = this._serializerCache.untypedValueSerializer(runtimeType)) == null) {
            ser = this._createAndCacheUntypedSerializer(runtimeType);
        }
        if (this.isUnknownTypeSerializer(ser)) {
            return null;
        }
        return ser;
    }

    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(Class<?> rawType) throws JsonMappingException {
        JsonSerializer<Object> ser;
        JavaType fullType = this._config.constructType(rawType);
        try {
            ser = this._createUntypedSerializer(fullType);
        } catch (IllegalArgumentException iae) {
            ser = null;
            this.reportMappingProblem(iae, ClassUtil.exceptionMessage(iae), new Object[0]);
        }
        if (ser != null) {
            this._serializerCache.addAndResolveNonTypedSerializer(rawType, fullType, ser, this);
        }
        return ser;
    }

    protected JsonSerializer<Object> _createAndCacheUntypedSerializer(JavaType type) throws JsonMappingException {
        JsonSerializer<Object> ser;
        try {
            ser = this._createUntypedSerializer(type);
        } catch (IllegalArgumentException iae) {
            ser = null;
            this.reportMappingProblem(iae, ClassUtil.exceptionMessage(iae), new Object[0]);
        }
        if (ser != null) {
            this._serializerCache.addAndResolveNonTypedSerializer(type, ser, this);
        }
        return ser;
    }

    protected JsonSerializer<Object> _createUntypedSerializer(JavaType type) throws JsonMappingException {
        return this._serializerFactory.createSerializer(this, type);
    }

    protected JsonSerializer<Object> _handleContextualResolvable(JsonSerializer<?> ser, BeanProperty property) throws JsonMappingException {
        if (ser instanceof ResolvableSerializer) {
            ((ResolvableSerializer)((Object)ser)).resolve(this);
        }
        return this.handleSecondaryContextualization(ser, property);
    }

    protected JsonSerializer<Object> _handleResolvable(JsonSerializer<?> ser) throws JsonMappingException {
        if (ser instanceof ResolvableSerializer) {
            ((ResolvableSerializer)((Object)ser)).resolve(this);
        }
        return ser;
    }

    protected final DateFormat _dateFormat() {
        if (this._dateFormat != null) {
            return this._dateFormat;
        }
        DateFormat df = this._config.getDateFormat();
        this._dateFormat = df = (DateFormat)df.clone();
        return df;
    }
}

