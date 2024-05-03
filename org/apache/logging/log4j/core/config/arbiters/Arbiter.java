/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.arbiters;

public interface Arbiter {
    public static final String ELEMENT_TYPE = "Arbiter";

    public boolean isCondition();
}

