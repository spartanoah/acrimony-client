/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.MapMessageJsonFormatter;
import org.apache.logging.log4j.message.ParameterFormatter;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.IndexedReadOnlyStringMap;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.MultiFormatStringBuilderFormattable;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.TriConsumer;

@AsynchronouslyFormattable
@PerformanceSensitive(value={"allocation"})
public class MapMessage<M extends MapMessage<M, V>, V>
implements MultiFormatStringBuilderFormattable {
    private static final long serialVersionUID = -5031471831131487120L;
    private final IndexedStringMap data;

    public MapMessage() {
        this.data = new SortedArrayStringMap();
    }

    public MapMessage(int initialCapacity) {
        this.data = new SortedArrayStringMap(initialCapacity);
    }

    public MapMessage(Map<String, V> map) {
        this.data = new SortedArrayStringMap(map);
    }

    @Override
    public String[] getFormats() {
        return MapFormat.names();
    }

    @Override
    public Object[] getParameters() {
        Object[] result = new Object[this.data.size()];
        for (int i = 0; i < this.data.size(); ++i) {
            result[i] = this.data.getValueAt(i);
        }
        return result;
    }

    @Override
    public String getFormat() {
        return "";
    }

    public Map<String, V> getData() {
        TreeMap result = new TreeMap();
        for (int i = 0; i < this.data.size(); ++i) {
            result.put(this.data.getKeyAt(i), this.data.getValueAt(i));
        }
        return Collections.unmodifiableMap(result);
    }

    public IndexedReadOnlyStringMap getIndexedReadOnlyStringMap() {
        return this.data;
    }

    public void clear() {
        this.data.clear();
    }

    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }

    public void put(String candidateKey, String value) {
        if (value == null) {
            throw new IllegalArgumentException("No value provided for key " + candidateKey);
        }
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
    }

    public void putAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            this.data.putValue(entry.getKey(), entry.getValue());
        }
    }

    public String get(String key) {
        Object result = this.data.getValue(key);
        return ParameterFormatter.deepToString(result);
    }

    public String remove(String key) {
        String result = this.get(key);
        this.data.remove(key);
        return result;
    }

    public String asString() {
        return this.format(null, new StringBuilder()).toString();
    }

    public String asString(String format) {
        try {
            return this.format(EnglishEnums.valueOf(MapFormat.class, format), new StringBuilder()).toString();
        } catch (IllegalArgumentException ex) {
            return this.asString();
        }
    }

    public <CV> void forEach(BiConsumer<String, ? super CV> action) {
        this.data.forEach(action);
    }

    public <CV, S> void forEach(TriConsumer<String, ? super CV, S> action, S state) {
        this.data.forEach(action, state);
    }

    private StringBuilder format(MapFormat format, StringBuilder sb) {
        if (format == null) {
            this.appendMap(sb);
        } else {
            switch (format) {
                case XML: {
                    this.asXml(sb);
                    break;
                }
                case JSON: {
                    this.asJson(sb);
                    break;
                }
                case JAVA: {
                    this.asJava(sb);
                    break;
                }
                case JAVA_UNQUOTED: {
                    this.asJavaUnquoted(sb);
                    break;
                }
                default: {
                    this.appendMap(sb);
                }
            }
        }
        return sb;
    }

    public void asXml(StringBuilder sb) {
        sb.append("<Map>\n");
        for (int i = 0; i < this.data.size(); ++i) {
            sb.append("  <Entry key=\"").append(this.data.getKeyAt(i)).append("\">");
            int size = sb.length();
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i), sb);
            StringBuilders.escapeXml(sb, size);
            sb.append("</Entry>\n");
        }
        sb.append("</Map>");
    }

    @Override
    public String getFormattedMessage() {
        return this.asString();
    }

    @Override
    public String getFormattedMessage(String[] formats) {
        return this.format(this.getFormat(formats), new StringBuilder()).toString();
    }

    private MapFormat getFormat(String[] formats) {
        if (formats == null || formats.length == 0) {
            return null;
        }
        for (int i = 0; i < formats.length; ++i) {
            MapFormat mapFormat = MapFormat.lookupIgnoreCase(formats[i]);
            if (mapFormat == null) continue;
            return mapFormat;
        }
        return null;
    }

    protected void appendMap(StringBuilder sb) {
        for (int i = 0; i < this.data.size(); ++i) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(this.data.getKeyAt(i)).append('=').append('\"');
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i), sb);
            sb.append('\"');
        }
    }

    protected void asJson(StringBuilder sb) {
        MapMessageJsonFormatter.format(sb, this.data);
    }

    protected void asJavaUnquoted(StringBuilder sb) {
        this.asJava(sb, false);
    }

    protected void asJava(StringBuilder sb) {
        this.asJava(sb, true);
    }

    private void asJava(StringBuilder sb, boolean quoted) {
        sb.append('{');
        for (int i = 0; i < this.data.size(); ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.data.getKeyAt(i)).append('=');
            if (quoted) {
                sb.append('\"');
            }
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i), sb);
            if (!quoted) continue;
            sb.append('\"');
        }
        sb.append('}');
    }

    public M newInstance(Map<String, V> map) {
        return (M)new MapMessage<M, V>(map);
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        this.format(null, buffer);
    }

    @Override
    public void formatTo(String[] formats, StringBuilder buffer) {
        this.format(this.getFormat(formats), buffer);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MapMessage that = (MapMessage)o;
        return this.data.equals(that.data);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    protected void validate(String key, boolean value) {
    }

    protected void validate(String key, byte value) {
    }

    protected void validate(String key, char value) {
    }

    protected void validate(String key, double value) {
    }

    protected void validate(String key, float value) {
    }

    protected void validate(String key, int value) {
    }

    protected void validate(String key, long value) {
    }

    protected void validate(String key, Object value) {
    }

    protected void validate(String key, short value) {
    }

    protected void validate(String key, String value) {
    }

    protected String toKey(String candidateKey) {
        return candidateKey;
    }

    public M with(String candidateKey, boolean value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, byte value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, char value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, Character.valueOf(value));
        return (M)this;
    }

    public M with(String candidateKey, double value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, float value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, Float.valueOf(value));
        return (M)this;
    }

    public M with(String candidateKey, int value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, long value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, Object value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, short value) {
        String key = this.toKey(candidateKey);
        this.validate(key, value);
        this.data.putValue(key, value);
        return (M)this;
    }

    public M with(String candidateKey, String value) {
        String key = this.toKey(candidateKey);
        this.put(key, value);
        return (M)this;
    }

    public static enum MapFormat {
        XML,
        JSON,
        JAVA,
        JAVA_UNQUOTED;


        public static MapFormat lookupIgnoreCase(String format) {
            return XML.name().equalsIgnoreCase(format) ? XML : (JSON.name().equalsIgnoreCase(format) ? JSON : (JAVA.name().equalsIgnoreCase(format) ? JAVA : (JAVA_UNQUOTED.name().equalsIgnoreCase(format) ? JAVA_UNQUOTED : null)));
        }

        public static String[] names() {
            return new String[]{XML.name(), JSON.name(), JAVA.name(), JAVA_UNQUOTED.name()};
        }
    }
}

