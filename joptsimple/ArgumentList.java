/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

class ArgumentList {
    private final String[] arguments;
    private int currentIndex;

    ArgumentList(String ... arguments) {
        this.arguments = (String[])arguments.clone();
    }

    boolean hasMore() {
        return this.currentIndex < this.arguments.length;
    }

    String next() {
        return this.arguments[this.currentIndex++];
    }

    String peek() {
        return this.arguments[this.currentIndex];
    }

    void treatNextAsLongOption() {
        if ('-' != this.arguments[this.currentIndex].charAt(0)) {
            this.arguments[this.currentIndex] = "--" + this.arguments[this.currentIndex];
        }
    }
}

