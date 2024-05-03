/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public abstract class CPRef
extends ConstantPoolEntry {
    CPClass className;
    transient int classNameIndex;
    protected CPNameAndType nameAndType;
    transient int nameAndTypeIndex;
    protected String cachedToString;

    public CPRef(byte type, CPClass className, CPNameAndType descriptor, int globalIndex) {
        super(type, globalIndex);
        this.className = className;
        this.nameAndType = descriptor;
        if (descriptor == null || className == null) {
            throw new NullPointerException("Null arguments are not allowed");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        CPRef other = (CPRef)obj;
        if (!this.className.equals(other.className)) {
            return false;
        }
        return this.nameAndType.equals(other.nameAndType);
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ClassFileEntry[] entries = new ClassFileEntry[]{this.className, this.nameAndType};
        return entries;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.nameAndTypeIndex = pool.indexOf(this.nameAndType);
        this.classNameIndex = pool.indexOf(this.className);
    }

    @Override
    public String toString() {
        if (this.cachedToString == null) {
            String type = this.getTag() == 9 ? "FieldRef" : (this.getTag() == 10 ? "MethoddRef" : (this.getTag() == 11 ? "InterfaceMethodRef" : "unknown"));
            this.cachedToString = type + ": " + this.className + "#" + this.nameAndType;
        }
        return this.cachedToString;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.classNameIndex);
        dos.writeShort(this.nameAndTypeIndex);
    }
}

