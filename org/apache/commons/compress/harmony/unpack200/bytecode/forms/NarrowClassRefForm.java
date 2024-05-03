/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ClassRefForm;

public class NarrowClassRefForm
extends ClassRefForm {
    public NarrowClassRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public NarrowClassRefForm(int opcode, String name, int[] rewrite, boolean widened) {
        super(opcode, name, rewrite, widened);
    }

    @Override
    protected void setNestedEntries(ByteCode byteCode, OperandManager operandManager, int offset) throws Pack200Exception {
        super.setNestedEntries(byteCode, operandManager, offset);
        if (!this.widened) {
            byteCode.setNestedPositions(new int[][]{{0, 1}});
        }
    }

    @Override
    public boolean nestedMustStartClassPool() {
        return !this.widened;
    }
}

