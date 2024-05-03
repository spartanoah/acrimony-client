/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPFloat
extends CPConstantNumber {
    public CPFloat(Float value, int globalIndex) {
        super((byte)4, value, globalIndex);
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeFloat(this.getNumber().floatValue());
    }

    @Override
    public String toString() {
        return "Float: " + this.getValue();
    }
}

