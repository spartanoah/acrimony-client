/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Type;

public interface TypeResolutionContext {
    public JavaType resolveType(Type var1);

    public static class Basic
    implements TypeResolutionContext {
        private final TypeFactory _typeFactory;
        private final TypeBindings _bindings;

        public Basic(TypeFactory tf, TypeBindings b) {
            this._typeFactory = tf;
            this._bindings = b;
        }

        @Override
        public JavaType resolveType(Type type) {
            if (type instanceof Class) {
                return this._typeFactory.constructType(type);
            }
            return this._typeFactory.constructType(type, this._bindings);
        }
    }
}

