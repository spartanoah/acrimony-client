/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public interface InputAccessor {
    public boolean hasMoreBytes() throws IOException;

    public byte nextByte() throws IOException;

    public void reset();

    public static class Std
    implements InputAccessor {
        protected final InputStream _in;
        protected final byte[] _buffer;
        protected final int _bufferedStart;
        protected int _bufferedEnd;
        protected int _ptr;

        public Std(InputStream in, byte[] buffer) {
            this._in = in;
            this._buffer = buffer;
            this._bufferedStart = 0;
            this._ptr = 0;
            this._bufferedEnd = 0;
        }

        public Std(byte[] inputDocument) {
            this(inputDocument, 0, inputDocument.length);
        }

        public Std(byte[] inputDocument, int start, int len) {
            this._in = null;
            this._buffer = inputDocument;
            this._ptr = start;
            this._bufferedStart = start;
            this._bufferedEnd = start + len;
        }

        @Override
        public boolean hasMoreBytes() throws IOException {
            if (this._ptr < this._bufferedEnd) {
                return true;
            }
            if (this._in == null) {
                return false;
            }
            int amount = this._buffer.length - this._ptr;
            if (amount < 1) {
                return false;
            }
            int count = this._in.read(this._buffer, this._ptr, amount);
            if (count <= 0) {
                return false;
            }
            this._bufferedEnd += count;
            return true;
        }

        @Override
        public byte nextByte() throws IOException {
            if (this._ptr >= this._bufferedEnd && !this.hasMoreBytes()) {
                throw new EOFException("Failed auto-detect: could not read more than " + this._ptr + " bytes (max buffer size: " + this._buffer.length + ")");
            }
            return this._buffer[this._ptr++];
        }

        @Override
        public void reset() {
            this._ptr = this._bufferedStart;
        }

        public DataFormatMatcher createMatcher(JsonFactory match, MatchStrength matchStrength) {
            return new DataFormatMatcher(this._in, this._buffer, this._bufferedStart, this._bufferedEnd - this._bufferedStart, match, matchStrength);
        }
    }
}

