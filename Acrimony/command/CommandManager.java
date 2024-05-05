/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.command;

import Acrimony.Acrimony;
import Acrimony.command.Command;
import Acrimony.command.impl.Bind;
import Acrimony.command.impl.Config;
import Acrimony.command.impl.Toggle;
import Acrimony.event.Listener;
import Acrimony.event.impl.ChatSendEvent;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
import java.util.ArrayList;

public class CommandManager {
    public final ArrayList<Command> commands = new ArrayList();

    public CommandManager() {
        Acrimony.instance.getEventManager().register(this);
        this.commands.add(new Toggle());
        this.commands.add(new Bind());
        this.commands.add(new Config());
    }

    @Listener
    public void onChatSend(ChatSendEvent event) {
        String message = event.getMessage();
        if (message.startsWith(".")) {
            Object command;
            event.setCancelled(true);
            String commandName = "";
            for (int i = 0; i < message.length(); ++i) {
                if (i <= 0) continue;
                char c = message.charAt(i);
                if (c == ' ') break;
                commandName = commandName + c;
            }
            if ((command = this.getCommandByName(commandName)) != null) {
                String commandWithoutDot = message.substring(1);
                String[] commandParts = commandWithoutDot.split(" ");
                ((Command)command).onCommand(commandParts);
            } else {
                Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.ERROR, "Command", "Command not found.", 3000L));
            }
        }
    }

    public <T extends Command> T getCommandByName(String name) {
        for (Command command : this.commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return (T)command;
            }
            if (command.getAliases() == null || command.getAliases().length <= 0) continue;
            for (String alias : command.getAliases()) {
                if (!alias.equalsIgnoreCase(name)) continue;
                return (T)command;
            }
        }
        return null;
    }
}

