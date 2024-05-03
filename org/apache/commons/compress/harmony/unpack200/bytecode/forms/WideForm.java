/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.VariableInstructionForm;

public class WideForm
extends VariableInstructionForm {
    public WideForm(int opcode, String name) {
        super(opcode, name);
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int instruction = operandManager.nextWideByteCode();
        if (instruction == 132) {
            this.setByteCodeOperandsFormat2(instruction, byteCode, operandManager, codeLength);
        } else {
            this.setByteCodeOperandsFormat1(instruction, byteCode, operandManager, codeLength);
        }
    }

    protected void setByteCodeOperandsFormat1(int instruction, ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int local = operandManager.nextLocal();
        int[] newRewrite = new int[4];
        int rewriteIndex = 0;
        newRewrite[rewriteIndex++] = byteCode.getOpcode();
        newRewrite[rewriteIndex++] = instruction;
        this.setRewrite2Bytes(local, rewriteIndex, newRewrite);
        rewriteIndex += 2;
        byteCode.setRewrite(newRewrite);
    }

    protected void setByteCodeOperandsFormat2(int instruction, ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int local = operandManager.nextLocal();
        int constWord = operandManager.nextShort();
        int[] newRewrite = new int[6];
        int rewriteIndex = 0;
        newRewrite[rewriteIndex++] = byteCode.getOpcode();
        newRewrite[rewriteIndex++] = instruction;
        this.setRewrite2Bytes(local, rewriteIndex, newRewrite);
        this.setRewrite2Bytes(constWord, rewriteIndex += 2, newRewrite);
        rewriteIndex += 2;
        byteCode.setRewrite(newRewrite);
    }
}

