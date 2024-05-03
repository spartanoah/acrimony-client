/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;

class FactoryBasedEnumDeserializer
extends StdDeserializer<Object>
implements ContextualDeserializer {
    private static final long serialVersionUID = 1L;
    protected final JavaType _inputType;
    protected final boolean _hasArgs;
    protected final AnnotatedMethod _factory;
    protected final JsonDeserializer<?> _deser;
    protected final ValueInstantiator _valueInstantiator;
    protected final SettableBeanProperty[] _creatorProps;
    private transient PropertyBasedCreator _propCreator;

    public FactoryBasedEnumDeserializer(Class<?> cls, AnnotatedMethod f, JavaType paramType, ValueInstantiator valueInstantiator, SettableBeanProperty[] creatorProps) {
        super(cls);
        this._factory = f;
        this._hasArgs = true;
        this._inputType = paramType.hasRawClass(String.class) ? null : paramType;
        this._deser = null;
        this._valueInstantiator = valueInstantiator;
        this._creatorProps = creatorProps;
    }

    public FactoryBasedEnumDeserializer(Class<?> cls, AnnotatedMethod f) {
        super(cls);
        this._factory = f;
        this._hasArgs = false;
        this._inputType = null;
        this._deser = null;
        this._valueInstantiator = null;
        this._creatorProps = null;
    }

    protected FactoryBasedEnumDeserializer(FactoryBasedEnumDeserializer base, JsonDeserializer<?> deser) {
        super(base._valueClass);
        this._inputType = base._inputType;
        this._factory = base._factory;
        this._hasArgs = base._hasArgs;
        this._valueInstantiator = base._valueInstantiator;
        this._creatorProps = base._creatorProps;
        this._deser = deser;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        if (this._deser == null && this._inputType != null && this._creatorProps == null) {
            return new FactoryBasedEnumDeserializer(this, ctxt.findContextualValueDeserializer(this._inputType, property));
        }
        return this;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.FALSE;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object value;
        if (this._deser != null) {
            value = this._deser.deserialize(p, ctxt);
        } else if (this._hasArgs) {
            JsonToken curr = p.currentToken();
            if (this._creatorProps != null) {
                if (!p.isExpectedStartObjectToken()) {
                    JavaType targetType = this.getValueType(ctxt);
                    ctxt.reportInputMismatch(targetType, "Input mismatch reading Enum %s: properties-based `@JsonCreator` (%s) expects JSON Object (JsonToken.START_OBJECT), got JsonToken.%s", new Object[]{ClassUtil.getTypeDescription(targetType), this._factory, p.currentToken()});
                }
                if (this._propCreator == null) {
                    this._propCreator = PropertyBasedCreator.construct(ctxt, this._valueInstantiator, this._creatorProps, ctxt.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
                }
                p.nextToken();
                return this.deserializeEnumUsingPropertyBased(p, ctxt, this._propCreator);
            }
            value = curr == JsonToken.VALUE_STRING || curr == JsonToken.FIELD_NAME ? p.getText() : (curr == JsonToken.VALUE_NUMBER_INT ? p.getNumberValue() : p.getValueAsString());
        } else {
            p.skipChildren();
            try {
                return this._factory.call();
            } catch (Exception e) {
                Throwable t = ClassUtil.throwRootCauseIfIOE(e);
                return ctxt.handleInstantiationProblem(this._valueClass, null, t);
            }
        }
        try {
            return this._factory.callOnWith(this._valueClass, value);
        } catch (Exception e) {
            Throwable t = ClassUtil.throwRootCauseIfIOE(e);
            if (ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL) && t instanceof IllegalArgumentException) {
                return null;
            }
            return ctxt.handleInstantiationProblem(this._valueClass, value, t);
        }
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        if (this._deser == null) {
            return this.deserialize(p, ctxt);
        }
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }

    protected Object deserializeEnumUsingPropertyBased(JsonParser p, DeserializationContext ctxt, PropertyBasedCreator creator) throws IOException {
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, null);
        JsonToken t = p.currentToken();
        while (t == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
            if (creatorProp != null) {
                buffer.assignParameter(creatorProp, this._deserializeWithErrorWrapping(p, ctxt, creatorProp));
            } else if (buffer.readIdProperty(propName)) {
                // empty if block
            }
            t = p.nextToken();
        }
        return creator.build(ctxt, buffer);
    }

    protected final Object _deserializeWithErrorWrapping(JsonParser p, DeserializationContext ctxt, SettableBeanProperty prop) throws IOException {
        try {
            return prop.deserialize(p, ctxt);
        } catch (Exception e) {
            return this.wrapAndThrow(e, this.handledType(), prop.getName(), ctxt);
        }
    }

    protected Object wrapAndThrow(Throwable t, Object bean, String fieldName, DeserializationContext ctxt) throws IOException {
        throw JsonMappingException.wrapWithPath(this.throwOrReturnThrowable(t, ctxt), bean, fieldName);
    }

    private Throwable throwOrReturnThrowable(Throwable t, DeserializationContext ctxt) throws IOException {
        boolean wrap;
        t = ClassUtil.getRootCause(t);
        ClassUtil.throwIfError(t);
        boolean bl = wrap = ctxt == null || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonProcessingException)) {
                throw (IOException)t;
            }
        } else if (!wrap) {
            ClassUtil.throwIfRTE(t);
        }
        return t;
    }
}

