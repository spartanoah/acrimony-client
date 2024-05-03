/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import java.lang.reflect.Array;

public final class ArrayType
extends TypeBase {
    private static final long serialVersionUID = 1L;
    protected final JavaType _componentType;
    protected final Object _emptyArray;

    protected ArrayType(JavaType componentType, TypeBindings bindings, Object emptyInstance, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(emptyInstance.getClass(), bindings, null, null, componentType.hashCode(), valueHandler, typeHandler, asStatic);
        this._componentType = componentType;
        this._emptyArray = emptyInstance;
    }

    public static ArrayType construct(JavaType componentType, TypeBindings bindings) {
        return ArrayType.construct(componentType, bindings, null, null);
    }

    public static ArrayType construct(JavaType componentType, TypeBindings bindings, Object valueHandler, Object typeHandler) {
        Object emptyInstance = Array.newInstance(componentType.getRawClass(), 0);
        return new ArrayType(componentType, bindings, emptyInstance, valueHandler, typeHandler, false);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        Object emptyInstance = Array.newInstance(contentType.getRawClass(), 0);
        return new ArrayType(contentType, this._bindings, emptyInstance, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ArrayType withTypeHandler(Object h) {
        if (h == this._typeHandler) {
            return this;
        }
        return new ArrayType(this._componentType, this._bindings, this._emptyArray, this._valueHandler, h, this._asStatic);
    }

    @Override
    public ArrayType withContentTypeHandler(Object h) {
        if (h == this._componentType.getTypeHandler()) {
            return this;
        }
        return new ArrayType(this._componentType.withTypeHandler(h), this._bindings, this._emptyArray, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ArrayType withValueHandler(Object h) {
        if (h == this._valueHandler) {
            return this;
        }
        return new ArrayType(this._componentType, this._bindings, this._emptyArray, h, this._typeHandler, this._asStatic);
    }

    @Override
    public ArrayType withContentValueHandler(Object h) {
        if (h == this._componentType.getValueHandler()) {
            return this;
        }
        return new ArrayType(this._componentType.withValueHandler(h), this._bindings, this._emptyArray, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ArrayType withStaticTyping() {
        if (this._asStatic) {
            return this;
        }
        return new ArrayType(this._componentType.withStaticTyping(), this._bindings, this._emptyArray, this._valueHandler, this._typeHandler, true);
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        return this._reportUnsupported();
    }

    @Override
    public JavaType refine(Class<?> contentClass, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return null;
    }

    private JavaType _reportUnsupported() {
        throw new UnsupportedOperationException("Cannot narrow or widen array types");
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public boolean hasGenericTypes() {
        return this._componentType.hasGenericTypes();
    }

    @Override
    public boolean isContainerType() {
        return true;
    }

    @Override
    public JavaType getContentType() {
        return this._componentType;
    }

    @Override
    public Object getContentValueHandler() {
        return this._componentType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return this._componentType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || this._componentType.hasHandlers();
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        sb.append('[');
        return this._componentType.getGenericSignature(sb);
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        sb.append('[');
        return this._componentType.getErasedSignature(sb);
    }

    @Override
    public String toString() {
        return "[array type, component type: " + this._componentType + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        ArrayType other = (ArrayType)o;
        return this._componentType.equals(other._componentType);
    }
}

