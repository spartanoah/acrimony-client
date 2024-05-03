/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.InternCache;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public final class CharsToNameCanonicalizer {
    public static final int HASH_MULT = 33;
    private static final int DEFAULT_T_SIZE = 64;
    private static final int MAX_T_SIZE = 65536;
    static final int MAX_ENTRIES_FOR_REUSE = 12000;
    static final int MAX_COLL_CHAIN_LENGTH = 100;
    private final CharsToNameCanonicalizer _parent;
    private final AtomicReference<TableInfo> _tableInfo;
    private final int _seed;
    private final int _flags;
    private boolean _canonicalize;
    private String[] _symbols;
    private Bucket[] _buckets;
    private int _size;
    private int _sizeThreshold;
    private int _indexMask;
    private int _longestCollisionList;
    private boolean _hashShared;
    private BitSet _overflows;

    private CharsToNameCanonicalizer(int seed) {
        this._parent = null;
        this._seed = seed;
        this._canonicalize = true;
        this._flags = -1;
        this._hashShared = false;
        this._longestCollisionList = 0;
        this._tableInfo = new AtomicReference<TableInfo>(TableInfo.createInitial(64));
    }

    private CharsToNameCanonicalizer(CharsToNameCanonicalizer parent, int flags, int seed, TableInfo parentState) {
        this._parent = parent;
        this._seed = seed;
        this._tableInfo = null;
        this._flags = flags;
        this._canonicalize = JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(flags);
        this._symbols = parentState.symbols;
        this._buckets = parentState.buckets;
        this._size = parentState.size;
        this._longestCollisionList = parentState.longestCollisionList;
        int arrayLen = this._symbols.length;
        this._sizeThreshold = CharsToNameCanonicalizer._thresholdSize(arrayLen);
        this._indexMask = arrayLen - 1;
        this._hashShared = true;
    }

    private static int _thresholdSize(int hashAreaSize) {
        return hashAreaSize - (hashAreaSize >> 2);
    }

    public static CharsToNameCanonicalizer createRoot() {
        long now = System.currentTimeMillis();
        int seed = (int)now + (int)(now >>> 32) | 1;
        return CharsToNameCanonicalizer.createRoot(seed);
    }

    protected static CharsToNameCanonicalizer createRoot(int seed) {
        return new CharsToNameCanonicalizer(seed);
    }

    public CharsToNameCanonicalizer makeChild(int flags) {
        return new CharsToNameCanonicalizer(this, flags, this._seed, this._tableInfo.get());
    }

    public void release() {
        if (!this.maybeDirty()) {
            return;
        }
        if (this._parent != null && this._canonicalize) {
            this._parent.mergeChild(new TableInfo(this));
            this._hashShared = true;
        }
    }

    private void mergeChild(TableInfo childState) {
        int childCount = childState.size;
        TableInfo currState = this._tableInfo.get();
        if (childCount == currState.size) {
            return;
        }
        if (childCount > 12000) {
            childState = TableInfo.createInitial(64);
        }
        this._tableInfo.compareAndSet(currState, childState);
    }

    public int size() {
        if (this._tableInfo != null) {
            return this._tableInfo.get().size;
        }
        return this._size;
    }

    public int bucketCount() {
        return this._symbols.length;
    }

    public boolean maybeDirty() {
        return !this._hashShared;
    }

    public int hashSeed() {
        return this._seed;
    }

    public int collisionCount() {
        int count = 0;
        for (Bucket bucket : this._buckets) {
            if (bucket == null) continue;
            count += bucket.length;
        }
        return count;
    }

    public int maxCollisionLength() {
        return this._longestCollisionList;
    }

    public String findSymbol(char[] buffer, int start, int len, int h) {
        if (len < 1) {
            return "";
        }
        if (!this._canonicalize) {
            return new String(buffer, start, len);
        }
        int index = this._hashToIndex(h);
        String sym = this._symbols[index];
        if (sym != null) {
            Bucket b;
            if (sym.length() == len) {
                int i = 0;
                while (sym.charAt(i) == buffer[start + i]) {
                    if (++i != len) continue;
                    return sym;
                }
            }
            if ((b = this._buckets[index >> 1]) != null) {
                sym = b.has(buffer, start, len);
                if (sym != null) {
                    return sym;
                }
                sym = this._findSymbol2(buffer, start, len, b.next);
                if (sym != null) {
                    return sym;
                }
            }
        }
        return this._addSymbol(buffer, start, len, h, index);
    }

    private String _findSymbol2(char[] buffer, int start, int len, Bucket b) {
        while (b != null) {
            String sym = b.has(buffer, start, len);
            if (sym != null) {
                return sym;
            }
            b = b.next;
        }
        return null;
    }

    private String _addSymbol(char[] buffer, int start, int len, int h, int index) {
        if (this._hashShared) {
            this.copyArrays();
            this._hashShared = false;
        } else if (this._size >= this._sizeThreshold) {
            this.rehash();
            index = this._hashToIndex(this.calcHash(buffer, start, len));
        }
        String newSymbol = new String(buffer, start, len);
        if (JsonFactory.Feature.INTERN_FIELD_NAMES.enabledIn(this._flags)) {
            newSymbol = InternCache.instance.intern(newSymbol);
        }
        ++this._size;
        if (this._symbols[index] == null) {
            this._symbols[index] = newSymbol;
        } else {
            int bix = index >> 1;
            Bucket newB = new Bucket(newSymbol, this._buckets[bix]);
            int collLen = newB.length;
            if (collLen > 100) {
                this._handleSpillOverflow(bix, newB, index);
            } else {
                this._buckets[bix] = newB;
                this._longestCollisionList = Math.max(collLen, this._longestCollisionList);
            }
        }
        return newSymbol;
    }

    private void _handleSpillOverflow(int bucketIndex, Bucket newBucket, int mainIndex) {
        if (this._overflows == null) {
            this._overflows = new BitSet();
            this._overflows.set(bucketIndex);
        } else if (this._overflows.get(bucketIndex)) {
            if (JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(this._flags)) {
                this.reportTooManyCollisions(100);
            }
            this._canonicalize = false;
        } else {
            this._overflows.set(bucketIndex);
        }
        this._symbols[mainIndex] = newBucket.symbol;
        this._buckets[bucketIndex] = null;
        this._size -= newBucket.length;
        this._longestCollisionList = -1;
    }

    public int _hashToIndex(int rawHash) {
        rawHash += rawHash >>> 15;
        rawHash ^= rawHash << 7;
        rawHash += rawHash >>> 3;
        return rawHash & this._indexMask;
    }

    public int calcHash(char[] buffer, int start, int len) {
        int hash = this._seed;
        int end = start + len;
        for (int i = start; i < end; ++i) {
            hash = hash * 33 + buffer[i];
        }
        return hash == 0 ? 1 : hash;
    }

    public int calcHash(String key) {
        int len = key.length();
        int hash = this._seed;
        for (int i = 0; i < len; ++i) {
            hash = hash * 33 + key.charAt(i);
        }
        return hash == 0 ? 1 : hash;
    }

    private void copyArrays() {
        String[] oldSyms = this._symbols;
        this._symbols = Arrays.copyOf(oldSyms, oldSyms.length);
        Bucket[] oldBuckets = this._buckets;
        this._buckets = Arrays.copyOf(oldBuckets, oldBuckets.length);
    }

    private void rehash() {
        int size = this._symbols.length;
        int newSize = size + size;
        if (newSize > 65536) {
            this._size = 0;
            this._canonicalize = false;
            this._symbols = new String[64];
            this._buckets = new Bucket[32];
            this._indexMask = 63;
            this._hashShared = false;
            return;
        }
        String[] oldSyms = this._symbols;
        Bucket[] oldBuckets = this._buckets;
        this._symbols = new String[newSize];
        this._buckets = new Bucket[newSize >> 1];
        this._indexMask = newSize - 1;
        this._sizeThreshold = CharsToNameCanonicalizer._thresholdSize(newSize);
        int count = 0;
        int maxColl = 0;
        for (int i = 0; i < size; ++i) {
            Bucket newB;
            String symbol = oldSyms[i];
            if (symbol == null) continue;
            ++count;
            int index = this._hashToIndex(this.calcHash(symbol));
            if (this._symbols[index] == null) {
                this._symbols[index] = symbol;
                continue;
            }
            int bix = index >> 1;
            this._buckets[bix] = newB = new Bucket(symbol, this._buckets[bix]);
            maxColl = Math.max(maxColl, newB.length);
        }
        int bucketSize = size >> 1;
        for (int i = 0; i < bucketSize; ++i) {
            Bucket b = oldBuckets[i];
            while (b != null) {
                ++count;
                String symbol = b.symbol;
                int index = this._hashToIndex(this.calcHash(symbol));
                if (this._symbols[index] == null) {
                    this._symbols[index] = symbol;
                } else {
                    Bucket newB;
                    int bix = index >> 1;
                    this._buckets[bix] = newB = new Bucket(symbol, this._buckets[bix]);
                    maxColl = Math.max(maxColl, newB.length);
                }
                b = b.next;
            }
        }
        this._longestCollisionList = maxColl;
        this._overflows = null;
        if (count != this._size) {
            throw new IllegalStateException(String.format("Internal error on SymbolTable.rehash(): had %d entries; now have %d", this._size, count));
        }
    }

    protected void reportTooManyCollisions(int maxLen) {
        throw new IllegalStateException("Longest collision chain in symbol table (of size " + this._size + ") now exceeds maximum, " + maxLen + " -- suspect a DoS attack based on hash collisions");
    }

    protected void verifyInternalConsistency() {
        int count = 0;
        int size = this._symbols.length;
        for (int i = 0; i < size; ++i) {
            String symbol = this._symbols[i];
            if (symbol == null) continue;
            ++count;
        }
        int bucketSize = size >> 1;
        for (int i = 0; i < bucketSize; ++i) {
            Bucket b = this._buckets[i];
            while (b != null) {
                ++count;
                b = b.next;
            }
        }
        if (count != this._size) {
            throw new IllegalStateException(String.format("Internal error: expected internal size %d vs calculated count %d", this._size, count));
        }
    }

    private static final class TableInfo {
        final int size;
        final int longestCollisionList;
        final String[] symbols;
        final Bucket[] buckets;

        public TableInfo(int size, int longestCollisionList, String[] symbols, Bucket[] buckets) {
            this.size = size;
            this.longestCollisionList = longestCollisionList;
            this.symbols = symbols;
            this.buckets = buckets;
        }

        public TableInfo(CharsToNameCanonicalizer src) {
            this.size = src._size;
            this.longestCollisionList = src._longestCollisionList;
            this.symbols = src._symbols;
            this.buckets = src._buckets;
        }

        public static TableInfo createInitial(int sz) {
            return new TableInfo(0, 0, new String[sz], new Bucket[sz >> 1]);
        }
    }

    static final class Bucket {
        public final String symbol;
        public final Bucket next;
        public final int length;

        public Bucket(String s, Bucket n) {
            this.symbol = s;
            this.next = n;
            this.length = n == null ? 1 : n.length + 1;
        }

        public String has(char[] buf, int start, int len) {
            if (this.symbol.length() != len) {
                return null;
            }
            int i = 0;
            do {
                if (this.symbol.charAt(i) == buf[start + i]) continue;
                return null;
            } while (++i < len);
            return this.symbol;
        }
    }
}

