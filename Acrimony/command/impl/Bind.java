/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command.impl;

import Acrimony.Acrimony;
import Acrimony.command.Command;
import Acrimony.module.Module;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
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
                Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "KeyBind Manager", "Bound module : " + ((Module)module).getName() + " to " + keyName, 3000L));
            }
        } else {
            Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.WARNING, "KeyBind Manager", "Usage : .bind module keybind", 3000L));
        }
    }
}

