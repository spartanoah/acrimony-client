/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.network;

import Acrimony.util.IMinecraft;

public class ServerUtil
implements IMinecraft {
    public static boolean isOnHypixel() {
        if (mc.getCurrentServerData() == null || ServerUtil.mc.thePlayer == null) {
            return false;
        }
        return ServerUtil.getCurrentServer().endsWith("hypixel.net");
    }

    public static String getCurrentServer() {
        if (mc.isSingleplayer()) {
            return "Singleplayer";
        }
        return ServerUtil.mc.getCurrentServerData().serverIP.toLowerCase();
    }
}

