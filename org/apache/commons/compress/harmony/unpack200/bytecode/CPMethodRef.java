/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class CPMethodRef
extends CPRef {
    private boolean hashcodeComputed;
    private int cachedHashCode;

    public CPMethodRef(CPClass className, CPNameAndType descriptor, int globalIndex) {
        super((byte)10, className, descriptor, globalIndex);
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.className, this.nameAndType};
    }

    private void generateHashCode() {
        this.hashcodeComputed = true;
        int PRIME = 31;
        int result = 1;
        result = 31 * result + this.className.hashCode();
        this.cachedHashCode = result = 31 * result + this.nameAndType.hashCode();
    }

    @Override
    public int hashCode() {
        if (!this.hashcodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }
}

