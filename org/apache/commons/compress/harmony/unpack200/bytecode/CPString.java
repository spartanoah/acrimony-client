/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstant;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class CPString
extends CPConstant {
    private transient int nameIndex;
    private final CPUTF8 name;
    private boolean hashcodeComputed;
    private int cachedHashCode;

    public CPString(CPUTF8 value, int globalIndex) {
        super((byte)8, value, globalIndex);
        this.name = value;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.nameIndex);
    }

    @Override
    public String toString() {
        return "String: " + this.getValue();
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.nameIndex = pool.indexOf(this.name);
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.name};
    }

    private void generateHashCode() {
        this.hashcodeComputed = true;
        int PRIME = 31;
        int result = 1;
        this.cachedHashCode = result = 31 * result + this.name.hashCode();
    }

    @Override
    public int hashCode() {
        if (!this.hashcodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }
}

