/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWithSerializerProvider;

public interface JsonObjectFormatVisitor
extends JsonFormatVisitorWithSerializerProvider {
    public void property(BeanProperty var1) throws JsonMappingException;

    public void property(String var1, JsonFormatVisitable var2, JavaType var3) throws JsonMappingException;

    public void optionalProperty(BeanProperty var1) throws JsonMappingException;

    public void optionalProperty(String var1, JsonFormatVisitable var2, JavaType var3) throws JsonMappingException;

    public static class Base
    implements JsonObjectFormatVisitor {
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
        public void property(BeanProperty prop) throws JsonMappingException {
        }

        @Override
        public void property(String name, JsonFormatVisitable handler, JavaType propertyTypeHint) throws JsonMappingException {
        }

        @Override
        public void optionalProperty(BeanProperty prop) throws JsonMappingException {
        }

        @Override
        public void optionalProperty(String name, JsonFormatVisitable handler, JavaType propertyTypeHint) throws JsonMappingException {
        }
    }
}

