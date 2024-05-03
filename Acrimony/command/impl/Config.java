/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command.impl;

import Acrimony.Acrimony;
import Acrimony.command.Command;
import Acrimony.util.misc.LogUtil;

public class Config
extends Command {
    public Config() {
        super("Config", "Loads or saves a config.");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length >= 3) {
            String action = args[1].toLowerCase();
            String configName = args[2];
            switch (action) {
                case "load": {
                    boolean success = Acrimony.instance.getFileSystem().loadConfig(configName, false);
                    if (success) {
                        LogUtil.addChatMessage("Loaded config " + configName);
                        break;
                    }
                    LogUtil.addChatMessage("Config not found.");
                    break;
                }
                case "save": {
                    Acrimony.instance.getFileSystem().saveConfig(configName);
                    LogUtil.addChatMessage("Saved config " + configName);
                }
            }
        }
    }
}

