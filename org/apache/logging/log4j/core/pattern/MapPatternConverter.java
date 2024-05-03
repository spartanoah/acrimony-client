/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Objects;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.message.MapMessage;

@Plugin(name="MapPatternConverter", category="Converter")
@ConverterKeys(value={"K", "map", "MAP"})
public final class MapPatternConverter
extends LogEventPatternConverter {
    private static final String JAVA_UNQUOTED = MapMessage.MapFormat.JAVA_UNQUOTED.name();
    private final String key;
    private final String[] format;

    private MapPatternConverter(String[] options, String ... format) {
        super(options != null && options.length > 0 ? "MAP{" + options[0] + '}' : "MAP", "map");
        this.key = options != null && options.length > 0 ? options[0] : null;
        this.format = format;
    }

    public static MapPatternConverter newInstance(String[] options) {
        return new MapPatternConverter(options, JAVA_UNQUOTED);
    }

    public static MapPatternConverter newInstance(String[] options, MapMessage.MapFormat format) {
        return new MapPatternConverter(options, Objects.toString((Object)format, JAVA_UNQUOTED));
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        if (!(event.getMessage() instanceof MapMessage)) {
            return;
        }
        MapMessage msg = (MapMessage)event.getMessage();
        if (this.key == null) {
            msg.formatTo(this.format, toAppendTo);
        } else {
            String val2 = msg.get(this.key);
            if (val2 != null) {
                toAppendTo.append(val2);
            }
        }
    }
}

