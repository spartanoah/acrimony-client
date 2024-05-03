/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Set;

public class BeanAsArrayBuilderDeserializer
extends BeanDeserializerBase {
    private static final long serialVersionUID = 1L;
    protected final BeanDeserializerBase _delegate;
    protected final SettableBeanProperty[] _orderedProperties;
    protected final AnnotatedMethod _buildMethod;
    protected final JavaType _targetType;

    public BeanAsArrayBuilderDeserializer(BeanDeserializerBase delegate, JavaType targetType, SettableBeanProperty[] ordered, AnnotatedMethod buildMethod) {
        super(delegate);
        this._delegate = delegate;
        this._targetType = targetType;
        this._orderedProperties = ordered;
        this._buildMethod = buildMethod;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
        return this._delegate.unwrappingDeserializer(unwrapper);
    }

    @Override
    public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
        return new BeanAsArrayBuilderDeserializer(this._delegate.withObjectIdReader(oir), this._targetType, this._orderedProperties, this._buildMethod);
    }

    @Override
    public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
        return new BeanAsArrayBuilderDeserializer(this._delegate.withIgnorableProperties(ignorableProps), this._targetType, this._orderedProperties, this._buildMethod);
    }

    @Override
    public BeanDeserializerBase withIgnoreAllUnknown(boolean ignoreUnknown) {
        return new BeanAsArrayBuilderDeserializer(this._delegate.withIgnoreAllUnknown(ignoreUnknown), this._targetType, this._orderedProperties, this._buildMethod);
    }

    @Override
    public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
        return new BeanAsArrayBuilderDeserializer(this._delegate.withBeanProperties(props), this._targetType, this._orderedProperties, this._buildMethod);
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        return this;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.FALSE;
    }

    protected final Object finishBuild(DeserializationContext ctxt, Object builder) throws IOException {
        try {
            return this._buildMethod.getMember().invoke(builder, null);
        } catch (Exception e) {
            return this.wrapInstantiationProblem(e, ctxt);
        }
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (!p.isExpectedStartArrayToken()) {
            return this.finishBuild(ctxt, this._deserializeFromNonArray(p, ctxt));
        }
        if (!this._vanillaProcessing) {
            return this.finishBuild(ctxt, this._deserializeNonVanilla(p, ctxt));
        }
        Object builder = this._valueInstantiator.createUsingDefault(ctxt);
        SettableBeanProperty[] props = this._orderedProperties;
        int i = 0;
        int propCount = props.length;
        while (true) {
            if (p.nextToken() == JsonToken.END_ARRAY) {
                return this.finishBuild(ctxt, builder);
            }
            if (i == propCount) break;
            SettableBeanProperty prop = props[i];
            if (prop != null) {
                try {
                    builder = prop.deserializeSetAndReturn(p, ctxt, builder);
                } catch (Exception e) {
                    this.wrapAndThrow(e, builder, prop.getName(), ctxt);
                }
            } else {
                p.skipChildren();
            }
            ++i;
        }
        if (!this._ignoreAllUnknown && ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            ctxt.reportInputMismatch(this.handledType(), "Unexpected JSON values; expected at most %d properties (in JSON Array)", propCount);
        }
        while (p.nextToken() != JsonToken.END_ARRAY) {
            p.skipChildren();
        }
        return this.finishBuild(ctxt, builder);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object value) throws IOException {
        return this._delegate.deserialize(p, ctxt, value);
    }

    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this._deserializeFromNonArray(p, ctxt);
    }

    protected Object _deserializeNonVanilla(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (this._nonStandardCreation) {
            return this.deserializeFromObjectUsingNonDefault(p, ctxt);
        }
        Object builder = this._valueInstantiator.createUsingDefault(ctxt);
        if (this._injectables != null) {
            this.injectValues(ctxt, builder);
        }
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        SettableBeanProperty[] props = this._orderedProperties;
        int i = 0;
        int propCount = props.length;
        while (true) {
            if (p.nextToken() == JsonToken.END_ARRAY) {
                return builder;
            }
            if (i == propCount) break;
            SettableBeanProperty prop = props[i];
            ++i;
            if (prop != null && (activeView == null || prop.visibleInView(activeView))) {
                try {
                    prop.deserializeSetAndReturn(p, ctxt, builder);
                } catch (Exception e) {
                    this.wrapAndThrow(e, builder, prop.getName(), ctxt);
                }
                continue;
            }
            p.skipChildren();
        }
        if (!this._ignoreAllUnknown && ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            ctxt.reportWrongTokenException(this, JsonToken.END_ARRAY, "Unexpected JSON value(s); expected at most %d properties (in JSON Array)", propCount);
        }
        while (p.nextToken() != JsonToken.END_ARRAY) {
            p.skipChildren();
        }
        return builder;
    }

    @Override
    protected final Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
        PropertyBasedCreator creator = this._propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
        SettableBeanProperty[] props = this._orderedProperties;
        int propCount = props.length;
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        int i = 0;
        Object builder = null;
        while (p.nextToken() != JsonToken.END_ARRAY) {
            block19: {
                SettableBeanProperty prop;
                SettableBeanProperty settableBeanProperty = prop = i < propCount ? props[i] : null;
                if (prop == null) {
                    p.skipChildren();
                } else if (activeView != null && !prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else if (builder != null) {
                    try {
                        builder = prop.deserializeSetAndReturn(p, ctxt, builder);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, builder, prop.getName(), ctxt);
                    }
                } else {
                    String propName = prop.getName();
                    SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
                    if (creatorProp != null) {
                        if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                            try {
                                builder = creator.build(ctxt, buffer);
                            } catch (Exception e) {
                                this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                                break block19;
                            }
                            if (builder.getClass() != this._beanType.getRawClass()) {
                                return ctxt.reportBadDefinition(this._beanType, String.format("Cannot support implicit polymorphic deserialization for POJOs-as-Arrays style: nominal type %s, actual type %s", this._beanType.getRawClass().getName(), builder.getClass().getName()));
                            }
                        }
                    } else if (!buffer.readIdProperty(propName)) {
                        buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
                    }
                }
            }
            ++i;
        }
        if (builder == null) {
            try {
                builder = creator.build(ctxt, buffer);
            } catch (Exception e) {
                return this.wrapInstantiationProblem(e, ctxt);
            }
        }
        return builder;
    }

    protected Object _deserializeFromNonArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        String message = "Cannot deserialize a POJO (of type %s) from non-Array representation (token: %s): type/property designed to be serialized as JSON Array";
        return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p.getCurrentToken(), p, message, new Object[]{this._beanType.getRawClass().getName(), p.getCurrentToken()});
    }
}

