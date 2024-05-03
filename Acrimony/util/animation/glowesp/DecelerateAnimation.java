/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation.glowesp;

import Acrimony.util.animation.glowesp.DecelerateAnimationParent;

public class DecelerateAnimation
extends DecelerateAnimationParent {
    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    @Override
    protected double getEquation(double x) {
        double x1 = x / (double)this.duration;
        return 1.0 - (x1 - 1.0) * (x1 - 1.0);
    }
}

