/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.signature.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import java.util.function.Consumer;

@FunctionalInterface
public interface DataConsumer
extends Consumer<byte[]> {
    @Override
    default public void accept(UUID uuid) {
        byte[] serializedUuid = new byte[16];
        ByteBuffer.wrap(serializedUuid).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        this.accept(serializedUuid);
    }
}

