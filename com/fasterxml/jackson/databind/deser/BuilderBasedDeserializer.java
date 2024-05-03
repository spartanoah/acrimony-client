/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayBuilderDeserializer;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class BuilderBasedDeserializer
extends BeanDeserializerBase {
    private static final long serialVersionUID = 1L;
    protected final AnnotatedMethod _buildMethod;
    protected final JavaType _targetType;

    public BuilderBasedDeserializer(BeanDeserializerBuilder builder, BeanDescription beanDesc, JavaType targetType, BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs, Set<String> ignorableProps, boolean ignoreAllUnknown, boolean hasViews) {
        super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
        this._targetType = targetType;
        this._buildMethod = builder.getBuildMethod();
        if (this._objectIdReader != null) {
            throw new IllegalArgumentException("Cannot use Object Id with Builder-based deserialization (type " + beanDesc.getType() + ")");
        }
    }

    @Deprecated
    public BuilderBasedDeserializer(BeanDeserializerBuilder builder, BeanDescription beanDesc, BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs, Set<String> ignorableProps, boolean ignoreAllUnknown, boolean hasViews) {
        this(builder, beanDesc, beanDesc.getType(), properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
    }

    protected BuilderBasedDeserializer(BuilderBasedDeserializer src) {
        this(src, src._ignoreAllUnknown);
    }

    protected BuilderBasedDeserializer(BuilderBasedDeserializer src, boolean ignoreAllUnknown) {
        super((BeanDeserializerBase)src, ignoreAllUnknown);
        this._buildMethod = src._buildMethod;
        this._targetType = src._targetType;
    }

    protected BuilderBasedDeserializer(BuilderBasedDeserializer src, NameTransformer unwrapper) {
        super((BeanDeserializerBase)src, unwrapper);
        this._buildMethod = src._buildMethod;
        this._targetType = src._targetType;
    }

    public BuilderBasedDeserializer(BuilderBasedDeserializer src, ObjectIdReader oir) {
        super((BeanDeserializerBase)src, oir);
        this._buildMethod = src._buildMethod;
        this._targetType = src._targetType;
    }

    public BuilderBasedDeserializer(BuilderBasedDeserializer src, Set<String> ignorableProps) {
        super((BeanDeserializerBase)src, ignorableProps);
        this._buildMethod = src._buildMethod;
        this._targetType = src._targetType;
    }

    public BuilderBasedDeserializer(BuilderBasedDeserializer src, BeanPropertyMap props) {
        super((BeanDeserializerBase)src, props);
        this._buildMethod = src._buildMethod;
        this._targetType = src._targetType;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
        return new BuilderBasedDeserializer(this, unwrapper);
    }

    @Override
    public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
        return new BuilderBasedDeserializer(this, oir);
    }

    @Override
    public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
        return new BuilderBasedDeserializer(this, ignorableProps);
    }

    @Override
    public BeanDeserializerBase withIgnoreAllUnknown(boolean ignoreUnknown) {
        return new BuilderBasedDeserializer(this, ignoreUnknown);
    }

    @Override
    public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
        return new BuilderBasedDeserializer(this, props);
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        SettableBeanProperty[] props = this._beanProperties.getPropertiesInInsertionOrder();
        return new BeanAsArrayBuilderDeserializer(this, this._targetType, props, this._buildMethod);
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.FALSE;
    }

    protected Object finishBuild(DeserializationContext ctxt, Object builder) throws IOException {
        if (null == this._buildMethod) {
            return builder;
        }
        try {
            return this._buildMethod.getMember().invoke(builder, null);
        } catch (Exception e) {
            return this.wrapInstantiationProblem(e, ctxt);
        }
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.isExpectedStartObjectToken()) {
            JsonToken t = p.nextToken();
            if (this._vanillaProcessing) {
                return this.finishBuild(ctxt, this.vanillaDeserialize(p, ctxt, t));
            }
            return this.finishBuild(ctxt, this.deserializeFromObject(p, ctxt));
        }
        switch (p.getCurrentTokenId()) {
            case 6: {
                return this.finishBuild(ctxt, this.deserializeFromString(p, ctxt));
            }
            case 7: {
                return this.finishBuild(ctxt, this.deserializeFromNumber(p, ctxt));
            }
            case 8: {
                return this.finishBuild(ctxt, this.deserializeFromDouble(p, ctxt));
            }
            case 12: {
                return p.getEmbeddedObject();
            }
            case 9: 
            case 10: {
                return this.finishBuild(ctxt, this.deserializeFromBoolean(p, ctxt));
            }
            case 3: {
                return this._deserializeFromArray(p, ctxt);
            }
            case 2: 
            case 5: {
                return this.finishBuild(ctxt, this.deserializeFromObject(p, ctxt));
            }
        }
        return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object value) throws IOException {
        Class<?> instRawType;
        JavaType valueType = this._targetType;
        Class<?> builderRawType = this.handledType();
        if (builderRawType.isAssignableFrom(instRawType = value.getClass())) {
            return ctxt.reportBadDefinition(valueType, String.format("Deserialization of %s by passing existing Builder (%s) instance not supported", valueType, builderRawType.getName()));
        }
        return ctxt.reportBadDefinition(valueType, String.format("Deserialization of %s by passing existing instance (of %s) not supported", valueType, instRawType.getName()));
    }

    private final Object vanillaDeserialize(JsonParser p, DeserializationContext ctxt, JsonToken t) throws IOException {
        Object bean = this._valueInstantiator.createUsingDefault(ctxt);
        while (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                try {
                    bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                } catch (Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            } else {
                this.handleUnknownVanilla(p, ctxt, bean, propName);
            }
            p.nextToken();
        }
        return bean;
    }

    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> view;
        if (this._nonStandardCreation) {
            if (this._unwrappedPropertyHandler != null) {
                return this.deserializeWithUnwrapped(p, ctxt);
            }
            if (this._externalTypeIdHandler != null) {
                return this.deserializeWithExternalTypeId(p, ctxt);
            }
            return this.deserializeFromObjectUsingNonDefault(p, ctxt);
        }
        Object bean = this._valueInstantiator.createUsingDefault(ctxt);
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        if (this._needViewProcesing && (view = ctxt.getActiveView()) != null) {
            return this.deserializeWithView(p, ctxt, bean, view);
        }
        while (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                try {
                    bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                } catch (Exception e) {
                    this.wrapAndThrow(e, bean, propName, ctxt);
                }
            } else {
                this.handleUnknownVanilla(p, ctxt, bean, propName);
            }
            p.nextToken();
        }
        return bean;
    }

    @Override
    protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object builder;
        PropertyBasedCreator creator = this._propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        TokenBuffer unknown = null;
        JsonToken t = p.getCurrentToken();
        while (t == JsonToken.FIELD_NAME) {
            block22: {
                String propName = p.getCurrentName();
                p.nextToken();
                SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
                if (creatorProp != null) {
                    if (activeView != null && !creatorProp.visibleInView(activeView)) {
                        p.skipChildren();
                    } else if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                        Object builder2;
                        p.nextToken();
                        try {
                            builder2 = creator.build(ctxt, buffer);
                        } catch (Exception e) {
                            this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                            break block22;
                        }
                        if (builder2.getClass() != this._beanType.getRawClass()) {
                            return this.handlePolymorphic(p, ctxt, builder2, unknown);
                        }
                        if (unknown != null) {
                            builder2 = this.handleUnknownProperties(ctxt, builder2, unknown);
                        }
                        return this._deserialize(p, ctxt, builder2);
                    }
                } else if (!buffer.readIdProperty(propName)) {
                    SettableBeanProperty prop = this._beanProperties.find(propName);
                    if (prop != null) {
                        buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
                    } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                        this.handleIgnoredProperty(p, ctxt, this.handledType(), propName);
                    } else if (this._anySetter != null) {
                        buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(p, ctxt));
                    } else {
                        if (unknown == null) {
                            unknown = new TokenBuffer(p, ctxt);
                        }
                        unknown.writeFieldName(propName);
                        unknown.copyCurrentStructure(p);
                    }
                }
            }
            t = p.nextToken();
        }
        try {
            builder = creator.build(ctxt, buffer);
        } catch (Exception e) {
            builder = this.wrapInstantiationProblem(e, ctxt);
        }
        if (unknown != null) {
            if (builder.getClass() != this._beanType.getRawClass()) {
                return this.handlePolymorphic(null, ctxt, builder, unknown);
            }
            return this.handleUnknownProperties(ctxt, builder, unknown);
        }
        return builder;
    }

    protected final Object _deserialize(JsonParser p, DeserializationContext ctxt, Object builder) throws IOException {
        Class<?> view;
        if (this._injectables != null) {
            this.injectValues(ctxt, builder);
        }
        if (this._unwrappedPropertyHandler != null) {
            if (p.hasToken(JsonToken.START_OBJECT)) {
                p.nextToken();
            }
            TokenBuffer tokens = new TokenBuffer(p, ctxt);
            tokens.writeStartObject();
            return this.deserializeWithUnwrapped(p, ctxt, builder, tokens);
        }
        if (this._externalTypeIdHandler != null) {
            return this.deserializeWithExternalTypeId(p, ctxt, builder);
        }
        if (this._needViewProcesing && (view = ctxt.getActiveView()) != null) {
            return this.deserializeWithView(p, ctxt, builder, view);
        }
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = p.nextToken();
        }
        while (t == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                try {
                    builder = prop.deserializeSetAndReturn(p, ctxt, builder);
                } catch (Exception e) {
                    this.wrapAndThrow(e, builder, propName, ctxt);
                }
            } else {
                this.handleUnknownVanilla(p, ctxt, builder, propName);
            }
            t = p.nextToken();
        }
        return builder;
    }

    @Override
    protected Object _deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonDeserializer delegateDeser = this._arrayDelegateDeserializer;
        if (delegateDeser != null || (delegateDeser = this._delegateDeserializer) != null) {
            Object builder = this._valueInstantiator.createUsingArrayDelegate(ctxt, delegateDeser.deserialize(p, ctxt));
            if (this._injectables != null) {
                this.injectValues(ctxt, builder);
            }
            return this.finishBuild(ctxt, builder);
        }
        if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            JsonToken t = p.nextToken();
            if (t == JsonToken.END_ARRAY && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                return null;
            }
            Object value = this.deserialize(p, ctxt);
            if (p.nextToken() != JsonToken.END_ARRAY) {
                this.handleMissingEndArrayForSingle(p, ctxt);
            }
            return value;
        }
        if (ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
            JsonToken t = p.nextToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            return ctxt.handleUnexpectedToken(this.getValueType(ctxt), JsonToken.START_ARRAY, p, null, new Object[0]);
        }
        return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p);
    }

    protected final Object deserializeWithView(JsonParser p, DeserializationContext ctxt, Object bean, Class<?> activeView) throws IOException {
        JsonToken t = p.getCurrentToken();
        while (t == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                if (!prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else {
                    try {
                        bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
            } else {
                this.handleUnknownVanilla(p, ctxt, bean, propName);
            }
            t = p.nextToken();
        }
        return bean;
    }

    protected Object deserializeWithUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> activeView;
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(ctxt, this._delegateDeserializer.deserialize(p, ctxt));
        }
        if (this._propertyBasedCreator != null) {
            return this.deserializeUsingPropertyBasedWithUnwrapped(p, ctxt);
        }
        TokenBuffer tokens = new TokenBuffer(p, ctxt);
        tokens.writeStartObject();
        Object bean = this._valueInstantiator.createUsingDefault(ctxt);
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        Class<?> clazz = activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        while (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                if (activeView != null && !prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else {
                    try {
                        bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
            } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                this.handleIgnoredProperty(p, ctxt, bean, propName);
            } else {
                tokens.writeFieldName(propName);
                tokens.copyCurrentStructure(p);
                if (this._anySetter != null) {
                    try {
                        this._anySetter.deserializeAndSet(p, ctxt, bean, propName);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
            }
            p.nextToken();
        }
        tokens.writeEndObject();
        return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, bean, tokens);
    }

    protected Object deserializeUsingPropertyBasedWithUnwrapped(JsonParser p, DeserializationContext ctxt) throws IOException {
        PropertyBasedCreator creator = this._propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
        TokenBuffer tokens = new TokenBuffer(p, ctxt);
        tokens.writeStartObject();
        Object builder = null;
        JsonToken t = p.getCurrentToken();
        while (t == JsonToken.FIELD_NAME) {
            block16: {
                String propName = p.getCurrentName();
                p.nextToken();
                SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
                if (creatorProp != null) {
                    if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                        t = p.nextToken();
                        try {
                            builder = creator.build(ctxt, buffer);
                        } catch (Exception e) {
                            this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                            break block16;
                        }
                        if (builder.getClass() != this._beanType.getRawClass()) {
                            return this.handlePolymorphic(p, ctxt, builder, tokens);
                        }
                        return this.deserializeWithUnwrapped(p, ctxt, builder, tokens);
                    }
                } else if (!buffer.readIdProperty(propName)) {
                    SettableBeanProperty prop = this._beanProperties.find(propName);
                    if (prop != null) {
                        buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
                    } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                        this.handleIgnoredProperty(p, ctxt, this.handledType(), propName);
                    } else {
                        tokens.writeFieldName(propName);
                        tokens.copyCurrentStructure(p);
                        if (this._anySetter != null) {
                            buffer.bufferAnyProperty(this._anySetter, propName, this._anySetter.deserialize(p, ctxt));
                        }
                    }
                }
            }
            t = p.nextToken();
        }
        tokens.writeEndObject();
        if (builder == null) {
            try {
                builder = creator.build(ctxt, buffer);
            } catch (Exception e) {
                return this.wrapInstantiationProblem(e, ctxt);
            }
        }
        return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, builder, tokens);
    }

    protected Object deserializeWithUnwrapped(JsonParser p, DeserializationContext ctxt, Object builder, TokenBuffer tokens) throws IOException {
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        JsonToken t = p.getCurrentToken();
        while (t == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            p.nextToken();
            if (prop != null) {
                if (activeView != null && !prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else {
                    try {
                        builder = prop.deserializeSetAndReturn(p, ctxt, builder);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, builder, propName, ctxt);
                    }
                }
            } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                this.handleIgnoredProperty(p, ctxt, builder, propName);
            } else {
                tokens.writeFieldName(propName);
                tokens.copyCurrentStructure(p);
                if (this._anySetter != null) {
                    this._anySetter.deserializeAndSet(p, ctxt, builder, propName);
                }
            }
            t = p.nextToken();
        }
        tokens.writeEndObject();
        return this._unwrappedPropertyHandler.processUnwrapped(p, ctxt, builder, tokens);
    }

    protected Object deserializeWithExternalTypeId(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (this._propertyBasedCreator != null) {
            return this.deserializeUsingPropertyBasedWithExternalTypeId(p, ctxt);
        }
        return this.deserializeWithExternalTypeId(p, ctxt, this._valueInstantiator.createUsingDefault(ctxt));
    }

    protected Object deserializeWithExternalTypeId(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        ExternalTypeHandler ext = this._externalTypeIdHandler.start();
        JsonToken t = p.getCurrentToken();
        while (t == JsonToken.FIELD_NAME) {
            String propName = p.getCurrentName();
            t = p.nextToken();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            if (prop != null) {
                if (t.isScalarValue()) {
                    ext.handleTypePropertyValue(p, ctxt, propName, bean);
                }
                if (activeView != null && !prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else {
                    try {
                        bean = prop.deserializeSetAndReturn(p, ctxt, bean);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                }
            } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                this.handleIgnoredProperty(p, ctxt, bean, propName);
            } else if (!ext.handlePropertyValue(p, ctxt, propName, bean)) {
                if (this._anySetter != null) {
                    try {
                        this._anySetter.deserializeAndSet(p, ctxt, bean, propName);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, propName, ctxt);
                    }
                } else {
                    this.handleUnknownProperty(p, ctxt, bean, propName);
                }
            }
            t = p.nextToken();
        }
        return ext.complete(p, ctxt, bean);
    }

    protected Object deserializeUsingPropertyBasedWithExternalTypeId(JsonParser p, DeserializationContext ctxt) throws IOException {
        JavaType t = this._targetType;
        return ctxt.reportBadDefinition(t, String.format("Deserialization (of %s) with Builder, External type id, @JsonCreator not yet implemented", t));
    }
}

