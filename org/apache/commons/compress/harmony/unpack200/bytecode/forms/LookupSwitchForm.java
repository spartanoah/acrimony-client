/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.SwitchForm;

public class LookupSwitchForm
extends SwitchForm {
    public LookupSwitchForm(int opcode, String name) {
        super(opcode, name);
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int case_count = operandManager.nextCaseCount();
        int default_pc = operandManager.nextLabel();
        int[] case_values = new int[case_count];
        for (int index = 0; index < case_count; ++index) {
            case_values[index] = operandManager.nextCaseValues();
        }
        int[] case_pcs = new int[case_count];
        for (int index = 0; index < case_count; ++index) {
            case_pcs[index] = operandManager.nextLabel();
        }
        int[] labelsArray = new int[case_count + 1];
        labelsArray[0] = default_pc;
        for (int index = 1; index < case_count + 1; ++index) {
            labelsArray[index] = case_pcs[index - 1];
        }
        byteCode.setByteCodeTargets(labelsArray);
        int padLength = 3 - codeLength % 4;
        int rewriteSize = 1 + padLength + 4 + 4 + 4 * case_values.length + 4 * case_pcs.length;
        int[] newRewrite = new int[rewriteSize];
        int rewriteIndex = 0;
        newRewrite[rewriteIndex++] = byteCode.getOpcode();
        for (int index = 0; index < padLength; ++index) {
            newRewrite[rewriteIndex++] = 0;
        }
        newRewrite[rewriteIndex++] = -1;
        newRewrite[rewriteIndex++] = -1;
        newRewrite[rewriteIndex++] = -1;
        newRewrite[rewriteIndex++] = -1;
        int npairsIndex = rewriteIndex;
        this.setRewrite4Bytes(case_values.length, npairsIndex, newRewrite);
        rewriteIndex += 4;
        for (int index = 0; index < case_values.length; ++index) {
            this.setRewrite4Bytes(case_values[index], rewriteIndex, newRewrite);
            rewriteIndex += 4;
            newRewrite[rewriteIndex++] = -1;
            newRewrite[rewriteIndex++] = -1;
            newRewrite[rewriteIndex++] = -1;
            newRewrite[rewriteIndex++] = -1;
        }
        byteCode.setRewrite(newRewrite);
    }
}

