/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnnotatedClassResolver {
    private static final Annotations NO_ANNOTATIONS = AnnotationCollector.emptyAnnotations();
    private static final Class<?> CLS_OBJECT = Object.class;
    private static final Class<?> CLS_ENUM = Enum.class;
    private static final Class<?> CLS_LIST = List.class;
    private static final Class<?> CLS_MAP = Map.class;
    private final MapperConfig<?> _config;
    private final AnnotationIntrospector _intr;
    private final ClassIntrospector.MixInResolver _mixInResolver;
    private final TypeBindings _bindings;
    private final JavaType _type;
    private final Class<?> _class;
    private final Class<?> _primaryMixin;
    private final boolean _collectAnnotations;

    AnnotatedClassResolver(MapperConfig<?> config, JavaType type, ClassIntrospector.MixInResolver r) {
        this._config = config;
        this._type = type;
        this._class = type.getRawClass();
        this._mixInResolver = r;
        this._bindings = type.getBindings();
        this._intr = config.isAnnotationProcessingEnabled() ? config.getAnnotationIntrospector() : null;
        this._primaryMixin = r == null ? null : r.findMixInClassFor(this._class);
        this._collectAnnotations = this._intr != null && (!ClassUtil.isJDKClass(this._class) || !this._type.isContainerType());
    }

    AnnotatedClassResolver(MapperConfig<?> config, Class<?> cls, ClassIntrospector.MixInResolver r) {
        this._config = config;
        this._type = null;
        this._class = cls;
        this._mixInResolver = r;
        this._bindings = TypeBindings.emptyBindings();
        if (config == null) {
            this._intr = null;
            this._primaryMixin = null;
        } else {
            this._intr = config.isAnnotationProcessingEnabled() ? config.getAnnotationIntrospector() : null;
            this._primaryMixin = r == null ? null : r.findMixInClassFor(this._class);
        }
        this._collectAnnotations = this._intr != null;
    }

    public static AnnotatedClass resolve(MapperConfig<?> config, JavaType forType, ClassIntrospector.MixInResolver r) {
        if (forType.isArrayType() && AnnotatedClassResolver.skippableArray(config, forType.getRawClass())) {
            return AnnotatedClassResolver.createArrayType(config, forType.getRawClass());
        }
        return new AnnotatedClassResolver(config, forType, r).resolveFully();
    }

    public static AnnotatedClass resolveWithoutSuperTypes(MapperConfig<?> config, Class<?> forType) {
        return AnnotatedClassResolver.resolveWithoutSuperTypes(config, forType, config);
    }

    public static AnnotatedClass resolveWithoutSuperTypes(MapperConfig<?> config, JavaType forType, ClassIntrospector.MixInResolver r) {
        if (forType.isArrayType() && AnnotatedClassResolver.skippableArray(config, forType.getRawClass())) {
            return AnnotatedClassResolver.createArrayType(config, forType.getRawClass());
        }
        return new AnnotatedClassResolver(config, forType, r).resolveWithoutSuperTypes();
    }

    public static AnnotatedClass resolveWithoutSuperTypes(MapperConfig<?> config, Class<?> forType, ClassIntrospector.MixInResolver r) {
        if (forType.isArray() && AnnotatedClassResolver.skippableArray(config, forType)) {
            return AnnotatedClassResolver.createArrayType(config, forType);
        }
        return new AnnotatedClassResolver(config, forType, r).resolveWithoutSuperTypes();
    }

    private static boolean skippableArray(MapperConfig<?> config, Class<?> type) {
        return config == null || config.findMixInClassFor(type) == null;
    }

    static AnnotatedClass createPrimordial(Class<?> raw) {
        return new AnnotatedClass(raw);
    }

    static AnnotatedClass createArrayType(MapperConfig<?> config, Class<?> raw) {
        return new AnnotatedClass(raw);
    }

    AnnotatedClass resolveFully() {
        ArrayList<JavaType> superTypes = new ArrayList<JavaType>(8);
        if (!this._type.hasRawClass(Object.class)) {
            if (this._type.isInterface()) {
                AnnotatedClassResolver._addSuperInterfaces(this._type, superTypes, false);
            } else {
                AnnotatedClassResolver._addSuperTypes(this._type, superTypes, false);
            }
        }
        return new AnnotatedClass(this._type, this._class, superTypes, this._primaryMixin, this.resolveClassAnnotations(superTypes), this._bindings, this._intr, this._mixInResolver, this._config.getTypeFactory(), this._collectAnnotations);
    }

    AnnotatedClass resolveWithoutSuperTypes() {
        List<JavaType> superTypes = Collections.emptyList();
        return new AnnotatedClass(null, this._class, superTypes, this._primaryMixin, this.resolveClassAnnotations(superTypes), this._bindings, this._intr, this._mixInResolver, this._config.getTypeFactory(), this._collectAnnotations);
    }

    private static void _addSuperTypes(JavaType type, List<JavaType> result, boolean addClassItself) {
        Class<?> cls = type.getRawClass();
        if (cls == CLS_OBJECT || cls == CLS_ENUM) {
            return;
        }
        if (addClassItself) {
            if (AnnotatedClassResolver._contains(result, cls)) {
                return;
            }
            result.add(type);
        }
        for (JavaType intCls : type.getInterfaces()) {
            AnnotatedClassResolver._addSuperInterfaces(intCls, result, true);
        }
        JavaType superType = type.getSuperClass();
        if (superType != null) {
            AnnotatedClassResolver._addSuperTypes(superType, result, true);
        }
    }

    private static void _addSuperInterfaces(JavaType type, List<JavaType> result, boolean addClassItself) {
        Class<?> cls = type.getRawClass();
        if (addClassItself) {
            if (AnnotatedClassResolver._contains(result, cls)) {
                return;
            }
            result.add(type);
            if (cls == CLS_LIST || cls == CLS_MAP) {
                return;
            }
        }
        for (JavaType intCls : type.getInterfaces()) {
            AnnotatedClassResolver._addSuperInterfaces(intCls, result, true);
        }
    }

    private static boolean _contains(List<JavaType> found, Class<?> raw) {
        int end = found.size();
        for (int i = 0; i < end; ++i) {
            if (found.get(i).getRawClass() != raw) continue;
            return true;
        }
        return false;
    }

    private Annotations resolveClassAnnotations(List<JavaType> superTypes) {
        boolean checkMixIns;
        if (this._intr == null) {
            return NO_ANNOTATIONS;
        }
        boolean bl = checkMixIns = this._mixInResolver != null && (!(this._mixInResolver instanceof SimpleMixInResolver) || ((SimpleMixInResolver)this._mixInResolver).hasMixIns());
        if (!checkMixIns && !this._collectAnnotations) {
            return NO_ANNOTATIONS;
        }
        AnnotationCollector resolvedCA = AnnotationCollector.emptyCollector();
        if (this._primaryMixin != null) {
            resolvedCA = this._addClassMixIns(resolvedCA, this._class, this._primaryMixin);
        }
        if (this._collectAnnotations) {
            resolvedCA = this._addAnnotationsIfNotPresent(resolvedCA, ClassUtil.findClassAnnotations(this._class));
        }
        for (JavaType type : superTypes) {
            if (checkMixIns) {
                Class<?> cls = type.getRawClass();
                resolvedCA = this._addClassMixIns(resolvedCA, cls, this._mixInResolver.findMixInClassFor(cls));
            }
            if (!this._collectAnnotations) continue;
            resolvedCA = this._addAnnotationsIfNotPresent(resolvedCA, ClassUtil.findClassAnnotations(type.getRawClass()));
        }
        if (checkMixIns) {
            resolvedCA = this._addClassMixIns(resolvedCA, Object.class, this._mixInResolver.findMixInClassFor(Object.class));
        }
        return resolvedCA.asAnnotations();
    }

    private AnnotationCollector _addClassMixIns(AnnotationCollector annotations, Class<?> target, Class<?> mixin) {
        if (mixin != null) {
            annotations = this._addAnnotationsIfNotPresent(annotations, ClassUtil.findClassAnnotations(mixin));
            for (Class<?> parent : ClassUtil.findSuperClasses(mixin, target, false)) {
                annotations = this._addAnnotationsIfNotPresent(annotations, ClassUtil.findClassAnnotations(parent));
            }
        }
        return annotations;
    }

    private AnnotationCollector _addAnnotationsIfNotPresent(AnnotationCollector c, Annotation[] anns) {
        if (anns != null) {
            for (Annotation ann : anns) {
                if (c.isPresent(ann)) continue;
                c = c.addOrOverride(ann);
                if (!this._intr.isAnnotationBundle(ann)) continue;
                c = this._addFromBundleIfNotPresent(c, ann);
            }
        }
        return c;
    }

    private AnnotationCollector _addFromBundleIfNotPresent(AnnotationCollector c, Annotation bundle) {
        for (Annotation ann : ClassUtil.findClassAnnotations(bundle.annotationType())) {
            if (ann instanceof Target || ann instanceof Retention || c.isPresent(ann)) continue;
            c = c.addOrOverride(ann);
            if (!this._intr.isAnnotationBundle(ann)) continue;
            c = this._addFromBundleIfNotPresent(c, ann);
        }
        return c;
    }
}

