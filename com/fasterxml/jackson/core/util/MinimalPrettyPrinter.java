/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import java.io.IOException;
import java.io.Serializable;

public class MinimalPrettyPrinter
implements PrettyPrinter,
Serializable {
    private static final long serialVersionUID = 1L;
    protected String _rootValueSeparator;
    protected Separators _separators;

    public MinimalPrettyPrinter() {
        this(DEFAULT_ROOT_VALUE_SEPARATOR.toString());
    }

    public MinimalPrettyPrinter(String rootValueSeparator) {
        this._rootValueSeparator = rootValueSeparator;
        this._separators = DEFAULT_SEPARATORS;
    }

    public void setRootValueSeparator(String sep) {
        this._rootValueSeparator = sep;
    }

    public MinimalPrettyPrinter setSeparators(Separators separators) {
        this._separators = separators;
        return this;
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator g) throws IOException {
        if (this._rootValueSeparator != null) {
            g.writeRaw(this._rootValueSeparator);
        }
    }

    @Override
    public void writeStartObject(JsonGenerator g) throws IOException {
        g.writeRaw('{');
    }

    @Override
    public void beforeObjectEntries(JsonGenerator g) throws IOException {
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(this._separators.getObjectFieldValueSeparator());
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator g) throws IOException {
        g.writeRaw(this._separators.getObjectEntrySeparator());
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
        g.writeRaw('}');
    }

    @Override
    public void writeStartArray(JsonGenerator g) throws IOException {
        g.writeRaw('[');
    }

    @Override
    public void beforeArrayValues(JsonGenerator g) throws IOException {
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(this._separators.getArrayValueSeparator());
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        g.writeRaw(']');
    }
}

