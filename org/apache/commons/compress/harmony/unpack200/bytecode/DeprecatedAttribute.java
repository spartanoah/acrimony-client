/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public class DeprecatedAttribute
extends Attribute {
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public DeprecatedAttribute() {
        super(attributeName);
    }

    @Override
    protected int getLength() {
        return 0;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
    }

    @Override
    public String toString() {
        return "Deprecated Attribute";
    }
}

