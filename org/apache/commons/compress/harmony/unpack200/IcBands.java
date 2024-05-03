/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.BandSet;
import org.apache.commons.compress.harmony.unpack200.IcTuple;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.SegmentUtils;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class IcBands
extends BandSet {
    private IcTuple[] icAll;
    private final String[] cpUTF8;
    private final String[] cpClass;
    private Map thisClassToTuple;
    private Map outerClassToTuples;

    public IcBands(Segment segment) {
        super(segment);
        this.cpClass = segment.getCpBands().getCpClass();
        this.cpUTF8 = segment.getCpBands().getCpUTF8();
    }

    @Override
    public void read(InputStream in) throws IOException, Pack200Exception {
        int innerClassCount = this.header.getInnerClassCount();
        int[] icThisClassInts = this.decodeBandInt("ic_this_class", in, Codec.UDELTA5, innerClassCount);
        String[] icThisClass = this.getReferences(icThisClassInts, this.cpClass);
        int[] icFlags = this.decodeBandInt("ic_flags", in, Codec.UNSIGNED5, innerClassCount);
        int outerClasses = SegmentUtils.countBit16(icFlags);
        int[] icOuterClassInts = this.decodeBandInt("ic_outer_class", in, Codec.DELTA5, outerClasses);
        String[] icOuterClass = new String[outerClasses];
        for (int i = 0; i < icOuterClass.length; ++i) {
            icOuterClass[i] = icOuterClassInts[i] == 0 ? null : this.cpClass[icOuterClassInts[i] - 1];
        }
        int[] icNameInts = this.decodeBandInt("ic_name", in, Codec.DELTA5, outerClasses);
        String[] icName = new String[outerClasses];
        for (int i = 0; i < icName.length; ++i) {
            icName[i] = icNameInts[i] == 0 ? null : this.cpUTF8[icNameInts[i] - 1];
        }
        this.icAll = new IcTuple[icThisClass.length];
        int index = 0;
        for (int i = 0; i < icThisClass.length; ++i) {
            String icTupleC = icThisClass[i];
            int icTupleF = icFlags[i];
            String icTupleC2 = null;
            String icTupleN = null;
            int cIndex = icThisClassInts[i];
            int c2Index = -1;
            int nIndex = -1;
            if ((icFlags[i] & 0x10000) != 0) {
                icTupleC2 = icOuterClass[index];
                icTupleN = icName[index];
                c2Index = icOuterClassInts[index] - 1;
                nIndex = icNameInts[index] - 1;
                ++index;
            }
            this.icAll[i] = new IcTuple(icTupleC, icTupleF, icTupleC2, icTupleN, cIndex, c2Index, nIndex, i);
        }
    }

    @Override
    public void unpack() throws IOException, Pack200Exception {
        IcTuple[] allTuples = this.getIcTuples();
        this.thisClassToTuple = new HashMap(allTuples.length);
        this.outerClassToTuples = new HashMap(allTuples.length);
        for (int index = 0; index < allTuples.length; ++index) {
            IcTuple tuple = allTuples[index];
            IcTuple result = this.thisClassToTuple.put(tuple.thisClassString(), tuple);
            if (result != null) {
                throw new Error("Collision detected in <thisClassString, IcTuple> mapping. There are at least two inner clases with the same name.");
            }
            if ((tuple.isAnonymous() || tuple.outerIsAnonymous()) && !tuple.nestedExplicitFlagSet()) continue;
            String key = tuple.outerClassString();
            ArrayList<IcTuple> bucket = (ArrayList<IcTuple>)this.outerClassToTuples.get(key);
            if (bucket == null) {
                bucket = new ArrayList<IcTuple>();
                this.outerClassToTuples.put(key, bucket);
            }
            bucket.add(tuple);
        }
    }

    public IcTuple[] getIcTuples() {
        return this.icAll;
    }

    public IcTuple[] getRelevantIcTuples(String className, ClassConstantPool cp) {
        HashSet<IcTuple> relevantTuplesContains = new HashSet<IcTuple>();
        ArrayList<IcTuple> relevantTuples = new ArrayList<IcTuple>();
        List relevantCandidates = (List)this.outerClassToTuples.get(className);
        if (relevantCandidates != null) {
            for (int index = 0; index < relevantCandidates.size(); ++index) {
                IcTuple tuple = (IcTuple)relevantCandidates.get(index);
                relevantTuplesContains.add(tuple);
                relevantTuples.add(tuple);
            }
        }
        List entries = cp.entries();
        for (int eIndex = 0; eIndex < entries.size(); ++eIndex) {
            ConstantPoolEntry entry = (ConstantPoolEntry)entries.get(eIndex);
            if (!(entry instanceof CPClass)) continue;
            CPClass clazz = (CPClass)entry;
            IcTuple relevant = (IcTuple)this.thisClassToTuple.get(clazz.name);
            if (relevant == null || !relevantTuplesContains.add(relevant)) continue;
            relevantTuples.add(relevant);
        }
        ArrayList<IcTuple> tuplesToScan = new ArrayList<IcTuple>(relevantTuples);
        ArrayList<IcTuple> tuplesToAdd = new ArrayList<IcTuple>();
        while (tuplesToScan.size() > 0) {
            int index;
            tuplesToAdd.clear();
            for (index = 0; index < tuplesToScan.size(); ++index) {
                IcTuple aRelevantTuple = (IcTuple)tuplesToScan.get(index);
                IcTuple relevant = (IcTuple)this.thisClassToTuple.get(aRelevantTuple.outerClassString());
                if (relevant == null || aRelevantTuple.outerIsAnonymous()) continue;
                tuplesToAdd.add(relevant);
            }
            tuplesToScan.clear();
            for (index = 0; index < tuplesToAdd.size(); ++index) {
                IcTuple tuple = (IcTuple)tuplesToAdd.get(index);
                if (!relevantTuplesContains.add(tuple)) continue;
                relevantTuples.add(tuple);
                tuplesToScan.add(tuple);
            }
        }
        Collections.sort(relevantTuples, (arg0, arg1) -> {
            Integer index1 = ((IcTuple)arg0).getTupleIndex();
            Integer index2 = ((IcTuple)arg1).getTupleIndex();
            return index1.compareTo(index2);
        });
        IcTuple[] relevantTuplesArray = new IcTuple[relevantTuples.size()];
        for (int i = 0; i < relevantTuplesArray.length; ++i) {
            relevantTuplesArray[i] = (IcTuple)relevantTuples.get(i);
        }
        return relevantTuplesArray;
    }
}

