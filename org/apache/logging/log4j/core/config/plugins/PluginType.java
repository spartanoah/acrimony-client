/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.plugins;

import java.io.Serializable;

public class PluginType<T>
implements Serializable {
    private static final long serialVersionUID = 4743255148794846612L;
    private final Class<T> pluginClass;
    private final String elementName;
    private final boolean printObject;
    private final boolean deferChildren;

    public PluginType(Class<T> clazz, String name, boolean printObj, boolean deferChildren) {
        this.pluginClass = clazz;
        this.elementName = name;
        this.printObject = printObj;
        this.deferChildren = deferChildren;
    }

    public Class<T> getPluginClass() {
        return this.pluginClass;
    }

    public String getElementName() {
        return this.elementName;
    }

    public boolean isObjectPrintable() {
        return this.printObject;
    }

    public boolean isDeferChildren() {
        return this.deferChildren;
    }
}

