/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CodeAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ByteCodeForm;

public class LabelForm
extends ByteCodeForm {
    protected boolean widened;

    public LabelForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    public LabelForm(int opcode, String name, int[] rewrite, boolean widened) {
        this(opcode, name, rewrite);
        this.widened = widened;
    }

    @Override
    public void fixUpByteCodeTargets(ByteCode byteCode, CodeAttribute codeAttribute) {
        int originalTarget = byteCode.getByteCodeTargets()[0];
        int sourceIndex = byteCode.getByteCodeIndex();
        int absoluteInstructionTargetIndex = sourceIndex + originalTarget;
        int targetValue = (Integer)codeAttribute.byteCodeOffsets.get(absoluteInstructionTargetIndex);
        int sourceValue = (Integer)codeAttribute.byteCodeOffsets.get(sourceIndex);
        byteCode.setOperandSigned2Bytes(targetValue - sourceValue, 0);
        if (this.widened) {
            byteCode.setNestedPositions(new int[][]{{0, 4}});
        } else {
            byteCode.setNestedPositions(new int[][]{{0, 2}});
        }
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        byteCode.setByteCodeTargets(new int[]{operandManager.nextLabel()});
    }
}

