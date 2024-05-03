/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWithSerializerProvider;

public interface JsonMapFormatVisitor
extends JsonFormatVisitorWithSerializerProvider {
    public void keyFormat(JsonFormatVisitable var1, JavaType var2) throws JsonMappingException;

    public void valueFormat(JsonFormatVisitable var1, JavaType var2) throws JsonMappingException;

    public static class Base
    implements JsonMapFormatVisitor {
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
        public void keyFormat(JsonFormatVisitable handler, JavaType keyType) throws JsonMappingException {
        }

        @Override
        public void valueFormat(JsonFormatVisitable handler, JavaType valueType) throws JsonMappingException {
        }
    }
}

