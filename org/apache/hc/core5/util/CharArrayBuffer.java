/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.io.Serializable;
import java.nio.CharBuffer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;

public final class CharArrayBuffer
implements CharSequence,
Serializable {
    private static final long serialVersionUID = -6208952725094867135L;
    private char[] array;
    private int len;

    public CharArrayBuffer(int capacity) {
        Args.notNegative(capacity, "Buffer capacity");
        this.array = new char[capacity];
    }

    private void expand(int newlen) {
        char[] newArray = new char[Math.max(this.array.length << 1, newlen)];
        System.arraycopy(this.array, 0, newArray, 0, this.len);
        this.array = newArray;
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
        int newlen = this.len + len;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        System.arraycopy(b, off, this.array, this.len, len);
        this.len = newlen;
    }

    public void append(String str) {
        String s = str != null ? str : "null";
        int strlen = s.length();
        int newlen = this.len + strlen;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        s.getChars(0, strlen, this.array, this.len);
        this.len = newlen;
    }

    public void append(CharArrayBuffer b, int off, int len) {
        if (b == null) {
            return;
        }
        this.append(b.array, off, len);
    }

    public void append(CharArrayBuffer b) {
        if (b == null) {
            return;
        }
        this.append(b.array, 0, b.len);
    }

    public void append(char ch) {
        int newlen = this.len + 1;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        this.array[this.len] = ch;
        this.len = newlen;
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
        int oldlen = this.len;
        int newlen = oldlen + len;
        if (newlen > this.array.length) {
            this.expand(newlen);
        }
        int i1 = off;
        for (int i2 = oldlen; i2 < newlen; ++i2) {
            this.array[i2] = (char)(b[i1] & 0xFF);
            ++i1;
        }
        this.len = newlen;
    }

    public void append(ByteArrayBuffer b, int off, int len) {
        if (b == null) {
            return;
        }
        this.append(b.array(), off, len);
    }

    public void append(Object obj) {
        this.append(String.valueOf(obj));
    }

    public void clear() {
        this.len = 0;
    }

    public char[] toCharArray() {
        char[] b = new char[this.len];
        if (this.len > 0) {
            System.arraycopy(this.array, 0, b, 0, this.len);
        }
        return b;
    }

    @Override
    public char charAt(int i) {
        return this.array[i];
    }

    public char[] array() {
        return this.array;
    }

    public int capacity() {
        return this.array.length;
    }

    @Override
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

    public int indexOf(int ch, int from, int to) {
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
            if (this.array[i] != ch) continue;
            return i;
        }
        return -1;
    }

    public int indexOf(int ch) {
        return this.indexOf(ch, 0, this.len);
    }

    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        return new String(this.array, beginIndex, endIndex - beginIndex);
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    public String substringTrimmed(int beginIndex, int endIndex) {
        int beginIndex0;
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        int endIndex0 = endIndex;
        for (beginIndex0 = beginIndex; beginIndex0 < endIndex && CharArrayBuffer.isWhitespace(this.array[beginIndex0]); ++beginIndex0) {
        }
        while (endIndex0 > beginIndex0 && CharArrayBuffer.isWhitespace(this.array[endIndex0 - 1])) {
            --endIndex0;
        }
        return new String(this.array, beginIndex0, endIndex0 - beginIndex0);
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("Negative beginIndex: " + beginIndex);
        }
        if (endIndex > this.len) {
            throw new IndexOutOfBoundsException("endIndex: " + endIndex + " > length: " + this.len);
        }
        if (beginIndex > endIndex) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex + " > endIndex: " + endIndex);
        }
        return CharBuffer.wrap(this.array, beginIndex, endIndex - beginIndex);
    }

    @Override
    public String toString() {
        return new String(this.array, 0, this.len);
    }
}

