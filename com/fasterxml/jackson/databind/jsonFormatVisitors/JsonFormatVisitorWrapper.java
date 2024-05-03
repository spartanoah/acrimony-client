/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWithSerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNullFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;

public interface JsonFormatVisitorWrapper
extends JsonFormatVisitorWithSerializerProvider {
    public JsonObjectFormatVisitor expectObjectFormat(JavaType var1) throws JsonMappingException;

    public JsonArrayFormatVisitor expectArrayFormat(JavaType var1) throws JsonMappingException;

    public JsonStringFormatVisitor expectStringFormat(JavaType var1) throws JsonMappingException;

    public JsonNumberFormatVisitor expectNumberFormat(JavaType var1) throws JsonMappingException;

    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType var1) throws JsonMappingException;

    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType var1) throws JsonMappingException;

    public JsonNullFormatVisitor expectNullFormat(JavaType var1) throws JsonMappingException;

    public JsonAnyFormatVisitor expectAnyFormat(JavaType var1) throws JsonMappingException;

    public JsonMapFormatVisitor expectMapFormat(JavaType var1) throws JsonMappingException;

    public static class Base
    implements JsonFormatVisitorWrapper {
        protected SerializerProvider _provider;

        public Base() {
        }

        public Base(SerializerProvider p) {
            this._provider = p;
        }

        @Override
        public SerializerProvider getProvider() {
            return this._provider;
        }

        @Override
        public void setProvider(SerializerProvider p) {
            this._provider = p;
        }

        @Override
        public JsonObjectFormatVisitor expectObjectFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonArrayFormatVisitor expectArrayFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonStringFormatVisitor expectStringFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonBooleanFormatVisitor expectBooleanFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonNullFormatVisitor expectNullFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonAnyFormatVisitor expectAnyFormat(JavaType type) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonMapFormatVisitor expectMapFormat(JavaType type) throws JsonMappingException {
            return null;
        }
    }
}

