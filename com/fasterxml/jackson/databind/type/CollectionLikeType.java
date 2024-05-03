/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

public class CollectionLikeType
extends TypeBase {
    private static final long serialVersionUID = 1L;
    protected final JavaType _elementType;

    protected CollectionLikeType(Class<?> collT, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType elemT, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(collT, bindings, superClass, superInts, elemT.hashCode(), valueHandler, typeHandler, asStatic);
        this._elementType = elemT;
    }

    protected CollectionLikeType(TypeBase base, JavaType elemT) {
        super(base);
        this._elementType = elemT;
    }

    public static CollectionLikeType construct(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType elemT) {
        return new CollectionLikeType(rawType, bindings, superClass, superInts, elemT, null, null, false);
    }

    @Deprecated
    public static CollectionLikeType construct(Class<?> rawType, JavaType elemT) {
        TypeVariable<Class<?>>[] vars = rawType.getTypeParameters();
        TypeBindings bindings = vars == null || vars.length != 1 ? TypeBindings.emptyBindings() : TypeBindings.create(rawType, elemT);
        return new CollectionLikeType(rawType, bindings, CollectionLikeType._bogusSuperClass(rawType), null, elemT, null, null, false);
    }

    public static CollectionLikeType upgradeFrom(JavaType baseType, JavaType elementType) {
        if (baseType instanceof TypeBase) {
            return new CollectionLikeType((TypeBase)baseType, elementType);
        }
        throw new IllegalArgumentException("Cannot upgrade from an instance of " + baseType.getClass());
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        return new CollectionLikeType(subclass, this._bindings, this._superClass, this._superInterfaces, this._elementType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (this._elementType == contentType) {
            return this;
        }
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, contentType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionLikeType withTypeHandler(Object h) {
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType, this._valueHandler, h, this._asStatic);
    }

    @Override
    public CollectionLikeType withContentTypeHandler(Object h) {
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withTypeHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionLikeType withValueHandler(Object h) {
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType, h, this._typeHandler, this._asStatic);
    }

    @Override
    public CollectionLikeType withContentValueHandler(Object h) {
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withValueHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public JavaType withHandlersFrom(JavaType src) {
        JavaType ct;
        JavaType type = super.withHandlersFrom(src);
        JavaType srcCt = src.getContentType();
        if (srcCt != null && (ct = this._elementType.withHandlersFrom(srcCt)) != this._elementType) {
            type = type.withContentType(ct);
        }
        return type;
    }

    @Override
    public CollectionLikeType withStaticTyping() {
        if (this._asStatic) {
            return this;
        }
        return new CollectionLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._elementType.withStaticTyping(), this._valueHandler, this._typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return new CollectionLikeType(rawType, bindings, superClass, superInterfaces, this._elementType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public boolean isContainerType() {
        return true;
    }

    @Override
    public boolean isCollectionLikeType() {
        return true;
    }

    @Override
    public JavaType getContentType() {
        return this._elementType;
    }

    @Override
    public Object getContentValueHandler() {
        return this._elementType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return this._elementType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || this._elementType.hasHandlers();
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return CollectionLikeType._classSignature(this._class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        CollectionLikeType._classSignature(this._class, sb, false);
        sb.append('<');
        this._elementType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        if (this._elementType != null) {
            sb.append('<');
            sb.append(this._elementType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }

    public boolean isTrueCollectionType() {
        return Collection.class.isAssignableFrom(this._class);
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
        CollectionLikeType other = (CollectionLikeType)o;
        return this._class == other._class && this._elementType.equals(other._elementType);
    }

    @Override
    public String toString() {
        return "[collection-like type; class " + this._class.getName() + ", contains " + this._elementType + "]";
    }
}

