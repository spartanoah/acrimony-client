/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.CommandSender
 *  net.md_5.bungee.api.ProxyServer
 *  net.md_5.bungee.api.connection.ProxiedPlayer
 *  net.md_5.bungee.api.plugin.Command
 *  net.md_5.bungee.api.plugin.Listener
 *  net.md_5.bungee.api.plugin.Plugin
 *  net.md_5.bungee.protocol.ProtocolConstants
 */
package com.viaversion.viaversion;

import com.google.common.collect.ImmutableList;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaServerProxyPlatform;
import com.viaversion.viaversion.bungee.commands.BungeeCommand;
import com.viaversion.viaversion.bungee.commands.BungeeCommandHandler;
import com.viaversion.viaversion.bungee.commands.BungeeCommandSender;
import com.viaversion.viaversion.bungee.platform.BungeeViaAPI;
import com.viaversion.viaversion.bungee.platform.BungeeViaConfig;
import com.viaversion.viaversion.bungee.platform.BungeeViaInjector;
import com.viaversion.viaversion.bungee.platform.BungeeViaLoader;
import com.viaversion.viaversion.bungee.platform.BungeeViaTask;
import com.viaversion.viaversion.bungee.service.ProtocolDetectorService;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.unsupported.UnsupportedServerSoftware;
import com.viaversion.viaversion.util.GsonUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.ProtocolConstants;

public class BungeePlugin
extends Plugin
implements ViaServerProxyPlatform<ProxiedPlayer>,
Listener {
    private final ProtocolDetectorService protocolDetectorService = new ProtocolDetectorService();
    private BungeeViaAPI api;
    private BungeeViaConfig config;

    public void onLoad() {
        try {
            ProtocolConstants.class.getField("MINECRAFT_1_19_4");
        } catch (NoSuchFieldException e) {
            this.getLogger().warning("      / \\");
            this.getLogger().warning("     /   \\");
            this.getLogger().warning("    /  |  \\");
            this.getLogger().warning("   /   |   \\         BUNGEECORD IS OUTDATED");
            this.getLogger().warning("  /         \\   VIAVERSION MAY NOT WORK AS INTENDED");
            this.getLogger().warning(" /     o     \\");
            this.getLogger().warning("/_____________\\");
        }
        this.api = new BungeeViaAPI();
        this.config = new BungeeViaConfig(this.getDataFolder());
        BungeeCommandHandler commandHandler = new BungeeCommandHandler();
        ProxyServer.getInstance().getPluginManager().registerCommand((Plugin)this, (Command)new BungeeCommand(commandHandler));
        Via.init(ViaManagerImpl.builder().platform(this).injector(new BungeeViaInjector()).loader(new BungeeViaLoader(this)).commandHandler(commandHandler).build());
    }

    public void onEnable() {
        ViaManagerImpl manager = (ViaManagerImpl)Via.getManager();
        manager.init();
        manager.onServerLoaded();
    }

    @Override
    public String getPlatformName() {
        return this.getProxy().getName();
    }

    @Override
    public String getPlatformVersion() {
        return this.getProxy().getVersion();
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public String getPluginVersion() {
        return this.getDescription().getVersion();
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        return new BungeeViaTask(this.getProxy().getScheduler().runAsync((Plugin)this, runnable));
    }

    @Override
    public PlatformTask runRepeatingAsync(Runnable runnable, long ticks) {
        return new BungeeViaTask(this.getProxy().getScheduler().schedule((Plugin)this, runnable, 0L, ticks * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        return this.runAsync(runnable);
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long delay) {
        return new BungeeViaTask(this.getProxy().getScheduler().schedule((Plugin)this, runnable, delay * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long period) {
        return this.runRepeatingAsync(runnable, period);
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        Collection players = this.getProxy().getPlayers();
        ViaCommandSender[] array = new ViaCommandSender[players.size()];
        int i = 0;
        for (ProxiedPlayer player : players) {
            array[i++] = new BungeeCommandSender((CommandSender)player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        this.getProxy().getPlayer(uuid).sendMessage(message);
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        ProxiedPlayer player = this.getProxy().getPlayer(uuid);
        if (player != null) {
            player.disconnect(message);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ViaAPI<ProxiedPlayer> getApi() {
        return this.api;
    }

    @Override
    public BungeeViaConfig getConf() {
        return this.config;
    }

    @Override
    public void onReload() {
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();
        ArrayList<PluginInfo> plugins = new ArrayList<PluginInfo>();
        for (Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            plugins.add(new PluginInfo(true, p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), Collections.singletonList(p.getDescription().getAuthor())));
        }
        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));
        platformSpecific.add("servers", GsonUtil.getGson().toJsonTree(this.protocolDetectorService.detectedProtocolVersions()));
        return platformSpecific;
    }

    @Override
    public Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
        ArrayList<UnsupportedSoftware> list = new ArrayList<UnsupportedSoftware>(ViaServerProxyPlatform.super.getUnsupportedSoftwareClasses());
        list.add(new UnsupportedServerSoftware.Builder().name("FlameCord").addClassName("dev._2lstudios.flamecord.FlameCord").reason("You are using proxy software that intentionally breaks ViaVersion. Please use another proxy software or move ViaVersion to each backend server instead of the proxy.").build());
        return ImmutableList.copyOf(list);
    }

    @Override
    public boolean hasPlugin(String name) {
        return this.getProxy().getPluginManager().getPlugin(name) != null;
    }

    @Override
    public ProtocolDetectorService protocolDetectorService() {
        return this.protocolDetectorService;
    }
}

