/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.Separators;
import java.io.IOException;

public interface PrettyPrinter {
    public static final Separators DEFAULT_SEPARATORS = Separators.createDefaultInstance();
    public static final SerializedString DEFAULT_ROOT_VALUE_SEPARATOR = new SerializedString(" ");

    public void writeRootValueSeparator(JsonGenerator var1) throws IOException;

    public void writeStartObject(JsonGenerator var1) throws IOException;

    public void writeEndObject(JsonGenerator var1, int var2) throws IOException;

    public void writeObjectEntrySeparator(JsonGenerator var1) throws IOException;

    public void writeObjectFieldValueSeparator(JsonGenerator var1) throws IOException;

    public void writeStartArray(JsonGenerator var1) throws IOException;

    public void writeEndArray(JsonGenerator var1, int var2) throws IOException;

    public void writeArrayValueSeparator(JsonGenerator var1) throws IOException;

    public void beforeArrayValues(JsonGenerator var1) throws IOException;

    public void beforeObjectEntries(JsonGenerator var1) throws IOException;
}

