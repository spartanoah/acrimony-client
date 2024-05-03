/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.dns.DnsRecord;

public interface DnsRawRecord
extends DnsRecord,
ByteBufHolder {
    @Override
    public DnsRawRecord copy();

    @Override
    public DnsRawRecord duplicate();

    public DnsRawRecord retainedDuplicate();

    public DnsRawRecord replace(ByteBuf var1);

    @Override
    public DnsRawRecord retain();

    @Override
    public DnsRawRecord retain(int var1);

    public DnsRawRecord touch();

    public DnsRawRecord touch(Object var1);
}

