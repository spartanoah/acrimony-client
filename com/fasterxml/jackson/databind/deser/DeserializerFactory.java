/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;

public abstract class DeserializerFactory {
    protected static final Deserializers[] NO_DESERIALIZERS = new Deserializers[0];

    public abstract DeserializerFactory withAdditionalDeserializers(Deserializers var1);

    public abstract DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers var1);

    public abstract DeserializerFactory withDeserializerModifier(BeanDeserializerModifier var1);

    public abstract DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver var1);

    public abstract DeserializerFactory withValueInstantiators(ValueInstantiators var1);

    public abstract JavaType mapAbstractType(DeserializationConfig var1, JavaType var2) throws JsonMappingException;

    public abstract ValueInstantiator findValueInstantiator(DeserializationContext var1, BeanDescription var2) throws JsonMappingException;

    public abstract JsonDeserializer<Object> createBeanDeserializer(DeserializationContext var1, JavaType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<Object> createBuilderBasedDeserializer(DeserializationContext var1, JavaType var2, BeanDescription var3, Class<?> var4) throws JsonMappingException;

    public abstract JsonDeserializer<?> createEnumDeserializer(DeserializationContext var1, JavaType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createReferenceDeserializer(DeserializationContext var1, ReferenceType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createTreeDeserializer(DeserializationConfig var1, JavaType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createArrayDeserializer(DeserializationContext var1, ArrayType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createCollectionDeserializer(DeserializationContext var1, CollectionType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext var1, CollectionLikeType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createMapDeserializer(DeserializationContext var1, MapType var2, BeanDescription var3) throws JsonMappingException;

    public abstract JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext var1, MapLikeType var2, BeanDescription var3) throws JsonMappingException;

    public abstract KeyDeserializer createKeyDeserializer(DeserializationContext var1, JavaType var2) throws JsonMappingException;

    public abstract TypeDeserializer findTypeDeserializer(DeserializationConfig var1, JavaType var2) throws JsonMappingException;

    public abstract boolean hasExplicitDeserializerFor(DeserializationConfig var1, Class<?> var2);
}

