/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.base;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.BaseProtocol1_7;
import java.util.UUID;

public class BaseProtocol1_16
extends BaseProtocol1_7 {
    @Override
    protected UUID passthroughLoginUUID(PacketWrapper wrapper) throws Exception {
        return wrapper.passthrough(Type.UUID);
    }
}

