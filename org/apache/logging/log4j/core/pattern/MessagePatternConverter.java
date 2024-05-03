/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import java.util.ArrayList;
import java.util.Locale;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.HtmlTextRenderer;
import org.apache.logging.log4j.core.pattern.JAnsiTextRenderer;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.MultiFormatStringBuilderFormattable;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="MessagePatternConverter", category="Converter")
@ConverterKeys(value={"m", "msg", "message"})
@PerformanceSensitive(value={"allocation"})
public class MessagePatternConverter
extends LogEventPatternConverter {
    private static final String LOOKUPS = "lookups";
    private static final String NOLOOKUPS = "nolookups";

    private MessagePatternConverter() {
        super("Message", "message");
    }

    private static TextRenderer loadMessageRenderer(String[] options) {
        if (options != null) {
            for (String option : options) {
                switch (option.toUpperCase(Locale.ROOT)) {
                    case "ANSI": {
                        if (Loader.isJansiAvailable()) {
                            return new JAnsiTextRenderer(options, JAnsiTextRenderer.DefaultMessageStyleMap);
                        }
                        StatusLogger.getLogger().warn("You requested ANSI message rendering but JANSI is not on the classpath.");
                        return null;
                    }
                    case "HTML": {
                        return new HtmlTextRenderer(options);
                    }
                }
            }
        }
        return null;
    }

    public static MessagePatternConverter newInstance(Configuration config, String[] options) {
        MessagePatternConverter result;
        String[] formats = MessagePatternConverter.withoutLookupOptions(options);
        TextRenderer textRenderer = MessagePatternConverter.loadMessageRenderer(formats);
        MessagePatternConverter messagePatternConverter = result = formats == null || formats.length == 0 ? SimpleMessagePatternConverter.INSTANCE : new FormattedMessagePatternConverter(formats);
        if (textRenderer != null) {
            result = new RenderingPatternConverter(result, textRenderer);
        }
        return result;
    }

    private static String[] withoutLookupOptions(String[] options) {
        if (options == null || options.length == 0) {
            return options;
        }
        ArrayList<String> results = new ArrayList<String>(options.length);
        for (String option : options) {
            if (LOOKUPS.equalsIgnoreCase(option) || NOLOOKUPS.equalsIgnoreCase(option)) {
                LOGGER.info("The {} option will be ignored. Message Lookups are no longer supported.", (Object)option);
                continue;
            }
            results.add(option);
        }
        return results.toArray(Strings.EMPTY_ARRAY);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        throw new UnsupportedOperationException();
    }

    private static final class RenderingPatternConverter
    extends MessagePatternConverter {
        private final MessagePatternConverter delegate;
        private final TextRenderer textRenderer;

        RenderingPatternConverter(MessagePatternConverter delegate, TextRenderer textRenderer) {
            this.delegate = delegate;
            this.textRenderer = textRenderer;
        }

        @Override
        public void format(LogEvent event, StringBuilder toAppendTo) {
            StringBuilder workingBuilder = new StringBuilder(80);
            this.delegate.format(event, workingBuilder);
            this.textRenderer.render(workingBuilder, toAppendTo);
        }
    }

    private static final class FormattedMessagePatternConverter
    extends MessagePatternConverter {
        private final String[] formats;

        FormattedMessagePatternConverter(String[] formats) {
            this.formats = formats;
        }

        @Override
        public void format(LogEvent event, StringBuilder toAppendTo) {
            Message msg = event.getMessage();
            if (msg instanceof StringBuilderFormattable) {
                if (msg instanceof MultiFormatStringBuilderFormattable) {
                    ((MultiFormatStringBuilderFormattable)msg).formatTo(this.formats, toAppendTo);
                } else {
                    ((StringBuilderFormattable)((Object)msg)).formatTo(toAppendTo);
                }
            } else if (msg != null) {
                toAppendTo.append(msg instanceof MultiformatMessage ? ((MultiformatMessage)msg).getFormattedMessage(this.formats) : msg.getFormattedMessage());
            }
        }
    }

    private static final class SimpleMessagePatternConverter
    extends MessagePatternConverter {
        private static final MessagePatternConverter INSTANCE = new SimpleMessagePatternConverter();

        private SimpleMessagePatternConverter() {
        }

        @Override
        public void format(LogEvent event, StringBuilder toAppendTo) {
            Message msg = event.getMessage();
            if (msg instanceof StringBuilderFormattable) {
                ((StringBuilderFormattable)((Object)msg)).formatTo(toAppendTo);
            } else if (msg != null) {
                toAppendTo.append(msg.getFormattedMessage());
            }
        }
    }
}

