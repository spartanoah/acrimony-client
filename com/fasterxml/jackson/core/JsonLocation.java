/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import java.io.Serializable;
import java.nio.charset.Charset;

public class JsonLocation
implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_CONTENT_SNIPPET = 500;
    public static final JsonLocation NA = new JsonLocation(null, -1L, -1L, -1, -1);
    protected final long _totalBytes;
    protected final long _totalChars;
    protected final int _lineNr;
    protected final int _columnNr;
    final transient Object _sourceRef;

    public JsonLocation(Object srcRef, long totalChars, int lineNr, int colNr) {
        this(srcRef, -1L, totalChars, lineNr, colNr);
    }

    public JsonLocation(Object sourceRef, long totalBytes, long totalChars, int lineNr, int columnNr) {
        this._sourceRef = sourceRef;
        this._totalBytes = totalBytes;
        this._totalChars = totalChars;
        this._lineNr = lineNr;
        this._columnNr = columnNr;
    }

    public Object getSourceRef() {
        return this._sourceRef;
    }

    public int getLineNr() {
        return this._lineNr;
    }

    public int getColumnNr() {
        return this._columnNr;
    }

    public long getCharOffset() {
        return this._totalChars;
    }

    public long getByteOffset() {
        return this._totalBytes;
    }

    public String sourceDescription() {
        return this._appendSourceDesc(new StringBuilder(100)).toString();
    }

    public int hashCode() {
        int hash = this._sourceRef == null ? 1 : this._sourceRef.hashCode();
        hash ^= this._lineNr;
        hash += this._columnNr;
        hash ^= (int)this._totalChars;
        return hash += (int)this._totalBytes;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof JsonLocation)) {
            return false;
        }
        JsonLocation otherLoc = (JsonLocation)other;
        if (this._sourceRef == null ? otherLoc._sourceRef != null : !this._sourceRef.equals(otherLoc._sourceRef)) {
            return false;
        }
        return this._lineNr == otherLoc._lineNr && this._columnNr == otherLoc._columnNr && this._totalChars == otherLoc._totalChars && this.getByteOffset() == otherLoc.getByteOffset();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("[Source: ");
        this._appendSourceDesc(sb);
        sb.append("; line: ");
        sb.append(this._lineNr);
        sb.append(", column: ");
        sb.append(this._columnNr);
        sb.append(']');
        return sb.toString();
    }

    protected StringBuilder _appendSourceDesc(StringBuilder sb) {
        int len;
        Object srcRef = this._sourceRef;
        if (srcRef == null) {
            sb.append("UNKNOWN");
            return sb;
        }
        Class<?> srcType = srcRef instanceof Class ? (Class<?>)srcRef : srcRef.getClass();
        String tn = srcType.getName();
        if (tn.startsWith("java.")) {
            tn = srcType.getSimpleName();
        } else if (srcRef instanceof byte[]) {
            tn = "byte[]";
        } else if (srcRef instanceof char[]) {
            tn = "char[]";
        }
        sb.append('(').append(tn).append(')');
        String charStr = " chars";
        if (srcRef instanceof CharSequence) {
            CharSequence cs = (CharSequence)srcRef;
            len = cs.length();
            len -= this._append(sb, cs.subSequence(0, Math.min(len, 500)).toString());
        } else if (srcRef instanceof char[]) {
            char[] ch = (char[])srcRef;
            len = ch.length;
            len -= this._append(sb, new String(ch, 0, Math.min(len, 500)));
        } else if (srcRef instanceof byte[]) {
            byte[] b = (byte[])srcRef;
            int maxLen = Math.min(b.length, 500);
            this._append(sb, new String(b, 0, maxLen, Charset.forName("UTF-8")));
            len = b.length - maxLen;
            charStr = " bytes";
        } else {
            len = 0;
        }
        if (len > 0) {
            sb.append("[truncated ").append(len).append(charStr).append(']');
        }
        return sb;
    }

    private int _append(StringBuilder sb, String content) {
        sb.append('\"').append(content).append('\"');
        return content.length();
    }
}

