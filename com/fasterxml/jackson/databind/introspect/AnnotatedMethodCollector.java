/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethodMap;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.CollectorBase;
import com.fasterxml.jackson.databind.introspect.MemberKey;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnnotatedMethodCollector
extends CollectorBase {
    private final ClassIntrospector.MixInResolver _mixInResolver;
    private final boolean _collectAnnotations;

    AnnotatedMethodCollector(AnnotationIntrospector intr, ClassIntrospector.MixInResolver mixins, boolean collectAnnotations) {
        super(intr);
        this._mixInResolver = intr == null ? null : mixins;
        this._collectAnnotations = collectAnnotations;
    }

    public static AnnotatedMethodMap collectMethods(AnnotationIntrospector intr, TypeResolutionContext tc, ClassIntrospector.MixInResolver mixins, TypeFactory types, JavaType type, List<JavaType> superTypes, Class<?> primaryMixIn, boolean collectAnnotations) {
        return new AnnotatedMethodCollector(intr, mixins, collectAnnotations).collect(types, tc, type, superTypes, primaryMixIn);
    }

    AnnotatedMethodMap collect(TypeFactory typeFactory, TypeResolutionContext tc, JavaType mainType, List<JavaType> superTypes, Class<?> primaryMixIn) {
        Object mixin;
        LinkedHashMap<MemberKey, MethodBuilder> methods = new LinkedHashMap<MemberKey, MethodBuilder>();
        this._addMemberMethods(tc, mainType.getRawClass(), methods, primaryMixIn);
        for (JavaType type : superTypes) {
            Class<?> clazz = this._mixInResolver == null ? null : this._mixInResolver.findMixInClassFor(type.getRawClass());
            this._addMemberMethods(new TypeResolutionContext.Basic(typeFactory, type.getBindings()), type.getRawClass(), methods, clazz);
        }
        boolean checkJavaLangObject = false;
        if (this._mixInResolver != null && (mixin = this._mixInResolver.findMixInClassFor(Object.class)) != null) {
            this._addMethodMixIns(tc, mainType.getRawClass(), (Map<MemberKey, MethodBuilder>)methods, (Class<?>)mixin);
            checkJavaLangObject = true;
        }
        if (checkJavaLangObject && this._intr != null && !methods.isEmpty()) {
            for (Map.Entry entry : methods.entrySet()) {
                MemberKey k = (MemberKey)entry.getKey();
                if (!"hashCode".equals(k.getName()) || 0 != k.argCount()) continue;
                try {
                    Method m = Object.class.getDeclaredMethod(k.getName(), new Class[0]);
                    if (m == null) continue;
                    MethodBuilder b = (MethodBuilder)entry.getValue();
                    b.annotations = this.collectDefaultAnnotations(b.annotations, m.getDeclaredAnnotations());
                    b.method = m;
                } catch (Exception m) {}
            }
        }
        if (methods.isEmpty()) {
            return new AnnotatedMethodMap();
        }
        LinkedHashMap<MemberKey, AnnotatedMethod> actual = new LinkedHashMap<MemberKey, AnnotatedMethod>(methods.size());
        for (Map.Entry entry : methods.entrySet()) {
            AnnotatedMethod am = ((MethodBuilder)entry.getValue()).build();
            if (am == null) continue;
            actual.put((MemberKey)entry.getKey(), am);
        }
        return new AnnotatedMethodMap(actual);
    }

    private void _addMemberMethods(TypeResolutionContext tc, Class<?> cls, Map<MemberKey, MethodBuilder> methods, Class<?> mixInCls) {
        if (mixInCls != null) {
            this._addMethodMixIns(tc, cls, methods, mixInCls);
        }
        if (cls == null) {
            return;
        }
        for (Method m : ClassUtil.getClassMethods(cls)) {
            Method old;
            if (!this._isIncludableMemberMethod(m)) continue;
            MemberKey key = new MemberKey(m);
            MethodBuilder b = methods.get(key);
            if (b == null) {
                AnnotationCollector c = this._intr == null ? AnnotationCollector.emptyCollector() : this.collectAnnotations(m.getDeclaredAnnotations());
                methods.put(key, new MethodBuilder(tc, m, c));
                continue;
            }
            if (this._collectAnnotations) {
                b.annotations = this.collectDefaultAnnotations(b.annotations, m.getDeclaredAnnotations());
            }
            if ((old = b.method) == null) {
                b.method = m;
                continue;
            }
            if (!Modifier.isAbstract(old.getModifiers()) || Modifier.isAbstract(m.getModifiers())) continue;
            b.method = m;
            b.typeContext = tc;
        }
    }

    protected void _addMethodMixIns(TypeResolutionContext tc, Class<?> targetClass, Map<MemberKey, MethodBuilder> methods, Class<?> mixInCls) {
        if (this._intr == null) {
            return;
        }
        for (Class<?> mixin : ClassUtil.findRawSuperTypes(mixInCls, targetClass, true)) {
            for (Method m : mixin.getDeclaredMethods()) {
                if (!this._isIncludableMemberMethod(m)) continue;
                MemberKey key = new MemberKey(m);
                MethodBuilder b = methods.get(key);
                Annotation[] anns = m.getDeclaredAnnotations();
                if (b == null) {
                    methods.put(key, new MethodBuilder(tc, null, this.collectAnnotations(anns)));
                    continue;
                }
                b.annotations = this.collectDefaultAnnotations(b.annotations, anns);
            }
        }
    }

    private boolean _isIncludableMemberMethod(Method m) {
        if (Modifier.isStatic(m.getModifiers()) || m.isSynthetic() || m.isBridge()) {
            return false;
        }
        int pcount = m.getParameterTypes().length;
        return pcount <= 2;
    }

    private static final class MethodBuilder {
        public TypeResolutionContext typeContext;
        public Method method;
        public AnnotationCollector annotations;

        public MethodBuilder(TypeResolutionContext tc, Method m, AnnotationCollector ann) {
            this.typeContext = tc;
            this.method = m;
            this.annotations = ann;
        }

        public AnnotatedMethod build() {
            if (this.method == null) {
                return null;
            }
            return new AnnotatedMethod(this.typeContext, this.method, this.annotations.asAnnotationMap(), null);
        }
    }
}

