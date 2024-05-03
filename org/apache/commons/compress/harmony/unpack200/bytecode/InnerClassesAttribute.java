/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class InnerClassesAttribute
extends Attribute {
    private static CPUTF8 attributeName;
    private final List innerClasses = new ArrayList();
    private final List nestedClassFileEntries = new ArrayList();

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public InnerClassesAttribute(String name) {
        super(attributeName);
        this.nestedClassFileEntries.add(this.getAttributeName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        InnerClassesAttribute other = (InnerClassesAttribute)obj;
        return !(this.getAttributeName() == null ? other.getAttributeName() != null : !this.getAttributeName().equals(other.getAttributeName()));
    }

    @Override
    protected int getLength() {
        return 2 + 8 * this.innerClasses.size();
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ClassFileEntry[] result = new ClassFileEntry[this.nestedClassFileEntries.size()];
        for (int index = 0; index < result.length; ++index) {
            result[index] = (ClassFileEntry)this.nestedClassFileEntries.get(index);
        }
        return result;
    }

    @Override
    public int hashCode() {
        int PRIME = 31;
        int result = super.hashCode();
        result = 31 * result + (this.getAttributeName() == null ? 0 : this.getAttributeName().hashCode());
        return result;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (int it = 0; it < this.innerClasses.size(); ++it) {
            InnerClassesEntry entry = (InnerClassesEntry)this.innerClasses.get(it);
            entry.resolve(pool);
        }
    }

    @Override
    public String toString() {
        return "InnerClasses: " + this.getAttributeName();
    }

    @Override
    protected void doWrite(DataOutputStream dos) throws IOException {
        super.doWrite(dos);
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.innerClasses.size());
        for (int it = 0; it < this.innerClasses.size(); ++it) {
            InnerClassesEntry entry = (InnerClassesEntry)this.innerClasses.get(it);
            entry.write(dos);
        }
    }

    public void addInnerClassesEntry(CPClass innerClass, CPClass outerClass, CPUTF8 innerName, int flags) {
        if (innerClass != null) {
            this.nestedClassFileEntries.add(innerClass);
        }
        if (outerClass != null) {
            this.nestedClassFileEntries.add(outerClass);
        }
        if (innerName != null) {
            this.nestedClassFileEntries.add(innerName);
        }
        this.addInnerClassesEntry(new InnerClassesEntry(innerClass, outerClass, innerName, flags));
    }

    private void addInnerClassesEntry(InnerClassesEntry innerClassesEntry) {
        this.innerClasses.add(innerClassesEntry);
    }

    private static class InnerClassesEntry {
        CPClass inner_class_info;
        CPClass outer_class_info;
        CPUTF8 inner_class_name;
        int inner_class_info_index = -1;
        int outer_class_info_index = -1;
        int inner_name_index = -1;
        int inner_class_access_flags = -1;

        public InnerClassesEntry(CPClass innerClass, CPClass outerClass, CPUTF8 innerName, int flags) {
            this.inner_class_info = innerClass;
            this.outer_class_info = outerClass;
            this.inner_class_name = innerName;
            this.inner_class_access_flags = flags;
        }

        public void resolve(ClassConstantPool pool) {
            if (this.inner_class_info != null) {
                this.inner_class_info.resolve(pool);
                this.inner_class_info_index = pool.indexOf(this.inner_class_info);
            } else {
                this.inner_class_info_index = 0;
            }
            if (this.inner_class_name != null) {
                this.inner_class_name.resolve(pool);
                this.inner_name_index = pool.indexOf(this.inner_class_name);
            } else {
                this.inner_name_index = 0;
            }
            if (this.outer_class_info != null) {
                this.outer_class_info.resolve(pool);
                this.outer_class_info_index = pool.indexOf(this.outer_class_info);
            } else {
                this.outer_class_info_index = 0;
            }
        }

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(this.inner_class_info_index);
            dos.writeShort(this.outer_class_info_index);
            dos.writeShort(this.inner_name_index);
            dos.writeShort(this.inner_class_access_flags);
        }
    }
}

