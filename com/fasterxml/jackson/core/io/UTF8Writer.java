/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.io.IOContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class UTF8Writer
extends Writer {
    static final int SURR1_FIRST = 55296;
    static final int SURR1_LAST = 56319;
    static final int SURR2_FIRST = 56320;
    static final int SURR2_LAST = 57343;
    private final IOContext _context;
    private OutputStream _out;
    private byte[] _outBuffer;
    private final int _outBufferEnd;
    private int _outPtr;
    private int _surrogate;

    public UTF8Writer(IOContext ctxt, OutputStream out) {
        this._context = ctxt;
        this._out = out;
        this._outBuffer = ctxt.allocWriteEncodingBuffer();
        this._outBufferEnd = this._outBuffer.length - 4;
        this._outPtr = 0;
    }

    @Override
    public Writer append(char c) throws IOException {
        this.write(c);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (this._out != null) {
            if (this._outPtr > 0) {
                this._out.write(this._outBuffer, 0, this._outPtr);
                this._outPtr = 0;
            }
            OutputStream out = this._out;
            this._out = null;
            byte[] buf = this._outBuffer;
            if (buf != null) {
                this._outBuffer = null;
                this._context.releaseWriteEncodingBuffer(buf);
            }
            out.close();
            int code = this._surrogate;
            this._surrogate = 0;
            if (code > 0) {
                UTF8Writer.illegalSurrogate(code);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (this._out != null) {
            if (this._outPtr > 0) {
                this._out.write(this._outBuffer, 0, this._outPtr);
                this._outPtr = 0;
            }
            this._out.flush();
        }
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        this.write(cbuf, 0, cbuf.length);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len < 2) {
            if (len == 1) {
                this.write(cbuf[off]);
            }
            return;
        }
        if (this._surrogate > 0) {
            second = cbuf[off++];
            --len;
            this.write(this.convertSurrogate(second));
        }
        outPtr = this._outPtr;
        outBuf = this._outBuffer;
        outBufLast = this._outBufferEnd;
        len += off;
        block0: while (off < len) {
            if (outPtr >= outBufLast) {
                this._out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }
            if ((c = cbuf[off++]) >= 128) ** GOTO lbl28
            outBuf[outPtr++] = (byte)c;
            maxInCount = len - off;
            maxOutCount = outBufLast - outPtr;
            if (maxInCount > maxOutCount) {
                maxInCount = maxOutCount;
            }
            maxInCount += off;
            while (off < maxInCount) {
                if ((c = cbuf[off++]) < 128) {
                    outBuf[outPtr++] = (byte)c;
                    continue;
                }
lbl28:
                // 3 sources

                if (c < 2048) {
                    outBuf[outPtr++] = (byte)(192 | c >> 6);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c < 55296 || c > 57343) {
                    outBuf[outPtr++] = (byte)(224 | c >> 12);
                    outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c > 56319) {
                    this._outPtr = outPtr;
                    UTF8Writer.illegalSurrogate(c);
                }
                this._surrogate = c;
                if (off >= len) break block0;
                if ((c = this.convertSurrogate(cbuf[off++])) > 0x10FFFF) {
                    this._outPtr = outPtr;
                    UTF8Writer.illegalSurrogate(c);
                }
                outBuf[outPtr++] = (byte)(240 | c >> 18);
                outBuf[outPtr++] = (byte)(128 | c >> 12 & 63);
                outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                outBuf[outPtr++] = (byte)(128 | c & 63);
                continue block0;
            }
        }
        this._outPtr = outPtr;
    }

    @Override
    public void write(int c) throws IOException {
        if (this._surrogate > 0) {
            c = this.convertSurrogate(c);
        } else if (c >= 55296 && c <= 57343) {
            if (c > 56319) {
                UTF8Writer.illegalSurrogate(c);
            }
            this._surrogate = c;
            return;
        }
        if (this._outPtr >= this._outBufferEnd) {
            this._out.write(this._outBuffer, 0, this._outPtr);
            this._outPtr = 0;
        }
        if (c < 128) {
            this._outBuffer[this._outPtr++] = (byte)c;
        } else {
            int ptr = this._outPtr;
            if (c < 2048) {
                this._outBuffer[ptr++] = (byte)(0xC0 | c >> 6);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            } else if (c <= 65535) {
                this._outBuffer[ptr++] = (byte)(0xE0 | c >> 12);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 6 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            } else {
                if (c > 0x10FFFF) {
                    UTF8Writer.illegalSurrogate(c);
                }
                this._outBuffer[ptr++] = (byte)(0xF0 | c >> 18);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 12 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c >> 6 & 0x3F);
                this._outBuffer[ptr++] = (byte)(0x80 | c & 0x3F);
            }
            this._outPtr = ptr;
        }
    }

    @Override
    public void write(String str) throws IOException {
        this.write(str, 0, str.length());
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        if (len < 2) {
            if (len == 1) {
                this.write(str.charAt(off));
            }
            return;
        }
        if (this._surrogate > 0) {
            second = str.charAt(off++);
            --len;
            this.write(this.convertSurrogate(second));
        }
        outPtr = this._outPtr;
        outBuf = this._outBuffer;
        outBufLast = this._outBufferEnd;
        len += off;
        block0: while (off < len) {
            if (outPtr >= outBufLast) {
                this._out.write(outBuf, 0, outPtr);
                outPtr = 0;
            }
            if ((c = str.charAt(off++)) >= 128) ** GOTO lbl28
            outBuf[outPtr++] = (byte)c;
            maxInCount = len - off;
            maxOutCount = outBufLast - outPtr;
            if (maxInCount > maxOutCount) {
                maxInCount = maxOutCount;
            }
            maxInCount += off;
            while (off < maxInCount) {
                if ((c = (int)str.charAt(off++)) < 128) {
                    outBuf[outPtr++] = (byte)c;
                    continue;
                }
lbl28:
                // 3 sources

                if (c < 2048) {
                    outBuf[outPtr++] = (byte)(192 | c >> 6);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c < 55296 || c > 57343) {
                    outBuf[outPtr++] = (byte)(224 | c >> 12);
                    outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                    outBuf[outPtr++] = (byte)(128 | c & 63);
                    continue block0;
                }
                if (c > 56319) {
                    this._outPtr = outPtr;
                    UTF8Writer.illegalSurrogate(c);
                }
                this._surrogate = c;
                if (off >= len) break block0;
                if ((c = this.convertSurrogate(str.charAt(off++))) > 0x10FFFF) {
                    this._outPtr = outPtr;
                    UTF8Writer.illegalSurrogate(c);
                }
                outBuf[outPtr++] = (byte)(240 | c >> 18);
                outBuf[outPtr++] = (byte)(128 | c >> 12 & 63);
                outBuf[outPtr++] = (byte)(128 | c >> 6 & 63);
                outBuf[outPtr++] = (byte)(128 | c & 63);
                continue block0;
            }
        }
        this._outPtr = outPtr;
    }

    protected int convertSurrogate(int secondPart) throws IOException {
        int firstPart = this._surrogate;
        this._surrogate = 0;
        if (secondPart < 56320 || secondPart > 57343) {
            throw new IOException("Broken surrogate pair: first char 0x" + Integer.toHexString(firstPart) + ", second 0x" + Integer.toHexString(secondPart) + "; illegal combination");
        }
        return 65536 + (firstPart - 55296 << 10) + (secondPart - 56320);
    }

    protected static void illegalSurrogate(int code) throws IOException {
        throw new IOException(UTF8Writer.illegalSurrogateDesc(code));
    }

    protected static String illegalSurrogateDesc(int code) {
        if (code > 0x10FFFF) {
            return "Illegal character point (0x" + Integer.toHexString(code) + ") to output; max is 0x10FFFF as per RFC 4627";
        }
        if (code >= 55296) {
            if (code <= 56319) {
                return "Unmatched first part of surrogate pair (0x" + Integer.toHexString(code) + ")";
            }
            return "Unmatched second part of surrogate pair (0x" + Integer.toHexString(code) + ")";
        }
        return "Illegal character point (0x" + Integer.toHexString(code) + ") to output";
    }
}

