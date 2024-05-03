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

public class RuntimeVisibleorInvisibleAnnotationsAttribute
extends AnnotationsAttribute {
    private final int num_annotations;
    private final AnnotationsAttribute.Annotation[] annotations;

    public RuntimeVisibleorInvisibleAnnotationsAttribute(CPUTF8 name, AnnotationsAttribute.Annotation[] annotations) {
        super(name);
        this.num_annotations = annotations.length;
        this.annotations = annotations;
    }

    @Override
    protected int getLength() {
        int length = 2;
        for (int i = 0; i < this.num_annotations; ++i) {
            length += this.annotations[i].getLength();
        }
        return length;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (int i = 0; i < this.annotations.length; ++i) {
            this.annotations[i].resolve(pool);
        }
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        int size = dos.size();
        dos.writeShort(this.num_annotations);
        for (int i = 0; i < this.num_annotations; ++i) {
            this.annotations[i].writeBody(dos);
        }
        if (dos.size() - size != this.getLength()) {
            throw new Error();
        }
    }

    @Override
    public String toString() {
        return this.attributeName.underlyingString() + ": " + this.num_annotations + " annotations";
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<CPUTF8> nested = new ArrayList<CPUTF8>();
        nested.add(this.attributeName);
        for (int i = 0; i < this.annotations.length; ++i) {
            nested.addAll(this.annotations[i].getClassFileEntries());
        }
        ClassFileEntry[] nestedEntries = new ClassFileEntry[nested.size()];
        for (int i = 0; i < nestedEntries.length; ++i) {
            nestedEntries[i] = (ClassFileEntry)nested.get(i);
        }
        return nestedEntries;
    }
}

