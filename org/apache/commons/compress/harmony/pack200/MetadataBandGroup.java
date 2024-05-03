/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.IntList;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;

public class MetadataBandGroup
extends BandSet {
    public static final int CONTEXT_CLASS = 0;
    public static final int CONTEXT_FIELD = 1;
    public static final int CONTEXT_METHOD = 2;
    private final String type;
    private int numBackwardsCalls = 0;
    public IntList param_NB = new IntList();
    public IntList anno_N = new IntList();
    public List type_RS = new ArrayList();
    public IntList pair_N = new IntList();
    public List name_RU = new ArrayList();
    public List T = new ArrayList();
    public List caseI_KI = new ArrayList();
    public List caseD_KD = new ArrayList();
    public List caseF_KF = new ArrayList();
    public List caseJ_KJ = new ArrayList();
    public List casec_RS = new ArrayList();
    public List caseet_RS = new ArrayList();
    public List caseec_RU = new ArrayList();
    public List cases_RU = new ArrayList();
    public IntList casearray_N = new IntList();
    public List nesttype_RS = new ArrayList();
    public IntList nestpair_N = new IntList();
    public List nestname_RU = new ArrayList();
    private final CpBands cpBands;
    private final int context;

    public MetadataBandGroup(String type, int context, CpBands cpBands, SegmentHeader segmentHeader, int effort) {
        super(effort, segmentHeader);
        this.type = type;
        this.cpBands = cpBands;
        this.context = context;
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing metadata band group...");
        if (this.hasContent()) {
            String contextStr = this.context == 0 ? "Class" : (this.context == 1 ? "Field" : "Method");
            byte[] encodedBand = null;
            if (!this.type.equals("AD")) {
                if (this.type.indexOf(80) != -1) {
                    encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " param_NB", this.param_NB.toArray(), Codec.BYTE1);
                    out.write(encodedBand);
                    PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " anno_N[" + this.param_NB.size() + "]");
                }
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " anno_N", this.anno_N.toArray(), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " anno_N[" + this.anno_N.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " type_RS", this.cpEntryListToArray(this.type_RS), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " type_RS[" + this.type_RS.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " pair_N", this.pair_N.toArray(), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " pair_N[" + this.pair_N.size() + "]");
                encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " name_RU", this.cpEntryListToArray(this.name_RU), Codec.UNSIGNED5);
                out.write(encodedBand);
                PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " name_RU[" + this.name_RU.size() + "]");
            }
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " T", this.tagListToArray(this.T), Codec.BYTE1);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " T[" + this.T.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseI_KI", this.cpEntryListToArray(this.caseI_KI), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseI_KI[" + this.caseI_KI.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseD_KD", this.cpEntryListToArray(this.caseD_KD), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseD_KD[" + this.caseD_KD.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseF_KF", this.cpEntryListToArray(this.caseF_KF), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseF_KF[" + this.caseF_KF.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseJ_KJ", this.cpEntryListToArray(this.caseJ_KJ), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseJ_KJ[" + this.caseJ_KJ.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " casec_RS", this.cpEntryListToArray(this.casec_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " casec_RS[" + this.casec_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseet_RS", this.cpEntryListToArray(this.caseet_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseet_RS[" + this.caseet_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " caseec_RU", this.cpEntryListToArray(this.caseec_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " caseec_RU[" + this.caseec_RU.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " cases_RU", this.cpEntryListToArray(this.cases_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " cases_RU[" + this.cases_RU.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " casearray_N", this.casearray_N.toArray(), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " casearray_N[" + this.casearray_N.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nesttype_RS", this.cpEntryListToArray(this.nesttype_RS), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nesttype_RS[" + this.nesttype_RS.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nestpair_N", this.nestpair_N.toArray(), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nestpair_N[" + this.nestpair_N.size() + "]");
            encodedBand = this.encodeBandInt(contextStr + "_" + this.type + " nestname_RU", this.cpEntryListToArray(this.nestname_RU), Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from " + contextStr + "_" + this.type + " nestname_RU[" + this.nestname_RU.size() + "]");
        }
    }

    private int[] tagListToArray(List t2) {
        int[] ints = new int[t2.size()];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = ((String)t2.get(i)).charAt(0);
        }
        return ints;
    }

    public void addParameterAnnotation(int numParams, int[] annoN, IntList pairN, List typeRS, List nameRU, List t, List values, List caseArrayN, List nestTypeRS, List nestNameRU, List nestPairN) {
        this.param_NB.add(numParams);
        for (int i = 0; i < annoN.length; ++i) {
            this.anno_N.add(annoN[i]);
        }
        this.pair_N.addAll(pairN);
        for (String desc : typeRS) {
            this.type_RS.add(this.cpBands.getCPSignature(desc));
        }
        for (String name : nameRU) {
            this.name_RU.add(this.cpBands.getCPUtf8(name));
        }
        Iterator valuesIterator = values.iterator();
        for (String tag : t) {
            Object value;
            this.T.add(tag);
            if (tag.equals("B") || tag.equals("C") || tag.equals("I") || tag.equals("S") || tag.equals("Z")) {
                value = (Integer)valuesIterator.next();
                this.caseI_KI.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("D")) {
                value = (Double)valuesIterator.next();
                this.caseD_KD.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("F")) {
                value = (Float)valuesIterator.next();
                this.caseF_KF.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("J")) {
                value = (Long)valuesIterator.next();
                this.caseJ_KJ.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("c")) {
                value = (String)valuesIterator.next();
                this.casec_RS.add(this.cpBands.getCPSignature((String)value));
                continue;
            }
            if (tag.equals("e")) {
                value = (String)valuesIterator.next();
                String value2 = (String)valuesIterator.next();
                this.caseet_RS.add(this.cpBands.getCPSignature((String)value));
                this.caseec_RU.add(this.cpBands.getCPUtf8(value2));
                continue;
            }
            if (!tag.equals("s")) continue;
            value = (String)valuesIterator.next();
            this.cases_RU.add(this.cpBands.getCPUtf8((String)value));
        }
        Iterator iterator = caseArrayN.iterator();
        while (iterator.hasNext()) {
            int arraySize = (Integer)iterator.next();
            this.casearray_N.add(arraySize);
            this.numBackwardsCalls += arraySize;
        }
        for (String type : nestTypeRS) {
            this.nesttype_RS.add(this.cpBands.getCPSignature(type));
        }
        for (String name : nestNameRU) {
            this.nestname_RU.add(this.cpBands.getCPUtf8(name));
        }
        for (Integer numPairs : nestPairN) {
            this.nestpair_N.add(numPairs);
            this.numBackwardsCalls += numPairs.intValue();
        }
    }

    public void addAnnotation(String desc, List nameRU, List t, List values, List caseArrayN, List nestTypeRS, List nestNameRU, List nestPairN) {
        this.type_RS.add(this.cpBands.getCPSignature(desc));
        this.pair_N.add(nameRU.size());
        for (String name : nameRU) {
            this.name_RU.add(this.cpBands.getCPUtf8(name));
        }
        Iterator valuesIterator = values.iterator();
        for (String tag : t) {
            Object value;
            this.T.add(tag);
            if (tag.equals("B") || tag.equals("C") || tag.equals("I") || tag.equals("S") || tag.equals("Z")) {
                value = (Integer)valuesIterator.next();
                this.caseI_KI.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("D")) {
                value = (Double)valuesIterator.next();
                this.caseD_KD.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("F")) {
                value = (Float)valuesIterator.next();
                this.caseF_KF.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("J")) {
                value = (Long)valuesIterator.next();
                this.caseJ_KJ.add(this.cpBands.getConstant(value));
                continue;
            }
            if (tag.equals("c")) {
                value = (String)valuesIterator.next();
                this.casec_RS.add(this.cpBands.getCPSignature((String)value));
                continue;
            }
            if (tag.equals("e")) {
                value = (String)valuesIterator.next();
                String value2 = (String)valuesIterator.next();
                this.caseet_RS.add(this.cpBands.getCPSignature((String)value));
                this.caseec_RU.add(this.cpBands.getCPUtf8(value2));
                continue;
            }
            if (!tag.equals("s")) continue;
            value = (String)valuesIterator.next();
            this.cases_RU.add(this.cpBands.getCPUtf8((String)value));
        }
        Iterator iterator = caseArrayN.iterator();
        while (iterator.hasNext()) {
            int arraySize = (Integer)iterator.next();
            this.casearray_N.add(arraySize);
            this.numBackwardsCalls += arraySize;
        }
        for (String type : nestTypeRS) {
            this.nesttype_RS.add(this.cpBands.getCPSignature(type));
        }
        for (String name : nestNameRU) {
            this.nestname_RU.add(this.cpBands.getCPUtf8(name));
        }
        for (Integer numPairs : nestPairN) {
            this.nestpair_N.add(numPairs);
            this.numBackwardsCalls += numPairs.intValue();
        }
    }

    public boolean hasContent() {
        return this.type_RS.size() > 0;
    }

    public int numBackwardsCalls() {
        return this.numBackwardsCalls;
    }

    public void incrementAnnoN() {
        this.anno_N.increment(this.anno_N.size() - 1);
    }

    public void newEntryInAnnoN() {
        this.anno_N.add(1);
    }

    public void removeLatest() {
        int latest = this.anno_N.remove(this.anno_N.size() - 1);
        for (int i = 0; i < latest; ++i) {
            this.type_RS.remove(this.type_RS.size() - 1);
            int pairs = this.pair_N.remove(this.pair_N.size() - 1);
            for (int j = 0; j < pairs; ++j) {
                this.removeOnePair();
            }
        }
    }

    private void removeOnePair() {
        block3: {
            String tag;
            block10: {
                block9: {
                    block8: {
                        block7: {
                            block6: {
                                block5: {
                                    block4: {
                                        block2: {
                                            tag = (String)this.T.remove(this.T.size() - 1);
                                            if (!tag.equals("B") && !tag.equals("C") && !tag.equals("I") && !tag.equals("S") && !tag.equals("Z")) break block2;
                                            this.caseI_KI.remove(this.caseI_KI.size() - 1);
                                            break block3;
                                        }
                                        if (!tag.equals("D")) break block4;
                                        this.caseD_KD.remove(this.caseD_KD.size() - 1);
                                        break block3;
                                    }
                                    if (!tag.equals("F")) break block5;
                                    this.caseF_KF.remove(this.caseF_KF.size() - 1);
                                    break block3;
                                }
                                if (!tag.equals("J")) break block6;
                                this.caseJ_KJ.remove(this.caseJ_KJ.size() - 1);
                                break block3;
                            }
                            if (!tag.equals("C")) break block7;
                            this.casec_RS.remove(this.casec_RS.size() - 1);
                            break block3;
                        }
                        if (!tag.equals("e")) break block8;
                        this.caseet_RS.remove(this.caseet_RS.size() - 1);
                        this.caseec_RU.remove(this.caseet_RS.size() - 1);
                        break block3;
                    }
                    if (!tag.equals("s")) break block9;
                    this.cases_RU.remove(this.cases_RU.size() - 1);
                    break block3;
                }
                if (!tag.equals("[")) break block10;
                int arraySize = this.casearray_N.remove(this.casearray_N.size() - 1);
                this.numBackwardsCalls -= arraySize;
                for (int k = 0; k < arraySize; ++k) {
                    this.removeOnePair();
                }
                break block3;
            }
            if (!tag.equals("@")) break block3;
            this.nesttype_RS.remove(this.nesttype_RS.size() - 1);
            int numPairs = this.nestpair_N.remove(this.nestpair_N.size() - 1);
            this.numBackwardsCalls -= numPairs;
            for (int i = 0; i < numPairs; ++i) {
                this.removeOnePair();
            }
        }
    }
}

