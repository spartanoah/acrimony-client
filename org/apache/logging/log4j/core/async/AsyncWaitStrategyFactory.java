/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.WaitStrategy
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.WaitStrategy;

public interface AsyncWaitStrategyFactory {
    public WaitStrategy createWaitStrategy();
}

