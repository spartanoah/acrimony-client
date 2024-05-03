/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.io.Serializable;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public final class ByteArrayBuffer
implements Serializable {
    private static final long serialVersionUID = 4359112959524048036L;
    private byte[] array;
    private int len;

    public ByteArrayBuffer(int capacity) {
        Args.notNegative(capacity, "Buffer capacity");
        this.array = new byte[capacity];
    }

    private void expand(int newlen) {
        byte[] newArray = new byte[Math.max(this.array.length << 1, newlen)];
        System.arraycopy(this.array, 0, newArray, 0, this.len);
        this.array = newArray;
    }

    public void append(byte[] b, int off, int len) {
        if (b == null) {
            return;
        }
        if (off < 0 || off > b.length || len < 0 || off + len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        if (len == 0) {
            return;
        }
        int newlen = this.len + len;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        System.arraycopy(b, off, this.array, this.len, len);
        this.len = newlen;
    }

    public void append(int b) {
        int newlen = this.len + 1;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        this.array[this.len] = (byte)b;
        this.len = newlen;
    }

    public void append(char[] b, int off, int len) {
        if (b == null) {
            return;
        }
        if (off < 0 || off > b.length || len < 0 || off + len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("off: " + off + " len: " + len + " b.length: " + b.length);
        }
        if (len == 0) {
            return;
        }
        int oldlen = this.len;
        int newlen = oldlen + len;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        int i1 = off;
        for (int i2 = oldlen; i2 < newlen; ++i2) {
            char c = b[i1];
            this.array[i2] = c >= ' ' && c <= '~' || c >= '\u00a0' && c <= '\u00ff' || c == '\t' ? (int)c : 63;
            ++i1;
        }
        this.len = newlen;
    }

    public void append(CharArrayBuffer b, int off, int len) {
        if (b == null) {
            return;
        }
        this.append(b.array(), off, len);
    }

    public void clear() {
        this.len = 0;
    }

    public byte[] toByteArray() {
        byte[] b = new byte[this.len];
        if (this.len > 0) {
            System.arraycopy(this.array, 0, b, 0, this.len);
        }
        return b;
    }

    public int byteAt(int i) {
        return this.array[i];
    }

    public int capacity() {
        return this.array.length;
    }

    public int length() {
        return this.len;
    }

    public void ensureCapacity(int required) {
        if (required <= 0) {
            return;
        }
        int available = this.array.length - this.len;
        if (required > available) {
            this.expand(this.len + required);
        }
    }

    public byte[] array() {
        return this.array;
    }

    public void setLength(int len) {
        if (len < 0 || len > this.array.length) {
            throw new IndexOutOfBoundsException("len: " + len + " < 0 or > buffer len: " + this.array.length);
        }
        this.len = len;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public boolean isFull() {
        return this.len == this.array.length;
    }

    public int indexOf(byte b, int from, int to) {
        int endIndex;
        int beginIndex = from;
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if ((endIndex = to) > this.len) {
            endIndex = this.len;
        }
        if (beginIndex > endIndex) {
            return -1;
        }
        for (int i = beginIndex; i < endIndex; ++i) {
            if (this.array[i] != b) continue;
            return i;
        }
        return -1;
    }

    public int indexOf(byte b) {
        return this.indexOf(b, 0, this.len);
    }
}

