/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.io;

import java.io.IOException;

@Deprecated
public interface OutputSupplier<T> {
    public T getOutput() throws IOException;
}

