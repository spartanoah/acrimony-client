/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import java.io.IOException;

public interface TypeIdResolver {
    public void init(JavaType var1);

    public String idFromValue(Object var1);

    public String idFromValueAndType(Object var1, Class<?> var2);

    public String idFromBaseType();

    public JavaType typeFromId(DatabindContext var1, String var2) throws IOException;

    public String getDescForKnownTypeIds();

    public JsonTypeInfo.Id getMechanism();
}

