/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command.impl;

import Acrimony.autoconfig.DownloadConfig;
import Acrimony.command.Command;
import Acrimony.util.misc.LogUtil;

public class ConfigList
extends Command {
    public ConfigList() {
        super("ConfigList", "ConfigList.", "list");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length >= 2) {
            String action;
            switch (action = args[1].toLowerCase()) {
                case "onlinelist": {
                    LogUtil.addChatMessage(String.valueOf("List of online configs: " + DownloadConfig.getOnlineConfigList(DownloadConfig.repoOwner, DownloadConfig.repoName, DownloadConfig.token)));
                }
            }
        }
    }
}

