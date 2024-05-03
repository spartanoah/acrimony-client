/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.Provider;

public class Log4jProvider
extends Provider {
    public Log4jProvider() {
        super(10, "2.6.0", Log4jContextFactory.class);
    }
}

