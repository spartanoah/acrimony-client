/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.json;

public class ParserConfiguration {
    public static final int UNDEFINED_MAXIMUM_NESTING_DEPTH = -1;
    public static final int DEFAULT_MAXIMUM_NESTING_DEPTH = 512;
    protected boolean keepStrings;
    protected int maxNestingDepth;

    public ParserConfiguration() {
        this.keepStrings = false;
        this.maxNestingDepth = 512;
    }

    protected ParserConfiguration(boolean keepStrings, int maxNestingDepth) {
        this.keepStrings = keepStrings;
        this.maxNestingDepth = maxNestingDepth;
    }

    protected ParserConfiguration clone() {
        return new ParserConfiguration(this.keepStrings, this.maxNestingDepth);
    }

    public boolean isKeepStrings() {
        return this.keepStrings;
    }

    public <T extends ParserConfiguration> T withKeepStrings(boolean newVal) {
        ParserConfiguration newConfig = this.clone();
        newConfig.keepStrings = newVal;
        return (T)newConfig;
    }

    public int getMaxNestingDepth() {
        return this.maxNestingDepth;
    }

    public <T extends ParserConfiguration> T withMaxNestingDepth(int maxNestingDepth) {
        ParserConfiguration newConfig = this.clone();
        newConfig.maxNestingDepth = maxNestingDepth > -1 ? maxNestingDepth : -1;
        return (T)newConfig;
    }
}

