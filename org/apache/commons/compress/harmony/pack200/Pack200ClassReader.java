/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassReader
 */
package org.apache.commons.compress.harmony.pack200;

import org.objectweb.asm.ClassReader;

public class Pack200ClassReader
extends ClassReader {
    private boolean lastConstantHadWideIndex;
    private int lastUnsignedShort;
    private boolean anySyntheticAttributes;
    private String fileName;

    public Pack200ClassReader(byte[] b) {
        super(b);
    }

    public int readUnsignedShort(int index) {
        int unsignedShort = super.readUnsignedShort(index);
        this.lastUnsignedShort = this.b[index - 1] == 19 ? unsignedShort : Short.MIN_VALUE;
        return unsignedShort;
    }

    public Object readConst(int item, char[] buf) {
        this.lastConstantHadWideIndex = item == this.lastUnsignedShort;
        return super.readConst(item, buf);
    }

    public String readUTF8(int arg0, char[] arg1) {
        String utf8 = super.readUTF8(arg0, arg1);
        if (!this.anySyntheticAttributes && "Synthetic".equals(utf8)) {
            this.anySyntheticAttributes = true;
        }
        return utf8;
    }

    public boolean lastConstantHadWideIndex() {
        return this.lastConstantHadWideIndex;
    }

    public boolean hasSyntheticAttributes() {
        return this.anySyntheticAttributes;
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public String getFileName() {
        return this.fileName;
    }
}

