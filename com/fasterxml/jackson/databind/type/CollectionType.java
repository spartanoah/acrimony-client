/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import java.lang.reflect.TypeVariable;

public final class CollectionType
extends CollectionLikeType {
    private static final long serialVersionUID = 1L;

    private CollectionType(Class<?> collT, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType elemT, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(collT, bindings, superClass, superInts, elemT, valueHandler, typeHandler, asStatic);
    }

    protected CollectionType(TypeBase base, JavaType elemT) {
        super(base, elemT);
    }

    public static CollectionType construct(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType elemT) {
        return new CollectionType(rawType, bindings, superClass, superInts, elemT, null, null, false);
    }

    @Deprecated
    public static CollectionType construct(Class<?> rawType, JavaType elemT) {
        TypeVariable<Class<?>>[] vars = rawType.getTypeParameters();
        TypeBindings bindings = vars == null || vars.length != 1 ? TypeBindings.emptyBindings() : TypeBindings.create(rawType, elemT);
        return new CollectionType(rawType, bindings, CollectionType._bogusSuperClass(rawType), null, elemT, null, null, false);
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        return new CollectionType(subclass, this._bindings, this._superClass, this._superInterfaces, this._elementType, null, null, this._asStatic);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (this._elementType == contentType) {
            return this;
        }
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, contentType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionType withTypeHandler(Object h) {
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType, this._valueHandler, h, this._asStatic);
    }

    @Override
    public CollectionType withContentTypeHandler(Object h) {
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withTypeHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionType withValueHandler(Object h) {
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType, h, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionType withContentValueHandler(Object h) {
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withValueHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionType withStaticTyping() {
        if (this._asStatic) {
            return this;
        }
        return new CollectionType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withStaticTyping(), this._valueHandler, this._typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return new CollectionType(rawType, bindings, superClass, superInterfaces, this._elementType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public String toString() {
        return "[collection type; class " + this._class.getName() + ", contains " + this._elementType + "]";
    }
}

