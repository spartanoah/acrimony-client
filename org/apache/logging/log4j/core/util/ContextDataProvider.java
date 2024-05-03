/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.util.Map;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.util.StringMap;

public interface ContextDataProvider {
    public Map<String, String> supplyContextData();

    default public StringMap supplyStringMap() {
        return new JdkMapAdapterStringMap(this.supplyContextData());
    }
}

