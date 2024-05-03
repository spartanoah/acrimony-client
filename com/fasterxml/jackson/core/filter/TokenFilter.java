/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenFilter {
    public static final TokenFilter INCLUDE_ALL = new TokenFilter();

    protected TokenFilter() {
    }

    public TokenFilter filterStartObject() {
        return this;
    }

    public TokenFilter filterStartArray() {
        return this;
    }

    public void filterFinishObject() {
    }

    public void filterFinishArray() {
    }

    public TokenFilter includeProperty(String name) {
        return this;
    }

    public TokenFilter includeElement(int index) {
        return this;
    }

    public TokenFilter includeRootValue(int index) {
        return this;
    }

    public boolean includeValue(JsonParser p) throws IOException {
        return this._includeScalar();
    }

    public boolean includeBoolean(boolean value) {
        return this._includeScalar();
    }

    public boolean includeNull() {
        return this._includeScalar();
    }

    public boolean includeString(String value) {
        return this._includeScalar();
    }

    public boolean includeString(Reader r, int maxLen) {
        return this._includeScalar();
    }

    public boolean includeNumber(int v) {
        return this._includeScalar();
    }

    public boolean includeNumber(long v) {
        return this._includeScalar();
    }

    public boolean includeNumber(float v) {
        return this._includeScalar();
    }

    public boolean includeNumber(double v) {
        return this._includeScalar();
    }

    public boolean includeNumber(BigDecimal v) {
        return this._includeScalar();
    }

    public boolean includeNumber(BigInteger v) {
        return this._includeScalar();
    }

    public boolean includeBinary() {
        return this._includeScalar();
    }

    public boolean includeRawValue() {
        return this._includeScalar();
    }

    public boolean includeEmbeddedValue(Object ob) {
        return this._includeScalar();
    }

    public String toString() {
        if (this == INCLUDE_ALL) {
            return "TokenFilter.INCLUDE_ALL";
        }
        return super.toString();
    }

    protected boolean _includeScalar() {
        return true;
    }
}

