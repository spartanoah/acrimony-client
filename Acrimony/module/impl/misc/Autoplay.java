/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.module.impl.misc;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.PacketReceiveEvent;
import Acrimony.event.impl.TickEvent;
import Acrimony.module.Category;
import Acrimony.module.Module;
import Acrimony.setting.impl.IntegerSetting;
import Acrimony.setting.impl.ModeSetting;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
import Acrimony.util.misc.TimerUtil;
import net.minecraft.network.play.server.S02PacketChat;

public class Autoplay
extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Solo insane", "Solo normal", "Solo insane");
    private final IntegerSetting delay = new IntegerSetting("Delay", 1500, 0, 4000, 50);
    private final String winMessage = "You won! Want to play again? Click here!";
    private final String loseMessage = "You died! Want to play again? Click here!";
    private final TimerUtil timer = new TimerUtil();
    private boolean waiting;

    public Autoplay() {
        super("Autoplay", Category.MISC);
        this.addSettings(this.mode, this.delay);
    }

    @Override
    public void onEnable() {
        this.waiting = false;
        this.timer.reset();
    }

    @Listener
    public void onTick(TickEvent event) {
        if (this.waiting && this.timer.getTimeElapsed() >= (long)this.delay.getValue()) {
            String command = "";
            switch (this.mode.getMode()) {
                case "Solo normal": {
                    command = "/play solo_normal";
                    break;
                }
                case "Solo insane": {
                    command = "/play solo_insane";
                }
            }
            Autoplay.mc.thePlayer.sendChatMessage(command);
            this.timer.reset();
            this.waiting = false;
        }
    }

    @Listener
    public void onReceive(PacketReceiveEvent event) {
        S02PacketChat packet;
        String message;
        if (event.getPacket() instanceof S02PacketChat && ((message = (packet = (S02PacketChat)event.getPacket()).getChatComponent().getUnformattedText()).contains("You won! Want to play again? Click here!") && message.length() < "You won! Want to play again? Click here!".length() + 3 || message.contains("You died! Want to play again? Click here!") && message.length() < "You died! Want to play again? Click here!".length() + 3)) {
            this.waiting = true;
            Acrimony.instance.getNotificationHandler().postNotification(new Notification(NotificationType.INFO, "Autoplay", "Sending you to a new game ", 2500L));
            this.timer.reset();
        }
    }
}

