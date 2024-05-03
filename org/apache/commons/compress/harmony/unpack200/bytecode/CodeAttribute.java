/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.BCIRenumberedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ExceptionTableEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTypeTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;

public class CodeAttribute
extends BCIRenumberedAttribute {
    public List attributes = new ArrayList();
    public List byteCodeOffsets = new ArrayList();
    public List byteCodes = new ArrayList();
    public int codeLength;
    public List exceptionTable;
    public int maxLocals;
    public int maxStack;
    private static CPUTF8 attributeName;

    public CodeAttribute(int maxStack, int maxLocals, byte[] codePacked, Segment segment, OperandManager operandManager, List exceptionTable) {
        super(attributeName);
        ByteCode byteCode;
        int i;
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.codeLength = 0;
        this.exceptionTable = exceptionTable;
        this.byteCodeOffsets.add(0);
        int byteCodeIndex = 0;
        for (i = 0; i < codePacked.length; ++i) {
            byteCode = ByteCode.getByteCode(codePacked[i] & 0xFF);
            byteCode.setByteCodeIndex(byteCodeIndex);
            ++byteCodeIndex;
            byteCode.extractOperands(operandManager, segment, this.codeLength);
            this.byteCodes.add(byteCode);
            this.codeLength += byteCode.getLength();
            int lastBytecodePosition = (Integer)this.byteCodeOffsets.get(this.byteCodeOffsets.size() - 1);
            if (byteCode.hasMultipleByteCodes()) {
                this.byteCodeOffsets.add(lastBytecodePosition + 1);
                ++byteCodeIndex;
            }
            if (i < codePacked.length - 1) {
                this.byteCodeOffsets.add(lastBytecodePosition + byteCode.getLength());
            }
            if (byteCode.getOpcode() != 196) continue;
            ++i;
        }
        for (i = 0; i < this.byteCodes.size(); ++i) {
            byteCode = (ByteCode)this.byteCodes.get(i);
            byteCode.applyByteCodeTargetFixup(this);
        }
    }

    @Override
    protected int getLength() {
        int attributesSize = 0;
        for (int it = 0; it < this.attributes.size(); ++it) {
            Attribute attribute = (Attribute)this.attributes.get(it);
            attributesSize += attribute.getLengthIncludingHeader();
        }
        return 8 + this.codeLength + 2 + this.exceptionTable.size() * 8 + 2 + attributesSize;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<ConstantPoolEntry> nestedEntries = new ArrayList<ConstantPoolEntry>(this.attributes.size() + this.byteCodes.size() + 10);
        nestedEntries.add(this.getAttributeName());
        nestedEntries.addAll(this.byteCodes);
        nestedEntries.addAll(this.attributes);
        for (int iter = 0; iter < this.exceptionTable.size(); ++iter) {
            ExceptionTableEntry entry = (ExceptionTableEntry)this.exceptionTable.get(iter);
            CPClass catchType = entry.getCatchType();
            if (catchType == null) continue;
            nestedEntries.add(catchType);
        }
        ClassFileEntry[] nestedEntryArray = new ClassFileEntry[nestedEntries.size()];
        nestedEntries.toArray(nestedEntryArray);
        return nestedEntryArray;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        int it;
        super.resolve(pool);
        for (it = 0; it < this.attributes.size(); ++it) {
            Attribute attribute = (Attribute)this.attributes.get(it);
            attribute.resolve(pool);
        }
        for (it = 0; it < this.byteCodes.size(); ++it) {
            ByteCode byteCode = (ByteCode)this.byteCodes.get(it);
            byteCode.resolve(pool);
        }
        for (it = 0; it < this.exceptionTable.size(); ++it) {
            ExceptionTableEntry entry = (ExceptionTableEntry)this.exceptionTable.get(it);
            entry.resolve(pool);
        }
    }

    @Override
    public String toString() {
        return "Code: " + this.getLength() + " bytes";
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        int it;
        dos.writeShort(this.maxStack);
        dos.writeShort(this.maxLocals);
        dos.writeInt(this.codeLength);
        for (it = 0; it < this.byteCodes.size(); ++it) {
            ByteCode byteCode = (ByteCode)this.byteCodes.get(it);
            byteCode.write(dos);
        }
        dos.writeShort(this.exceptionTable.size());
        for (it = 0; it < this.exceptionTable.size(); ++it) {
            ExceptionTableEntry entry = (ExceptionTableEntry)this.exceptionTable.get(it);
            entry.write(dos);
        }
        dos.writeShort(this.attributes.size());
        for (it = 0; it < this.attributes.size(); ++it) {
            Attribute attribute = (Attribute)this.attributes.get(it);
            attribute.write(dos);
        }
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        if (attribute instanceof LocalVariableTableAttribute) {
            ((LocalVariableTableAttribute)attribute).setCodeLength(this.codeLength);
        }
        if (attribute instanceof LocalVariableTypeTableAttribute) {
            ((LocalVariableTypeTableAttribute)attribute).setCodeLength(this.codeLength);
        }
    }

    @Override
    protected int[] getStartPCs() {
        return null;
    }

    @Override
    public void renumber(List byteCodeOffsets) {
        for (int iter = 0; iter < this.exceptionTable.size(); ++iter) {
            ExceptionTableEntry entry = (ExceptionTableEntry)this.exceptionTable.get(iter);
            entry.renumber(byteCodeOffsets);
        }
    }

    public static void setAttributeName(CPUTF8 attributeName) {
        CodeAttribute.attributeName = attributeName;
    }
}

