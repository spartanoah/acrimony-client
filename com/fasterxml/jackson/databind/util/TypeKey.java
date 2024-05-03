/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.JavaType;

public class TypeKey {
    protected int _hashCode;
    protected Class<?> _class;
    protected JavaType _type;
    protected boolean _isTyped;

    public TypeKey() {
    }

    public TypeKey(TypeKey src) {
        this._hashCode = src._hashCode;
        this._class = src._class;
        this._type = src._type;
        this._isTyped = src._isTyped;
    }

    public TypeKey(Class<?> key, boolean typed) {
        this._class = key;
        this._type = null;
        this._isTyped = typed;
        this._hashCode = typed ? TypeKey.typedHash(key) : TypeKey.untypedHash(key);
    }

    public TypeKey(JavaType key, boolean typed) {
        this._type = key;
        this._class = null;
        this._isTyped = typed;
        this._hashCode = typed ? TypeKey.typedHash(key) : TypeKey.untypedHash(key);
    }

    public static final int untypedHash(Class<?> cls) {
        return cls.getName().hashCode();
    }

    public static final int typedHash(Class<?> cls) {
        return cls.getName().hashCode() + 1;
    }

    public static final int untypedHash(JavaType type) {
        return type.hashCode() - 1;
    }

    public static final int typedHash(JavaType type) {
        return type.hashCode() - 2;
    }

    public final void resetTyped(Class<?> cls) {
        this._type = null;
        this._class = cls;
        this._isTyped = true;
        this._hashCode = TypeKey.typedHash(cls);
    }

    public final void resetUntyped(Class<?> cls) {
        this._type = null;
        this._class = cls;
        this._isTyped = false;
        this._hashCode = TypeKey.untypedHash(cls);
    }

    public final void resetTyped(JavaType type) {
        this._type = type;
        this._class = null;
        this._isTyped = true;
        this._hashCode = TypeKey.typedHash(type);
    }

    public final void resetUntyped(JavaType type) {
        this._type = type;
        this._class = null;
        this._isTyped = false;
        this._hashCode = TypeKey.untypedHash(type);
    }

    public boolean isTyped() {
        return this._isTyped;
    }

    public Class<?> getRawType() {
        return this._class;
    }

    public JavaType getType() {
        return this._type;
    }

    public final int hashCode() {
        return this._hashCode;
    }

    public final String toString() {
        if (this._class != null) {
            return "{class: " + this._class.getName() + ", typed? " + this._isTyped + "}";
        }
        return "{type: " + this._type + ", typed? " + this._isTyped + "}";
    }

    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        TypeKey other = (TypeKey)o;
        if (other._isTyped == this._isTyped) {
            if (this._class != null) {
                return other._class == this._class;
            }
            return this._type.equals(other._type);
        }
        return false;
    }
}

