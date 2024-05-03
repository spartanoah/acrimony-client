/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.codehaus.stax2.XMLStreamWriter2
 *  org.codehaus.stax2.typed.Base64Variant
 */
package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.Instantiatable;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.typed.Base64Variant;

public class DefaultXmlPrettyPrinter
implements XmlPrettyPrinter,
Instantiatable<DefaultXmlPrettyPrinter>,
Serializable {
    private static final long serialVersionUID = 1L;
    protected Indenter _arrayIndenter = new FixedSpaceIndenter();
    protected Indenter _objectIndenter = new Lf2SpacesIndenter();
    protected boolean _spacesInObjectEntries = true;
    protected transient int _nesting = 0;
    protected transient boolean _justHadStartElement;

    public DefaultXmlPrettyPrinter() {
    }

    protected DefaultXmlPrettyPrinter(DefaultXmlPrettyPrinter base) {
        this._arrayIndenter = base._arrayIndenter;
        this._objectIndenter = base._objectIndenter;
        this._spacesInObjectEntries = base._spacesInObjectEntries;
        this._nesting = base._nesting;
    }

    public void indentArraysWith(Indenter i) {
        this._arrayIndenter = i == null ? new NopIndenter() : i;
    }

    public void indentObjectsWith(Indenter i) {
        this._objectIndenter = i == null ? new NopIndenter() : i;
    }

    public void spacesInObjectEntries(boolean b) {
        this._spacesInObjectEntries = b;
    }

    @Override
    public DefaultXmlPrettyPrinter createInstance() {
        return new DefaultXmlPrettyPrinter(this);
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw('\n');
    }

    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException {
    }

    @Override
    public void writeStartArray(JsonGenerator gen) throws IOException {
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
    }

    @Override
    public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
    }

    @Override
    public void beforeObjectEntries(JsonGenerator gen) throws IOException, JsonGenerationException {
    }

    @Override
    public void writeStartObject(JsonGenerator gen) throws IOException {
        if (!this._objectIndenter.isInline()) {
            if (this._nesting > 0) {
                this._objectIndenter.writeIndentation(gen, this._nesting);
            }
            ++this._nesting;
        }
        this._justHadStartElement = true;
        ((ToXmlGenerator)gen)._handleStartObject();
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
    }

    @Override
    public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {
        if (!this._objectIndenter.isInline()) {
            --this._nesting;
        }
        if (this._justHadStartElement) {
            this._justHadStartElement = false;
        } else {
            this._objectIndenter.writeIndentation(gen, this._nesting);
        }
        ((ToXmlGenerator)gen)._handleEndObject();
    }

    @Override
    public void writeStartElement(XMLStreamWriter2 sw, String nsURI, String localName) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            if (this._justHadStartElement) {
                this._justHadStartElement = false;
            }
            this._objectIndenter.writeIndentation(sw, this._nesting);
            ++this._nesting;
        }
        sw.writeStartElement(nsURI, localName);
        this._justHadStartElement = true;
    }

    @Override
    public void writeEndElement(XMLStreamWriter2 sw, int nrOfEntries) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            --this._nesting;
        }
        if (this._justHadStartElement) {
            this._justHadStartElement = false;
        } else {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeEndElement();
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, String text, boolean isCData) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        if (isCData) {
            sw.writeCData(text);
        } else {
            sw.writeCharacters(text);
        }
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, char[] buffer, int offset, int len, boolean isCData) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        if (isCData) {
            sw.writeCData(buffer, offset, len);
        } else {
            sw.writeCharacters(buffer, offset, len);
        }
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, boolean value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeBoolean(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, int value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeInt(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, long value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeLong(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, double value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeDouble(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, float value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeFloat(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, BigInteger value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeInteger(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, BigDecimal value) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeDecimal(value);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, Base64Variant base64variant, byte[] data, int offset, int len) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeStartElement(nsURI, localName);
        sw.writeBinary(base64variant, data, offset, len);
        sw.writeEndElement();
        this._justHadStartElement = false;
    }

    @Override
    public void writeLeafNullElement(XMLStreamWriter2 sw, String nsURI, String localName) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeEmptyElement(nsURI, localName);
        this._justHadStartElement = false;
    }

    public void writeLeafXsiNilElement(XMLStreamWriter2 sw, String nsURI, String localName) throws XMLStreamException {
        if (!this._objectIndenter.isInline()) {
            this._objectIndenter.writeIndentation(sw, this._nesting);
        }
        sw.writeEmptyElement(nsURI, localName);
        sw.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
        this._justHadStartElement = false;
    }

    @Override
    public void writePrologLinefeed(XMLStreamWriter2 sw) throws XMLStreamException {
        sw.writeRaw(Lf2SpacesIndenter.SYSTEM_LINE_SEPARATOR);
    }

    protected static class Lf2SpacesIndenter
    implements Indenter,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final String SYSTEM_LINE_SEPARATOR;
        static final int SPACE_COUNT = 64;
        static final char[] SPACES;

        @Override
        public boolean isInline() {
            return false;
        }

        @Override
        public void writeIndentation(XMLStreamWriter2 sw, int level) throws XMLStreamException {
            sw.writeRaw(SYSTEM_LINE_SEPARATOR);
            level += level;
            while (level > 64) {
                sw.writeRaw(SPACES, 0, 64);
                level -= SPACES.length;
            }
            sw.writeRaw(SPACES, 0, level);
        }

        @Override
        public void writeIndentation(JsonGenerator jg, int level) throws IOException {
            jg.writeRaw(SYSTEM_LINE_SEPARATOR);
            level += level;
            while (level > 64) {
                jg.writeRaw(SPACES, 0, 64);
                level -= SPACES.length;
            }
            jg.writeRaw(SPACES, 0, level);
        }

        static {
            String lf = null;
            try {
                lf = System.getProperty("line.separator");
            } catch (Throwable throwable) {
                // empty catch block
            }
            SYSTEM_LINE_SEPARATOR = lf == null ? "\n" : lf;
            SPACES = new char[64];
            Arrays.fill(SPACES, ' ');
        }
    }

    protected static class FixedSpaceIndenter
    implements Indenter,
    Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public void writeIndentation(XMLStreamWriter2 sw, int level) throws XMLStreamException {
            sw.writeRaw(" ");
        }

        @Override
        public void writeIndentation(JsonGenerator g, int level) throws IOException {
            g.writeRaw(' ');
        }

        @Override
        public boolean isInline() {
            return true;
        }
    }

    protected static class NopIndenter
    implements Indenter,
    Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public void writeIndentation(JsonGenerator jg, int level) {
        }

        @Override
        public boolean isInline() {
            return true;
        }

        @Override
        public void writeIndentation(XMLStreamWriter2 sw, int level) {
        }
    }

    public static interface Indenter {
        public void writeIndentation(JsonGenerator var1, int var2) throws IOException;

        public void writeIndentation(XMLStreamWriter2 var1, int var2) throws XMLStreamException;

        public boolean isInline();
    }
}

