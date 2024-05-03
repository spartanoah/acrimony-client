/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPLong
extends CPConstantNumber {
    public CPLong(Long value, int globalIndex) {
        super((byte)5, value, globalIndex);
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeLong(this.getNumber().longValue());
    }

    @Override
    public String toString() {
        return "Long: " + this.getValue();
    }
}

