/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamFactory {
    public InputStream create(InputStream var1) throws IOException;
}

