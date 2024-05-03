/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LastTags
implements StorableObject {
    private final List<RegistryTags> registryTags = new ArrayList<RegistryTags>();

    public LastTags(PacketWrapper wrapper) throws Exception {
        int length = wrapper.passthrough(Type.VAR_INT);
        for (int i = 0; i < length; ++i) {
            ArrayList<Tag> tags = new ArrayList<Tag>();
            String registryKey = wrapper.passthrough(Type.STRING);
            int tagsSize = wrapper.passthrough(Type.VAR_INT);
            for (int j = 0; j < tagsSize; ++j) {
                String key = wrapper.passthrough(Type.STRING);
                int[] ids = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                tags.add(new Tag(key, ids));
            }
            this.registryTags.add(new RegistryTags(registryKey, tags));
        }
    }

    public void sendLastTags(UserConnection connection) throws Exception {
        if (this.registryTags.isEmpty()) {
            return;
        }
        PacketWrapper packet = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, connection);
        packet.write(Type.VAR_INT, this.registryTags.size());
        for (RegistryTags registryTag : this.registryTags) {
            packet.write(Type.STRING, registryTag.registryKey);
            packet.write(Type.VAR_INT, registryTag.tags.size());
            for (Tag tag : registryTag.tags) {
                packet.write(Type.STRING, tag.key);
                packet.write(Type.VAR_INT_ARRAY_PRIMITIVE, Arrays.copyOf(tag.ids, tag.ids.length));
            }
        }
        packet.send(Protocol1_20_2To1_20.class);
    }

    private static final class Tag {
        private final String key;
        private final int[] ids;

        private Tag(String key, int[] ids) {
            this.key = key;
            this.ids = ids;
        }
    }

    private static final class RegistryTags {
        private final String registryKey;
        private final List<Tag> tags;

        private RegistryTags(String registryKey, List<Tag> tags) {
            this.registryKey = registryKey;
            this.tags = tags;
        }
    }
}

