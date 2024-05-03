/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer.search;

import io.netty.buffer.search.AbstractSearchProcessorFactory;
import io.netty.buffer.search.SearchProcessor;
import io.netty.util.internal.PlatformDependent;

public class KmpSearchProcessorFactory
extends AbstractSearchProcessorFactory {
    private final int[] jumpTable;
    private final byte[] needle;

    KmpSearchProcessorFactory(byte[] needle) {
        this.needle = (byte[])needle.clone();
        this.jumpTable = new int[needle.length + 1];
        int j = 0;
        for (int i = 1; i < needle.length; ++i) {
            while (j > 0 && needle[j] != needle[i]) {
                j = this.jumpTable[j];
            }
            if (needle[j] == needle[i]) {
                // empty if block
            }
            this.jumpTable[i + 1] = ++j;
        }
    }

    @Override
    public Processor newSearchProcessor() {
        return new Processor(this.needle, this.jumpTable);
    }

    public static class Processor
    implements SearchProcessor {
        private final byte[] needle;
        private final int[] jumpTable;
        private long currentPosition;

        Processor(byte[] needle, int[] jumpTable) {
            this.needle = needle;
            this.jumpTable = jumpTable;
        }

        @Override
        public boolean process(byte value) {
            while (this.currentPosition > 0L && PlatformDependent.getByte((byte[])this.needle, (long)this.currentPosition) != value) {
                this.currentPosition = PlatformDependent.getInt((int[])this.jumpTable, (long)this.currentPosition);
            }
            if (PlatformDependent.getByte((byte[])this.needle, (long)this.currentPosition) == value) {
                ++this.currentPosition;
            }
            if (this.currentPosition == (long)this.needle.length) {
                this.currentPosition = PlatformDependent.getInt((int[])this.jumpTable, (long)this.currentPosition);
                return false;
            }
            return true;
        }

        @Override
        public void reset() {
            this.currentPosition = 0L;
        }
    }
}

