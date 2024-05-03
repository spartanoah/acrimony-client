/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.notification;

import Acrimony.ui.notification.NotificationType;
import Acrimony.util.misc.TimerUtil;

public class Notification {
    private final NotificationType type;
    private final String title;
    private final String text;
    private final long duration;
    private final TimerUtil timer;

    public Notification(NotificationType type, String title, String text, long duration) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.duration = duration;
        this.timer = new TimerUtil();
    }

    public boolean shouldBeRemoved() {
        return this.timer.getTimeElapsed() > this.duration;
    }

    public NotificationType getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public String getText() {
        return this.text;
    }

    public long getDuration() {
        return this.duration;
    }
}

