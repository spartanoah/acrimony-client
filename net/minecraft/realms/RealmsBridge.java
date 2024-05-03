/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.realms;

import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsBridge
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private GuiScreen previousScreen;

    public void switchToRealms(GuiScreen p_switchToRealms_1_) {
        this.previousScreen = p_switchToRealms_1_;
        try {
            Class<?> oclass = Class.forName("com.mojang.realmsclient.RealmsMainScreen");
            Constructor<?> constructor = oclass.getDeclaredConstructor(RealmsScreen.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(this);
            Minecraft.getMinecraft().displayGuiScreen(((RealmsScreen)object).getProxy());
        } catch (Exception exception) {
            LOGGER.error("Realms module missing", (Throwable)exception);
        }
    }

    public GuiScreenRealmsProxy getNotificationScreen(GuiScreen p_getNotificationScreen_1_) {
        try {
            this.previousScreen = p_getNotificationScreen_1_;
            Class<?> oclass = Class.forName("com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen");
            Constructor<?> constructor = oclass.getDeclaredConstructor(RealmsScreen.class);
            constructor.setAccessible(true);
            Object object = constructor.newInstance(this);
            return ((RealmsScreen)object).getProxy();
        } catch (Exception exception) {
            LOGGER.error("Realms module missing", (Throwable)exception);
            return null;
        }
    }

    @Override
    public void init() {
        Minecraft.getMinecraft().displayGuiScreen(this.previousScreen);
    }
}

