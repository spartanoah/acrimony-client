/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.BCIRenumberedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class LineNumberTableAttribute
extends BCIRenumberedAttribute {
    private final int line_number_table_length;
    private final int[] start_pcs;
    private final int[] line_numbers;
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public LineNumberTableAttribute(int line_number_table_length, int[] start_pcs, int[] line_numbers) {
        super(attributeName);
        this.line_number_table_length = line_number_table_length;
        this.start_pcs = start_pcs;
        this.line_numbers = line_numbers;
    }

    @Override
    protected int getLength() {
        return 2 + 4 * this.line_number_table_length;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.line_number_table_length);
        for (int i = 0; i < this.line_number_table_length; ++i) {
            dos.writeShort(this.start_pcs[i]);
            dos.writeShort(this.line_numbers[i]);
        }
    }

    @Override
    public String toString() {
        return "LineNumberTable: " + this.line_number_table_length + " lines";
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.getAttributeName()};
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
    }

    @Override
    protected int[] getStartPCs() {
        return this.start_pcs;
    }
}

