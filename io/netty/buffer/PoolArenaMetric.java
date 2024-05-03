/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.PoolChunkListMetric;
import io.netty.buffer.PoolSubpageMetric;
import io.netty.buffer.SizeClassesMetric;
import java.util.List;

public interface PoolArenaMetric
extends SizeClassesMetric {
    public int numThreadCaches();

    @Deprecated
    public int numTinySubpages();

    public int numSmallSubpages();

    public int numChunkLists();

    @Deprecated
    public List<PoolSubpageMetric> tinySubpages();

    public List<PoolSubpageMetric> smallSubpages();

    public List<PoolChunkListMetric> chunkLists();

    public long numAllocations();

    @Deprecated
    public long numTinyAllocations();

    public long numSmallAllocations();

    public long numNormalAllocations();

    public long numHugeAllocations();

    public long numDeallocations();

    @Deprecated
    public long numTinyDeallocations();

    public long numSmallDeallocations();

    public long numNormalDeallocations();

    public long numHugeDeallocations();

    public long numActiveAllocations();

    @Deprecated
    public long numActiveTinyAllocations();

    public long numActiveSmallAllocations();

    public long numActiveNormalAllocations();

    public long numActiveHugeAllocations();

    public long numActiveBytes();
}

