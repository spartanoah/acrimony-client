/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.notification;

import Acrimony.Acrimony;
import Acrimony.event.Listener;
import Acrimony.event.impl.RenderEvent;
import Acrimony.font.AcrimonyFont;
import Acrimony.module.impl.visual.ClientTheme;
import Acrimony.ui.notification.Notification;
import Acrimony.ui.notification.NotificationType;
import Acrimony.util.IMinecraft;
import Acrimony.util.animation.AnimationHolder;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.render.DrawUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationHandler
implements IMinecraft {
    private final ArrayList<AnimationHolder<Notification>> notifications = new ArrayList();
    private ClientTheme theme;

    public NotificationHandler() {
        Acrimony.instance.getEventManager().register(this);
        this.theme = Acrimony.instance.getModuleManager().getModule(ClientTheme.class);
    }

    @Listener
    public void onRender(RenderEvent event) {
        if (!this.notifications.isEmpty()) {
            ArrayList<AnimationHolder<Notification>> toRemove = new ArrayList<AnimationHolder<Notification>>();
            int notifAmount = 0;
            for (AnimationHolder<Notification> notif : this.notifications) {
                if (!notif.isAnimDone() && notif.isRendered()) {
                    notif.setAnimType(AnimationType.POP);
                    notif.setAnimDuration(180L);
                } else {
                    notif.setAnimType(AnimationType.POP);
                    notif.setAnimDuration(150L);
                }
                notif.updateState(!notif.get().shouldBeRemoved());
                if (!notif.isRendered() && notif.isAnimDone()) {
                    toRemove.add(notif);
                }
                ++notifAmount;
            }
            if (!toRemove.isEmpty()) {
                for (AnimationHolder<Notification> notif : toRemove) {
                    this.notifications.remove(notif);
                    --notifAmount;
                }
                toRemove.clear();
            }
            if (notifAmount > 0) {
                int start;
                AcrimonyFont sfpro = Acrimony.instance.getFontManager().getSfpro();
                AcrimonyFont sfprobold = Acrimony.instance.getFontManager().getSfprobold();
                AcrimonyFont icon = Acrimony.instance.getFontManager().getIcon();
                ScaledResolution sr = new ScaledResolution(mc);
                int height = 28;
                int spacing = 4;
                int y = start = sr.getScaledHeight() - 2 - height * notifAmount - spacing * (notifAmount - 1);
                for (AnimationHolder<Notification> notif : this.notifications) {
                    int textWidth = sfpro.getStringWidth(notif.get().getText());
                    int length = Math.max(textWidth, 8);
                    int startX = sr.getScaledWidth() - length;
                    int startY = y;
                    int endX = sr.getScaledWidth() - 2;
                    int endY = y + height;
                    notif.render(() -> {
                        int color = ((Notification)notif.get()).getType() == NotificationType.INFO ? new Color(33, 115, 33, 255).getRGB() : (((Notification)notif.get()).getType() == NotificationType.WARNING ? new Color(121, 121, 26, 255).getRGB() : (((Notification)notif.get()).getType() == NotificationType.ERROR ? new Color(134, 28, 28, 255).getRGB() : 0x65000000));
                        DrawUtil.drawRoundedRect(startX - 38, startY, endX, endY, 0.0, -2063597568);
                        DrawUtil.drawRoundedRect(startX - 38, startY, startX - 10, endY, 0.0, color);
                        DrawUtil.drawGradientSideways(startX - 38, startY, endX, startY + 2, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        DrawUtil.drawGradientSideways(startX - 38, endY - 2, endX, endY, ClientTheme.color1.getRGB(), ClientTheme.color2.getRGB());
                        Gui.drawRect(startX - 38, startY, startX - 36, endY, ClientTheme.color1.getRGB());
                        Gui.drawRect(endX - 2, startY, endX, endY, ClientTheme.color2.getRGB());
                        if (((Notification)notif.get()).getType() == NotificationType.INFO) {
                            icon.drawStringWithShadow("o", (double)startX - 31.5, (double)(startY + 8), -1);
                        } else if (((Notification)notif.get()).getType() == NotificationType.WARNING) {
                            icon.drawStringWithShadow("r", (double)startX - 31.5, (double)(startY + 8), -1);
                        } else if (((Notification)notif.get()).getType() == NotificationType.ERROR) {
                            icon.drawStringWithShadow("p", startX - 30, startY + 7, -1);
                        }
                        sfprobold.drawStringWithShadow(((Notification)notif.get()).getTitle(), startX - 6, startY + 5, -1);
                        sfpro.drawStringWithShadow(((Notification)notif.get()).getText(), startX - 6, startY + 16, -1);
                    }, startX, startY, endX, endY);
                    y += height + spacing;
                }
            }
        }
    }

    public void postNotification(Notification notif) {
        this.notifications.add(new AnimationHolder<Notification>(notif));
    }
}

