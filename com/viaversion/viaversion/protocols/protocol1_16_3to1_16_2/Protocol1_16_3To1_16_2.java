/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16_3to1_16_2;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;

public class Protocol1_16_3To1_16_2
extends AbstractProtocol<ClientboundPackets1_16_2, ClientboundPackets1_16_2, ServerboundPackets1_16_2, ServerboundPackets1_16_2> {
    public Protocol1_16_3To1_16_2() {
        super(ClientboundPackets1_16_2.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16_2.class, ServerboundPackets1_16_2.class);
    }
}

