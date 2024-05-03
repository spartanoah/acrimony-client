/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.InternCache;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class ByteQuadsCanonicalizer {
    private static final int DEFAULT_T_SIZE = 64;
    private static final int MAX_T_SIZE = 65536;
    private static final int MIN_HASH_SIZE = 16;
    static final int MAX_ENTRIES_FOR_REUSE = 6000;
    private final ByteQuadsCanonicalizer _parent;
    private final AtomicReference<TableInfo> _tableInfo;
    private final int _seed;
    private boolean _intern;
    private final boolean _failOnDoS;
    private int[] _hashArea;
    private int _hashSize;
    private int _secondaryStart;
    private int _tertiaryStart;
    private int _tertiaryShift;
    private int _count;
    private String[] _names;
    private int _spilloverEnd;
    private int _longNameOffset;
    private boolean _hashShared;
    private static final int MULT = 33;
    private static final int MULT2 = 65599;
    private static final int MULT3 = 31;

    private ByteQuadsCanonicalizer(int sz, boolean intern, int seed, boolean failOnDoS) {
        this._parent = null;
        this._seed = seed;
        this._intern = intern;
        this._failOnDoS = failOnDoS;
        if (sz < 16) {
            sz = 16;
        } else if ((sz & sz - 1) != 0) {
            int curr;
            for (curr = 16; curr < sz; curr += curr) {
            }
            sz = curr;
        }
        this._tableInfo = new AtomicReference<TableInfo>(TableInfo.createInitial(sz));
    }

    private ByteQuadsCanonicalizer(ByteQuadsCanonicalizer parent, boolean intern, int seed, boolean failOnDoS, TableInfo state) {
        this._parent = parent;
        this._seed = seed;
        this._intern = intern;
        this._failOnDoS = failOnDoS;
        this._tableInfo = null;
        this._count = state.count;
        this._hashSize = state.size;
        this._secondaryStart = this._hashSize << 2;
        this._tertiaryStart = this._secondaryStart + (this._secondaryStart >> 1);
        this._tertiaryShift = state.tertiaryShift;
        this._hashArea = state.mainHash;
        this._names = state.names;
        this._spilloverEnd = state.spilloverEnd;
        this._longNameOffset = state.longNameOffset;
        this._hashShared = true;
    }

    public static ByteQuadsCanonicalizer createRoot() {
        long now = System.currentTimeMillis();
        int seed = (int)now + (int)(now >>> 32) | 1;
        return ByteQuadsCanonicalizer.createRoot(seed);
    }

    protected static ByteQuadsCanonicalizer createRoot(int seed) {
        return new ByteQuadsCanonicalizer(64, true, seed, true);
    }

    public ByteQuadsCanonicalizer makeChild(int flags) {
        return new ByteQuadsCanonicalizer(this, JsonFactory.Feature.INTERN_FIELD_NAMES.enabledIn(flags), this._seed, JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(flags), this._tableInfo.get());
    }

    public void release() {
        if (this._parent != null && this.maybeDirty()) {
            this._parent.mergeChild(new TableInfo(this));
            this._hashShared = true;
        }
    }

    private void mergeChild(TableInfo childState) {
        int childCount = childState.count;
        TableInfo currState = this._tableInfo.get();
        if (childCount == currState.count) {
            return;
        }
        if (childCount > 6000) {
            childState = TableInfo.createInitial(64);
        }
        this._tableInfo.compareAndSet(currState, childState);
    }

    public int size() {
        if (this._tableInfo != null) {
            return this._tableInfo.get().count;
        }
        return this._count;
    }

    public int bucketCount() {
        return this._hashSize;
    }

    public boolean maybeDirty() {
        return !this._hashShared;
    }

    public int hashSeed() {
        return this._seed;
    }

    public int primaryCount() {
        int count = 0;
        int end = this._secondaryStart;
        for (int offset = 3; offset < end; offset += 4) {
            if (this._hashArea[offset] == 0) continue;
            ++count;
        }
        return count;
    }

    public int secondaryCount() {
        int count = 0;
        int end = this._tertiaryStart;
        for (int offset = this._secondaryStart + 3; offset < end; offset += 4) {
            if (this._hashArea[offset] == 0) continue;
            ++count;
        }
        return count;
    }

    public int tertiaryCount() {
        int offset;
        int count = 0;
        int end = offset + this._hashSize;
        for (offset = this._tertiaryStart + 3; offset < end; offset += 4) {
            if (this._hashArea[offset] == 0) continue;
            ++count;
        }
        return count;
    }

    public int spilloverCount() {
        return this._spilloverEnd - this._spilloverStart() >> 2;
    }

    public int totalCount() {
        int count = 0;
        int end = this._hashSize << 3;
        for (int offset = 3; offset < end; offset += 4) {
            if (this._hashArea[offset] == 0) continue;
            ++count;
        }
        return count;
    }

    public String toString() {
        int pri = this.primaryCount();
        int sec = this.secondaryCount();
        int tert = this.tertiaryCount();
        int spill = this.spilloverCount();
        int total = this.totalCount();
        return String.format("[%s: size=%d, hashSize=%d, %d/%d/%d/%d pri/sec/ter/spill (=%s), total:%d]", this.getClass().getName(), this._count, this._hashSize, pri, sec, tert, spill, pri + sec + tert + spill, total);
    }

    public String findName(int q1) {
        int offset2;
        int[] hashArea = this._hashArea;
        int offset = this._calcOffset(this.calcHash(q1));
        int len = hashArea[offset + 3];
        if (len == 1) {
            if (hashArea[offset] == q1) {
                return this._names[offset >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        if ((len = hashArea[(offset2 = this._secondaryStart + (offset >> 3 << 2)) + 3]) == 1) {
            if (hashArea[offset2] == q1) {
                return this._names[offset2 >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        return this._findSecondary(offset, q1);
    }

    public String findName(int q1, int q2) {
        int offset2;
        int[] hashArea = this._hashArea;
        int offset = this._calcOffset(this.calcHash(q1, q2));
        int len = hashArea[offset + 3];
        if (len == 2) {
            if (q1 == hashArea[offset] && q2 == hashArea[offset + 1]) {
                return this._names[offset >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        if ((len = hashArea[(offset2 = this._secondaryStart + (offset >> 3 << 2)) + 3]) == 2) {
            if (q1 == hashArea[offset2] && q2 == hashArea[offset2 + 1]) {
                return this._names[offset2 >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        return this._findSecondary(offset, q1, q2);
    }

    public String findName(int q1, int q2, int q3) {
        int offset2;
        int[] hashArea = this._hashArea;
        int offset = this._calcOffset(this.calcHash(q1, q2, q3));
        int len = hashArea[offset + 3];
        if (len == 3) {
            if (q1 == hashArea[offset] && hashArea[offset + 1] == q2 && hashArea[offset + 2] == q3) {
                return this._names[offset >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        if ((len = hashArea[(offset2 = this._secondaryStart + (offset >> 3 << 2)) + 3]) == 3) {
            if (q1 == hashArea[offset2] && hashArea[offset2 + 1] == q2 && hashArea[offset2 + 2] == q3) {
                return this._names[offset2 >> 2];
            }
        } else if (len == 0) {
            return null;
        }
        return this._findSecondary(offset, q1, q2, q3);
    }

    public String findName(int[] q, int qlen) {
        if (qlen < 4) {
            switch (qlen) {
                case 3: {
                    return this.findName(q[0], q[1], q[2]);
                }
                case 2: {
                    return this.findName(q[0], q[1]);
                }
                case 1: {
                    return this.findName(q[0]);
                }
            }
            return "";
        }
        int hash = this.calcHash(q, qlen);
        int offset = this._calcOffset(hash);
        int[] hashArea = this._hashArea;
        int len = hashArea[offset + 3];
        if (hash == hashArea[offset] && len == qlen && this._verifyLongName(q, qlen, hashArea[offset + 1])) {
            return this._names[offset >> 2];
        }
        if (len == 0) {
            return null;
        }
        int offset2 = this._secondaryStart + (offset >> 3 << 2);
        int len2 = hashArea[offset2 + 3];
        if (hash == hashArea[offset2] && len2 == qlen && this._verifyLongName(q, qlen, hashArea[offset2 + 1])) {
            return this._names[offset2 >> 2];
        }
        return this._findSecondary(offset, hash, q, qlen);
    }

    private final int _calcOffset(int hash) {
        int ix = hash & this._hashSize - 1;
        return ix << 2;
    }

    private String _findSecondary(int origOffset, int q1) {
        int offset;
        int[] hashArea = this._hashArea;
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset + bucketSize;
        for (offset = this._tertiaryStart + (origOffset >> this._tertiaryShift + 2 << this._tertiaryShift); offset < end; offset += 4) {
            int len = hashArea[offset + 3];
            if (q1 == hashArea[offset] && 1 == len) {
                return this._names[offset >> 2];
            }
            if (len != 0) continue;
            return null;
        }
        for (offset = this._spilloverStart(); offset < this._spilloverEnd; offset += 4) {
            if (q1 != hashArea[offset] || 1 != hashArea[offset + 3]) continue;
            return this._names[offset >> 2];
        }
        return null;
    }

    private String _findSecondary(int origOffset, int q1, int q2) {
        int offset;
        int[] hashArea = this._hashArea;
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset + bucketSize;
        for (offset = this._tertiaryStart + (origOffset >> this._tertiaryShift + 2 << this._tertiaryShift); offset < end; offset += 4) {
            int len = hashArea[offset + 3];
            if (q1 == hashArea[offset] && q2 == hashArea[offset + 1] && 2 == len) {
                return this._names[offset >> 2];
            }
            if (len != 0) continue;
            return null;
        }
        for (offset = this._spilloverStart(); offset < this._spilloverEnd; offset += 4) {
            if (q1 != hashArea[offset] || q2 != hashArea[offset + 1] || 2 != hashArea[offset + 3]) continue;
            return this._names[offset >> 2];
        }
        return null;
    }

    private String _findSecondary(int origOffset, int q1, int q2, int q3) {
        int offset;
        int[] hashArea = this._hashArea;
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset + bucketSize;
        for (offset = this._tertiaryStart + (origOffset >> this._tertiaryShift + 2 << this._tertiaryShift); offset < end; offset += 4) {
            int len = hashArea[offset + 3];
            if (q1 == hashArea[offset] && q2 == hashArea[offset + 1] && q3 == hashArea[offset + 2] && 3 == len) {
                return this._names[offset >> 2];
            }
            if (len != 0) continue;
            return null;
        }
        for (offset = this._spilloverStart(); offset < this._spilloverEnd; offset += 4) {
            if (q1 != hashArea[offset] || q2 != hashArea[offset + 1] || q3 != hashArea[offset + 2] || 3 != hashArea[offset + 3]) continue;
            return this._names[offset >> 2];
        }
        return null;
    }

    private String _findSecondary(int origOffset, int hash, int[] q, int qlen) {
        int offset;
        int[] hashArea = this._hashArea;
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset + bucketSize;
        for (offset = this._tertiaryStart + (origOffset >> this._tertiaryShift + 2 << this._tertiaryShift); offset < end; offset += 4) {
            int len = hashArea[offset + 3];
            if (hash == hashArea[offset] && qlen == len && this._verifyLongName(q, qlen, hashArea[offset + 1])) {
                return this._names[offset >> 2];
            }
            if (len != 0) continue;
            return null;
        }
        for (offset = this._spilloverStart(); offset < this._spilloverEnd; offset += 4) {
            if (hash != hashArea[offset] || qlen != hashArea[offset + 3] || !this._verifyLongName(q, qlen, hashArea[offset + 1])) continue;
            return this._names[offset >> 2];
        }
        return null;
    }

    private boolean _verifyLongName(int[] q, int qlen, int spillOffset) {
        int[] hashArea = this._hashArea;
        int ix = 0;
        switch (qlen) {
            default: {
                return this._verifyLongName2(q, qlen, spillOffset);
            }
            case 8: {
                if (q[ix++] != hashArea[spillOffset++]) {
                    return false;
                }
            }
            case 7: {
                if (q[ix++] != hashArea[spillOffset++]) {
                    return false;
                }
            }
            case 6: {
                if (q[ix++] != hashArea[spillOffset++]) {
                    return false;
                }
            }
            case 5: {
                if (q[ix++] == hashArea[spillOffset++]) break;
                return false;
            }
            case 4: 
        }
        if (q[ix++] != hashArea[spillOffset++]) {
            return false;
        }
        if (q[ix++] != hashArea[spillOffset++]) {
            return false;
        }
        if (q[ix++] != hashArea[spillOffset++]) {
            return false;
        }
        return q[ix++] == hashArea[spillOffset++];
    }

    private boolean _verifyLongName2(int[] q, int qlen, int spillOffset) {
        int ix = 0;
        do {
            if (q[ix++] == this._hashArea[spillOffset++]) continue;
            return false;
        } while (ix < qlen);
        return true;
    }

    public String addName(String name, int q1) {
        this._verifySharing();
        if (this._intern) {
            name = InternCache.instance.intern(name);
        }
        int offset = this._findOffsetForAdd(this.calcHash(q1));
        this._hashArea[offset] = q1;
        this._hashArea[offset + 3] = 1;
        this._names[offset >> 2] = name;
        ++this._count;
        return name;
    }

    public String addName(String name, int q1, int q2) {
        this._verifySharing();
        if (this._intern) {
            name = InternCache.instance.intern(name);
        }
        int hash = q2 == 0 ? this.calcHash(q1) : this.calcHash(q1, q2);
        int offset = this._findOffsetForAdd(hash);
        this._hashArea[offset] = q1;
        this._hashArea[offset + 1] = q2;
        this._hashArea[offset + 3] = 2;
        this._names[offset >> 2] = name;
        ++this._count;
        return name;
    }

    public String addName(String name, int q1, int q2, int q3) {
        this._verifySharing();
        if (this._intern) {
            name = InternCache.instance.intern(name);
        }
        int offset = this._findOffsetForAdd(this.calcHash(q1, q2, q3));
        this._hashArea[offset] = q1;
        this._hashArea[offset + 1] = q2;
        this._hashArea[offset + 2] = q3;
        this._hashArea[offset + 3] = 3;
        this._names[offset >> 2] = name;
        ++this._count;
        return name;
    }

    public String addName(String name, int[] q, int qlen) {
        this._verifySharing();
        if (this._intern) {
            name = InternCache.instance.intern(name);
        }
        switch (qlen) {
            case 1: {
                int offset = this._findOffsetForAdd(this.calcHash(q[0]));
                this._hashArea[offset] = q[0];
                this._hashArea[offset + 3] = 1;
                break;
            }
            case 2: {
                int offset = this._findOffsetForAdd(this.calcHash(q[0], q[1]));
                this._hashArea[offset] = q[0];
                this._hashArea[offset + 1] = q[1];
                this._hashArea[offset + 3] = 2;
                break;
            }
            case 3: {
                int offset = this._findOffsetForAdd(this.calcHash(q[0], q[1], q[2]));
                this._hashArea[offset] = q[0];
                this._hashArea[offset + 1] = q[1];
                this._hashArea[offset + 2] = q[2];
                this._hashArea[offset + 3] = 3;
                break;
            }
            default: {
                int longStart;
                int hash = this.calcHash(q, qlen);
                int offset = this._findOffsetForAdd(hash);
                this._hashArea[offset] = hash;
                this._hashArea[offset + 1] = longStart = this._appendLongName(q, qlen);
                this._hashArea[offset + 3] = qlen;
            }
        }
        this._names[offset >> 2] = name;
        ++this._count;
        return name;
    }

    private void _verifySharing() {
        if (this._hashShared) {
            this._hashArea = Arrays.copyOf(this._hashArea, this._hashArea.length);
            this._names = Arrays.copyOf(this._names, this._names.length);
            this._hashShared = false;
        }
    }

    private int _findOffsetForAdd(int hash) {
        int[] hashArea = this._hashArea;
        int offset = this._calcOffset(hash);
        if (hashArea[offset + 3] == 0) {
            return offset;
        }
        if (this._checkNeedForRehash()) {
            return this._resizeAndFindOffsetForAdd(hash);
        }
        int offset2 = this._secondaryStart + (offset >> 3 << 2);
        if (hashArea[offset2 + 3] == 0) {
            return offset2;
        }
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset2 + bucketSize;
        for (offset2 = this._tertiaryStart + (offset >> this._tertiaryShift + 2 << this._tertiaryShift); offset2 < end; offset2 += 4) {
            if (hashArea[offset2 + 3] != 0) continue;
            return offset2;
        }
        offset = this._spilloverEnd;
        this._spilloverEnd += 4;
        end = this._hashSize << 3;
        if (this._spilloverEnd >= end) {
            if (this._failOnDoS) {
                this._reportTooManyCollisions();
            }
            return this._resizeAndFindOffsetForAdd(hash);
        }
        return offset;
    }

    private int _resizeAndFindOffsetForAdd(int hash) {
        this.rehash();
        int offset = this._calcOffset(hash);
        int[] hashArea = this._hashArea;
        if (hashArea[offset + 3] == 0) {
            return offset;
        }
        int offset2 = this._secondaryStart + (offset >> 3 << 2);
        if (hashArea[offset2 + 3] == 0) {
            return offset2;
        }
        int bucketSize = 1 << this._tertiaryShift;
        int end = offset2 + bucketSize;
        for (offset2 = this._tertiaryStart + (offset >> this._tertiaryShift + 2 << this._tertiaryShift); offset2 < end; offset2 += 4) {
            if (hashArea[offset2 + 3] != 0) continue;
            return offset2;
        }
        offset = this._spilloverEnd;
        this._spilloverEnd += 4;
        return offset;
    }

    private boolean _checkNeedForRehash() {
        int spillCount;
        return this._count > this._hashSize >> 1 && ((spillCount = this._spilloverEnd - this._spilloverStart() >> 2) > 1 + this._count >> 7 || (double)this._count > (double)this._hashSize * 0.8);
    }

    private int _appendLongName(int[] quads, int qlen) {
        int start = this._longNameOffset;
        if (start + qlen > this._hashArea.length) {
            int toAdd = start + qlen - this._hashArea.length;
            int minAdd = Math.min(4096, this._hashSize);
            int newSize = this._hashArea.length + Math.max(toAdd, minAdd);
            this._hashArea = Arrays.copyOf(this._hashArea, newSize);
        }
        System.arraycopy(quads, 0, this._hashArea, start, qlen);
        this._longNameOffset += qlen;
        return start;
    }

    public int calcHash(int q1) {
        int hash = q1 ^ this._seed;
        hash += hash >>> 16;
        hash ^= hash << 3;
        hash += hash >>> 12;
        return hash;
    }

    public int calcHash(int q1, int q2) {
        int hash = q1;
        hash += hash >>> 15;
        hash ^= hash >>> 9;
        hash += q2 * 33;
        hash ^= this._seed;
        hash += hash >>> 16;
        hash ^= hash >>> 4;
        hash += hash << 3;
        return hash;
    }

    public int calcHash(int q1, int q2, int q3) {
        int hash = q1 ^ this._seed;
        hash += hash >>> 9;
        hash *= 31;
        hash += q2;
        hash *= 33;
        hash += hash >>> 15;
        hash ^= q3;
        hash += hash >>> 4;
        hash += hash >>> 15;
        hash ^= hash << 9;
        return hash;
    }

    public int calcHash(int[] q, int qlen) {
        if (qlen < 4) {
            throw new IllegalArgumentException();
        }
        int hash = q[0] ^ this._seed;
        hash += hash >>> 9;
        hash += q[1];
        hash += hash >>> 15;
        hash *= 33;
        hash ^= q[2];
        hash += hash >>> 4;
        for (int i = 3; i < qlen; ++i) {
            int next = q[i];
            next ^= next >> 21;
            hash += next;
        }
        hash *= 65599;
        hash += hash >>> 19;
        hash ^= hash << 5;
        return hash;
    }

    private void rehash() {
        this._hashShared = false;
        int[] oldHashArea = this._hashArea;
        String[] oldNames = this._names;
        int oldSize = this._hashSize;
        int oldCount = this._count;
        int newSize = oldSize + oldSize;
        int oldEnd = this._spilloverEnd;
        if (newSize > 65536) {
            this.nukeSymbols(true);
            return;
        }
        this._hashArea = new int[oldHashArea.length + (oldSize << 3)];
        this._hashSize = newSize;
        this._secondaryStart = newSize << 2;
        this._tertiaryStart = this._secondaryStart + (this._secondaryStart >> 1);
        this._tertiaryShift = ByteQuadsCanonicalizer._calcTertiaryShift(newSize);
        this._names = new String[oldNames.length << 1];
        this.nukeSymbols(false);
        int copyCount = 0;
        int[] q = new int[16];
        int end = oldEnd;
        block5: for (int offset = 0; offset < end; offset += 4) {
            int len = oldHashArea[offset + 3];
            if (len == 0) continue;
            ++copyCount;
            String name = oldNames[offset >> 2];
            switch (len) {
                case 1: {
                    q[0] = oldHashArea[offset];
                    this.addName(name, q, 1);
                    continue block5;
                }
                case 2: {
                    q[0] = oldHashArea[offset];
                    q[1] = oldHashArea[offset + 1];
                    this.addName(name, q, 2);
                    continue block5;
                }
                case 3: {
                    q[0] = oldHashArea[offset];
                    q[1] = oldHashArea[offset + 1];
                    q[2] = oldHashArea[offset + 2];
                    this.addName(name, q, 3);
                    continue block5;
                }
                default: {
                    if (len > q.length) {
                        q = new int[len];
                    }
                    int qoff = oldHashArea[offset + 1];
                    System.arraycopy(oldHashArea, qoff, q, 0, len);
                    this.addName(name, q, len);
                }
            }
        }
        if (copyCount != oldCount) {
            throw new IllegalStateException("Failed rehash(): old count=" + oldCount + ", copyCount=" + copyCount);
        }
    }

    private void nukeSymbols(boolean fill) {
        this._count = 0;
        this._spilloverEnd = this._spilloverStart();
        this._longNameOffset = this._hashSize << 3;
        if (fill) {
            Arrays.fill(this._hashArea, 0);
            Arrays.fill(this._names, null);
        }
    }

    private final int _spilloverStart() {
        int offset = this._hashSize;
        return (offset << 3) - offset;
    }

    protected void _reportTooManyCollisions() {
        if (this._hashSize <= 1024) {
            return;
        }
        throw new IllegalStateException("Spill-over slots in symbol table with " + this._count + " entries, hash area of " + this._hashSize + " slots is now full (all " + (this._hashSize >> 3) + " slots -- suspect a DoS attack based on hash collisions. You can disable the check via `JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW`");
    }

    static int _calcTertiaryShift(int primarySlots) {
        int tertSlots = primarySlots >> 2;
        if (tertSlots < 64) {
            return 4;
        }
        if (tertSlots <= 256) {
            return 5;
        }
        if (tertSlots <= 1024) {
            return 6;
        }
        return 7;
    }

    private static final class TableInfo {
        public final int size;
        public final int count;
        public final int tertiaryShift;
        public final int[] mainHash;
        public final String[] names;
        public final int spilloverEnd;
        public final int longNameOffset;

        public TableInfo(int size, int count, int tertiaryShift, int[] mainHash, String[] names, int spilloverEnd, int longNameOffset) {
            this.size = size;
            this.count = count;
            this.tertiaryShift = tertiaryShift;
            this.mainHash = mainHash;
            this.names = names;
            this.spilloverEnd = spilloverEnd;
            this.longNameOffset = longNameOffset;
        }

        public TableInfo(ByteQuadsCanonicalizer src) {
            this.size = src._hashSize;
            this.count = src._count;
            this.tertiaryShift = src._tertiaryShift;
            this.mainHash = src._hashArea;
            this.names = src._names;
            this.spilloverEnd = src._spilloverEnd;
            this.longNameOffset = src._longNameOffset;
        }

        public static TableInfo createInitial(int sz) {
            int hashAreaSize = sz << 3;
            int tertShift = ByteQuadsCanonicalizer._calcTertiaryShift(sz);
            return new TableInfo(sz, 0, tertShift, new int[hashAreaSize], new String[sz << 1], hashAreaSize - sz, hashAreaSize);
        }
    }
}

