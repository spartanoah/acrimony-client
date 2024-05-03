/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public final class AnnotatedField
extends AnnotatedMember
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final transient Field _field;
    protected Serialization _serialization;

    public AnnotatedField(TypeResolutionContext contextClass, Field field, AnnotationMap annMap) {
        super(contextClass, annMap);
        this._field = field;
    }

    @Override
    public AnnotatedField withAnnotations(AnnotationMap ann) {
        return new AnnotatedField(this._typeContext, this._field, ann);
    }

    protected AnnotatedField(Serialization ser) {
        super(null, null);
        this._field = null;
        this._serialization = ser;
    }

    @Override
    public Field getAnnotated() {
        return this._field;
    }

    @Override
    public int getModifiers() {
        return this._field.getModifiers();
    }

    @Override
    public String getName() {
        return this._field.getName();
    }

    @Override
    public Class<?> getRawType() {
        return this._field.getType();
    }

    @Override
    public JavaType getType() {
        return this._typeContext.resolveType(this._field.getGenericType());
    }

    @Override
    public Class<?> getDeclaringClass() {
        return this._field.getDeclaringClass();
    }

    @Override
    public Member getMember() {
        return this._field;
    }

    @Override
    public void setValue(Object pojo, Object value) throws IllegalArgumentException {
        try {
            this._field.set(pojo, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to setValue() for field " + this.getFullName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Object getValue(Object pojo) throws IllegalArgumentException {
        try {
            return this._field.get(pojo);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to getValue() for field " + this.getFullName() + ": " + e.getMessage(), e);
        }
    }

    public int getAnnotationCount() {
        return this._annotations.size();
    }

    public boolean isTransient() {
        return Modifier.isTransient(this.getModifiers());
    }

    @Override
    public int hashCode() {
        return this._field.getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        return ClassUtil.hasClass(o, this.getClass()) && ((AnnotatedField)o)._field == this._field;
    }

    @Override
    public String toString() {
        return "[field " + this.getFullName() + "]";
    }

    Object writeReplace() {
        return new AnnotatedField(new Serialization(this._field));
    }

    Object readResolve() {
        Class<?> clazz = this._serialization.clazz;
        try {
            Field f = clazz.getDeclaredField(this._serialization.name);
            if (!f.isAccessible()) {
                ClassUtil.checkAndFixAccess(f, false);
            }
            return new AnnotatedField(null, f, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find method '" + this._serialization.name + "' from Class '" + clazz.getName());
        }
    }

    private static final class Serialization
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected Class<?> clazz;
        protected String name;

        public Serialization(Field f) {
            this.clazz = f.getDeclaringClass();
            this.name = f.getName();
        }
    }
}

