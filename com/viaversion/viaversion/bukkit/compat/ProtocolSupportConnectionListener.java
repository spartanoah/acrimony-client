/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  protocolsupport.api.Connection$PacketListener
 *  protocolsupport.api.Connection$PacketListener$PacketEvent
 */
package com.viaversion.viaversion.bukkit.compat;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.compat.ProtocolSupportCompat;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import java.lang.reflect.Method;
import protocolsupport.api.Connection;

final class ProtocolSupportConnectionListener
extends Connection.PacketListener {
    static final Method ADD_PACKET_LISTENER_METHOD;
    private static final Class<?> HANDSHAKE_PACKET_CLASS;
    private static final Method GET_VERSION_METHOD;
    private static final Method SET_VERSION_METHOD;
    private static final Method REMOVE_PACKET_LISTENER_METHOD;
    private static final Method GET_LATEST_METHOD;
    private static final Object PROTOCOL_VERSION_MINECRAFT_FUTURE;
    private static final Object PROTOCOL_TYPE_PC;
    private final Object connection;

    ProtocolSupportConnectionListener(Object connection) {
        this.connection = connection;
    }

    public void onPacketReceiving(Connection.PacketListener.PacketEvent event) {
        try {
            if (HANDSHAKE_PACKET_CLASS.isInstance(event.getPacket()) && GET_VERSION_METHOD.invoke(this.connection, new Object[0]) == PROTOCOL_VERSION_MINECRAFT_FUTURE) {
                Object packet = event.getPacket();
                int protocolVersion = (Integer)HANDSHAKE_PACKET_CLASS.getDeclaredMethod(ProtocolSupportCompat.handshakeVersionMethod().methodName(), new Class[0]).invoke(packet, new Object[0]);
                if (protocolVersion == Via.getAPI().getServerVersion().lowestSupportedVersion()) {
                    SET_VERSION_METHOD.invoke(this.connection, GET_LATEST_METHOD.invoke(null, PROTOCOL_TYPE_PC));
                }
            }
            REMOVE_PACKET_LISTENER_METHOD.invoke(this.connection, new Object[]{this});
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            HANDSHAKE_PACKET_CLASS = NMSUtil.nms("PacketHandshakingInSetProtocol", "net.minecraft.network.protocol.handshake.PacketHandshakingInSetProtocol");
            Class<?> connectionImplClass = Class.forName("protocolsupport.protocol.ConnectionImpl");
            Class<?> connectionClass = Class.forName("protocolsupport.api.Connection");
            Class<?> packetListenerClass = Class.forName("protocolsupport.api.Connection$PacketListener");
            Class<?> protocolVersionClass = Class.forName("protocolsupport.api.ProtocolVersion");
            Class<?> protocolTypeClass = Class.forName("protocolsupport.api.ProtocolType");
            GET_VERSION_METHOD = connectionClass.getDeclaredMethod("getVersion", new Class[0]);
            SET_VERSION_METHOD = connectionImplClass.getDeclaredMethod("setVersion", protocolVersionClass);
            PROTOCOL_VERSION_MINECRAFT_FUTURE = protocolVersionClass.getDeclaredField("MINECRAFT_FUTURE").get(null);
            GET_LATEST_METHOD = protocolVersionClass.getDeclaredMethod("getLatest", protocolTypeClass);
            PROTOCOL_TYPE_PC = protocolTypeClass.getDeclaredField("PC").get(null);
            ADD_PACKET_LISTENER_METHOD = connectionClass.getDeclaredMethod("addPacketListener", packetListenerClass);
            REMOVE_PACKET_LISTENER_METHOD = connectionClass.getDeclaredMethod("removePacketListener", packetListenerClass);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

