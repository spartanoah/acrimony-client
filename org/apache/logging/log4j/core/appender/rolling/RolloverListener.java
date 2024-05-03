/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling;

public interface RolloverListener {
    public void rolloverTriggered(String var1);

    public void rolloverComplete(String var1);
}

