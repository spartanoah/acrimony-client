/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.MpscLinkedQueueNode;

public abstract class OneTimeTask
extends MpscLinkedQueueNode<Runnable>
implements Runnable {
    @Override
    public Runnable value() {
        return this;
    }
}

