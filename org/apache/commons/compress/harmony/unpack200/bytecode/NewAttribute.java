/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.BCIRenumberedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class NewAttribute
extends BCIRenumberedAttribute {
    private final List lengths = new ArrayList();
    private final List body = new ArrayList();
    private ClassConstantPool pool;
    private final int layoutIndex;

    public NewAttribute(CPUTF8 attributeName, int layoutIndex) {
        super(attributeName);
        this.layoutIndex = layoutIndex;
    }

    public int getLayoutIndex() {
        return this.layoutIndex;
    }

    @Override
    protected int getLength() {
        int length = 0;
        for (int iter = 0; iter < this.lengths.size(); ++iter) {
            length += ((Integer)this.lengths.get(iter)).intValue();
        }
        return length;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        for (int i = 0; i < this.lengths.size(); ++i) {
            int length = (Integer)this.lengths.get(i);
            Object obj = this.body.get(i);
            long value = 0L;
            if (obj instanceof Long) {
                value = (Long)obj;
            } else if (obj instanceof ClassFileEntry) {
                value = this.pool.indexOf((ClassFileEntry)obj);
            } else if (obj instanceof BCValue) {
                value = ((BCValue)obj).actualValue;
            }
            if (length == 1) {
                dos.writeByte((int)value);
                continue;
            }
            if (length == 2) {
                dos.writeShort((int)value);
                continue;
            }
            if (length == 4) {
                dos.writeInt((int)value);
                continue;
            }
            if (length != 8) continue;
            dos.writeLong(value);
        }
    }

    @Override
    public String toString() {
        return this.attributeName.underlyingString();
    }

    public void addInteger(int length, long value) {
        this.lengths.add(length);
        this.body.add(value);
    }

    public void addBCOffset(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCOffset(value));
    }

    public void addBCIndex(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCIndex(value));
    }

    public void addBCLength(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCLength(value));
    }

    public void addToBody(int length, Object value) {
        this.lengths.add(length);
        this.body.add(value);
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (int iter = 0; iter < this.body.size(); ++iter) {
            Object element = this.body.get(iter);
            if (!(element instanceof ClassFileEntry)) continue;
            ((ClassFileEntry)element).resolve(pool);
        }
        this.pool = pool;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        int total = 1;
        for (int iter = 0; iter < this.body.size(); ++iter) {
            Object element = this.body.get(iter);
            if (!(element instanceof ClassFileEntry)) continue;
            ++total;
        }
        ClassFileEntry[] nested = new ClassFileEntry[total];
        nested[0] = this.getAttributeName();
        int i = 1;
        for (int iter = 0; iter < this.body.size(); ++iter) {
            Object element = this.body.get(iter);
            if (!(element instanceof ClassFileEntry)) continue;
            nested[i] = (ClassFileEntry)element;
            ++i;
        }
        return nested;
    }

    @Override
    protected int[] getStartPCs() {
        return null;
    }

    @Override
    public void renumber(List byteCodeOffsets) {
        if (!this.renumbered) {
            Object previous = null;
            for (Object obj : this.body) {
                if (obj instanceof BCIndex) {
                    BCIndex bcIndex = (BCIndex)obj;
                    bcIndex.setActualValue((Integer)byteCodeOffsets.get(bcIndex.index));
                } else if (obj instanceof BCOffset) {
                    BCOffset bcOffset = (BCOffset)obj;
                    if (previous instanceof BCIndex) {
                        int index = ((BCIndex)previous).index + bcOffset.offset;
                        bcOffset.setIndex(index);
                        bcOffset.setActualValue((Integer)byteCodeOffsets.get(index));
                    } else if (previous instanceof BCOffset) {
                        int index = ((BCOffset)previous).index + bcOffset.offset;
                        bcOffset.setIndex(index);
                        bcOffset.setActualValue((Integer)byteCodeOffsets.get(index));
                    } else {
                        bcOffset.setActualValue((Integer)byteCodeOffsets.get(bcOffset.offset));
                    }
                } else if (obj instanceof BCLength) {
                    BCLength bcLength = (BCLength)obj;
                    BCIndex prevIndex = previous;
                    int index = prevIndex.index + bcLength.length;
                    int actualLength = (Integer)byteCodeOffsets.get(index) - prevIndex.actualValue;
                    bcLength.setActualValue(actualLength);
                }
                previous = obj;
            }
            this.renumbered = true;
        }
    }

    private static abstract class BCValue {
        int actualValue;

        private BCValue() {
        }

        public void setActualValue(int value) {
            this.actualValue = value;
        }
    }

    private static class BCLength
    extends BCValue {
        private final int length;

        public BCLength(int length) {
            this.length = length;
        }
    }

    private static class BCIndex
    extends BCValue {
        private final int index;

        public BCIndex(int index) {
            this.index = index;
        }
    }

    private static class BCOffset
    extends BCValue {
        private final int offset;
        private int index;

        public BCOffset(int offset) {
            this.offset = offset;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}

