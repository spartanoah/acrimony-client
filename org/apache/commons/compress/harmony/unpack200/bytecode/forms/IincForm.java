/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ByteCodeForm;

public class IincForm
extends ByteCodeForm {
    public IincForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int local = operandManager.nextLocal();
        int constant = operandManager.nextByte();
        byteCode.setOperandBytes(new int[]{local, constant});
    }
}

