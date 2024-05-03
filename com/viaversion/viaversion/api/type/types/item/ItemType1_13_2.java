/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemType1_13_2
extends Type<Item> {
    public ItemType1_13_2() {
        super(Item.class);
    }

    @Override
    public @Nullable Item read(ByteBuf buffer) throws Exception {
        boolean present = buffer.readBoolean();
        if (!present) {
            return null;
        }
        DataItem item = new DataItem();
        item.setIdentifier(Type.VAR_INT.readPrimitive(buffer));
        item.setAmount(buffer.readByte());
        item.setTag((CompoundTag)Type.NAMED_COMPOUND_TAG.read(buffer));
        return item;
    }

    @Override
    public void write(ByteBuf buffer, @Nullable Item object) throws Exception {
        if (object == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Type.VAR_INT.writePrimitive(buffer, object.identifier());
            buffer.writeByte(object.amount());
            Type.NAMED_COMPOUND_TAG.write(buffer, object.tag());
        }
    }
}

