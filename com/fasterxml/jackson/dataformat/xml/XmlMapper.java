/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.cfg.CoercionAction
 *  com.fasterxml.jackson.databind.cfg.CoercionInputShape
 *  com.fasterxml.jackson.databind.type.LogicalType
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.dataformat.xml.DefaultingXmlTypeResolverBuilder;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.deser.XmlDeserializationContext;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class XmlMapper
extends ObjectMapper {
    private static final long serialVersionUID = 1L;
    protected static final JacksonXmlModule DEFAULT_XML_MODULE = new JacksonXmlModule();
    protected static final DefaultXmlPrettyPrinter DEFAULT_XML_PRETTY_PRINTER = new DefaultXmlPrettyPrinter();
    protected final JacksonXmlModule _xmlModule;

    public XmlMapper() {
        this(new XmlFactory());
    }

    public XmlMapper(XMLInputFactory inputF, XMLOutputFactory outF) {
        this(new XmlFactory(inputF, outF));
    }

    public XmlMapper(XMLInputFactory inputF) {
        this(new XmlFactory(inputF));
    }

    public XmlMapper(XmlFactory xmlFactory) {
        this(xmlFactory, DEFAULT_XML_MODULE);
    }

    public XmlMapper(JacksonXmlModule module) {
        this(new XmlFactory(), module);
    }

    public XmlMapper(XmlFactory xmlFactory, JacksonXmlModule module) {
        super(xmlFactory, new XmlSerializerProvider(new XmlRootNameLookup()), new XmlDeserializationContext(BeanDeserializerFactory.instance));
        this._xmlModule = module;
        if (module != null) {
            this.registerModule(module);
        }
        this._serializationConfig = this._serializationConfig.withDefaultPrettyPrinter(DEFAULT_XML_PRETTY_PRINTER);
        this.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        this.setBase64Variant(Base64Variants.MIME);
        this.coercionConfigDefaults().setAcceptBlankAsEmpty(Boolean.TRUE).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
        this.coercionConfigFor(LogicalType.Integer).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        this.coercionConfigFor(LogicalType.Float).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        this.coercionConfigFor(LogicalType.Boolean).setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }

    protected XmlMapper(XmlMapper src) {
        super(src);
        this._xmlModule = src._xmlModule;
    }

    @Override
    public XmlMapper copy() {
        this._checkInvalidCopy(XmlMapper.class);
        return new XmlMapper(this);
    }

    public static Builder xmlBuilder() {
        return new Builder(new XmlMapper());
    }

    public static Builder builder() {
        return new Builder(new XmlMapper());
    }

    public static Builder builder(XmlFactory streamFactory) {
        return new Builder(new XmlMapper(streamFactory));
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    protected TypeResolverBuilder<?> _constructDefaultTypeResolverBuilder(ObjectMapper.DefaultTyping applicability, PolymorphicTypeValidator ptv) {
        return new DefaultingXmlTypeResolverBuilder(applicability, ptv);
    }

    @Deprecated
    protected void setXMLTextElementName(String name) {
        ((XmlFactory)this._jsonFactory).setXMLTextElementName(name);
    }

    @Deprecated
    public XmlMapper setDefaultUseWrapper(boolean state) {
        AnnotationIntrospector ai0 = this.getDeserializationConfig().getAnnotationIntrospector();
        for (AnnotationIntrospector ai : ai0.allIntrospectors()) {
            if (!(ai instanceof JacksonXmlAnnotationIntrospector)) continue;
            ((JacksonXmlAnnotationIntrospector)ai).setDefaultUseWrapper(state);
        }
        return this;
    }

    @Override
    public XmlFactory getFactory() {
        return (XmlFactory)this._jsonFactory;
    }

    public ObjectMapper configure(ToXmlGenerator.Feature f, boolean state) {
        ((XmlFactory)this._jsonFactory).configure(f, state);
        return this;
    }

    public ObjectMapper configure(FromXmlParser.Feature f, boolean state) {
        ((XmlFactory)this._jsonFactory).configure(f, state);
        return this;
    }

    public ObjectMapper enable(ToXmlGenerator.Feature f) {
        ((XmlFactory)this._jsonFactory).enable(f);
        return this;
    }

    public ObjectMapper enable(FromXmlParser.Feature f) {
        ((XmlFactory)this._jsonFactory).enable(f);
        return this;
    }

    public ObjectMapper disable(ToXmlGenerator.Feature f) {
        ((XmlFactory)this._jsonFactory).disable(f);
        return this;
    }

    public ObjectMapper disable(FromXmlParser.Feature f) {
        ((XmlFactory)this._jsonFactory).disable(f);
        return this;
    }

    public <T> T readValue(XMLStreamReader r, Class<T> valueType) throws IOException {
        return this.readValue(r, this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(XMLStreamReader r, TypeReference<T> valueTypeRef) throws IOException {
        return this.readValue(r, this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(XMLStreamReader r, JavaType valueType) throws IOException {
        FromXmlParser p = this.getFactory().createParser(r);
        return super.readValue((JsonParser)p, valueType);
    }

    public void writeValue(XMLStreamWriter w0, Object value) throws IOException {
        ToXmlGenerator g = this.getFactory().createGenerator(w0);
        super.writeValue(g, value);
    }

    public static class Builder
    extends MapperBuilder<XmlMapper, Builder> {
        public Builder(XmlMapper m) {
            super(m);
        }

        public Builder enable(FromXmlParser.Feature ... features) {
            for (FromXmlParser.Feature f : features) {
                ((XmlMapper)this._mapper).enable(f);
            }
            return this;
        }

        public Builder disable(FromXmlParser.Feature ... features) {
            for (FromXmlParser.Feature f : features) {
                ((XmlMapper)this._mapper).disable(f);
            }
            return this;
        }

        public Builder configure(FromXmlParser.Feature feature, boolean state) {
            if (state) {
                ((XmlMapper)this._mapper).enable(feature);
            } else {
                ((XmlMapper)this._mapper).disable(feature);
            }
            return this;
        }

        public Builder enable(ToXmlGenerator.Feature ... features) {
            for (ToXmlGenerator.Feature f : features) {
                ((XmlMapper)this._mapper).enable(f);
            }
            return this;
        }

        public Builder disable(ToXmlGenerator.Feature ... features) {
            for (ToXmlGenerator.Feature f : features) {
                ((XmlMapper)this._mapper).disable(f);
            }
            return this;
        }

        public Builder configure(ToXmlGenerator.Feature feature, boolean state) {
            if (state) {
                ((XmlMapper)this._mapper).enable(feature);
            } else {
                ((XmlMapper)this._mapper).disable(feature);
            }
            return this;
        }

        public Builder nameForTextElement(String name) {
            ((XmlMapper)this._mapper).setXMLTextElementName(name);
            return this;
        }

        public Builder defaultUseWrapper(boolean state) {
            ((XmlMapper)this._mapper).setDefaultUseWrapper(state);
            return this;
        }
    }
}

