/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.misc;

import Acrimony.util.IMinecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyboardUtil {
    public static boolean isPressed(KeyBinding key) {
        return Keyboard.isKeyDown(key.getKeyCode()) && IMinecraft.mc.currentScreen == null;
    }

    public static void resetKeybinding(KeyBinding key) {
        key.pressed = IMinecraft.mc.currentScreen != null ? false : KeyboardUtil.isPressed(key);
    }

    public static void resetKeybindings(KeyBinding ... keys) {
        for (KeyBinding key : keys) {
            KeyboardUtil.resetKeybinding(key);
        }
    }
}

