/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net.ssl;

public class StoreConfigurationException
extends Exception {
    private static final long serialVersionUID = 1L;

    public StoreConfigurationException(Exception e) {
        super(e);
    }

    public StoreConfigurationException(String message) {
        super(message);
    }

    public StoreConfigurationException(String message, Exception e) {
        super(message, e);
    }
}

