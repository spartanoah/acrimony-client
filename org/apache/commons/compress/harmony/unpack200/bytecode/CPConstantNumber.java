/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstant;

public abstract class CPConstantNumber
extends CPConstant {
    public CPConstantNumber(byte tag, Object value, int globalIndex) {
        super(tag, value, globalIndex);
    }

    protected Number getNumber() {
        return (Number)this.getValue();
    }
}

