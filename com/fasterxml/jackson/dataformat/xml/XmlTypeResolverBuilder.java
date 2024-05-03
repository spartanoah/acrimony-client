/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.IOException;
import java.util.Collection;

public class XmlTypeResolverBuilder
extends StdTypeResolverBuilder {
    @Override
    public StdTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idRes) {
        super.init(idType, idRes);
        if (this._typeProperty != null) {
            this._typeProperty = StaxUtil.sanitizeXmlTypeName(this._typeProperty);
        }
        return this;
    }

    @Override
    public StdTypeResolverBuilder typeProperty(String typeIdPropName) {
        if (typeIdPropName == null || typeIdPropName.length() == 0) {
            typeIdPropName = this._idType.getDefaultPropertyName();
        }
        this._typeProperty = StaxUtil.sanitizeXmlTypeName(typeIdPropName);
        return this;
    }

    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType, PolymorphicTypeValidator subtypeValidator, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
        if (this._customIdResolver != null) {
            return this._customIdResolver;
        }
        switch (this._idType) {
            case CLASS: {
                return new XmlClassNameIdResolver(baseType, config.getTypeFactory(), this.subTypeValidator(config));
            }
            case MINIMAL_CLASS: {
                return new XmlMinimalClassNameIdResolver(baseType, config.getTypeFactory(), this.subTypeValidator(config));
            }
        }
        return super.idResolver(config, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }

    protected static String encodeXmlClassName(String className) {
        int ix = className.lastIndexOf(36);
        if (ix >= 0) {
            StringBuilder sb = new StringBuilder(className);
            do {
                sb.replace(ix, ix + 1, "..");
            } while ((ix = className.lastIndexOf(36, ix - 1)) >= 0);
            className = sb.toString();
        }
        return className;
    }

    protected static String decodeXmlClassName(String className) {
        int ix = className.lastIndexOf("..");
        if (ix >= 0) {
            StringBuilder sb = new StringBuilder(className);
            do {
                sb.replace(ix, ix + 2, "$");
            } while ((ix = className.lastIndexOf("..", ix - 1)) >= 0);
            className = sb.toString();
        }
        return className;
    }

    protected static class XmlMinimalClassNameIdResolver
    extends MinimalClassNameIdResolver {
        public XmlMinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory, PolymorphicTypeValidator ptv) {
            super(baseType, typeFactory, ptv);
        }

        @Override
        public String idFromValue(Object value) {
            return XmlTypeResolverBuilder.encodeXmlClassName(super.idFromValue(value));
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            return super.typeFromId(context, XmlTypeResolverBuilder.decodeXmlClassName(id));
        }
    }

    protected static class XmlClassNameIdResolver
    extends ClassNameIdResolver {
        public XmlClassNameIdResolver(JavaType baseType, TypeFactory typeFactory, PolymorphicTypeValidator ptv) {
            super(baseType, typeFactory, ptv);
        }

        @Override
        public String idFromValue(Object value) {
            return XmlTypeResolverBuilder.encodeXmlClassName(super.idFromValue(value));
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            return super.typeFromId(context, XmlTypeResolverBuilder.decodeXmlClassName(id));
        }
    }
}

