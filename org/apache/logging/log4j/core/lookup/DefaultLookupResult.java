/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Objects;
import org.apache.logging.log4j.core.lookup.LookupResult;

final class DefaultLookupResult
implements LookupResult {
    private final String value;

    DefaultLookupResult(String value) {
        this.value = Objects.requireNonNull(value, "value is required");
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public boolean isLookupEvaluationAllowedInValue() {
        return false;
    }

    public String toString() {
        return "DefaultLookupResult{value='" + this.value + "'}";
    }
}

