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

public class SourceFileAttribute
extends Attribute {
    private final CPUTF8 name;
    private int nameIndex;
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public SourceFileAttribute(CPUTF8 name) {
        super(attributeName);
        this.name = name;
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
        SourceFileAttribute other = (SourceFileAttribute)obj;
        return !(this.name == null ? other.name != null : !this.name.equals(other.name));
    }

    @Override
    public boolean isSourceFileAttribute() {
        return true;
    }

    @Override
    protected int getLength() {
        return 2;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.getAttributeName(), this.name};
    }

    @Override
    public int hashCode() {
        int PRIME = 31;
        int result = super.hashCode();
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.nameIndex = pool.indexOf(this.name);
    }

    @Override
    public String toString() {
        return "SourceFile: " + this.name;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.nameIndex);
    }
}

