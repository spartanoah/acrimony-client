/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ReferenceForm;

public abstract class SingleByteReferenceForm
extends ReferenceForm {
    protected boolean widened;

    public SingleByteReferenceForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected abstract int getOffset(OperandManager var1);

    @Override
    protected abstract int getPoolID();

    @Override
    protected void setNestedEntries(ByteCode byteCode, OperandManager operandManager, int offset) throws Pack200Exception {
        super.setNestedEntries(byteCode, operandManager, offset);
        if (this.widened) {
            byteCode.setNestedPositions(new int[][]{{0, 2}});
        } else {
            byteCode.setNestedPositions(new int[][]{{0, 1}});
        }
    }

    @Override
    public boolean nestedMustStartClassPool() {
        return !this.widened;
    }
}

