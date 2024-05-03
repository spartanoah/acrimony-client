/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;

public abstract class TypeIdResolverBase
implements TypeIdResolver {
    protected final TypeFactory _typeFactory;
    protected final JavaType _baseType;

    protected TypeIdResolverBase() {
        this(null, null);
    }

    protected TypeIdResolverBase(JavaType baseType, TypeFactory typeFactory) {
        this._baseType = baseType;
        this._typeFactory = typeFactory;
    }

    @Override
    public void init(JavaType bt) {
    }

    @Override
    public String idFromBaseType() {
        return this.idFromValueAndType(null, this._baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        throw new IllegalStateException("Sub-class " + this.getClass().getName() + " MUST implement `typeFromId(DatabindContext,String)");
    }

    @Override
    public String getDescForKnownTypeIds() {
        return null;
    }
}

