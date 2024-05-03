/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;

public class JSONPObject
implements JsonSerializable {
    protected final String _function;
    protected final Object _value;
    protected final JavaType _serializationType;

    public JSONPObject(String function, Object value) {
        this(function, value, null);
    }

    public JSONPObject(String function, Object value, JavaType asType) {
        this._function = function;
        this._value = value;
        this._serializationType = asType;
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        this.serialize(gen, provider);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeRaw(this._function);
        gen.writeRaw('(');
        if (this._value == null) {
            provider.defaultSerializeNull(gen);
        } else {
            boolean override;
            boolean bl = override = gen.getCharacterEscapes() == null;
            if (override) {
                gen.setCharacterEscapes(JsonpCharacterEscapes.instance());
            }
            try {
                if (this._serializationType != null) {
                    provider.findTypedValueSerializer(this._serializationType, true, null).serialize(this._value, gen, provider);
                } else {
                    provider.findTypedValueSerializer(this._value.getClass(), true, null).serialize(this._value, gen, provider);
                }
            } finally {
                if (override) {
                    gen.setCharacterEscapes(null);
                }
            }
        }
        gen.writeRaw(')');
    }

    public String getFunction() {
        return this._function;
    }

    public Object getValue() {
        return this._value;
    }

    public JavaType getSerializationType() {
        return this._serializationType;
    }
}

