/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.compress.harmony.unpack200.bytecode.AnnotationsAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class AnnotationDefaultAttribute
extends AnnotationsAttribute {
    private final AnnotationsAttribute.ElementValue element_value;
    private static CPUTF8 attributeName;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public AnnotationDefaultAttribute(AnnotationsAttribute.ElementValue element_value) {
        super(attributeName);
        this.element_value = element_value;
    }

    @Override
    protected int getLength() {
        return this.element_value.getLength();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        this.element_value.writeBody(dos);
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.element_value.resolve(pool);
    }

    @Override
    public String toString() {
        return "AnnotationDefault: " + this.element_value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<CPUTF8> nested = new ArrayList<CPUTF8>();
        nested.add(attributeName);
        nested.addAll(this.element_value.getClassFileEntries());
        ClassFileEntry[] nestedEntries = new ClassFileEntry[nested.size()];
        for (int i = 0; i < nestedEntries.length; ++i) {
            nestedEntries[i] = (ClassFileEntry)nested.get(i);
        }
        return nestedEntries;
    }
}

