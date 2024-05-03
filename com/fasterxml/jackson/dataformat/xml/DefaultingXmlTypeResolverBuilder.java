/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlTypeResolverBuilder;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.Serializable;
import java.util.Collection;

public class DefaultingXmlTypeResolverBuilder
extends ObjectMapper.DefaultTypeResolverBuilder
implements Serializable {
    private static final long serialVersionUID = 1L;

    public DefaultingXmlTypeResolverBuilder(ObjectMapper.DefaultTyping t, PolymorphicTypeValidator ptv) {
        super(t, ptv);
    }

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
                return new XmlTypeResolverBuilder.XmlClassNameIdResolver(baseType, config.getTypeFactory(), this.subTypeValidator(config));
            }
            case MINIMAL_CLASS: {
                return new XmlTypeResolverBuilder.XmlMinimalClassNameIdResolver(baseType, config.getTypeFactory(), this.subTypeValidator(config));
            }
        }
        return super.idResolver(config, baseType, subtypeValidator, subtypes, forSer, forDeser);
    }
}

