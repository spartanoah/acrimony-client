/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.util.TypeUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WrapperHandlingDeserializer
extends DelegatingDeserializer {
    private static final long serialVersionUID = 1L;
    protected final Set<String> _namesToWrap;
    protected final JavaType _type;
    protected final boolean _caseInsensitive;

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate) {
        this(delegate, null);
    }

    public WrapperHandlingDeserializer(BeanDeserializerBase delegate, Set<String> namesToWrap) {
        super(delegate);
        this._namesToWrap = namesToWrap;
        this._type = delegate.getValueType();
        this._caseInsensitive = delegate.isCaseInsensitive();
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee0) {
        throw new IllegalStateException("Internal error: should never get called");
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType vt = this._type;
        if (vt == null) {
            vt = ctxt.constructType(this._delegatee.handledType());
        }
        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(this._delegatee, property, vt);
        BeanDeserializerBase newDelegatee = this._verifyDeserType(del);
        Iterator<SettableBeanProperty> it = newDelegatee.properties();
        HashSet<String> unwrappedNames = null;
        while (it.hasNext()) {
            PropertyName wrapperName;
            SettableBeanProperty prop = it.next();
            JavaType type = prop.getType();
            if (!TypeUtil.isIndexedType(type) || (wrapperName = prop.getWrapperName()) != null && wrapperName != PropertyName.NO_NAME) continue;
            if (unwrappedNames == null) {
                unwrappedNames = new HashSet<String>();
            }
            unwrappedNames.add(prop.getName());
            for (PropertyName alias : prop.findAliases(ctxt.getConfig())) {
                unwrappedNames.add(alias.getSimpleName());
            }
        }
        if (unwrappedNames == null) {
            return newDelegatee;
        }
        return new WrapperHandlingDeserializer(newDelegatee, unwrappedNames);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        this._configureParser(p);
        return this._delegatee.deserialize(p, ctxt);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        this._configureParser(p);
        return this._delegatee.deserialize(p, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        this._configureParser(p);
        return this._delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }

    protected final void _configureParser(JsonParser p) throws IOException {
        JsonToken t;
        while (p instanceof JsonParserDelegate) {
            p = ((JsonParserDelegate)p).delegate();
        }
        if (p instanceof FromXmlParser && ((t = p.currentToken()) == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY || t == JsonToken.FIELD_NAME)) {
            ((FromXmlParser)p).addVirtualWrapping(this._namesToWrap, this._caseInsensitive);
        }
    }

    protected BeanDeserializerBase _verifyDeserType(JsonDeserializer<?> deser) {
        if (!(deser instanceof BeanDeserializerBase)) {
            throw new IllegalArgumentException("Can not change delegate to be of type " + deser.getClass().getName());
        }
        return (BeanDeserializerBase)deser;
    }
}

