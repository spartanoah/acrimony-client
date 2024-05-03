/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;

public class JsonFactoryBuilder
extends TSFBuilder<JsonFactory, JsonFactoryBuilder> {
    protected CharacterEscapes _characterEscapes;
    protected SerializableString _rootValueSeparator;
    protected int _maximumNonEscapedChar;
    protected char _quoteChar = (char)34;

    public JsonFactoryBuilder() {
        this._rootValueSeparator = JsonFactory.DEFAULT_ROOT_VALUE_SEPARATOR;
        this._maximumNonEscapedChar = 0;
    }

    public JsonFactoryBuilder(JsonFactory base) {
        super(base);
        this._characterEscapes = base.getCharacterEscapes();
        this._rootValueSeparator = base._rootValueSeparator;
        this._maximumNonEscapedChar = base._maximumNonEscapedChar;
    }

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature f) {
        this._legacyEnable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature first, JsonReadFeature ... other) {
        this._legacyEnable(first.mappedFeature());
        this.enable(first);
        for (JsonReadFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature f) {
        this._legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature first, JsonReadFeature ... other) {
        this._legacyDisable(first.mappedFeature());
        for (JsonReadFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonReadFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature f) {
        JsonGenerator.Feature old = f.mappedFeature();
        if (old != null) {
            this._legacyEnable(old);
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature first, JsonWriteFeature ... other) {
        this._legacyEnable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature f) {
        this._legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature first, JsonWriteFeature ... other) {
        this._legacyDisable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            this._legacyDisable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonWriteFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public JsonFactoryBuilder characterEscapes(CharacterEscapes esc) {
        this._characterEscapes = esc;
        return this;
    }

    public JsonFactoryBuilder rootValueSeparator(String sep) {
        this._rootValueSeparator = sep == null ? null : new SerializedString(sep);
        return this;
    }

    public JsonFactoryBuilder rootValueSeparator(SerializableString sep) {
        this._rootValueSeparator = sep;
        return this;
    }

    public JsonFactoryBuilder highestNonEscapedChar(int maxNonEscaped) {
        this._maximumNonEscapedChar = maxNonEscaped <= 0 ? 0 : Math.max(127, maxNonEscaped);
        return this;
    }

    public JsonFactoryBuilder quoteChar(char ch) {
        if (ch > '\u007f') {
            throw new IllegalArgumentException("Can only use Unicode characters up to 0x7F as quote characters");
        }
        this._quoteChar = ch;
        return this;
    }

    public CharacterEscapes characterEscapes() {
        return this._characterEscapes;
    }

    public SerializableString rootValueSeparator() {
        return this._rootValueSeparator;
    }

    public int highestNonEscapedChar() {
        return this._maximumNonEscapedChar;
    }

    public char quoteChar() {
        return this._quoteChar;
    }

    @Override
    public JsonFactory build() {
        return new JsonFactory(this);
    }
}

