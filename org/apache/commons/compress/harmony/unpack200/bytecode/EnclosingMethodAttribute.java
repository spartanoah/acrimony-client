/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class EnclosingMethodAttribute
extends Attribute {
    private int class_index;
    private int method_index;
    private final CPClass cpClass;
    private final CPNameAndType method;
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public EnclosingMethodAttribute(CPClass cpClass, CPNameAndType method) {
        super(attributeName);
        this.cpClass = cpClass;
        this.method = method;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        if (this.method != null) {
            return new ClassFileEntry[]{attributeName, this.cpClass, this.method};
        }
        return new ClassFileEntry[]{attributeName, this.cpClass};
    }

    @Override
    protected int getLength() {
        return 4;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.cpClass.resolve(pool);
        this.class_index = pool.indexOf(this.cpClass);
        if (this.method != null) {
            this.method.resolve(pool);
            this.method_index = pool.indexOf(this.method);
        } else {
            this.method_index = 0;
        }
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.class_index);
        dos.writeShort(this.method_index);
    }

    @Override
    public String toString() {
        return "EnclosingMethod";
    }
}

