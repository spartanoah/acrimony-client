/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.viaversion.viabackwards;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import com.viaversion.viabackwards.fabric.util.LoggerWrapper;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

public class ViaFabricAddon
implements ViaBackwardsPlatform,
Runnable {
    private final Logger logger = new LoggerWrapper(LogManager.getLogger("ViaBackwards"));
    private File configDir;

    @Override
    public void run() {
        Path configDirPath = FabricLoader.getInstance().getConfigDir().resolve("ViaBackwards");
        this.configDir = configDirPath.toFile();
        this.init(this.getDataFolder());
    }

    @Override
    public void disable() {
    }

    @Override
    public File getDataFolder() {
        return this.configDir;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}

