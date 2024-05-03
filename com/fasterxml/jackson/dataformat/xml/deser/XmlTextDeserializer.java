/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;

public class XmlTextDeserializer
extends DelegatingDeserializer {
    private static final long serialVersionUID = 1L;
    protected final int _xmlTextPropertyIndex;
    protected final SettableBeanProperty _xmlTextProperty;
    protected final ValueInstantiator _valueInstantiator;

    public XmlTextDeserializer(BeanDeserializerBase delegate, SettableBeanProperty prop) {
        super(delegate);
        this._xmlTextProperty = prop;
        this._xmlTextPropertyIndex = prop.getPropertyIndex();
        this._valueInstantiator = delegate.getValueInstantiator();
    }

    public XmlTextDeserializer(BeanDeserializerBase delegate, int textPropIndex) {
        super(delegate);
        this._xmlTextPropertyIndex = textPropIndex;
        this._valueInstantiator = delegate.getValueInstantiator();
        this._xmlTextProperty = delegate.findProperty(textPropIndex);
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee0) {
        throw new IllegalStateException("Internal error: should never get called");
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new XmlTextDeserializer(this._verifyDeserType(this._delegatee), this._xmlTextPropertyIndex);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            Object bean = this._valueInstantiator.createUsingDefault(ctxt);
            this._xmlTextProperty.deserializeAndSet(p, ctxt, bean);
            return bean;
        }
        return this._delegatee.deserialize(p, ctxt);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            this._xmlTextProperty.deserializeAndSet(p, ctxt, bean);
            return bean;
        }
        return this._delegatee.deserialize(p, ctxt, bean);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return this._delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }

    protected BeanDeserializerBase _verifyDeserType(JsonDeserializer<?> deser) {
        if (!(deser instanceof BeanDeserializerBase)) {
            throw new IllegalArgumentException("Can not change delegate to be of type " + deser.getClass().getName());
        }
        return (BeanDeserializerBase)deser;
    }
}

