/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.CollectorBase;
import com.fasterxml.jackson.databind.introspect.MemberKey;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AnnotatedCreatorCollector
extends CollectorBase {
    private final TypeResolutionContext _typeContext;
    private final boolean _collectAnnotations;
    private AnnotatedConstructor _defaultConstructor;

    AnnotatedCreatorCollector(AnnotationIntrospector intr, TypeResolutionContext tc, boolean collectAnnotations) {
        super(intr);
        this._typeContext = tc;
        this._collectAnnotations = collectAnnotations;
    }

    public static AnnotatedClass.Creators collectCreators(AnnotationIntrospector intr, TypeResolutionContext tc, JavaType type, Class<?> primaryMixIn, boolean collectAnnotations) {
        boolean checkClassAnnotations = intr != null && !ClassUtil.isJDKClass(type.getRawClass());
        return new AnnotatedCreatorCollector(intr, tc, checkClassAnnotations).collect(type, primaryMixIn);
    }

    AnnotatedClass.Creators collect(JavaType type, Class<?> primaryMixIn) {
        List<AnnotatedConstructor> constructors = this._findPotentialConstructors(type, primaryMixIn);
        List<AnnotatedMethod> factories = this._findPotentialFactories(type, primaryMixIn);
        if (this._collectAnnotations) {
            if (this._defaultConstructor != null && this._intr.hasIgnoreMarker(this._defaultConstructor)) {
                this._defaultConstructor = null;
            }
            int i = constructors.size();
            while (--i >= 0) {
                if (!this._intr.hasIgnoreMarker(constructors.get(i))) continue;
                constructors.remove(i);
            }
            i = factories.size();
            while (--i >= 0) {
                if (!this._intr.hasIgnoreMarker(factories.get(i))) continue;
                factories.remove(i);
            }
        }
        return new AnnotatedClass.Creators(this._defaultConstructor, constructors, factories);
    }

    private List<AnnotatedConstructor> _findPotentialConstructors(JavaType type, Class<?> primaryMixIn) {
        int ctorCount;
        List<AnnotatedConstructor> result;
        ClassUtil.Ctor defaultCtor = null;
        ArrayList<ClassUtil.Ctor> ctors = null;
        if (!type.isEnumType()) {
            ClassUtil.Ctor[] declaredCtors;
            for (ClassUtil.Ctor ctor : declaredCtors = ClassUtil.getConstructors(type.getRawClass())) {
                if (!AnnotatedCreatorCollector.isIncludableConstructor(ctor.getConstructor())) continue;
                if (ctor.getParamCount() == 0) {
                    defaultCtor = ctor;
                    continue;
                }
                if (ctors == null) {
                    ctors = new ArrayList<ClassUtil.Ctor>();
                }
                ctors.add(ctor);
            }
        }
        if (ctors == null) {
            result = Collections.emptyList();
            if (defaultCtor == null) {
                return result;
            }
            ctorCount = 0;
        } else {
            ctorCount = ctors.size();
            result = new ArrayList<AnnotatedConstructor>(ctorCount);
            for (int i = 0; i < ctorCount; ++i) {
                result.add(null);
            }
        }
        if (primaryMixIn != null) {
            MemberKey[] ctorKeys = null;
            block2: for (ClassUtil.Ctor mixinCtor : ClassUtil.getConstructors(primaryMixIn)) {
                if (mixinCtor.getParamCount() == 0) {
                    if (defaultCtor == null) continue;
                    this._defaultConstructor = this.constructDefaultConstructor(defaultCtor, mixinCtor);
                    defaultCtor = null;
                    continue;
                }
                if (ctors == null) continue;
                if (ctorKeys == null) {
                    ctorKeys = new MemberKey[ctorCount];
                    for (int i = 0; i < ctorCount; ++i) {
                        ctorKeys[i] = new MemberKey(((ClassUtil.Ctor)ctors.get(i)).getConstructor());
                    }
                }
                MemberKey key = new MemberKey(mixinCtor.getConstructor());
                for (int i = 0; i < ctorCount; ++i) {
                    if (!key.equals(ctorKeys[i])) continue;
                    result.set(i, this.constructNonDefaultConstructor((ClassUtil.Ctor)ctors.get(i), mixinCtor));
                    continue block2;
                }
            }
        }
        if (defaultCtor != null) {
            this._defaultConstructor = this.constructDefaultConstructor(defaultCtor, null);
        }
        for (int i = 0; i < ctorCount; ++i) {
            AnnotatedConstructor ctor = result.get(i);
            if (ctor != null) continue;
            result.set(i, this.constructNonDefaultConstructor((ClassUtil.Ctor)ctors.get(i), null));
        }
        return result;
    }

    private List<AnnotatedMethod> _findPotentialFactories(JavaType type, Class<?> primaryMixIn) {
        ArrayList<Method> candidates = null;
        for (Method method : ClassUtil.getClassMethods(type.getRawClass())) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (candidates == null) {
                candidates = new ArrayList<Method>();
            }
            candidates.add(method);
        }
        if (candidates == null) {
            return Collections.emptyList();
        }
        int factoryCount = candidates.size();
        ArrayList<AnnotatedMethod> result = new ArrayList<AnnotatedMethod>(factoryCount);
        for (int i = 0; i < factoryCount; ++i) {
            result.add(null);
        }
        if (primaryMixIn != null) {
            MemberKey[] methodKeys = null;
            block2: for (Method mixinFactory : primaryMixIn.getDeclaredMethods()) {
                if (!Modifier.isStatic(mixinFactory.getModifiers())) continue;
                if (methodKeys == null) {
                    methodKeys = new MemberKey[factoryCount];
                    for (int i = 0; i < factoryCount; ++i) {
                        methodKeys[i] = new MemberKey((Method)candidates.get(i));
                    }
                }
                MemberKey key = new MemberKey(mixinFactory);
                for (int i = 0; i < factoryCount; ++i) {
                    if (!key.equals(methodKeys[i])) continue;
                    result.set(i, this.constructFactoryCreator((Method)candidates.get(i), mixinFactory));
                    continue block2;
                }
            }
        }
        for (int i = 0; i < factoryCount; ++i) {
            AnnotatedMethod annotatedMethod = (AnnotatedMethod)result.get(i);
            if (annotatedMethod != null) continue;
            result.set(i, this.constructFactoryCreator((Method)candidates.get(i), null));
        }
        return result;
    }

    protected AnnotatedConstructor constructDefaultConstructor(ClassUtil.Ctor ctor, ClassUtil.Ctor mixin) {
        return new AnnotatedConstructor(this._typeContext, ctor.getConstructor(), this.collectAnnotations(ctor, mixin), NO_ANNOTATION_MAPS);
    }

    protected AnnotatedConstructor constructNonDefaultConstructor(ClassUtil.Ctor ctor, ClassUtil.Ctor mixin) {
        AnnotationMap[] resolvedAnnotations;
        int paramCount = ctor.getParamCount();
        if (this._intr == null) {
            return new AnnotatedConstructor(this._typeContext, ctor.getConstructor(), AnnotatedCreatorCollector._emptyAnnotationMap(), AnnotatedCreatorCollector._emptyAnnotationMaps(paramCount));
        }
        if (paramCount == 0) {
            return new AnnotatedConstructor(this._typeContext, ctor.getConstructor(), this.collectAnnotations(ctor, mixin), NO_ANNOTATION_MAPS);
        }
        Annotation[][] paramAnns = ctor.getParameterAnnotations();
        if (paramCount != paramAnns.length) {
            resolvedAnnotations = null;
            Class<?> dc = ctor.getDeclaringClass();
            if (ClassUtil.isEnumType(dc) && paramCount == paramAnns.length + 2) {
                Annotation[][] old = paramAnns;
                paramAnns = new Annotation[old.length + 2][];
                System.arraycopy(old, 0, paramAnns, 2, old.length);
                resolvedAnnotations = this.collectAnnotations(paramAnns, (Annotation[][])null);
            } else if (dc.isMemberClass() && paramCount == paramAnns.length + 1) {
                Annotation[][] old = paramAnns;
                paramAnns = new Annotation[old.length + 1][];
                System.arraycopy(old, 0, paramAnns, 1, old.length);
                paramAnns[0] = NO_ANNOTATIONS;
                resolvedAnnotations = this.collectAnnotations(paramAnns, (Annotation[][])null);
            }
            if (resolvedAnnotations == null) {
                throw new IllegalStateException(String.format("Internal error: constructor for %s has mismatch: %d parameters; %d sets of annotations", ctor.getDeclaringClass().getName(), paramCount, paramAnns.length));
            }
        } else {
            resolvedAnnotations = this.collectAnnotations(paramAnns, mixin == null ? (Annotation[][])null : mixin.getParameterAnnotations());
        }
        return new AnnotatedConstructor(this._typeContext, ctor.getConstructor(), this.collectAnnotations(ctor, mixin), resolvedAnnotations);
    }

    protected AnnotatedMethod constructFactoryCreator(Method m, Method mixin) {
        int paramCount = m.getParameterTypes().length;
        if (this._intr == null) {
            return new AnnotatedMethod(this._typeContext, m, AnnotatedCreatorCollector._emptyAnnotationMap(), AnnotatedCreatorCollector._emptyAnnotationMaps(paramCount));
        }
        if (paramCount == 0) {
            return new AnnotatedMethod(this._typeContext, m, this.collectAnnotations(m, mixin), NO_ANNOTATION_MAPS);
        }
        return new AnnotatedMethod(this._typeContext, m, this.collectAnnotations(m, mixin), this.collectAnnotations(m.getParameterAnnotations(), mixin == null ? (Annotation[][])null : mixin.getParameterAnnotations()));
    }

    private AnnotationMap[] collectAnnotations(Annotation[][] mainAnns, Annotation[][] mixinAnns) {
        if (this._collectAnnotations) {
            int count = mainAnns.length;
            AnnotationMap[] result = new AnnotationMap[count];
            for (int i = 0; i < count; ++i) {
                AnnotationCollector c = this.collectAnnotations(AnnotationCollector.emptyCollector(), mainAnns[i]);
                if (mixinAnns != null) {
                    c = this.collectAnnotations(c, mixinAnns[i]);
                }
                result[i] = c.asAnnotationMap();
            }
            return result;
        }
        return NO_ANNOTATION_MAPS;
    }

    private AnnotationMap collectAnnotations(ClassUtil.Ctor main, ClassUtil.Ctor mixin) {
        if (this._collectAnnotations) {
            AnnotationCollector c = this.collectAnnotations(main.getDeclaredAnnotations());
            if (mixin != null) {
                c = this.collectAnnotations(c, mixin.getDeclaredAnnotations());
            }
            return c.asAnnotationMap();
        }
        return AnnotatedCreatorCollector._emptyAnnotationMap();
    }

    private final AnnotationMap collectAnnotations(AnnotatedElement main, AnnotatedElement mixin) {
        AnnotationCollector c = this.collectAnnotations(main.getDeclaredAnnotations());
        if (mixin != null) {
            c = this.collectAnnotations(c, mixin.getDeclaredAnnotations());
        }
        return c.asAnnotationMap();
    }

    private static boolean isIncludableConstructor(Constructor<?> c) {
        return !c.isSynthetic();
    }
}

