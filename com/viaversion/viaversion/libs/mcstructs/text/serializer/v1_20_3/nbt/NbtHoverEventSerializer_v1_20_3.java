/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.nbt;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.snbt.exceptions.SNbtSerializeException;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEventAction;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.EntityHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.ItemHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.TextHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.ITypedSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;
import com.viaversion.viaversion.libs.mcstructs.text.utils.CodecUtils;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.UUID;

public class NbtHoverEventSerializer_v1_20_3
implements ITypedSerializer<Tag, AHoverEvent> {
    private static final String ACTION = "action";
    private static final String CONTENTS = "contents";
    private static final String VALUE = "value";
    private final TextComponentCodec codec;
    private final ITypedSerializer<Tag, ATextComponent> textSerializer;
    private final SNbtSerializer<CompoundTag> sNbtSerializer;

    public NbtHoverEventSerializer_v1_20_3(TextComponentCodec codec, ITypedSerializer<Tag, ATextComponent> textSerializer, SNbtSerializer<CompoundTag> sNbtSerializer) {
        this.codec = codec;
        this.textSerializer = textSerializer;
        this.sNbtSerializer = sNbtSerializer;
    }

    @Override
    public Tag serialize(AHoverEvent object) {
        CompoundTag out = new CompoundTag();
        out.putString(ACTION, object.getAction().getName());
        if (object instanceof TextHoverEvent) {
            TextHoverEvent textHoverEvent = (TextHoverEvent)object;
            out.put(CONTENTS, this.textSerializer.serialize(textHoverEvent.getText()));
        } else if (object instanceof ItemHoverEvent) {
            ItemHoverEvent itemHoverEvent = (ItemHoverEvent)object;
            CompoundTag contents = new CompoundTag();
            contents.putString("id", itemHoverEvent.getItem().get());
            if (itemHoverEvent.getCount() != 1) {
                contents.putInt("count", itemHoverEvent.getCount());
            }
            if (itemHoverEvent.getNbt() != null) {
                try {
                    contents.putString("tag", this.sNbtSerializer.serialize(itemHoverEvent.getNbt()));
                } catch (SNbtSerializeException e) {
                    throw new IllegalStateException("Failed to serialize nbt", e);
                }
            }
            out.put(CONTENTS, contents);
        } else if (object instanceof EntityHoverEvent) {
            EntityHoverEvent entityHoverEvent = (EntityHoverEvent)object;
            CompoundTag contents = new CompoundTag();
            contents.putString("type", entityHoverEvent.getEntityType().get());
            contents.put("id", new IntArrayTag(new int[]{(int)(entityHoverEvent.getUuid().getMostSignificantBits() >> 32), (int)(entityHoverEvent.getUuid().getMostSignificantBits() & 0xFFFFFFFFL), (int)(entityHoverEvent.getUuid().getLeastSignificantBits() >> 32), (int)(entityHoverEvent.getUuid().getLeastSignificantBits() & 0xFFFFFFFFL)}));
            if (entityHoverEvent.getName() != null) {
                contents.put("name", this.textSerializer.serialize(entityHoverEvent.getName()));
            }
            out.put(CONTENTS, contents);
        } else {
            throw new IllegalArgumentException("Unknown hover event type: " + object.getClass().getName());
        }
        return out;
    }

    @Override
    public AHoverEvent deserialize(Tag object) {
        if (!(object instanceof CompoundTag)) {
            throw new IllegalArgumentException("Nbt tag is not a compound tag");
        }
        CompoundTag tag = (CompoundTag)object;
        HoverEventAction action = HoverEventAction.getByName(CodecUtils.requiredString(tag, ACTION), false);
        if (action == null) {
            throw new IllegalArgumentException("Unknown hover event action: " + (tag.get(ACTION) instanceof StringTag ? ((StringTag)tag.get(ACTION)).getValue() : ""));
        }
        if (!action.isUserDefinable()) {
            throw new IllegalArgumentException("Hover event action is not user definable: " + (Object)((Object)action));
        }
        if (tag.contains(CONTENTS)) {
            switch (action) {
                case SHOW_TEXT: {
                    return new TextHoverEvent(action, this.textSerializer.deserialize((Tag)tag.get(CONTENTS)));
                }
                case SHOW_ITEM: {
                    CompoundTag contents;
                    if (tag.get(CONTENTS) instanceof StringTag) {
                        return new ItemHoverEvent(action, Identifier.of(tag.get(CONTENTS) instanceof StringTag ? ((StringTag)tag.get(CONTENTS)).getValue() : ""), 1, null);
                    }
                    if (tag.get(CONTENTS) instanceof CompoundTag) {
                        contents = tag.get(CONTENTS) instanceof CompoundTag ? (CompoundTag)tag.get(CONTENTS) : new CompoundTag();
                        String id = CodecUtils.requiredString(contents, "id");
                        Integer count = CodecUtils.optionalInt(contents, "count");
                        String itemTag = CodecUtils.optionalString(contents, "tag");
                        try {
                            return new ItemHoverEvent(action, Identifier.of(id), count == null ? 1 : count, itemTag == null ? null : this.sNbtSerializer.deserialize(itemTag));
                        } catch (Throwable t) {
                            this.sneak(t);
                        }
                    } else {
                        throw new IllegalArgumentException("Expected string or compound tag for 'contents' tag");
                    }
                }
                case SHOW_ENTITY: {
                    CompoundTag contents = CodecUtils.requiredCompound(tag, CONTENTS);
                    Identifier type = Identifier.of(CodecUtils.requiredString(contents, "type"));
                    UUID id = this.getUUID((Tag)contents.get("id"));
                    ATextComponent name = contents.contains("name") ? this.textSerializer.deserialize((Tag)contents.get("name")) : null;
                    return new EntityHoverEvent(action, type, id, name);
                }
            }
            throw new IllegalArgumentException("Unknown hover event action: " + (Object)((Object)action));
        }
        if (tag.contains(VALUE)) {
            ATextComponent value = this.textSerializer.deserialize((Tag)tag.get(VALUE));
            try {
                switch (action) {
                    case SHOW_TEXT: {
                        return new TextHoverEvent(action, value);
                    }
                    case SHOW_ITEM: {
                        CompoundTag parsed = this.sNbtSerializer.deserialize(value.asUnformattedString());
                        Identifier id = Identifier.of(parsed.get("id") instanceof StringTag ? ((StringTag)parsed.get("id")).getValue() : "");
                        byte count = parsed.get("Count") instanceof ByteTag ? ((ByteTag)parsed.get("Count")).asByte() : (byte)0;
                        CompoundTag itemTag = parsed.get("tag") instanceof CompoundTag ? (CompoundTag)parsed.get("tag") : null;
                        return new ItemHoverEvent(action, id, count, itemTag);
                    }
                    case SHOW_ENTITY: {
                        CompoundTag parsed = this.sNbtSerializer.deserialize(value.asUnformattedString());
                        ATextComponent name = this.codec.deserializeJson(parsed.get("name") instanceof StringTag ? ((StringTag)parsed.get("name")).getValue() : "");
                        Identifier type = Identifier.of(parsed.get("type") instanceof StringTag ? ((StringTag)parsed.get("type")).getValue() : "");
                        UUID uuid = UUID.fromString(parsed.get("id") instanceof StringTag ? ((StringTag)parsed.get("id")).getValue() : "");
                        return new EntityHoverEvent(action, type, uuid, name);
                    }
                }
                throw new IllegalArgumentException("Unknown hover event action: " + (Object)((Object)action));
            } catch (Throwable t) {
                this.sneak(t);
            }
        }
        throw new IllegalArgumentException("Missing 'contents' or 'value' tag");
    }

    private <T extends Throwable> void sneak(Throwable t) throws T {
        throw t;
    }

    private UUID getUUID(Tag tag) {
        if (!(tag instanceof IntArrayTag || tag instanceof ListTag || tag instanceof StringTag)) {
            throw new IllegalArgumentException("Expected int array, list or string tag for 'id' tag");
        }
        int[] value = null;
        if (tag instanceof StringTag) {
            return UUID.fromString(((StringTag)tag).getValue());
        }
        if (tag instanceof IntArrayTag) {
            value = ((IntArrayTag)tag).getValue();
            if (value.length != 4) {
                throw new IllegalArgumentException("Expected int array with 4 values for 'id' tag");
            }
        } else {
            ListTag list = (ListTag)tag;
            if (list.size() != 4) {
                throw new IllegalArgumentException("Expected list with 4 values for 'id' tag");
            }
            if (!list.getElementType().isAssignableFrom(NumberTag.class)) {
                throw new IllegalArgumentException("Expected list with number values for 'id' tag");
            }
        }
        return new UUID((long)value[0] << 32 | (long)value[1] & 0xFFFFFFFFL, (long)value[2] << 32 | (long)value[3] & 0xFFFFFFFFL);
    }
}

