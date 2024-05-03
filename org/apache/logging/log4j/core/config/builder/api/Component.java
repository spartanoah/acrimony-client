/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.builder.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Component {
    private final Map<String, String> attributes = new LinkedHashMap<String, String>();
    private final List<Component> components = new ArrayList<Component>();
    private final String pluginType;
    private final String value;

    public Component(String pluginType) {
        this(pluginType, null, null);
    }

    public Component(String pluginType, String name) {
        this(pluginType, name, null);
    }

    public Component(String pluginType, String name, String value) {
        this.pluginType = pluginType;
        this.value = value;
        if (name != null && name.length() > 0) {
            this.attributes.put("name", name);
        }
    }

    public Component() {
        this.pluginType = null;
        this.value = null;
    }

    public String addAttribute(String key, String newValue) {
        return this.attributes.put(key, newValue);
    }

    public void addComponent(Component component) {
        this.components.add(component);
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public List<Component> getComponents() {
        return this.components;
    }

    public String getPluginType() {
        return this.pluginType;
    }

    public String getValue() {
        return this.value;
    }
}

