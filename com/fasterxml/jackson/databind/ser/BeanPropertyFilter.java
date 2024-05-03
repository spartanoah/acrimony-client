/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

@Deprecated
public interface BeanPropertyFilter {
    public void serializeAsField(Object var1, JsonGenerator var2, SerializerProvider var3, BeanPropertyWriter var4) throws Exception;

    @Deprecated
    public void depositSchemaProperty(BeanPropertyWriter var1, ObjectNode var2, SerializerProvider var3) throws JsonMappingException;

    public void depositSchemaProperty(BeanPropertyWriter var1, JsonObjectFormatVisitor var2, SerializerProvider var3) throws JsonMappingException;
}

