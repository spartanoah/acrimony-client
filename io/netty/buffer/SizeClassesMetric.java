/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

public interface SizeClassesMetric {
    public int sizeIdx2size(int var1);

    public int sizeIdx2sizeCompute(int var1);

    public long pageIdx2size(int var1);

    public long pageIdx2sizeCompute(int var1);

    public int size2SizeIdx(int var1);

    public int pages2pageIdx(int var1);

    public int pages2pageIdxFloor(int var1);

    public int normalizeSize(int var1);
}

