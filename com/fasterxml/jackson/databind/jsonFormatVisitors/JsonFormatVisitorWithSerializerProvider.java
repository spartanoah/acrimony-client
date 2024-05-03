/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.SerializerProvider;

public interface JsonFormatVisitorWithSerializerProvider {
    public SerializerProvider getProvider();

    public void setProvider(SerializerProvider var1);
}

