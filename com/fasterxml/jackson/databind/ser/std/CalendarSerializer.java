/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

@JacksonStdImpl
public class CalendarSerializer
extends DateTimeSerializerBase<Calendar> {
    public static final CalendarSerializer instance = new CalendarSerializer();

    public CalendarSerializer() {
        this(null, null);
    }

    public CalendarSerializer(Boolean useTimestamp, DateFormat customFormat) {
        super(Calendar.class, useTimestamp, customFormat);
    }

    public CalendarSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
        return new CalendarSerializer(timestamp, customFormat);
    }

    @Override
    protected long _timestamp(Calendar value) {
        return value == null ? 0L : value.getTimeInMillis();
    }

    @Override
    public void serialize(Calendar value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (this._asTimestamp(provider)) {
            g.writeNumber(this._timestamp(value));
            return;
        }
        this._serializeAsString(value.getTime(), g, provider);
    }
}

