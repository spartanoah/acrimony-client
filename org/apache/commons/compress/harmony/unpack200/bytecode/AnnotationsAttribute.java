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
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstant;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public abstract class AnnotationsAttribute
extends Attribute {
    public AnnotationsAttribute(CPUTF8 attributeName) {
        super(attributeName);
    }

    public static class ElementValue {
        private final Object value;
        private final int tag;
        private int constant_value_index = -1;

        public ElementValue(int tag, Object value) {
            this.tag = tag;
            this.value = value;
        }

        public List getClassFileEntries() {
            ArrayList<Object> entries = new ArrayList<Object>(1);
            if (this.value instanceof CPNameAndType) {
                entries.add(((CPNameAndType)this.value).name);
                entries.add(((CPNameAndType)this.value).descriptor);
            } else if (this.value instanceof ClassFileEntry) {
                entries.add(this.value);
            } else if (this.value instanceof ElementValue[]) {
                ElementValue[] values = (ElementValue[])this.value;
                for (int i = 0; i < values.length; ++i) {
                    entries.addAll(values[i].getClassFileEntries());
                }
            } else if (this.value instanceof Annotation) {
                entries.addAll(((Annotation)this.value).getClassFileEntries());
            }
            return entries;
        }

        public void resolve(ClassConstantPool pool) {
            if (this.value instanceof CPConstant) {
                ((CPConstant)this.value).resolve(pool);
                this.constant_value_index = pool.indexOf((CPConstant)this.value);
            } else if (this.value instanceof CPClass) {
                ((CPClass)this.value).resolve(pool);
                this.constant_value_index = pool.indexOf((CPClass)this.value);
            } else if (this.value instanceof CPUTF8) {
                ((CPUTF8)this.value).resolve(pool);
                this.constant_value_index = pool.indexOf((CPUTF8)this.value);
            } else if (this.value instanceof CPNameAndType) {
                ((CPNameAndType)this.value).resolve(pool);
            } else if (this.value instanceof Annotation) {
                ((Annotation)this.value).resolve(pool);
            } else if (this.value instanceof ElementValue[]) {
                ElementValue[] nestedValues = (ElementValue[])this.value;
                for (int i = 0; i < nestedValues.length; ++i) {
                    nestedValues[i].resolve(pool);
                }
            }
        }

        public void writeBody(DataOutputStream dos) throws IOException {
            dos.writeByte(this.tag);
            if (this.constant_value_index != -1) {
                dos.writeShort(this.constant_value_index);
            } else if (this.value instanceof CPNameAndType) {
                ((CPNameAndType)this.value).writeBody(dos);
            } else if (this.value instanceof Annotation) {
                ((Annotation)this.value).writeBody(dos);
            } else if (this.value instanceof ElementValue[]) {
                ElementValue[] nestedValues = (ElementValue[])this.value;
                dos.writeShort(nestedValues.length);
                for (int i = 0; i < nestedValues.length; ++i) {
                    nestedValues[i].writeBody(dos);
                }
            } else {
                throw new Error("");
            }
        }

        public int getLength() {
            switch (this.tag) {
                case 66: 
                case 67: 
                case 68: 
                case 70: 
                case 73: 
                case 74: 
                case 83: 
                case 90: 
                case 99: 
                case 115: {
                    return 3;
                }
                case 101: {
                    return 5;
                }
                case 91: {
                    int length = 3;
                    ElementValue[] nestedValues = (ElementValue[])this.value;
                    for (int i = 0; i < nestedValues.length; ++i) {
                        length += nestedValues[i].getLength();
                    }
                    return length;
                }
                case 64: {
                    return 1 + ((Annotation)this.value).getLength();
                }
            }
            return 0;
        }
    }

    public static class Annotation {
        private final int num_pairs;
        private final CPUTF8[] element_names;
        private final ElementValue[] element_values;
        private final CPUTF8 type;
        private int type_index;
        private int[] name_indexes;

        public Annotation(int num_pairs, CPUTF8 type, CPUTF8[] element_names, ElementValue[] element_values) {
            this.num_pairs = num_pairs;
            this.type = type;
            this.element_names = element_names;
            this.element_values = element_values;
        }

        public int getLength() {
            int length = 4;
            for (int i = 0; i < this.num_pairs; ++i) {
                length += 2;
                length += this.element_values[i].getLength();
            }
            return length;
        }

        public void resolve(ClassConstantPool pool) {
            this.type.resolve(pool);
            this.type_index = pool.indexOf(this.type);
            this.name_indexes = new int[this.num_pairs];
            for (int i = 0; i < this.element_names.length; ++i) {
                this.element_names[i].resolve(pool);
                this.name_indexes[i] = pool.indexOf(this.element_names[i]);
                this.element_values[i].resolve(pool);
            }
        }

        public void writeBody(DataOutputStream dos) throws IOException {
            dos.writeShort(this.type_index);
            dos.writeShort(this.num_pairs);
            for (int i = 0; i < this.num_pairs; ++i) {
                dos.writeShort(this.name_indexes[i]);
                this.element_values[i].writeBody(dos);
            }
        }

        public List getClassFileEntries() {
            ArrayList<CPUTF8> entries = new ArrayList<CPUTF8>();
            for (int i = 0; i < this.element_names.length; ++i) {
                entries.add(this.element_names[i]);
                entries.addAll(this.element_values[i].getClassFileEntries());
            }
            entries.add(this.type);
            return entries;
        }
    }
}

