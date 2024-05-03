/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.SingleByteReferenceForm;

public class IntRefForm
extends SingleByteReferenceForm {
    public IntRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public IntRefForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    @Override
    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextIntRef();
    }

    @Override
    protected int getPoolID() {
        return 2;
    }
}

