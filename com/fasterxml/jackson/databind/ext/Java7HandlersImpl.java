/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ext.Java7Handlers;
import com.fasterxml.jackson.databind.ext.NioPathDeserializer;
import com.fasterxml.jackson.databind.ext.NioPathSerializer;
import java.nio.file.Path;

public class Java7HandlersImpl
extends Java7Handlers {
    private final Class<?> _pathClass = Path.class;

    @Override
    public Class<?> getClassJavaNioFilePath() {
        return this._pathClass;
    }

    @Override
    public JsonDeserializer<?> getDeserializerForJavaNioFilePath(Class<?> rawType) {
        if (rawType == this._pathClass) {
            return new NioPathDeserializer();
        }
        return null;
    }

    @Override
    public JsonSerializer<?> getSerializerForJavaNioFilePath(Class<?> rawType) {
        if (this._pathClass.isAssignableFrom(rawType)) {
            return new NioPathSerializer();
        }
        return null;
    }
}

