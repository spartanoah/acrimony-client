/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Store
extends HashMap<String, Map<String, Set<String>>> {
    public Store() {
    }

    public Store(Map<String, Map<String, Set<String>>> storeMap) {
        super(storeMap);
    }
}

