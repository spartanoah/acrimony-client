/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMember;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public class CPField
extends CPMember {
    public CPField(CPUTF8 name, CPUTF8 descriptor, long flags, List attributes) {
        super(name, descriptor, flags, attributes);
    }

    @Override
    public String toString() {
        return "Field: " + this.name + "(" + this.descriptor + ")";
    }
}

