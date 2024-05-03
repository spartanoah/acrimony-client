/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWithSerializerProvider;

public interface JsonArrayFormatVisitor
extends JsonFormatVisitorWithSerializerProvider {
    public void itemsFormat(JsonFormatVisitable var1, JavaType var2) throws JsonMappingException;

    public void itemsFormat(JsonFormatTypes var1) throws JsonMappingException;

    public static class Base
    implements JsonArrayFormatVisitor {
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
        public void itemsFormat(JsonFormatVisitable handler, JavaType elementType) throws JsonMappingException {
        }

        @Override
        public void itemsFormat(JsonFormatTypes format) throws JsonMappingException {
        }
    }
}

