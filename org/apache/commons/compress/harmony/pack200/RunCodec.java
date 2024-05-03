/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PopulationCodec;

public class RunCodec
extends Codec {
    private int k;
    private final Codec aCodec;
    private final Codec bCodec;
    private int last;

    public RunCodec(int k, Codec aCodec, Codec bCodec) throws Pack200Exception {
        if (k <= 0) {
            throw new Pack200Exception("Cannot have a RunCodec for a negative number of numbers");
        }
        if (aCodec == null || bCodec == null) {
            throw new Pack200Exception("Must supply both codecs for a RunCodec");
        }
        this.k = k;
        this.aCodec = aCodec;
        this.bCodec = bCodec;
    }

    @Override
    public int decode(InputStream in) throws IOException, Pack200Exception {
        return this.decode(in, this.last);
    }

    @Override
    public int decode(InputStream in, long last) throws IOException, Pack200Exception {
        if (--this.k >= 0) {
            int value = this.aCodec.decode(in, this.last);
            this.last = this.k == 0 ? 0 : value;
            return this.normalise(value, this.aCodec);
        }
        this.last = this.bCodec.decode(in, this.last);
        return this.normalise(this.last, this.bCodec);
    }

    private int normalise(int value, Codec codecUsed) {
        BHSDCodec bhsd;
        if (codecUsed instanceof BHSDCodec && (bhsd = (BHSDCodec)codecUsed).isDelta()) {
            long cardinality = bhsd.cardinality();
            while ((long)value > bhsd.largest()) {
                value = (int)((long)value - cardinality);
            }
            while ((long)value < bhsd.smallest()) {
                value = (int)((long)value + cardinality);
            }
        }
        return value;
    }

    @Override
    public int[] decodeInts(int n, InputStream in) throws IOException, Pack200Exception {
        int[] band = new int[n];
        int[] aValues = this.aCodec.decodeInts(this.k, in);
        this.normalise(aValues, this.aCodec);
        int[] bValues = this.bCodec.decodeInts(n - this.k, in);
        this.normalise(bValues, this.bCodec);
        System.arraycopy(aValues, 0, band, 0, this.k);
        System.arraycopy(bValues, 0, band, this.k, n - this.k);
        this.lastBandLength = this.aCodec.lastBandLength + this.bCodec.lastBandLength;
        return band;
    }

    private void normalise(int[] band, Codec codecUsed) {
        block8: {
            block7: {
                if (!(codecUsed instanceof BHSDCodec)) break block7;
                BHSDCodec bhsd = (BHSDCodec)codecUsed;
                if (!bhsd.isDelta()) break block8;
                long cardinality = bhsd.cardinality();
                for (int i = 0; i < band.length; ++i) {
                    while ((long)band[i] > bhsd.largest()) {
                        int n = i;
                        band[n] = (int)((long)band[n] - cardinality);
                    }
                    while ((long)band[i] < bhsd.smallest()) {
                        int n = i;
                        band[n] = (int)((long)band[n] + cardinality);
                    }
                }
                break block8;
            }
            if (codecUsed instanceof PopulationCodec) {
                PopulationCodec popCodec = (PopulationCodec)codecUsed;
                int[] favoured = (int[])popCodec.getFavoured().clone();
                Arrays.sort(favoured);
                for (int i = 0; i < band.length; ++i) {
                    BHSDCodec bhsd;
                    Codec theCodec;
                    boolean favouredValue = Arrays.binarySearch(favoured, band[i]) > -1;
                    Codec codec = theCodec = favouredValue ? popCodec.getFavouredCodec() : popCodec.getUnfavouredCodec();
                    if (!(theCodec instanceof BHSDCodec) || !(bhsd = (BHSDCodec)theCodec).isDelta()) continue;
                    long cardinality = bhsd.cardinality();
                    while ((long)band[i] > bhsd.largest()) {
                        int n = i;
                        band[n] = (int)((long)band[n] - cardinality);
                    }
                    while ((long)band[i] < bhsd.smallest()) {
                        int n = i;
                        band[n] = (int)((long)band[n] + cardinality);
                    }
                }
            }
        }
    }

    public String toString() {
        return "RunCodec[k=" + this.k + ";aCodec=" + this.aCodec + "bCodec=" + this.bCodec + "]";
    }

    @Override
    public byte[] encode(int value, int last) throws Pack200Exception {
        throw new Pack200Exception("Must encode entire band at once with a RunCodec");
    }

    @Override
    public byte[] encode(int value) throws Pack200Exception {
        throw new Pack200Exception("Must encode entire band at once with a RunCodec");
    }

    public int getK() {
        return this.k;
    }

    public Codec getACodec() {
        return this.aCodec;
    }

    public Codec getBCodec() {
        return this.bCodec;
    }
}

