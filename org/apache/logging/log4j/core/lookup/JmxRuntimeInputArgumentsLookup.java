/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.MapLookup;

@Plugin(name="jvmrunargs", category="Lookup")
public class JmxRuntimeInputArgumentsLookup
extends MapLookup {
    public static final JmxRuntimeInputArgumentsLookup JMX_SINGLETON;

    public JmxRuntimeInputArgumentsLookup() {
    }

    public JmxRuntimeInputArgumentsLookup(Map<String, String> map) {
        super(map);
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return this.lookup(key);
    }

    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }
        Map<String, String> map = this.getMap();
        return map == null ? null : map.get(key);
    }

    static {
        List<String> argsList = ManagementFactory.getRuntimeMXBean().getInputArguments();
        JMX_SINGLETON = new JmxRuntimeInputArgumentsLookup(MapLookup.toMap(argsList));
    }
}

