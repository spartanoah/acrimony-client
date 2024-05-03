/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPClass;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;

public class IcBands
extends BandSet {
    private final Set innerClasses = new TreeSet();
    private final CpBands cpBands;
    private int bit16Count = 0;
    private final Map outerToInner = new HashMap();

    public IcBands(SegmentHeader segmentHeader, CpBands cpBands, int effort) {
        super(effort, segmentHeader);
        this.cpBands = cpBands;
    }

    public void finaliseBands() {
        this.segmentHeader.setIc_count(this.innerClasses.size());
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing internal class bands...");
        int[] ic_this_class = new int[this.innerClasses.size()];
        int[] ic_flags = new int[this.innerClasses.size()];
        int[] ic_outer_class = new int[this.bit16Count];
        int[] ic_name = new int[this.bit16Count];
        int index2 = 0;
        ArrayList innerClassesList = new ArrayList(this.innerClasses);
        for (int i = 0; i < ic_this_class.length; ++i) {
            IcTuple icTuple = (IcTuple)innerClassesList.get(i);
            ic_this_class[i] = icTuple.C.getIndex();
            ic_flags[i] = icTuple.F;
            if ((icTuple.F & 0x10000) == 0) continue;
            ic_outer_class[index2] = icTuple.C2 == null ? 0 : icTuple.C2.getIndex() + 1;
            ic_name[index2] = icTuple.N == null ? 0 : icTuple.N.getIndex() + 1;
            ++index2;
        }
        byte[] encodedBand = this.encodeBandInt("ic_this_class", ic_this_class, Codec.UDELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_this_class[" + ic_this_class.length + "]");
        encodedBand = this.encodeBandInt("ic_flags", ic_flags, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_flags[" + ic_flags.length + "]");
        encodedBand = this.encodeBandInt("ic_outer_class", ic_outer_class, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_outer_class[" + ic_outer_class.length + "]");
        encodedBand = this.encodeBandInt("ic_name", ic_name, Codec.DELTA5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from ic_name[" + ic_name.length + "]");
    }

    public void addInnerClass(String name, String outerName, String innerName, int flags) {
        if (outerName != null || innerName != null) {
            if (this.namesArePredictable(name, outerName, innerName)) {
                IcTuple innerClass = new IcTuple(this.cpBands.getCPClass(name), flags, null, null);
                this.addToMap(outerName, innerClass);
                this.innerClasses.add(innerClass);
            } else {
                IcTuple icTuple = new IcTuple(this.cpBands.getCPClass(name), flags |= 0x10000, this.cpBands.getCPClass(outerName), this.cpBands.getCPUtf8(innerName));
                boolean added = this.innerClasses.add(icTuple);
                if (added) {
                    ++this.bit16Count;
                    this.addToMap(outerName, icTuple);
                }
            }
        } else {
            IcTuple innerClass = new IcTuple(this.cpBands.getCPClass(name), flags, null, null);
            this.addToMap(this.getOuter(name), innerClass);
            this.innerClasses.add(innerClass);
        }
    }

    public List getInnerClassesForOuter(String outerClassName) {
        return (List)this.outerToInner.get(outerClassName);
    }

    private String getOuter(String name) {
        return name.substring(0, name.lastIndexOf(36));
    }

    private void addToMap(String outerName, IcTuple icTuple) {
        ArrayList<IcTuple> tuples = (ArrayList<IcTuple>)this.outerToInner.get(outerName);
        if (tuples == null) {
            tuples = new ArrayList<IcTuple>();
            this.outerToInner.put(outerName, tuples);
            tuples.add(icTuple);
        } else {
            for (IcTuple icT : tuples) {
                if (!icTuple.equals(icT)) continue;
                return;
            }
            tuples.add(icTuple);
        }
    }

    private boolean namesArePredictable(String name, String outerName, String innerName) {
        return name.equals(outerName + '$' + innerName) && innerName.indexOf(36) == -1;
    }

    public IcTuple getIcTuple(CPClass inner) {
        for (IcTuple icTuple : this.innerClasses) {
            if (!icTuple.C.equals(inner)) continue;
            return icTuple;
        }
        return null;
    }

    class IcTuple
    implements Comparable {
        protected CPClass C;
        protected int F;
        protected CPClass C2;
        protected CPUTF8 N;

        public IcTuple(CPClass C, int F, CPClass C2, CPUTF8 N) {
            this.C = C;
            this.F = F;
            this.C2 = C2;
            this.N = N;
        }

        public boolean equals(Object o) {
            if (o instanceof IcTuple) {
                IcTuple icT = (IcTuple)o;
                return this.C.equals(icT.C) && this.F == icT.F && (this.C2 != null ? this.C2.equals(icT.C2) : icT.C2 == null) && (this.N != null ? this.N.equals(icT.N) : icT.N == null);
            }
            return false;
        }

        public String toString() {
            return this.C.toString();
        }

        public int compareTo(Object arg0) {
            return this.C.compareTo(((IcTuple)arg0).C);
        }

        public boolean isAnonymous() {
            String className = this.C.toString();
            String innerName = className.substring(className.lastIndexOf(36) + 1);
            return Character.isDigit(innerName.charAt(0));
        }
    }
}

