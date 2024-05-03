/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ByteCodeForm;

public abstract class VariableInstructionForm
extends ByteCodeForm {
    public VariableInstructionForm(int opcode, String name) {
        super(opcode, name);
    }

    public void setRewrite4Bytes(int operand, int[] rewrite) {
        int firstOperandPosition = -1;
        for (int index = 0; index < rewrite.length - 3; ++index) {
            if (rewrite[index] != -1 || rewrite[index + 1] != -1 || rewrite[index + 2] != -1 || rewrite[index + 3] != -1) continue;
            firstOperandPosition = index;
            break;
        }
        this.setRewrite4Bytes(operand, firstOperandPosition, rewrite);
    }

    public void setRewrite4Bytes(int operand, int absPosition, int[] rewrite) {
        if (absPosition < 0) {
            throw new Error("Trying to rewrite " + this + " but there is no room for 4 bytes");
        }
        int byteCodeRewriteLength = rewrite.length;
        if (absPosition + 3 > byteCodeRewriteLength) {
            throw new Error("Trying to rewrite " + this + " with an int at position " + absPosition + " but this won't fit in the rewrite array");
        }
        rewrite[absPosition] = (0xFF000000 & operand) >> 24;
        rewrite[absPosition + 1] = (0xFF0000 & operand) >> 16;
        rewrite[absPosition + 2] = (0xFF00 & operand) >> 8;
        rewrite[absPosition + 3] = 0xFF & operand;
    }

    public void setRewrite2Bytes(int operand, int absPosition, int[] rewrite) {
        if (absPosition < 0) {
            throw new Error("Trying to rewrite " + this + " but there is no room for 4 bytes");
        }
        int byteCodeRewriteLength = rewrite.length;
        if (absPosition + 1 > byteCodeRewriteLength) {
            throw new Error("Trying to rewrite " + this + " with an int at position " + absPosition + " but this won't fit in the rewrite array");
        }
        rewrite[absPosition] = (0xFF00 & operand) >> 8;
        rewrite[absPosition + 1] = 0xFF & operand;
    }
}

