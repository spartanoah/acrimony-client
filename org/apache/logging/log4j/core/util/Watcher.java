/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import java.util.List;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.util.Source;

public interface Watcher {
    public static final String CATEGORY = "Watcher";
    public static final String ELEMENT_TYPE = "watcher";

    public List<ConfigurationListener> getListeners();

    public void modified();

    public boolean isModified();

    public long getLastModified();

    public void watching(Source var1);

    public Source getSource();

    public Watcher newWatcher(Reconfigurable var1, List<ConfigurationListener> var2, long var3);
}

