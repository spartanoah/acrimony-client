/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class MapLikeType
extends TypeBase {
    private static final long serialVersionUID = 1L;
    protected final JavaType _keyType;
    protected final JavaType _valueType;

    protected MapLikeType(Class<?> mapType, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType keyT, JavaType valueT, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(mapType, bindings, superClass, superInts, keyT.hashCode() ^ valueT.hashCode(), valueHandler, typeHandler, asStatic);
        this._keyType = keyT;
        this._valueType = valueT;
    }

    protected MapLikeType(TypeBase base, JavaType keyT, JavaType valueT) {
        super(base);
        this._keyType = keyT;
        this._valueType = valueT;
    }

    public static MapLikeType upgradeFrom(JavaType baseType, JavaType keyT, JavaType valueT) {
        if (baseType instanceof TypeBase) {
            return new MapLikeType((TypeBase)baseType, keyT, valueT);
        }
        throw new IllegalArgumentException("Cannot upgrade from an instance of " + baseType.getClass());
    }

    @Deprecated
    public static MapLikeType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
        TypeVariable<Class<?>>[] vars = rawType.getTypeParameters();
        TypeBindings bindings = vars == null || vars.length != 2 ? TypeBindings.emptyBindings() : TypeBindings.create(rawType, keyT, valueT);
        return new MapLikeType(rawType, bindings, MapLikeType._bogusSuperClass(rawType), null, keyT, valueT, null, null, false);
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        return new MapLikeType(subclass, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    public MapLikeType withKeyType(JavaType keyType) {
        if (keyType == this._keyType) {
            return this;
        }
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, keyType, this._valueType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (this._valueType == contentType) {
            return this;
        }
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, contentType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public MapLikeType withTypeHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType, this._valueHandler, h, this._asStatic);
    }

    @Override
    public MapLikeType withContentTypeHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType.withTypeHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public MapLikeType withValueHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType, h, this._typeHandler, this._asStatic);
    }

    @Override
    public MapLikeType withContentValueHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType.withValueHandler(h), this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public JavaType withHandlersFrom(JavaType src) {
        JavaType ct;
        JavaType srcCt;
        JavaType ct2;
        JavaType type = super.withHandlersFrom(src);
        JavaType srcKeyType = src.getKeyType();
        if (type instanceof MapLikeType && srcKeyType != null && (ct2 = this._keyType.withHandlersFrom(srcKeyType)) != this._keyType) {
            type = ((MapLikeType)type).withKeyType(ct2);
        }
        if ((srcCt = src.getContentType()) != null && (ct = this._valueType.withHandlersFrom(srcCt)) != this._valueType) {
            type = type.withContentType(ct);
        }
        return type;
    }

    @Override
    public MapLikeType withStaticTyping() {
        if (this._asStatic) {
            return this;
        }
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType, this._valueType.withStaticTyping(), this._valueHandler, this._typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return new MapLikeType(rawType, bindings, superClass, superInterfaces, this._keyType, this._valueType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        if (this._keyType != null) {
            sb.append('<');
            sb.append(this._keyType.toCanonical());
            sb.append(',');
            sb.append(this._valueType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }

    @Override
    public boolean isContainerType() {
        return true;
    }

    @Override
    public boolean isMapLikeType() {
        return true;
    }

    @Override
    public JavaType getKeyType() {
        return this._keyType;
    }

    @Override
    public JavaType getContentType() {
        return this._valueType;
    }

    @Override
    public Object getContentValueHandler() {
        return this._valueType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return this._valueType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || this._valueType.hasHandlers() || this._keyType.hasHandlers();
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return MapLikeType._classSignature(this._class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        MapLikeType._classSignature(this._class, sb, false);
        sb.append('<');
        this._keyType.getGenericSignature(sb);
        this._valueType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    public MapLikeType withKeyTypeHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType.withTypeHandler(h), this._valueType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    public MapLikeType withKeyValueHandler(Object h) {
        return new MapLikeType(this._class, this._bindings, this._superClass, this._superInterfaces, this._keyType.withValueHandler(h), this._valueType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    public boolean isTrueMapType() {
        return Map.class.isAssignableFrom(this._class);
    }

    @Override
    public String toString() {
        return String.format("[map-like type; class %s, %s -> %s]", this._class.getName(), this._keyType, this._valueType);
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
        MapLikeType other = (MapLikeType)o;
        return this._class == other._class && this._keyType.equals(other._keyType) && this._valueType.equals(other._valueType);
    }
}

