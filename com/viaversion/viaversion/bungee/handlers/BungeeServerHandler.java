/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.connection.ProxiedPlayer
 *  net.md_5.bungee.api.connection.Server
 *  net.md_5.bungee.api.event.ServerConnectEvent
 *  net.md_5.bungee.api.event.ServerConnectedEvent
 *  net.md_5.bungee.api.event.ServerSwitchEvent
 *  net.md_5.bungee.api.plugin.Listener
 *  net.md_5.bungee.api.score.Team
 *  net.md_5.bungee.event.EventHandler
 *  net.md_5.bungee.protocol.packet.PluginMessage
 */
package com.viaversion.viaversion.bungee.handlers;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPipeline;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.bungee.storage.BungeeStorage;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class BungeeServerHandler
implements Listener {
    private static final Method getHandshake;
    private static final Method getRegisteredChannels;
    private static final Method getBrandMessage;
    private static final Method setProtocol;
    private static final Method getEntityMap;
    private static final Method setVersion;
    private static final Field entityRewrite;
    private static final Field channelWrapper;

    @EventHandler(priority=120)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UserConnection user = Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }
        if (!user.has(BungeeStorage.class)) {
            user.put(new BungeeStorage(event.getPlayer()));
        }
        int serverProtocolVersion = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(event.getTarget().getName());
        int clientProtocolVersion = user.getProtocolInfo().getProtocolVersion();
        List<ProtocolPathEntry> protocols = Via.getManager().getProtocolManager().getProtocolPath(clientProtocolVersion, serverProtocolVersion);
        try {
            Object handshake = getHandshake.invoke(event.getPlayer().getPendingConnection(), new Object[0]);
            setProtocol.invoke(handshake, protocols == null ? clientProtocolVersion : serverProtocolVersion);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority=-120)
    public void onServerConnected(ServerConnectedEvent event) {
        try {
            this.checkServerChange(event, Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority=-120)
    public void onServerSwitch(ServerSwitchEvent event) {
        int playerId;
        UserConnection userConnection = Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId());
        if (userConnection == null) {
            return;
        }
        try {
            playerId = Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(userConnection);
        } catch (Exception ignored) {
            return;
        }
        for (EntityTracker tracker : userConnection.getEntityTrackers()) {
            tracker.setClientEntityId(playerId);
        }
        for (StorableObject object : userConnection.getStoredObjects().values()) {
            if (!(object instanceof ClientEntityIdChangeListener)) continue;
            ((ClientEntityIdChangeListener)((Object)object)).setClientEntityId(playerId);
        }
    }

    public void checkServerChange(ServerConnectedEvent event, UserConnection user) throws Exception {
        boolean toOldId;
        if (user == null) {
            return;
        }
        BungeeStorage storage = user.get(BungeeStorage.class);
        if (storage == null) {
            return;
        }
        Server server = event.getServer();
        if (server == null || server.getInfo().getName().equals(storage.getCurrentServer())) {
            return;
        }
        EntityTracker1_9 oldEntityTracker = (EntityTracker1_9)user.getEntityTracker(Protocol1_9To1_8.class);
        if (oldEntityTracker != null && oldEntityTracker.isAutoTeam() && oldEntityTracker.isTeamExists()) {
            oldEntityTracker.sendTeamPacket(false, true);
        }
        String serverName = server.getInfo().getName();
        storage.setCurrentServer(serverName);
        int serverProtocolVersion = Via.proxyPlatform().protocolDetectorService().serverProtocolVersion(serverName);
        if (serverProtocolVersion <= ProtocolVersion.v1_8.getVersion() && storage.getBossbar() != null) {
            if (user.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
                for (UUID uuid : storage.getBossbar()) {
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, null, user);
                    wrapper.write(Type.UUID, uuid);
                    wrapper.write(Type.VAR_INT, 1);
                    wrapper.send(Protocol1_9To1_8.class);
                }
            }
            storage.getBossbar().clear();
        }
        ProtocolInfo info = user.getProtocolInfo();
        int previousServerProtocol = info.getServerProtocolVersion();
        List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(info.getProtocolVersion(), serverProtocolVersion);
        ProtocolPipeline pipeline = user.getProtocolInfo().getPipeline();
        user.clearStoredObjects(true);
        pipeline.cleanPipes();
        if (protocolPath == null) {
            serverProtocolVersion = info.getProtocolVersion();
        } else {
            ArrayList<Protocol> protocols = new ArrayList<Protocol>(protocolPath.size());
            for (ProtocolPathEntry entry : protocolPath) {
                protocols.add(entry.protocol());
            }
            pipeline.add(protocols);
        }
        info.setServerProtocolVersion(serverProtocolVersion);
        pipeline.add(Via.getManager().getProtocolManager().getBaseProtocol(serverProtocolVersion));
        int id1_13 = ProtocolVersion.v1_13.getVersion();
        boolean toNewId = previousServerProtocol < id1_13 && serverProtocolVersion >= id1_13;
        boolean bl = toOldId = previousServerProtocol >= id1_13 && serverProtocolVersion < id1_13;
        if (previousServerProtocol != -1 && (toNewId || toOldId)) {
            PluginMessage brandMessage;
            Collection registeredChannels = (Collection)getRegisteredChannels.invoke(event.getPlayer().getPendingConnection(), new Object[0]);
            if (!registeredChannels.isEmpty()) {
                HashSet<Object> newChannels = new HashSet<Object>();
                Iterator iterator = registeredChannels.iterator();
                while (iterator.hasNext()) {
                    Object channel = (String)iterator.next();
                    String oldChannel = channel;
                    channel = toNewId ? InventoryPackets.getNewPluginChannelId((String)channel) : InventoryPackets.getOldPluginChannelId((String)channel);
                    if (channel == null) {
                        iterator.remove();
                        continue;
                    }
                    if (oldChannel.equals(channel)) continue;
                    iterator.remove();
                    newChannels.add(channel);
                }
                registeredChannels.addAll(newChannels);
            }
            if ((brandMessage = (PluginMessage)getBrandMessage.invoke(event.getPlayer().getPendingConnection(), new Object[0])) != null) {
                String channel = brandMessage.getTag();
                channel = toNewId ? InventoryPackets.getNewPluginChannelId(channel) : InventoryPackets.getOldPluginChannelId(channel);
                if (channel != null) {
                    brandMessage.setTag(channel);
                }
            }
        }
        user.put(storage);
        user.setActive(protocolPath != null);
        for (Protocol protocol : pipeline.pipes()) {
            protocol.init(user);
        }
        ProxiedPlayer player = storage.getPlayer();
        EntityTracker1_9 newTracker = (EntityTracker1_9)user.getEntityTracker(Protocol1_9To1_8.class);
        if (newTracker != null && Via.getConfig().isAutoTeam()) {
            String currentTeam = null;
            for (Team team : player.getScoreboard().getTeams()) {
                if (!team.getPlayers().contains(info.getUsername())) continue;
                currentTeam = team.getName();
            }
            newTracker.setAutoTeam(true);
            if (currentTeam == null) {
                newTracker.sendTeamPacket(true, true);
                newTracker.setCurrentTeam("viaversion");
            } else {
                newTracker.setAutoTeam(Via.getConfig().isAutoTeam());
                newTracker.setCurrentTeam(currentTeam);
            }
        }
        Object wrapper = channelWrapper.get(player);
        setVersion.invoke(wrapper, serverProtocolVersion);
        Object entityMap = getEntityMap.invoke(null, serverProtocolVersion);
        entityRewrite.set(player, entityMap);
    }

    static {
        try {
            getHandshake = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getHandshake", new Class[0]);
            getRegisteredChannels = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getRegisteredChannels", new Class[0]);
            getBrandMessage = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredMethod("getBrandMessage", new Class[0]);
            setProtocol = Class.forName("net.md_5.bungee.protocol.packet.Handshake").getDeclaredMethod("setProtocolVersion", Integer.TYPE);
            getEntityMap = Class.forName("net.md_5.bungee.entitymap.EntityMap").getDeclaredMethod("getEntityMap", Integer.TYPE);
            setVersion = Class.forName("net.md_5.bungee.netty.ChannelWrapper").getDeclaredMethod("setVersion", Integer.TYPE);
            channelWrapper = Class.forName("net.md_5.bungee.UserConnection").getDeclaredField("ch");
            channelWrapper.setAccessible(true);
            entityRewrite = Class.forName("net.md_5.bungee.UserConnection").getDeclaredField("entityRewrite");
            entityRewrite.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            Via.getPlatform().getLogger().severe("Error initializing BungeeServerHandler, try updating BungeeCord or ViaVersion!");
            throw new RuntimeException(e);
        }
    }
}

