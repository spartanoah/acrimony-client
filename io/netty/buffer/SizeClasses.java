/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.PoolThreadCache;
import io.netty.buffer.SizeClassesMetric;

abstract class SizeClasses
implements SizeClassesMetric {
    static final int LOG2_QUANTUM = 4;
    private static final int LOG2_SIZE_CLASS_GROUP = 2;
    private static final int LOG2_MAX_LOOKUP_SIZE = 12;
    private static final int INDEX_IDX = 0;
    private static final int LOG2GROUP_IDX = 1;
    private static final int LOG2DELTA_IDX = 2;
    private static final int NDELTA_IDX = 3;
    private static final int PAGESIZE_IDX = 4;
    private static final int SUBPAGE_IDX = 5;
    private static final int LOG2_DELTA_LOOKUP_IDX = 6;
    private static final byte no = 0;
    private static final byte yes = 1;
    protected final int pageSize;
    protected final int pageShifts;
    protected final int chunkSize;
    protected final int directMemoryCacheAlignment;
    final int nSizes;
    int nSubpages;
    int nPSizes;
    int smallMaxSizeIdx;
    private int lookupMaxSize;
    private final short[][] sizeClasses;
    private final int[] pageIdx2sizeTab;
    private final int[] sizeIdx2sizeTab;
    private final int[] size2idxTab;

    protected SizeClasses(int pageSize, int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
        this.pageSize = pageSize;
        this.pageShifts = pageShifts;
        this.chunkSize = chunkSize;
        this.directMemoryCacheAlignment = directMemoryCacheAlignment;
        int group = PoolThreadCache.log2(chunkSize) + 1 - 4;
        this.sizeClasses = new short[group << 2][7];
        this.nSizes = this.sizeClasses();
        this.sizeIdx2sizeTab = new int[this.nSizes];
        this.pageIdx2sizeTab = new int[this.nPSizes];
        this.idx2SizeTab(this.sizeIdx2sizeTab, this.pageIdx2sizeTab);
        this.size2idxTab = new int[this.lookupMaxSize >> 4];
        this.size2idxTab(this.size2idxTab);
    }

    private int sizeClasses() {
        int normalMaxSize = -1;
        int index = 0;
        int size = 0;
        int log2Group = 4;
        int log2Delta = 4;
        int ndeltaLimit = 4;
        int nDelta = 0;
        while (nDelta < ndeltaLimit) {
            size = this.sizeClass(index++, log2Group, log2Delta, nDelta++);
        }
        log2Group += 2;
        while (size < this.chunkSize) {
            nDelta = 1;
            while (nDelta <= ndeltaLimit && size < this.chunkSize) {
                normalMaxSize = size = this.sizeClass(index++, log2Group, log2Delta, nDelta++);
            }
            ++log2Group;
            ++log2Delta;
        }
        assert (this.chunkSize == normalMaxSize);
        return index;
    }

    private int sizeClass(int index, int log2Group, int log2Delta, int nDelta) {
        int log2Size;
        int pageSize;
        int size;
        short isMultiPageSize = log2Delta >= this.pageShifts ? (short)1 : ((size = (1 << log2Group) + (1 << log2Delta) * nDelta) == size / (pageSize = 1 << this.pageShifts) * pageSize ? (short)1 : 0);
        int log2Ndelta = nDelta == 0 ? 0 : PoolThreadCache.log2(nDelta);
        boolean remove = 1 << log2Ndelta < nDelta;
        int n = log2Size = log2Delta + log2Ndelta == log2Group ? log2Group + 1 : log2Group;
        if (log2Size == log2Group) {
            remove = true;
        }
        short isSubpage = log2Size < this.pageShifts + 2 ? (short)1 : 0;
        int log2DeltaLookup = log2Size < 12 || log2Size == 12 && !remove ? log2Delta : 0;
        short[] sz = new short[]{(short)index, (short)log2Group, (short)log2Delta, (short)nDelta, isMultiPageSize, isSubpage, (short)log2DeltaLookup};
        this.sizeClasses[index] = sz;
        int size2 = (1 << log2Group) + (nDelta << log2Delta);
        if (sz[4] == 1) {
            ++this.nPSizes;
        }
        if (sz[5] == 1) {
            ++this.nSubpages;
            this.smallMaxSizeIdx = index;
        }
        if (sz[6] != 0) {
            this.lookupMaxSize = size2;
        }
        return size2;
    }

    private void idx2SizeTab(int[] sizeIdx2sizeTab, int[] pageIdx2sizeTab) {
        int pageIdx = 0;
        for (int i = 0; i < this.nSizes; ++i) {
            int size;
            short[] sizeClass = this.sizeClasses[i];
            short log2Group = sizeClass[1];
            short log2Delta = sizeClass[2];
            short nDelta = sizeClass[3];
            sizeIdx2sizeTab[i] = size = (1 << log2Group) + (nDelta << log2Delta);
            if (sizeClass[4] != 1) continue;
            pageIdx2sizeTab[pageIdx++] = size;
        }
    }

    private void size2idxTab(int[] size2idxTab) {
        int idx = 0;
        int size = 0;
        int i = 0;
        while (size <= this.lookupMaxSize) {
            short log2Delta = this.sizeClasses[i][2];
            int times = 1 << log2Delta - 4;
            while (size <= this.lookupMaxSize && times-- > 0) {
                size2idxTab[idx++] = i;
                size = idx + 1 << 4;
            }
            ++i;
        }
    }

    @Override
    public int sizeIdx2size(int sizeIdx) {
        return this.sizeIdx2sizeTab[sizeIdx];
    }

    @Override
    public int sizeIdx2sizeCompute(int sizeIdx) {
        int group = sizeIdx >> 2;
        int mod = sizeIdx & 3;
        int groupSize = group == 0 ? 0 : 32 << group;
        int shift = group == 0 ? 1 : group;
        int lgDelta = shift + 4 - 1;
        int modSize = mod + 1 << lgDelta;
        return groupSize + modSize;
    }

    @Override
    public long pageIdx2size(int pageIdx) {
        return this.pageIdx2sizeTab[pageIdx];
    }

    @Override
    public long pageIdx2sizeCompute(int pageIdx) {
        int group = pageIdx >> 2;
        int mod = pageIdx & 3;
        long groupSize = group == 0 ? 0L : 1L << this.pageShifts + 2 - 1 << group;
        int shift = group == 0 ? 1 : group;
        int log2Delta = shift + this.pageShifts - 1;
        int modSize = mod + 1 << log2Delta;
        return groupSize + (long)modSize;
    }

    @Override
    public int size2SizeIdx(int size) {
        if (size == 0) {
            return 0;
        }
        if (size > this.chunkSize) {
            return this.nSizes;
        }
        if (this.directMemoryCacheAlignment > 0) {
            size = this.alignSize(size);
        }
        if (size <= this.lookupMaxSize) {
            return this.size2idxTab[size - 1 >> 4];
        }
        int x = PoolThreadCache.log2((size << 1) - 1);
        int shift = x < 7 ? 0 : x - 6;
        int group = shift << 2;
        int log2Delta = x < 7 ? 4 : x - 2 - 1;
        int deltaInverseMask = -1 << log2Delta;
        int mod = (size - 1 & deltaInverseMask) >> log2Delta & 3;
        return group + mod;
    }

    @Override
    public int pages2pageIdx(int pages) {
        return this.pages2pageIdxCompute(pages, false);
    }

    @Override
    public int pages2pageIdxFloor(int pages) {
        return this.pages2pageIdxCompute(pages, true);
    }

    private int pages2pageIdxCompute(int pages, boolean floor) {
        int pageSize = pages << this.pageShifts;
        if (pageSize > this.chunkSize) {
            return this.nPSizes;
        }
        int x = PoolThreadCache.log2((pageSize << 1) - 1);
        int shift = x < 2 + this.pageShifts ? 0 : x - (2 + this.pageShifts);
        int group = shift << 2;
        int log2Delta = x < 2 + this.pageShifts + 1 ? this.pageShifts : x - 2 - 1;
        int deltaInverseMask = -1 << log2Delta;
        int mod = (pageSize - 1 & deltaInverseMask) >> log2Delta & 3;
        int pageIdx = group + mod;
        if (floor && this.pageIdx2sizeTab[pageIdx] > pages << this.pageShifts) {
            --pageIdx;
        }
        return pageIdx;
    }

    private int alignSize(int size) {
        int delta = size & this.directMemoryCacheAlignment - 1;
        return delta == 0 ? size : size + this.directMemoryCacheAlignment - delta;
    }

    @Override
    public int normalizeSize(int size) {
        if (size == 0) {
            return this.sizeIdx2sizeTab[0];
        }
        if (this.directMemoryCacheAlignment > 0) {
            size = this.alignSize(size);
        }
        if (size <= this.lookupMaxSize) {
            int ret = this.sizeIdx2sizeTab[this.size2idxTab[size - 1 >> 4]];
            assert (ret == SizeClasses.normalizeSizeCompute(size));
            return ret;
        }
        return SizeClasses.normalizeSizeCompute(size);
    }

    private static int normalizeSizeCompute(int size) {
        int x = PoolThreadCache.log2((size << 1) - 1);
        int log2Delta = x < 7 ? 4 : x - 2 - 1;
        int delta = 1 << log2Delta;
        int delta_mask = delta - 1;
        return size + delta_mask & ~delta_mask;
    }
}

