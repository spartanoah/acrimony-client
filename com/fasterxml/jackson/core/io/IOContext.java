/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

public class IOContext {
    protected final Object _sourceRef;
    protected JsonEncoding _encoding;
    protected final boolean _managedResource;
    protected final BufferRecycler _bufferRecycler;
    protected byte[] _readIOBuffer;
    protected byte[] _writeEncodingBuffer;
    protected byte[] _base64Buffer;
    protected char[] _tokenCBuffer;
    protected char[] _concatCBuffer;
    protected char[] _nameCopyBuffer;

    public IOContext(BufferRecycler br, Object sourceRef, boolean managedResource) {
        this._bufferRecycler = br;
        this._sourceRef = sourceRef;
        this._managedResource = managedResource;
    }

    public void setEncoding(JsonEncoding enc) {
        this._encoding = enc;
    }

    public IOContext withEncoding(JsonEncoding enc) {
        this._encoding = enc;
        return this;
    }

    public Object getSourceReference() {
        return this._sourceRef;
    }

    public JsonEncoding getEncoding() {
        return this._encoding;
    }

    public boolean isResourceManaged() {
        return this._managedResource;
    }

    public TextBuffer constructTextBuffer() {
        return new TextBuffer(this._bufferRecycler);
    }

    public byte[] allocReadIOBuffer() {
        this._verifyAlloc(this._readIOBuffer);
        this._readIOBuffer = this._bufferRecycler.allocByteBuffer(0);
        return this._readIOBuffer;
    }

    public byte[] allocReadIOBuffer(int minSize) {
        this._verifyAlloc(this._readIOBuffer);
        this._readIOBuffer = this._bufferRecycler.allocByteBuffer(0, minSize);
        return this._readIOBuffer;
    }

    public byte[] allocWriteEncodingBuffer() {
        this._verifyAlloc(this._writeEncodingBuffer);
        this._writeEncodingBuffer = this._bufferRecycler.allocByteBuffer(1);
        return this._writeEncodingBuffer;
    }

    public byte[] allocWriteEncodingBuffer(int minSize) {
        this._verifyAlloc(this._writeEncodingBuffer);
        this._writeEncodingBuffer = this._bufferRecycler.allocByteBuffer(1, minSize);
        return this._writeEncodingBuffer;
    }

    public byte[] allocBase64Buffer() {
        this._verifyAlloc(this._base64Buffer);
        this._base64Buffer = this._bufferRecycler.allocByteBuffer(3);
        return this._base64Buffer;
    }

    public byte[] allocBase64Buffer(int minSize) {
        this._verifyAlloc(this._base64Buffer);
        this._base64Buffer = this._bufferRecycler.allocByteBuffer(3, minSize);
        return this._base64Buffer;
    }

    public char[] allocTokenBuffer() {
        this._verifyAlloc(this._tokenCBuffer);
        this._tokenCBuffer = this._bufferRecycler.allocCharBuffer(0);
        return this._tokenCBuffer;
    }

    public char[] allocTokenBuffer(int minSize) {
        this._verifyAlloc(this._tokenCBuffer);
        this._tokenCBuffer = this._bufferRecycler.allocCharBuffer(0, minSize);
        return this._tokenCBuffer;
    }

    public char[] allocConcatBuffer() {
        this._verifyAlloc(this._concatCBuffer);
        this._concatCBuffer = this._bufferRecycler.allocCharBuffer(1);
        return this._concatCBuffer;
    }

    public char[] allocNameCopyBuffer(int minSize) {
        this._verifyAlloc(this._nameCopyBuffer);
        this._nameCopyBuffer = this._bufferRecycler.allocCharBuffer(3, minSize);
        return this._nameCopyBuffer;
    }

    public void releaseReadIOBuffer(byte[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._readIOBuffer);
            this._readIOBuffer = null;
            this._bufferRecycler.releaseByteBuffer(0, buf);
        }
    }

    public void releaseWriteEncodingBuffer(byte[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._writeEncodingBuffer);
            this._writeEncodingBuffer = null;
            this._bufferRecycler.releaseByteBuffer(1, buf);
        }
    }

    public void releaseBase64Buffer(byte[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._base64Buffer);
            this._base64Buffer = null;
            this._bufferRecycler.releaseByteBuffer(3, buf);
        }
    }

    public void releaseTokenBuffer(char[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._tokenCBuffer);
            this._tokenCBuffer = null;
            this._bufferRecycler.releaseCharBuffer(0, buf);
        }
    }

    public void releaseConcatBuffer(char[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._concatCBuffer);
            this._concatCBuffer = null;
            this._bufferRecycler.releaseCharBuffer(1, buf);
        }
    }

    public void releaseNameCopyBuffer(char[] buf) {
        if (buf != null) {
            this._verifyRelease(buf, this._nameCopyBuffer);
            this._nameCopyBuffer = null;
            this._bufferRecycler.releaseCharBuffer(3, buf);
        }
    }

    protected final void _verifyAlloc(Object buffer) {
        if (buffer != null) {
            throw new IllegalStateException("Trying to call same allocXxx() method second time");
        }
    }

    protected final void _verifyRelease(byte[] toRelease, byte[] src) {
        if (toRelease != src && toRelease.length < src.length) {
            throw this.wrongBuf();
        }
    }

    protected final void _verifyRelease(char[] toRelease, char[] src) {
        if (toRelease != src && toRelease.length < src.length) {
            throw this.wrongBuf();
        }
    }

    private IllegalArgumentException wrongBuf() {
        return new IllegalArgumentException("Trying to release buffer smaller than original");
    }
}

