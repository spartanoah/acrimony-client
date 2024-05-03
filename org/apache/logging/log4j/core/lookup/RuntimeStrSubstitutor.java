/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

public final class RuntimeStrSubstitutor
extends StrSubstitutor {
    public RuntimeStrSubstitutor() {
    }

    public RuntimeStrSubstitutor(Map<String, String> valueMap) {
        super(valueMap);
    }

    public RuntimeStrSubstitutor(Properties properties) {
        super(properties);
    }

    public RuntimeStrSubstitutor(StrLookup lookup) {
        super(lookup);
    }

    public RuntimeStrSubstitutor(StrSubstitutor other) {
        super(other);
    }

    @Override
    public String toString() {
        return "RuntimeStrSubstitutor{" + super.toString() + "}";
    }
}

