/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;

@JacksonStdImpl
public class SqlDateSerializer
extends DateTimeSerializerBase<Date> {
    public SqlDateSerializer() {
        this(null, null);
    }

    protected SqlDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
        super(Date.class, useTimestamp, customFormat);
    }

    public SqlDateSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
        return new SqlDateSerializer(timestamp, customFormat);
    }

    @Override
    protected long _timestamp(Date value) {
        return value == null ? 0L : value.getTime();
    }

    @Override
    public void serialize(Date value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (this._asTimestamp(provider)) {
            g.writeNumber(this._timestamp(value));
            return;
        }
        if (this._customFormat == null) {
            g.writeString(value.toString());
            return;
        }
        this._serializeAsString(value, g, provider);
    }
}

