/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import java.io.IOException;
import java.util.EnumSet;

public class EnumSetDeserializer
extends StdDeserializer<EnumSet<?>>
implements ContextualDeserializer {
    private static final long serialVersionUID = 1L;
    protected final JavaType _enumType;
    protected JsonDeserializer<Enum<?>> _enumDeserializer;
    protected final NullValueProvider _nullProvider;
    protected final boolean _skipNullValues;
    protected final Boolean _unwrapSingle;

    public EnumSetDeserializer(JavaType enumType, JsonDeserializer<?> deser) {
        super(EnumSet.class);
        this._enumType = enumType;
        if (!enumType.isEnumType()) {
            throw new IllegalArgumentException("Type " + enumType + " not Java Enum type");
        }
        this._enumDeserializer = deser;
        this._unwrapSingle = null;
        this._nullProvider = null;
        this._skipNullValues = false;
    }

    @Deprecated
    protected EnumSetDeserializer(EnumSetDeserializer base, JsonDeserializer<?> deser, Boolean unwrapSingle) {
        this(base, deser, base._nullProvider, unwrapSingle);
    }

    protected EnumSetDeserializer(EnumSetDeserializer base, JsonDeserializer<?> deser, NullValueProvider nuller, Boolean unwrapSingle) {
        super(base);
        this._enumType = base._enumType;
        this._enumDeserializer = deser;
        this._nullProvider = nuller;
        this._skipNullValues = NullsConstantProvider.isSkipper(nuller);
        this._unwrapSingle = unwrapSingle;
    }

    public EnumSetDeserializer withDeserializer(JsonDeserializer<?> deser) {
        if (this._enumDeserializer == deser) {
            return this;
        }
        return new EnumSetDeserializer(this, deser, this._nullProvider, this._unwrapSingle);
    }

    @Deprecated
    public EnumSetDeserializer withResolved(JsonDeserializer<?> deser, Boolean unwrapSingle) {
        return this.withResolved(deser, this._nullProvider, unwrapSingle);
    }

    public EnumSetDeserializer withResolved(JsonDeserializer<?> deser, NullValueProvider nuller, Boolean unwrapSingle) {
        if (this._unwrapSingle == unwrapSingle && this._enumDeserializer == deser && this._nullProvider == deser) {
            return this;
        }
        return new EnumSetDeserializer(this, deser, nuller, unwrapSingle);
    }

    @Override
    public boolean isCachable() {
        return this._enumType.getValueHandler() == null;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.TRUE;
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return this.constructSet();
    }

    @Override
    public AccessPattern getEmptyAccessPattern() {
        return AccessPattern.DYNAMIC;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        Boolean unwrapSingle = this.findFormatFeature(ctxt, property, EnumSet.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JsonDeserializer<Object> deser = this._enumDeserializer;
        deser = deser == null ? ctxt.findContextualValueDeserializer(this._enumType, property) : ctxt.handleSecondaryContextualization(deser, property, this._enumType);
        return this.withResolved(deser, this.findContentNullProvider(ctxt, property, deser), unwrapSingle);
    }

    @Override
    public EnumSet<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        EnumSet result = this.constructSet();
        if (!p.isExpectedStartArrayToken()) {
            return this.handleNonArray(p, ctxt, result);
        }
        return this._deserialize(p, ctxt, result);
    }

    @Override
    public EnumSet<?> deserialize(JsonParser p, DeserializationContext ctxt, EnumSet<?> result) throws IOException {
        if (!p.isExpectedStartArrayToken()) {
            return this.handleNonArray(p, ctxt, result);
        }
        return this._deserialize(p, ctxt, result);
    }

    protected final EnumSet<?> _deserialize(JsonParser p, DeserializationContext ctxt, EnumSet result) throws IOException {
        try {
            JsonToken t;
            while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
                Enum value;
                if (t == JsonToken.VALUE_NULL) {
                    if (this._skipNullValues) continue;
                    value = (Enum)this._nullProvider.getNullValue(ctxt);
                } else {
                    value = this._enumDeserializer.deserialize(p, ctxt);
                }
                if (value == null) continue;
                result.add(value);
            }
        } catch (Exception e) {
            throw JsonMappingException.wrapWithPath((Throwable)e, (Object)result, result.size());
        }
        return result;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromArray(p, ctxt);
    }

    private EnumSet constructSet() {
        return EnumSet.noneOf(this._enumType.getRawClass());
    }

    protected EnumSet<?> handleNonArray(JsonParser p, DeserializationContext ctxt, EnumSet result) throws IOException {
        boolean canWrap;
        boolean bl = canWrap = this._unwrapSingle == Boolean.TRUE || this._unwrapSingle == null && ctxt.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        if (!canWrap) {
            return (EnumSet)ctxt.handleUnexpectedToken(EnumSet.class, p);
        }
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return (EnumSet)ctxt.handleUnexpectedToken(this._enumType, p);
        }
        try {
            Enum<?> value = this._enumDeserializer.deserialize(p, ctxt);
            if (value != null) {
                result.add(value);
            }
        } catch (Exception e) {
            throw JsonMappingException.wrapWithPath((Throwable)e, (Object)result, result.size());
        }
        return result;
    }
}

