/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@Plugin(name="encode", category="Converter")
@ConverterKeys(value={"enc", "encode"})
@PerformanceSensitive(value={"allocation"})
public final class EncodingPatternConverter
extends LogEventPatternConverter {
    private final List<PatternFormatter> formatters;
    private final EscapeFormat escapeFormat;

    private EncodingPatternConverter(List<PatternFormatter> formatters, EscapeFormat escapeFormat) {
        super("encode", "encode");
        this.formatters = formatters;
        this.escapeFormat = escapeFormat;
    }

    @Override
    public boolean handlesThrowable() {
        return this.formatters != null && this.formatters.stream().map(PatternFormatter::getConverter).anyMatch(LogEventPatternConverter::handlesThrowable);
    }

    public static EncodingPatternConverter newInstance(Configuration config, String[] options) {
        if (options.length > 2 || options.length == 0) {
            LOGGER.error("Incorrect number of options on escape. Expected 1 or 2, but received {}", (Object)options.length);
            return null;
        }
        if (options[0] == null) {
            LOGGER.error("No pattern supplied on escape");
            return null;
        }
        EscapeFormat escapeFormat = options.length < 2 ? EscapeFormat.HTML : EnglishEnums.valueOf(EscapeFormat.class, options[1], EscapeFormat.HTML);
        PatternParser parser = PatternLayout.createPatternParser(config);
        List<PatternFormatter> formatters = parser.parse(options[0]);
        return new EncodingPatternConverter(formatters, escapeFormat);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        int start = toAppendTo.length();
        for (int i = 0; i < this.formatters.size(); ++i) {
            this.formatters.get(i).format(event, toAppendTo);
        }
        this.escapeFormat.escape(toAppendTo, start);
    }

    private static enum EscapeFormat {
        HTML{

            @Override
            void escape(StringBuilder toAppendTo, int start) {
                int i;
                int origLength;
                int firstSpecialChar = origLength = toAppendTo.length();
                for (i = origLength - 1; i >= start; --i) {
                    char c = toAppendTo.charAt(i);
                    String escaped = this.escapeChar(c);
                    if (escaped == null) continue;
                    firstSpecialChar = i;
                    for (int j = 0; j < escaped.length() - 1; ++j) {
                        toAppendTo.append(' ');
                    }
                }
                int j = toAppendTo.length();
                for (i = origLength - 1; i >= firstSpecialChar; --i) {
                    char c = toAppendTo.charAt(i);
                    String escaped = this.escapeChar(c);
                    if (escaped == null) {
                        toAppendTo.setCharAt(--j, c);
                        continue;
                    }
                    toAppendTo.replace(j - escaped.length(), j, escaped);
                    j -= escaped.length();
                }
            }

            private String escapeChar(char c) {
                switch (c) {
                    case '\r': {
                        return "\\r";
                    }
                    case '\n': {
                        return "\\n";
                    }
                    case '&': {
                        return "&amp;";
                    }
                    case '<': {
                        return "&lt;";
                    }
                    case '>': {
                        return "&gt;";
                    }
                    case '\"': {
                        return "&quot;";
                    }
                    case '\'': {
                        return "&apos;";
                    }
                    case '/': {
                        return "&#x2F;";
                    }
                }
                return null;
            }
        }
        ,
        JSON{

            @Override
            void escape(StringBuilder toAppendTo, int start) {
                StringBuilders.escapeJson(toAppendTo, start);
            }
        }
        ,
        CRLF{

            @Override
            void escape(StringBuilder toAppendTo, int start) {
                int i;
                int origLength;
                int firstSpecialChar = origLength = toAppendTo.length();
                for (i = origLength - 1; i >= start; --i) {
                    char c = toAppendTo.charAt(i);
                    if (c != '\r' && c != '\n') continue;
                    firstSpecialChar = i;
                    toAppendTo.append(' ');
                }
                int j = toAppendTo.length();
                block5: for (i = origLength - 1; i >= firstSpecialChar; --i) {
                    char c = toAppendTo.charAt(i);
                    switch (c) {
                        case '\r': {
                            toAppendTo.setCharAt(--j, 'r');
                            toAppendTo.setCharAt(--j, '\\');
                            continue block5;
                        }
                        case '\n': {
                            toAppendTo.setCharAt(--j, 'n');
                            toAppendTo.setCharAt(--j, '\\');
                            continue block5;
                        }
                        default: {
                            toAppendTo.setCharAt(--j, c);
                        }
                    }
                }
            }
        }
        ,
        XML{

            @Override
            void escape(StringBuilder toAppendTo, int start) {
                StringBuilders.escapeXml(toAppendTo, start);
            }
        };


        abstract void escape(StringBuilder var1, int var2);
    }
}

