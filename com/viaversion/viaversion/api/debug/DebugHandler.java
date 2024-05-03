/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.debug;

import com.google.common.annotations.Beta;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import java.util.logging.Level;

@Beta
public interface DebugHandler {
    public boolean enabled();

    public void setEnabled(boolean var1);

    public void addPacketTypeNameToLog(String var1);

    public void addPacketTypeToLog(PacketType var1);

    public boolean removePacketTypeNameToLog(String var1);

    public void clearPacketTypesToLog();

    public boolean logPostPacketTransform();

    public void setLogPostPacketTransform(boolean var1);

    public boolean shouldLog(PacketWrapper var1, Direction var2);

    default public void enableAndLogIds(PacketType ... packetTypes) {
        this.setEnabled(true);
        for (PacketType packetType : packetTypes) {
            this.addPacketTypeToLog(packetType);
        }
    }

    default public void error(String error, Throwable t) {
        if (!Via.getConfig().isSuppressConversionWarnings() || this.enabled()) {
            Via.getPlatform().getLogger().log(Level.SEVERE, error, t);
        }
    }
}

