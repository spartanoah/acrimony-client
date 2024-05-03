/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Pack200Adapter {
    protected static final int DEFAULT_BUFFER_SIZE = 8192;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final SortedMap<String, String> properties = new TreeMap<String, String>();

    public SortedMap<String, String> properties() {
        return this.properties;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.support.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    protected void completed(double value) {
        this.firePropertyChange("pack.progress", null, String.valueOf((int)(100.0 * value)));
    }
}

