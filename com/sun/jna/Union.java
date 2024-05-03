/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.sun.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;
import java.util.Iterator;

public abstract class Union
extends Structure {
    private Structure.StructField activeField;
    Structure.StructField biggestField;

    protected Union() {
    }

    protected Union(Pointer p) {
        super(p);
    }

    protected Union(Pointer p, int alignType) {
        super(p, alignType);
    }

    protected Union(TypeMapper mapper) {
        super(mapper);
    }

    protected Union(Pointer p, int alignType, TypeMapper mapper) {
        super(p, alignType, mapper);
    }

    public void setType(Class type) {
        this.ensureAllocated();
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (f.type != type) continue;
            this.activeField = f;
            return;
        }
        throw new IllegalArgumentException("No field of type " + type + " in " + this);
    }

    public void setType(String fieldName) {
        this.ensureAllocated();
        Structure.StructField f = (Structure.StructField)this.fields().get(fieldName);
        if (f == null) {
            throw new IllegalArgumentException("No field named " + fieldName + " in " + this);
        }
        this.activeField = f;
    }

    public Object readField(String fieldName) {
        this.ensureAllocated();
        this.setType(fieldName);
        return super.readField(fieldName);
    }

    public void writeField(String fieldName) {
        this.ensureAllocated();
        this.setType(fieldName);
        super.writeField(fieldName);
    }

    public void writeField(String fieldName, Object value) {
        this.ensureAllocated();
        this.setType(fieldName);
        super.writeField(fieldName, value);
    }

    public Object getTypedValue(Class type) {
        this.ensureAllocated();
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (f.type != type) continue;
            this.activeField = f;
            this.read();
            return this.getField(this.activeField);
        }
        throw new IllegalArgumentException("No field of type " + type + " in " + this);
    }

    public Object setTypedValue(Object object) {
        Structure.StructField f = this.findField(object.getClass());
        if (f != null) {
            this.activeField = f;
            this.setField(f, object);
            return this;
        }
        throw new IllegalArgumentException("No field of type " + object.getClass() + " in " + this);
    }

    private Structure.StructField findField(Class type) {
        this.ensureAllocated();
        Iterator i = this.fields().values().iterator();
        while (i.hasNext()) {
            Structure.StructField f = (Structure.StructField)i.next();
            if (!f.type.isAssignableFrom(type)) continue;
            return f;
        }
        return null;
    }

    void writeField(Structure.StructField field) {
        if (field == this.activeField) {
            super.writeField(field);
        }
    }

    Object readField(Structure.StructField field) {
        if (field == this.activeField || !Structure.class.isAssignableFrom(field.type) && !String.class.isAssignableFrom(field.type) && !WString.class.isAssignableFrom(field.type)) {
            return super.readField(field);
        }
        return null;
    }

    int calculateSize(boolean force, boolean avoidFFIType) {
        int size = super.calculateSize(force, avoidFFIType);
        if (size != -1) {
            int fsize = 0;
            Iterator i = this.fields().values().iterator();
            while (i.hasNext()) {
                Structure.StructField f = (Structure.StructField)i.next();
                f.offset = 0;
                if (f.size <= fsize && (f.size != fsize || !(class$com$sun$jna$Structure == null ? Union.class$("com.sun.jna.Structure") : class$com$sun$jna$Structure).isAssignableFrom(f.type))) continue;
                fsize = f.size;
                this.biggestField = f;
            }
            size = this.calculateAlignedSize(fsize);
            if (size > 0 && this instanceof Structure.ByValue && !avoidFFIType) {
                this.getTypeInfo();
            }
        }
        return size;
    }

    protected int getNativeAlignment(Class type, Object value, boolean isFirstElement) {
        return super.getNativeAlignment(type, value, true);
    }

    Pointer getTypeInfo() {
        if (this.biggestField == null) {
            return null;
        }
        return super.getTypeInfo();
    }
}

