/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.sevenz;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.commons.compress.archivers.sevenz.BindPair;
import org.apache.commons.compress.archivers.sevenz.Coder;

class Folder {
    Coder[] coders;
    long totalInputStreams;
    long totalOutputStreams;
    BindPair[] bindPairs;
    long[] packedStreams;
    long[] unpackSizes;
    boolean hasCrc;
    long crc;
    int numUnpackSubStreams;
    static final Folder[] EMPTY_FOLDER_ARRAY = new Folder[0];

    Folder() {
    }

    Iterable<Coder> getOrderedCoders() throws IOException {
        if (this.packedStreams == null || this.coders == null || this.packedStreams.length == 0 || this.coders.length == 0) {
            return Collections.emptyList();
        }
        LinkedList<Coder> l = new LinkedList<Coder>();
        int current = (int)this.packedStreams[0];
        while (current >= 0 && current < this.coders.length) {
            if (l.contains(this.coders[current])) {
                throw new IOException("folder uses the same coder more than once in coder chain");
            }
            l.addLast(this.coders[current]);
            int pair = this.findBindPairForOutStream(current);
            current = pair != -1 ? (int)this.bindPairs[pair].inIndex : -1;
        }
        return l;
    }

    int findBindPairForInStream(int index) {
        if (this.bindPairs != null) {
            for (int i = 0; i < this.bindPairs.length; ++i) {
                if (this.bindPairs[i].inIndex != (long)index) continue;
                return i;
            }
        }
        return -1;
    }

    int findBindPairForOutStream(int index) {
        if (this.bindPairs != null) {
            for (int i = 0; i < this.bindPairs.length; ++i) {
                if (this.bindPairs[i].outIndex != (long)index) continue;
                return i;
            }
        }
        return -1;
    }

    long getUnpackSize() {
        if (this.totalOutputStreams == 0L) {
            return 0L;
        }
        for (int i = (int)this.totalOutputStreams - 1; i >= 0; --i) {
            if (this.findBindPairForOutStream(i) >= 0) continue;
            return this.unpackSizes[i];
        }
        return 0L;
    }

    long getUnpackSizeForCoder(Coder coder) {
        if (this.coders != null) {
            for (int i = 0; i < this.coders.length; ++i) {
                if (this.coders[i] != coder) continue;
                return this.unpackSizes[i];
            }
        }
        return 0L;
    }

    public String toString() {
        return "Folder with " + this.coders.length + " coders, " + this.totalInputStreams + " input streams, " + this.totalOutputStreams + " output streams, " + this.bindPairs.length + " bind pairs, " + this.packedStreams.length + " packed streams, " + this.unpackSizes.length + " unpack sizes, " + (this.hasCrc ? "with CRC " + this.crc : "without CRC") + " and " + this.numUnpackSubStreams + " unpack streams";
    }
}

