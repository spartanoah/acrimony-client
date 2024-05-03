/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

interface PemEncoded
extends ByteBufHolder {
    public boolean isSensitive();

    @Override
    public PemEncoded copy();

    @Override
    public PemEncoded duplicate();

    public PemEncoded retainedDuplicate();

    public PemEncoded replace(ByteBuf var1);

    @Override
    public PemEncoded retain();

    @Override
    public PemEncoded retain(int var1);

    public PemEncoded touch();

    public PemEncoded touch(Object var1);
}

