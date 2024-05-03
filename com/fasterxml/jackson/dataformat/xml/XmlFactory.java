/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.codehaus.stax2.XMLInputFactory2
 *  org.codehaus.stax2.io.Stax2ByteArraySource
 *  org.codehaus.stax2.io.Stax2CharArraySource
 */
package com.fasterxml.jackson.dataformat.xml;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.XmlFactoryBuilder;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.io.Stax2ByteArraySource;
import org.codehaus.stax2.io.Stax2CharArraySource;

public class XmlFactory
extends JsonFactory {
    private static final long serialVersionUID = 1L;
    public static final String FORMAT_NAME_XML = "XML";
    static final int DEFAULT_XML_PARSER_FEATURE_FLAGS = FromXmlParser.Feature.collectDefaults();
    static final int DEFAULT_XML_GENERATOR_FEATURE_FLAGS = ToXmlGenerator.Feature.collectDefaults();
    protected int _xmlParserFeatures;
    protected int _xmlGeneratorFeatures;
    protected transient XMLInputFactory _xmlInputFactory;
    protected transient XMLOutputFactory _xmlOutputFactory;
    protected String _cfgNameForTextElement;
    protected transient String _jdkXmlInFactory;
    protected transient String _jdkXmlOutFactory;
    private static final byte UTF8_BOM_1 = -17;
    private static final byte UTF8_BOM_2 = -69;
    private static final byte UTF8_BOM_3 = -65;
    private static final byte BYTE_x = 120;
    private static final byte BYTE_m = 109;
    private static final byte BYTE_l = 108;
    private static final byte BYTE_D = 68;
    private static final byte BYTE_LT = 60;
    private static final byte BYTE_QMARK = 63;
    private static final byte BYTE_EXCL = 33;
    private static final byte BYTE_HYPHEN = 45;

    public XmlFactory() {
        this(null, null, null);
    }

    public XmlFactory(ObjectCodec oc) {
        this(oc, null, null);
    }

    public XmlFactory(XMLInputFactory xmlIn) {
        this(null, xmlIn, null);
    }

    public XmlFactory(XMLInputFactory xmlIn, XMLOutputFactory xmlOut) {
        this(null, xmlIn, xmlOut);
    }

    public XmlFactory(ObjectCodec oc, XMLInputFactory xmlIn, XMLOutputFactory xmlOut) {
        this(oc, DEFAULT_XML_PARSER_FEATURE_FLAGS, DEFAULT_XML_GENERATOR_FEATURE_FLAGS, xmlIn, xmlOut, null);
    }

    protected XmlFactory(ObjectCodec oc, int xpFeatures, int xgFeatures, XMLInputFactory xmlIn, XMLOutputFactory xmlOut, String nameForTextElem) {
        super(oc);
        this._xmlParserFeatures = xpFeatures;
        this._xmlGeneratorFeatures = xgFeatures;
        this._cfgNameForTextElement = nameForTextElem;
        if (xmlIn == null) {
            xmlIn = XMLInputFactory.newFactory(XMLInputFactory.class.getName(), this.getClass().getClassLoader());
            xmlIn.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
            xmlIn.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        }
        if (xmlOut == null) {
            xmlOut = XMLOutputFactory.newFactory(XMLOutputFactory.class.getName(), this.getClass().getClassLoader());
        }
        this._initFactories(xmlIn, xmlOut);
        this._xmlInputFactory = xmlIn;
        this._xmlOutputFactory = xmlOut;
    }

    protected XmlFactory(XmlFactory src, ObjectCodec oc) {
        super(src, oc);
        this._xmlParserFeatures = src._xmlParserFeatures;
        this._xmlGeneratorFeatures = src._xmlGeneratorFeatures;
        this._cfgNameForTextElement = src._cfgNameForTextElement;
        this._xmlInputFactory = src._xmlInputFactory;
        this._xmlOutputFactory = src._xmlOutputFactory;
    }

    protected XmlFactory(XmlFactoryBuilder b) {
        super(b, false);
        this._xmlParserFeatures = b.formatParserFeaturesMask();
        this._xmlGeneratorFeatures = b.formatGeneratorFeaturesMask();
        this._cfgNameForTextElement = b.nameForTextElement();
        this._xmlInputFactory = b.xmlInputFactory();
        this._xmlOutputFactory = b.xmlOutputFactory();
        this._initFactories(this._xmlInputFactory, this._xmlOutputFactory);
    }

    public static XmlFactoryBuilder builder() {
        return new XmlFactoryBuilder();
    }

    public XmlFactoryBuilder rebuild() {
        return new XmlFactoryBuilder(this);
    }

    protected void _initFactories(XMLInputFactory xmlIn, XMLOutputFactory xmlOut) {
        xmlOut.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
        xmlIn.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
    }

    @Override
    public XmlFactory copy() {
        this._checkInvalidCopy(XmlFactory.class);
        return new XmlFactory(this, null);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    protected Object readResolve() {
        XMLOutputFactory outf;
        XMLInputFactory inf;
        if (this._jdkXmlInFactory == null) {
            throw new IllegalStateException("No XMLInputFactory class name read during JDK deserialization");
        }
        if (this._jdkXmlOutFactory == null) {
            throw new IllegalStateException("No XMLOutputFactory class name read during JDK deserialization");
        }
        try {
            inf = (XMLInputFactory)Class.forName(this._jdkXmlInFactory).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            outf = (XMLOutputFactory)Class.forName(this._jdkXmlOutFactory).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return new XmlFactory(this._objectCodec, this._xmlParserFeatures, this._xmlGeneratorFeatures, inf, outf, this._cfgNameForTextElement);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this._jdkXmlInFactory = in.readUTF();
        this._jdkXmlOutFactory = in.readUTF();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(this._xmlInputFactory.getClass().getName());
        out.writeUTF(this._xmlOutputFactory.getClass().getName());
    }

    public void setXMLTextElementName(String name) {
        this._cfgNameForTextElement = name;
    }

    public String getXMLTextElementName() {
        return this._cfgNameForTextElement;
    }

    public final XmlFactory configure(FromXmlParser.Feature f, boolean state) {
        if (state) {
            this.enable(f);
        } else {
            this.disable(f);
        }
        return this;
    }

    public XmlFactory enable(FromXmlParser.Feature f) {
        this._xmlParserFeatures |= f.getMask();
        return this;
    }

    public XmlFactory disable(FromXmlParser.Feature f) {
        this._xmlParserFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(FromXmlParser.Feature f) {
        return (this._xmlParserFeatures & f.getMask()) != 0;
    }

    @Override
    public int getFormatParserFeatures() {
        return this._xmlParserFeatures;
    }

    @Override
    public int getFormatGeneratorFeatures() {
        return this._xmlGeneratorFeatures;
    }

    public final XmlFactory configure(ToXmlGenerator.Feature f, boolean state) {
        if (state) {
            this.enable(f);
        } else {
            this.disable(f);
        }
        return this;
    }

    public XmlFactory enable(ToXmlGenerator.Feature f) {
        this._xmlGeneratorFeatures |= f.getMask();
        return this;
    }

    public XmlFactory disable(ToXmlGenerator.Feature f) {
        this._xmlGeneratorFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(ToXmlGenerator.Feature f) {
        return (this._xmlGeneratorFeatures & f.getMask()) != 0;
    }

    public XMLInputFactory getXMLInputFactory() {
        return this._xmlInputFactory;
    }

    @Deprecated
    public void setXMLInputFactory(XMLInputFactory f) {
        this._xmlInputFactory = f;
    }

    public XMLOutputFactory getXMLOutputFactory() {
        return this._xmlOutputFactory;
    }

    @Deprecated
    public void setXMLOutputFactory(XMLOutputFactory f) {
        this._xmlOutputFactory = f;
    }

    @Override
    public String getFormatName() {
        return FORMAT_NAME_XML;
    }

    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException {
        return XmlFactory.hasXMLFormat(acc);
    }

    @Override
    public boolean requiresCustomCodec() {
        return true;
    }

    @Override
    public boolean canUseCharArrays() {
        return false;
    }

    public Class<FromXmlParser.Feature> getFormatReadFeatureType() {
        return FromXmlParser.Feature.class;
    }

    public Class<ToXmlGenerator.Feature> getFormatWriteFeatureType() {
        return ToXmlGenerator.Feature.class;
    }

    @Override
    public JsonParser createParser(String content) throws IOException {
        Reader r = new StringReader(content);
        IOContext ctxt = this._createContext(this._createContentReference(r), true);
        if (this._inputDecorator != null) {
            r = this._inputDecorator.decorate(ctxt, r);
        }
        return this._createParser(r, ctxt);
    }

    @Override
    public ToXmlGenerator createGenerator(OutputStream out) throws IOException {
        return this.createGenerator(out, JsonEncoding.UTF8);
    }

    @Override
    public ToXmlGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(out), false);
        ctxt.setEncoding(enc);
        return new ToXmlGenerator(ctxt, this._generatorFeatures, this._xmlGeneratorFeatures, this._objectCodec, this._createXmlWriter(ctxt, out));
    }

    @Override
    public ToXmlGenerator createGenerator(Writer out) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(out), false);
        return new ToXmlGenerator(ctxt, this._generatorFeatures, this._xmlGeneratorFeatures, this._objectCodec, this._createXmlWriter(ctxt, out));
    }

    @Override
    public ToXmlGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        IOContext ctxt = this._createContext(this._createContentReference(out), true);
        ctxt.setEncoding(enc);
        return new ToXmlGenerator(ctxt, this._generatorFeatures, this._xmlGeneratorFeatures, this._objectCodec, this._createXmlWriter(ctxt, out));
    }

    public FromXmlParser createParser(XMLStreamReader sr) throws IOException {
        if (sr.getEventType() != 1) {
            sr = this._initializeXmlReader(sr);
        }
        FromXmlParser xp = new FromXmlParser(this._createContext(this._createContentReference(sr), false), this._parserFeatures, this._xmlParserFeatures, this._objectCodec, sr);
        if (this._cfgNameForTextElement != null) {
            xp.setXMLTextElementName(this._cfgNameForTextElement);
        }
        return xp;
    }

    public ToXmlGenerator createGenerator(XMLStreamWriter sw) throws IOException {
        sw = this._initializeXmlWriter(sw);
        IOContext ctxt = this._createContext(this._createContentReference(sw), false);
        return new ToXmlGenerator(ctxt, this._generatorFeatures, this._xmlGeneratorFeatures, this._objectCodec, sw);
    }

    @Override
    protected FromXmlParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        XMLStreamReader sr;
        try {
            sr = this._xmlInputFactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            return (FromXmlParser)StaxUtil.throwAsParseException(e, null);
        }
        sr = this._initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, this._parserFeatures, this._xmlParserFeatures, this._objectCodec, sr);
        if (this._cfgNameForTextElement != null) {
            xp.setXMLTextElementName(this._cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(Reader r, IOContext ctxt) throws IOException {
        XMLStreamReader sr;
        try {
            sr = this._xmlInputFactory.createXMLStreamReader(r);
        } catch (XMLStreamException e) {
            return (FromXmlParser)StaxUtil.throwAsParseException(e, null);
        }
        sr = this._initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, this._parserFeatures, this._xmlParserFeatures, this._objectCodec, sr);
        if (this._cfgNameForTextElement != null) {
            xp.setXMLTextElementName(this._cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recycleBuffer) throws IOException {
        XMLStreamReader sr;
        try {
            sr = this._xmlInputFactory instanceof XMLInputFactory2 ? this._xmlInputFactory.createXMLStreamReader((Source)new Stax2CharArraySource(data, offset, len)) : this._xmlInputFactory.createXMLStreamReader(new CharArrayReader(data, offset, len));
        } catch (XMLStreamException e) {
            return (FromXmlParser)StaxUtil.throwAsParseException(e, null);
        }
        sr = this._initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, this._parserFeatures, this._xmlParserFeatures, this._objectCodec, sr);
        if (this._cfgNameForTextElement != null) {
            xp.setXMLTextElementName(this._cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected FromXmlParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        XMLStreamReader sr;
        try {
            sr = this._xmlInputFactory instanceof XMLInputFactory2 ? this._xmlInputFactory.createXMLStreamReader((Source)new Stax2ByteArraySource(data, offset, len)) : this._xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(data, offset, len));
        } catch (XMLStreamException e) {
            return (FromXmlParser)StaxUtil.throwAsParseException(e, null);
        }
        sr = this._initializeXmlReader(sr);
        FromXmlParser xp = new FromXmlParser(ctxt, this._parserFeatures, this._xmlParserFeatures, this._objectCodec, sr);
        if (this._cfgNameForTextElement != null) {
            xp.setXMLTextElementName(this._cfgNameForTextElement);
        }
        return xp;
    }

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        VersionUtil.throwInternal();
        return null;
    }

    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, OutputStream out) throws IOException {
        XMLStreamWriter sw;
        try {
            sw = this._xmlOutputFactory.createXMLStreamWriter(this._decorate(ctxt, out), "UTF-8");
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
        }
        return this._initializeXmlWriter(sw);
    }

    protected XMLStreamWriter _createXmlWriter(IOContext ctxt, Writer w) throws IOException {
        XMLStreamWriter sw;
        try {
            sw = this._xmlOutputFactory.createXMLStreamWriter(this._decorate(ctxt, w));
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
        }
        return this._initializeXmlWriter(sw);
    }

    protected final XMLStreamWriter _initializeXmlWriter(XMLStreamWriter sw) throws IOException {
        try {
            sw.setDefaultNamespace("");
        } catch (Exception e) {
            throw new JsonGenerationException(e.getMessage(), e, null);
        }
        return sw;
    }

    protected final XMLStreamReader _initializeXmlReader(XMLStreamReader sr) throws IOException {
        try {
            while (sr.next() != 1) {
            }
        } catch (Exception e) {
            throw new JsonParseException(null, e.getMessage(), (Throwable)e);
        }
        return sr;
    }

    public static MatchStrength hasXMLFormat(InputAccessor acc) throws IOException {
        boolean maybeXmlDecl;
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte b = acc.nextByte();
        if (b == -17) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != -69) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != -65) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
        }
        boolean bl = maybeXmlDecl = b == 60;
        if (!maybeXmlDecl) {
            int ch = XmlFactory.skipSpace(acc, b);
            if (ch < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = (byte)ch;
            if (b != 60) {
                return MatchStrength.NO_MATCH;
            }
        }
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        b = acc.nextByte();
        if (b == 63) {
            b = acc.nextByte();
            if (b == 120) {
                if (maybeXmlDecl && acc.hasMoreBytes() && acc.nextByte() == 109 && acc.hasMoreBytes() && acc.nextByte() == 108) {
                    return MatchStrength.FULL_MATCH;
                }
                return MatchStrength.SOLID_MATCH;
            }
            if (XmlFactory.validXmlNameStartChar(acc, b)) {
                return MatchStrength.SOLID_MATCH;
            }
        } else if (b == 33) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
            if (b == 45) {
                if (!acc.hasMoreBytes()) {
                    return MatchStrength.INCONCLUSIVE;
                }
                if (acc.nextByte() == 45) {
                    return MatchStrength.SOLID_MATCH;
                }
            } else if (b == 68) {
                return XmlFactory.tryMatch(acc, "OCTYPE", MatchStrength.SOLID_MATCH);
            }
        } else if (XmlFactory.validXmlNameStartChar(acc, b)) {
            return MatchStrength.SOLID_MATCH;
        }
        return MatchStrength.NO_MATCH;
    }

    private static final boolean validXmlNameStartChar(InputAccessor acc, byte b) throws IOException {
        int ch = b & 0xFF;
        return ch >= 65;
    }

    private static final MatchStrength tryMatch(InputAccessor acc, String matchStr, MatchStrength fullMatchStrength) throws IOException {
        int len = matchStr.length();
        for (int i = 0; i < len; ++i) {
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() == matchStr.charAt(i)) continue;
            return MatchStrength.NO_MATCH;
        }
        return fullMatchStrength;
    }

    private static final int skipSpace(InputAccessor acc, byte b) throws IOException {
        int ch;
        while ((ch = b & 0xFF) == 32 || ch == 13 || ch == 10 || ch == 9) {
            if (!acc.hasMoreBytes()) {
                return -1;
            }
            b = acc.nextByte();
            int n = b & 0xFF;
        }
        return ch;
    }

    protected OutputStream _decorate(IOContext ioCtxt, OutputStream out) throws IOException {
        OutputStream out2;
        if (this._outputDecorator != null && (out2 = this._outputDecorator.decorate(ioCtxt, out)) != null) {
            return out2;
        }
        return out;
    }

    protected Writer _decorate(IOContext ioCtxt, Writer out) throws IOException {
        Writer out2;
        if (this._outputDecorator != null && (out2 = this._outputDecorator.decorate(ioCtxt, out)) != null) {
            return out2;
        }
        return out;
    }
}

