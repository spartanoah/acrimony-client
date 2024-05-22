/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command.impl;

import Acrimony.Acrimony;
import Acrimony.autoconfig.DownloadConfig;
import Acrimony.command.Command;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
import Acrimony.util.misc.LogUtil;
import java.util.List;

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
                        Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "Config announcement", "Loaded config " + configName + " in game.", 3000L));
                        break;
                    }
                    Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "Config announcement", "Config not found in FileSystem, Please make sure you type the correct config name in Chat.", 3000L));
                    break;
                }
                case "save": {
                    Acrimony.instance.getFileSystem().saveConfig(configName);
                    Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "Config announcement", "Saved config as " + configName + " in game.", 3000L));
                    break;
                }
                case "install": {
                    DownloadConfig.downloadConfig(configName, DownloadConfig.repoOwner, DownloadConfig.repoName, DownloadConfig.downloadPath, DownloadConfig.token);
                    System.out.println("testsss");
                }
            }
        }
        if (args[2].toLowerCase().equals("onlinelist")) {
            List<String> fileList = DownloadConfig.getOnlineConfigList(DownloadConfig.repoOwner, DownloadConfig.repoName, DownloadConfig.token);
            System.out.println(fileList);
            if (fileList != null) {
                for (String file : fileList) {
                    LogUtil.addChatMessage("Acrimony: " + file);
                }
            } else {
                Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "Config announcement", "Error Fetching List of Online Configs", 3000L));
            }
        }
    }
}

