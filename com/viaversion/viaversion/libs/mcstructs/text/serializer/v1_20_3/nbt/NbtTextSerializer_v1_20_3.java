/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.nbt;

import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbtSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.KeybindComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.NbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.ScoreComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.SelectorComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.BlockNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.EntityNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.nbt.StorageNbtComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.ITypedSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_3.nbt.NbtStyleSerializer_v1_20_3;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.DoubleTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.FloatTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ShortTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.ArrayList;
import java.util.List;

public class NbtTextSerializer_v1_20_3
implements ITypedSerializer<Tag, ATextComponent> {
    private final ITypedSerializer<Tag, Style> styleSerializer;

    public NbtTextSerializer_v1_20_3(TextComponentCodec codec, SNbtSerializer<CompoundTag> sNbtSerializer) {
        this.styleSerializer = new NbtStyleSerializer_v1_20_3(codec, this, sNbtSerializer);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Tag serialize(ATextComponent object) {
        ATextComponent component;
        CompoundTag out = new CompoundTag();
        if (object instanceof StringComponent) {
            component = (StringComponent)object;
            if (component.getSiblings().isEmpty() && component.getStyle().isEmpty()) {
                return new StringTag(((StringComponent)component).getText());
            }
            out.putString("text", ((StringComponent)component).getText());
        } else if (object instanceof TranslationComponent) {
            component = (TranslationComponent)object;
            out.putString("translate", ((TranslationComponent)component).getKey());
            if (((TranslationComponent)component).getFallback() != null) {
                out.putString("fallback", ((TranslationComponent)component).getFallback());
            }
            if (((TranslationComponent)component).getArgs().length > 0) {
                ArrayList<Tag> args = new ArrayList<Tag>();
                for (Object arg : ((TranslationComponent)component).getArgs()) {
                    args.add(this.convert(arg));
                }
                out.put("with", this.optimizeAndConvert(args));
            }
        } else if (object instanceof KeybindComponent) {
            component = (KeybindComponent)object;
            out.putString("keybind", ((KeybindComponent)component).getKeybind());
        } else if (object instanceof ScoreComponent) {
            component = (ScoreComponent)object;
            CompoundTag score = new CompoundTag();
            score.putString("name", ((ScoreComponent)component).getName());
            score.putString("objective", ((ScoreComponent)component).getObjective());
            out.put("score", score);
        } else if (object instanceof SelectorComponent) {
            component = (SelectorComponent)object;
            out.putString("selector", ((SelectorComponent)component).getSelector());
            if (((SelectorComponent)component).getSeparator() != null) {
                out.put("separator", this.serialize(((SelectorComponent)component).getSeparator()));
            }
        } else {
            if (!(object instanceof NbtComponent)) throw new IllegalArgumentException("Unknown component type: " + object.getClass().getName());
            component = (NbtComponent)object;
            out.putString("nbt", ((NbtComponent)component).getComponent());
            if (((NbtComponent)component).isResolve()) {
                out.putByte("interpret", (byte)1);
            }
            if (component instanceof EntityNbtComponent) {
                EntityNbtComponent entityComponent = (EntityNbtComponent)component;
                out.putString("entity", entityComponent.getSelector());
            } else if (component instanceof BlockNbtComponent) {
                BlockNbtComponent blockNbtComponent = (BlockNbtComponent)component;
                out.putString("block", blockNbtComponent.getPos());
            } else {
                if (!(component instanceof StorageNbtComponent)) throw new IllegalArgumentException("Unknown Nbt component type: " + component.getClass().getName());
                StorageNbtComponent storageNbtComponent = (StorageNbtComponent)component;
                out.putString("storage", storageNbtComponent.getId().get());
            }
        }
        CompoundTag style = (CompoundTag)this.styleSerializer.serialize(object.getStyle());
        if (!style.isEmpty()) {
            out.putAll(style);
        }
        if (object.getSiblings().isEmpty()) return out;
        ArrayList<Tag> siblings = new ArrayList<Tag>();
        for (ATextComponent sibling : object.getSiblings()) {
            siblings.add(this.serialize(sibling));
        }
        out.put("extra", this.optimizeAndConvert(siblings));
        return out;
    }

    private Tag convert(Object object) {
        if (object instanceof Boolean) {
            return new ByteTag((byte)((Boolean)object != false ? (char)'\u0001' : '\u0000'));
        }
        if (object instanceof Byte) {
            return new ByteTag((Byte)object);
        }
        if (object instanceof Short) {
            return new ShortTag((Short)object);
        }
        if (object instanceof Integer) {
            return new IntTag((Integer)object);
        }
        if (object instanceof Long) {
            return new LongTag((Long)object);
        }
        if (object instanceof Float) {
            return new FloatTag(((Float)object).floatValue());
        }
        if (object instanceof Double) {
            return new DoubleTag((Double)object);
        }
        if (object instanceof String) {
            return new StringTag((String)object);
        }
        if (object instanceof ATextComponent) {
            return this.serialize((ATextComponent)object);
        }
        throw new IllegalArgumentException("Unknown object type: " + object.getClass().getName());
    }

    private Tag optimizeAndConvert(List<Tag> tags) {
        Tag commonType = this.getCommonType(tags);
        if (commonType == null) {
            ListTag out = new ListTag();
            for (Tag tag : tags) {
                if (tag instanceof CompoundTag) {
                    out.add((CompoundTag)tag);
                    continue;
                }
                CompoundTag marker = new CompoundTag();
                marker.put("", tag);
                out.add(marker);
            }
            return out;
        }
        if (commonType instanceof ByteTag) {
            byte[] bytes = new byte[tags.size()];
            for (int i = 0; i < tags.size(); ++i) {
                bytes[i] = ((ByteTag)tags.get(i)).getValue();
            }
            return new ByteArrayTag(bytes);
        }
        if (commonType instanceof IntTag) {
            int[] ints = new int[tags.size()];
            for (int i = 0; i < tags.size(); ++i) {
                ints[i] = ((IntTag)tags.get(i)).getValue();
            }
            return new IntArrayTag(ints);
        }
        if (commonType instanceof LongTag) {
            long[] longs = new long[tags.size()];
            for (int i = 0; i < tags.size(); ++i) {
                longs[i] = ((LongTag)tags.get(i)).getValue();
            }
            return new LongArrayTag(longs);
        }
        ListTag out = new ListTag();
        for (Tag tag : tags) {
            out.add(tag);
        }
        return out;
    }

    private Tag getCommonType(List<Tag> tags) {
        if (tags.size() == 1) {
            return tags.get(0);
        }
        Tag type = tags.get(0);
        for (int i = 1; i < tags.size(); ++i) {
            if (type.getClass() == tags.get(i).getClass()) continue;
            return null;
        }
        return type;
    }

    @Override
    public ATextComponent deserialize(Tag object) {
        int i;
        String type;
        if (object instanceof StringTag) {
            return new StringComponent(((StringTag)object).getValue());
        }
        if (object instanceof ListTag) {
            if (((ListTag)object).isEmpty()) {
                throw new IllegalArgumentException("Empty list tag");
            }
            ListTag listTag = (ListTag)object;
            ATextComponent[] components = new ATextComponent[listTag.size()];
            for (int i2 = 0; i2 < listTag.size(); ++i2) {
                components[i2] = this.deserialize((Tag)listTag.get(i2));
            }
            if (components.length == 1) {
                return components[0];
            }
            ATextComponent parent = components[0];
            for (int i3 = 1; i3 < components.length; ++i3) {
                parent.append(components[i3]);
            }
            return parent;
        }
        if (!(object instanceof CompoundTag)) {
            throw new IllegalArgumentException("Unknown component type: " + object.getClass());
        }
        ATextComponent component = null;
        CompoundTag tag = (CompoundTag)object;
        String string = type = tag.get("type") instanceof StringTag ? ((StringTag)tag.get("type")).getValue() : null;
        if (tag.get("text") instanceof StringTag && (type == null || type.equals("text"))) {
            component = new StringComponent(tag.get("text") instanceof StringTag ? ((StringTag)tag.get("text")).getValue() : "");
        } else if (tag.get("translate") instanceof StringTag && (type == null || type.equals("translatable"))) {
            String fallback;
            String key = tag.get("translate") instanceof StringTag ? ((StringTag)tag.get("translate")).getValue() : "";
            String string2 = fallback = tag.get("fallback") instanceof StringTag ? ((StringTag)tag.get("fallback")).getValue() : null;
            if (tag.contains("with")) {
                List<Tag> with = this.unwrapMarkers(this.getArrayOrList(tag, "with"));
                Object[] args = new Object[with.size()];
                for (i = 0; i < with.size(); ++i) {
                    Tag arg = with.get(i);
                    args[i] = arg instanceof NumberTag ? ((NumberTag)arg).getValue() : (arg instanceof StringTag ? ((StringTag)arg).getValue() : this.deserialize(arg));
                }
                component = new TranslationComponent(key, args).setFallback(fallback);
            } else {
                component = new TranslationComponent(key, new Object[0]).setFallback(fallback);
            }
        } else if (tag.get("keybind") instanceof StringTag && (type == null || type.equals("keybind"))) {
            component = new KeybindComponent(tag.get("keybind") instanceof StringTag ? ((StringTag)tag.get("keybind")).getValue() : "");
        } else if (tag.get("score") instanceof CompoundTag && (tag.get("score") instanceof CompoundTag ? (CompoundTag)tag.get("score") : new CompoundTag()).get("name") instanceof StringTag && (tag.get("score") instanceof CompoundTag ? (CompoundTag)tag.get("score") : new CompoundTag()).get("objective") instanceof StringTag && (type == null || type.equals("score"))) {
            CompoundTag score = tag.get("score") instanceof CompoundTag ? (CompoundTag)tag.get("score") : new CompoundTag();
            String name = score.get("name") instanceof StringTag ? ((StringTag)score.get("name")).getValue() : "";
            String objective = score.get("objective") instanceof StringTag ? ((StringTag)score.get("objective")).getValue() : "";
            component = new ScoreComponent(name, objective);
        } else if (tag.get("selector") instanceof StringTag && (type == null || type.equals("selector"))) {
            String selector = tag.get("selector") instanceof StringTag ? ((StringTag)tag.get("selector")).getValue() : "";
            ATextComponent separator = null;
            if (tag.contains("separator")) {
                separator = this.deserialize((Tag)tag.get("separator"));
            }
            component = new SelectorComponent(selector, separator);
        } else if (tag.get("nbt") instanceof StringTag && (type == null || type.equals("nbt"))) {
            String nbt = tag.get("nbt") instanceof StringTag ? ((StringTag)tag.get("nbt")).getValue() : "";
            boolean interpret = tag.get("interpret") instanceof ByteTag ? ((ByteTag)tag.get("interpret")).asBoolean() : false;
            ATextComponent separator = null;
            if (tag.contains("separator")) {
                try {
                    separator = this.deserialize((Tag)tag.get("separator"));
                } catch (Throwable args) {
                    // empty catch block
                }
            }
            String source = tag.get("source") instanceof StringTag ? ((StringTag)tag.get("source")).getValue() : null;
            boolean typeFound = false;
            if (tag.get("entity") instanceof StringTag && (source == null || source.equals("entity"))) {
                component = new EntityNbtComponent(nbt, interpret, separator, tag.get("entity") instanceof StringTag ? ((StringTag)tag.get("entity")).getValue() : "");
                typeFound = true;
            } else if (tag.get("block") instanceof StringTag && (source == null || source.equals("block"))) {
                component = new BlockNbtComponent(nbt, interpret, separator, tag.get("block") instanceof StringTag ? ((StringTag)tag.get("block")).getValue() : "");
                typeFound = true;
            } else if (tag.get("storage") instanceof StringTag && (source == null || source.equals("storage"))) {
                try {
                    component = new StorageNbtComponent(nbt, interpret, separator, Identifier.of(tag.get("storage") instanceof StringTag ? ((StringTag)tag.get("storage")).getValue() : ""));
                    typeFound = true;
                } catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (!typeFound) {
                throw new IllegalArgumentException("Unknown Nbt component type: " + tag.getClass());
            }
        } else {
            throw new IllegalArgumentException("Unknown component type: " + tag.getClass());
        }
        Style style = this.styleSerializer.deserialize(tag);
        if (!style.isEmpty()) {
            component.setStyle(style);
        }
        if (tag.contains("extra")) {
            ListTag extraTag;
            if (!(tag.get("extra") instanceof ListTag)) {
                throw new IllegalArgumentException("Expected list tag for 'extra' tag");
            }
            ListTag listTag = extraTag = tag.get("extra") instanceof ListTag ? (ListTag)tag.get("extra") : new ListTag();
            if (extraTag.isEmpty()) {
                throw new IllegalArgumentException("Empty extra list tag");
            }
            List<Tag> unwrapped = this.unwrapMarkers(extraTag);
            ATextComponent[] extra = new ATextComponent[unwrapped.size()];
            for (i = 0; i < unwrapped.size(); ++i) {
                extra[i] = this.deserialize(unwrapped.get(i));
            }
            component.append(extra);
        }
        return component;
    }

    private ListTag getArrayOrList(CompoundTag tag, String key) {
        if (tag.get(key) instanceof ListTag) {
            return tag.get(key) instanceof ListTag ? (ListTag)tag.get(key) : new ListTag();
        }
        if (tag.get(key) instanceof ByteArrayTag) {
            return ((ByteArrayTag)tag.get(key)).toListTag();
        }
        if (tag.get(key) instanceof IntArrayTag) {
            return ((IntArrayTag)tag.get(key)).toListTag();
        }
        if (tag.get(key) instanceof LongArrayTag) {
            return ((LongArrayTag)tag.get(key)).toListTag();
        }
        throw new IllegalArgumentException("Expected array or list tag for '" + key + "' tag");
    }

    private List<Tag> unwrapMarkers(ListTag list) {
        ArrayList<Tag> out = new ArrayList<Tag>();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag) {
                CompoundTag compound = (CompoundTag)tag;
                if (compound.size() == 1 && compound.contains("")) {
                    out.add((Tag)compound.get(""));
                    continue;
                }
                out.add(tag);
                continue;
            }
            out.add(tag);
        }
        return out;
    }
}

