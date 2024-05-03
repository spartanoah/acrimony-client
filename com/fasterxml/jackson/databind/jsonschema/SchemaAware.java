/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsonschema;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.lang.reflect.Type;

public interface SchemaAware {
    public JsonNode getSchema(SerializerProvider var1, Type var2) throws JsonMappingException;

    public JsonNode getSchema(SerializerProvider var1, Type var2, boolean var3) throws JsonMappingException;
}

