/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.TokenStreamFactory;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.TimeZone;

public abstract class MapperBuilder<M extends ObjectMapper, B extends MapperBuilder<M, B>> {
    protected final M _mapper;

    protected MapperBuilder(M mapper) {
        this._mapper = mapper;
    }

    public M build() {
        return this._mapper;
    }

    public boolean isEnabled(MapperFeature f) {
        return ((ObjectMapper)this._mapper).isEnabled(f);
    }

    public boolean isEnabled(DeserializationFeature f) {
        return ((ObjectMapper)this._mapper).isEnabled(f);
    }

    public boolean isEnabled(SerializationFeature f) {
        return ((ObjectMapper)this._mapper).isEnabled(f);
    }

    public boolean isEnabled(JsonParser.Feature f) {
        return ((ObjectMapper)this._mapper).isEnabled(f);
    }

    public boolean isEnabled(JsonGenerator.Feature f) {
        return ((ObjectMapper)this._mapper).isEnabled(f);
    }

    public TokenStreamFactory streamFactory() {
        return ((ObjectMapper)this._mapper).tokenStreamFactory();
    }

    public B enable(MapperFeature ... features) {
        ((ObjectMapper)this._mapper).enable(features);
        return this._this();
    }

    public B disable(MapperFeature ... features) {
        ((ObjectMapper)this._mapper).disable(features);
        return this._this();
    }

