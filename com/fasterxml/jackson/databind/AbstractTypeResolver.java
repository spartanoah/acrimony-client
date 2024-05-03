/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;

public abstract class AbstractTypeResolver {
    public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
        return null;
    }

    @Deprecated
    public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
        return null;
    }

    public JavaType resolveAbstractType(DeserializationConfig config, BeanDescription typeDesc) {
        return null;
    }
}

