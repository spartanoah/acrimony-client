/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.spi;

import java.util.List;
import org.apache.logging.log4j.spi.LoggerContextShutdownAware;

public interface LoggerContextShutdownEnabled {
    public void addShutdownListener(LoggerContextShutdownAware var1);

    public List<LoggerContextShutdownAware> getListeners();
}

