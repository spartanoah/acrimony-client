/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public abstract class StdConverter<IN, OUT>
implements Converter<IN, OUT> {
    @Override
    public abstract OUT convert(IN var1);

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return this._findConverterType(typeFactory).containedType(0);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return this._findConverterType(typeFactory).containedType(1);
    }

    protected JavaType _findConverterType(TypeFactory tf) {
        JavaType thisType = tf.constructType(this.getClass());
        JavaType convType = thisType.findSuperType(Converter.class);
        if (convType == null || convType.containedTypeCount() < 2) {
            throw new IllegalStateException("Cannot find OUT type parameter for Converter of type " + this.getClass().getName());
        }
        return convType;
    }
}

