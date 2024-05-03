/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.AttributeLayout;
import org.apache.commons.compress.harmony.unpack200.AttributeLayoutMap;
import org.apache.commons.compress.harmony.unpack200.BandSet;
import org.apache.commons.compress.harmony.unpack200.CpBands;
import org.apache.commons.compress.harmony.unpack200.IMatcher;
import org.apache.commons.compress.harmony.unpack200.IcBands;
import org.apache.commons.compress.harmony.unpack200.IcTuple;
import org.apache.commons.compress.harmony.unpack200.MetadataBandGroup;
import org.apache.commons.compress.harmony.unpack200.NewAttributeBands;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.SegmentOptions;
import org.apache.commons.compress.harmony.unpack200.SegmentUtils;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantValueAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.DeprecatedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.EnclosingMethodAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ExceptionsAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LineNumberTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.LocalVariableTypeTableAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SignatureAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.SourceFileAttribute;

public class ClassBands
extends BandSet {
    private int[] classFieldCount;
    private long[] classFlags;
    private long[] classAccessFlags;
    private int[][] classInterfacesInts;
    private int[] classMethodCount;
    private int[] classSuperInts;
    private String[] classThis;
    private int[] classThisInts;
    private ArrayList[] classAttributes;
    private int[] classVersionMajor;
    private int[] classVersionMinor;
    private IcTuple[][] icLocal;
    private List[] codeAttributes;
    private int[] codeHandlerCount;
    private int[] codeMaxNALocals;
    private int[] codeMaxStack;
    private ArrayList[][] fieldAttributes;
    private String[][] fieldDescr;
    private int[][] fieldDescrInts;
    private long[][] fieldFlags;
    private long[][] fieldAccessFlags;
    private ArrayList[][] methodAttributes;
    private String[][] methodDescr;
    private int[][] methodDescrInts;
    private long[][] methodFlags;
    private long[][] methodAccessFlags;
    private final AttributeLayoutMap attrMap;
    private final CpBands cpBands;
    private final SegmentOptions options;
    private final int classCount;
    private int[] methodAttrCalls;
    private int[][] codeHandlerStartP;
    private int[][] codeHandlerEndPO;
    private int[][] codeHandlerCatchPO;
    private int[][] codeHandlerClassRCN;
    private boolean[] codeHasAttributes;

    public ClassBands(Segment segment) {
        super(segment);
        this.attrMap = segment.getAttrDefinitionBands().getAttributeDefinitionMap();
        this.cpBands = segment.getCpBands();
        this.classCount = this.header.getClassCount();
        this.options = this.header.getOptions();
    }

    @Override
    public void read(InputStream in) throws IOException, Pack200Exception {
        int classCount = this.header.getClassCount();
        this.classThisInts = this.decodeBandInt("class_this", in, Codec.DELTA5, classCount);
        this.classThis = this.getReferences(this.classThisInts, this.cpBands.getCpClass());
        this.classSuperInts = this.decodeBandInt("class_super", in, Codec.DELTA5, classCount);
        int[] classInterfaceLengths = this.decodeBandInt("class_interface_count", in, Codec.DELTA5, classCount);
        this.classInterfacesInts = this.decodeBandInt("class_interface", in, Codec.DELTA5, classInterfaceLengths);
        this.classFieldCount = this.decodeBandInt("class_field_count", in, Codec.DELTA5, classCount);
        this.classMethodCount = this.decodeBandInt("class_method_count", in, Codec.DELTA5, classCount);
        this.parseFieldBands(in);
        this.parseMethodBands(in);
        this.parseClassAttrBands(in);
        this.parseCodeBands(in);
    }

    @Override
    public void unpack() {
    }

    private void parseFieldBands(InputStream in) throws IOException, Pack200Exception {
        this.fieldDescrInts = this.decodeBandInt("field_descr", in, Codec.DELTA5, this.classFieldCount);
        this.fieldDescr = this.getReferences(this.fieldDescrInts, this.cpBands.getCpDescriptor());
        this.parseFieldAttrBands(in);
    }

    private void parseFieldAttrBands(InputStream in) throws IOException, Pack200Exception {
        int i;
        int backwardsCallsUsed;
        this.fieldFlags = this.parseFlags("field_flags", in, this.classFieldCount, Codec.UNSIGNED5, this.options.hasFieldFlagsHi());
        int fieldAttrCount = SegmentUtils.countBit16(this.fieldFlags);
        int[] fieldAttrCounts = this.decodeBandInt("field_attr_count", in, Codec.UNSIGNED5, fieldAttrCount);
        int[][] fieldAttrIndexes = this.decodeBandInt("field_attr_indexes", in, Codec.UNSIGNED5, fieldAttrCounts);
        int callCount = this.getCallCount(fieldAttrIndexes, this.fieldFlags, 1);
        int[] fieldAttrCalls = this.decodeBandInt("field_attr_calls", in, Codec.UNSIGNED5, callCount);
        this.fieldAttributes = new ArrayList[this.classCount][];
        for (int i2 = 0; i2 < this.classCount; ++i2) {
            this.fieldAttributes[i2] = new ArrayList[this.fieldFlags[i2].length];
            for (int j = 0; j < this.fieldFlags[i2].length; ++j) {
                this.fieldAttributes[i2][j] = new ArrayList();
            }
        }
        AttributeLayout constantValueLayout = this.attrMap.getAttributeLayout("ConstantValue", 1);
        int constantCount = SegmentUtils.countMatches(this.fieldFlags, (IMatcher)constantValueLayout);
        int[] field_constantValue_KQ = this.decodeBandInt("field_ConstantValue_KQ", in, Codec.UNSIGNED5, constantCount);
        int constantValueIndex = 0;
        AttributeLayout signatureLayout = this.attrMap.getAttributeLayout("Signature", 1);
        int signatureCount = SegmentUtils.countMatches(this.fieldFlags, (IMatcher)signatureLayout);
        int[] fieldSignatureRS = this.decodeBandInt("field_Signature_RS", in, Codec.UNSIGNED5, signatureCount);
        int signatureIndex = 0;
        AttributeLayout deprecatedLayout = this.attrMap.getAttributeLayout("Deprecated", 1);
        for (int i3 = 0; i3 < this.classCount; ++i3) {
            for (int j = 0; j < this.fieldFlags[i3].length; ++j) {
                ClassFileEntry value;
                String type;
                int colon;
                String desc;
                long result;
                long flag = this.fieldFlags[i3][j];
                if (deprecatedLayout.matches(flag)) {
                    this.fieldAttributes[i3][j].add(new DeprecatedAttribute());
                }
                if (constantValueLayout.matches(flag)) {
                    result = field_constantValue_KQ[constantValueIndex];
                    desc = this.fieldDescr[i3][j];
                    colon = desc.indexOf(58);
                    type = desc.substring(colon + 1);
                    if (type.equals("B") || type.equals("S") || type.equals("C") || type.equals("Z")) {
                        type = "I";
                    }
                    value = constantValueLayout.getValue(result, type, this.cpBands.getConstantPool());
                    this.fieldAttributes[i3][j].add(new ConstantValueAttribute(value));
                    ++constantValueIndex;
                }
                if (!signatureLayout.matches(flag)) continue;
                result = fieldSignatureRS[signatureIndex];
                desc = this.fieldDescr[i3][j];
                colon = desc.indexOf(58);
                type = desc.substring(colon + 1);
                value = (CPUTF8)signatureLayout.getValue(result, type, this.cpBands.getConstantPool());
                this.fieldAttributes[i3][j].add(new SignatureAttribute((CPUTF8)value));
                ++signatureIndex;
            }
        }
        int backwardsCallIndex = backwardsCallsUsed = this.parseFieldMetadataBands(in, fieldAttrCalls);
        int limit = this.options.hasFieldFlagsHi() ? 62 : 31;
        AttributeLayout[] otherLayouts = new AttributeLayout[limit + 1];
        int[] counts = new int[limit + 1];
        List[] otherAttributes = new List[limit + 1];
        for (i = 0; i < limit; ++i) {
            AttributeLayout layout = this.attrMap.getAttributeLayout(i, 1);
            if (layout == null || layout.isDefaultLayout()) continue;
            otherLayouts[i] = layout;
            counts[i] = SegmentUtils.countMatches(this.fieldFlags, (IMatcher)layout);
        }
        for (i = 0; i < counts.length; ++i) {
            if (counts[i] <= 0) continue;
            NewAttributeBands bands = this.attrMap.getAttributeBands(otherLayouts[i]);
            otherAttributes[i] = bands.parseAttributes(in, counts[i]);
            int numBackwardsCallables = otherLayouts[i].numBackwardsCallables();
            if (numBackwardsCallables <= 0) continue;
            int[] backwardsCalls = new int[numBackwardsCallables];
            System.arraycopy(fieldAttrCalls, backwardsCallIndex, backwardsCalls, 0, numBackwardsCallables);
            bands.setBackwardsCalls(backwardsCalls);
            backwardsCallIndex += numBackwardsCallables;
        }
        for (i = 0; i < this.classCount; ++i) {
            for (int j = 0; j < this.fieldFlags[i].length; ++j) {
                long flag = this.fieldFlags[i][j];
                int othersAddedAtStart = 0;
                for (int k = 0; k < otherLayouts.length; ++k) {
                    if (otherLayouts[k] == null || !otherLayouts[k].matches(flag)) continue;
                    if (otherLayouts[k].getIndex() < 15) {
                        this.fieldAttributes[i][j].add(othersAddedAtStart++, otherAttributes[k].get(0));
                    } else {
                        this.fieldAttributes[i][j].add(otherAttributes[k].get(0));
                    }
                    otherAttributes[k].remove(0);
                }
            }
        }
    }

    private void parseMethodBands(InputStream in) throws IOException, Pack200Exception {
        this.methodDescrInts = this.decodeBandInt("method_descr", in, Codec.MDELTA5, this.classMethodCount);
        this.methodDescr = this.getReferences(this.methodDescrInts, this.cpBands.getCpDescriptor());
        this.parseMethodAttrBands(in);
    }

    private void parseMethodAttrBands(InputStream in) throws IOException, Pack200Exception {
        int i;
        int backwardsCallsUsed;
        this.methodFlags = this.parseFlags("method_flags", in, this.classMethodCount, Codec.UNSIGNED5, this.options.hasMethodFlagsHi());
        int methodAttrCount = SegmentUtils.countBit16(this.methodFlags);
        int[] methodAttrCounts = this.decodeBandInt("method_attr_count", in, Codec.UNSIGNED5, methodAttrCount);
        int[][] methodAttrIndexes = this.decodeBandInt("method_attr_indexes", in, Codec.UNSIGNED5, methodAttrCounts);
        int callCount = this.getCallCount(methodAttrIndexes, this.methodFlags, 2);
        this.methodAttrCalls = this.decodeBandInt("method_attr_calls", in, Codec.UNSIGNED5, callCount);
        this.methodAttributes = new ArrayList[this.classCount][];
        for (int i2 = 0; i2 < this.classCount; ++i2) {
            this.methodAttributes[i2] = new ArrayList[this.methodFlags[i2].length];
            for (int j = 0; j < this.methodFlags[i2].length; ++j) {
                this.methodAttributes[i2][j] = new ArrayList();
            }
        }
        AttributeLayout methodExceptionsLayout = this.attrMap.getAttributeLayout("Exceptions", 2);
        int count = SegmentUtils.countMatches(this.methodFlags, (IMatcher)methodExceptionsLayout);
        int[] numExceptions = this.decodeBandInt("method_Exceptions_n", in, Codec.UNSIGNED5, count);
        int[][] methodExceptionsRS = this.decodeBandInt("method_Exceptions_RC", in, Codec.UNSIGNED5, numExceptions);
        AttributeLayout methodSignatureLayout = this.attrMap.getAttributeLayout("Signature", 2);
        int count1 = SegmentUtils.countMatches(this.methodFlags, (IMatcher)methodSignatureLayout);
        int[] methodSignatureRS = this.decodeBandInt("method_signature_RS", in, Codec.UNSIGNED5, count1);
        AttributeLayout deprecatedLayout = this.attrMap.getAttributeLayout("Deprecated", 2);
        int methodExceptionsIndex = 0;
        int methodSignatureIndex = 0;
        for (int i3 = 0; i3 < this.methodAttributes.length; ++i3) {
            for (int j = 0; j < this.methodAttributes[i3].length; ++j) {
                long flag = this.methodFlags[i3][j];
                if (methodExceptionsLayout.matches(flag)) {
                    int n = numExceptions[methodExceptionsIndex];
                    int[] exceptions = methodExceptionsRS[methodExceptionsIndex];
                    CPClass[] exceptionClasses = new CPClass[n];
                    for (int k = 0; k < n; ++k) {
                        exceptionClasses[k] = this.cpBands.cpClassValue(exceptions[k]);
                    }
                    this.methodAttributes[i3][j].add(new ExceptionsAttribute(exceptionClasses));
                    ++methodExceptionsIndex;
                }
                if (methodSignatureLayout.matches(flag)) {
                    long result = methodSignatureRS[methodSignatureIndex];
                    String desc = this.methodDescr[i3][j];
                    int colon = desc.indexOf(58);
                    String type = desc.substring(colon + 1);
                    if (type.equals("B") || type.equals("H")) {
                        type = "I";
                    }
                    CPUTF8 value = (CPUTF8)methodSignatureLayout.getValue(result, type, this.cpBands.getConstantPool());
                    this.methodAttributes[i3][j].add(new SignatureAttribute(value));
                    ++methodSignatureIndex;
                }
                if (!deprecatedLayout.matches(flag)) continue;
                this.methodAttributes[i3][j].add(new DeprecatedAttribute());
            }
        }
        int backwardsCallIndex = backwardsCallsUsed = this.parseMethodMetadataBands(in, this.methodAttrCalls);
        int limit = this.options.hasMethodFlagsHi() ? 62 : 31;
        AttributeLayout[] otherLayouts = new AttributeLayout[limit + 1];
        int[] counts = new int[limit + 1];
        List[] otherAttributes = new List[limit + 1];
        for (i = 0; i < limit; ++i) {
            AttributeLayout layout = this.attrMap.getAttributeLayout(i, 2);
            if (layout == null || layout.isDefaultLayout()) continue;
            otherLayouts[i] = layout;
            counts[i] = SegmentUtils.countMatches(this.methodFlags, (IMatcher)layout);
        }
        for (i = 0; i < counts.length; ++i) {
            if (counts[i] <= 0) continue;
            NewAttributeBands bands = this.attrMap.getAttributeBands(otherLayouts[i]);
            otherAttributes[i] = bands.parseAttributes(in, counts[i]);
            int numBackwardsCallables = otherLayouts[i].numBackwardsCallables();
            if (numBackwardsCallables <= 0) continue;
            int[] backwardsCalls = new int[numBackwardsCallables];
            System.arraycopy(this.methodAttrCalls, backwardsCallIndex, backwardsCalls, 0, numBackwardsCallables);
            bands.setBackwardsCalls(backwardsCalls);
            backwardsCallIndex += numBackwardsCallables;
        }
        for (i = 0; i < this.methodAttributes.length; ++i) {
            for (int j = 0; j < this.methodAttributes[i].length; ++j) {
                long flag = this.methodFlags[i][j];
                int othersAddedAtStart = 0;
                for (int k = 0; k < otherLayouts.length; ++k) {
                    if (otherLayouts[k] == null || !otherLayouts[k].matches(flag)) continue;
                    if (otherLayouts[k].getIndex() < 15) {
                        this.methodAttributes[i][j].add(othersAddedAtStart++, otherAttributes[k].get(0));
                    } else {
                        this.methodAttributes[i][j].add(otherAttributes[k].get(0));
                    }
                    otherAttributes[k].remove(0);
                }
            }
        }
    }

    private int getCallCount(int[][] methodAttrIndexes, long[][] flags, int context) throws Pack200Exception {
        int i;
        int callCount = 0;
        for (int i2 = 0; i2 < methodAttrIndexes.length; ++i2) {
            for (int j = 0; j < methodAttrIndexes[i2].length; ++j) {
                int index = methodAttrIndexes[i2][j];
                AttributeLayout layout = this.attrMap.getAttributeLayout(index, context);
                callCount += layout.numBackwardsCallables();
            }
        }
        int layoutsUsed = 0;
        for (i = 0; i < flags.length; ++i) {
            for (int j = 0; j < flags[i].length; ++j) {
                layoutsUsed = (int)((long)layoutsUsed | flags[i][j]);
            }
        }
        for (i = 0; i < 26; ++i) {
            if ((layoutsUsed & 1 << i) == 0) continue;
            AttributeLayout layout = this.attrMap.getAttributeLayout(i, context);
            callCount += layout.numBackwardsCallables();
        }
        return callCount;
    }

    private void parseClassAttrBands(InputStream in) throws IOException, Pack200Exception {
        int i;
        String[] cpUTF8 = this.cpBands.getCpUTF8();
        String[] cpClass = this.cpBands.getCpClass();
        this.classAttributes = new ArrayList[this.classCount];
        for (int i2 = 0; i2 < this.classCount; ++i2) {
            this.classAttributes[i2] = new ArrayList();
        }
        this.classFlags = this.parseFlags("class_flags", in, this.classCount, Codec.UNSIGNED5, this.options.hasClassFlagsHi());
        int classAttrCount = SegmentUtils.countBit16(this.classFlags);
        int[] classAttrCounts = this.decodeBandInt("class_attr_count", in, Codec.UNSIGNED5, classAttrCount);
        int[][] classAttrIndexes = this.decodeBandInt("class_attr_indexes", in, Codec.UNSIGNED5, classAttrCounts);
        int callCount = this.getCallCount(classAttrIndexes, new long[][]{this.classFlags}, 0);
        int[] classAttrCalls = this.decodeBandInt("class_attr_calls", in, Codec.UNSIGNED5, callCount);
        AttributeLayout deprecatedLayout = this.attrMap.getAttributeLayout("Deprecated", 0);
        AttributeLayout sourceFileLayout = this.attrMap.getAttributeLayout("SourceFile", 0);
        int sourceFileCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)sourceFileLayout);
        int[] classSourceFile = this.decodeBandInt("class_SourceFile_RUN", in, Codec.UNSIGNED5, sourceFileCount);
        AttributeLayout enclosingMethodLayout = this.attrMap.getAttributeLayout("EnclosingMethod", 0);
        int enclosingMethodCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)enclosingMethodLayout);
        int[] enclosingMethodRC = this.decodeBandInt("class_EnclosingMethod_RC", in, Codec.UNSIGNED5, enclosingMethodCount);
        int[] enclosingMethodRDN = this.decodeBandInt("class_EnclosingMethod_RDN", in, Codec.UNSIGNED5, enclosingMethodCount);
        AttributeLayout signatureLayout = this.attrMap.getAttributeLayout("Signature", 0);
        int signatureCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)signatureLayout);
        int[] classSignature = this.decodeBandInt("class_Signature_RS", in, Codec.UNSIGNED5, signatureCount);
        int backwardsCallsUsed = this.parseClassMetadataBands(in, classAttrCalls);
        AttributeLayout innerClassLayout = this.attrMap.getAttributeLayout("InnerClasses", 0);
        int innerClassCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)innerClassLayout);
        int[] classInnerClassesN = this.decodeBandInt("class_InnerClasses_N", in, Codec.UNSIGNED5, innerClassCount);
        int[][] classInnerClassesRC = this.decodeBandInt("class_InnerClasses_RC", in, Codec.UNSIGNED5, classInnerClassesN);
        int[][] classInnerClassesF = this.decodeBandInt("class_InnerClasses_F", in, Codec.UNSIGNED5, classInnerClassesN);
        int flagsCount = 0;
        for (int i3 = 0; i3 < classInnerClassesF.length; ++i3) {
            for (int j = 0; j < classInnerClassesF[i3].length; ++j) {
                if (classInnerClassesF[i3][j] == 0) continue;
                ++flagsCount;
            }
        }
        int[] classInnerClassesOuterRCN = this.decodeBandInt("class_InnerClasses_outer_RCN", in, Codec.UNSIGNED5, flagsCount);
        int[] classInnerClassesNameRUN = this.decodeBandInt("class_InnerClasses_name_RUN", in, Codec.UNSIGNED5, flagsCount);
        AttributeLayout versionLayout = this.attrMap.getAttributeLayout("class-file version", 0);
        int versionCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)versionLayout);
        int[] classFileVersionMinorH = this.decodeBandInt("class_file_version_minor_H", in, Codec.UNSIGNED5, versionCount);
        int[] classFileVersionMajorH = this.decodeBandInt("class_file_version_major_H", in, Codec.UNSIGNED5, versionCount);
        if (versionCount > 0) {
            this.classVersionMajor = new int[this.classCount];
            this.classVersionMinor = new int[this.classCount];
        }
        int defaultVersionMajor = this.header.getDefaultClassMajorVersion();
        int defaultVersionMinor = this.header.getDefaultClassMinorVersion();
        int backwardsCallIndex = backwardsCallsUsed;
        int limit = this.options.hasClassFlagsHi() ? 62 : 31;
        AttributeLayout[] otherLayouts = new AttributeLayout[limit + 1];
        int[] counts = new int[limit + 1];
        List[] otherAttributes = new List[limit + 1];
        for (i = 0; i < limit; ++i) {
            AttributeLayout layout = this.attrMap.getAttributeLayout(i, 0);
            if (layout == null || layout.isDefaultLayout()) continue;
            otherLayouts[i] = layout;
            counts[i] = SegmentUtils.countMatches(this.classFlags, (IMatcher)layout);
        }
        for (i = 0; i < counts.length; ++i) {
            if (counts[i] <= 0) continue;
            NewAttributeBands bands = this.attrMap.getAttributeBands(otherLayouts[i]);
            otherAttributes[i] = bands.parseAttributes(in, counts[i]);
            int numBackwardsCallables = otherLayouts[i].numBackwardsCallables();
            if (numBackwardsCallables <= 0) continue;
            int[] backwardsCalls = new int[numBackwardsCallables];
            System.arraycopy(classAttrCalls, backwardsCallIndex, backwardsCalls, 0, numBackwardsCallables);
            bands.setBackwardsCalls(backwardsCalls);
            backwardsCallIndex += numBackwardsCallables;
        }
        int sourceFileIndex = 0;
        int enclosingMethodIndex = 0;
        int signatureIndex = 0;
        int innerClassIndex = 0;
        int innerClassC2NIndex = 0;
        int versionIndex = 0;
        this.icLocal = new IcTuple[this.classCount][];
        for (int i4 = 0; i4 < this.classCount; ++i4) {
            ClassFileEntry value;
            long result;
            long flag = this.classFlags[i4];
            if (deprecatedLayout.matches(this.classFlags[i4])) {
                this.classAttributes[i4].add(new DeprecatedAttribute());
            }
            if (sourceFileLayout.matches(flag)) {
                result = classSourceFile[sourceFileIndex];
                value = sourceFileLayout.getValue(result, this.cpBands.getConstantPool());
                if (value == null) {
                    String className = this.classThis[i4].substring(this.classThis[i4].lastIndexOf(47) + 1);
                    className = className.substring(className.lastIndexOf(46) + 1);
                    char[] chars = className.toCharArray();
                    int index = -1;
                    for (int j = 0; j < chars.length; ++j) {
                        if (chars[j] > '-') continue;
                        index = j;
                        break;
                    }
                    if (index > -1) {
                        className = className.substring(0, index);
                    }
                    value = this.cpBands.cpUTF8Value(className + ".java", true);
                }
                this.classAttributes[i4].add(new SourceFileAttribute((CPUTF8)value));
                ++sourceFileIndex;
            }
            if (enclosingMethodLayout.matches(flag)) {
                CPClass theClass = this.cpBands.cpClassValue(enclosingMethodRC[enclosingMethodIndex]);
                CPNameAndType theMethod = null;
                if (enclosingMethodRDN[enclosingMethodIndex] != 0) {
                    theMethod = this.cpBands.cpNameAndTypeValue(enclosingMethodRDN[enclosingMethodIndex] - 1);
                }
                this.classAttributes[i4].add(new EnclosingMethodAttribute(theClass, theMethod));
                ++enclosingMethodIndex;
            }
            if (signatureLayout.matches(flag)) {
                result = classSignature[signatureIndex];
                value = (CPUTF8)signatureLayout.getValue(result, this.cpBands.getConstantPool());
                this.classAttributes[i4].add(new SignatureAttribute((CPUTF8)value));
                ++signatureIndex;
            }
            if (innerClassLayout.matches(flag)) {
                this.icLocal[i4] = new IcTuple[classInnerClassesN[innerClassIndex]];
                for (int j = 0; j < this.icLocal[i4].length; ++j) {
                    IcTuple icTuple;
                    int icTupleCIndex = classInnerClassesRC[innerClassIndex][j];
                    int icTupleC2Index = -1;
                    int icTupleNIndex = -1;
                    String icTupleC = cpClass[icTupleCIndex];
                    int icTupleF = classInnerClassesF[innerClassIndex][j];
                    String icTupleC2 = null;
                    String icTupleN = null;
                    if (icTupleF != 0) {
                        icTupleC2Index = classInnerClassesOuterRCN[innerClassC2NIndex];
                        icTupleNIndex = classInnerClassesNameRUN[innerClassC2NIndex];
                        icTupleC2 = cpClass[icTupleC2Index];
                        icTupleN = cpUTF8[icTupleNIndex];
                        ++innerClassC2NIndex;
                    } else {
                        IcBands icBands = this.segment.getIcBands();
                        IcTuple[] icAll = icBands.getIcTuples();
                        for (int k = 0; k < icAll.length; ++k) {
                            if (!icAll[k].getC().equals(icTupleC)) continue;
                            icTupleF = icAll[k].getF();
                            icTupleC2 = icAll[k].getC2();
                            icTupleN = icAll[k].getN();
                            break;
                        }
                    }
                    this.icLocal[i4][j] = icTuple = new IcTuple(icTupleC, icTupleF, icTupleC2, icTupleN, icTupleCIndex, icTupleC2Index, icTupleNIndex, j);
                }
                ++innerClassIndex;
            }
            if (versionLayout.matches(flag)) {
                this.classVersionMajor[i4] = classFileVersionMajorH[versionIndex];
                this.classVersionMinor[i4] = classFileVersionMinorH[versionIndex];
                ++versionIndex;
            } else if (this.classVersionMajor != null) {
                this.classVersionMajor[i4] = defaultVersionMajor;
                this.classVersionMinor[i4] = defaultVersionMinor;
            }
            for (int j = 0; j < otherLayouts.length; ++j) {
                if (otherLayouts[j] == null || !otherLayouts[j].matches(flag)) continue;
                this.classAttributes[i4].add(otherAttributes[j].get(0));
                otherAttributes[j].remove(0);
            }
        }
    }

    private void parseCodeBands(InputStream in) throws Pack200Exception, IOException {
        AttributeLayout layout = this.attrMap.getAttributeLayout("Code", 2);
        int codeCount = SegmentUtils.countMatches(this.methodFlags, (IMatcher)layout);
        int[] codeHeaders = this.decodeBandInt("code_headers", in, Codec.BYTE1, codeCount);
        boolean allCodeHasFlags = this.segment.getSegmentHeader().getOptions().hasAllCodeFlags();
        if (!allCodeHasFlags) {
            this.codeHasAttributes = new boolean[codeCount];
        }
        int codeSpecialHeader = 0;
        for (int i = 0; i < codeCount; ++i) {
            if (codeHeaders[i] != 0) continue;
            ++codeSpecialHeader;
            if (allCodeHasFlags) continue;
            this.codeHasAttributes[i] = true;
        }
        int[] codeMaxStackSpecials = this.decodeBandInt("code_max_stack", in, Codec.UNSIGNED5, codeSpecialHeader);
        int[] codeMaxNALocalsSpecials = this.decodeBandInt("code_max_na_locals", in, Codec.UNSIGNED5, codeSpecialHeader);
        int[] codeHandlerCountSpecials = this.decodeBandInt("code_handler_count", in, Codec.UNSIGNED5, codeSpecialHeader);
        this.codeMaxStack = new int[codeCount];
        this.codeMaxNALocals = new int[codeCount];
        this.codeHandlerCount = new int[codeCount];
        int special = 0;
        for (int i = 0; i < codeCount; ++i) {
            int header = 0xFF & codeHeaders[i];
            if (header < 0) {
                throw new IllegalStateException("Shouldn't get here");
            }
            if (header == 0) {
                this.codeMaxStack[i] = codeMaxStackSpecials[special];
                this.codeMaxNALocals[i] = codeMaxNALocalsSpecials[special];
                this.codeHandlerCount[i] = codeHandlerCountSpecials[special];
                ++special;
                continue;
            }
            if (header <= 144) {
                this.codeMaxStack[i] = (header - 1) % 12;
                this.codeMaxNALocals[i] = (header - 1) / 12;
                this.codeHandlerCount[i] = 0;
                continue;
            }
            if (header <= 208) {
                this.codeMaxStack[i] = (header - 145) % 8;
                this.codeMaxNALocals[i] = (header - 145) / 8;
                this.codeHandlerCount[i] = 1;
                continue;
            }
            if (header <= 255) {
                this.codeMaxStack[i] = (header - 209) % 7;
                this.codeMaxNALocals[i] = (header - 209) / 7;
                this.codeHandlerCount[i] = 2;
                continue;
            }
            throw new IllegalStateException("Shouldn't get here either");
        }
        this.codeHandlerStartP = this.decodeBandInt("code_handler_start_P", in, Codec.BCI5, this.codeHandlerCount);
        this.codeHandlerEndPO = this.decodeBandInt("code_handler_end_PO", in, Codec.BRANCH5, this.codeHandlerCount);
        this.codeHandlerCatchPO = this.decodeBandInt("code_handler_catch_PO", in, Codec.BRANCH5, this.codeHandlerCount);
        this.codeHandlerClassRCN = this.decodeBandInt("code_handler_class_RCN", in, Codec.UNSIGNED5, this.codeHandlerCount);
        int codeFlagsCount = allCodeHasFlags ? codeCount : codeSpecialHeader;
        this.codeAttributes = new List[codeFlagsCount];
        for (int i = 0; i < this.codeAttributes.length; ++i) {
            this.codeAttributes[i] = new ArrayList();
        }
        this.parseCodeAttrBands(in, codeFlagsCount);
    }

    private void parseCodeAttrBands(InputStream in, int codeFlagsCount) throws IOException, Pack200Exception {
        int i;
        long[] codeFlags = this.parseFlags("code_flags", in, codeFlagsCount, Codec.UNSIGNED5, this.segment.getSegmentHeader().getOptions().hasCodeFlagsHi());
        int codeAttrCount = SegmentUtils.countBit16(codeFlags);
        int[] codeAttrCounts = this.decodeBandInt("code_attr_count", in, Codec.UNSIGNED5, codeAttrCount);
        int[][] codeAttrIndexes = this.decodeBandInt("code_attr_indexes", in, Codec.UNSIGNED5, codeAttrCounts);
        int callCount = 0;
        for (int i2 = 0; i2 < codeAttrIndexes.length; ++i2) {
            for (int j = 0; j < codeAttrIndexes[i2].length; ++j) {
                int index = codeAttrIndexes[i2][j];
                AttributeLayout layout = this.attrMap.getAttributeLayout(index, 3);
                callCount += layout.numBackwardsCallables();
            }
        }
        int[] codeAttrCalls = this.decodeBandInt("code_attr_calls", in, Codec.UNSIGNED5, callCount);
        AttributeLayout lineNumberTableLayout = this.attrMap.getAttributeLayout("LineNumberTable", 3);
        int lineNumberTableCount = SegmentUtils.countMatches(codeFlags, (IMatcher)lineNumberTableLayout);
        int[] lineNumberTableN = this.decodeBandInt("code_LineNumberTable_N", in, Codec.UNSIGNED5, lineNumberTableCount);
        int[][] lineNumberTableBciP = this.decodeBandInt("code_LineNumberTable_bci_P", in, Codec.BCI5, lineNumberTableN);
        int[][] lineNumberTableLine = this.decodeBandInt("code_LineNumberTable_line", in, Codec.UNSIGNED5, lineNumberTableN);
        AttributeLayout localVariableTableLayout = this.attrMap.getAttributeLayout("LocalVariableTable", 3);
        AttributeLayout localVariableTypeTableLayout = this.attrMap.getAttributeLayout("LocalVariableTypeTable", 3);
        int lengthLocalVariableNBand = SegmentUtils.countMatches(codeFlags, (IMatcher)localVariableTableLayout);
        int[] localVariableTableN = this.decodeBandInt("code_LocalVariableTable_N", in, Codec.UNSIGNED5, lengthLocalVariableNBand);
        int[][] localVariableTableBciP = this.decodeBandInt("code_LocalVariableTable_bci_P", in, Codec.BCI5, localVariableTableN);
        int[][] localVariableTableSpanO = this.decodeBandInt("code_LocalVariableTable_span_O", in, Codec.BRANCH5, localVariableTableN);
        CPUTF8[][] localVariableTableNameRU = this.parseCPUTF8References("code_LocalVariableTable_name_RU", in, Codec.UNSIGNED5, localVariableTableN);
        CPUTF8[][] localVariableTableTypeRS = this.parseCPSignatureReferences("code_LocalVariableTable_type_RS", in, Codec.UNSIGNED5, localVariableTableN);
        int[][] localVariableTableSlot = this.decodeBandInt("code_LocalVariableTable_slot", in, Codec.UNSIGNED5, localVariableTableN);
        int lengthLocalVariableTypeTableNBand = SegmentUtils.countMatches(codeFlags, (IMatcher)localVariableTypeTableLayout);
        int[] localVariableTypeTableN = this.decodeBandInt("code_LocalVariableTypeTable_N", in, Codec.UNSIGNED5, lengthLocalVariableTypeTableNBand);
        int[][] localVariableTypeTableBciP = this.decodeBandInt("code_LocalVariableTypeTable_bci_P", in, Codec.BCI5, localVariableTypeTableN);
        int[][] localVariableTypeTableSpanO = this.decodeBandInt("code_LocalVariableTypeTable_span_O", in, Codec.BRANCH5, localVariableTypeTableN);
        CPUTF8[][] localVariableTypeTableNameRU = this.parseCPUTF8References("code_LocalVariableTypeTable_name_RU", in, Codec.UNSIGNED5, localVariableTypeTableN);
        CPUTF8[][] localVariableTypeTableTypeRS = this.parseCPSignatureReferences("code_LocalVariableTypeTable_type_RS", in, Codec.UNSIGNED5, localVariableTypeTableN);
        int[][] localVariableTypeTableSlot = this.decodeBandInt("code_LocalVariableTypeTable_slot", in, Codec.UNSIGNED5, localVariableTypeTableN);
        int backwardsCallIndex = 0;
        int limit = this.options.hasCodeFlagsHi() ? 62 : 31;
        AttributeLayout[] otherLayouts = new AttributeLayout[limit + 1];
        int[] counts = new int[limit + 1];
        List[] otherAttributes = new List[limit + 1];
        for (i = 0; i < limit; ++i) {
            AttributeLayout layout = this.attrMap.getAttributeLayout(i, 3);
            if (layout == null || layout.isDefaultLayout()) continue;
            otherLayouts[i] = layout;
            counts[i] = SegmentUtils.countMatches(codeFlags, (IMatcher)layout);
        }
        for (i = 0; i < counts.length; ++i) {
            if (counts[i] <= 0) continue;
            NewAttributeBands bands = this.attrMap.getAttributeBands(otherLayouts[i]);
            otherAttributes[i] = bands.parseAttributes(in, counts[i]);
            int numBackwardsCallables = otherLayouts[i].numBackwardsCallables();
            if (numBackwardsCallables <= 0) continue;
            int[] backwardsCalls = new int[numBackwardsCallables];
            System.arraycopy(codeAttrCalls, backwardsCallIndex, backwardsCalls, 0, numBackwardsCallables);
            bands.setBackwardsCalls(backwardsCalls);
            backwardsCallIndex += numBackwardsCallables;
        }
        int lineNumberIndex = 0;
        int lvtIndex = 0;
        int lvttIndex = 0;
        for (int i3 = 0; i3 < codeFlagsCount; ++i3) {
            if (lineNumberTableLayout.matches(codeFlags[i3])) {
                LineNumberTableAttribute lnta = new LineNumberTableAttribute(lineNumberTableN[lineNumberIndex], lineNumberTableBciP[lineNumberIndex], lineNumberTableLine[lineNumberIndex]);
                ++lineNumberIndex;
                this.codeAttributes[i3].add(lnta);
            }
            if (localVariableTableLayout.matches(codeFlags[i3])) {
                LocalVariableTableAttribute lvta = new LocalVariableTableAttribute(localVariableTableN[lvtIndex], localVariableTableBciP[lvtIndex], localVariableTableSpanO[lvtIndex], localVariableTableNameRU[lvtIndex], localVariableTableTypeRS[lvtIndex], localVariableTableSlot[lvtIndex]);
                ++lvtIndex;
                this.codeAttributes[i3].add(lvta);
            }
            if (localVariableTypeTableLayout.matches(codeFlags[i3])) {
                LocalVariableTypeTableAttribute lvtta = new LocalVariableTypeTableAttribute(localVariableTypeTableN[lvttIndex], localVariableTypeTableBciP[lvttIndex], localVariableTypeTableSpanO[lvttIndex], localVariableTypeTableNameRU[lvttIndex], localVariableTypeTableTypeRS[lvttIndex], localVariableTypeTableSlot[lvttIndex]);
                ++lvttIndex;
                this.codeAttributes[i3].add(lvtta);
            }
            for (int j = 0; j < otherLayouts.length; ++j) {
                if (otherLayouts[j] == null || !otherLayouts[j].matches(codeFlags[i3])) continue;
                this.codeAttributes[i3].add(otherAttributes[j].get(0));
                otherAttributes[j].remove(0);
            }
        }
    }

    private int parseFieldMetadataBands(InputStream in, int[] fieldAttrCalls) throws Pack200Exception, IOException {
        int backwardsCallsUsed = 0;
        String[] RxA = new String[]{"RVA", "RIA"};
        AttributeLayout rvaLayout = this.attrMap.getAttributeLayout("RuntimeVisibleAnnotations", 1);
        AttributeLayout riaLayout = this.attrMap.getAttributeLayout("RuntimeInvisibleAnnotations", 1);
        int rvaCount = SegmentUtils.countMatches(this.fieldFlags, (IMatcher)rvaLayout);
        int riaCount = SegmentUtils.countMatches(this.fieldFlags, (IMatcher)riaLayout);
        int[] RxACount = new int[]{rvaCount, riaCount};
        int[] backwardsCalls = new int[]{0, 0};
        if (rvaCount > 0) {
            backwardsCalls[0] = fieldAttrCalls[0];
            ++backwardsCallsUsed;
            if (riaCount > 0) {
                backwardsCalls[1] = fieldAttrCalls[1];
                ++backwardsCallsUsed;
            }
        } else if (riaCount > 0) {
            backwardsCalls[1] = fieldAttrCalls[0];
            ++backwardsCallsUsed;
        }
        MetadataBandGroup[] mb = this.parseMetadata(in, RxA, RxACount, backwardsCalls, "field");
        List rvaAttributes = mb[0].getAttributes();
        List riaAttributes = mb[1].getAttributes();
        int rvaAttributesIndex = 0;
        int riaAttributesIndex = 0;
        for (int i = 0; i < this.fieldFlags.length; ++i) {
            for (int j = 0; j < this.fieldFlags[i].length; ++j) {
                if (rvaLayout.matches(this.fieldFlags[i][j])) {
                    this.fieldAttributes[i][j].add(rvaAttributes.get(rvaAttributesIndex++));
                }
                if (!riaLayout.matches(this.fieldFlags[i][j])) continue;
                this.fieldAttributes[i][j].add(riaAttributes.get(riaAttributesIndex++));
            }
        }
        return backwardsCallsUsed;
    }

    private MetadataBandGroup[] parseMetadata(InputStream in, String[] RxA, int[] RxACount, int[] backwardsCallCounts, String contextName) throws IOException, Pack200Exception {
        MetadataBandGroup[] mbg = new MetadataBandGroup[RxA.length];
        for (int i = 0; i < RxA.length; ++i) {
            mbg[i] = new MetadataBandGroup(RxA[i], this.cpBands);
            String rxa = RxA[i];
            if (rxa.indexOf(80) >= 0) {
                mbg[i].param_NB = this.decodeBandInt(contextName + "_" + rxa + "_param_NB", in, Codec.BYTE1, RxACount[i]);
            }
            int pairCount = 0;
            if (!rxa.equals("AD")) {
                mbg[i].anno_N = this.decodeBandInt(contextName + "_" + rxa + "_anno_N", in, Codec.UNSIGNED5, RxACount[i]);
                mbg[i].type_RS = this.parseCPSignatureReferences(contextName + "_" + rxa + "_type_RS", in, Codec.UNSIGNED5, mbg[i].anno_N);
                mbg[i].pair_N = this.decodeBandInt(contextName + "_" + rxa + "_pair_N", in, Codec.UNSIGNED5, mbg[i].anno_N);
                for (int j = 0; j < mbg[i].pair_N.length; ++j) {
                    for (int k = 0; k < mbg[i].pair_N[j].length; ++k) {
                        pairCount += mbg[i].pair_N[j][k];
                    }
                }
                mbg[i].name_RU = this.parseCPUTF8References(contextName + "_" + rxa + "_name_RU", in, Codec.UNSIGNED5, pairCount);
            } else {
                pairCount = RxACount[i];
            }
            mbg[i].T = this.decodeBandInt(contextName + "_" + rxa + "_T", in, Codec.BYTE1, pairCount + backwardsCallCounts[i]);
            int ICount = 0;
            int DCount = 0;
            int FCount = 0;
            int JCount = 0;
            int cCount = 0;
            int eCount = 0;
            int sCount = 0;
            int arrayCount = 0;
            int atCount = 0;
            block14: for (int j = 0; j < mbg[i].T.length; ++j) {
                char c = (char)mbg[i].T[j];
                switch (c) {
                    case 'B': 
                    case 'C': 
                    case 'I': 
                    case 'S': 
                    case 'Z': {
                        ++ICount;
                        continue block14;
                    }
                    case 'D': {
                        ++DCount;
                        continue block14;
                    }
                    case 'F': {
                        ++FCount;
                        continue block14;
                    }
                    case 'J': {
                        ++JCount;
                        continue block14;
                    }
                    case 'c': {
                        ++cCount;
                        continue block14;
                    }
                    case 'e': {
                        ++eCount;
                        continue block14;
                    }
                    case 's': {
                        ++sCount;
                        continue block14;
                    }
                    case '[': {
                        ++arrayCount;
                        continue block14;
                    }
                    case '@': {
                        ++atCount;
                    }
                }
            }
            mbg[i].caseI_KI = this.parseCPIntReferences(contextName + "_" + rxa + "_caseI_KI", in, Codec.UNSIGNED5, ICount);
            mbg[i].caseD_KD = this.parseCPDoubleReferences(contextName + "_" + rxa + "_caseD_KD", in, Codec.UNSIGNED5, DCount);
            mbg[i].caseF_KF = this.parseCPFloatReferences(contextName + "_" + rxa + "_caseF_KF", in, Codec.UNSIGNED5, FCount);
            mbg[i].caseJ_KJ = this.parseCPLongReferences(contextName + "_" + rxa + "_caseJ_KJ", in, Codec.UNSIGNED5, JCount);
            mbg[i].casec_RS = this.parseCPSignatureReferences(contextName + "_" + rxa + "_casec_RS", in, Codec.UNSIGNED5, cCount);
            mbg[i].caseet_RS = this.parseReferences(contextName + "_" + rxa + "_caseet_RS", in, Codec.UNSIGNED5, eCount, this.cpBands.getCpSignature());
            mbg[i].caseec_RU = this.parseReferences(contextName + "_" + rxa + "_caseec_RU", in, Codec.UNSIGNED5, eCount, this.cpBands.getCpUTF8());
            mbg[i].cases_RU = this.parseCPUTF8References(contextName + "_" + rxa + "_cases_RU", in, Codec.UNSIGNED5, sCount);
            mbg[i].casearray_N = this.decodeBandInt(contextName + "_" + rxa + "_casearray_N", in, Codec.UNSIGNED5, arrayCount);
            mbg[i].nesttype_RS = this.parseCPUTF8References(contextName + "_" + rxa + "_nesttype_RS", in, Codec.UNSIGNED5, atCount);
            mbg[i].nestpair_N = this.decodeBandInt(contextName + "_" + rxa + "_nestpair_N", in, Codec.UNSIGNED5, atCount);
            int nestPairCount = 0;
            for (int j = 0; j < mbg[i].nestpair_N.length; ++j) {
                nestPairCount += mbg[i].nestpair_N[j];
            }
            mbg[i].nestname_RU = this.parseCPUTF8References(contextName + "_" + rxa + "_nestname_RU", in, Codec.UNSIGNED5, nestPairCount);
        }
        return mbg;
    }

    private int parseMethodMetadataBands(InputStream in, int[] methodAttrCalls) throws Pack200Exception, IOException {
        int i;
        int backwardsCallsUsed = 0;
        String[] RxA = new String[]{"RVA", "RIA", "RVPA", "RIPA", "AD"};
        int[] rxaCounts = new int[]{0, 0, 0, 0, 0};
        AttributeLayout rvaLayout = this.attrMap.getAttributeLayout("RuntimeVisibleAnnotations", 2);
        AttributeLayout riaLayout = this.attrMap.getAttributeLayout("RuntimeInvisibleAnnotations", 2);
        AttributeLayout rvpaLayout = this.attrMap.getAttributeLayout("RuntimeVisibleParameterAnnotations", 2);
        AttributeLayout ripaLayout = this.attrMap.getAttributeLayout("RuntimeInvisibleParameterAnnotations", 2);
        AttributeLayout adLayout = this.attrMap.getAttributeLayout("AnnotationDefault", 2);
        AttributeLayout[] rxaLayouts = new AttributeLayout[]{rvaLayout, riaLayout, rvpaLayout, ripaLayout, adLayout};
        for (int i2 = 0; i2 < rxaLayouts.length; ++i2) {
            rxaCounts[i2] = SegmentUtils.countMatches(this.methodFlags, (IMatcher)rxaLayouts[i2]);
        }
        int[] backwardsCalls = new int[5];
        int methodAttrIndex = 0;
        for (int i3 = 0; i3 < backwardsCalls.length; ++i3) {
            if (rxaCounts[i3] > 0) {
                ++backwardsCallsUsed;
                backwardsCalls[i3] = methodAttrCalls[methodAttrIndex];
                ++methodAttrIndex;
                continue;
            }
            backwardsCalls[i3] = 0;
        }
        MetadataBandGroup[] mbgs = this.parseMetadata(in, RxA, rxaCounts, backwardsCalls, "method");
        List[] attributeLists = new List[RxA.length];
        int[] attributeListIndexes = new int[RxA.length];
        for (i = 0; i < mbgs.length; ++i) {
            attributeLists[i] = mbgs[i].getAttributes();
            attributeListIndexes[i] = 0;
        }
        for (i = 0; i < this.methodFlags.length; ++i) {
            for (int j = 0; j < this.methodFlags[i].length; ++j) {
                for (int k = 0; k < rxaLayouts.length; ++k) {
                    if (!rxaLayouts[k].matches(this.methodFlags[i][j])) continue;
                    int n = k;
                    int n2 = attributeListIndexes[n];
                    attributeListIndexes[n] = n2 + 1;
                    this.methodAttributes[i][j].add(attributeLists[k].get(n2));
                }
            }
        }
        return backwardsCallsUsed;
    }

    private int parseClassMetadataBands(InputStream in, int[] classAttrCalls) throws Pack200Exception, IOException {
        int numBackwardsCalls = 0;
        String[] RxA = new String[]{"RVA", "RIA"};
        AttributeLayout rvaLayout = this.attrMap.getAttributeLayout("RuntimeVisibleAnnotations", 0);
        AttributeLayout riaLayout = this.attrMap.getAttributeLayout("RuntimeInvisibleAnnotations", 0);
        int rvaCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)rvaLayout);
        int riaCount = SegmentUtils.countMatches(this.classFlags, (IMatcher)riaLayout);
        int[] RxACount = new int[]{rvaCount, riaCount};
        int[] backwardsCalls = new int[]{0, 0};
        if (rvaCount > 0) {
            ++numBackwardsCalls;
            backwardsCalls[0] = classAttrCalls[0];
            if (riaCount > 0) {
                ++numBackwardsCalls;
                backwardsCalls[1] = classAttrCalls[1];
            }
        } else if (riaCount > 0) {
            ++numBackwardsCalls;
            backwardsCalls[1] = classAttrCalls[0];
        }
        MetadataBandGroup[] mbgs = this.parseMetadata(in, RxA, RxACount, backwardsCalls, "class");
        List rvaAttributes = mbgs[0].getAttributes();
        List riaAttributes = mbgs[1].getAttributes();
        int rvaAttributesIndex = 0;
        int riaAttributesIndex = 0;
        for (int i = 0; i < this.classFlags.length; ++i) {
            if (rvaLayout.matches(this.classFlags[i])) {
                this.classAttributes[i].add(rvaAttributes.get(rvaAttributesIndex++));
            }
            if (!riaLayout.matches(this.classFlags[i])) continue;
            this.classAttributes[i].add(riaAttributes.get(riaAttributesIndex++));
        }
        return numBackwardsCalls;
    }

    public ArrayList[] getClassAttributes() {
        return this.classAttributes;
    }

    public int[] getClassFieldCount() {
        return this.classFieldCount;
    }

    public long[] getRawClassFlags() {
        return this.classFlags;
    }

    public long[] getClassFlags() throws Pack200Exception {
        if (this.classAccessFlags == null) {
            int i;
            long mask = 32767L;
            for (i = 0; i < 16; ++i) {
                AttributeLayout layout = this.attrMap.getAttributeLayout(i, 0);
                if (layout == null || layout.isDefaultLayout()) continue;
                mask &= (long)(~(1 << i));
            }
            this.classAccessFlags = new long[this.classFlags.length];
            for (i = 0; i < this.classFlags.length; ++i) {
                this.classAccessFlags[i] = this.classFlags[i] & mask;
            }
        }
        return this.classAccessFlags;
    }

    public int[][] getClassInterfacesInts() {
        return this.classInterfacesInts;
    }

    public int[] getClassMethodCount() {
        return this.classMethodCount;
    }

    public int[] getClassSuperInts() {
        return this.classSuperInts;
    }

    public int[] getClassThisInts() {
        return this.classThisInts;
    }

    public int[] getCodeMaxNALocals() {
        return this.codeMaxNALocals;
    }

    public int[] getCodeMaxStack() {
        return this.codeMaxStack;
    }

    public ArrayList[][] getFieldAttributes() {
        return this.fieldAttributes;
    }

    public int[][] getFieldDescrInts() {
        return this.fieldDescrInts;
    }

    public int[][] getMethodDescrInts() {
        return this.methodDescrInts;
    }

    public long[][] getFieldFlags() throws Pack200Exception {
        if (this.fieldAccessFlags == null) {
            int i;
            long mask = 32767L;
            for (i = 0; i < 16; ++i) {
                AttributeLayout layout = this.attrMap.getAttributeLayout(i, 1);
                if (layout == null || layout.isDefaultLayout()) continue;
                mask &= (long)(~(1 << i));
            }
            this.fieldAccessFlags = new long[this.fieldFlags.length][];
            for (i = 0; i < this.fieldFlags.length; ++i) {
                this.fieldAccessFlags[i] = new long[this.fieldFlags[i].length];
                for (int j = 0; j < this.fieldFlags[i].length; ++j) {
                    this.fieldAccessFlags[i][j] = this.fieldFlags[i][j] & mask;
                }
            }
        }
        return this.fieldAccessFlags;
    }

    public ArrayList getOrderedCodeAttributes() {
        ArrayList orderedAttributeList = new ArrayList(this.codeAttributes.length);
        for (int classIndex = 0; classIndex < this.codeAttributes.length; ++classIndex) {
            ArrayList<Attribute> currentAttributes = new ArrayList<Attribute>(this.codeAttributes[classIndex].size());
            for (int attributeIndex = 0; attributeIndex < this.codeAttributes[classIndex].size(); ++attributeIndex) {
                Attribute attribute = (Attribute)this.codeAttributes[classIndex].get(attributeIndex);
                currentAttributes.add(attribute);
            }
            orderedAttributeList.add(currentAttributes);
        }
        return orderedAttributeList;
    }

    public ArrayList[][] getMethodAttributes() {
        return this.methodAttributes;
    }

    public String[][] getMethodDescr() {
        return this.methodDescr;
    }

    public long[][] getMethodFlags() throws Pack200Exception {
        if (this.methodAccessFlags == null) {
            int i;
            long mask = 32767L;
            for (i = 0; i < 16; ++i) {
                AttributeLayout layout = this.attrMap.getAttributeLayout(i, 2);
                if (layout == null || layout.isDefaultLayout()) continue;
                mask &= (long)(~(1 << i));
            }
            this.methodAccessFlags = new long[this.methodFlags.length][];
            for (i = 0; i < this.methodFlags.length; ++i) {
                this.methodAccessFlags[i] = new long[this.methodFlags[i].length];
                for (int j = 0; j < this.methodFlags[i].length; ++j) {
                    this.methodAccessFlags[i][j] = this.methodFlags[i][j] & mask;
                }
            }
        }
        return this.methodAccessFlags;
    }

    public int[] getClassVersionMajor() {
        return this.classVersionMajor;
    }

    public int[] getClassVersionMinor() {
        return this.classVersionMinor;
    }

    public int[] getCodeHandlerCount() {
        return this.codeHandlerCount;
    }

    public int[][] getCodeHandlerCatchPO() {
        return this.codeHandlerCatchPO;
    }

    public int[][] getCodeHandlerClassRCN() {
        return this.codeHandlerClassRCN;
    }

    public int[][] getCodeHandlerEndPO() {
        return this.codeHandlerEndPO;
    }

    public int[][] getCodeHandlerStartP() {
        return this.codeHandlerStartP;
    }

    public IcTuple[][] getIcLocal() {
        return this.icLocal;
    }

    public boolean[] getCodeHasAttributes() {
        return this.codeHasAttributes;
    }
}

