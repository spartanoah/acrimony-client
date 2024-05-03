/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMember;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public class CPMethod
extends CPMember {
    private boolean hashcodeComputed;
    private int cachedHashCode;

    public CPMethod(CPUTF8 name, CPUTF8 descriptor, long flags, List attributes) {
        super(name, descriptor, flags, attributes);
    }

    @Override
    public String toString() {
        return "Method: " + this.name + "(" + this.descriptor + ")";
    }

    private void generateHashCode() {
        this.hashcodeComputed = true;
        int PRIME = 31;
        int result = 1;
        result = 31 * result + this.name.hashCode();
        this.cachedHashCode = result = 31 * result + this.descriptor.hashCode();
    }

    @Override
    public int hashCode() {
        if (!this.hashcodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }
}

