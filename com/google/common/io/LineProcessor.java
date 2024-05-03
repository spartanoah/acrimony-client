/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.io;

import com.google.common.annotations.Beta;
import java.io.IOException;

@Beta
public interface LineProcessor<T> {
    public boolean processLine(String var1) throws IOException;

    public T getResult();
}

