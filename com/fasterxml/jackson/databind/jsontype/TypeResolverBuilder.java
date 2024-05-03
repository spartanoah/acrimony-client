/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.util.Collection;

public interface TypeResolverBuilder<T extends TypeResolverBuilder<T>> {
    public Class<?> getDefaultImpl();

    public TypeSerializer buildTypeSerializer(SerializationConfig var1, JavaType var2, Collection<NamedType> var3);

    public TypeDeserializer buildTypeDeserializer(DeserializationConfig var1, JavaType var2, Collection<NamedType> var3);

    public T init(JsonTypeInfo.Id var1, TypeIdResolver var2);

    public T inclusion(JsonTypeInfo.As var1);

    public T typeProperty(String var1);

    public T defaultImpl(Class<?> var1);

    public T typeIdVisibility(boolean var1);
}