    public B configure(MapperFeature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature, state);
        return this._this();
    }

    public B enable(SerializationFeature ... features) {
        for (SerializationFeature f : features) {
            ((ObjectMapper)this._mapper).enable(f);
        }
        return this._this();
    }

    public B disable(SerializationFeature ... features) {
        for (SerializationFeature f : features) {
            ((ObjectMapper)this._mapper).disable(f);
        }
        return this._this();
    }

    public B configure(SerializationFeature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature, state);
        return this._this();
    }

    public B enable(DeserializationFeature ... features) {
        for (DeserializationFeature f : features) {
            ((ObjectMapper)this._mapper).enable(f);
        }
        return this._this();
    }

    public B disable(DeserializationFeature ... features) {
        for (DeserializationFeature f : features) {
            ((ObjectMapper)this._mapper).disable(f);
        }
        return this._this();
    }

    public B configure(DeserializationFeature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature, state);
        return this._this();
    }

    public B enable(JsonParser.Feature ... features) {
        ((ObjectMapper)this._mapper).enable(features);
        return this._this();
    }

    public B disable(JsonParser.Feature ... features) {
        ((ObjectMapper)this._mapper).disable(features);
        return this._this();
    }

    public B configure(JsonParser.Feature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature, state);
        return this._this();
    }

    public B enable(JsonGenerator.Feature ... features) {
        ((ObjectMapper)this._mapper).enable(features);
        return this._this();
    }

    public B disable(JsonGenerator.Feature ... features) {
        ((ObjectMapper)this._mapper).disable(features);
        return this._this();
    }

    public B configure(JsonGenerator.Feature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature, state);
        return this._this();
    }

    public B enable(StreamReadFeature ... features) {
        for (StreamReadFeature f : features) {
            ((ObjectMapper)this._mapper).enable(f.mappedFeature());
        }
        return this._this();
    }

    public B disable(StreamReadFeature ... features) {
        for (StreamReadFeature f : features) {
            ((ObjectMapper)this._mapper).disable(f.mappedFeature());
        }
        return this._this();
    }

    public B configure(StreamReadFeature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature.mappedFeature(), state);
        return this._this();
    }

    public B enable(StreamWriteFeature ... features) {
        for (StreamWriteFeature f : features) {
            ((ObjectMapper)this._mapper).enable(f.mappedFeature());
        }
        return this._this();
    }

    public B disable(StreamWriteFeature ... features) {
        for (StreamWriteFeature f : features) {
            ((ObjectMapper)this._mapper).disable(f.mappedFeature());
        }
        return this._this();
    }

    public B configure(StreamWriteFeature feature, boolean state) {
        ((ObjectMapper)this._mapper).configure(feature.mappedFeature(), state);
        return this._this();
    }

    public B addModule(Module module) {
        ((ObjectMapper)this._mapper).registerModule(module);
        return this._this();
    }

    public B addModules(Module ... modules) {
        for (Module module : modules) {
            this.addModule(module);
        }
        return this._this();
    }

    public B addModules(Iterable<? extends Module> modules) {
        for (Module module : modules) {
            this.addModule(module);
        }
        return this._this();
    }

    public static List<Module> findModules() {
        return MapperBuilder.findModules(null);
    }

    public static List<Module> findModules(ClassLoader classLoader) {
        ArrayList<Module> modules = new ArrayList<Module>();
        ServiceLoader<Module> loader = MapperBuilder.secureGetServiceLoader(Module.class, classLoader);
        for (Module module : loader) {
            modules.add(module);
        }
        return modules;
    }

    private static <T> ServiceLoader<T> secureGetServiceLoader(final Class<T> clazz, final ClassLoader classLoader) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return classLoader == null ? ServiceLoader.load(clazz) : ServiceLoader.load(clazz, classLoader);
        }
        return (ServiceLoader)AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<T>>(){

            @Override
            public ServiceLoader<T> run() {
                return classLoader == null ? ServiceLoader.load(clazz) : ServiceLoader.load(clazz, classLoader);
            }
        });
    }

    public B findAndAddModules() {
        return this.addModules(MapperBuilder.findModules());
    }

    public B annotationIntrospector(AnnotationIntrospector intr) {
        ((ObjectMapper)this._mapper).setAnnotationIntrospector(intr);
        return this._this();
    }

    public B nodeFactory(JsonNodeFactory f) {
        ((ObjectMapper)this._mapper).setNodeFactory(f);
        return this._this();
    }

    public B typeFactory(TypeFactory f) {
        ((ObjectMapper)this._mapper).setTypeFactory(f);
        return this._this();
    }

    public B subtypeResolver(SubtypeResolver r) {
        ((ObjectMapper)this._mapper).setSubtypeResolver(r);
        return this._this();
    }

    public B visibility(VisibilityChecker<?> vc) {
        ((ObjectMapper)this._mapper).setVisibility(vc);
        return this._this();
    }

    public B visibility(PropertyAccessor forMethod, JsonAutoDetect.Visibility visibility) {
        ((ObjectMapper)this._mapper).setVisibility(forMethod, visibility);
        return this._this();
    }

    public B handlerInstantiator(HandlerInstantiator hi) {
        ((ObjectMapper)this._mapper).setHandlerInstantiator(hi);
        return this._this();
    }

    public B propertyNamingStrategy(PropertyNamingStrategy s) {
        ((ObjectMapper)this._mapper).setPropertyNamingStrategy(s);
        return this._this();
    }

    public B serializerFactory(SerializerFactory f) {
        ((ObjectMapper)this._mapper).setSerializerFactory(f);
        return this._this();
    }

    public B filterProvider(FilterProvider prov) {
        ((ObjectMapper)this._mapper).setFilterProvider(prov);
        return this._this();
    }

    public B defaultPrettyPrinter(PrettyPrinter pp) {
        ((ObjectMapper)this._mapper).setDefaultPrettyPrinter(pp);
        return this._this();
    }

    public B injectableValues(InjectableValues v) {
        ((ObjectMapper)this._mapper).setInjectableValues(v);
        return this._this();
    }

    public B addHandler(DeserializationProblemHandler h) {
        ((ObjectMapper)this._mapper).addHandler(h);
        return this._this();
    }

    public B clearProblemHandlers() {
        ((ObjectMapper)this._mapper).clearProblemHandlers();
        return this._this();
    }

    public B defaultSetterInfo(JsonSetter.Value v) {
        ((ObjectMapper)this._mapper).setDefaultSetterInfo(v);
        return this._this();
    }

    public B defaultMergeable(Boolean b) {
        ((ObjectMapper)this._mapper).setDefaultMergeable(b);
        return this._this();
    }

    public B defaultLeniency(Boolean b) {
        ((ObjectMapper)this._mapper).setDefaultLeniency(b);
        return this._this();
    }

    public B defaultDateFormat(DateFormat df) {
        ((ObjectMapper)this._mapper).setDateFormat(df);
        return this._this();
    }

    public B defaultTimeZone(TimeZone tz) {
        ((ObjectMapper)this._mapper).setTimeZone(tz);
        return this._this();
    }

    public B defaultLocale(Locale locale) {
        ((ObjectMapper)this._mapper).setLocale(locale);
        return this._this();
    }

    public B defaultBase64Variant(Base64Variant v) {
        ((ObjectMapper)this._mapper).setBase64Variant(v);
        return this._this();
    }

    public B serializationInclusion(JsonInclude.Include incl) {
        ((ObjectMapper)this._mapper).setSerializationInclusion(incl);
        return this._this();
    }

    public B defaultPropertyInclusion(JsonInclude.Value incl) {
        ((ObjectMapper)this._mapper).setDefaultPropertyInclusion(incl);
        return this._this();
    }

    public B addMixIn(Class<?> target, Class<?> mixinSource) {
        ((ObjectMapper)this._mapper).addMixIn(target, mixinSource);
        return this._this();
    }

    public B registerSubtypes(Class<?> ... subtypes) {
        ((ObjectMapper)this._mapper).registerSubtypes(subtypes);
        return this._this();
    }

    public B registerSubtypes(NamedType ... subtypes) {
        ((ObjectMapper)this._mapper).registerSubtypes(subtypes);
        return this._this();
    }

    public B registerSubtypes(Collection<Class<?>> subtypes) {
        ((ObjectMapper)this._mapper).registerSubtypes(subtypes);
        return this._this();
    }

    public B polymorphicTypeValidator(PolymorphicTypeValidator ptv) {
        ((ObjectMapper)this._mapper).setPolymorphicTypeValidator(ptv);
        return this._this();
    }

    public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator) {
        ((ObjectMapper)this._mapper).activateDefaultTyping(subtypeValidator);
        return this._this();
    }

    public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping dti) {
        ((ObjectMapper)this._mapper).activateDefaultTyping(subtypeValidator, dti);
        return this._this();
    }

    public B activateDefaultTyping(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping applicability, JsonTypeInfo.As includeAs) {
        ((ObjectMapper)this._mapper).activateDefaultTyping(subtypeValidator, applicability, includeAs);
        return this._this();
    }

    public B activateDefaultTypingAsProperty(PolymorphicTypeValidator subtypeValidator, ObjectMapper.DefaultTyping applicability, String propertyName) {
        ((ObjectMapper)this._mapper).activateDefaultTypingAsProperty(subtypeValidator, applicability, propertyName);
        return this._this();
    }

    public B deactivateDefaultTyping() {
        ((ObjectMapper)this._mapper).deactivateDefaultTyping();
        return this._this();
    }

    public B setDefaultTyping(TypeResolverBuilder<?> typer) {
        ((ObjectMapper)this._mapper).setDefaultTyping(typer);
        return this._this();
    }

    protected final B _this() {
        return (B)this;
    }
}

