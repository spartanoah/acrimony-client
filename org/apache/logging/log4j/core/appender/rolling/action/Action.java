/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.IOException;

public interface Action
extends Runnable {
    public boolean execute() throws IOException;

    public void close();

    public boolean isComplete();
}

