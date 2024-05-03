/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command.impl;

import Acrimony.Acrimony;
import Acrimony.command.Command;
import Acrimony.module.Module;
import Acrimony.util.misc.LogUtil;
import org.lwjgl.input.Keyboard;

public class Bind
extends Command {
    public Bind() {
        super("Bind", "Changes the keybind of the specified module.");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length >= 3) {
            Object module = Acrimony.instance.getModuleManager().getModuleByNameNoSpace(args[1]);
            if (module != null) {
                String keyName = args[2].toUpperCase();
                ((Module)module).setKey(Keyboard.getKeyIndex(keyName));
                LogUtil.addChatMessage("Bound " + ((Module)module).getName() + " to " + keyName);
            }
        } else {
            LogUtil.addChatMessage("Usage : .bind module keybind");
        }
    }
}

