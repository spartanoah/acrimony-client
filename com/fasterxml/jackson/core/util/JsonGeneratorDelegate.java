/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonGeneratorDelegate
extends JsonGenerator {
    protected JsonGenerator delegate;
    protected boolean delegateCopyMethods;

    public JsonGeneratorDelegate(JsonGenerator d) {
        this(d, true);
    }

    public JsonGeneratorDelegate(JsonGenerator d, boolean delegateCopyMethods) {
        this.delegate = d;
        this.delegateCopyMethods = delegateCopyMethods;
    }

    @Override
    public Object getCurrentValue() {
        return this.delegate.getCurrentValue();
    }

    @Override
    public void setCurrentValue(Object v) {
        this.delegate.setCurrentValue(v);
    }

    @Override
    public ObjectCodec getCodec() {
        return this.delegate.getCodec();
    }

    @Override
    public JsonGenerator setCodec(ObjectCodec oc) {
        this.delegate.setCodec(oc);
        return this;
    }

    @Override
    public void setSchema(FormatSchema schema) {
        this.delegate.setSchema(schema);
    }

    @Override
    public FormatSchema getSchema() {
        return this.delegate.getSchema();
    }

    @Override
    public Version version() {
        return this.delegate.version();
    }

    @Override
    public Object getOutputTarget() {
        return this.delegate.getOutputTarget();
    }

    @Override
    public int getOutputBuffered() {
        return this.delegate.getOutputBuffered();
    }

    @Override
    public boolean canUseSchema(FormatSchema schema) {
        return this.delegate.canUseSchema(schema);
    }

    @Override
    public boolean canWriteTypeId() {
        return this.delegate.canWriteTypeId();
    }

    @Override
    public boolean canWriteObjectId() {
        return this.delegate.canWriteObjectId();
    }

    @Override
    public boolean canWriteBinaryNatively() {
        return this.delegate.canWriteBinaryNatively();
    }

    @Override
    public boolean canOmitFields() {
        return this.delegate.canOmitFields();
    }

    @Override
    public boolean canWriteFormattedNumbers() {
        return this.delegate.canWriteFormattedNumbers();
    }

    @Override
    public JsonGenerator enable(JsonGenerator.Feature f) {
        this.delegate.enable(f);
        return this;
    }

    @Override
    public JsonGenerator disable(JsonGenerator.Feature f) {
        this.delegate.disable(f);
        return this;
    }

    @Override
    public boolean isEnabled(JsonGenerator.Feature f) {
        return this.delegate.isEnabled(f);
    }

    @Override
    public int getFeatureMask() {
        return this.delegate.getFeatureMask();
    }

    @Override
    @Deprecated
    public JsonGenerator setFeatureMask(int mask) {
        this.delegate.setFeatureMask(mask);
        return this;
    }

    @Override
    public JsonGenerator overrideStdFeatures(int values, int mask) {
        this.delegate.overrideStdFeatures(values, mask);
        return this;
    }

    @Override
    public JsonGenerator overrideFormatFeatures(int values, int mask) {
        this.delegate.overrideFormatFeatures(values, mask);
        return this;
    }

    @Override
    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        this.delegate.setPrettyPrinter(pp);
        return this;
    }

    @Override
    public PrettyPrinter getPrettyPrinter() {
        return this.delegate.getPrettyPrinter();
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        this.delegate.useDefaultPrettyPrinter();
        return this;
    }

    @Override
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        this.delegate.setHighestNonEscapedChar(charCode);
        return this;
    }

    @Override
    public int getHighestEscapedChar() {
        return this.delegate.getHighestEscapedChar();
    }

    @Override
    public CharacterEscapes getCharacterEscapes() {
        return this.delegate.getCharacterEscapes();
    }

    @Override
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
        this.delegate.setCharacterEscapes(esc);
        return this;
    }

    @Override
    public JsonGenerator setRootValueSeparator(SerializableString sep) {
        this.delegate.setRootValueSeparator(sep);
        return this;
    }

    @Override
    public void writeStartArray() throws IOException {
        this.delegate.writeStartArray();
    }

    @Override
    public void writeStartArray(int size) throws IOException {
        this.delegate.writeStartArray(size);
    }

    @Override
    public void writeStartArray(Object forValue) throws IOException {
        this.delegate.writeStartArray(forValue);
    }

    @Override
    public void writeStartArray(Object forValue, int size) throws IOException {
        this.delegate.writeStartArray(forValue, size);
    }

    @Override
    public void writeEndArray() throws IOException {
        this.delegate.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        this.delegate.writeStartObject();
    }

    @Override
    public void writeStartObject(Object forValue) throws IOException {
        this.delegate.writeStartObject(forValue);
    }

    @Override
    public void writeStartObject(Object forValue, int size) throws IOException {
        this.delegate.writeStartObject(forValue, size);
    }

    @Override
    public void writeEndObject() throws IOException {
        this.delegate.writeEndObject();
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        this.delegate.writeFieldName(name);
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException {
        this.delegate.writeFieldName(name);
    }

    @Override
    public void writeFieldId(long id) throws IOException {
        this.delegate.writeFieldId(id);
    }

    @Override
    public void writeArray(int[] array, int offset, int length) throws IOException {
        this.delegate.writeArray(array, offset, length);
    }

    @Override
    public void writeArray(long[] array, int offset, int length) throws IOException {
        this.delegate.writeArray(array, offset, length);
    }

    @Override
    public void writeArray(double[] array, int offset, int length) throws IOException {
        this.delegate.writeArray(array, offset, length);
    }

    @Override
    public void writeArray(String[] array, int offset, int length) throws IOException {
        this.delegate.writeArray(array, offset, length);
    }

    @Override
    public void writeString(String text) throws IOException {
        this.delegate.writeString(text);
    }

    @Override
    public void writeString(Reader reader, int len) throws IOException {
        this.delegate.writeString(reader, len);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        this.delegate.writeString(text, offset, len);
    }

    @Override
    public void writeString(SerializableString text) throws IOException {
        this.delegate.writeString(text);
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        this.delegate.writeRawUTF8String(text, offset, length);
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        this.delegate.writeUTF8String(text, offset, length);
    }

    @Override
    public void writeRaw(String text) throws IOException {
        this.delegate.writeRaw(text);
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        this.delegate.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(SerializableString raw) throws IOException {
        this.delegate.writeRaw(raw);
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        this.delegate.writeRaw(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException {
        this.delegate.writeRaw(c);
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        this.delegate.writeRawValue(text);
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException {
        this.delegate.writeRawValue(text, offset, len);
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        this.delegate.writeRawValue(text, offset, len);
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        this.delegate.writeBinary(b64variant, data, offset, len);
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
        return this.delegate.writeBinary(b64variant, data, dataLength);
    }

    @Override
    public void writeNumber(short v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(double v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(float v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException {
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, UnsupportedOperationException {
        this.delegate.writeNumber(encodedValue);
    }

    @Override
    public void writeNumber(char[] encodedValueBuffer, int offset, int length) throws IOException, UnsupportedOperationException {
        this.delegate.writeNumber(encodedValueBuffer, offset, length);
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        this.delegate.writeBoolean(state);
    }

    @Override
    public void writeNull() throws IOException {
        this.delegate.writeNull();
    }

    @Override
    public void writeOmittedField(String fieldName) throws IOException {
        this.delegate.writeOmittedField(fieldName);
    }

    @Override
    public void writeObjectId(Object id) throws IOException {
        this.delegate.writeObjectId(id);
    }

    @Override
    public void writeObjectRef(Object id) throws IOException {
        this.delegate.writeObjectRef(id);
    }

    @Override
    public void writeTypeId(Object id) throws IOException {
        this.delegate.writeTypeId(id);
    }

    @Override
    public void writeEmbeddedObject(Object object) throws IOException {
        this.delegate.writeEmbeddedObject(object);
    }

    @Override
    public void writeObject(Object pojo) throws IOException {
        if (this.delegateCopyMethods) {
            this.delegate.writeObject(pojo);
            return;
        }
        if (pojo == null) {
            this.writeNull();
        } else {
            ObjectCodec c = this.getCodec();
            if (c != null) {
                c.writeValue(this, pojo);
                return;
            }
            this._writeSimpleObject(pojo);
        }
    }

    @Override
    public void writeTree(TreeNode tree) throws IOException {
        if (this.delegateCopyMethods) {
            this.delegate.writeTree(tree);
            return;
        }
        if (tree == null) {
            this.writeNull();
        } else {
            ObjectCodec c = this.getCodec();
            if (c == null) {
                throw new IllegalStateException("No ObjectCodec defined");
            }
            c.writeTree(this, tree);
        }
    }

    @Override
    public void copyCurrentEvent(JsonParser p) throws IOException {
        if (this.delegateCopyMethods) {
            this.delegate.copyCurrentEvent(p);
        } else {
            super.copyCurrentEvent(p);
        }
    }

    @Override
    public void copyCurrentStructure(JsonParser p) throws IOException {
        if (this.delegateCopyMethods) {
            this.delegate.copyCurrentStructure(p);
        } else {
            super.copyCurrentStructure(p);
        }
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return this.delegate.getOutputContext();
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
    }

    @Override
    public boolean isClosed() {
        return this.delegate.isClosed();
    }

    @Deprecated
    public JsonGenerator getDelegate() {
        return this.delegate;
    }

    public JsonGenerator delegate() {
        return this.delegate;
    }
}

