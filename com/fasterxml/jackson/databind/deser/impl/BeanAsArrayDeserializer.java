/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Set;

public class BeanAsArrayDeserializer
extends BeanDeserializerBase {
    private static final long serialVersionUID = 1L;
    protected final BeanDeserializerBase _delegate;
    protected final SettableBeanProperty[] _orderedProperties;

    public BeanAsArrayDeserializer(BeanDeserializerBase delegate, SettableBeanProperty[] ordered) {
        super(delegate);
        this._delegate = delegate;
        this._orderedProperties = ordered;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
        return this._delegate.unwrappingDeserializer(unwrapper);
    }

    @Override
    public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
        return new BeanAsArrayDeserializer(this._delegate.withObjectIdReader(oir), this._orderedProperties);
    }

    @Override
    public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
        return new BeanAsArrayDeserializer(this._delegate.withIgnorableProperties(ignorableProps), this._orderedProperties);
    }

    @Override
    public BeanDeserializerBase withIgnoreAllUnknown(boolean ignoreUnknown) {
        return new BeanAsArrayDeserializer(this._delegate.withIgnoreAllUnknown(ignoreUnknown), this._orderedProperties);
    }

    @Override
    public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
        return new BeanAsArrayDeserializer(this._delegate.withBeanProperties(props), this._orderedProperties);
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        return this;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (!p.isExpectedStartArrayToken()) {
            return this._deserializeFromNonArray(p, ctxt);
        }
        if (!this._vanillaProcessing) {
            return this._deserializeNonVanilla(p, ctxt);
        }
        Object bean = this._valueInstantiator.createUsingDefault(ctxt);
        p.setCurrentValue(bean);
        SettableBeanProperty[] props = this._orderedProperties;
        int i = 0;
        int propCount = props.length;
        while (true) {
            if (p.nextToken() == JsonToken.END_ARRAY) {
                return bean;
            }
            if (i == propCount) break;
            SettableBeanProperty prop = props[i];
            if (prop != null) {
                try {
                    prop.deserializeAndSet(p, ctxt, bean);
                } catch (Exception e) {
                    this.wrapAndThrow(e, bean, prop.getName(), ctxt);
                }
            } else {
                p.skipChildren();
            }
            ++i;
        }
        if (!this._ignoreAllUnknown && ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            ctxt.reportWrongTokenException(this, JsonToken.END_ARRAY, "Unexpected JSON values; expected at most %d properties (in JSON Array)", propCount);
        }
        do {
            p.skipChildren();
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return bean;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
        p.setCurrentValue(bean);
        if (!p.isExpectedStartArrayToken()) {
            return this._deserializeFromNonArray(p, ctxt);
        }
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        SettableBeanProperty[] props = this._orderedProperties;
        int i = 0;
        int propCount = props.length;
        while (true) {
            if (p.nextToken() == JsonToken.END_ARRAY) {
                return bean;
            }
            if (i == propCount) break;
            SettableBeanProperty prop = props[i];
            if (prop != null) {
                try {
                    prop.deserializeAndSet(p, ctxt, bean);
                } catch (Exception e) {
                    this.wrapAndThrow(e, bean, prop.getName(), ctxt);
                }
            } else {
                p.skipChildren();
            }
            ++i;
        }
        if (!this._ignoreAllUnknown && ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            ctxt.reportWrongTokenException(this, JsonToken.END_ARRAY, "Unexpected JSON values; expected at most %d properties (in JSON Array)", propCount);
        }
        do {
            p.skipChildren();
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return bean;
    }

    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this._deserializeFromNonArray(p, ctxt);
    }

    protected Object _deserializeNonVanilla(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (this._nonStandardCreation) {
            return this.deserializeFromObjectUsingNonDefault(p, ctxt);
        }
        Object bean = this._valueInstantiator.createUsingDefault(ctxt);
        p.setCurrentValue(bean);
        if (this._injectables != null) {
            this.injectValues(ctxt, bean);
        }
        Class<?> activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        SettableBeanProperty[] props = this._orderedProperties;
        int i = 0;
        int propCount = props.length;
        while (true) {
            if (p.nextToken() == JsonToken.END_ARRAY) {
                return bean;
            }
            if (i == propCount) break;
            SettableBeanProperty prop = props[i];
            ++i;
            if (prop != null && (activeView == null || prop.visibleInView(activeView))) {
                try {
                    prop.deserializeAndSet(p, ctxt, bean);
                } catch (Exception e) {
                    this.wrapAndThrow(e, bean, prop.getName(), ctxt);
                }
                continue;
            }
            p.skipChildren();
        }
        if (!this._ignoreAllUnknown) {
            ctxt.reportWrongTokenException(this, JsonToken.END_ARRAY, "Unexpected JSON values; expected at most %d properties (in JSON Array)", propCount);
        }
        do {
            p.skipChildren();
        } while (p.nextToken() != JsonToken.END_ARRAY);
        return bean;
    }

    @Override
    protected final Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> activeView;
        PropertyBasedCreator creator = this._propertyBasedCreator;
        PropertyValueBuffer buffer = creator.startBuilding(p, ctxt, this._objectIdReader);
        SettableBeanProperty[] props = this._orderedProperties;
        int propCount = props.length;
        int i = 0;
        Object bean = null;
        Class<?> clazz = activeView = this._needViewProcesing ? ctxt.getActiveView() : null;
        while (p.nextToken() != JsonToken.END_ARRAY) {
            block19: {
                SettableBeanProperty prop;
                SettableBeanProperty settableBeanProperty = prop = i < propCount ? props[i] : null;
                if (prop == null) {
                    p.skipChildren();
                } else if (activeView != null && !prop.visibleInView(activeView)) {
                    p.skipChildren();
                } else if (bean != null) {
                    try {
                        prop.deserializeAndSet(p, ctxt, bean);
                    } catch (Exception e) {
                        this.wrapAndThrow(e, bean, prop.getName(), ctxt);
                    }
                } else {
                    String propName = prop.getName();
                    SettableBeanProperty creatorProp = creator.findCreatorProperty(propName);
                    if (creatorProp != null) {
                        if (buffer.assignParameter(creatorProp, creatorProp.deserialize(p, ctxt))) {
                            try {
                                bean = creator.build(ctxt, buffer);
                            } catch (Exception e) {
                                this.wrapAndThrow(e, this._beanType.getRawClass(), propName, ctxt);
                                break block19;
                            }
                            p.setCurrentValue(bean);
                            if (bean.getClass() != this._beanType.getRawClass()) {
                                ctxt.reportBadDefinition(this._beanType, String.format("Cannot support implicit polymorphic deserialization for POJOs-as-Arrays style: nominal type %s, actual type %s", this._beanType.getRawClass().getName(), bean.getClass().getName()));
                            }
                        }
                    } else if (!buffer.readIdProperty(propName)) {
                        buffer.bufferProperty(prop, prop.deserialize(p, ctxt));
                    }
                }
            }
            ++i;
        }
        if (bean == null) {
            try {
                bean = creator.build(ctxt, buffer);
            } catch (Exception e) {
                return this.wrapInstantiationProblem(e, ctxt);
            }
        }
        return bean;
    }

    protected Object _deserializeFromNonArray(JsonParser p, DeserializationContext ctxt) throws IOException {
        String message = "Cannot deserialize a POJO (of type %s) from non-Array representation (token: %s): type/property designed to be serialized as JSON Array";
        return ctxt.handleUnexpectedToken(this.getValueType(ctxt), p.getCurrentToken(), p, message, new Object[]{this._beanType.getRawClass().getName(), p.getCurrentToken()});
    }
}

