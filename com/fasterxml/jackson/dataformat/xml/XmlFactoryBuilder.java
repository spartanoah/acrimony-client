/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public class XmlFactoryBuilder
extends TSFBuilder<XmlFactory, XmlFactoryBuilder> {
    protected int _formatParserFeatures;
    protected int _formatGeneratorFeatures;
    protected XMLInputFactory _xmlInputFactory;
    protected XMLOutputFactory _xmlOutputFactory;
    protected String _nameForTextElement;
    protected ClassLoader _classLoaderForStax;

    protected XmlFactoryBuilder() {
        this._formatParserFeatures = XmlFactory.DEFAULT_XML_PARSER_FEATURE_FLAGS;
        this._formatGeneratorFeatures = XmlFactory.DEFAULT_XML_GENERATOR_FEATURE_FLAGS;
        this._classLoaderForStax = null;
    }

    public XmlFactoryBuilder(XmlFactory base) {
        super(base);
        this._formatParserFeatures = base._xmlParserFeatures;
        this._formatGeneratorFeatures = base._xmlGeneratorFeatures;
        this._xmlInputFactory = base._xmlInputFactory;
        this._xmlOutputFactory = base._xmlOutputFactory;
        this._nameForTextElement = base._cfgNameForTextElement;
        this._classLoaderForStax = null;
    }

    public int formatParserFeaturesMask() {
        return this._formatParserFeatures;
    }

    public int formatGeneratorFeaturesMask() {
        return this._formatGeneratorFeatures;
    }

    public String nameForTextElement() {
        return this._nameForTextElement;
    }

    public XMLInputFactory xmlInputFactory() {
        if (this._xmlInputFactory == null) {
            return this.defaultInputFactory();
        }
        return this._xmlInputFactory;
    }

    protected XMLInputFactory defaultInputFactory() {
        XMLInputFactory xmlIn = XMLInputFactory.newFactory(XMLInputFactory.class.getName(), this.staxClassLoader());
        xmlIn.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        xmlIn.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        return xmlIn;
    }

    public XMLOutputFactory xmlOutputFactory() {
        if (this._xmlOutputFactory == null) {
            return this.defaultOutputFactory();
        }
        return this._xmlOutputFactory;
    }

    protected XMLOutputFactory defaultOutputFactory() {
        XMLOutputFactory xmlOut = XMLOutputFactory.newFactory(XMLOutputFactory.class.getName(), this.staxClassLoader());
        xmlOut.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
        return xmlOut;
    }

    protected ClassLoader staxClassLoader() {
        return this._classLoaderForStax == null ? this.getClass().getClassLoader() : this._classLoaderForStax;
    }

    public XmlFactoryBuilder enable(FromXmlParser.Feature f) {
        this._formatParserFeatures |= f.getMask();
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder enable(FromXmlParser.Feature first, FromXmlParser.Feature ... other) {
        this._formatParserFeatures |= first.getMask();
        for (FromXmlParser.Feature f : other) {
            this._formatParserFeatures |= f.getMask();
        }
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder disable(FromXmlParser.Feature f) {
        this._formatParserFeatures &= ~f.getMask();
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder disable(FromXmlParser.Feature first, FromXmlParser.Feature ... other) {
        this._formatParserFeatures &= ~first.getMask();
        for (FromXmlParser.Feature f : other) {
            this._formatParserFeatures &= ~f.getMask();
        }
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder configure(FromXmlParser.Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public XmlFactoryBuilder enable(ToXmlGenerator.Feature f) {
        this._formatGeneratorFeatures |= f.getMask();
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder enable(ToXmlGenerator.Feature first, ToXmlGenerator.Feature ... other) {
        this._formatGeneratorFeatures |= first.getMask();
        for (ToXmlGenerator.Feature f : other) {
            this._formatGeneratorFeatures |= f.getMask();
        }
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder disable(ToXmlGenerator.Feature f) {
        this._formatGeneratorFeatures &= ~f.getMask();
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder disable(ToXmlGenerator.Feature first, ToXmlGenerator.Feature ... other) {
        this._formatGeneratorFeatures &= ~first.getMask();
        for (ToXmlGenerator.Feature f : other) {
            this._formatGeneratorFeatures &= ~f.getMask();
        }
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder configure(ToXmlGenerator.Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public XmlFactoryBuilder nameForTextElement(String name) {
        this._nameForTextElement = name;
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder xmlInputFactory(XMLInputFactory xmlIn) {
        this._xmlInputFactory = xmlIn;
        return (XmlFactoryBuilder)this._this();
    }

    public XmlFactoryBuilder xmlOutputFactory(XMLOutputFactory xmlOut) {
        this._xmlOutputFactory = xmlOut;
        return (XmlFactoryBuilder)this._this();
    }

    @Deprecated
    public XmlFactoryBuilder inputFactory(XMLInputFactory xmlIn) {
        return this.xmlInputFactory(xmlIn);
    }

    @Deprecated
    public XmlFactoryBuilder outputFactory(XMLOutputFactory xmlOut) {
        return this.xmlOutputFactory(xmlOut);
    }

    public XmlFactoryBuilder staxClassLoader(ClassLoader cl) {
        this._classLoaderForStax = cl;
        return (XmlFactoryBuilder)this._this();
    }

    @Override
    public XmlFactory build() {
        return new XmlFactory(this);
    }
}

