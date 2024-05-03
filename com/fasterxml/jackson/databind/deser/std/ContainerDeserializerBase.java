/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class ContainerDeserializerBase<T>
extends StdDeserializer<T>
implements ValueInstantiator.Gettable {
    protected final JavaType _containerType;
    protected final NullValueProvider _nullProvider;
    protected final boolean _skipNullValues;
    protected final Boolean _unwrapSingle;

    protected ContainerDeserializerBase(JavaType selfType, NullValueProvider nuller, Boolean unwrapSingle) {
        super(selfType);
        this._containerType = selfType;
        this._unwrapSingle = unwrapSingle;
        this._nullProvider = nuller;
        this._skipNullValues = NullsConstantProvider.isSkipper(nuller);
    }

    protected ContainerDeserializerBase(JavaType selfType) {
        this(selfType, null, null);
    }

    protected ContainerDeserializerBase(ContainerDeserializerBase<?> base) {
        this(base, base._nullProvider, base._unwrapSingle);
    }

    protected ContainerDeserializerBase(ContainerDeserializerBase<?> base, NullValueProvider nuller, Boolean unwrapSingle) {
        super(base._containerType);
        this._containerType = base._containerType;
        this._nullProvider = nuller;
        this._unwrapSingle = unwrapSingle;
        this._skipNullValues = NullsConstantProvider.isSkipper(nuller);
    }

    @Override
    public JavaType getValueType() {
        return this._containerType;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.TRUE;
    }

    @Override
    public SettableBeanProperty findBackReference(String refName) {
        JsonDeserializer<Object> valueDeser = this.getContentDeserializer();
        if (valueDeser == null) {
            throw new IllegalArgumentException(String.format("Cannot handle managed/back reference '%s': type: container deserializer of type %s returned null for 'getContentDeserializer()'", refName, this.getClass().getName()));
        }
        return valueDeser.findBackReference(refName);
    }

    public JavaType getContentType() {
        if (this._containerType == null) {
            return TypeFactory.unknownType();
        }
        return this._containerType.getContentType();
    }

    public abstract JsonDeserializer<Object> getContentDeserializer();

    @Override
    public ValueInstantiator getValueInstantiator() {
        return null;
    }

    @Override
    public AccessPattern getEmptyAccessPattern() {
        return AccessPattern.DYNAMIC;
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        ValueInstantiator vi = this.getValueInstantiator();
        if (vi == null || !vi.canCreateUsingDefault()) {
            JavaType type = this.getValueType();
            ctxt.reportBadDefinition(type, String.format("Cannot create empty instance of %s, no default Creator", type));
        }
        try {
            return vi.createUsingDefault(ctxt);
        } catch (IOException e) {
            return ClassUtil.throwAsMappingException(ctxt, e);
        }
    }

    protected <BOGUS> BOGUS wrapAndThrow(Throwable t, Object ref, String key) throws IOException {
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        ClassUtil.throwIfError(t);
        if (t instanceof IOException && !(t instanceof JsonMappingException)) {
            throw (IOException)t;
        }
        throw JsonMappingException.wrapWithPath(t, ref, ClassUtil.nonNull(key, "N/A"));
    }
}

