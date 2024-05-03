/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public abstract class MapperConfig<T extends MapperConfig<T>>
implements ClassIntrospector.MixInResolver,
Serializable {
    private static final long serialVersionUID = 2L;
    protected static final JsonInclude.Value EMPTY_INCLUDE = JsonInclude.Value.empty();
    protected static final JsonFormat.Value EMPTY_FORMAT = JsonFormat.Value.empty();
    protected final int _mapperFeatures;
    protected final BaseSettings _base;

    protected MapperConfig(BaseSettings base, int mapperFeatures) {
        this._base = base;
        this._mapperFeatures = mapperFeatures;
    }

    protected MapperConfig(MapperConfig<T> src, int mapperFeatures) {
        this._base = src._base;
        this._mapperFeatures = mapperFeatures;
    }

    protected MapperConfig(MapperConfig<T> src, BaseSettings base) {
        this._base = base;
        this._mapperFeatures = src._mapperFeatures;
    }

    protected MapperConfig(MapperConfig<T> src) {
        this._base = src._base;
        this._mapperFeatures = src._mapperFeatures;
    }

    public static <F extends Enum<F>> int collectFeatureDefaults(Class<F> enumClass) {
        int flags = 0;
        for (Enum value : (Enum[])enumClass.getEnumConstants()) {
            if (!((ConfigFeature)((Object)value)).enabledByDefault()) continue;
            flags |= ((ConfigFeature)((Object)value)).getMask();
        }
        return flags;
    }

    public abstract T with(MapperFeature ... var1);

    public abstract T without(MapperFeature ... var1);

    public abstract T with(MapperFeature var1, boolean var2);

    public final boolean isEnabled(MapperFeature f) {
        return f.enabledIn(this._mapperFeatures);
    }

    public final boolean hasMapperFeatures(int featureMask) {
        return (this._mapperFeatures & featureMask) == featureMask;
    }

    public final boolean isAnnotationProcessingEnabled() {
        return this.isEnabled(MapperFeature.USE_ANNOTATIONS);
    }

    public final boolean canOverrideAccessModifiers() {
        return this.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
    }

    public final boolean shouldSortPropertiesAlphabetically() {
        return this.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    }

    public abstract boolean useRootWrapping();

    public SerializableString compileString(String src) {
        return new SerializedString(src);
    }

    public ClassIntrospector getClassIntrospector() {
        return this._base.getClassIntrospector();
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        if (this.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            return this._base.getAnnotationIntrospector();
        }
        return NopAnnotationIntrospector.instance;
    }

    public final PropertyNamingStrategy getPropertyNamingStrategy() {
        return this._base.getPropertyNamingStrategy();
    }

    public final HandlerInstantiator getHandlerInstantiator() {
        return this._base.getHandlerInstantiator();
    }

    public final TypeResolverBuilder<?> getDefaultTyper(JavaType baseType) {
        return this._base.getTypeResolverBuilder();
    }

    public abstract SubtypeResolver getSubtypeResolver();

    public PolymorphicTypeValidator getPolymorphicTypeValidator() {
        PolymorphicTypeValidator ptv = this._base.getPolymorphicTypeValidator();
        if (ptv == LaissezFaireSubTypeValidator.instance && this.isEnabled(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)) {
            ptv = new DefaultBaseTypeLimitingValidator();
        }
        return ptv;
    }

    public final TypeFactory getTypeFactory() {
        return this._base.getTypeFactory();
    }

    public final JavaType constructType(Class<?> cls) {
        return this.getTypeFactory().constructType(cls);
    }

    public final JavaType constructType(TypeReference<?> valueTypeRef) {
        return this.getTypeFactory().constructType(valueTypeRef.getType());
    }

    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
        return this.getTypeFactory().constructSpecializedType(baseType, subclass, true);
    }

    public BeanDescription introspectClassAnnotations(Class<?> cls) {
        return this.introspectClassAnnotations(this.constructType(cls));
    }

    public BeanDescription introspectClassAnnotations(JavaType type) {
        return this.getClassIntrospector().forClassAnnotations(this, type, this);
    }

    public BeanDescription introspectDirectClassAnnotations(Class<?> cls) {
        return this.introspectDirectClassAnnotations(this.constructType(cls));
    }

    public final BeanDescription introspectDirectClassAnnotations(JavaType type) {
        return this.getClassIntrospector().forDirectClassAnnotations(this, type, this);
    }

    public abstract ConfigOverride findConfigOverride(Class<?> var1);

    public abstract ConfigOverride getConfigOverride(Class<?> var1);

    public abstract JsonInclude.Value getDefaultPropertyInclusion();

    public abstract JsonInclude.Value getDefaultPropertyInclusion(Class<?> var1);

    public JsonInclude.Value getDefaultPropertyInclusion(Class<?> baseType, JsonInclude.Value defaultIncl) {
        JsonInclude.Value v = this.getConfigOverride(baseType).getInclude();
        if (v != null) {
            return v;
        }
        return defaultIncl;
    }

    public abstract JsonInclude.Value getDefaultInclusion(Class<?> var1, Class<?> var2);

    public JsonInclude.Value getDefaultInclusion(Class<?> baseType, Class<?> propertyType, JsonInclude.Value defaultIncl) {
        JsonInclude.Value baseOverride = this.getConfigOverride(baseType).getInclude();
        JsonInclude.Value propOverride = this.getConfigOverride(propertyType).getIncludeAsProperty();
        JsonInclude.Value result = JsonInclude.Value.mergeAll(defaultIncl, baseOverride, propOverride);
        return result;
    }

    public abstract JsonFormat.Value getDefaultPropertyFormat(Class<?> var1);

    public abstract JsonIgnoreProperties.Value getDefaultPropertyIgnorals(Class<?> var1);

    public abstract JsonIgnoreProperties.Value getDefaultPropertyIgnorals(Class<?> var1, AnnotatedClass var2);

    public abstract VisibilityChecker<?> getDefaultVisibilityChecker();

    public abstract VisibilityChecker<?> getDefaultVisibilityChecker(Class<?> var1, AnnotatedClass var2);

    public abstract JsonSetter.Value getDefaultSetterInfo();

    public abstract Boolean getDefaultMergeable();

    public abstract Boolean getDefaultMergeable(Class<?> var1);

    public final DateFormat getDateFormat() {
        return this._base.getDateFormat();
    }

    public final Locale getLocale() {
        return this._base.getLocale();
    }

    public final TimeZone getTimeZone() {
        return this._base.getTimeZone();
    }

    public abstract Class<?> getActiveView();

    public Base64Variant getBase64Variant() {
        return this._base.getBase64Variant();
    }

    public abstract ContextAttributes getAttributes();

    public abstract PropertyName findRootName(JavaType var1);

    public abstract PropertyName findRootName(Class<?> var1);

    public TypeResolverBuilder<?> typeResolverBuilderInstance(Annotated annotated, Class<? extends TypeResolverBuilder<?>> builderClass) {
        TypeResolverBuilder<?> builder;
        HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null && (builder = hi.typeResolverBuilderInstance(this, annotated, builderClass)) != null) {
            return builder;
        }
        return ClassUtil.createInstance(builderClass, this.canOverrideAccessModifiers());
    }

    public TypeIdResolver typeIdResolverInstance(Annotated annotated, Class<? extends TypeIdResolver> resolverClass) {
        TypeIdResolver builder;
        HandlerInstantiator hi = this.getHandlerInstantiator();
        if (hi != null && (builder = hi.typeIdResolverInstance(this, annotated, resolverClass)) != null) {
            return builder;
        }
        return ClassUtil.createInstance(resolverClass, this.canOverrideAccessModifiers());
    }
}

