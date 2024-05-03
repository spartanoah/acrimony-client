/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.IMatcher;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class AttributeLayout
implements IMatcher {
    public static final String ACC_ABSTRACT = "ACC_ABSTRACT";
    public static final String ACC_ANNOTATION = "ACC_ANNOTATION";
    public static final String ACC_ENUM = "ACC_ENUM";
    public static final String ACC_FINAL = "ACC_FINAL";
    public static final String ACC_INTERFACE = "ACC_INTERFACE";
    public static final String ACC_NATIVE = "ACC_NATIVE";
    public static final String ACC_PRIVATE = "ACC_PRIVATE";
    public static final String ACC_PROTECTED = "ACC_PROTECTED";
    public static final String ACC_PUBLIC = "ACC_PUBLIC";
    public static final String ACC_STATIC = "ACC_STATIC";
    public static final String ACC_STRICT = "ACC_STRICT";
    public static final String ACC_SYNCHRONIZED = "ACC_SYNCHRONIZED";
    public static final String ACC_SYNTHETIC = "ACC_SYNTHETIC";
    public static final String ACC_TRANSIENT = "ACC_TRANSIENT";
    public static final String ACC_VOLATILE = "ACC_VOLATILE";
    public static final String ATTRIBUTE_ANNOTATION_DEFAULT = "AnnotationDefault";
    public static final String ATTRIBUTE_CLASS_FILE_VERSION = "class-file version";
    public static final String ATTRIBUTE_CODE = "Code";
    public static final String ATTRIBUTE_CONSTANT_VALUE = "ConstantValue";
    public static final String ATTRIBUTE_DEPRECATED = "Deprecated";
    public static final String ATTRIBUTE_ENCLOSING_METHOD = "EnclosingMethod";
    public static final String ATTRIBUTE_EXCEPTIONS = "Exceptions";
    public static final String ATTRIBUTE_INNER_CLASSES = "InnerClasses";
    public static final String ATTRIBUTE_LINE_NUMBER_TABLE = "LineNumberTable";
    public static final String ATTRIBUTE_LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    public static final String ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
    public static final String ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
    public static final String ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
    public static final String ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
    public static final String ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
    public static final String ATTRIBUTE_SIGNATURE = "Signature";
    public static final String ATTRIBUTE_SOURCE_FILE = "SourceFile";
    public static final int CONTEXT_CLASS = 0;
    public static final int CONTEXT_CODE = 3;
    public static final int CONTEXT_FIELD = 1;
    public static final int CONTEXT_METHOD = 2;
    public static final String[] contextNames = new String[]{"Class", "Field", "Method", "Code"};
    private final int context;
    private final int index;
    private final String layout;
    private long mask;
    private final String name;
    private final boolean isDefault;
    private int backwardsCallCount;

    private static ClassFileEntry getValue(String layout, long value, SegmentConstantPool pool) throws Pack200Exception {
        if (layout.startsWith("R")) {
            if (layout.indexOf(78) != -1) {
                --value;
            }
            if (layout.startsWith("RU")) {
                return pool.getValue(1, value);
            }
            if (layout.startsWith("RS")) {
                return pool.getValue(8, value);
            }
        } else if (layout.startsWith("K")) {
            char type = layout.charAt(1);
            switch (type) {
                case 'S': {
                    return pool.getValue(6, value);
                }
                case 'C': 
                case 'I': {
                    return pool.getValue(2, value);
                }
                case 'F': {
                    return pool.getValue(3, value);
                }
                case 'J': {
                    return pool.getValue(4, value);
                }
                case 'D': {
                    return pool.getValue(5, value);
                }
            }
        }
        throw new Pack200Exception("Unknown layout encoding: " + layout);
    }

    public AttributeLayout(String name, int context, String layout, int index) throws Pack200Exception {
        this(name, context, layout, index, true);
    }

    public AttributeLayout(String name, int context, String layout, int index, boolean isDefault) throws Pack200Exception {
        this.index = index;
        this.context = context;
        this.mask = index >= 0 ? 1L << index : 0L;
        if (context != 0 && context != 3 && context != 1 && context != 2) {
            throw new Pack200Exception("Attribute context out of range: " + context);
        }
        if (layout == null) {
            throw new Pack200Exception("Cannot have a null layout");
        }
        if (name == null || name.length() == 0) {
            throw new Pack200Exception("Cannot have an unnamed layout");
        }
        this.name = name;
        this.layout = layout;
        this.isDefault = isDefault;
    }

    public Codec getCodec() {
        if (this.layout.indexOf(79) >= 0) {
            return Codec.BRANCH5;
        }
        if (this.layout.indexOf(80) >= 0) {
            return Codec.BCI5;
        }
        if (this.layout.indexOf(83) >= 0 && this.layout.indexOf("KS") < 0 && this.layout.indexOf("RS") < 0) {
            return Codec.SIGNED5;
        }
        if (this.layout.indexOf(66) >= 0) {
            return Codec.BYTE1;
        }
        return Codec.UNSIGNED5;
    }

    public String getLayout() {
        return this.layout;
    }

    public ClassFileEntry getValue(long value, SegmentConstantPool pool) throws Pack200Exception {
        return AttributeLayout.getValue(this.layout, value, pool);
    }

    public ClassFileEntry getValue(long value, String type, SegmentConstantPool pool) throws Pack200Exception {
        if (!this.layout.startsWith("KQ")) {
            return AttributeLayout.getValue(this.layout, value, pool);
        }
        if (type.equals("Ljava/lang/String;")) {
            ClassFileEntry value2 = AttributeLayout.getValue("KS", value, pool);
            return value2;
        }
        return AttributeLayout.getValue("K" + type + this.layout.substring(2), value, pool);
    }

    public int hashCode() {
        int PRIME = 31;
        int r = 1;
        if (this.name != null) {
            r = r * 31 + this.name.hashCode();
        }
        if (this.layout != null) {
            r = r * 31 + this.layout.hashCode();
        }
        r = r * 31 + this.index;
        r = r * 31 + this.context;
        return r;
    }

    @Override
    public boolean matches(long value) {
        return (value & this.mask) != 0L;
    }

    public String toString() {
        return contextNames[this.context] + ": " + this.name;
    }

    public int getContext() {
        return this.context;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public int numBackwardsCallables() {
        if (this.layout == "*") {
            return 1;
        }
        return this.backwardsCallCount;
    }

    public boolean isDefaultLayout() {
        return this.isDefault;
    }

    public void setBackwardsCallCount(int backwardsCallCount) {
        this.backwardsCallCount = backwardsCallCount;
    }
}

