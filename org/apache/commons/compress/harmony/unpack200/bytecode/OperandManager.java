/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;

public class OperandManager {
    int[] bcCaseCount;
    int[] bcCaseValue;
    int[] bcByte;
    int[] bcShort;
    int[] bcLocal;
    int[] bcLabel;
    int[] bcIntRef;
    int[] bcFloatRef;
    int[] bcLongRef;
    int[] bcDoubleRef;
    int[] bcStringRef;
    int[] bcClassRef;
    int[] bcFieldRef;
    int[] bcMethodRef;
    int[] bcIMethodRef;
    int[] bcThisField;
    int[] bcSuperField;
    int[] bcThisMethod;
    int[] bcSuperMethod;
    int[] bcInitRef;
    int[] wideByteCodes;
    int bcCaseCountIndex;
    int bcCaseValueIndex;
    int bcByteIndex;
    int bcShortIndex;
    int bcLocalIndex;
    int bcLabelIndex;
    int bcIntRefIndex;
    int bcFloatRefIndex;
    int bcLongRefIndex;
    int bcDoubleRefIndex;
    int bcStringRefIndex;
    int bcClassRefIndex;
    int bcFieldRefIndex;
    int bcMethodRefIndex;
    int bcIMethodRefIndex;
    int bcThisFieldIndex;
    int bcSuperFieldIndex;
    int bcThisMethodIndex;
    int bcSuperMethodIndex;
    int bcInitRefIndex;
    int wideByteCodeIndex;
    Segment segment;
    String currentClass;
    String superClass;
    String newClass;

    public OperandManager(int[] bcCaseCount, int[] bcCaseValue, int[] bcByte, int[] bcShort, int[] bcLocal, int[] bcLabel, int[] bcIntRef, int[] bcFloatRef, int[] bcLongRef, int[] bcDoubleRef, int[] bcStringRef, int[] bcClassRef, int[] bcFieldRef, int[] bcMethodRef, int[] bcIMethodRef, int[] bcThisField, int[] bcSuperField, int[] bcThisMethod, int[] bcSuperMethod, int[] bcInitRef, int[] wideByteCodes) {
        this.bcCaseCount = bcCaseCount;
        this.bcCaseValue = bcCaseValue;
        this.bcByte = bcByte;
        this.bcShort = bcShort;
        this.bcLocal = bcLocal;
        this.bcLabel = bcLabel;
        this.bcIntRef = bcIntRef;
        this.bcFloatRef = bcFloatRef;
        this.bcLongRef = bcLongRef;
        this.bcDoubleRef = bcDoubleRef;
        this.bcStringRef = bcStringRef;
        this.bcClassRef = bcClassRef;
        this.bcFieldRef = bcFieldRef;
        this.bcMethodRef = bcMethodRef;
        this.bcIMethodRef = bcIMethodRef;
        this.bcThisField = bcThisField;
        this.bcSuperField = bcSuperField;
        this.bcThisMethod = bcThisMethod;
        this.bcSuperMethod = bcSuperMethod;
        this.bcInitRef = bcInitRef;
        this.wideByteCodes = wideByteCodes;
    }

    public int nextCaseCount() {
        return this.bcCaseCount[this.bcCaseCountIndex++];
    }

    public int nextCaseValues() {
        return this.bcCaseValue[this.bcCaseValueIndex++];
    }

    public int nextByte() {
        return this.bcByte[this.bcByteIndex++];
    }

    public int nextShort() {
        return this.bcShort[this.bcShortIndex++];
    }

    public int nextLocal() {
        return this.bcLocal[this.bcLocalIndex++];
    }

    public int nextLabel() {
        return this.bcLabel[this.bcLabelIndex++];
    }

    public int nextIntRef() {
        return this.bcIntRef[this.bcIntRefIndex++];
    }

    public int nextFloatRef() {
        return this.bcFloatRef[this.bcFloatRefIndex++];
    }

    public int nextLongRef() {
        return this.bcLongRef[this.bcLongRefIndex++];
    }

    public int nextDoubleRef() {
        return this.bcDoubleRef[this.bcDoubleRefIndex++];
    }

    public int nextStringRef() {
        return this.bcStringRef[this.bcStringRefIndex++];
    }

    public int nextClassRef() {
        return this.bcClassRef[this.bcClassRefIndex++];
    }

    public int nextFieldRef() {
        return this.bcFieldRef[this.bcFieldRefIndex++];
    }

    public int nextMethodRef() {
        return this.bcMethodRef[this.bcMethodRefIndex++];
    }

    public int nextIMethodRef() {
        return this.bcIMethodRef[this.bcIMethodRefIndex++];
    }

    public int nextThisFieldRef() {
        return this.bcThisField[this.bcThisFieldIndex++];
    }

    public int nextSuperFieldRef() {
        return this.bcSuperField[this.bcSuperFieldIndex++];
    }

    public int nextThisMethodRef() {
        return this.bcThisMethod[this.bcThisMethodIndex++];
    }

    public int nextSuperMethodRef() {
        return this.bcSuperMethod[this.bcSuperMethodIndex++];
    }

    public int nextInitRef() {
        return this.bcInitRef[this.bcInitRefIndex++];
    }

    public int nextWideByteCode() {
        return this.wideByteCodes[this.wideByteCodeIndex++];
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public SegmentConstantPool globalConstantPool() {
        return this.segment.getConstantPool();
    }

    public void setCurrentClass(String string) {
        this.currentClass = string;
    }

    public void setSuperClass(String string) {
        this.superClass = string;
    }

    public void setNewClass(String string) {
        this.newClass = string;
    }

    public String getCurrentClass() {
        if (null == this.currentClass) {
            throw new Error("Current class not set yet");
        }
        return this.currentClass;
    }

    public String getSuperClass() {
        if (null == this.superClass) {
            throw new Error("SuperClass not set yet");
        }
        return this.superClass;
    }

    public String getNewClass() {
        if (null == this.newClass) {
            throw new Error("New class not set yet");
        }
        return this.newClass;
    }
}

