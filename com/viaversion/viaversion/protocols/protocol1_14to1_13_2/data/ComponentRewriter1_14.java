/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;

public class ComponentRewriter1_14<C extends ClientboundPacketType>
extends ComponentRewriter1_13<C> {
    public ComponentRewriter1_14(Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
    }
}

