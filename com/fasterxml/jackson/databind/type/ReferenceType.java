/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;

public class ReferenceType
extends SimpleType {
    private static final long serialVersionUID = 1L;
    protected final JavaType _referencedType;
    protected final JavaType _anchorType;

    protected ReferenceType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType refType, JavaType anchorType, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(cls, bindings, superClass, superInts, refType.hashCode(), valueHandler, typeHandler, asStatic);
        this._referencedType = refType;
        this._anchorType = anchorType == null ? this : anchorType;
    }

    protected ReferenceType(TypeBase base, JavaType refType) {
        super(base);
        this._referencedType = refType;
        this._anchorType = this;
    }

    public static ReferenceType upgradeFrom(JavaType baseType, JavaType refdType) {
        if (refdType == null) {
            throw new IllegalArgumentException("Missing referencedType");
        }
        if (baseType instanceof TypeBase) {
            return new ReferenceType((TypeBase)baseType, refdType);
        }
        throw new IllegalArgumentException("Cannot upgrade from an instance of " + baseType.getClass());
    }

    public static ReferenceType construct(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, JavaType refType) {
        return new ReferenceType(cls, bindings, superClass, superInts, refType, null, null, null, false);
    }

    @Deprecated
    public static ReferenceType construct(Class<?> cls, JavaType refType) {
        return new ReferenceType(cls, TypeBindings.emptyBindings(), null, null, null, refType, null, null, false);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (this._referencedType == contentType) {
            return this;
        }
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, contentType, this._anchorType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ReferenceType withTypeHandler(Object h) {
        if (h == this._typeHandler) {
            return this;
        }
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, this._referencedType, this._anchorType, this._valueHandler, h, this._asStatic);
    }

    @Override
    public ReferenceType withContentTypeHandler(Object h) {
        if (h == this._referencedType.getTypeHandler()) {
            return this;
        }
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, this._referencedType.withTypeHandler(h), this._anchorType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ReferenceType withValueHandler(Object h) {
        if (h == this._valueHandler) {
            return this;
        }
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, this._referencedType, this._anchorType, h, this._typeHandler, this._asStatic);
    }

    @Override
    public ReferenceType withContentValueHandler(Object h) {
        if (h == this._referencedType.getValueHandler()) {
            return this;
        }
        JavaType refdType = this._referencedType.withValueHandler(h);
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, refdType, this._anchorType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public ReferenceType withStaticTyping() {
        if (this._asStatic) {
            return this;
        }
        return new ReferenceType(this._class, this._bindings, this._superClass, this._superInterfaces, this._referencedType.withStaticTyping(), this._anchorType, this._valueHandler, this._typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return new ReferenceType(rawType, this._bindings, superClass, superInterfaces, this._referencedType, this._anchorType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        sb.append('<');
        sb.append(this._referencedType.toCanonical());
        sb.append('>');
        return sb.toString();
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        return new ReferenceType(subclass, this._bindings, this._superClass, this._superInterfaces, this._referencedType, this._anchorType, this._valueHandler, this._typeHandler, this._asStatic);
    }

    @Override
    public JavaType getContentType() {
        return this._referencedType;
    }

    @Override
    public JavaType getReferencedType() {
        return this._referencedType;
    }

    @Override
    public boolean hasContentType() {
        return true;
    }

    @Override
    public boolean isReferenceType() {
        return true;
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return ReferenceType._classSignature(this._class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        ReferenceType._classSignature(this._class, sb, false);
        sb.append('<');
        sb = this._referencedType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    public JavaType getAnchorType() {
        return this._anchorType;
    }

    public boolean isAnchorType() {
        return this._anchorType == this;
    }

    @Override
    public String toString() {
        return new StringBuilder(40).append("[reference type, class ").append(this.buildCanonicalName()).append('<').append(this._referencedType).append('>').append(']').toString();
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
        ReferenceType other = (ReferenceType)o;
        if (other._class != this._class) {
            return false;
        }
        return this._referencedType.equals(other._referencedType);
    }
}

