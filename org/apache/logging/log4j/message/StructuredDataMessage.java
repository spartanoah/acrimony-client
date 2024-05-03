/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.message;

import java.util.Map;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.StructuredDataId;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.StringBuilders;

@AsynchronouslyFormattable
public class StructuredDataMessage
extends MapMessage<StructuredDataMessage, String> {
    private static final long serialVersionUID = 1703221292892071920L;
    private static final int MAX_LENGTH = 32;
    private static final int HASHVAL = 31;
    private StructuredDataId id;
    private String message;
    private String type;
    private final int maxLength;

    public StructuredDataMessage(String id, String msg, String type) {
        this(id, msg, type, 32);
    }

    public StructuredDataMessage(String id, String msg, String type, int maxLength) {
        this.id = new StructuredDataId(id, null, null, maxLength);
        this.message = msg;
        this.type = type;
        this.maxLength = maxLength;
    }

    public StructuredDataMessage(String id, String msg, String type, Map<String, String> data) {
        this(id, msg, type, data, 32);
    }

    public StructuredDataMessage(String id, String msg, String type, Map<String, String> data, int maxLength) {
        super(data);
        this.id = new StructuredDataId(id, null, null, maxLength);
        this.message = msg;
        this.type = type;
        this.maxLength = maxLength;
    }

    public StructuredDataMessage(StructuredDataId id, String msg, String type) {
        this(id, msg, type, 32);
    }

    public StructuredDataMessage(StructuredDataId id, String msg, String type, int maxLength) {
        this.id = id;
        this.message = msg;
        this.type = type;
        this.maxLength = maxLength;
    }

    public StructuredDataMessage(StructuredDataId id, String msg, String type, Map<String, String> data) {
        this(id, msg, type, data, 32);
    }

    public StructuredDataMessage(StructuredDataId id, String msg, String type, Map<String, String> data, int maxLength) {
        super(data);
        this.id = id;
        this.message = msg;
        this.type = type;
        this.maxLength = maxLength;
    }

    private StructuredDataMessage(StructuredDataMessage msg, Map<String, String> map) {
        super(map);
        this.id = msg.id;
        this.message = msg.message;
        this.type = msg.type;
        this.maxLength = 32;
    }

    protected StructuredDataMessage() {
        this.maxLength = 32;
    }

    @Override
    public String[] getFormats() {
        String[] formats = new String[Format.values().length];
        int i = 0;
        for (Format format : Format.values()) {
            formats[i++] = format.name();
        }
        return formats;
    }

    public StructuredDataId getId() {
        return this.id;
    }

    protected void setId(String id) {
        this.id = new StructuredDataId(id, null, null);
    }

    protected void setId(StructuredDataId id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    protected void setType(String type) {
        if (type.length() > 32) {
            throw new IllegalArgumentException("structured data type exceeds maximum length of 32 characters: " + type);
        }
        this.type = type;
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        this.asString(Format.FULL, null, buffer);
    }

    @Override
    public void formatTo(String[] formats, StringBuilder buffer) {
        this.asString(this.getFormat(formats), null, buffer);
    }

    @Override
    public String getFormat() {
        return this.message;
    }

    protected void setMessageFormat(String msg) {
        this.message = msg;
    }

    @Override
    public String asString() {
        return this.asString(Format.FULL, null);
    }

    @Override
    public String asString(String format) {
        try {
            return this.asString(EnglishEnums.valueOf(Format.class, format), null);
        } catch (IllegalArgumentException ex) {
            return this.asString();
        }
    }

    public final String asString(Format format, StructuredDataId structuredDataId) {
        StringBuilder sb = new StringBuilder();
        this.asString(format, structuredDataId, sb);
        return sb.toString();
    }

    public final void asString(Format format, StructuredDataId structuredDataId, StringBuilder sb) {
        String msg;
        StructuredDataId sdId;
        boolean full = Format.FULL.equals((Object)format);
        if (full) {
            String myType = this.getType();
            if (myType == null) {
                return;
            }
            sb.append(this.getType()).append(' ');
        }
        if ((sdId = (sdId = this.getId()) != null ? sdId.makeId(structuredDataId) : structuredDataId) == null || sdId.getName() == null) {
            return;
        }
        if (Format.XML.equals((Object)format)) {
            this.asXml(sdId, sb);
            return;
        }
        sb.append('[');
        StringBuilders.appendValue(sb, sdId);
        sb.append(' ');
        this.appendMap(sb);
        sb.append(']');
        if (full && (msg = this.getFormat()) != null) {
            sb.append(' ').append(msg);
        }
    }

    private void asXml(StructuredDataId structuredDataId, StringBuilder sb) {
        sb.append("<StructuredData>\n");
        sb.append("<type>").append(this.type).append("</type>\n");
        sb.append("<id>").append(structuredDataId).append("</id>\n");
        super.asXml(sb);
        sb.append("\n</StructuredData>\n");
    }

    @Override
    public String getFormattedMessage() {
        return this.asString(Format.FULL, null);
    }

    @Override
    public String getFormattedMessage(String[] formats) {
        return this.asString(this.getFormat(formats), null);
    }

    private Format getFormat(String[] formats) {
        if (formats != null && formats.length > 0) {
            for (int i = 0; i < formats.length; ++i) {
                String format = formats[i];
                if (Format.XML.name().equalsIgnoreCase(format)) {
                    return Format.XML;
                }
                if (!Format.FULL.name().equalsIgnoreCase(format)) continue;
                return Format.FULL;
            }
            return null;
        }
        return Format.FULL;
    }

    @Override
    public String toString() {
        return this.asString(null, null);
    }

    @Override
    public StructuredDataMessage newInstance(Map<String, String> map) {
        return new StructuredDataMessage(this, map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StructuredDataMessage that = (StructuredDataMessage)o;
        if (!super.equals(o)) {
            return false;
        }
        if (this.type != null ? !this.type.equals(that.type) : that.type != null) {
            return false;
        }
        if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
            return false;
        }
        return !(this.message != null ? !this.message.equals(that.message) : that.message != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.id != null ? this.id.hashCode() : 0);
        result = 31 * result + (this.message != null ? this.message.hashCode() : 0);
        return result;
    }

    @Override
    protected void validate(String key, boolean value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, byte value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, char value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, double value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, float value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, int value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, long value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, Object value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, short value) {
        this.validateKey(key);
    }

    @Override
    protected void validate(String key, String value) {
        this.validateKey(key);
    }

    protected void validateKey(String key) {
        if (this.maxLength > 0 && key.length() > this.maxLength) {
            throw new IllegalArgumentException("Structured data keys are limited to " + this.maxLength + " characters. key: " + key);
        }
        for (int i = 0; i < key.length(); ++i) {
            char c = key.charAt(i);
            if (c >= '!' && c <= '~' && c != '=' && c != ']' && c != '\"') continue;
            throw new IllegalArgumentException("Structured data keys must contain printable US ASCII charactersand may not contain a space, =, ], or \"");
        }
    }

    public static enum Format {
        XML,
        FULL;

    }
}

