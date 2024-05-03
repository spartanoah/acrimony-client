/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPSignature;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.ConstantPoolEntry;

public class CPNameAndType
extends ConstantPoolEntry
implements Comparable {
    private final CPUTF8 name;
    private final CPSignature signature;

    public CPNameAndType(CPUTF8 name, CPSignature signature) {
        this.name = name;
        this.signature = signature;
    }

    public String toString() {
        return this.name + ":" + this.signature;
    }

    public int compareTo(Object obj) {
        if (obj instanceof CPNameAndType) {
            CPNameAndType nat = (CPNameAndType)obj;
            int compareSignature = this.signature.compareTo(nat.signature);
            if (compareSignature == 0) {
                return this.name.compareTo(nat.name);
            }
            return compareSignature;
        }
        return 0;
    }

    public int getNameIndex() {
        return this.name.getIndex();
    }

    public String getName() {
        return this.name.getUnderlyingString();
    }

    public int getTypeIndex() {
        return this.signature.getIndex();
    }
}

