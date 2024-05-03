/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.core.filter.TokenFilterContext;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

public class FilteringGeneratorDelegate
extends JsonGeneratorDelegate {
    protected TokenFilter rootFilter;
    protected boolean _allowMultipleMatches;
    protected boolean _includePath;
    @Deprecated
    protected boolean _includeImmediateParent;
    protected TokenFilterContext _filterContext;
    protected TokenFilter _itemFilter;
    protected int _matchCount;

    public FilteringGeneratorDelegate(JsonGenerator d, TokenFilter f, boolean includePath, boolean allowMultipleMatches) {
        super(d, false);
        this.rootFilter = f;
        this._itemFilter = f;
        this._filterContext = TokenFilterContext.createRootContext(f);
        this._includePath = includePath;
        this._allowMultipleMatches = allowMultipleMatches;
    }

    public TokenFilter getFilter() {
        return this.rootFilter;
    }

    public JsonStreamContext getFilterContext() {
        return this._filterContext;
    }

    public int getMatchCount() {
        return this._matchCount;
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return this._filterContext;
    }

    @Override
    public void writeStartArray() throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray();
            return;
        }
        this._itemFilter = this._filterContext.checkValue(this._itemFilter);
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            this._itemFilter = this._itemFilter.filterStartArray();
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray();
        } else {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, false);
        }
    }

    @Override
    public void writeStartArray(int size) throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(size);
            return;
        }
        this._itemFilter = this._filterContext.checkValue(this._itemFilter);
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            this._itemFilter = this._itemFilter.filterStartArray();
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(size);
        } else {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, false);
        }
    }

    @Override
    public void writeStartArray(Object forValue) throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(forValue);
            return;
        }
        this._itemFilter = this._filterContext.checkValue(this._itemFilter);
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            this._itemFilter = this._itemFilter.filterStartArray();
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(forValue);
        } else {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, false);
        }
    }

    @Override
    public void writeStartArray(Object forValue, int size) throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(forValue, size);
            return;
        }
        this._itemFilter = this._filterContext.checkValue(this._itemFilter);
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildArrayContext(null, false);
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            this._itemFilter = this._itemFilter.filterStartArray();
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, true);
            this.delegate.writeStartArray(forValue, size);
        } else {
            this._filterContext = this._filterContext.createChildArrayContext(this._itemFilter, false);
        }
    }

    @Override
    public void writeEndArray() throws IOException {
        this._filterContext = this._filterContext.closeArray(this.delegate);
        if (this._filterContext != null) {
            this._itemFilter = this._filterContext.getFilter();
        }
    }

    @Override
    public void writeStartObject() throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, true);
            this.delegate.writeStartObject();
            return;
        }
        TokenFilter f = this._filterContext.checkValue(this._itemFilter);
        if (f == null) {
            return;
        }
        if (f != TokenFilter.INCLUDE_ALL) {
            f = f.filterStartObject();
        }
        if (f == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildObjectContext(f, true);
            this.delegate.writeStartObject();
        } else {
            this._filterContext = this._filterContext.createChildObjectContext(f, false);
        }
    }

    @Override
    public void writeStartObject(Object forValue) throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, true);
            this.delegate.writeStartObject(forValue);
            return;
        }
        TokenFilter f = this._filterContext.checkValue(this._itemFilter);
        if (f == null) {
            return;
        }
        if (f != TokenFilter.INCLUDE_ALL) {
            f = f.filterStartObject();
        }
        if (f == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildObjectContext(f, true);
            this.delegate.writeStartObject(forValue);
        } else {
            this._filterContext = this._filterContext.createChildObjectContext(f, false);
        }
    }

    @Override
    public void writeStartObject(Object forValue, int size) throws IOException {
        if (this._itemFilter == null) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, false);
            return;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            this._filterContext = this._filterContext.createChildObjectContext(this._itemFilter, true);
            this.delegate.writeStartObject(forValue, size);
            return;
        }
        TokenFilter f = this._filterContext.checkValue(this._itemFilter);
        if (f == null) {
            return;
        }
        if (f != TokenFilter.INCLUDE_ALL) {
            f = f.filterStartObject();
        }
        if (f == TokenFilter.INCLUDE_ALL) {
            this._checkParentPath();
            this._filterContext = this._filterContext.createChildObjectContext(f, true);
            this.delegate.writeStartObject(forValue, size);
        } else {
            this._filterContext = this._filterContext.createChildObjectContext(f, false);
        }
    }

    @Override
    public void writeEndObject() throws IOException {
        this._filterContext = this._filterContext.closeObject(this.delegate);
        if (this._filterContext != null) {
            this._itemFilter = this._filterContext.getFilter();
        }
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        TokenFilter state = this._filterContext.setFieldName(name);
        if (state == null) {
            this._itemFilter = null;
            return;
        }
        if (state == TokenFilter.INCLUDE_ALL) {
            this._itemFilter = state;
            this.delegate.writeFieldName(name);
            return;
        }
        this._itemFilter = state = state.includeProperty(name);
        if (state == TokenFilter.INCLUDE_ALL) {
            this._checkPropertyParentPath();
        }
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException {
        TokenFilter state = this._filterContext.setFieldName(name.getValue());
        if (state == null) {
            this._itemFilter = null;
            return;
        }
        if (state == TokenFilter.INCLUDE_ALL) {
            this._itemFilter = state;
            this.delegate.writeFieldName(name);
            return;
        }
        this._itemFilter = state = state.includeProperty(name.getValue());
        if (state == TokenFilter.INCLUDE_ALL) {
            this._checkPropertyParentPath();
        }
    }

    @Override
    public void writeFieldId(long id) throws IOException {
        this.writeFieldName(Long.toString(id));
    }

    @Override
    public void writeString(String value) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeString(value)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeString(value);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            String value = new String(text, offset, len);
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeString(value)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeString(text, offset, len);
    }

    @Override
    public void writeString(SerializableString value) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeString(value.getValue())) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeString(value);
    }

    @Override
    public void writeString(Reader reader, int len) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeString(reader, len)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeString(reader, len);
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRawUTF8String(text, offset, length);
        }
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeUTF8String(text, offset, length);
        }
    }

    @Override
    public void writeRaw(String text) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRaw(text);
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRaw(text, offset, len);
        }
    }

    @Override
    public void writeRaw(SerializableString text) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRaw(text);
        }
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRaw(text, offset, len);
        }
    }

    @Override
    public void writeRaw(char c) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRaw(c);
        }
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRawValue(text);
        }
    }

    @Override
    public void writeRawValue(String text, int offset, int len) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRawValue(text, offset, len);
        }
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        if (this._checkRawValueWrite()) {
            this.delegate.writeRawValue(text, offset, len);
        }
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        if (this._checkBinaryWrite()) {
            this.delegate.writeBinary(b64variant, data, offset, len);
        }
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
        if (this._checkBinaryWrite()) {
            return this.delegate.writeBinary(b64variant, data, dataLength);
        }
        return -1;
    }

    @Override
    public void writeNumber(short v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(long v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(double v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(float v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNumber(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(v);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException, UnsupportedOperationException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeRawValue()) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(encodedValue);
    }

    @Override
    public void writeNumber(char[] encodedValueBuffer, int offset, int length) throws IOException, UnsupportedOperationException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeRawValue()) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNumber(encodedValueBuffer, offset, length);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeBoolean(v)) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeBoolean(v);
    }

    @Override
    public void writeNull() throws IOException {
        if (this._itemFilter == null) {
            return;
        }
        if (this._itemFilter != TokenFilter.INCLUDE_ALL) {
            TokenFilter state = this._filterContext.checkValue(this._itemFilter);
            if (state == null) {
                return;
            }
            if (state != TokenFilter.INCLUDE_ALL && !state.includeNull()) {
                return;
            }
            this._checkParentPath();
        }
        this.delegate.writeNull();
    }

    @Override
    public void writeOmittedField(String fieldName) throws IOException {
        if (this._itemFilter != null) {
            this.delegate.writeOmittedField(fieldName);
        }
    }

    @Override
    public void writeObjectId(Object id) throws IOException {
        if (this._itemFilter != null) {
            this.delegate.writeObjectId(id);
        }
    }

    @Override
    public void writeObjectRef(Object id) throws IOException {
        if (this._itemFilter != null) {
            this.delegate.writeObjectRef(id);
        }
    }

    @Override
    public void writeTypeId(Object id) throws IOException {
        if (this._itemFilter != null) {
            this.delegate.writeTypeId(id);
        }
    }

    protected void _checkParentPath() throws IOException {
        ++this._matchCount;
        if (this._includePath) {
            this._filterContext.writePath(this.delegate);
        }
        if (!this._allowMultipleMatches) {
            this._filterContext.skipParentChecks();
        }
    }

    protected void _checkPropertyParentPath() throws IOException {
        ++this._matchCount;
        if (this._includePath) {
            this._filterContext.writePath(this.delegate);
        } else if (this._includeImmediateParent) {
            this._filterContext.writeImmediatePath(this.delegate);
        }
        if (!this._allowMultipleMatches) {
            this._filterContext.skipParentChecks();
        }
    }

    protected boolean _checkBinaryWrite() throws IOException {
        if (this._itemFilter == null) {
            return false;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            return true;
        }
        if (this._itemFilter.includeBinary()) {
            this._checkParentPath();
            return true;
        }
        return false;
    }

    protected boolean _checkRawValueWrite() throws IOException {
        if (this._itemFilter == null) {
            return false;
        }
        if (this._itemFilter == TokenFilter.INCLUDE_ALL) {
            return true;
        }
        if (this._itemFilter.includeRawValue()) {
            this._checkParentPath();
            return true;
        }
        return false;
    }
}

