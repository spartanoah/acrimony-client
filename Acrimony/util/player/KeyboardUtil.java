/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.player;

import Acrimony.util.IMinecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyboardUtil
implements IMinecraft {
    public static boolean isPressed(KeyBinding key) {
        return Keyboard.isKeyDown(key.getKeyCode());
    }

    public static void resetKeybinding(KeyBinding key) {
        key.pressed = KeyboardUtil.mc.currentScreen != null ? false : KeyboardUtil.isPressed(key);
    }

    public static void resetKeybindings(KeyBinding ... keys) {
        for (KeyBinding key : keys) {
            KeyboardUtil.resetKeybinding(key);
        }
    }
}

