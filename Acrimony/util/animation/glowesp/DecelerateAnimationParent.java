/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation.glowesp;

import Acrimony.util.animation.glowesp.Direction;
import Acrimony.util.misc.TimerUtil;

public abstract class DecelerateAnimationParent {
    public TimerUtil timerUtil = new TimerUtil();
    protected int duration;
    protected double endPoint;
    protected Direction direction;

    public DecelerateAnimationParent(int ms, double endPoint) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = Direction.FORWARDS;
    }

    public DecelerateAnimationParent(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public boolean finished(Direction direction) {
        return this.isDone() && this.direction.equals((Object)direction);
    }

    public double getLinearOutput() {
        return 1.0 - (double)this.timerUtil.getTimeElapsed() / (double)this.duration * this.endPoint;
    }

    public double getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }

    public void reset() {
        this.timerUtil.reset();
    }

    public boolean isDone() {
        return this.timerUtil.hasTimeElapsed(this.duration);
    }

    public void changeDirection() {
        this.setDirection(this.direction.opposite());
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTimeElapsed(System.currentTimeMillis() - ((long)this.duration - Math.min((long)this.duration, this.timerUtil.getTimeElapsed())));
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (this.direction == Direction.FORWARDS) {
            if (this.isDone()) {
                return this.endPoint;
            }
            return this.getEquation(this.timerUtil.getTimeElapsed()) * this.endPoint;
        }
        if (this.isDone()) {
            return 0.0;
        }
        if (this.correctOutput()) {
            double revTime = Math.min((long)this.duration, Math.max(0L, (long)this.duration - this.timerUtil.getTimeElapsed()));
            return this.getEquation(revTime) * this.endPoint;
        }
        return (1.0 - this.getEquation(this.timerUtil.getTimeElapsed())) * this.endPoint;
    }

    protected abstract double getEquation(double var1);
}

