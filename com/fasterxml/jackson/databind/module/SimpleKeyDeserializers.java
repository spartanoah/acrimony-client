/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.type.ClassKey;
import java.io.Serializable;
import java.util.HashMap;

public class SimpleKeyDeserializers
implements KeyDeserializers,
Serializable {
    private static final long serialVersionUID = 1L;
    protected HashMap<ClassKey, KeyDeserializer> _classMappings = null;

    public SimpleKeyDeserializers addDeserializer(Class<?> forClass, KeyDeserializer deser) {
        if (this._classMappings == null) {
            this._classMappings = new HashMap();
        }
        this._classMappings.put(new ClassKey(forClass), deser);
        return this;
    }

    @Override
    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
        if (this._classMappings == null) {
            return null;
        }
        return this._classMappings.get(new ClassKey(type.getRawClass()));
    }
}

