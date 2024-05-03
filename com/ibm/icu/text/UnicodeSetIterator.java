/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import java.util.Iterator;

public class UnicodeSetIterator {
    public static int IS_STRING = -1;
    public int codepoint;
    public int codepointEnd;
    public String string;
    private UnicodeSet set;
    private int endRange = 0;
    private int range = 0;
    protected int endElement;
    protected int nextElement;
    private Iterator<String> stringIterator = null;

    public UnicodeSetIterator(UnicodeSet set) {
        this.reset(set);
    }

    public UnicodeSetIterator() {
        this.reset(new UnicodeSet());
    }

    public boolean next() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.nextElement++;
            this.codepoint = this.codepointEnd;
            return true;
        }
        if (this.range < this.endRange) {
            this.loadRange(++this.range);
            this.codepointEnd = this.nextElement++;
            this.codepoint = this.codepointEnd;
            return true;
        }
        if (this.stringIterator == null) {
            return false;
        }
        this.codepoint = IS_STRING;
        this.string = this.stringIterator.next();
        if (!this.stringIterator.hasNext()) {
            this.stringIterator = null;
        }
        return true;
    }

    public boolean nextRange() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        }
        if (this.range < this.endRange) {
            this.loadRange(++this.range);
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        }
        if (this.stringIterator == null) {
            return false;
        }
        this.codepoint = IS_STRING;
        this.string = this.stringIterator.next();
        if (!this.stringIterator.hasNext()) {
            this.stringIterator = null;
        }
        return true;
    }

    public void reset(UnicodeSet uset) {
        this.set = uset;
        this.reset();
    }

    public void reset() {
        this.endRange = this.set.getRangeCount() - 1;
        this.range = 0;
        this.endElement = -1;
        this.nextElement = 0;
        if (this.endRange >= 0) {
            this.loadRange(this.range);
        }
        this.stringIterator = null;
        if (this.set.strings != null) {
            this.stringIterator = this.set.strings.iterator();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
        }
    }

    public String getString() {
        if (this.codepoint != IS_STRING) {
            return UTF16.valueOf(this.codepoint);
        }
        return this.string;
    }

    public UnicodeSet getSet() {
        return this.set;
    }

    protected void loadRange(int aRange) {
        this.nextElement = this.set.getRangeStart(aRange);
        this.endElement = this.set.getRangeEnd(aRange);
    }
}

