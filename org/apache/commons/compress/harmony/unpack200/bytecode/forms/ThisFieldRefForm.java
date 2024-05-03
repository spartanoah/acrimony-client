/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ClassSpecificReferenceForm;

public class ThisFieldRefForm
extends ClassSpecificReferenceForm {
    public ThisFieldRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextThisFieldRef();
    }

    @Override
    protected int getPoolID() {
        return 10;
    }

    @Override
    protected String context(OperandManager operandManager) {
        return operandManager.getCurrentClass();
    }
}

