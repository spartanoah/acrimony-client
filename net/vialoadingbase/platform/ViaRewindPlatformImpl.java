/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.vialoadingbase.platform;

import com.viaversion.viarewind.api.ViaRewindPlatform;
import java.io.File;
import java.util.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSword;
import net.vialoadingbase.ViaLoadingBase;

public class ViaRewindPlatformImpl
implements ViaRewindPlatform {
    public ViaRewindPlatformImpl(File directory) {
        this.init(new File(directory, "viarewind.yml"));
    }

    @Override
    public Logger getLogger() {
        return ViaLoadingBase.LOGGER;
    }

    @Override
    public boolean isSword() {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
            return false;
        }
        return Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem() != null && Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }
}

