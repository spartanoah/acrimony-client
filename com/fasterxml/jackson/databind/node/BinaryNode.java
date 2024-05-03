/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;
import java.util.Arrays;

public class BinaryNode
extends ValueNode {
    private static final long serialVersionUID = 2L;
    static final BinaryNode EMPTY_BINARY_NODE = new BinaryNode(new byte[0]);
    protected final byte[] _data;

    public BinaryNode(byte[] data) {
        this._data = data;
    }

    public BinaryNode(byte[] data, int offset, int length) {
        if (offset == 0 && length == data.length) {
            this._data = data;
        } else {
            this._data = new byte[length];
            System.arraycopy(data, offset, this._data, 0, length);
        }
    }

    public static BinaryNode valueOf(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data);
    }

    public static BinaryNode valueOf(byte[] data, int offset, int length) {
        if (data == null) {
            return null;
        }
        if (length == 0) {
            return EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data, offset, length);
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.BINARY;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_EMBEDDED_OBJECT;
    }

    @Override
    public byte[] binaryValue() {
        return this._data;
    }

    @Override
    public String asText() {
        return Base64Variants.getDefaultVariant().encode(this._data, false);
    }

    @Override
    public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeBinary(provider.getConfig().getBase64Variant(), this._data, 0, this._data.length);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof BinaryNode)) {
            return false;
        }
        return Arrays.equals(((BinaryNode)o)._data, this._data);
    }

    @Override
    public int hashCode() {
        return this._data == null ? -1 : this._data.length;
    }
}

