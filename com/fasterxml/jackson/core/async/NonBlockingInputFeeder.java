/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.async;

public interface NonBlockingInputFeeder {
    public boolean needMoreInput();

    public void endOfInput();
}

