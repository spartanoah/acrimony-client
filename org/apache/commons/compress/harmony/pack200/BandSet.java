/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.CanonicalCodecFamilies;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CodecEncoding;
import org.apache.commons.compress.harmony.pack200.ConstantPoolEntry;
import org.apache.commons.compress.harmony.pack200.IntList;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PopulationCodec;
import org.apache.commons.compress.harmony.pack200.RunCodec;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;

public abstract class BandSet {
    protected final SegmentHeader segmentHeader;
    final int effort;
    private static final int[] effortThresholds = new int[]{0, 0, 1000, 500, 100, 100, 100, 100, 100, 0};
    private long[] canonicalLargest;
    private long[] canonicalSmallest;

    public BandSet(int effort, SegmentHeader header) {
        this.effort = effort;
        this.segmentHeader = header;
    }

    public abstract void pack(OutputStream var1) throws IOException, Pack200Exception;

    public byte[] encodeScalar(int[] band, BHSDCodec codec) throws Pack200Exception {
        return codec.encode(band);
    }

    public byte[] encodeScalar(int value, BHSDCodec codec) throws Pack200Exception {
        return codec.encode(value);
    }

    public byte[] encodeBandInt(String name, int[] ints, BHSDCodec defaultCodec) throws Pack200Exception {
        byte[] encodedBand = null;
        if (this.effort > 1 && ints.length >= effortThresholds[this.effort]) {
            BandAnalysisResults results = this.analyseBand(name, ints, defaultCodec);
            Codec betterCodec = results.betterCodec;
            encodedBand = results.encodedBand;
            if (betterCodec != null) {
                if (betterCodec instanceof BHSDCodec) {
                    int[] specifierBand = CodecEncoding.getSpecifier(betterCodec, defaultCodec);
                    int specifier = specifierBand[0];
                    if (specifierBand.length > 1) {
                        for (int i = 1; i < specifierBand.length; ++i) {
                            this.segmentHeader.appendBandCodingSpecifier(specifierBand[i]);
                        }
                    }
                    specifier = defaultCodec.isSigned() ? -1 - specifier : (specifier += defaultCodec.getL());
                    byte[] specifierEncoded = defaultCodec.encode(new int[]{specifier});
                    byte[] band = new byte[specifierEncoded.length + encodedBand.length];
                    System.arraycopy(specifierEncoded, 0, band, 0, specifierEncoded.length);
                    System.arraycopy(encodedBand, 0, band, specifierEncoded.length, encodedBand.length);
                    return band;
                }
                if (betterCodec instanceof PopulationCodec) {
                    int[] extraSpecifierInfo = results.extraMetadata;
                    for (int i = 0; i < extraSpecifierInfo.length; ++i) {
                        this.segmentHeader.appendBandCodingSpecifier(extraSpecifierInfo[i]);
                    }
                    return encodedBand;
                }
                if (betterCodec instanceof RunCodec) {
                    // empty if block
                }
            }
        }
        if (ints.length > 0) {
            if (encodedBand == null) {
                encodedBand = defaultCodec.encode(ints);
            }
            int first = ints[0];
            if (defaultCodec.getB() != 1) {
                if (defaultCodec.isSigned() && first >= -256 && first <= -1) {
                    int specifier = -1 - CodecEncoding.getSpecifierForDefaultCodec(defaultCodec);
                    byte[] specifierEncoded = defaultCodec.encode(new int[]{specifier});
                    byte[] band = new byte[specifierEncoded.length + encodedBand.length];
                    System.arraycopy(specifierEncoded, 0, band, 0, specifierEncoded.length);
                    System.arraycopy(encodedBand, 0, band, specifierEncoded.length, encodedBand.length);
                    return band;
                }
                if (!defaultCodec.isSigned() && first >= defaultCodec.getL() && first <= defaultCodec.getL() + 255) {
                    int specifier = CodecEncoding.getSpecifierForDefaultCodec(defaultCodec) + defaultCodec.getL();
                    byte[] specifierEncoded = defaultCodec.encode(new int[]{specifier});
                    byte[] band = new byte[specifierEncoded.length + encodedBand.length];
                    System.arraycopy(specifierEncoded, 0, band, 0, specifierEncoded.length);
                    System.arraycopy(encodedBand, 0, band, specifierEncoded.length, encodedBand.length);
                    return band;
                }
            }
            return encodedBand;
        }
        return new byte[0];
    }

