/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;

public class POJONode
extends ValueNode {
    private static final long serialVersionUID = 2L;
    protected final Object _value;

    public POJONode(Object v) {
        this._value = v;
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.POJO;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_EMBEDDED_OBJECT;
    }

    @Override
    public byte[] binaryValue() throws IOException {
        if (this._value instanceof byte[]) {
            return (byte[])this._value;
        }
        return super.binaryValue();
    }

    @Override
    public String asText() {
        return this._value == null ? "null" : this._value.toString();
    }

    @Override
    public String asText(String defaultValue) {
        return this._value == null ? defaultValue : this._value.toString();
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        if (this._value != null && this._value instanceof Boolean) {
            return (Boolean)this._value;
        }
        return defaultValue;
    }

    @Override
    public int asInt(int defaultValue) {
        if (this._value instanceof Number) {
            return ((Number)this._value).intValue();
        }
        return defaultValue;
    }

    @Override
    public long asLong(long defaultValue) {
        if (this._value instanceof Number) {
            return ((Number)this._value).longValue();
        }
        return defaultValue;
    }

    @Override
    public double asDouble(double defaultValue) {
        if (this._value instanceof Number) {
            return ((Number)this._value).doubleValue();
        }
        return defaultValue;
    }

    @Override
    public final void serialize(JsonGenerator gen, SerializerProvider ctxt) throws IOException {
        if (this._value == null) {
            ctxt.defaultSerializeNull(gen);
        } else if (this._value instanceof JsonSerializable) {
            ((JsonSerializable)this._value).serialize(gen, ctxt);
        } else {
            ctxt.defaultSerializeValue(this._value, gen);
        }
    }

    public Object getPojo() {
        return this._value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof POJONode) {
            return this._pojoEquals((POJONode)o);
        }
        return false;
    }

    protected boolean _pojoEquals(POJONode other) {
        if (this._value == null) {
            return other._value == null;
        }
        return this._value.equals(other._value);
    }

    @Override
    public int hashCode() {
        return this._value.hashCode();
    }
}

