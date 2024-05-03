/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;

public class ThrowableDeserializer
extends BeanDeserializer {
    private static final long serialVersionUID = 1L;
    protected static final String PROP_NAME_MESSAGE = "message";

    public ThrowableDeserializer(BeanDeserializer baseDeserializer) {
        super(baseDeserializer);
        this._vanillaProcessing = false;
    }

    protected ThrowableDeserializer(BeanDeserializer src, NameTransformer unwrapper) {
        super((BeanDeserializerBase)src, unwrapper);
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
        if (this.getClass() != ThrowableDeserializer.class) {
            return this;
        }
        return new ThrowableDeserializer(this, unwrapper);
    }

    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (this._propertyBasedCreator != null) {
            return this._deserializeUsingPropertyBased(p, ctxt);
        }
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(ctxt, this._delegateDeserializer.deserialize(p, ctxt));
        }
        if (this._beanType.isAbstract()) {
            return ctxt.handleMissingInstantiator(this.handledType(), this.getValueInstantiator(), p, "abstract type (need to add/enable type information?)", new Object[0]);
        }
        boolean hasStringCreator = this._valueInstantiator.canCreateFromString();
        boolean hasDefaultCtor = this._valueInstantiator.canCreateUsingDefault();
        if (!hasStringCreator && !hasDefaultCtor) {
            return ctxt.handleMissingInstantiator(this.handledType(), this.getValueInstantiator(), p, "Throwable needs a default constructor, a single-String-arg constructor; or explicit @JsonCreator", new Object[0]);
        }
        Object throwable = null;
        Object[] pending = null;
        int pendingIx = 0;
        while (!p.hasToken(JsonToken.END_OBJECT)) {
            String propName = p.getCurrentName();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            p.nextToken();
            if (prop != null) {
                if (throwable != null) {
                    prop.deserializeAndSet(p, ctxt, throwable);
                } else {
                    if (pending == null) {
                        int len = this._beanProperties.size();
                        pending = new Object[len + len];
                    }
                    pending[pendingIx++] = prop;
                    pending[pendingIx++] = prop.deserialize(p, ctxt);
                }
            } else {
                boolean isMessage = PROP_NAME_MESSAGE.equals(propName);
                if (isMessage && hasStringCreator) {
                    throwable = this._valueInstantiator.createFromString(ctxt, p.getValueAsString());
                    if (pending != null) {
                        int len = pendingIx;
                        for (int i = 0; i < len; i += 2) {
                            prop = (SettableBeanProperty)pending[i];
                            prop.set(throwable, pending[i + 1]);
                        }
                        pending = null;
                    }
                } else if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                    p.skipChildren();
                } else if (this._anySetter != null) {
                    this._anySetter.deserializeAndSet(p, ctxt, throwable, propName);
                } else {
                    this.handleUnknownProperty(p, ctxt, throwable, propName);
                }
            }
            p.nextToken();
        }
        if (throwable == null) {
            throwable = hasStringCreator ? this._valueInstantiator.createFromString(ctxt, null) : this._valueInstantiator.createUsingDefault(ctxt);
            if (pending != null) {
                int len = pendingIx;
                for (int i = 0; i < len; i += 2) {
                    SettableBeanProperty prop = (SettableBeanProperty)pending[i];
                    prop.set(throwable, pending[i + 1]);
                }
            }
        }
        return throwable;
    }
}

