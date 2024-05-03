/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import java.util.Queue;

public interface EventLoopTaskQueueFactory {
    public Queue<Runnable> newTaskQueue(int var1);
}

