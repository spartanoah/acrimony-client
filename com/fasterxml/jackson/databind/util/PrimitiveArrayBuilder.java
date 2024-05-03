/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

public abstract class PrimitiveArrayBuilder<T> {
    static final int INITIAL_CHUNK_SIZE = 12;
    static final int SMALL_CHUNK_SIZE = 16384;
    static final int MAX_CHUNK_SIZE = 262144;
    protected T _freeBuffer;
    protected Node<T> _bufferHead;
    protected Node<T> _bufferTail;
    protected int _bufferedEntryCount;

    protected PrimitiveArrayBuilder() {
    }

    public int bufferedSize() {
        return this._bufferedEntryCount;
    }

    public T resetAndStart() {
        this._reset();
        return this._freeBuffer == null ? this._constructArray(12) : this._freeBuffer;
    }

    public final T appendCompletedChunk(T fullChunk, int fullChunkLength) {
        Node<T> next = new Node<T>(fullChunk, fullChunkLength);
        if (this._bufferHead == null) {
            this._bufferTail = next;
            this._bufferHead = this._bufferTail;
        } else {
            this._bufferTail.linkNext(next);
            this._bufferTail = next;
        }
        this._bufferedEntryCount += fullChunkLength;
        int nextLen = fullChunkLength;
        nextLen = nextLen < 16384 ? (nextLen += nextLen) : (nextLen += nextLen >> 2);
        return this._constructArray(nextLen);
    }

    public T completeAndClearBuffer(T lastChunk, int lastChunkEntries) {
        int totalSize = lastChunkEntries + this._bufferedEntryCount;
        T resultArray = this._constructArray(totalSize);
        int ptr = 0;
        for (Node<T> n = this._bufferHead; n != null; n = n.next()) {
            ptr = n.copyData(resultArray, ptr);
        }
        System.arraycopy(lastChunk, 0, resultArray, ptr, lastChunkEntries);
        if ((ptr += lastChunkEntries) != totalSize) {
            throw new IllegalStateException("Should have gotten " + totalSize + " entries, got " + ptr);
        }
        return resultArray;
    }

    protected abstract T _constructArray(int var1);

    protected void _reset() {
        if (this._bufferTail != null) {
            this._freeBuffer = this._bufferTail.getData();
        }
        this._bufferTail = null;
        this._bufferHead = null;
        this._bufferedEntryCount = 0;
    }

    static final class Node<T> {
        final T _data;
        final int _dataLength;
        Node<T> _next;

        public Node(T data, int dataLen) {
            this._data = data;
            this._dataLength = dataLen;
        }

        public T getData() {
            return this._data;
        }

        public int copyData(T dst, int ptr) {
            System.arraycopy(this._data, 0, dst, ptr, this._dataLength);
            return ptr += this._dataLength;
        }

        public Node<T> next() {
            return this._next;
        }

        public void linkNext(Node<T> next) {
            if (this._next != null) {
                throw new IllegalStateException();
            }
            this._next = next;
        }
    }
}

