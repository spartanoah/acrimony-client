/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPInterfaceMethodRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ReferenceForm;

public class IMethodRefForm
extends ReferenceForm {
    public IMethodRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextIMethodRef();
    }

    @Override
    protected int getPoolID() {
        return 12;
    }

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int count;
        super.setByteCodeOperands(byteCode, operandManager, codeLength);
        byteCode.getRewrite()[3] = count = ((CPInterfaceMethodRef)byteCode.getNestedClassFileEntries()[0]).invokeInterfaceCount();
    }
}

