/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.http.multipart.HttpData;
import java.io.IOException;

public interface Attribute
extends HttpData {
    public String getValue() throws IOException;

    public void setValue(String var1) throws IOException;

    @Override
    public Attribute copy();

    @Override
    public Attribute duplicate();

    @Override
    public Attribute retain();

    @Override
    public Attribute retain(int var1);
}

