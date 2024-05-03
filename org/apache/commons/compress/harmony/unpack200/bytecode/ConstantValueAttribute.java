/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class ConstantValueAttribute
extends Attribute {
    private int constantIndex;
    private final ClassFileEntry entry;
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public ConstantValueAttribute(ClassFileEntry entry) {
        super(attributeName);
        if (entry == null) {
            throw new NullPointerException();
        }
        this.entry = entry;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ConstantValueAttribute other = (ConstantValueAttribute)obj;
        return !(this.entry == null ? other.entry != null : !this.entry.equals(other.entry));
    }

    @Override
    protected int getLength() {
        return 2;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.getAttributeName(), this.entry};
    }

    @Override
    public int hashCode() {
        int PRIME = 31;
        int result = super.hashCode();
        result = 31 * result + (this.entry == null ? 0 : this.entry.hashCode());
        return result;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.entry.resolve(pool);
        this.constantIndex = pool.indexOf(this.entry);
    }

    @Override
    public String toString() {
        return "Constant:" + this.entry;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.constantIndex);
    }
}

