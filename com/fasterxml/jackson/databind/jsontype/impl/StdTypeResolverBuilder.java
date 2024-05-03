/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsExistingPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsExternalTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsExternalTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.util.Collection;

public class StdTypeResolverBuilder
implements TypeResolverBuilder<StdTypeResolverBuilder> {
    protected JsonTypeInfo.Id _idType;
    protected JsonTypeInfo.As _includeAs;
    protected String _typeProperty;
    protected boolean _typeIdVisible = false;
    protected Class<?> _defaultImpl;
    protected TypeIdResolver _customIdResolver;

    public StdTypeResolverBuilder() {
    }

    protected StdTypeResolverBuilder(JsonTypeInfo.Id idType, JsonTypeInfo.As idAs, String propName) {
        this._idType = idType;
        this._includeAs = idAs;
        this._typeProperty = propName;
    }

    public static StdTypeResolverBuilder noTypeInfoBuilder() {
        return new StdTypeResolverBuilder().init(JsonTypeInfo.Id.NONE, null);
    }

    @Override
    public StdTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idRes) {
        if (idType == null) {
            throw new IllegalArgumentException("idType cannot be null");
        }
        this._idType = idType;
        this._customIdResolver = idRes;
        this._typeProperty = idType.getDefaultPropertyName();
        return this;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        if (this._idType == JsonTypeInfo.Id.NONE) {
            return null;
        }
        if (baseType.isPrimitive() && !this.allowPrimitiveTypes(config, baseType)) {
            return null;
        }
        TypeIdResolver idRes = this.idResolver(config, baseType, this.subTypeValidator(config), subtypes, true, false);
        switch (this._includeAs) {
            case WRAPPER_ARRAY: {
                return new AsArrayTypeSerializer(idRes, null);
            }
            case PROPERTY: {
                return new AsPropertyTypeSerializer(idRes, null, this._typeProperty);
            }
            case WRAPPER_OBJECT: {
                return new AsWrapperTypeSerializer(idRes, null);
            }
            case EXTERNAL_PROPERTY: {
                return new AsExternalTypeSerializer(idRes, null, this._typeProperty);
            }
            case EXISTING_PROPERTY: {
                return new AsExistingPropertyTypeSerializer(idRes, null, this._typeProperty);
            }
        }
        throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + (Object)((Object)this._includeAs));
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        if (this._idType == JsonTypeInfo.Id.NONE) {
            return null;
        }
        if (baseType.isPrimitive() && !this.allowPrimitiveTypes(config, baseType)) {
            return null;
        }
        PolymorphicTypeValidator subTypeValidator = this.verifyBaseTypeValidity(config, baseType);
        TypeIdResolver idRes = this.idResolver(config, baseType, subTypeValidator, subtypes, false, true);
        JavaType defaultImpl = this.defineDefaultImpl(config, baseType);
        switch (this._includeAs) {
            case WRAPPER_ARRAY: {
                return new AsArrayTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
            }
            case PROPERTY: 
            case EXISTING_PROPERTY: {
                return new AsPropertyTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl, this._includeAs);
            }
            case WRAPPER_OBJECT: {
                return new AsWrapperTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
            }
            case EXTERNAL_PROPERTY: {
                return new AsExternalTypeDeserializer(baseType, idRes, this._typeProperty, this._typeIdVisible, defaultImpl);
            }
        }
        throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + (Object)((Object)this._includeAs));
    }

    protected JavaType defineDefaultImpl(DeserializationConfig config, JavaType baseType) {
        JavaType defaultImpl = this._defaultImpl == null ? (config.isEnabled(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL) && !baseType.isAbstract() ? baseType : null) : (this._defaultImpl == Void.class || this._defaultImpl == NoClass.class ? config.getTypeFactory().constructType(this._defaultImpl) : (baseType.hasRawClass(this._defaultImpl) ? baseType : (baseType.isTypeOrSuperTypeOf(this._defaultImpl) ? config.getTypeFactory().constructSpecializedType(baseType, this._defaultImpl) : null)));
        return defaultImpl;
    }

    @Override
    public StdTypeResolverBuilder inclusion(JsonTypeInfo.As includeAs) {
        if (includeAs == null) {
            throw new IllegalArgumentException("includeAs cannot be null");
        }
        this._includeAs = includeAs;
        return this;
    }

    @Override
    public StdTypeResolverBuilder typeProperty(String typeIdPropName) {
        if (typeIdPropName == null || typeIdPropName.length() == 0) {
            typeIdPropName = this._idType.getDefaultPropertyName();
        }
        this._typeProperty = typeIdPropName;
        return this;
    }

    @Override
    public StdTypeResolverBuilder defaultImpl(Class<?> defaultImpl) {
        this._defaultImpl = defaultImpl;
        return this;
    }

    @Override
    public StdTypeResolverBuilder typeIdVisibility(boolean isVisible) {
        this._typeIdVisible = isVisible;
        return this;
    }

    @Override
    public Class<?> getDefaultImpl() {
        return this._defaultImpl;
    }

    public String getTypeProperty() {
        return this._typeProperty;
    }

    public boolean isTypeIdVisible() {
        return this._typeIdVisible;
    }

    protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType, PolymorphicTypeValidator subtypeValidator, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
        if (this._customIdResolver != null) {
            return this._customIdResolver;
        }
        if (this._idType == null) {
            throw new IllegalStateException("Cannot build, 'init()' not yet called");
        }
        switch (this._idType) {
            case CLASS: {
                return ClassNameIdResolver.construct(baseType, config, subtypeValidator);
            }
            case MINIMAL_CLASS: {
                return MinimalClassNameIdResolver.construct(baseType, config, subtypeValidator);
            }
            case NAME: {
                return TypeNameIdResolver.construct(config, baseType, subtypes, forSer, forDeser);
            }
            case NONE: {
                return null;
            }
        }
        throw new IllegalStateException("Do not know how to construct standard type id resolver for idType: " + (Object)((Object)this._idType));
    }

    public PolymorphicTypeValidator subTypeValidator(MapperConfig<?> config) {
        return config.getPolymorphicTypeValidator();
    }

    protected PolymorphicTypeValidator verifyBaseTypeValidity(MapperConfig<?> config, JavaType baseType) {
        PolymorphicTypeValidator ptv = this.subTypeValidator(config);
        if (this._idType == JsonTypeInfo.Id.CLASS || this._idType == JsonTypeInfo.Id.MINIMAL_CLASS) {
            PolymorphicTypeValidator.Validity validity = ptv.validateBaseType(config, baseType);
            if (validity == PolymorphicTypeValidator.Validity.DENIED) {
                return this.reportInvalidBaseType(config, baseType, ptv);
            }
            if (validity == PolymorphicTypeValidator.Validity.ALLOWED) {
                return LaissezFaireSubTypeValidator.instance;
            }
        }
        return ptv;
    }

    protected PolymorphicTypeValidator reportInvalidBaseType(MapperConfig<?> config, JavaType baseType, PolymorphicTypeValidator ptv) {
        throw new IllegalArgumentException(String.format("Configured `PolymorphicTypeValidator` (of type %s) denied resolution of all subtypes of base type %s", ClassUtil.classNameOf(ptv), ClassUtil.classNameOf(baseType.getRawClass())));
    }

    protected boolean allowPrimitiveTypes(MapperConfig<?> config, JavaType baseType) {
        return false;
    }
}

