/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import java.nio.ByteBuffer;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.util.Args;

abstract class SSLManagedBuffer {
    SSLManagedBuffer() {
    }

    abstract ByteBuffer acquire();

    abstract void release();

    abstract boolean isAcquired();

    abstract boolean hasData();

    static SSLManagedBuffer create(SSLBufferMode mode, int size) {
        return mode == SSLBufferMode.DYNAMIC ? new DynamicBuffer(size) : new StaticBuffer(size);
    }

    static final class DynamicBuffer
    extends SSLManagedBuffer {
        private ByteBuffer wrapped;
        private final int length;

        public DynamicBuffer(int size) {
            Args.positive(size, "size");
            this.length = size;
        }

        @Override
        public ByteBuffer acquire() {
            if (this.wrapped != null) {
                return this.wrapped;
            }
            this.wrapped = ByteBuffer.allocate(this.length);
            return this.wrapped;
        }

        @Override
        public void release() {
            this.wrapped = null;
        }

        @Override
        public boolean isAcquired() {
            return this.wrapped != null;
        }

        @Override
        public boolean hasData() {
            return this.wrapped != null && this.wrapped.position() > 0;
        }
    }

    static final class StaticBuffer
    extends SSLManagedBuffer {
        private final ByteBuffer buffer;

        public StaticBuffer(int size) {
            Args.positive(size, "size");
            this.buffer = ByteBuffer.allocate(size);
        }

        @Override
        public ByteBuffer acquire() {
            return this.buffer;
        }

        @Override
        public void release() {
        }

        @Override
        public boolean isAcquired() {
            return true;
        }

        @Override
        public boolean hasData() {
            return this.buffer.position() > 0;
        }
    }
}

