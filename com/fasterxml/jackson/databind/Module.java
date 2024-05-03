/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import java.util.Collection;
import java.util.Collections;

public abstract class Module
implements Versioned {
    public abstract String getModuleName();

    @Override
    public abstract Version version();

    public Object getTypeId() {
        return this.getClass().getName();
    }

    public abstract void setupModule(SetupContext var1);

    public Iterable<? extends Module> getDependencies() {
        return Collections.emptyList();
    }

    public static interface SetupContext {
        public Version getMapperVersion();

        public <C extends ObjectCodec> C getOwner();

        public TypeFactory getTypeFactory();

        public boolean isEnabled(MapperFeature var1);

        public boolean isEnabled(DeserializationFeature var1);

        public boolean isEnabled(SerializationFeature var1);

        public boolean isEnabled(JsonFactory.Feature var1);

        public boolean isEnabled(JsonParser.Feature var1);

        public boolean isEnabled(JsonGenerator.Feature var1);

        public MutableConfigOverride configOverride(Class<?> var1);

        public void addDeserializers(Deserializers var1);

        public void addKeyDeserializers(KeyDeserializers var1);

        public void addSerializers(Serializers var1);

        public void addKeySerializers(Serializers var1);

        public void addBeanDeserializerModifier(BeanDeserializerModifier var1);

        public void addBeanSerializerModifier(BeanSerializerModifier var1);

        public void addAbstractTypeResolver(AbstractTypeResolver var1);

        public void addTypeModifier(TypeModifier var1);

        public void addValueInstantiators(ValueInstantiators var1);

        public void setClassIntrospector(ClassIntrospector var1);

        public void insertAnnotationIntrospector(AnnotationIntrospector var1);

        public void appendAnnotationIntrospector(AnnotationIntrospector var1);

        public void registerSubtypes(Class<?> ... var1);

        public void registerSubtypes(NamedType ... var1);

        public void registerSubtypes(Collection<Class<?>> var1);

        public void setMixInAnnotations(Class<?> var1, Class<?> var2);

        public void addDeserializationProblemHandler(DeserializationProblemHandler var1);

        public void setNamingStrategy(PropertyNamingStrategy var1);
    }
}

