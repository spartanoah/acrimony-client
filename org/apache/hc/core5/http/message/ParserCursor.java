/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.util.Args;

public class ParserCursor {
    private final int lowerBound;
    private final int upperBound;
    private int pos;

    public ParserCursor(int lowerBound, int upperBound) {
        Args.notNegative(lowerBound, "lowerBound");
        Args.check(lowerBound <= upperBound, "lowerBound cannot be greater than upperBound");
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.pos = lowerBound;
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

    public int getUpperBound() {
        return this.upperBound;
    }

    public int getPos() {
        return this.pos;
    }

    public void updatePos(int pos) {
        if (pos < this.lowerBound) {
            throw new IndexOutOfBoundsException("pos: " + pos + " < lowerBound: " + this.lowerBound);
        }
        if (pos > this.upperBound) {
            throw new IndexOutOfBoundsException("pos: " + pos + " > upperBound: " + this.upperBound);
        }
        this.pos = pos;
    }

    public boolean atEnd() {
        return this.pos >= this.upperBound;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        buffer.append(Integer.toString(this.lowerBound));
        buffer.append('>');
        buffer.append(Integer.toString(this.pos));
        buffer.append('>');
        buffer.append(Integer.toString(this.upperBound));
        buffer.append(']');
        return buffer.toString();
    }
}

