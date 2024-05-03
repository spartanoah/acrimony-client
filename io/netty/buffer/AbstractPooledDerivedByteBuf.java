/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledDuplicatedByteBuf;
import io.netty.buffer.PooledSlicedByteBuf;
import io.netty.buffer.SimpleLeakAwareByteBuf;
import io.netty.buffer.UnpooledDuplicatedByteBuf;
import io.netty.buffer.UnpooledSlicedByteBuf;
import io.netty.util.internal.ObjectPool;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class AbstractPooledDerivedByteBuf
extends AbstractReferenceCountedByteBuf {
    private final ObjectPool.Handle<AbstractPooledDerivedByteBuf> recyclerHandle;
    private AbstractByteBuf rootParent;
    private ByteBuf parent;

    AbstractPooledDerivedByteBuf(ObjectPool.Handle<? extends AbstractPooledDerivedByteBuf> recyclerHandle) {
        super(0);
        this.recyclerHandle = recyclerHandle;
    }

    final void parent(ByteBuf newParent) {
        assert (newParent instanceof SimpleLeakAwareByteBuf);
        this.parent = newParent;
    }

    @Override
    public final AbstractByteBuf unwrap() {
        return this.rootParent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final <U extends AbstractPooledDerivedByteBuf> U init(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex, int maxCapacity) {
        wrapped.retain();
        this.parent = wrapped;
        this.rootParent = unwrapped;
        try {
            this.maxCapacity(maxCapacity);
            this.setIndex0(readerIndex, writerIndex);
            this.resetRefCnt();
            AbstractPooledDerivedByteBuf castThis = this;
            wrapped = null;
            AbstractPooledDerivedByteBuf abstractPooledDerivedByteBuf = castThis;
            return (U)abstractPooledDerivedByteBuf;
        } finally {
            if (wrapped != null) {
                this.rootParent = null;
                this.parent = null;
                wrapped.release();
            }
        }
    }

    @Override
    protected final void deallocate() {
        ByteBuf parent = this.parent;
        this.recyclerHandle.recycle(this);
        parent.release();
    }

    @Override
    public final ByteBufAllocator alloc() {
        return this.unwrap().alloc();
    }

    @Override
    @Deprecated
    public final ByteOrder order() {
        return this.unwrap().order();
    }

    public boolean isReadOnly() {
        return this.unwrap().isReadOnly();
    }

    @Override
    public final boolean isDirect() {
        return this.unwrap().isDirect();
    }

    @Override
    public boolean hasArray() {
        return this.unwrap().hasArray();
    }

    @Override
    public byte[] array() {
        return this.unwrap().array();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.unwrap().hasMemoryAddress();
    }

    public boolean isContiguous() {
        return this.unwrap().isContiguous();
    }

    @Override
    public final int nioBufferCount() {
        return this.unwrap().nioBufferCount();
    }

    @Override
    public final ByteBuffer internalNioBuffer(int index, int length) {
        return this.nioBuffer(index, length);
    }

    public final ByteBuf retainedSlice() {
        int index = this.readerIndex();
        return this.retainedSlice(index, this.writerIndex() - index);
    }

    @Override
    public ByteBuf slice(int index, int length) {
        this.ensureAccessible();
        return new PooledNonRetainedSlicedByteBuf(this, this.unwrap(), index, length);
    }

    final ByteBuf duplicate0() {
        this.ensureAccessible();
        return new PooledNonRetainedDuplicateByteBuf(this, this.unwrap());
    }

    private static final class PooledNonRetainedSlicedByteBuf
    extends UnpooledSlicedByteBuf {
        private final ByteBuf referenceCountDelegate;

        PooledNonRetainedSlicedByteBuf(ByteBuf referenceCountDelegate, AbstractByteBuf buffer, int index, int length) {
            super(buffer, index, length);
            this.referenceCountDelegate = referenceCountDelegate;
        }

        boolean isAccessible0() {
            return this.referenceCountDelegate.isAccessible();
        }

        int refCnt0() {
            return this.referenceCountDelegate.refCnt();
        }

        ByteBuf retain0() {
            this.referenceCountDelegate.retain();
            return this;
        }

        ByteBuf retain0(int increment) {
            this.referenceCountDelegate.retain(increment);
            return this;
        }

        ByteBuf touch0() {
            this.referenceCountDelegate.touch();
            return this;
        }

        ByteBuf touch0(Object hint) {
            this.referenceCountDelegate.touch(hint);
            return this;
        }

        boolean release0() {
            return this.referenceCountDelegate.release();
        }

        boolean release0(int decrement) {
            return this.referenceCountDelegate.release(decrement);
        }

        @Override
        public ByteBuf duplicate() {
            this.ensureAccessible();
            return new PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, this.unwrap()).setIndex(this.idx(this.readerIndex()), this.idx(this.writerIndex()));
        }

        public ByteBuf retainedDuplicate() {
            return PooledDuplicatedByteBuf.newInstance(this.unwrap(), this, this.idx(this.readerIndex()), this.idx(this.writerIndex()));
        }

        @Override
        public ByteBuf slice(int index, int length) {
            this.checkIndex(index, length);
            return new PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, this.unwrap(), this.idx(index), length);
        }

        public ByteBuf retainedSlice() {
            return this.retainedSlice(0, this.capacity());
        }

        public ByteBuf retainedSlice(int index, int length) {
            return PooledSlicedByteBuf.newInstance(this.unwrap(), this, this.idx(index), length);
        }
    }

    private static final class PooledNonRetainedDuplicateByteBuf
    extends UnpooledDuplicatedByteBuf {
        private final ByteBuf referenceCountDelegate;

        PooledNonRetainedDuplicateByteBuf(ByteBuf referenceCountDelegate, AbstractByteBuf buffer) {
            super(buffer);
            this.referenceCountDelegate = referenceCountDelegate;
        }

        boolean isAccessible0() {
            return this.referenceCountDelegate.isAccessible();
        }

        int refCnt0() {
            return this.referenceCountDelegate.refCnt();
        }

        ByteBuf retain0() {
            this.referenceCountDelegate.retain();
            return this;
        }

        ByteBuf retain0(int increment) {
            this.referenceCountDelegate.retain(increment);
            return this;
        }

        ByteBuf touch0() {
            this.referenceCountDelegate.touch();
            return this;
        }

        ByteBuf touch0(Object hint) {
            this.referenceCountDelegate.touch(hint);
            return this;
        }

        boolean release0() {
            return this.referenceCountDelegate.release();
        }

        boolean release0(int decrement) {
            return this.referenceCountDelegate.release(decrement);
        }

        @Override
        public ByteBuf duplicate() {
            this.ensureAccessible();
            return new PooledNonRetainedDuplicateByteBuf(this.referenceCountDelegate, this);
        }

        public ByteBuf retainedDuplicate() {
            return PooledDuplicatedByteBuf.newInstance(this.unwrap(), this, this.readerIndex(), this.writerIndex());
        }

        @Override
        public ByteBuf slice(int index, int length) {
            this.checkIndex(index, length);
            return new PooledNonRetainedSlicedByteBuf(this.referenceCountDelegate, this.unwrap(), index, length);
        }

        public ByteBuf retainedSlice() {
            return this.retainedSlice(this.readerIndex(), this.capacity());
        }

        public ByteBuf retainedSlice(int index, int length) {
            return PooledSlicedByteBuf.newInstance(this.unwrap(), this, index, length);
        }
    }
}

