/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public abstract class CPConstant
extends ConstantPoolEntry {
    private final Object value;

    public CPConstant(byte tag, Object value, int globalIndex) {
        super(tag, globalIndex);
        this.value = value;
        if (value == null) {
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
        CPConstant other = (CPConstant)obj;
        return !(this.value == null ? other.value != null : !this.value.equals(other.value));
    }

    @Override
    public int hashCode() {
        int PRIME = 31;
        int result = 1;
        result = 31 * result + (this.value == null ? 0 : this.value.hashCode());
        return result;
    }

    protected Object getValue() {
        return this.value;
    }
}

