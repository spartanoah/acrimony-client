/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.core.config.plugins.util.PluginBuilder;

public interface Builder<T> {
    public T build();

    default public boolean isValid() {
        return PluginBuilder.validateFields(this, this.getErrorPrefix());
    }

    default public String getErrorPrefix() {
        return "Component";
    }
}

