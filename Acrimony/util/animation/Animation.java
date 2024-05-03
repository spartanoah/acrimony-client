/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation;

import Acrimony.util.animation.AnimationState;
import Acrimony.util.animation.AnimationType;
import Acrimony.util.misc.TimerUtil;
import Acrimony.util.misc.VoidFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class Animation {
    private boolean rendered;
    private AnimationType animType;
    private long animDuration = 250L;
    private AnimationState state = AnimationState.IN;
    private int overScaledDurationPercentage = 50;
    private final TimerUtil timer = new TimerUtil();
    private boolean animDone = true;
    private boolean lastEnabled;
    private float timeElapsed;

    public void updateState(boolean rendered) {
        this.rendered = rendered;
        if (this.timer.getTimeElapsed() >= this.animDuration) {
            this.animDone = true;
        }
        if (this.animDone) {
            this.timer.reset();
        }
        if (rendered && !this.lastEnabled) {
            this.state = AnimationState.IN;
            if (!this.animDone) {
                this.timer.setTimeElapsed(this.animDuration - this.timer.getTimeElapsed());
            }
            this.animDone = false;
        } else if (!rendered && this.lastEnabled) {
            this.state = AnimationState.OUT;
            if (!this.animDone) {
                this.timer.setTimeElapsed(this.animDuration - this.timer.getTimeElapsed());
            }
            this.animDone = false;
        }
        this.timeElapsed = Math.max(this.timer.getTimeElapsed(), 1L);
        this.lastEnabled = rendered;
    }

    public void render(VoidFunction renderInstructions, float startX, float startY, float endX, float endY) {
        GL11.glPushMatrix();
        if (!this.animDone) {
            if (this.state == AnimationState.IN) {
                this.animationIn(startX, startY, endX, endY, false);
            } else {
                this.animationOut(startX, startY, endX, endY, false);
            }
        }
        renderInstructions.execute();
        if (!this.animDone) {
            if (this.state == AnimationState.IN) {
                this.animationIn(startX, startY, endX, endY, true);
            } else {
                this.animationOut(startX, startY, endX, endY, true);
            }
        }
        GL11.glPopMatrix();
    }

    public float getYMult() {
        this.timeElapsed = Math.max(this.timer.getTimeElapsed(), 1L);
        if (!this.animDone) {
            if (this.animType == AnimationType.POP || this.animType == AnimationType.SLIDE) {
                if (this.rendered) {
                    return this.timeElapsed / (float)this.animDuration;
                }
                return 1.0f - this.timeElapsed / (float)this.animDuration;
            }
            if (this.animType == AnimationType.BOUNCE) {
                boolean firstPart;
                float overScaledDuration = (float)this.overScaledDurationPercentage / 100.0f;
                float normalDuration = 1.0f - overScaledDuration;
                float anim1 = (float)this.animDuration * normalDuration;
                float anim2 = (float)this.animDuration * overScaledDuration;
                if (this.rendered) {
                    boolean firstPart2;
                    boolean bl = firstPart2 = this.timeElapsed < anim1;
                    if (firstPart2) {
                        float thing = this.timeElapsed / anim1;
                        float thing2 = 1.8f - thing * 0.6f;
                        float mult = thing * thing2;
                        return mult;
                    }
                    float thing = (this.timeElapsed - anim1) / anim2;
                    float thing2 = 0.4f - thing * 0.2f;
                    float mult = thing * thing2;
                    return 1.2f - mult;
                }
                boolean bl = firstPart = this.timeElapsed < anim2;
                if (firstPart) {
                    float thing = this.timeElapsed / anim2;
                    float thing2 = 0.4f - thing * 0.2f;
                    float mult = thing * thing2;
                    return 1.0f + mult;
                }
                float thing = (this.timeElapsed - anim2) / anim1;
                float thing2 = 1.5f - thing * 0.3f;
                float mult = thing * thing2;
                return 1.2f - mult;
            }
        }
        return 1.0f;
    }

    private void animationIn(float startX, float startY, float endX, float endY, boolean post) {
        switch (this.animType) {
            case POP: 
            case BOUNCE: {
                if (post) {
                    this.stopScaling(this.getYMult(), startX, startY, endX, endY);
                    break;
                }
                this.startScaling(this.getYMult(), startX, startY, endX, endY);
                break;
            }
            case SLIDE: {
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                int screenWidth = sr.getScaledWidth() + 3;
                float x = (startX + endX) / 2.0f >= (float)(sr.getScaledWidth() / 2) ? ((float)screenWidth - startX) * (1.0f - this.getYMult()) : -endX * (1.0f - this.getYMult());
                GL11.glTranslatef(post ? -x : x, 0.0f, 0.0f);
            }
        }
    }

    private void animationOut(float startX, float startY, float endX, float endY, boolean post) {
        switch (this.animType) {
            case POP: 
            case BOUNCE: {
                if (post) {
                    this.stopScaling(this.getYMult(), startX, startY, endX, endY);
                    break;
                }
                this.startScaling(this.getYMult(), startX, startY, endX, endY);
                break;
            }
            case SLIDE: {
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                int screenWidth = sr.getScaledWidth() + 3;
                float x = (startX + endX) / 2.0f >= (float)(sr.getScaledWidth() / 2) ? ((float)screenWidth - startX) * (1.0f - this.getYMult()) : -endX * (1.0f - this.getYMult());
                GL11.glTranslatef(post ? -x : x, 0.0f, 0.0f);
            }
        }
    }

    private void startScaling(float mult, float startX, float startY, float endX, float endY) {
        float middleX = startX + (endX - startX) * 0.5f;
        float middleY = startY + (endY - startY) * 0.5f;
        float translateX = middleX - middleX * mult;
        float translateY = middleY - middleY * mult;
        GL11.glTranslatef(translateX, translateY, 1.0f);
        GL11.glScalef(mult, mult, 1.0f);
    }

    private void stopScaling(float mult, float startX, float startY, float endX, float endY) {
        float middleX = startX + (endX - startX) * 0.5f;
        float middleY = startY + (endY - startY) * 0.5f;
        float translateX = middleX - middleX * mult;
        float translateY = middleY - middleY * mult;
        GL11.glScalef(1.0f / mult, 1.0f / mult, 1.0f);
        GL11.glTranslatef(-translateX, -translateY, 1.0f);
    }

    public boolean isRendered() {
        return this.rendered;
    }

    public AnimationType getAnimType() {
        return this.animType;
    }

    public long getAnimDuration() {
        return this.animDuration;
    }

    public AnimationState getState() {
        return this.state;
    }

    public int getOverScaledDurationPercentage() {
        return this.overScaledDurationPercentage;
    }

    public TimerUtil getTimer() {
        return this.timer;
    }

    public boolean isAnimDone() {
        return this.animDone;
    }

    public boolean isLastEnabled() {
        return this.lastEnabled;
    }

    public float getTimeElapsed() {
        return this.timeElapsed;
    }

    public void setAnimType(AnimationType animType) {
        this.animType = animType;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }

    public void setOverScaledDurationPercentage(int overScaledDurationPercentage) {
        this.overScaledDurationPercentage = overScaledDurationPercentage;
    }
}

