/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_15_2to1_15_1;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;

public class Protocol1_15_2To1_15_1
extends AbstractProtocol<ClientboundPackets1_15, ClientboundPackets1_15, ServerboundPackets1_14, ServerboundPackets1_14> {
    public Protocol1_15_2To1_15_1() {
        super(ClientboundPackets1_15.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }
}

