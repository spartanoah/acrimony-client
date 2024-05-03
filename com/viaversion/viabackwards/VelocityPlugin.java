/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.velocitypowered.api.event.PostOrder
 *  com.velocitypowered.api.event.Subscribe
 *  com.velocitypowered.api.event.proxy.ProxyInitializeEvent
 *  com.velocitypowered.api.plugin.Dependency
 *  com.velocitypowered.api.plugin.Plugin
 *  com.velocitypowered.api.plugin.annotation.DataDirectory
 */
package com.viaversion.viabackwards;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.velocity.util.LoggerWrapper;
import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;

@Plugin(id="viabackwards", name="ViaBackwards", version="4.7.0-1.20-pre1-SNAPSHOT", authors={"Matsv", "kennytv", "Gerrygames", "creeper123123321", "ForceUpdate1"}, description="Allow older Minecraft versions to connect to a newer server version.", dependencies={@Dependency(id="viaversion")})
public class VelocityPlugin
implements ViaBackwardsPlatform {
    private java.util.logging.Logger logger;
    @Inject
    private Logger loggerSlf4j;
    @Inject
    @DataDirectory
    private Path configPath;

    @Subscribe(order=PostOrder.LATE)
    public void onProxyStart(ProxyInitializeEvent event) {
        this.logger = new LoggerWrapper(this.loggerSlf4j);
        Via.getManager().addEnableListener(() -> this.init(this.getDataFolder()));
    }

    @Override
    public void disable() {
    }

    @Override
    public File getDataFolder() {
        return this.configPath.toFile();
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return this.logger;
    }
}

