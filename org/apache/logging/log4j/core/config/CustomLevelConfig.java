/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config;

import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="CustomLevel", category="Core", printObject=true)
public final class CustomLevelConfig {
    static final CustomLevelConfig[] EMPTY_ARRAY = new CustomLevelConfig[0];
    private final String levelName;
    private final int intLevel;

    private CustomLevelConfig(String levelName, int intLevel) {
        this.levelName = Objects.requireNonNull(levelName, "levelName is null");
        this.intLevel = intLevel;
    }

    @PluginFactory
    public static CustomLevelConfig createLevel(@PluginAttribute(value="name") String levelName, @PluginAttribute(value="intLevel") int intLevel) {
        StatusLogger.getLogger().debug("Creating CustomLevel(name='{}', intValue={})", (Object)levelName, (Object)intLevel);
        Level.forName(levelName, intLevel);
        return new CustomLevelConfig(levelName, intLevel);
    }

    public String getLevelName() {
        return this.levelName;
    }

    public int getIntLevel() {
        return this.intLevel;
    }

    public int hashCode() {
        return this.intLevel ^ this.levelName.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CustomLevelConfig)) {
            return false;
        }
        CustomLevelConfig other = (CustomLevelConfig)object;
        return this.intLevel == other.intLevel && this.levelName.equals(other.levelName);
    }

    public String toString() {
        return "CustomLevel[name=" + this.levelName + ", intLevel=" + this.intLevel + "]";
    }
}

