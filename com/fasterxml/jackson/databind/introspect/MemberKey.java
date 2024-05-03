/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MemberKey {
    static final Class<?>[] NO_CLASSES = new Class[0];
    final String _name;
    final Class<?>[] _argTypes;

    public MemberKey(Method m) {
        this(m.getName(), m.getParameterTypes());
    }

    public MemberKey(Constructor<?> ctor) {
        this("", ctor.getParameterTypes());
    }

    public MemberKey(String name, Class<?>[] argTypes) {
        this._name = name;
        this._argTypes = argTypes == null ? NO_CLASSES : argTypes;
    }

    public String getName() {
        return this._name;
    }

    public int argCount() {
        return this._argTypes.length;
    }

    public String toString() {
        return this._name + "(" + this._argTypes.length + "-args)";
    }

    public int hashCode() {
        return this._name.hashCode() + this._argTypes.length;
    }

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
        MemberKey other = (MemberKey)o;
        if (!this._name.equals(other._name)) {
            return false;
        }
        Class<?>[] otherArgs = other._argTypes;
        int len = this._argTypes.length;
        if (otherArgs.length != len) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            Class<?> type1 = otherArgs[i];
            Class<?> type2 = this._argTypes[i];
            if (type1 == type2) continue;
            return false;
        }
        return true;
    }
}

