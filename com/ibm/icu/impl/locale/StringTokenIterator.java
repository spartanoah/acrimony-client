/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl.locale;

public class StringTokenIterator {
    private String _text;
    private String _dlms;
    private String _token;
    private int _start;
    private int _end;
    private boolean _done;

    public StringTokenIterator(String text, String dlms) {
        this._text = text;
        this._dlms = dlms;
        this.setStart(0);
    }

    public String first() {
        this.setStart(0);
        return this._token;
    }

    public String current() {
        return this._token;
    }

    public int currentStart() {
        return this._start;
    }

    public int currentEnd() {
        return this._end;
    }

    public boolean isDone() {
        return this._done;
    }

    public String next() {
        if (this.hasNext()) {
            this._start = this._end + 1;
            this._end = this.nextDelimiter(this._start);
            this._token = this._text.substring(this._start, this._end);
        } else {
            this._start = this._end;
            this._token = null;
            this._done = true;
        }
        return this._token;
    }

    public boolean hasNext() {
        return this._end < this._text.length();
    }

    public StringTokenIterator setStart(int offset) {
        if (offset > this._text.length()) {
            throw new IndexOutOfBoundsException();
        }
        this._start = offset;
        this._end = this.nextDelimiter(this._start);
        this._token = this._text.substring(this._start, this._end);
        this._done = false;
        return this;
    }

    public StringTokenIterator setText(String text) {
        this._text = text;
        this.setStart(0);
        return this;
    }

    private int nextDelimiter(int start) {
        int idx;
        block0: for (idx = start; idx < this._text.length(); ++idx) {
            char c = this._text.charAt(idx);
            for (int i = 0; i < this._dlms.length(); ++i) {
                if (c == this._dlms.charAt(i)) break block0;
            }
        }
        return idx;
    }
}

