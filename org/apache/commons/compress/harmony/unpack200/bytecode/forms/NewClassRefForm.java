/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ClassRefForm;

public class NewClassRefForm
extends ClassRefForm {
    public NewClassRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        ClassFileEntry[] nested = null;
        int offset = this.getOffset(operandManager);
        if (offset == 0) {
            SegmentConstantPool globalPool = operandManager.globalConstantPool();
            nested = new ClassFileEntry[]{globalPool.getClassPoolEntry(operandManager.getCurrentClass())};
            byteCode.setNested(nested);
            byteCode.setNestedPositions(new int[][]{{0, 2}});
        } else {
            try {
                this.setNestedEntries(byteCode, operandManager, offset);
            } catch (Pack200Exception ex) {
                throw new Error("Got a pack200 exception. What to do?");
            }
        }
        operandManager.setNewClass(((CPClass)byteCode.getNestedClassFileEntries()[0]).getName());
    }
}

