/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer.search;

import io.netty.buffer.search.AbstractSearchProcessorFactory;
import io.netty.buffer.search.SearchProcessor;
import io.netty.util.internal.PlatformDependent;

public class BitapSearchProcessorFactory
extends AbstractSearchProcessorFactory {
    private final long[] bitMasks = new long[256];
    private final long successBit;

    BitapSearchProcessorFactory(byte[] needle) {
        if (needle.length > 64) {
            throw new IllegalArgumentException("Maximum supported search pattern length is 64, got " + needle.length);
        }
        long bit = 1L;
        for (byte c : needle) {
            int n = c & 0xFF;
            this.bitMasks[n] = this.bitMasks[n] | bit;
            bit <<= 1;
        }
        this.successBit = 1L << needle.length - 1;
    }

    @Override
    public Processor newSearchProcessor() {
        return new Processor(this.bitMasks, this.successBit);
    }

    public static class Processor
    implements SearchProcessor {
        private final long[] bitMasks;
        private final long successBit;
        private long currentMask;

        Processor(long[] bitMasks, long successBit) {
            this.bitMasks = bitMasks;
            this.successBit = successBit;
        }

        @Override
        public boolean process(byte value) {
            this.currentMask = (this.currentMask << 1 | 1L) & PlatformDependent.getLong((long[])this.bitMasks, (long)((long)value & 0xFFL));
            return (this.currentMask & this.successBit) == 0L;
        }

        @Override
        public void reset() {
            this.currentMask = 0L;
        }
    }
}

