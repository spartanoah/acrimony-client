/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.async;

import com.fasterxml.jackson.core.async.NonBlockingInputFeeder;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface ByteBufferFeeder
extends NonBlockingInputFeeder {
    public void feedInput(ByteBuffer var1) throws IOException;
}

