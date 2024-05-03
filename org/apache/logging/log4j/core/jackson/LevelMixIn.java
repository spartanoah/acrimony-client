/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.logging.log4j.Level;

@JsonIgnoreProperties(value={"name", "declaringClass", "standardLevel"})
abstract class LevelMixIn {
    LevelMixIn() {
    }

    @JsonCreator(mode=JsonCreator.Mode.DELEGATING)
    public static Level getLevel(String name) {
        return null;
    }

    @JsonValue
    public abstract String name();
}

