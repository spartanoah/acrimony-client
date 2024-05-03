/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ReferenceForm;

public class ClassRefForm
extends ReferenceForm {
    protected boolean widened;

    public ClassRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public ClassRefForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    @Override
    protected void setNestedEntries(ByteCode byteCode, OperandManager operandManager, int offset) throws Pack200Exception {
        if (offset != 0) {
            super.setNestedEntries(byteCode, operandManager, offset - 1);
            return;
        }
        SegmentConstantPool globalPool = operandManager.globalConstantPool();
        ClassFileEntry[] nested = null;
        nested = new ClassFileEntry[]{globalPool.getClassPoolEntry(operandManager.getCurrentClass())};
        byteCode.setNested(nested);
        byteCode.setNestedPositions(new int[][]{{0, 2}});
    }

    @Override
    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextClassRef();
    }

    @Override
    protected int getPoolID() {
        return 7;
    }
}

