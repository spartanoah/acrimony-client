/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.misc;

import Acrimony.Acrimony;
import Acrimony.util.IMinecraft;
import java.util.Objects;
import net.minecraft.util.ChatComponentText;

public class LogUtil
implements IMinecraft {
    private static final String prefix;

    public static void print(Object message) {
        System.out.println(prefix + " " + message);
    }

    public static void addChatMessage(String message) {
        LogUtil.mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }

    static {
        StringBuilder stringBuilder = new StringBuilder().append("[");
        Objects.requireNonNull(Acrimony.instance);
        prefix = stringBuilder.append("Acrimony").append("]").toString();
    }
}

