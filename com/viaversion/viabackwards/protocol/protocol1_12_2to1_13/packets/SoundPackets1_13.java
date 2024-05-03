/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_12_2to1_13.packets;

import com.viaversion.viabackwards.protocol.protocol1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viabackwards.protocol.protocol1_12_2to1_13.data.NamedSoundMapping;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;

public class SoundPackets1_13
extends RewriterBase<Protocol1_12_2To1_13> {
    private static final String[] SOUND_SOURCES = new String[]{"master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"};

    public SoundPackets1_13(Protocol1_12_2To1_13 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        ((Protocol1_12_2To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.NAMED_SOUND, wrapper -> {
            String sound = wrapper.read(Type.STRING);
            String mappedSound = NamedSoundMapping.getOldId(sound);
            if (mappedSound != null || (mappedSound = ((Protocol1_12_2To1_13)this.protocol).getMappingData().getMappedNamedSound(sound)) != null) {
                wrapper.write(Type.STRING, mappedSound);
            } else {
                wrapper.write(Type.STRING, sound);
            }
        });
        ((Protocol1_12_2To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.STOP_SOUND, ClientboundPackets1_12_1.PLUGIN_MESSAGE, wrapper -> {
            String sound;
            wrapper.write(Type.STRING, "MC|StopSound");
            byte flags = wrapper.read(Type.BYTE);
            String source = (flags & 1) != 0 ? SOUND_SOURCES[wrapper.read(Type.VAR_INT)] : "";
            if ((flags & 2) != 0) {
                String newSound = wrapper.read(Type.STRING);
                sound = ((Protocol1_12_2To1_13)this.protocol).getMappingData().getMappedNamedSound(newSound);
                if (sound == null) {
                    sound = "";
                }
            } else {
                sound = "";
            }
            wrapper.write(Type.STRING, source);
            wrapper.write(Type.STRING, sound);
        });
        ((Protocol1_12_2To1_13)this.protocol).registerClientbound(ClientboundPackets1_13.SOUND, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.handler(wrapper -> {
                    int newSound = wrapper.get(Type.VAR_INT, 0);
                    int oldSound = ((Protocol1_12_2To1_13)SoundPackets1_13.this.protocol).getMappingData().getSoundMappings().getNewId(newSound);
                    if (oldSound == -1) {
                        wrapper.cancel();
                    } else {
                        wrapper.set(Type.VAR_INT, 0, oldSound);
                    }
                });
            }
        });
    }
}

