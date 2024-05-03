/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualKeyDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.TypeWrappedDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.LinkedNode;
import com.fasterxml.jackson.databind.util.ObjectBuffer;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DeserializationContext
extends DatabindContext
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final DeserializerCache _cache;
    protected final DeserializerFactory _factory;
    protected final DeserializationConfig _config;
    protected final int _featureFlags;
    protected final Class<?> _view;
    protected transient JsonParser _parser;
    protected final InjectableValues _injectableValues;
    protected transient ArrayBuilders _arrayBuilders;
    protected transient ObjectBuffer _objectBuffer;
    protected transient DateFormat _dateFormat;
    protected transient ContextAttributes _attributes;
    protected LinkedNode<JavaType> _currentType;

    protected DeserializationContext(DeserializerFactory df) {
        this(df, null);
    }

    protected DeserializationContext(DeserializerFactory df, DeserializerCache cache) {
        if (df == null) {
            throw new NullPointerException("Cannot pass null DeserializerFactory");
        }
        this._factory = df;
        if (cache == null) {
            cache = new DeserializerCache();
        }
        this._cache = cache;
        this._featureFlags = 0;
        this._config = null;
        this._injectableValues = null;
        this._view = null;
        this._attributes = null;
    }

    protected DeserializationContext(DeserializationContext src, DeserializerFactory factory) {
        this._cache = src._cache;
        this._factory = factory;
        this._config = src._config;
        this._featureFlags = src._featureFlags;
        this._view = src._view;
        this._parser = src._parser;
        this._injectableValues = src._injectableValues;
        this._attributes = src._attributes;
    }

    protected DeserializationContext(DeserializationContext src, DeserializationConfig config, JsonParser p, InjectableValues injectableValues) {
        this._cache = src._cache;
        this._factory = src._factory;
        this._config = config;
        this._featureFlags = config.getDeserializationFeatures();
        this._view = config.getActiveView();
        this._parser = p;
        this._injectableValues = injectableValues;
        this._attributes = config.getAttributes();
    }

    protected DeserializationContext(DeserializationContext src) {
        this._cache = new DeserializerCache();
        this._factory = src._factory;
        this._config = src._config;
        this._featureFlags = src._featureFlags;
        this._view = src._view;
        this._injectableValues = null;
    }

    public DeserializationConfig getConfig() {
        return this._config;
    }

    @Override
    public final Class<?> getActiveView() {
        return this._view;
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
        return this.getConfig().getTypeFactory().constructSpecializedType(baseType, subclass, false);
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
    public DeserializationContext setAttribute(Object key, Object value) {
        this._attributes = this._attributes.withPerCallAttribute(key, value);
        return this;
    }

    public JavaType getContextualType() {
        return this._currentType == null ? null : this._currentType.value();
    }

    public DeserializerFactory getFactory() {
        return this._factory;
    }

    public final boolean isEnabled(DeserializationFeature feat) {
        return (this._featureFlags & feat.getMask()) != 0;
    }

    public final int getDeserializationFeatures() {
        return this._featureFlags;
    }

    public final boolean hasDeserializationFeatures(int featureMask) {
        return (this._featureFlags & featureMask) == featureMask;
    }

    public final boolean hasSomeOfFeatures(int featureMask) {
        return (this._featureFlags & featureMask) != 0;
    }

    public final JsonParser getParser() {
        return this._parser;
    }

    public final Object findInjectableValue(Object valueId, BeanProperty forProperty, Object beanInstance) throws JsonMappingException {
        if (this._injectableValues == null) {
            this.reportBadDefinition(ClassUtil.classOf(valueId), String.format("No 'injectableValues' configured, cannot inject value with id [%s]", valueId));
        }
        return this._injectableValues.findInjectableValue(valueId, this, forProperty, beanInstance);
    }

    public final Base64Variant getBase64Variant() {
        return this._config.getBase64Variant();
    }

    public final JsonNodeFactory getNodeFactory() {
        return this._config.getNodeFactory();
    }

    public boolean hasValueDeserializerFor(JavaType type, AtomicReference<Throwable> cause) {
        try {
            return this._cache.hasValueDeserializerFor(this, this._factory, type);
        } catch (JsonMappingException e) {
            if (cause != null) {
                cause.set(e);
            }
        } catch (RuntimeException e) {
            if (cause == null) {
                throw e;
            }
            cause.set(e);
        }
        return false;
    }

    public final JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._cache.findValueDeserializer(this, this._factory, type);
        if (deser != null) {
            deser = this.handleSecondaryContextualization(deser, prop, type);
        }
        return deser;
    }

    public final JsonDeserializer<Object> findNonContextualValueDeserializer(JavaType type) throws JsonMappingException {
        return this._cache.findValueDeserializer(this, this._factory, type);
    }

    public final JsonDeserializer<Object> findRootValueDeserializer(JavaType type) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._cache.findValueDeserializer(this, this._factory, type);
        if (deser == null) {
            return null;
        }
        deser = this.handleSecondaryContextualization(deser, null, type);
        TypeDeserializer typeDeser = this._factory.findTypeDeserializer(this._config, type);
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(null);
            return new TypeWrappedDeserializer(typeDeser, deser);
        }
        return deser;
    }

    public final KeyDeserializer findKeyDeserializer(JavaType keyType, BeanProperty prop) throws JsonMappingException {
        KeyDeserializer kd = this._cache.findKeyDeserializer(this, this._factory, keyType);
        if (kd instanceof ContextualKeyDeserializer) {
            kd = ((ContextualKeyDeserializer)((Object)kd)).createContextual(this, prop);
        }
        return kd;
    }

    public abstract ReadableObjectId findObjectId(Object var1, ObjectIdGenerator<?> var2, ObjectIdResolver var3);

    public abstract void checkUnresolvedObjectId() throws UnresolvedForwardReference;

    public final JavaType constructType(Class<?> cls) {
        return cls == null ? null : this._config.constructType(cls);
    }

    public Class<?> findClass(String className) throws ClassNotFoundException {
        return this.getTypeFactory().findClass(className);
    }

    public final ObjectBuffer leaseObjectBuffer() {
        ObjectBuffer buf = this._objectBuffer;
        if (buf == null) {
            buf = new ObjectBuffer();
        } else {
            this._objectBuffer = null;
        }
        return buf;
    }

    public final void returnObjectBuffer(ObjectBuffer buf) {
        if (this._objectBuffer == null || buf.initialCapacity() >= this._objectBuffer.initialCapacity()) {
            this._objectBuffer = buf;
        }
    }

    public final ArrayBuilders getArrayBuilders() {
        if (this._arrayBuilders == null) {
            this._arrayBuilders = new ArrayBuilders();
        }
        return this._arrayBuilders;
    }

    public abstract JsonDeserializer<Object> deserializerInstance(Annotated var1, Object var2) throws JsonMappingException;

    public abstract KeyDeserializer keyDeserializerInstance(Annotated var1, Object var2) throws JsonMappingException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonDeserializer<?> handlePrimaryContextualization(JsonDeserializer<?> deser, BeanProperty prop, JavaType type) throws JsonMappingException {
        if (deser instanceof ContextualDeserializer) {
            this._currentType = new LinkedNode<JavaType>(type, this._currentType);
            try {
                deser = ((ContextualDeserializer)((Object)deser)).createContextual(this, prop);
            } finally {
                this._currentType = this._currentType.next();
            }
        }
        return deser;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonDeserializer<?> handleSecondaryContextualization(JsonDeserializer<?> deser, BeanProperty prop, JavaType type) throws JsonMappingException {
        if (deser instanceof ContextualDeserializer) {
            this._currentType = new LinkedNode<JavaType>(type, this._currentType);
            try {
                deser = ((ContextualDeserializer)((Object)deser)).createContextual(this, prop);
            } finally {
                this._currentType = this._currentType.next();
            }
        }
        return deser;
    }

    public Date parseDate(String dateStr) throws IllegalArgumentException {
        try {
            DateFormat df = this.getDateFormat();
            return df.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Failed to parse Date value '%s': %s", dateStr, ClassUtil.exceptionMessage(e)));
        }
    }

    public Calendar constructCalendar(Date d) {
        Calendar c = Calendar.getInstance(this.getTimeZone());
        c.setTime(d);
        return c;
    }

    public <T> T readValue(JsonParser p, Class<T> type) throws IOException {
        return this.readValue(p, this.getTypeFactory().constructType(type));
    }

    public <T> T readValue(JsonParser p, JavaType type) throws IOException {
        JsonDeserializer<Object> deser = this.findRootValueDeserializer(type);
        if (deser == null) {
            this.reportBadDefinition(type, "Could not find JsonDeserializer for type " + ClassUtil.getTypeDescription(type));
        }
        return (T)deser.deserialize(p, this);
    }

    public <T> T readPropertyValue(JsonParser p, BeanProperty prop, Class<T> type) throws IOException {
        return this.readPropertyValue(p, prop, this.getTypeFactory().constructType(type));
    }

    public <T> T readPropertyValue(JsonParser p, BeanProperty prop, JavaType type) throws IOException {
        JsonDeserializer<Object> deser = this.findContextualValueDeserializer(type, prop);
        if (deser == null) {
            return this.reportBadDefinition(type, String.format("Could not find JsonDeserializer for type %s (via property %s)", ClassUtil.getTypeDescription(type), ClassUtil.nameOf(prop)));
        }
        return (T)deser.deserialize(p, this);
    }

    public JsonNode readTree(JsonParser p) throws IOException {
        JsonToken t = p.currentToken();
        if (t == null && (t = p.nextToken()) == null) {
            return this.getNodeFactory().missingNode();
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNodeFactory().nullNode();
        }
        return (JsonNode)this.findRootValueDeserializer(this._config.constructType(JsonNode.class)).deserialize(p, this);
    }

    public boolean handleUnknownProperty(JsonParser p, JsonDeserializer<?> deser, Object instanceOrClass, String propName) throws IOException {
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            if (!h.value().handleUnknownProperty(this, p, deser, instanceOrClass, propName)) continue;
            return true;
        }
        if (!this.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            p.skipChildren();
            return true;
        }
        Collection<Object> propIds = deser == null ? null : deser.getKnownPropertyNames();
        throw UnrecognizedPropertyException.from(this._parser, instanceOrClass, propName, propIds);
    }

    public Object handleWeirdKey(Class<?> keyClass, String keyValue, String msg, Object ... msgArgs) throws IOException {
        msg = this._format(msg, msgArgs);
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object key = h.value().handleWeirdKey(this, keyClass, keyValue, msg);
            if (key == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (key == null || keyClass.isInstance(key)) {
                return key;
            }
            throw this.weirdStringException(keyValue, keyClass, String.format("DeserializationProblemHandler.handleWeirdStringValue() for type %s returned value of type %s", ClassUtil.getClassDescription(keyClass), ClassUtil.getClassDescription(key)));
        }
        throw this.weirdKeyException(keyClass, keyValue, msg);
    }

    public Object handleWeirdStringValue(Class<?> targetClass, String value, String msg, Object ... msgArgs) throws IOException {
        msg = this._format(msg, msgArgs);
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object instance = h.value().handleWeirdStringValue(this, targetClass, value, msg);
            if (instance == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (this._isCompatible(targetClass, instance)) {
                return instance;
            }
            throw this.weirdStringException(value, targetClass, String.format("DeserializationProblemHandler.handleWeirdStringValue() for type %s returned value of type %s", ClassUtil.getClassDescription(targetClass), ClassUtil.getClassDescription(instance)));
        }
        throw this.weirdStringException(value, targetClass, msg);
    }

    public Object handleWeirdNumberValue(Class<?> targetClass, Number value, String msg, Object ... msgArgs) throws IOException {
        msg = this._format(msg, msgArgs);
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object key = h.value().handleWeirdNumberValue(this, targetClass, value, msg);
            if (key == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (this._isCompatible(targetClass, key)) {
                return key;
            }
            throw this.weirdNumberException(value, targetClass, this._format("DeserializationProblemHandler.handleWeirdNumberValue() for type %s returned value of type %s", ClassUtil.getClassDescription(targetClass), ClassUtil.getClassDescription(key)));
        }
        throw this.weirdNumberException(value, targetClass, msg);
    }

    public Object handleWeirdNativeValue(JavaType targetType, Object badValue, JsonParser p) throws IOException {
        Class<?> raw = targetType.getRawClass();
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object goodValue = h.value().handleWeirdNativeValue(this, targetType, badValue, p);
            if (goodValue == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (goodValue == null || raw.isInstance(goodValue)) {
                return goodValue;
            }
            throw JsonMappingException.from(p, this._format("DeserializationProblemHandler.handleWeirdNativeValue() for type %s returned value of type %s", ClassUtil.getClassDescription(targetType), ClassUtil.getClassDescription(goodValue)));
        }
        throw this.weirdNativeValueException(badValue, raw);
    }

    public Object handleMissingInstantiator(Class<?> instClass, ValueInstantiator valueInst, JsonParser p, String msg, Object ... msgArgs) throws IOException {
        if (p == null) {
            p = this.getParser();
        }
        msg = this._format(msg, msgArgs);
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object instance = h.value().handleMissingInstantiator(this, instClass, valueInst, p, msg);
            if (instance == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (this._isCompatible(instClass, instance)) {
                return instance;
            }
            this.reportBadDefinition(this.constructType(instClass), String.format("DeserializationProblemHandler.handleMissingInstantiator() for type %s returned value of type %s", ClassUtil.getClassDescription(instClass), ClassUtil.getClassDescription(instance)));
        }
        if (valueInst == null) {
            msg = String.format("Cannot construct instance of %s: %s", ClassUtil.nameOf(instClass), msg);
            return this.reportBadDefinition(instClass, msg);
        }
        if (!valueInst.canInstantiate()) {
            msg = String.format("Cannot construct instance of %s (no Creators, like default constructor, exist): %s", ClassUtil.nameOf(instClass), msg);
            return this.reportBadDefinition(instClass, msg);
        }
        msg = String.format("Cannot construct instance of %s (although at least one Creator exists): %s", ClassUtil.nameOf(instClass), msg);
        return this.reportInputMismatch(instClass, msg, new Object[0]);
    }

    public Object handleInstantiationProblem(Class<?> instClass, Object argument, Throwable t) throws IOException {
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object instance = h.value().handleInstantiationProblem(this, instClass, argument, t);
            if (instance == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (this._isCompatible(instClass, instance)) {
                return instance;
            }
            this.reportBadDefinition(this.constructType(instClass), String.format("DeserializationProblemHandler.handleInstantiationProblem() for type %s returned value of type %s", ClassUtil.getClassDescription(instClass), ClassUtil.classNameOf(instance)));
        }
        ClassUtil.throwIfIOE(t);
        if (!this.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)) {
            ClassUtil.throwIfRTE(t);
        }
        throw this.instantiationException(instClass, t);
    }

    public Object handleUnexpectedToken(Class<?> instClass, JsonParser p) throws IOException {
        return this.handleUnexpectedToken(this.constructType(instClass), p.getCurrentToken(), p, null, new Object[0]);
    }

    public Object handleUnexpectedToken(Class<?> instClass, JsonToken t, JsonParser p, String msg, Object ... msgArgs) throws IOException {
        return this.handleUnexpectedToken(this.constructType(instClass), t, p, msg, msgArgs);
    }

    public Object handleUnexpectedToken(JavaType targetType, JsonParser p) throws IOException {
        return this.handleUnexpectedToken(targetType, p.getCurrentToken(), p, null, new Object[0]);
    }

    public Object handleUnexpectedToken(JavaType targetType, JsonToken t, JsonParser p, String msg, Object ... msgArgs) throws IOException {
        msg = this._format(msg, msgArgs);
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            Object instance = h.value().handleUnexpectedToken(this, targetType, t, p, msg);
            if (instance == DeserializationProblemHandler.NOT_HANDLED) continue;
            if (this._isCompatible(targetType.getRawClass(), instance)) {
                return instance;
            }
            this.reportBadDefinition(targetType, String.format("DeserializationProblemHandler.handleUnexpectedToken() for type %s returned value of type %s", ClassUtil.getClassDescription(targetType), ClassUtil.classNameOf(instance)));
        }
        if (msg == null) {
            msg = t == null ? String.format("Unexpected end-of-input when binding data into %s", ClassUtil.getTypeDescription(targetType)) : String.format("Cannot deserialize instance of %s out of %s token", new Object[]{ClassUtil.getTypeDescription(targetType), t});
        }
        if (t != null && t.isScalarValue()) {
            p.getText();
        }
        this.reportInputMismatch(targetType, msg, new Object[0]);
        return null;
    }

    public JavaType handleUnknownTypeId(JavaType baseType, String id, TypeIdResolver idResolver, String extraDesc) throws IOException {
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            JavaType type = h.value().handleUnknownTypeId(this, baseType, id, idResolver, extraDesc);
            if (type == null) continue;
            if (type.hasRawClass(Void.class)) {
                return null;
            }
            if (type.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return type;
            }
            throw this.invalidTypeIdException(baseType, id, "problem handler tried to resolve into non-subtype: " + ClassUtil.getTypeDescription(type));
        }
        if (!this.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)) {
            return null;
        }
        throw this.invalidTypeIdException(baseType, id, extraDesc);
    }

    public JavaType handleMissingTypeId(JavaType baseType, TypeIdResolver idResolver, String extraDesc) throws IOException {
        for (LinkedNode<DeserializationProblemHandler> h = this._config.getProblemHandlers(); h != null; h = h.next()) {
            JavaType type = h.value().handleMissingTypeId(this, baseType, idResolver, extraDesc);
            if (type == null) continue;
            if (type.hasRawClass(Void.class)) {
                return null;
            }
            if (type.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return type;
            }
            throw this.invalidTypeIdException(baseType, null, "problem handler tried to resolve into non-subtype: " + ClassUtil.getTypeDescription(type));
        }
        throw this.missingTypeIdException(baseType, extraDesc);
    }

    public void handleBadMerge(JsonDeserializer<?> deser) throws JsonMappingException {
        if (!this.isEnabled(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE)) {
            JavaType type = this.constructType(deser.handledType());
            String msg = String.format("Invalid configuration: values of type %s cannot be merged", ClassUtil.getTypeDescription(type));
            throw InvalidDefinitionException.from(this.getParser(), msg, type);
        }
    }

    protected boolean _isCompatible(Class<?> target, Object value) {
        if (value == null || target.isInstance(value)) {
            return true;
        }
        return target.isPrimitive() && ClassUtil.wrapperType(target).isInstance(value);
    }

    public void reportWrongTokenException(JsonDeserializer<?> deser, JsonToken expToken, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw this.wrongTokenException(this.getParser(), deser.handledType(), expToken, msg);
    }

    public void reportWrongTokenException(JavaType targetType, JsonToken expToken, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw this.wrongTokenException(this.getParser(), targetType, expToken, msg);
    }

    public void reportWrongTokenException(Class<?> targetType, JsonToken expToken, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw this.wrongTokenException(this.getParser(), targetType, expToken, msg);
    }

    public <T> T reportUnresolvedObjectId(ObjectIdReader oidReader, Object bean) throws JsonMappingException {
        String msg = String.format("No Object Id found for an instance of %s, to assign to property '%s'", ClassUtil.classNameOf(bean), oidReader.propertyName);
        return this.reportInputMismatch(oidReader.idProperty, msg, new Object[0]);
    }

    public <T> T reportInputMismatch(JsonDeserializer<?> src, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw MismatchedInputException.from(this.getParser(), src.handledType(), msg);
    }

    public <T> T reportInputMismatch(Class<?> targetType, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw MismatchedInputException.from(this.getParser(), targetType, msg);
    }

    public <T> T reportInputMismatch(JavaType targetType, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw MismatchedInputException.from(this.getParser(), targetType, msg);
    }

    public <T> T reportInputMismatch(BeanProperty prop, String msg, Object ... msgArgs) throws JsonMappingException {
        AnnotatedMember member;
        msg = this._format(msg, msgArgs);
        JavaType type = prop == null ? null : prop.getType();
        MismatchedInputException e = MismatchedInputException.from(this.getParser(), type, msg);
        if (prop != null && (member = prop.getMember()) != null) {
            e.prependPath(member.getDeclaringClass(), prop.getName());
        }
        throw e;
    }

    public <T> T reportPropertyInputMismatch(Class<?> targetType, String propertyName, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        MismatchedInputException e = MismatchedInputException.from(this.getParser(), targetType, msg);
        if (propertyName != null) {
            e.prependPath(targetType, propertyName);
        }
        throw e;
    }

    public <T> T reportPropertyInputMismatch(JavaType targetType, String propertyName, String msg, Object ... msgArgs) throws JsonMappingException {
        return this.reportPropertyInputMismatch(targetType.getRawClass(), propertyName, msg, msgArgs);
    }

    public <T> T reportTrailingTokens(Class<?> targetType, JsonParser p, JsonToken trailingToken) throws JsonMappingException {
        throw MismatchedInputException.from(p, targetType, String.format("Trailing token (of type %s) found after value (bound as %s): not allowed as per `DeserializationFeature.FAIL_ON_TRAILING_TOKENS`", new Object[]{trailingToken, ClassUtil.nameOf(targetType)}));
    }

    @Deprecated
    public void reportWrongTokenException(JsonParser p, JsonToken expToken, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        throw this.wrongTokenException(p, expToken, msg);
    }

    @Deprecated
    public void reportUnknownProperty(Object instanceOrClass, String fieldName, JsonDeserializer<?> deser) throws JsonMappingException {
        if (this.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            Collection<Object> propIds = deser == null ? null : deser.getKnownPropertyNames();
            throw UnrecognizedPropertyException.from(this._parser, instanceOrClass, fieldName, propIds);
        }
    }

    @Deprecated
    public void reportMissingContent(String msg, Object ... msgArgs) throws JsonMappingException {
        throw MismatchedInputException.from(this.getParser(), (JavaType)null, "No content to map due to end-of-input");
    }

    public <T> T reportBadTypeDefinition(BeanDescription bean, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        String beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        msg = String.format("Invalid type definition for type %s: %s", beanDesc, msg);
        throw InvalidDefinitionException.from(this._parser, msg, bean, null);
    }

    public <T> T reportBadPropertyDefinition(BeanDescription bean, BeanPropertyDefinition prop, String msg, Object ... msgArgs) throws JsonMappingException {
        msg = this._format(msg, msgArgs);
        String propName = ClassUtil.nameOf(prop);
        String beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        msg = String.format("Invalid definition for property %s (of type %s): %s", propName, beanDesc, msg);
        throw InvalidDefinitionException.from(this._parser, msg, bean, prop);
    }

    @Override
    public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
        throw InvalidDefinitionException.from(this._parser, msg, type);
    }

    @Deprecated
    public <T> T reportBadMerge(JsonDeserializer<?> deser) throws JsonMappingException {
        this.handleBadMerge(deser);
        return null;
    }

    public JsonMappingException wrongTokenException(JsonParser p, JavaType targetType, JsonToken expToken, String extra) {
        String msg = String.format("Unexpected token (%s), expected %s", new Object[]{p.getCurrentToken(), expToken});
        msg = this._colonConcat(msg, extra);
        return MismatchedInputException.from(p, targetType, msg);
    }

    public JsonMappingException wrongTokenException(JsonParser p, Class<?> targetType, JsonToken expToken, String extra) {
        String msg = String.format("Unexpected token (%s), expected %s", new Object[]{p.getCurrentToken(), expToken});
        msg = this._colonConcat(msg, extra);
        return MismatchedInputException.from(p, targetType, msg);
    }

    @Deprecated
    public JsonMappingException wrongTokenException(JsonParser p, JsonToken expToken, String msg) {
        return this.wrongTokenException(p, (JavaType)null, expToken, msg);
    }

    public JsonMappingException weirdKeyException(Class<?> keyClass, String keyValue, String msg) {
        return InvalidFormatException.from(this._parser, String.format("Cannot deserialize Map key of type %s from String %s: %s", ClassUtil.nameOf(keyClass), this._quotedString(keyValue), msg), keyValue, keyClass);
    }

    public JsonMappingException weirdStringException(String value, Class<?> instClass, String msgBase) {
        String msg = String.format("Cannot deserialize value of type %s from String %s: %s", ClassUtil.nameOf(instClass), this._quotedString(value), msgBase);
        return InvalidFormatException.from(this._parser, msg, value, instClass);
    }

    public JsonMappingException weirdNumberException(Number value, Class<?> instClass, String msg) {
        return InvalidFormatException.from(this._parser, String.format("Cannot deserialize value of type %s from number %s: %s", ClassUtil.nameOf(instClass), String.valueOf(value), msg), value, instClass);
    }

    public JsonMappingException weirdNativeValueException(Object value, Class<?> instClass) {
        return InvalidFormatException.from(this._parser, String.format("Cannot deserialize value of type %s from native value (`JsonToken.VALUE_EMBEDDED_OBJECT`) of type %s: incompatible types", ClassUtil.nameOf(instClass), ClassUtil.classNameOf(value)), value, instClass);
    }

    public JsonMappingException instantiationException(Class<?> instClass, Throwable cause) {
        String excMsg;
        if (cause == null) {
            excMsg = "N/A";
        } else {
            excMsg = ClassUtil.exceptionMessage(cause);
            if (excMsg == null) {
                excMsg = ClassUtil.nameOf(cause.getClass());
            }
        }
        String msg = String.format("Cannot construct instance of %s, problem: %s", ClassUtil.nameOf(instClass), excMsg);
        return ValueInstantiationException.from(this._parser, msg, this.constructType(instClass), cause);
    }

    public JsonMappingException instantiationException(Class<?> instClass, String msg0) {
        return ValueInstantiationException.from(this._parser, String.format("Cannot construct instance of %s: %s", ClassUtil.nameOf(instClass), msg0), this.constructType(instClass));
    }

    @Override
    public JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
        String msg = String.format("Could not resolve type id '%s' as a subtype of %s", typeId, ClassUtil.getTypeDescription(baseType));
        return InvalidTypeIdException.from(this._parser, this._colonConcat(msg, extraDesc), baseType, typeId);
    }

    public JsonMappingException missingTypeIdException(JavaType baseType, String extraDesc) {
        String msg = String.format("Missing type id when trying to resolve subtype of %s", baseType);
        return InvalidTypeIdException.from(this._parser, this._colonConcat(msg, extraDesc), baseType, null);
    }

    @Deprecated
    public JsonMappingException unknownTypeException(JavaType type, String id, String extraDesc) {
        String msg = String.format("Could not resolve type id '%s' into a subtype of %s", id, ClassUtil.getTypeDescription(type));
        msg = this._colonConcat(msg, extraDesc);
        return MismatchedInputException.from(this._parser, type, msg);
    }

    @Deprecated
    public JsonMappingException endOfInputException(Class<?> instClass) {
        return MismatchedInputException.from(this._parser, instClass, "Unexpected end-of-input when trying to deserialize a " + instClass.getName());
    }

    @Deprecated
    public void reportMappingException(String msg, Object ... msgArgs) throws JsonMappingException {
        throw JsonMappingException.from(this.getParser(), this._format(msg, msgArgs));
    }

    @Deprecated
    public JsonMappingException mappingException(String message) {
        return JsonMappingException.from(this.getParser(), message);
    }

    @Deprecated
    public JsonMappingException mappingException(String msg, Object ... msgArgs) {
        return JsonMappingException.from(this.getParser(), this._format(msg, msgArgs));
    }

    @Deprecated
    public JsonMappingException mappingException(Class<?> targetClass) {
        return this.mappingException(targetClass, this._parser.getCurrentToken());
    }

    @Deprecated
    public JsonMappingException mappingException(Class<?> targetClass, JsonToken token) {
        return JsonMappingException.from(this._parser, String.format("Cannot deserialize instance of %s out of %s token", new Object[]{ClassUtil.nameOf(targetClass), token}));
    }

    protected DateFormat getDateFormat() {
        if (this._dateFormat != null) {
            return this._dateFormat;
        }
        DateFormat df = this._config.getDateFormat();
        this._dateFormat = df = (DateFormat)df.clone();
        return df;
    }
}

