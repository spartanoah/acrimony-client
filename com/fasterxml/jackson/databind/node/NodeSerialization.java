/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.node.InternalNodeMapper;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

class NodeSerialization
implements Serializable,
Externalizable {
    private static final long serialVersionUID = 1L;
    public byte[] json;

    public NodeSerialization() {
    }

    public NodeSerialization(byte[] b) {
        this.json = b;
    }

    protected Object readResolve() {
        try {
            return InternalNodeMapper.bytesToNode(this.json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to JDK deserialize `JsonNode` value: " + e.getMessage(), e);
        }
    }

    public static NodeSerialization from(Object o) {
        try {
            return new NodeSerialization(InternalNodeMapper.valueToBytes(o));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to JDK serialize `" + o.getClass().getSimpleName() + "` value: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.json.length);
        out.write(this.json);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int len = in.readInt();
        this.json = new byte[len];
        in.readFully(this.json, 0, len);
    }
}

