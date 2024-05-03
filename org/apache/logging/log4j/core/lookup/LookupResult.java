/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

public interface LookupResult {
    public String value();

    default public boolean isLookupEvaluationAllowedInValue() {
        return false;
    }
}

