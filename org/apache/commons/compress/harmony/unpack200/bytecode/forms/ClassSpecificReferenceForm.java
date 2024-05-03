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

public abstract class ClassSpecificReferenceForm
extends ReferenceForm {
    public ClassSpecificReferenceForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected abstract int getOffset(OperandManager var1);

    @Override
    protected abstract int getPoolID();

    protected abstract String context(OperandManager var1);

    @Override
    protected void setNestedEntries(ByteCode byteCode, OperandManager operandManager, int offset) throws Pack200Exception {
        SegmentConstantPool globalPool = operandManager.globalConstantPool();
        ClassFileEntry[] nested = null;
        nested = new ClassFileEntry[]{globalPool.getClassSpecificPoolEntry(this.getPoolID(), offset, this.context(operandManager))};
        byteCode.setNested(nested);
        byteCode.setNestedPositions(new int[][]{{0, 2}});
    }
}

