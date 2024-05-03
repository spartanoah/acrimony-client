/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Collection;
import java.util.Map;

public class SimpleType
extends TypeBase {
    private static final long serialVersionUID = 1L;

    protected SimpleType(Class<?> cls) {
        this(cls, TypeBindings.emptyBindings(), null, null);
    }

    protected SimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts) {
        this(cls, bindings, superClass, superInts, null, null, false);
    }

    protected SimpleType(TypeBase base) {
        super(base);
    }

    protected SimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(cls, bindings, superClass, superInts, 0, valueHandler, typeHandler, asStatic);
    }

    protected SimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, int extraHash, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(cls, bindings, superClass, superInts, extraHash, valueHandler, typeHandler, asStatic);
    }

    public static SimpleType constructUnsafe(Class<?> raw) {
        return new SimpleType(raw, null, null, null, null, null, false);
    }

    @Deprecated
    public static SimpleType construct(Class<?> cls) {
        if (Map.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot construct SimpleType for a Map (class: " + cls.getName() + ")");
        }
        if (Collection.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot construct SimpleType for a Collection (class: " + cls.getName() + ")");
        }
        if (cls.isArray()) {
            throw new IllegalArgumentException("Cannot construct SimpleType for an array (class: " + cls.getName() + ")");
        }
        TypeBindings b = TypeBindings.emptyBindings();
        return new SimpleType(cls, b, SimpleType._buildSuperClass(cls.getSuperclass(), b), null, null, null, false);
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass) {
        Class<?>[] nextI;
        if (this._class == subclass) {
            return this;
        }
        if (!this._class.isAssignableFrom(subclass)) {
            return new SimpleType(subclass, this._bindings, this, this._superInterfaces, this._valueHandler, this._typeHandler, this._asStatic);
        }
        Class<?> next = subclass.getSuperclass();
        if (next == this._class) {
            return new SimpleType(subclass, this._bindings, this, this._superInterfaces, this._valueHandler, this._typeHandler, this._asStatic);
        }
        if (next != null && this._class.isAssignableFrom(next)) {
            JavaType superb = this._narrow(next);
            return new SimpleType(subclass, this._bindings, superb, null, this._valueHandler, this._typeHandler, this._asStatic);
        }
        for (Class<?> iface : nextI = subclass.getInterfaces()) {
            if (iface == this._class) {
                return new SimpleType(subclass, this._bindings, null, new JavaType[]{this}, this._valueHandler, this._typeHandler, this._asStatic);
            }
            if (!this._class.isAssignableFrom(iface)) continue;
            JavaType superb = this._narrow(iface);
            return new SimpleType(subclass, this._bindings, null, new JavaType[]{superb}, this._valueHandler, this._typeHandler, this._asStatic);
        }
        throw new IllegalArgumentException("Internal error: Cannot resolve sub-type for Class " + subclass.getName() + " to " + this._class.getName());
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContentType()");
    }

    @Override
    public SimpleType withTypeHandler(Object h) {
        if (this._typeHandler == h) {
            return this;
        }
        return new SimpleType(this._class, this._bindings, this._superClass, this._superInterfaces, this._valueHandler, h, this._asStatic);
    }

    @Override
    public JavaType withContentTypeHandler(Object h) {
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContenTypeHandler()");
    }

    @Override
    public SimpleType withValueHandler(Object h) {
        if (h == this._valueHandler) {
            return this;
        }
        return new SimpleType(this._class, this._bindings, this._superClass, this._superInterfaces, h, this._typeHandler, this._asStatic);
    }

    @Override
    public SimpleType withContentValueHandler(Object h) {
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContenValueHandler()");
    }

    @Override
    public SimpleType withStaticTyping() {
        return this._asStatic ? this : new SimpleType(this._class, this._bindings, this._superClass, this._superInterfaces, this._valueHandler, this._typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return null;
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this._class.getName());
        int count = this._bindings.size();
        if (count > 0) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                JavaType t = this.containedType(i);
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(t.toCanonical());
            }
            sb.append('>');
        }
        return sb.toString();
    }

    @Override
    public boolean isContainerType() {
        return false;
    }

    @Override
    public boolean hasContentType() {
        return false;
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return SimpleType._classSignature(this._class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        SimpleType._classSignature(this._class, sb, false);
        int count = this._bindings.size();
        if (count > 0) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                sb = this.containedType(i).getGenericSignature(sb);
            }
            sb.append('>');
        }
        sb.append(';');
        return sb;
    }

    private static JavaType _buildSuperClass(Class<?> superClass, TypeBindings b) {
        if (superClass == null) {
            return null;
        }
        if (superClass == Object.class) {
            return TypeFactory.unknownType();
        }
        JavaType superSuper = SimpleType._buildSuperClass(superClass.getSuperclass(), b);
        return new SimpleType(superClass, b, superSuper, null, null, null, false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(40);
        sb.append("[simple type, class ").append(this.buildCanonicalName()).append(']');
        return sb.toString();
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
        SimpleType other = (SimpleType)o;
        if (other._class != this._class) {
            return false;
        }
        TypeBindings b1 = this._bindings;
        TypeBindings b2 = other._bindings;
        return b1.equals(b2);
    }
}

