/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.AbstractFenceConnectionHandler;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.ConnectionData;
import java.util.ArrayList;
import java.util.List;

public class GlassConnectionHandler
extends AbstractFenceConnectionHandler {
    static List<ConnectionData.ConnectorInitAction> init() {
        ArrayList<ConnectionData.ConnectorInitAction> actions = new ArrayList<ConnectionData.ConnectorInitAction>(18);
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:white_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:orange_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:magenta_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:light_blue_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:yellow_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:lime_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:pink_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:gray_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:light_gray_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:cyan_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:purple_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:blue_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:brown_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:green_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:red_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:black_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:glass_pane"));
        actions.add(new GlassConnectionHandler("pane").getInitAction("minecraft:iron_bars"));
        return actions;
    }

    public GlassConnectionHandler(String blockConnections) {
        super(blockConnections);
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        int states = super.getStates(user, position, blockState);
        if (states != 0) {
            return (byte)states;
        }
        ProtocolInfo protocolInfo = user.getProtocolInfo();
        return (byte)(protocolInfo.getServerProtocolVersion() <= 47 && protocolInfo.getServerProtocolVersion() != -1 ? 15 : states);
    }
}

