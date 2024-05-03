/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public abstract class BCIRenumberedAttribute
extends Attribute {
    protected boolean renumbered;

    @Override
    public boolean hasBCIRenumbering() {
        return true;
    }

    public BCIRenumberedAttribute(CPUTF8 attributeName) {
        super(attributeName);
    }

    @Override
    protected abstract int getLength();

    @Override
    protected abstract void writeBody(DataOutputStream var1) throws IOException;

    @Override
    public abstract String toString();

    protected abstract int[] getStartPCs();

    public void renumber(List byteCodeOffsets) throws Pack200Exception {
        if (this.renumbered) {
            throw new Error("Trying to renumber a line number table that has already been renumbered");
        }
        this.renumbered = true;
        int[] startPCs = this.getStartPCs();
        for (int index = 0; index < startPCs.length; ++index) {
            startPCs[index] = (Integer)byteCodeOffsets.get(startPCs[index]);
        }
    }
}

