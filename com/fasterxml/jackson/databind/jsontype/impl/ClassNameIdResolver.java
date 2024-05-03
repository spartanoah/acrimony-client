/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;

public class ClassNameIdResolver
extends TypeIdResolverBase {
    private static final String JAVA_UTIL_PKG = "java.util.";
    protected final PolymorphicTypeValidator _subTypeValidator;

    @Deprecated
    protected ClassNameIdResolver(JavaType baseType, TypeFactory typeFactory) {
        this(baseType, typeFactory, LaissezFaireSubTypeValidator.instance);
    }

    public ClassNameIdResolver(JavaType baseType, TypeFactory typeFactory, PolymorphicTypeValidator ptv) {
        super(baseType, typeFactory);
        this._subTypeValidator = ptv;
    }

    public static ClassNameIdResolver construct(JavaType baseType, MapperConfig<?> config, PolymorphicTypeValidator ptv) {
        return new ClassNameIdResolver(baseType, config.getTypeFactory(), ptv);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CLASS;
    }

    public void registerSubtype(Class<?> type, String name) {
    }

    @Override
    public String idFromValue(Object value) {
        return this._idFrom(value, value.getClass(), this._typeFactory);
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> type) {
        return this._idFrom(value, type, this._typeFactory);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {
        return this._typeFromId(id, context);
    }

    protected JavaType _typeFromId(String id, DatabindContext ctxt) throws IOException {
        JavaType t = ctxt.resolveAndValidateSubType(this._baseType, id, this._subTypeValidator);
        if (t == null && ctxt instanceof DeserializationContext) {
            return ((DeserializationContext)ctxt).handleUnknownTypeId(this._baseType, id, this, "no such class found");
        }
        return t;
    }

    protected String _idFrom(Object value, Class<?> cls, TypeFactory typeFactory) {
        Class<?> staticType;
        Class<?> outer;
        String str;
        if (ClassUtil.isEnumType(cls) && !cls.isEnum()) {
            cls = cls.getSuperclass();
        }
        if ((str = cls.getName()).startsWith(JAVA_UTIL_PKG)) {
            if (value instanceof EnumSet) {
                Class<? extends Enum<?>> enumClass = ClassUtil.findEnumType((EnumSet)value);
                str = typeFactory.constructCollectionType(EnumSet.class, enumClass).toCanonical();
            } else if (value instanceof EnumMap) {
                Class<? extends Enum<?>> enumClass = ClassUtil.findEnumType((EnumMap)value);
                Class<Object> valueClass = Object.class;
                str = typeFactory.constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
            }
        } else if (str.indexOf(36) >= 0 && (outer = ClassUtil.getOuterClass(cls)) != null && ClassUtil.getOuterClass(staticType = this._baseType.getRawClass()) == null) {
            cls = this._baseType.getRawClass();
            str = cls.getName();
        }
        return str;
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "class name used as type id";
    }
}