    private BandAnalysisResults analyseBand(String name, int[] band, BHSDCodec defaultCodec) throws Pack200Exception {
        BandAnalysisResults results = new BandAnalysisResults();
        if (this.canonicalLargest == null) {
            this.canonicalLargest = new long[116];
            this.canonicalSmallest = new long[116];
            for (int i = 1; i < this.canonicalLargest.length; ++i) {
                this.canonicalLargest[i] = CodecEncoding.getCanonicalCodec(i).largest();
                this.canonicalSmallest[i] = CodecEncoding.getCanonicalCodec(i).smallest();
            }
        }
        BandData bandData = new BandData(band);
        byte[] encoded = defaultCodec.encode(band);
        BandAnalysisResults.access$102(results, encoded);
        if (encoded.length <= band.length + 23 - 2 * this.effort) {
            return results;
        }
        if (!bandData.anyNegatives() && (long)bandData.largest <= Codec.BYTE1.largest()) {
            BandAnalysisResults.access$102(results, Codec.BYTE1.encode(band));
            results.betterCodec = Codec.BYTE1;
            return results;
        }
        if (this.effort > 3 && !name.equals("POPULATION")) {
            int numDistinctValues = bandData.numDistinctValues();
            float distinctValuesAsProportion = (float)numDistinctValues / (float)band.length;
            if (numDistinctValues < 100 || (double)distinctValuesAsProportion < 0.02 || this.effort > 6 && (double)distinctValuesAsProportion < 0.04) {
                this.encodeWithPopulationCodec(name, band, defaultCodec, bandData, results);
                if (this.timeToStop(results)) {
                    return results;
                }
            }
        }
        ArrayList<BHSDCodec[]> codecFamiliesToTry = new ArrayList<BHSDCodec[]>();
        if (bandData.mainlyPositiveDeltas() && bandData.mainlySmallDeltas()) {
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs2);
        }
        if (bandData.wellCorrelated()) {
            if (bandData.mainlyPositiveDeltas()) {
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs1);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs3);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs4);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs5);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs1);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs3);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs4);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs5);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs2);
            } else {
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs1);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs3);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs2);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs4);
                codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs5);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaSignedCodecs1);
                codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaSignedCodecs2);
            }
        } else if (bandData.anyNegatives()) {
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaSignedCodecs1);
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaSignedCodecs2);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs1);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs2);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs3);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs4);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaSignedCodecs5);
        } else {
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs1);
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs3);
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs4);
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs5);
            codecFamiliesToTry.add(CanonicalCodecFamilies.nonDeltaUnsignedCodecs2);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs1);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs3);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs4);
            codecFamiliesToTry.add(CanonicalCodecFamilies.deltaUnsignedCodecs5);
        }
        if (name.equalsIgnoreCase("cpint")) {
            System.out.print("");
        }
        for (BHSDCodec[] family : codecFamiliesToTry) {
            this.tryCodecs(name, band, defaultCodec, bandData, results, encoded, family);
            if (!this.timeToStop(results)) continue;
            break;
        }
        return results;
    }

    private boolean timeToStop(BandAnalysisResults results) {
        if (this.effort > 6) {
            return results.numCodecsTried >= this.effort * 2;
        }
        return results.numCodecsTried >= this.effort;
    }

    private void tryCodecs(String name, int[] band, BHSDCodec defaultCodec, BandData bandData, BandAnalysisResults results, byte[] encoded, BHSDCodec[] potentialCodecs) throws Pack200Exception {
        for (int i = 0; i < potentialCodecs.length; ++i) {
            int saved;
            byte[] specifierEncoded;
            byte[] encoded2;
            BHSDCodec potential = potentialCodecs[i];
            if (potential.equals(defaultCodec)) {
                return;
            }
            if (potential.isDelta()) {
                if (potential.largest() >= (long)bandData.largestDelta && potential.smallest() <= (long)bandData.smallestDelta && potential.largest() >= (long)bandData.largest && potential.smallest() <= (long)bandData.smallest) {
                    encoded2 = potential.encode(band);
                    results.numCodecsTried++;
                    specifierEncoded = defaultCodec.encode(CodecEncoding.getSpecifier(potential, null));
                    saved = encoded.length - encoded2.length - specifierEncoded.length;
                    if (saved > results.saved) {
                        results.betterCodec = potential;
                        BandAnalysisResults.access$102(results, encoded2);
                        results.saved = saved;
                    }
                }
            } else if (potential.largest() >= (long)bandData.largest && potential.smallest() <= (long)bandData.smallest) {
                encoded2 = potential.encode(band);
                results.numCodecsTried++;
                specifierEncoded = defaultCodec.encode(CodecEncoding.getSpecifier(potential, null));
                saved = encoded.length - encoded2.length - specifierEncoded.length;
                if (saved > results.saved) {
                    results.betterCodec = potential;
                    BandAnalysisResults.access$102(results, encoded2);
                    results.saved = saved;
                }
            }
            if (!this.timeToStop(results)) continue;
            return;
        }
    }

    private void encodeWithPopulationCodec(String name, int[] band, BHSDCodec defaultCodec, BandData bandData, BandAnalysisResults results) throws Pack200Exception {
        int i;
        int[] specifiers;
        byte[] tokensEncoded;
        results.numCodecsTried = results.numCodecsTried + 3;
        Map distinctValues = bandData.distinctValues;
        ArrayList<Integer> favoured = new ArrayList<Integer>();
        for (Integer value : distinctValues.keySet()) {
            Integer count = (Integer)distinctValues.get(value);
            if (count <= 2 && distinctValues.size() >= 256) continue;
            favoured.add(value);
        }
        if (distinctValues.size() > 255) {
            Collections.sort(favoured, (arg0, arg1) -> ((Integer)distinctValues.get(arg1)).compareTo((Integer)distinctValues.get(arg0)));
        }
        IntList unfavoured = new IntList();
        HashMap<Integer, Integer> favouredToIndex = new HashMap<Integer, Integer>();
        for (int i2 = 0; i2 < favoured.size(); ++i2) {
            Integer value = (Integer)favoured.get(i2);
            favouredToIndex.put(value, i2);
        }
        int[] tokens = new int[band.length];
        for (int i3 = 0; i3 < band.length; ++i3) {
            Integer favouredIndex = (Integer)favouredToIndex.get(band[i3]);
            if (favouredIndex == null) {
                tokens[i3] = 0;
                unfavoured.add(band[i3]);
                continue;
            }
            tokens[i3] = favouredIndex + 1;
        }
        favoured.add((Integer)favoured.get(favoured.size() - 1));
        int[] favouredBand = this.integerListToArray(favoured);
        int[] unfavouredBand = unfavoured.toArray();
        BandAnalysisResults favouredResults = this.analyseBand("POPULATION", favouredBand, defaultCodec);
        BandAnalysisResults unfavouredResults = this.analyseBand("POPULATION", unfavouredBand, defaultCodec);
        int tdefL = 0;
        int l = 0;
        Codec tokenCodec = null;
        int k = favoured.size() - 1;
        if (k < 256) {
            tdefL = 1;
            tokensEncoded = Codec.BYTE1.encode(tokens);
        } else {
            boolean d;
            BandAnalysisResults tokenResults = this.analyseBand("POPULATION", tokens, defaultCodec);
            tokenCodec = tokenResults.betterCodec;
            tokensEncoded = tokenResults.encodedBand;
            if (tokenCodec == null) {
                tokenCodec = defaultCodec;
            }
            l = ((BHSDCodec)tokenCodec).getL();
            int h = ((BHSDCodec)tokenCodec).getH();
            int s = ((BHSDCodec)tokenCodec).getS();
            int b = ((BHSDCodec)tokenCodec).getB();
            boolean bl = d = ((BHSDCodec)tokenCodec).isDelta();
            if (s == 0 && !d) {
                BHSDCodec oneLowerB;
                boolean canUseTDefL = true;
                if (b > 1 && (oneLowerB = new BHSDCodec(b - 1, h)).largest() >= (long)k) {
                    canUseTDefL = false;
                }
                if (canUseTDefL) {
                    switch (l) {
                        case 4: {
                            tdefL = 1;
                            break;
                        }
                        case 8: {
                            tdefL = 2;
                            break;
                        }
                        case 16: {
                            tdefL = 3;
                            break;
                        }
                        case 32: {
                            tdefL = 4;
                            break;
                        }
                        case 64: {
                            tdefL = 5;
                            break;
                        }
                        case 128: {
                            tdefL = 6;
                            break;
                        }
                        case 192: {
                            tdefL = 7;
                            break;
                        }
                        case 224: {
                            tdefL = 8;
                            break;
                        }
                        case 240: {
                            tdefL = 9;
                            break;
                        }
                        case 248: {
                            tdefL = 10;
                            break;
                        }
                        case 252: {
                            tdefL = 11;
                        }
                    }
                }
            }
        }
        byte[] favouredEncoded = favouredResults.encodedBand;
        byte[] unfavouredEncoded = unfavouredResults.encodedBand;
        Codec favouredCodec = favouredResults.betterCodec;
        Codec unfavouredCodec = unfavouredResults.betterCodec;
        int specifier = 141 + (favouredCodec == null ? 1 : 0) + 4 * tdefL + (unfavouredCodec == null ? 2 : 0);
        IntList extraBandMetadata = new IntList(3);
        if (favouredCodec != null) {
            specifiers = CodecEncoding.getSpecifier(favouredCodec, null);
            for (i = 0; i < specifiers.length; ++i) {
                extraBandMetadata.add(specifiers[i]);
            }
        }
        if (tdefL == 0) {
            specifiers = CodecEncoding.getSpecifier(tokenCodec, null);
            for (i = 0; i < specifiers.length; ++i) {
                extraBandMetadata.add(specifiers[i]);
            }
        }
        if (unfavouredCodec != null) {
            specifiers = CodecEncoding.getSpecifier(unfavouredCodec, null);
            for (i = 0; i < specifiers.length; ++i) {
                extraBandMetadata.add(specifiers[i]);
            }
        }
        int[] extraMetadata = extraBandMetadata.toArray();
        byte[] extraMetadataEncoded = Codec.UNSIGNED5.encode(extraMetadata);
        specifier = defaultCodec.isSigned() ? -1 - specifier : (specifier += defaultCodec.getL());
        byte[] firstValueEncoded = defaultCodec.encode(new int[]{specifier});
        int totalBandLength = firstValueEncoded.length + favouredEncoded.length + tokensEncoded.length + unfavouredEncoded.length;
        if (totalBandLength + extraMetadataEncoded.length < results.encodedBand.length) {
            results.saved = results.saved + (results.encodedBand.length - (totalBandLength + extraMetadataEncoded.length));
            byte[] encodedBand = new byte[totalBandLength];
            System.arraycopy(firstValueEncoded, 0, encodedBand, 0, firstValueEncoded.length);
            System.arraycopy(favouredEncoded, 0, encodedBand, firstValueEncoded.length, favouredEncoded.length);
            System.arraycopy(tokensEncoded, 0, encodedBand, firstValueEncoded.length + favouredEncoded.length, tokensEncoded.length);
            System.arraycopy(unfavouredEncoded, 0, encodedBand, firstValueEncoded.length + favouredEncoded.length + tokensEncoded.length, unfavouredEncoded.length);
            BandAnalysisResults.access$102(results, encodedBand);
            BandAnalysisResults.access$202(results, extraMetadata);
            if (l != 0) {
                results.betterCodec = new PopulationCodec(favouredCodec, l, unfavouredCodec);
            } else {
                results.betterCodec = new PopulationCodec(favouredCodec, tokenCodec, unfavouredCodec);
            }
        }
    }

    protected byte[] encodeFlags(String name, long[] flags, BHSDCodec loCodec, BHSDCodec hiCodec, boolean haveHiFlags) throws Pack200Exception {
        if (!haveHiFlags) {
            int[] loBits = new int[flags.length];
            for (int i = 0; i < flags.length; ++i) {
                loBits[i] = (int)flags[i];
            }
            return this.encodeBandInt(name, loBits, loCodec);
        }
        int[] hiBits = new int[flags.length];
        int[] loBits = new int[flags.length];
        for (int i = 0; i < flags.length; ++i) {
            long l = flags[i];
            hiBits[i] = (int)(l >> 32);
            loBits[i] = (int)l;
        }
        byte[] hi = this.encodeBandInt(name, hiBits, hiCodec);
        byte[] lo = this.encodeBandInt(name, loBits, loCodec);
        byte[] total = new byte[hi.length + lo.length];
        System.arraycopy(hi, 0, total, 0, hi.length);
        System.arraycopy(lo, 0, total, hi.length + 1, lo.length);
        return total;
    }

    protected int[] integerListToArray(List integerList) {
        int[] array = new int[integerList.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = (Integer)integerList.get(i);
        }
        return array;
    }

    protected long[] longListToArray(List longList) {
        long[] array = new long[longList.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = (Long)longList.get(i);
        }
        return array;
    }

    protected int[] cpEntryListToArray(List list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = ((ConstantPoolEntry)list.get(i)).getIndex();
            if (array[i] >= 0) continue;
            throw new RuntimeException("Index should be > 0");
        }
        return array;
    }

    protected int[] cpEntryOrNullListToArray(List theList) {
        int[] array = new int[theList.size()];
        for (int j = 0; j < array.length; ++j) {
            ConstantPoolEntry cpEntry = (ConstantPoolEntry)theList.get(j);
            int n = array[j] = cpEntry == null ? 0 : cpEntry.getIndex() + 1;
            if (cpEntry == null || cpEntry.getIndex() >= 0) continue;
            throw new RuntimeException("Index should be > 0");
        }
        return array;
    }

    protected byte[] encodeFlags(String name, long[][] flags, BHSDCodec loCodec, BHSDCodec hiCodec, boolean haveHiFlags) throws Pack200Exception {
        return this.encodeFlags(name, this.flatten(flags), loCodec, hiCodec, haveHiFlags);
    }

    private long[] flatten(long[][] flags) {
        int totalSize = 0;
        for (int i = 0; i < flags.length; ++i) {
            totalSize += flags[i].length;
        }
        long[] flatArray = new long[totalSize];
        int index = 0;
        for (int i = 0; i < flags.length; ++i) {
            for (int j = 0; j < flags[i].length; ++j) {
                flatArray[index] = flags[i][j];
                ++index;
            }
        }
        return flatArray;
    }

    public class BandAnalysisResults {
        private int numCodecsTried = 0;
        private int saved = 0;
        private int[] extraMetadata;
        private byte[] encodedBand;
        private Codec betterCodec;

        static /* synthetic */ byte[] access$102(BandAnalysisResults x0, byte[] x1) {
            x0.encodedBand = x1;
            return x1;
        }

        static /* synthetic */ int[] access$202(BandAnalysisResults x0, int[] x1) {
            x0.extraMetadata = x1;
            return x1;
        }
    }

    public class BandData {
        private final int[] band;
        private int smallest = Integer.MAX_VALUE;
        private int largest = Integer.MIN_VALUE;
        private int smallestDelta;
        private int largestDelta;
        private int deltaIsAscending = 0;
        private int smallDeltaCount = 0;
        private double averageAbsoluteDelta = 0.0;
        private double averageAbsoluteValue = 0.0;
        private Map distinctValues;

        public BandData(int[] band) {
            this.band = band;
            Integer one = 1;
            for (int i = 0; i < band.length; ++i) {
                Integer value;
                Integer count;
                if (band[i] < this.smallest) {
                    this.smallest = band[i];
                }
                if (band[i] > this.largest) {
                    this.largest = band[i];
                }
                if (i != 0) {
                    int delta = band[i] - band[i - 1];
                    if (delta < this.smallestDelta) {
                        this.smallestDelta = delta;
                    }
                    if (delta > this.largestDelta) {
                        this.largestDelta = delta;
                    }
                    if (delta >= 0) {
                        ++this.deltaIsAscending;
                    }
                    this.averageAbsoluteDelta += (double)Math.abs(delta) / (double)(band.length - 1);
                    if (Math.abs(delta) < 256) {
                        ++this.smallDeltaCount;
                    }
                } else {
                    this.smallestDelta = band[0];
                    this.largestDelta = band[0];
                }
                this.averageAbsoluteValue += (double)Math.abs(band[i]) / (double)band.length;
                if (BandSet.this.effort <= 3) continue;
                if (this.distinctValues == null) {
                    this.distinctValues = new HashMap();
                }
                count = (count = (Integer)this.distinctValues.get(value = Integer.valueOf(band[i]))) == null ? one : Integer.valueOf(count + 1);
                this.distinctValues.put(value, count);
            }
        }

        public boolean mainlySmallDeltas() {
            return (float)this.smallDeltaCount / (float)this.band.length > 0.7f;
        }

        public boolean wellCorrelated() {
            return this.averageAbsoluteDelta * 3.1 < this.averageAbsoluteValue;
        }

        public boolean mainlyPositiveDeltas() {
            return (float)this.deltaIsAscending / (float)this.band.length > 0.95f;
        }

        public boolean anyNegatives() {
            return this.smallest < 0;
        }

        public int numDistinctValues() {
            if (this.distinctValues == null) {
                return this.band.length;
            }
            return this.distinctValues.size();
        }
    }
}

