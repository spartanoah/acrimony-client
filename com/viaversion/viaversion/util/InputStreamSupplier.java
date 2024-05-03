/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.util;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {
    public InputStream get() throws IOException;
}

