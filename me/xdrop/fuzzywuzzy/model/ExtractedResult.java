/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.model;

public class ExtractedResult
implements Comparable<ExtractedResult> {
    private String string;
    private int score;
    private int index;

    public ExtractedResult(String string, int score, int index) {
        this.string = string;
        this.score = score;
        this.index = index;
    }

    @Override
    public int compareTo(ExtractedResult o) {
        return Integer.compare(this.getScore(), o.getScore());
    }

    public String getString() {
        return this.string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getScore() {
        return this.score;
    }

    public int getIndex() {
        return this.index;
    }

    public String toString() {
        return "(string: " + this.string + ", score: " + this.score + ", index: " + this.index + ")";
    }
}

