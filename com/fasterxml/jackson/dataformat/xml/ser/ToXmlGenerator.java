/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.StreamWriteCapability
 *  com.fasterxml.jackson.core.util.JacksonFeatureSet
 *  org.codehaus.stax2.XMLStreamWriter2
 *  org.codehaus.stax2.ri.Stax2WriterAdapter
 *  org.codehaus.stax2.typed.Base64Variant
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.StreamWriteCapability;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.JacksonFeatureSet;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;

public class ToXmlGenerator
extends GeneratorBase {
    protected static final String DEFAULT_UNKNOWN_ELEMENT = "unknown";
    protected final XMLStreamWriter2 _xmlWriter;
    protected final XMLStreamWriter _originalXmlWriter;
    protected final boolean _stax2Emulation;
    protected final IOContext _ioContext;
    protected int _formatFeatures;
    protected XmlPrettyPrinter _xmlPrettyPrinter;
    protected boolean _initialized;
    protected QName _nextName = null;
    protected boolean _nextIsAttribute = false;
    protected boolean _nextIsUnwrapped = false;
    protected boolean _nextIsCData = false;
    protected LinkedList<QName> _elementNameStack = new LinkedList();

    public ToXmlGenerator(IOContext ctxt, int stdFeatures, int xmlFeatures, ObjectCodec codec, XMLStreamWriter sw) {
        super(stdFeatures, codec);
        this._formatFeatures = xmlFeatures;
        this._ioContext = ctxt;
        this._originalXmlWriter = sw;
        this._xmlWriter = Stax2WriterAdapter.wrapIfNecessary((XMLStreamWriter)sw);
        this._stax2Emulation = this._xmlWriter != sw;
        this._xmlPrettyPrinter = this._cfgPrettyPrinter instanceof XmlPrettyPrinter ? (XmlPrettyPrinter)this._cfgPrettyPrinter : null;
    }

    public void initGenerator() throws IOException {
        if (this._initialized) {
            return;
        }
        this._initialized = true;
        try {
            if (Feature.WRITE_XML_1_1.enabledIn(this._formatFeatures)) {
                this._xmlWriter.writeStartDocument("UTF-8", "1.1");
            } else if (Feature.WRITE_XML_DECLARATION.enabledIn(this._formatFeatures)) {
                this._xmlWriter.writeStartDocument("UTF-8", "1.0");
            } else {
                return;
            }
            if (this._xmlPrettyPrinter != null && !this._stax2Emulation) {
                this._xmlPrettyPrinter.writePrologLinefeed(this._xmlWriter);
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    protected PrettyPrinter _constructDefaultPrettyPrinter() {
        return new DefaultXmlPrettyPrinter();
    }

    @Override
    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        this._cfgPrettyPrinter = pp;
        this._xmlPrettyPrinter = pp instanceof XmlPrettyPrinter ? (XmlPrettyPrinter)pp : null;
        return this;
    }

    @Override
    public Object getOutputTarget() {
        return this._originalXmlWriter;
    }

    @Override
    public int getOutputBuffered() {
        return -1;
    }

    @Override
    public int getFormatFeatures() {
        return this._formatFeatures;
    }

    @Override
    public JsonGenerator overrideFormatFeatures(int values, int mask) {
        int oldF = this._formatFeatures;
        int newF = this._formatFeatures & ~mask | values & mask;
        if (oldF != newF) {
            this._formatFeatures = newF;
        }
        return this;
    }

    public ToXmlGenerator enable(Feature f) {
        this._formatFeatures |= f.getMask();
        return this;
    }

    public ToXmlGenerator disable(Feature f) {
        this._formatFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (this._formatFeatures & f.getMask()) != 0;
    }

    public ToXmlGenerator configure(Feature f, boolean state) {
        if (state) {
            this.enable(f);
        } else {
            this.disable(f);
        }
        return this;
    }

    @Override
    public boolean canWriteFormattedNumbers() {
        return true;
    }

    public JacksonFeatureSet<StreamWriteCapability> getWriteCapabilities() {
        return DEFAULT_TEXTUAL_WRITE_CAPABILITIES;
    }

    public boolean inRoot() {
        return this._writeContext.inRoot();
    }

    public XMLStreamWriter getStaxWriter() {
        return this._xmlWriter;
    }

    public void setNextIsAttribute(boolean isAttribute) {
        this._nextIsAttribute = isAttribute;
    }

    public void setNextIsUnwrapped(boolean isUnwrapped) {
        this._nextIsUnwrapped = isUnwrapped;
    }

    public void setNextIsCData(boolean isCData) {
        this._nextIsCData = isCData;
    }

    public final void setNextName(QName name) {
        this._nextName = name;
    }

    public final boolean setNextNameIfMissing(QName name) {
        if (this._nextName == null) {
            this._nextName = name;
            return true;
        }
        return false;
    }

    public void startWrappedValue(QName wrapperName, QName wrappedName) throws IOException {
        if (wrapperName != null) {
            try {
                if (this._xmlPrettyPrinter != null) {
                    this._xmlPrettyPrinter.writeStartElement(this._xmlWriter, wrapperName.getNamespaceURI(), wrapperName.getLocalPart());
                } else {
                    this._xmlWriter.writeStartElement(wrapperName.getNamespaceURI(), wrapperName.getLocalPart());
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsGenerationException(e, this);
            }
        }
        this.setNextName(wrappedName);
    }

    public void finishWrappedValue(QName wrapperName, QName wrappedName) throws IOException {
        if (wrapperName != null) {
            try {
                if (this._xmlPrettyPrinter != null) {
                    this._xmlPrettyPrinter.writeEndElement(this._xmlWriter, this._writeContext.getEntryCount());
                } else {
                    this._xmlWriter.writeEndElement();
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwAsGenerationException(e, this);
            }
        }
    }

    public void writeRepeatedFieldName() throws IOException {
        if (this._writeContext.writeFieldName(this._nextName.getLocalPart()) == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
    }

    @Override
    public final void writeFieldName(String name) throws IOException {
        if (this._writeContext.writeFieldName(name) == 4) {
            this._reportError("Can not write a field name, expecting a value");
        }
        String ns = this._nextName == null ? "" : this._nextName.getNamespaceURI();
        this.setNextName(new QName(ns, name));
    }

    @Override
    public final void writeStringField(String fieldName, String value) throws IOException {
        this.writeFieldName(fieldName);
        this.writeString(value);
    }

    @Override
    public final void writeStartArray() throws IOException {
        this._verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
        }
    }

    @Override
    public final void writeEndArray() throws IOException {
        if (!this._writeContext.inArray()) {
            this._reportError("Current context not Array but " + this._writeContext.typeDesc());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
        }
        this._writeContext = this._writeContext.getParent();
    }

    @Override
    public final void writeStartObject() throws IOException {
        this._verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
        } else {
            this._handleStartObject();
        }
    }

    @Override
    public final void writeEndObject() throws IOException {
        if (!this._writeContext.inObject()) {
            this._reportError("Current context not Object but " + this._writeContext.typeDesc());
        }
        this._writeContext = this._writeContext.getParent();
        if (this._cfgPrettyPrinter != null) {
            int count = this._nextIsAttribute ? 0 : this._writeContext.getEntryCount();
            this._cfgPrettyPrinter.writeEndObject(this, count);
        } else {
            this._handleEndObject();
        }
    }

    public final void _handleStartObject() throws IOException {
        if (this._nextName == null) {
            this.handleMissingName();
        }
        this._elementNameStack.addLast(this._nextName);
        try {
            this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    public final void _handleEndObject() throws IOException {
        if (this._elementNameStack.isEmpty()) {
            throw new JsonGenerationException("Can not write END_ELEMENT without open START_ELEMENT", (JsonGenerator)this);
        }
        this._nextName = this._elementNameStack.removeLast();
        try {
            this._nextIsAttribute = false;
            this._xmlWriter.writeEndElement();
            if (this._elementNameStack.isEmpty() && this._xmlPrettyPrinter != null && !this._stax2Emulation) {
                this._xmlPrettyPrinter.writePrologLinefeed(this._xmlWriter);
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException {
        this.writeFieldName(name.getValue());
    }

    @Override
    public void writeString(String text) throws IOException {
        if (text == null) {
            this.writeNull();
            return;
        }
        this._verifyValueWrite("write String value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeAttribute(this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), text);
            } else if (this.checkNextIsUnwrapped()) {
                if (this._nextIsCData) {
                    this._xmlWriter.writeCData(text);
                } else {
                    this._xmlWriter.writeCharacters(text);
                }
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), text, this._nextIsCData);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                if (this._nextIsCData) {
                    this._xmlWriter.writeCData(text);
                } else {
                    this._xmlWriter.writeCharacters(text);
                }
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        this._verifyValueWrite("write String value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeAttribute(this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), new String(text, offset, len));
            } else if (this.checkNextIsUnwrapped()) {
                if (this._nextIsCData) {
                    this._xmlWriter.writeCData(text, offset, len);
                } else {
                    this._xmlWriter.writeCharacters(text, offset, len);
                }
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), text, offset, len, this._nextIsCData);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                if (this._nextIsCData) {
                    this._xmlWriter.writeCData(text, offset, len);
                } else {
                    this._xmlWriter.writeCharacters(text, offset, len);
                }
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeString(SerializableString text) throws IOException {
        this.writeString(text.getValue());
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        this._reportUnsupportedOperation();
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        this._reportUnsupportedOperation();
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRawValue");
        }
        try {
            this._verifyValueWrite("write raw value");
            if (this._nextName == null) {
                this.handleMissingName();
            }
            if (this._nextIsAttribute) {
                this._xmlWriter.writeAttribute(this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), text);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeRaw(text);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRawValue");
        }
        try {
            this._verifyValueWrite("write raw value");
            if (this._nextName == null) {
                this.handleMissingName();
            }
            if (this._nextIsAttribute) {
                this._xmlWriter.writeAttribute(this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), text.substring(offset, offset + len));
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeRaw(text, offset, len);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRawValue");
        }
        this._verifyValueWrite("write raw value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeAttribute(this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), new String(text, offset, len));
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeRaw(text, offset, len);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRawValue(SerializableString text) throws IOException {
        this._reportUnsupportedOperation();
    }

    @Override
    public void writeRaw(String text) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRaw");
        }
        try {
            this._xmlWriter.writeRaw(text);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRaw");
        }
        try {
            this._xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        if (this._stax2Emulation) {
            this._reportUnimplementedStax2("writeRaw");
        }
        try {
            this._xmlWriter.writeRaw(text, offset, len);
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeRaw(char c) throws IOException {
        this.writeRaw(String.valueOf(c));
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        if (data == null) {
            this.writeNull();
            return;
        }
        this._verifyValueWrite("write Binary value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        org.codehaus.stax2.typed.Base64Variant stax2base64v = StaxUtil.toStax2Base64Variant(b64variant);
        try {
            if (this._nextIsAttribute) {
                byte[] fullBuffer = this.toFullBuffer(data, offset, len);
                this._xmlWriter.writeBinaryAttribute(stax2base64v, "", this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), fullBuffer);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeBinary(stax2base64v, data, offset, len);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), stax2base64v, data, offset, len);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeBinary(stax2base64v, data, offset, len);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
        if (data == null) {
            this.writeNull();
            return 0;
        }
        this._verifyValueWrite("write Binary value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        org.codehaus.stax2.typed.Base64Variant stax2base64v = StaxUtil.toStax2Base64Variant(b64variant);
        try {
            if (this._nextIsAttribute) {
                byte[] fullBuffer = this.toFullBuffer(data, dataLength);
                this._xmlWriter.writeBinaryAttribute(stax2base64v, "", this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), fullBuffer);
            } else if (this.checkNextIsUnwrapped()) {
                this.writeStreamAsBinary(stax2base64v, data, dataLength);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), stax2base64v, this.toFullBuffer(data, dataLength), 0, dataLength);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this.writeStreamAsBinary(stax2base64v, data, dataLength);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
        return dataLength;
    }

    private void writeStreamAsBinary(org.codehaus.stax2.typed.Base64Variant stax2base64v, InputStream data, int len) throws IOException, XMLStreamException {
        int read;
        byte[] tmp = new byte[3];
        int offset = 0;
        while ((read = data.read(tmp, offset, Math.min(3 - offset, len))) != -1) {
            len -= read;
            if ((offset += read) == 3) {
                offset = 0;
                this._xmlWriter.writeBinary(stax2base64v, tmp, 0, 3);
            }
            if (len != 0) continue;
        }
        if (offset > 0) {
            this._xmlWriter.writeBinary(stax2base64v, tmp, 0, offset);
        }
    }

    private byte[] toFullBuffer(byte[] data, int offset, int len) {
        if (offset == 0 && len == data.length) {
            return data;
        }
        byte[] result = new byte[len];
        if (len > 0) {
            System.arraycopy(data, offset, result, 0, len);
        }
        return result;
    }

    private byte[] toFullBuffer(InputStream data, int len) throws IOException {
        int count;
        byte[] result = new byte[len];
        for (int offset = 0; offset < len; offset += count) {
            count = data.read(result, offset, len - offset);
            if (count >= 0) continue;
            this._reportError("Too few bytes available: missing " + (len - offset) + " bytes (out of " + len + ")");
        }
        return result;
    }

    @Override
    public void writeBoolean(boolean value) throws IOException {
        this._verifyValueWrite("write boolean value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeBooleanAttribute(null, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), value);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeBoolean(value);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), value);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeBoolean(value);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNull() throws IOException {
        this._verifyValueWrite("write null value");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (!this._nextIsAttribute && !this.checkNextIsUnwrapped()) {
                boolean asXsiNil = this.isEnabled(Feature.WRITE_NULLS_AS_XSI_NIL);
                if (this._xmlPrettyPrinter != null) {
                    if (asXsiNil && this._xmlPrettyPrinter instanceof DefaultXmlPrettyPrinter) {
                        ((DefaultXmlPrettyPrinter)this._xmlPrettyPrinter).writeLeafXsiNilElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                    } else {
                        this._xmlPrettyPrinter.writeLeafNullElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                    }
                } else if (asXsiNil) {
                    this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                    this._xmlWriter.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
                    this._xmlWriter.writeEndElement();
                } else {
                    this._xmlWriter.writeEmptyElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(int i) throws IOException {
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeIntAttribute(null, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), i);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeInt(i);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), i);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeInt(i);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(long l) throws IOException {
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeLongAttribute(null, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), l);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeLong(l);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), l);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeLong(l);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(double d) throws IOException {
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeDoubleAttribute(null, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), d);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeDouble(d);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), d);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeDouble(d);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(float f) throws IOException {
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeFloatAttribute(null, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), f);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeFloat(f);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), f);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeFloat(f);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(BigDecimal dec) throws IOException {
        if (dec == null) {
            this.writeNull();
            return;
        }
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        boolean usePlain = this.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        try {
            if (this._nextIsAttribute) {
                if (usePlain) {
                    this._xmlWriter.writeAttribute("", this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), dec.toPlainString());
                } else {
                    this._xmlWriter.writeDecimalAttribute("", this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), dec);
                }
            } else if (this.checkNextIsUnwrapped()) {
                if (usePlain) {
                    this._xmlWriter.writeCharacters(dec.toPlainString());
                } else {
                    this._xmlWriter.writeDecimal(dec);
                }
            } else if (this._xmlPrettyPrinter != null) {
                if (usePlain) {
                    this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), dec.toPlainString(), false);
                } else {
                    this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), dec);
                }
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                if (usePlain) {
                    this._xmlWriter.writeCharacters(dec.toPlainString());
                } else {
                    this._xmlWriter.writeDecimal(dec);
                }
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(BigInteger value) throws IOException {
        if (value == null) {
            this.writeNull();
            return;
        }
        this._verifyValueWrite("write number");
        if (this._nextName == null) {
            this.handleMissingName();
        }
        try {
            if (this._nextIsAttribute) {
                this._xmlWriter.writeIntegerAttribute("", this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), value);
            } else if (this.checkNextIsUnwrapped()) {
                this._xmlWriter.writeInteger(value);
            } else if (this._xmlPrettyPrinter != null) {
                this._xmlPrettyPrinter.writeLeafElement(this._xmlWriter, this._nextName.getNamespaceURI(), this._nextName.getLocalPart(), value);
            } else {
                this._xmlWriter.writeStartElement(this._nextName.getNamespaceURI(), this._nextName.getLocalPart());
                this._xmlWriter.writeInteger(value);
                this._xmlWriter.writeEndElement();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, UnsupportedOperationException {
        this.writeString(encodedValue);
    }

    @Override
    protected final void _verifyValueWrite(String typeMsg) throws IOException {
        int status = this._writeContext.writeValue();
        if (status == 5) {
            this._reportError("Can not " + typeMsg + ", expecting field name");
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
            try {
                this._xmlWriter.flush();
            } catch (XMLStreamException e) {
                StaxUtil.throwAsGenerationException(e, this);
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)) {
            try {
                while (true) {
                    JsonWriteContext ctxt;
                    if ((ctxt = this._writeContext).inArray()) {
                        this.writeEndArray();
                        continue;
                    }
                    if (ctxt.inObject()) {
                        this.writeEndObject();
                        continue;
                    }
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new JsonGenerationException(e, (JsonGenerator)this);
            }
        }
        try {
            if (this._ioContext.isResourceManaged() || this.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
                this._xmlWriter.closeCompletely();
            } else {
                this._xmlWriter.close();
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    @Override
    protected void _releaseBuffers() {
    }

    protected boolean checkNextIsUnwrapped() {
        if (this._nextIsUnwrapped) {
            this._nextIsUnwrapped = false;
            return true;
        }
        return false;
    }

    protected void handleMissingName() {
        throw new IllegalStateException("No element/attribute name specified when trying to output element");
    }

    protected void _reportUnimplementedStax2(String missingMethod) throws IOException {
        throw new JsonGenerationException("Underlying Stax XMLStreamWriter (of type " + this._originalXmlWriter.getClass().getName() + ") does not implement Stax2 API natively and is missing method '" + missingMethod + "': this breaks functionality such as indentation that relies on it. You need to upgrade to using compliant Stax implementation like Woodstox or Aalto", (JsonGenerator)this);
    }

    public static enum Feature implements FormatFeature
    {
        WRITE_XML_DECLARATION(false),
        WRITE_XML_1_1(false),
        WRITE_NULLS_AS_XSI_NIL(false),
        UNWRAP_ROOT_OBJECT_NODE(false);

        final boolean _defaultState;
        final int _mask;

        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : Feature.values()) {
                if (!f.enabledByDefault()) continue;
                flags |= f.getMask();
            }
            return flags;
        }

        private Feature(boolean defaultState) {
            this._defaultState = defaultState;
            this._mask = 1 << this.ordinal();
        }

        @Override
        public boolean enabledByDefault() {
            return this._defaultState;
        }

        @Override
        public int getMask() {
            return this._mask;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & this.getMask()) != 0;
        }
    }
}

