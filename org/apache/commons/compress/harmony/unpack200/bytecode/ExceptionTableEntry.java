/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;

public class ExceptionTableEntry {
    private final int startPC;
    private final int endPC;
    private final int handlerPC;
    private final CPClass catchType;
    private int startPcRenumbered;
    private int endPcRenumbered;
    private int handlerPcRenumbered;
    private int catchTypeIndex;

    public ExceptionTableEntry(int startPC, int endPC, int handlerPC, CPClass catchType) {
        this.startPC = startPC;
        this.endPC = endPC;
        this.handlerPC = handlerPC;
        this.catchType = catchType;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeShort(this.startPcRenumbered);
        dos.writeShort(this.endPcRenumbered);
        dos.writeShort(this.handlerPcRenumbered);
        dos.writeShort(this.catchTypeIndex);
    }

    public void renumber(List byteCodeOffsets) {
        this.startPcRenumbered = (Integer)byteCodeOffsets.get(this.startPC);
        int endPcIndex = this.startPC + this.endPC;
        this.endPcRenumbered = (Integer)byteCodeOffsets.get(endPcIndex);
        int handlerPcIndex = endPcIndex + this.handlerPC;
        this.handlerPcRenumbered = (Integer)byteCodeOffsets.get(handlerPcIndex);
    }

    public CPClass getCatchType() {
        return this.catchType;
    }

    public void resolve(ClassConstantPool pool) {
        if (this.catchType == null) {
            this.catchTypeIndex = 0;
            return;
        }
        this.catchType.resolve(pool);
        this.catchTypeIndex = pool.indexOf(this.catchType);
    }
}

