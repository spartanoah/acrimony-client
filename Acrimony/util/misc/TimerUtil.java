/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.misc;

import net.minecraft.client.Minecraft;

public class TimerUtil {
    private long lastTime;
    public long lastMS = System.currentTimeMillis();

    public TimerUtil() {
        this.reset();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - this.lastMS > time) {
            if (reset) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - this.lastMS > time;
    }

    public boolean hasTimeElapsed(double time) {
        return this.hasTimeElapsed((long)time);
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public void setTime(long time) {
        this.lastMS = time;
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - this.lastTime;
    }

    public void setTimeElapsed(long time) {
        this.lastTime = System.currentTimeMillis() - time;
    }

    public void reset() {
        this.lastTime = System.currentTimeMillis();
        this.lastMS = System.currentTimeMillis();
    }

    public static double deltaTime() {
        double d;
        Minecraft.getMinecraft();
        if (Minecraft.getDebugFPS() > 0) {
            Minecraft.getMinecraft();
            d = 1.6 / (double)Minecraft.getDebugFPS();
        } else {
            d = 10.0;
        }
        return d;
    }

    public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 0.0f) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0.0f) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        return current + movement;
    }
}

