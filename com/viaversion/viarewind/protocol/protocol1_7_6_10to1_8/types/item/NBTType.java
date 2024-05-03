/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.libs.opennbt.tag.io.NBTIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBTType
extends Type<CompoundTag> {
    public NBTType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(ByteBuf buffer) throws IOException {
        short length = buffer.readShort();
        if (length <= 0) {
            return null;
        }
        ByteBuf compressed = buffer.readSlice(length);
        try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteBufInputStream(compressed));){
            CompoundTag compoundTag = NBTIO.reader(CompoundTag.class).named().read(gzipStream);
            return compoundTag;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(ByteBuf buffer, CompoundTag nbt) throws Exception {
        if (nbt == null) {
            buffer.writeShort(-1);
            return;
        }
        ByteBuf compressedBuf = buffer.alloc().buffer();
        try {
            try (GZIPOutputStream gzipStream = new GZIPOutputStream(new ByteBufOutputStream(compressedBuf));){
                NBTIO.writer().named().write(gzipStream, (Tag)nbt);
            }
            buffer.writeShort(compressedBuf.readableBytes());
            buffer.writeBytes(compressedBuf);
        } finally {
            compressedBuf.release();
        }
    }
}

