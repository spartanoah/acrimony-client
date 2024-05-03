/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPInteger
extends CPConstantNumber {
    public CPInteger(Integer value, int globalIndex) {
        super((byte)3, value, globalIndex);
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeInt(this.getNumber().intValue());
    }

    @Override
    public String toString() {
        return "Integer: " + this.getValue();
    }
}

