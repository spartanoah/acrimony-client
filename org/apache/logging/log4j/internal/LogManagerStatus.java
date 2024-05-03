/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.internal;

public class LogManagerStatus {
    private static boolean initialized = false;

    public static void setInitialized(boolean managerStatus) {
        initialized = managerStatus;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}

