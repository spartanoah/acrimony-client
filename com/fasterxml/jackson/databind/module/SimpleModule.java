/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class SimpleModule
extends Module
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final String _name;
    protected final Version _version;
    protected SimpleSerializers _serializers = null;
    protected SimpleDeserializers _deserializers = null;
    protected SimpleSerializers _keySerializers = null;
    protected SimpleKeyDeserializers _keyDeserializers = null;
    protected SimpleAbstractTypeResolver _abstractTypes = null;
    protected SimpleValueInstantiators _valueInstantiators = null;
    protected BeanDeserializerModifier _deserializerModifier = null;
    protected BeanSerializerModifier _serializerModifier = null;
    protected HashMap<Class<?>, Class<?>> _mixins = null;
    protected LinkedHashSet<NamedType> _subtypes = null;
    protected PropertyNamingStrategy _namingStrategy = null;

    public SimpleModule() {
        this._name = this.getClass() == SimpleModule.class ? "SimpleModule-" + System.identityHashCode(this) : this.getClass().getName();
        this._version = Version.unknownVersion();
    }

    public SimpleModule(String name) {
        this(name, Version.unknownVersion());
    }

    public SimpleModule(Version version) {
        this._name = version.getArtifactId();
        this._version = version;
    }

    public SimpleModule(String name, Version version) {
        this._name = name;
        this._version = version;
    }

    public SimpleModule(String name, Version version, Map<Class<?>, JsonDeserializer<?>> deserializers) {
        this(name, version, deserializers, null);
    }

    public SimpleModule(String name, Version version, List<JsonSerializer<?>> serializers) {
        this(name, version, null, serializers);
    }

    public SimpleModule(String name, Version version, Map<Class<?>, JsonDeserializer<?>> deserializers, List<JsonSerializer<?>> serializers) {
        this._name = name;
        this._version = version;
        if (deserializers != null) {
            this._deserializers = new SimpleDeserializers(deserializers);
        }
        if (serializers != null) {
            this._serializers = new SimpleSerializers(serializers);
        }
    }

    @Override
    public Object getTypeId() {
        if (this.getClass() == SimpleModule.class) {
            return null;
        }
        return super.getTypeId();
    }

    public void setSerializers(SimpleSerializers s) {
        this._serializers = s;
    }

    public void setDeserializers(SimpleDeserializers d) {
        this._deserializers = d;
    }

    public void setKeySerializers(SimpleSerializers ks) {
        this._keySerializers = ks;
    }

    public void setKeyDeserializers(SimpleKeyDeserializers kd) {
        this._keyDeserializers = kd;
    }

    public void setAbstractTypes(SimpleAbstractTypeResolver atr) {
        this._abstractTypes = atr;
    }

    public void setValueInstantiators(SimpleValueInstantiators svi) {
        this._valueInstantiators = svi;
    }

    public SimpleModule setDeserializerModifier(BeanDeserializerModifier mod) {
        this._deserializerModifier = mod;
        return this;
    }

    public SimpleModule setSerializerModifier(BeanSerializerModifier mod) {
        this._serializerModifier = mod;
        return this;
    }

    protected SimpleModule setNamingStrategy(PropertyNamingStrategy naming) {
        this._namingStrategy = naming;
        return this;
    }

    public SimpleModule addSerializer(JsonSerializer<?> ser) {
        this._checkNotNull(ser, "serializer");
        if (this._serializers == null) {
            this._serializers = new SimpleSerializers();
        }
        this._serializers.addSerializer(ser);
        return this;
    }

    public <T> SimpleModule addSerializer(Class<? extends T> type, JsonSerializer<T> ser) {
        this._checkNotNull(type, "type to register serializer for");
        this._checkNotNull(ser, "serializer");
        if (this._serializers == null) {
            this._serializers = new SimpleSerializers();
        }
        this._serializers.addSerializer(type, ser);
        return this;
    }

    public <T> SimpleModule addKeySerializer(Class<? extends T> type, JsonSerializer<T> ser) {
        this._checkNotNull(type, "type to register key serializer for");
        this._checkNotNull(ser, "key serializer");
        if (this._keySerializers == null) {
            this._keySerializers = new SimpleSerializers();
        }
        this._keySerializers.addSerializer(type, ser);
        return this;
    }

    public <T> SimpleModule addDeserializer(Class<T> type, JsonDeserializer<? extends T> deser) {
        this._checkNotNull(type, "type to register deserializer for");
        this._checkNotNull(deser, "deserializer");
        if (this._deserializers == null) {
            this._deserializers = new SimpleDeserializers();
        }
        this._deserializers.addDeserializer(type, deser);
        return this;
    }

    public SimpleModule addKeyDeserializer(Class<?> type, KeyDeserializer deser) {
        this._checkNotNull(type, "type to register key deserializer for");
        this._checkNotNull(deser, "key deserializer");
        if (this._keyDeserializers == null) {
            this._keyDeserializers = new SimpleKeyDeserializers();
        }
        this._keyDeserializers.addDeserializer(type, deser);
        return this;
    }

    public <T> SimpleModule addAbstractTypeMapping(Class<T> superType, Class<? extends T> subType) {
        this._checkNotNull(superType, "abstract type to map");
        this._checkNotNull(subType, "concrete type to map to");
        if (this._abstractTypes == null) {
            this._abstractTypes = new SimpleAbstractTypeResolver();
        }
        this._abstractTypes = this._abstractTypes.addMapping(superType, subType);
        return this;
    }

    public SimpleModule registerSubtypes(Class<?> ... subtypes) {
        if (this._subtypes == null) {
            this._subtypes = new LinkedHashSet();
        }
        for (Class<?> subtype : subtypes) {
            this._checkNotNull(subtype, "subtype to register");
            this._subtypes.add(new NamedType(subtype));
        }
        return this;
    }

    public SimpleModule registerSubtypes(NamedType ... subtypes) {
        if (this._subtypes == null) {
            this._subtypes = new LinkedHashSet();
        }
        for (NamedType subtype : subtypes) {
            this._checkNotNull(subtype, "subtype to register");
            this._subtypes.add(subtype);
        }
        return this;
    }

    public SimpleModule registerSubtypes(Collection<Class<?>> subtypes) {
        if (this._subtypes == null) {
            this._subtypes = new LinkedHashSet();
        }
        for (Class<?> subtype : subtypes) {
            this._checkNotNull(subtype, "subtype to register");
            this._subtypes.add(new NamedType(subtype));
        }
        return this;
    }

    public SimpleModule addValueInstantiator(Class<?> beanType, ValueInstantiator inst) {
        this._checkNotNull(beanType, "class to register value instantiator for");
        this._checkNotNull(inst, "value instantiator");
        if (this._valueInstantiators == null) {
            this._valueInstantiators = new SimpleValueInstantiators();
        }
        this._valueInstantiators = this._valueInstantiators.addValueInstantiator(beanType, inst);
        return this;
    }

    public SimpleModule setMixInAnnotation(Class<?> targetType, Class<?> mixinClass) {
        this._checkNotNull(targetType, "target type");
        this._checkNotNull(mixinClass, "mixin class");
        if (this._mixins == null) {
            this._mixins = new HashMap();
        }
        this._mixins.put(targetType, mixinClass);
        return this;
    }

    @Override
    public String getModuleName() {
        return this._name;
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        if (this._serializers != null) {
            context.addSerializers(this._serializers);
        }
        if (this._deserializers != null) {
            context.addDeserializers(this._deserializers);
        }
        if (this._keySerializers != null) {
            context.addKeySerializers(this._keySerializers);
        }
        if (this._keyDeserializers != null) {
            context.addKeyDeserializers(this._keyDeserializers);
        }
        if (this._abstractTypes != null) {
            context.addAbstractTypeResolver(this._abstractTypes);
        }
        if (this._valueInstantiators != null) {
            context.addValueInstantiators(this._valueInstantiators);
        }
        if (this._deserializerModifier != null) {
            context.addBeanDeserializerModifier(this._deserializerModifier);
        }
        if (this._serializerModifier != null) {
            context.addBeanSerializerModifier(this._serializerModifier);
        }
        if (this._subtypes != null && this._subtypes.size() > 0) {
            context.registerSubtypes(this._subtypes.toArray(new NamedType[this._subtypes.size()]));
        }
        if (this._namingStrategy != null) {
            context.setNamingStrategy(this._namingStrategy);
        }
        if (this._mixins != null) {
            for (Map.Entry<Class<?>, Class<?>> entry : this._mixins.entrySet()) {
                context.setMixInAnnotations(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Version version() {
        return this._version;
    }

    protected void _checkNotNull(Object thingy, String type) {
        if (thingy == null) {
            throw new IllegalArgumentException(String.format("Cannot pass `null` as %s", type));
        }
    }
}

