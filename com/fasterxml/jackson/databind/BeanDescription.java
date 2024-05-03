/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.Converter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BeanDescription {
    protected final JavaType _type;

    protected BeanDescription(JavaType type) {
        this._type = type;
    }

    public JavaType getType() {
        return this._type;
    }

    public Class<?> getBeanClass() {
        return this._type.getRawClass();
    }

    public boolean isNonStaticInnerClass() {
        return this.getClassInfo().isNonStaticInnerClass();
    }

    public abstract AnnotatedClass getClassInfo();

    public abstract ObjectIdInfo getObjectIdInfo();

    public abstract boolean hasKnownClassAnnotations();

    @Deprecated
    public abstract TypeBindings bindingsForBeanType();

    @Deprecated
    public abstract JavaType resolveType(Type var1);

    public abstract Annotations getClassAnnotations();

    public abstract List<BeanPropertyDefinition> findProperties();

    public abstract Set<String> getIgnoredPropertyNames();

    public abstract List<BeanPropertyDefinition> findBackReferences();

    @Deprecated
    public abstract Map<String, AnnotatedMember> findBackReferenceProperties();

    public abstract List<AnnotatedConstructor> getConstructors();

    public abstract List<AnnotatedMethod> getFactoryMethods();

    public abstract AnnotatedConstructor findDefaultConstructor();

    public abstract Constructor<?> findSingleArgConstructor(Class<?> ... var1);

    public abstract Method findFactoryMethod(Class<?> ... var1);

    public abstract AnnotatedMember findJsonValueAccessor();

    public abstract AnnotatedMember findAnyGetter();

    public abstract AnnotatedMember findAnySetterAccessor();

    public abstract AnnotatedMethod findMethod(String var1, Class<?>[] var2);

    @Deprecated
    public abstract AnnotatedMethod findJsonValueMethod();

    @Deprecated
    public AnnotatedMethod findAnySetter() {
        AnnotatedMember m = this.findAnySetterAccessor();
        if (m instanceof AnnotatedMethod) {
            return (AnnotatedMethod)m;
        }
        return null;
    }

    @Deprecated
    public AnnotatedMember findAnySetterField() {
        AnnotatedMember m = this.findAnySetterAccessor();
        if (m instanceof AnnotatedField) {
            return m;
        }
        return null;
    }

    public abstract JsonInclude.Value findPropertyInclusion(JsonInclude.Value var1);

    public abstract JsonFormat.Value findExpectedFormat(JsonFormat.Value var1);

    public abstract Converter<Object, Object> findSerializationConverter();

    public abstract Converter<Object, Object> findDeserializationConverter();

    public String findClassDescription() {
        return null;
    }

    public abstract Map<Object, AnnotatedMember> findInjectables();

    public abstract Class<?> findPOJOBuilder();

    public abstract JsonPOJOBuilder.Value findPOJOBuilderConfig();

    public abstract Object instantiateBean(boolean var1);

    public abstract Class<?>[] findDefaultViews();
}

