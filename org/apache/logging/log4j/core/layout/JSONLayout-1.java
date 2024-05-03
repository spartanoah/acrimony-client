/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.helpers.Throwables;
import org.apache.logging.log4j.core.helpers.Transform;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;

@Plugin(name="JSONLayout", category="Core", elementType="layout", printObject=true)
public class JSONLayout
extends AbstractStringLayout {
    private static final int DEFAULT_SIZE = 256;
    private static final String DEFAULT_EOL = "\r\n";
    private static final String COMPACT_EOL = "";
    private static final String DEFAULT_INDENT = "  ";
    private static final String COMPACT_INDENT = "";
    private static final String[] FORMATS = new String[]{"json"};
    private final boolean locationInfo;
    private final boolean properties;
    private final boolean complete;
    private final String eol;
    private final String indent1;
    private final String indent2;
    private final String indent3;
    private final String indent4;
    private volatile boolean firstLayoutDone;

    protected JSONLayout(boolean locationInfo, boolean properties, boolean complete, boolean compact, Charset charset) {
        super(charset);
        this.locationInfo = locationInfo;
        this.properties = properties;
        this.complete = complete;
        this.eol = compact ? "" : DEFAULT_EOL;
        this.indent1 = compact ? "" : DEFAULT_INDENT;
        this.indent2 = this.indent1 + this.indent1;
        this.indent3 = this.indent2 + this.indent1;
        this.indent4 = this.indent3 + this.indent1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String toSerializable(LogEvent event) {
        Throwable throwable;
        StringBuilder buf = new StringBuilder(256);
        boolean check = this.firstLayoutDone;
        if (!this.firstLayoutDone) {
            JSONLayout jSONLayout = this;
            synchronized (jSONLayout) {
                check = this.firstLayoutDone;
                if (!check) {
                    this.firstLayoutDone = true;
                } else {
                    buf.append(',');
                    buf.append(this.eol);
                }
            }
        } else {
            buf.append(',');
            buf.append(this.eol);
        }
        buf.append(this.indent1);
        buf.append('{');
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"logger\":\"");
        String name = event.getLoggerName();
        if (name.isEmpty()) {
            name = "root";
        }
        buf.append(Transform.escapeJsonControlCharacters(name));
        buf.append("\",");
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"timestamp\":\"");
        buf.append(event.getMillis());
        buf.append("\",");
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"level\":\"");
        buf.append(Transform.escapeJsonControlCharacters(String.valueOf(event.getLevel())));
        buf.append("\",");
        buf.append(this.eol);
        buf.append(this.indent2);
        buf.append("\"thread\":\"");
        buf.append(Transform.escapeJsonControlCharacters(event.getThreadName()));
        buf.append("\",");
        buf.append(this.eol);
        Message msg = event.getMessage();
        if (msg != null) {
            boolean jsonSupported = false;
            if (msg instanceof MultiformatMessage) {
                String[] formats;
                for (String format : formats = ((MultiformatMessage)msg).getFormats()) {
                    if (!format.equalsIgnoreCase("JSON")) continue;
                    jsonSupported = true;
                    break;
                }
            }
            buf.append(this.indent2);
            buf.append("\"message\":\"");
            if (jsonSupported) {
                buf.append(((MultiformatMessage)msg).getFormattedMessage(FORMATS));
            } else {
                Transform.appendEscapingCDATA(buf, event.getMessage().getFormattedMessage());
            }
            buf.append('\"');
        }
        if (event.getContextStack().getDepth() > 0) {
            buf.append(",");
            buf.append(this.eol);
            buf.append("\"ndc\":");
            Transform.appendEscapingCDATA(buf, event.getContextStack().toString());
            buf.append("\"");
        }
        if ((throwable = event.getThrown()) != null) {
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"throwable\":\"");
            List<String> list = Throwables.toStringList(throwable);
            for (String str : list) {
                buf.append(Transform.escapeJsonControlCharacters(str));
                buf.append("\\\\n");
            }
            buf.append("\"");
        }
        if (this.locationInfo) {
            StackTraceElement element = event.getSource();
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"LocationInfo\":{");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"class\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getClassName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"method\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getMethodName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"file\":\"");
            buf.append(Transform.escapeJsonControlCharacters(element.getFileName()));
            buf.append("\",");
            buf.append(this.eol);
            buf.append(this.indent3);
            buf.append("\"line\":\"");
            buf.append(element.getLineNumber());
            buf.append("\"");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("}");
        }
        if (this.properties && event.getContextMap().size() > 0) {
            buf.append(",");
            buf.append(this.eol);
            buf.append(this.indent2);
            buf.append("\"Properties\":[");
            buf.append(this.eol);
            Set<Map.Entry<String, String>> entrySet = event.getContextMap().entrySet();
            int i = 1;
            for (Map.Entry<String, String> entry : entrySet) {
                buf.append(this.indent3);
                buf.append('{');
                buf.append(this.eol);
                buf.append(this.indent4);
                buf.append("\"name\":\"");
                buf.append(Transform.escapeJsonControlCharacters(entry.getKey()));
                buf.append("\",");
                buf.append(this.eol);
                buf.append(this.indent4);
                buf.append("\"value\":\"");
                buf.append(Transform.escapeJsonControlCharacters(String.valueOf(entry.getValue())));
                buf.append("\"");
                buf.append(this.eol);
                buf.append(this.indent3);
                buf.append("}");
                if (i < entrySet.size()) {
                    buf.append(",");
                }
                buf.append(this.eol);
                ++i;
            }
            buf.append(this.indent2);
            buf.append("]");
        }
        buf.append(this.eol);
        buf.append(this.indent1);
        buf.append("}");
        return buf.toString();
    }

    @Override
    public byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        buf.append(this.eol);
        return buf.toString().getBytes(this.getCharset());
    }

    @Override
    public byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        return (this.eol + "]" + this.eol).getBytes(this.getCharset());
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("version", "2.0");
        return result;
    }

    @Override
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    @PluginFactory
    public static JSONLayout createLayout(@PluginAttribute(value="locationInfo") String locationInfo, @PluginAttribute(value="properties") String properties, @PluginAttribute(value="complete") String completeStr, @PluginAttribute(value="compact") String compactStr, @PluginAttribute(value="charset") String charsetName) {
        Charset charset = Charsets.getSupportedCharset(charsetName, Charsets.UTF_8);
        boolean info = Boolean.parseBoolean(locationInfo);
        boolean props = Boolean.parseBoolean(properties);
        boolean complete = Boolean.parseBoolean(completeStr);
        boolean compact = Boolean.parseBoolean(compactStr);
        return new JSONLayout(info, props, complete, compact, charset);
    }
}

