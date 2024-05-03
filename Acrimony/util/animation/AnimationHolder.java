/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation;

import Acrimony.util.animation.Animation;

public class AnimationHolder<T>
extends Animation {
    private T t;

    public AnimationHolder(T t) {
        this.t = t;
    }

    public T get() {
        return this.t;
    }
}

