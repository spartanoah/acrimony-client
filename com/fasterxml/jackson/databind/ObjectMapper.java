/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectMapper
extends ObjectCodec
implements Versioned,
Serializable {
    private static final long serialVersionUID = 2L;
    protected static final AnnotationIntrospector DEFAULT_ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector();
    protected static final BaseSettings DEFAULT_BASE = new BaseSettings(null, DEFAULT_ANNOTATION_INTROSPECTOR, null, TypeFactory.defaultInstance(), null, StdDateFormat.instance, null, Locale.getDefault(), null, Base64Variants.getDefaultVariant(), LaissezFaireSubTypeValidator.instance);
    protected final JsonFactory _jsonFactory;
    protected TypeFactory _typeFactory;
    protected InjectableValues _injectableValues;
    protected SubtypeResolver _subtypeResolver;
    protected final ConfigOverrides _configOverrides;
    protected SimpleMixInResolver _mixIns;
    protected SerializationConfig _serializationConfig;
    protected DefaultSerializerProvider _serializerProvider;
    protected SerializerFactory _serializerFactory;
    protected DeserializationConfig _deserializationConfig;
    protected DefaultDeserializationContext _deserializationContext;
    protected Set<Object> _registeredModuleTypes;
    protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers = new ConcurrentHashMap(64, 0.6f, 2);

    public ObjectMapper() {
        this(null, null, null);
    }

    public ObjectMapper(JsonFactory jf) {
        this(jf, null, null);
    }

    protected ObjectMapper(ObjectMapper src) {
        this._jsonFactory = src._jsonFactory.copy();
        this._jsonFactory.setCodec(this);
        this._subtypeResolver = src._subtypeResolver.copy();
        this._typeFactory = src._typeFactory;
        this._injectableValues = src._injectableValues;
        this._configOverrides = src._configOverrides.copy();
        this._mixIns = src._mixIns.copy();
        RootNameLookup rootNames = new RootNameLookup();
        this._serializationConfig = new SerializationConfig(src._serializationConfig, this._subtypeResolver, this._mixIns, rootNames, this._configOverrides);
        this._deserializationConfig = new DeserializationConfig(src._deserializationConfig, this._subtypeResolver, this._mixIns, rootNames, this._configOverrides);
        this._serializerProvider = src._serializerProvider.copy();
        this._deserializationContext = src._deserializationContext.copy();
        this._serializerFactory = src._serializerFactory;
        Set<Object> reg = src._registeredModuleTypes;
        this._registeredModuleTypes = reg == null ? null : new LinkedHashSet<Object>(reg);
    }

    public ObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc) {
        SimpleMixInResolver mixins;
        if (jf == null) {
            this._jsonFactory = new MappingJsonFactory(this);
        } else {
            this._jsonFactory = jf;
            if (jf.getCodec() == null) {
                this._jsonFactory.setCodec(this);
            }
        }
        this._subtypeResolver = new StdSubtypeResolver();
        RootNameLookup rootNames = new RootNameLookup();
        this._typeFactory = TypeFactory.defaultInstance();
        this._mixIns = mixins = new SimpleMixInResolver(null);
        BaseSettings base = DEFAULT_BASE.withClassIntrospector(this.defaultClassIntrospector());
        this._configOverrides = new ConfigOverrides();
        this._serializationConfig = new SerializationConfig(base, this._subtypeResolver, mixins, rootNames, this._configOverrides);
        this._deserializationConfig = new DeserializationConfig(base, this._subtypeResolver, mixins, rootNames, this._configOverrides);
        boolean needOrder = this._jsonFactory.requiresPropertyOrdering();
        if (needOrder ^ this._serializationConfig.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)) {
            this.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, needOrder);
        }
        this._serializerProvider = sp == null ? new DefaultSerializerProvider.Impl() : sp;
        this._deserializationContext = dc == null ? new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance) : dc;
        this._serializerFactory = BeanSerializerFactory.instance;
    }

    protected ClassIntrospector defaultClassIntrospector() {
        return new BasicClassIntrospector();
    }

    public ObjectMapper copy() {
        this._checkInvalidCopy(ObjectMapper.class);
        return new ObjectMapper(this);
    }

    protected void _checkInvalidCopy(Class<?> exp) {
        if (this.getClass() != exp) {
            throw new IllegalStateException("Failed copy(): " + this.getClass().getName() + " (version: " + this.version() + ") does not override copy(); it has to");
        }
    }

    protected ObjectReader _newReader(DeserializationConfig config) {
        return new ObjectReader(this, config);
    }

    protected ObjectReader _newReader(DeserializationConfig config, JavaType valueType, Object valueToUpdate, FormatSchema schema, InjectableValues injectableValues) {
        return new ObjectReader(this, config, valueType, valueToUpdate, schema, injectableValues);
    }

    protected ObjectWriter _newWriter(SerializationConfig config) {
        return new ObjectWriter(this, config);
    }

    protected ObjectWriter _newWriter(SerializationConfig config, FormatSchema schema) {
        return new ObjectWriter(this, config, schema);
    }

    protected ObjectWriter _newWriter(SerializationConfig config, JavaType rootType, PrettyPrinter pp) {
        return new ObjectWriter(this, config, rootType, pp);
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    public ObjectMapper registerModule(Module module) {
        Object typeId;
        this._assertNotNull("module", module);
        String name = module.getModuleName();
        if (name == null) {
            throw new IllegalArgumentException("Module without defined name");
        }
        Version version = module.version();
        if (version == null) {
            throw new IllegalArgumentException("Module without defined version");
        }
        for (Module module2 : module.getDependencies()) {
            this.registerModule(module2);
        }
        if (this.isEnabled(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS) && (typeId = module.getTypeId()) != null) {
            if (this._registeredModuleTypes == null) {
                this._registeredModuleTypes = new LinkedHashSet<Object>();
            }
            if (!this._registeredModuleTypes.add(typeId)) {
                return this;
            }
        }
        module.setupModule(new Module.SetupContext(){

            @Override
            public Version getMapperVersion() {
                return ObjectMapper.this.version();
            }

            @Override
            public <C extends ObjectCodec> C getOwner() {
                return (C)ObjectMapper.this;
            }

            @Override
            public TypeFactory getTypeFactory() {
                return ObjectMapper.this._typeFactory;
            }

            @Override
            public boolean isEnabled(MapperFeature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public boolean isEnabled(DeserializationFeature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public boolean isEnabled(SerializationFeature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public boolean isEnabled(JsonFactory.Feature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public boolean isEnabled(JsonParser.Feature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public boolean isEnabled(JsonGenerator.Feature f) {
                return ObjectMapper.this.isEnabled(f);
            }

            @Override
            public MutableConfigOverride configOverride(Class<?> type) {
                return ObjectMapper.this.configOverride(type);
            }

            @Override
            public void addDeserializers(Deserializers d) {
                DeserializerFactory df = ObjectMapper.this._deserializationContext._factory.withAdditionalDeserializers(d);
                ObjectMapper.this._deserializationContext = ObjectMapper.this._deserializationContext.with(df);
            }

            @Override
            public void addKeyDeserializers(KeyDeserializers d) {
                DeserializerFactory df = ObjectMapper.this._deserializationContext._factory.withAdditionalKeyDeserializers(d);
                ObjectMapper.this._deserializationContext = ObjectMapper.this._deserializationContext.with(df);
            }

            @Override
            public void addBeanDeserializerModifier(BeanDeserializerModifier modifier) {
                DeserializerFactory df = ObjectMapper.this._deserializationContext._factory.withDeserializerModifier(modifier);
                ObjectMapper.this._deserializationContext = ObjectMapper.this._deserializationContext.with(df);
            }

            @Override
            public void addSerializers(Serializers s) {
                ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withAdditionalSerializers(s);
            }

            @Override
            public void addKeySerializers(Serializers s) {
                ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withAdditionalKeySerializers(s);
            }

            @Override
            public void addBeanSerializerModifier(BeanSerializerModifier modifier) {
                ObjectMapper.this._serializerFactory = ObjectMapper.this._serializerFactory.withSerializerModifier(modifier);
            }

            @Override
            public void addAbstractTypeResolver(AbstractTypeResolver resolver) {
                DeserializerFactory df = ObjectMapper.this._deserializationContext._factory.withAbstractTypeResolver(resolver);
                ObjectMapper.this._deserializationContext = ObjectMapper.this._deserializationContext.with(df);
            }

            @Override
            public void addTypeModifier(TypeModifier modifier) {
                TypeFactory f = ObjectMapper.this._typeFactory;
                f = f.withModifier(modifier);
                ObjectMapper.this.setTypeFactory(f);
            }

            @Override
            public void addValueInstantiators(ValueInstantiators instantiators) {
                DeserializerFactory df = ObjectMapper.this._deserializationContext._factory.withValueInstantiators(instantiators);
                ObjectMapper.this._deserializationContext = ObjectMapper.this._deserializationContext.with(df);
            }

            @Override
            public void setClassIntrospector(ClassIntrospector ci) {
                ObjectMapper.this._deserializationConfig = (DeserializationConfig)ObjectMapper.this._deserializationConfig.with(ci);
                ObjectMapper.this._serializationConfig = (SerializationConfig)ObjectMapper.this._serializationConfig.with(ci);
            }

            @Override
            public void insertAnnotationIntrospector(AnnotationIntrospector ai) {
                ObjectMapper.this._deserializationConfig = (DeserializationConfig)ObjectMapper.this._deserializationConfig.withInsertedAnnotationIntrospector(ai);
                ObjectMapper.this._serializationConfig = (SerializationConfig)ObjectMapper.this._serializationConfig.withInsertedAnnotationIntrospector(ai);
            }

            @Override
            public void appendAnnotationIntrospector(AnnotationIntrospector ai) {
                ObjectMapper.this._deserializationConfig = (DeserializationConfig)ObjectMapper.this._deserializationConfig.withAppendedAnnotationIntrospector(ai);
                ObjectMapper.this._serializationConfig = (SerializationConfig)ObjectMapper.this._serializationConfig.withAppendedAnnotationIntrospector(ai);
            }

            @Override
            public void registerSubtypes(Class<?> ... subtypes) {
                ObjectMapper.this.registerSubtypes(subtypes);
            }

            @Override
            public void registerSubtypes(NamedType ... subtypes) {
                ObjectMapper.this.registerSubtypes(subtypes);
            }

            @Override
            public void registerSubtypes(Collection<Class<?>> subtypes) {
                ObjectMapper.this.registerSubtypes(subtypes);
            }

            @Override
            public void setMixInAnnotations(Class<?> target, Class<?> mixinSource) {
                ObjectMapper.this.addMixIn(target, mixinSource);
            }

            @Override
            public void addDeserializationProblemHandler(DeserializationProblemHandler handler) {
                ObjectMapper.this.addHandler(handler);
            }

            @Override
            public void setNamingStrategy(PropertyNamingStrategy naming) {
                ObjectMapper.this.setPropertyNamingStrategy(naming);
            }
        });
        return this;
    }

    public ObjectMapper registerModules(Module ... modules) {
        for (Module module : modules) {
            this.registerModule(module);
        }
        return this;
    }

    public ObjectMapper registerModules(Iterable<? extends Module> modules) {
        this._assertNotNull("modules", modules);
        for (Module module : modules) {
            this.registerModule(module);
        }
        return this;
    }

    public Set<Object> getRegisteredModuleIds() {
        return this._registeredModuleTypes == null ? Collections.emptySet() : Collections.unmodifiableSet(this._registeredModuleTypes);
    }

    public static List<Module> findModules() {
        return ObjectMapper.findModules(null);
    }

    public static List<Module> findModules(ClassLoader classLoader) {
        ArrayList<Module> modules = new ArrayList<Module>();
        ServiceLoader<Module> loader = ObjectMapper.secureGetServiceLoader(Module.class, classLoader);
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

    public ObjectMapper findAndRegisterModules() {
        return this.registerModules(ObjectMapper.findModules());
    }

    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        this._assertNotNull("out", out);
        JsonGenerator g = this._jsonFactory.createGenerator(out, JsonEncoding.UTF8);
        this._serializationConfig.initialize(g);
        return g;
    }

    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        this._assertNotNull("out", out);
        JsonGenerator g = this._jsonFactory.createGenerator(out, enc);
        this._serializationConfig.initialize(g);
        return g;
    }

    public JsonGenerator createGenerator(Writer w) throws IOException {
        this._assertNotNull("w", w);
        JsonGenerator g = this._jsonFactory.createGenerator(w);
        this._serializationConfig.initialize(g);
        return g;
    }

    public JsonGenerator createGenerator(File outputFile, JsonEncoding enc) throws IOException {
        this._assertNotNull("outputFile", outputFile);
        JsonGenerator g = this._jsonFactory.createGenerator(outputFile, enc);
        this._serializationConfig.initialize(g);
        return g;
    }

    public JsonGenerator createGenerator(DataOutput out) throws IOException {
        this._assertNotNull("out", out);
        JsonGenerator g = this._jsonFactory.createGenerator(out);
        this._serializationConfig.initialize(g);
        return g;
    }

    public JsonParser createParser(File src) throws IOException {
        this._assertNotNull("src", src);
        JsonParser p = this._jsonFactory.createParser(src);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(URL src) throws IOException {
        this._assertNotNull("src", src);
        JsonParser p = this._jsonFactory.createParser(src);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(InputStream in) throws IOException {
        this._assertNotNull("in", in);
        JsonParser p = this._jsonFactory.createParser(in);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(Reader r) throws IOException {
        this._assertNotNull("r", r);
        JsonParser p = this._jsonFactory.createParser(r);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(byte[] content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(byte[] content, int offset, int len) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content, offset, len);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(String content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(char[] content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(char[] content, int offset, int len) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content, offset, len);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createParser(DataInput content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._jsonFactory.createParser(content);
        this._deserializationConfig.initialize(p);
        return p;
    }

    public JsonParser createNonBlockingByteArrayParser() throws IOException {
        JsonParser p = this._jsonFactory.createNonBlockingByteArrayParser();
        this._deserializationConfig.initialize(p);
        return p;
    }

    public SerializationConfig getSerializationConfig() {
        return this._serializationConfig;
    }

    public DeserializationConfig getDeserializationConfig() {
        return this._deserializationConfig;
    }

    public DeserializationContext getDeserializationContext() {
        return this._deserializationContext;
    }

    public ObjectMapper setSerializerFactory(SerializerFactory f) {
        this._serializerFactory = f;
        return this;
    }

    public SerializerFactory getSerializerFactory() {
        return this._serializerFactory;
    }

    public ObjectMapper setSerializerProvider(DefaultSerializerProvider p) {
        this._serializerProvider = p;
        return this;
    }

    public SerializerProvider getSerializerProvider() {
        return this._serializerProvider;
    }

    public SerializerProvider getSerializerProviderInstance() {
        return this._serializerProvider(this._serializationConfig);
    }

    public ObjectMapper setMixIns(Map<Class<?>, Class<?>> sourceMixins) {
        this._mixIns.setLocalDefinitions(sourceMixins);
        return this;
    }

    public ObjectMapper addMixIn(Class<?> target, Class<?> mixinSource) {
        this._mixIns.addLocalDefinition(target, mixinSource);
        return this;
    }

    public ObjectMapper setMixInResolver(ClassIntrospector.MixInResolver resolver) {
        SimpleMixInResolver r = this._mixIns.withOverrides(resolver);
        if (r != this._mixIns) {
            this._mixIns = r;
            this._deserializationConfig = new DeserializationConfig(this._deserializationConfig, r);
            this._serializationConfig = new SerializationConfig(this._serializationConfig, r);
        }
        return this;
    }

    public Class<?> findMixInClassFor(Class<?> cls) {
        return this._mixIns.findMixInClassFor(cls);
    }

    public int mixInCount() {
        return this._mixIns.localSize();
    }

    @Deprecated
    public void setMixInAnnotations(Map<Class<?>, Class<?>> sourceMixins) {
        this.setMixIns(sourceMixins);
    }

    @Deprecated
    public final void addMixInAnnotations(Class<?> target, Class<?> mixinSource) {
        this.addMixIn(target, mixinSource);
    }

    public VisibilityChecker<?> getVisibilityChecker() {
        return this._serializationConfig.getDefaultVisibilityChecker();
    }

    public ObjectMapper setVisibility(VisibilityChecker<?> vc) {
        this._configOverrides.setDefaultVisibility(vc);
        return this;
    }

    public ObjectMapper setVisibility(PropertyAccessor forMethod, JsonAutoDetect.Visibility visibility) {
        VisibilityChecker<?> vc = this._configOverrides.getDefaultVisibility();
        vc = vc.withVisibility(forMethod, visibility);
        this._configOverrides.setDefaultVisibility(vc);
        return this;
    }

    public SubtypeResolver getSubtypeResolver() {
        return this._subtypeResolver;
    }

    public ObjectMapper setSubtypeResolver(SubtypeResolver str) {
        this._subtypeResolver = str;
        this._deserializationConfig = this._deserializationConfig.with(str);
        this._serializationConfig = this._serializationConfig.with(str);
        return this;
    }

    public ObjectMapper setAnnotationIntrospector(AnnotationIntrospector ai) {
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(ai);
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(ai);
        return this;
    }

    public ObjectMapper setAnnotationIntrospectors(AnnotationIntrospector serializerAI, AnnotationIntrospector deserializerAI) {
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(serializerAI);
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(deserializerAI);
        return this;
    }

    public ObjectMapper setPropertyNamingStrategy(PropertyNamingStrategy s) {
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(s);
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(s);
        return this;
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return this._serializationConfig.getPropertyNamingStrategy();
    }

    public ObjectMapper setDefaultPrettyPrinter(PrettyPrinter pp) {
        this._serializationConfig = this._serializationConfig.withDefaultPrettyPrinter(pp);
        return this;
    }

    @Deprecated
    public void setVisibilityChecker(VisibilityChecker<?> vc) {
        this.setVisibility(vc);
    }

    public ObjectMapper setPolymorphicTypeValidator(PolymorphicTypeValidator ptv) {
        BaseSettings s = this._deserializationConfig.getBaseSettings().with(ptv);
        this._deserializationConfig = this._deserializationConfig._withBase(s);
        return this;
    }

    public PolymorphicTypeValidator getPolymorphicTypeValidator() {
        return this._deserializationConfig.getBaseSettings().getPolymorphicTypeValidator();
    }

    public ObjectMapper setSerializationInclusion(JsonInclude.Include incl) {
        this.setPropertyInclusion(JsonInclude.Value.construct(incl, incl));
        return this;
    }

    @Deprecated
    public ObjectMapper setPropertyInclusion(JsonInclude.Value incl) {
        return this.setDefaultPropertyInclusion(incl);
    }

    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Value incl) {
        this._configOverrides.setDefaultInclusion(incl);
        return this;
    }

    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Include incl) {
        this._configOverrides.setDefaultInclusion(JsonInclude.Value.construct(incl, incl));
        return this;
    }

    public ObjectMapper setDefaultSetterInfo(JsonSetter.Value v) {
        this._configOverrides.setDefaultSetterInfo(v);
        return this;
    }

    public ObjectMapper setDefaultVisibility(JsonAutoDetect.Value vis) {
        this._configOverrides.setDefaultVisibility(VisibilityChecker.Std.construct(vis));
        return this;
    }

    public ObjectMapper setDefaultMergeable(Boolean b) {
        this._configOverrides.setDefaultMergeable(b);
        return this;
    }

    public ObjectMapper setDefaultLeniency(Boolean b) {
        this._configOverrides.setDefaultLeniency(b);
        return this;
    }

    public void registerSubtypes(Class<?> ... classes) {
        this.getSubtypeResolver().registerSubtypes(classes);
    }

    public void registerSubtypes(NamedType ... types) {
        this.getSubtypeResolver().registerSubtypes(types);
    }

    public void registerSubtypes(Collection<Class<?>> subtypes) {
        this.getSubtypeResolver().registerSubtypes(subtypes);
    }

    public ObjectMapper activateDefaultTyping(PolymorphicTypeValidator ptv) {
        return this.activateDefaultTyping(ptv, DefaultTyping.OBJECT_AND_NON_CONCRETE);
    }

    public ObjectMapper activateDefaultTyping(PolymorphicTypeValidator ptv, DefaultTyping applicability) {
        return this.activateDefaultTyping(ptv, applicability, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    public ObjectMapper activateDefaultTyping(PolymorphicTypeValidator ptv, DefaultTyping applicability, JsonTypeInfo.As includeAs) {
        if (includeAs == JsonTypeInfo.As.EXTERNAL_PROPERTY) {
            throw new IllegalArgumentException("Cannot use includeAs of " + (Object)((Object)includeAs));
        }
        TypeResolverBuilder<?> typer = this._constructDefaultTypeResolverBuilder(applicability, ptv);
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(includeAs);
        return this.setDefaultTyping(typer);
    }

    public ObjectMapper activateDefaultTypingAsProperty(PolymorphicTypeValidator ptv, DefaultTyping applicability, String propertyName) {
        TypeResolverBuilder<?> typer = this._constructDefaultTypeResolverBuilder(applicability, this.getPolymorphicTypeValidator());
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        typer = typer.typeProperty(propertyName);
        return this.setDefaultTyping(typer);
    }

    public ObjectMapper deactivateDefaultTyping() {
        return this.setDefaultTyping(null);
    }

    public ObjectMapper setDefaultTyping(TypeResolverBuilder<?> typer) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(typer);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(typer);
        return this;
    }

    @Deprecated
    public ObjectMapper enableDefaultTyping() {
        return this.activateDefaultTyping(this.getPolymorphicTypeValidator());
    }

    @Deprecated
    public ObjectMapper enableDefaultTyping(DefaultTyping dti) {
        return this.enableDefaultTyping(dti, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    @Deprecated
    public ObjectMapper enableDefaultTyping(DefaultTyping applicability, JsonTypeInfo.As includeAs) {
        return this.activateDefaultTyping(this.getPolymorphicTypeValidator(), applicability, includeAs);
    }

    @Deprecated
    public ObjectMapper enableDefaultTypingAsProperty(DefaultTyping applicability, String propertyName) {
        return this.activateDefaultTypingAsProperty(this.getPolymorphicTypeValidator(), applicability, propertyName);
    }

    @Deprecated
    public ObjectMapper disableDefaultTyping() {
        return this.setDefaultTyping(null);
    }

    public MutableConfigOverride configOverride(Class<?> type) {
        return this._configOverrides.findOrCreateOverride(type);
    }

    public TypeFactory getTypeFactory() {
        return this._typeFactory;
    }

    public ObjectMapper setTypeFactory(TypeFactory f) {
        this._typeFactory = f;
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(f);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(f);
        return this;
    }

    public JavaType constructType(Type t) {
        this._assertNotNull("t", t);
        return this._typeFactory.constructType(t);
    }

    public JsonNodeFactory getNodeFactory() {
        return this._deserializationConfig.getNodeFactory();
    }

    public ObjectMapper setNodeFactory(JsonNodeFactory f) {
        this._deserializationConfig = this._deserializationConfig.with(f);
        return this;
    }

    public ObjectMapper addHandler(DeserializationProblemHandler h) {
        this._deserializationConfig = this._deserializationConfig.withHandler(h);
        return this;
    }

    public ObjectMapper clearProblemHandlers() {
        this._deserializationConfig = this._deserializationConfig.withNoProblemHandlers();
        return this;
    }

    public ObjectMapper setConfig(DeserializationConfig config) {
        this._assertNotNull("config", config);
        this._deserializationConfig = config;
        return this;
    }

    @Deprecated
    public void setFilters(FilterProvider filterProvider) {
        this._serializationConfig = this._serializationConfig.withFilters(filterProvider);
    }

    public ObjectMapper setFilterProvider(FilterProvider filterProvider) {
        this._serializationConfig = this._serializationConfig.withFilters(filterProvider);
        return this;
    }

    public ObjectMapper setBase64Variant(Base64Variant v) {
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(v);
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(v);
        return this;
    }

    public ObjectMapper setConfig(SerializationConfig config) {
        this._assertNotNull("config", config);
        this._serializationConfig = config;
        return this;
    }

    public JsonFactory tokenStreamFactory() {
        return this._jsonFactory;
    }

    @Override
    public JsonFactory getFactory() {
        return this._jsonFactory;
    }

    @Override
    @Deprecated
    public JsonFactory getJsonFactory() {
        return this.getFactory();
    }

    public ObjectMapper setDateFormat(DateFormat dateFormat) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(dateFormat);
        this._serializationConfig = this._serializationConfig.with(dateFormat);
        return this;
    }

    public DateFormat getDateFormat() {
        return this._serializationConfig.getDateFormat();
    }

    public Object setHandlerInstantiator(HandlerInstantiator hi) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(hi);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(hi);
        return this;
    }

    public ObjectMapper setInjectableValues(InjectableValues injectableValues) {
        this._injectableValues = injectableValues;
        return this;
    }

    public InjectableValues getInjectableValues() {
        return this._injectableValues;
    }

    public ObjectMapper setLocale(Locale l) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(l);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(l);
        return this;
    }

    public ObjectMapper setTimeZone(TimeZone tz) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(tz);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(tz);
        return this;
    }

    public boolean isEnabled(MapperFeature f) {
        return this._serializationConfig.isEnabled(f);
    }

    public ObjectMapper configure(MapperFeature f, boolean state) {
        this._serializationConfig = state ? (SerializationConfig)this._serializationConfig.with(new MapperFeature[]{f}) : (SerializationConfig)this._serializationConfig.without(new MapperFeature[]{f});
        this._deserializationConfig = state ? (DeserializationConfig)this._deserializationConfig.with(new MapperFeature[]{f}) : (DeserializationConfig)this._deserializationConfig.without(new MapperFeature[]{f});
        return this;
    }

    public ObjectMapper enable(MapperFeature ... f) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.with(f);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.with(f);
        return this;
    }

    public ObjectMapper disable(MapperFeature ... f) {
        this._deserializationConfig = (DeserializationConfig)this._deserializationConfig.without(f);
        this._serializationConfig = (SerializationConfig)this._serializationConfig.without(f);
        return this;
    }

    public boolean isEnabled(SerializationFeature f) {
        return this._serializationConfig.isEnabled(f);
    }

    public ObjectMapper configure(SerializationFeature f, boolean state) {
        this._serializationConfig = state ? this._serializationConfig.with(f) : this._serializationConfig.without(f);
        return this;
    }

    public ObjectMapper enable(SerializationFeature f) {
        this._serializationConfig = this._serializationConfig.with(f);
        return this;
    }

    public ObjectMapper enable(SerializationFeature first, SerializationFeature ... f) {
        this._serializationConfig = this._serializationConfig.with(first, f);
        return this;
    }

    public ObjectMapper disable(SerializationFeature f) {
        this._serializationConfig = this._serializationConfig.without(f);
        return this;
    }

    public ObjectMapper disable(SerializationFeature first, SerializationFeature ... f) {
        this._serializationConfig = this._serializationConfig.without(first, f);
        return this;
    }

    public boolean isEnabled(DeserializationFeature f) {
        return this._deserializationConfig.isEnabled(f);
    }

    public ObjectMapper configure(DeserializationFeature f, boolean state) {
        this._deserializationConfig = state ? this._deserializationConfig.with(f) : this._deserializationConfig.without(f);
        return this;
    }

    public ObjectMapper enable(DeserializationFeature feature) {
        this._deserializationConfig = this._deserializationConfig.with(feature);
        return this;
    }

    public ObjectMapper enable(DeserializationFeature first, DeserializationFeature ... f) {
        this._deserializationConfig = this._deserializationConfig.with(first, f);
        return this;
    }

    public ObjectMapper disable(DeserializationFeature feature) {
        this._deserializationConfig = this._deserializationConfig.without(feature);
        return this;
    }

    public ObjectMapper disable(DeserializationFeature first, DeserializationFeature ... f) {
        this._deserializationConfig = this._deserializationConfig.without(first, f);
        return this;
    }

    public boolean isEnabled(JsonParser.Feature f) {
        return this._deserializationConfig.isEnabled(f, this._jsonFactory);
    }

    public ObjectMapper configure(JsonParser.Feature f, boolean state) {
        this._jsonFactory.configure(f, state);
        return this;
    }

    public ObjectMapper enable(JsonParser.Feature ... features) {
        for (JsonParser.Feature f : features) {
            this._jsonFactory.enable(f);
        }
        return this;
    }

    public ObjectMapper disable(JsonParser.Feature ... features) {
        for (JsonParser.Feature f : features) {
            this._jsonFactory.disable(f);
        }
        return this;
    }

    public boolean isEnabled(JsonGenerator.Feature f) {
        return this._serializationConfig.isEnabled(f, this._jsonFactory);
    }

    public ObjectMapper configure(JsonGenerator.Feature f, boolean state) {
        this._jsonFactory.configure(f, state);
        return this;
    }

    public ObjectMapper enable(JsonGenerator.Feature ... features) {
        for (JsonGenerator.Feature f : features) {
            this._jsonFactory.enable(f);
        }
        return this;
    }

    public ObjectMapper disable(JsonGenerator.Feature ... features) {
        for (JsonGenerator.Feature f : features) {
            this._jsonFactory.disable(f);
        }
        return this;
    }

    public boolean isEnabled(JsonFactory.Feature f) {
        return this._jsonFactory.isEnabled(f);
    }

    public boolean isEnabled(StreamReadFeature f) {
        return this.isEnabled(f.mappedFeature());
    }

    public boolean isEnabled(StreamWriteFeature f) {
        return this.isEnabled(f.mappedFeature());
    }

    @Override
    public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("p", p);
        return (T)this._readValue(this.getDeserializationConfig(), p, this._typeFactory.constructType(valueType));
    }

    @Override
    public <T> T readValue(JsonParser p, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("p", p);
        return (T)this._readValue(this.getDeserializationConfig(), p, this._typeFactory.constructType(valueTypeRef));
    }

    @Override
    public final <T> T readValue(JsonParser p, ResolvedType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("p", p);
        return (T)this._readValue(this.getDeserializationConfig(), p, (JavaType)valueType);
    }

    public <T> T readValue(JsonParser p, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("p", p);
        return (T)this._readValue(this.getDeserializationConfig(), p, valueType);
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException, JsonProcessingException {
        this._assertNotNull("p", p);
        DeserializationConfig cfg = this.getDeserializationConfig();
        JsonToken t = p.getCurrentToken();
        if (t == null && (t = p.nextToken()) == null) {
            return null;
        }
        JsonNode n = (JsonNode)this._readValue(cfg, p, this.constructType((Type)((Object)JsonNode.class)));
        if (n == null) {
            n = this.getNodeFactory().nullNode();
        }
        JsonNode result = n;
        return (T)result;
    }

    public <T> MappingIterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException, JsonProcessingException {
        return this.readValues(p, (JavaType)valueType);
    }

    public <T> MappingIterator<T> readValues(JsonParser p, JavaType valueType) throws IOException, JsonProcessingException {
        this._assertNotNull("p", p);
        DeserializationConfig config = this.getDeserializationConfig();
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p, config);
        JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt, valueType);
        return new MappingIterator(valueType, p, ctxt, deser, false, null);
    }

    public <T> MappingIterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException, JsonProcessingException {
        return this.readValues(p, this._typeFactory.constructType(valueType));
    }

    public <T> MappingIterator<T> readValues(JsonParser p, TypeReference<T> valueTypeRef) throws IOException, JsonProcessingException {
        return this.readValues(p, this._typeFactory.constructType(valueTypeRef));
    }

    public JsonNode readTree(InputStream in) throws IOException {
        this._assertNotNull("in", in);
        return this._readTreeAndClose(this._jsonFactory.createParser(in));
    }

    public JsonNode readTree(Reader r) throws IOException {
        this._assertNotNull("r", r);
        return this._readTreeAndClose(this._jsonFactory.createParser(r));
    }

    public JsonNode readTree(String content) throws JsonProcessingException, JsonMappingException {
        this._assertNotNull("content", content);
        try {
            return this._readTreeAndClose(this._jsonFactory.createParser(content));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    public JsonNode readTree(byte[] content) throws IOException {
        this._assertNotNull("content", content);
        return this._readTreeAndClose(this._jsonFactory.createParser(content));
    }

    public JsonNode readTree(byte[] content, int offset, int len) throws IOException {
        this._assertNotNull("content", content);
        return this._readTreeAndClose(this._jsonFactory.createParser(content, offset, len));
    }

    public JsonNode readTree(File file) throws IOException, JsonProcessingException {
        this._assertNotNull("file", file);
        return this._readTreeAndClose(this._jsonFactory.createParser(file));
    }

    public JsonNode readTree(URL source) throws IOException {
        this._assertNotNull("source", source);
        return this._readTreeAndClose(this._jsonFactory.createParser(source));
    }

    @Override
    public void writeValue(JsonGenerator g, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._assertNotNull("g", g);
        SerializationConfig config = this.getSerializationConfig();
        if (config.isEnabled(SerializationFeature.INDENT_OUTPUT) && g.getPrettyPrinter() == null) {
            g.setPrettyPrinter(config.constructDefaultPrettyPrinter());
        }
        if (config.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._writeCloseableValue(g, value, config);
        } else {
            this._serializerProvider(config).serializeValue(g, value);
            if (config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
                g.flush();
            }
        }
    }

    @Override
    public void writeTree(JsonGenerator g, TreeNode rootNode) throws IOException, JsonProcessingException {
        this._assertNotNull("g", g);
        SerializationConfig config = this.getSerializationConfig();
        this._serializerProvider(config).serializeValue(g, rootNode);
        if (config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
            g.flush();
        }
    }

    public void writeTree(JsonGenerator g, JsonNode rootNode) throws IOException, JsonProcessingException {
        this._assertNotNull("g", g);
        SerializationConfig config = this.getSerializationConfig();
        this._serializerProvider(config).serializeValue(g, rootNode);
        if (config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
            g.flush();
        }
    }

    @Override
    public ObjectNode createObjectNode() {
        return this._deserializationConfig.getNodeFactory().objectNode();
    }

    @Override
    public ArrayNode createArrayNode() {
        return this._deserializationConfig.getNodeFactory().arrayNode();
    }

    @Override
    public JsonNode missingNode() {
        return this._deserializationConfig.getNodeFactory().missingNode();
    }

    @Override
    public JsonNode nullNode() {
        return this._deserializationConfig.getNodeFactory().nullNode();
    }

    @Override
    public JsonParser treeAsTokens(TreeNode n) {
        this._assertNotNull("n", n);
        return new TreeTraversingParser((JsonNode)n, this);
    }

    @Override
    public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException {
        if (n == null) {
            return null;
        }
        try {
            Object ob;
            if (TreeNode.class.isAssignableFrom(valueType) && valueType.isAssignableFrom(n.getClass())) {
                return (T)n;
            }
            JsonToken tt = n.asToken();
            if (tt == JsonToken.VALUE_NULL) {
                return null;
            }
            if (tt == JsonToken.VALUE_EMBEDDED_OBJECT && n instanceof POJONode && ((ob = ((POJONode)n).getPojo()) == null || valueType.isInstance(ob))) {
                return (T)ob;
            }
            return this.readValue(this.treeAsTokens(n), valueType);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public <T extends JsonNode> T valueToTree(Object fromValue) throws IllegalArgumentException {
        JsonNode result;
        if (fromValue == null) {
            return (T)this.getNodeFactory().nullNode();
        }
        TokenBuffer buf = new TokenBuffer(this, false);
        if (this.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            buf = buf.forceUseOfBigDecimal(true);
        }
        try {
            this.writeValue(buf, fromValue);
            JsonParser p = buf.asParser();
            result = (JsonNode)this.readTree(p);
            p.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T)result;
    }

    public boolean canSerialize(Class<?> type) {
        return this._serializerProvider(this.getSerializationConfig()).hasSerializerFor(type, null);
    }

    public boolean canSerialize(Class<?> type, AtomicReference<Throwable> cause) {
        return this._serializerProvider(this.getSerializationConfig()).hasSerializerFor(type, cause);
    }

    public boolean canDeserialize(JavaType type) {
        return this.createDeserializationContext(null, this.getDeserializationConfig()).hasValueDeserializerFor(type, null);
    }

    public boolean canDeserialize(JavaType type, AtomicReference<Throwable> cause) {
        return this.createDeserializationContext(null, this.getDeserializationConfig()).hasValueDeserializerFor(type, cause);
    }

    public <T> T readValue(File src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(File src, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(File src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public <T> T readValue(URL src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(URL src, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(URL src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public <T> T readValue(String content, Class<T> valueType) throws JsonProcessingException, JsonMappingException {
        this._assertNotNull("content", content);
        return this.readValue(content, this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(String content, TypeReference<T> valueTypeRef) throws JsonProcessingException, JsonMappingException {
        this._assertNotNull("content", content);
        return this.readValue(content, this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(String content, JavaType valueType) throws JsonProcessingException, JsonMappingException {
        this._assertNotNull("content", content);
        try {
            return (T)this._readMapAndClose(this._jsonFactory.createParser(content), valueType);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    public <T> T readValue(Reader src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(Reader src, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(Reader src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public <T> T readValue(InputStream src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(InputStream src, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(InputStream src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(byte[] src, int offset, int len, Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src, offset, len), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(byte[] src, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(byte[] src, int offset, int len, TypeReference<T> valueTypeRef) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src, offset, len), this._typeFactory.constructType(valueTypeRef));
    }

    public <T> T readValue(byte[] src, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public <T> T readValue(byte[] src, int offset, int len, JavaType valueType) throws IOException, JsonParseException, JsonMappingException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src, offset, len), valueType);
    }

    public <T> T readValue(DataInput src, Class<T> valueType) throws IOException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), this._typeFactory.constructType(valueType));
    }

    public <T> T readValue(DataInput src, JavaType valueType) throws IOException {
        this._assertNotNull("src", src);
        return (T)this._readMapAndClose(this._jsonFactory.createParser(src), valueType);
    }

    public void writeValue(File resultFile, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._writeValueAndClose(this.createGenerator(resultFile, JsonEncoding.UTF8), value);
    }

    public void writeValue(OutputStream out, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._writeValueAndClose(this.createGenerator(out, JsonEncoding.UTF8), value);
    }

    public void writeValue(DataOutput out, Object value) throws IOException {
        this._writeValueAndClose(this.createGenerator(out), value);
    }

    public void writeValue(Writer w, Object value) throws IOException, JsonGenerationException, JsonMappingException {
        this._writeValueAndClose(this.createGenerator(w), value);
    }

    public String writeValueAsString(Object value) throws JsonProcessingException {
        SegmentedStringWriter sw = new SegmentedStringWriter(this._jsonFactory._getBufferRecycler());
        try {
            this._writeValueAndClose(this.createGenerator(sw), value);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        ByteArrayBuilder bb = new ByteArrayBuilder(this._jsonFactory._getBufferRecycler());
        try {
            this._writeValueAndClose(this.createGenerator(bb, JsonEncoding.UTF8), value);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
        byte[] result = bb.toByteArray();
        bb.release();
        return result;
    }

    public ObjectWriter writer() {
        return this._newWriter(this.getSerializationConfig());
    }

    public ObjectWriter writer(SerializationFeature feature) {
        return this._newWriter(this.getSerializationConfig().with(feature));
    }

    public ObjectWriter writer(SerializationFeature first, SerializationFeature ... other) {
        return this._newWriter(this.getSerializationConfig().with(first, other));
    }

    public ObjectWriter writer(DateFormat df) {
        return this._newWriter(this.getSerializationConfig().with(df));
    }

    public ObjectWriter writerWithView(Class<?> serializationView) {
        return this._newWriter((SerializationConfig)this.getSerializationConfig().withView((Class)serializationView));
    }

    public ObjectWriter writerFor(Class<?> rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType == null ? null : this._typeFactory.constructType(rootType), null);
    }

    public ObjectWriter writerFor(TypeReference<?> rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType == null ? null : this._typeFactory.constructType(rootType), null);
    }

    public ObjectWriter writerFor(JavaType rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType, null);
    }

    public ObjectWriter writer(PrettyPrinter pp) {
        if (pp == null) {
            pp = ObjectWriter.NULL_PRETTY_PRINTER;
        }
        return this._newWriter(this.getSerializationConfig(), null, pp);
    }

    public ObjectWriter writerWithDefaultPrettyPrinter() {
        SerializationConfig config = this.getSerializationConfig();
        return this._newWriter(config, null, config.getDefaultPrettyPrinter());
    }

    public ObjectWriter writer(FilterProvider filterProvider) {
        return this._newWriter(this.getSerializationConfig().withFilters(filterProvider));
    }

    public ObjectWriter writer(FormatSchema schema) {
        this._verifySchemaType(schema);
        return this._newWriter(this.getSerializationConfig(), schema);
    }

    public ObjectWriter writer(Base64Variant defaultBase64) {
        return this._newWriter((SerializationConfig)this.getSerializationConfig().with(defaultBase64));
    }

    public ObjectWriter writer(CharacterEscapes escapes) {
        return this._newWriter(this.getSerializationConfig()).with(escapes);
    }

    public ObjectWriter writer(ContextAttributes attrs) {
        return this._newWriter(this.getSerializationConfig().with(attrs));
    }

    @Deprecated
    public ObjectWriter writerWithType(Class<?> rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType == null ? null : this._typeFactory.constructType(rootType), null);
    }

    @Deprecated
    public ObjectWriter writerWithType(TypeReference<?> rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType == null ? null : this._typeFactory.constructType(rootType), null);
    }

    @Deprecated
    public ObjectWriter writerWithType(JavaType rootType) {
        return this._newWriter(this.getSerializationConfig(), rootType, null);
    }

    public ObjectReader reader() {
        return this._newReader(this.getDeserializationConfig()).with(this._injectableValues);
    }

    public ObjectReader reader(DeserializationFeature feature) {
        return this._newReader(this.getDeserializationConfig().with(feature));
    }

    public ObjectReader reader(DeserializationFeature first, DeserializationFeature ... other) {
        return this._newReader(this.getDeserializationConfig().with(first, other));
    }

    public ObjectReader readerForUpdating(Object valueToUpdate) {
        JavaType t = this._typeFactory.constructType(valueToUpdate.getClass());
        return this._newReader(this.getDeserializationConfig(), t, valueToUpdate, null, this._injectableValues);
    }

    public ObjectReader readerFor(JavaType type) {
        return this._newReader(this.getDeserializationConfig(), type, null, null, this._injectableValues);
    }

    public ObjectReader readerFor(Class<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructType(type), null, null, this._injectableValues);
    }

    public ObjectReader readerFor(TypeReference<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructType(type), null, null, this._injectableValues);
    }

    public ObjectReader readerForArrayOf(Class<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructArrayType(type), null, null, this._injectableValues);
    }

    public ObjectReader readerForListOf(Class<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructCollectionType(List.class, type), null, null, this._injectableValues);
    }

    public ObjectReader readerForMapOf(Class<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructMapType(Map.class, String.class, type), null, null, this._injectableValues);
    }

    public ObjectReader reader(JsonNodeFactory f) {
        return this._newReader(this.getDeserializationConfig()).with(f);
    }

    public ObjectReader reader(FormatSchema schema) {
        this._verifySchemaType(schema);
        return this._newReader(this.getDeserializationConfig(), null, null, schema, this._injectableValues);
    }

    public ObjectReader reader(InjectableValues injectableValues) {
        return this._newReader(this.getDeserializationConfig(), null, null, null, injectableValues);
    }

    public ObjectReader readerWithView(Class<?> view) {
        return this._newReader((DeserializationConfig)this.getDeserializationConfig().withView((Class)view));
    }

    public ObjectReader reader(Base64Variant defaultBase64) {
        return this._newReader((DeserializationConfig)this.getDeserializationConfig().with(defaultBase64));
    }

    public ObjectReader reader(ContextAttributes attrs) {
        return this._newReader(this.getDeserializationConfig().with(attrs));
    }

    @Deprecated
    public ObjectReader reader(JavaType type) {
        return this._newReader(this.getDeserializationConfig(), type, null, null, this._injectableValues);
    }

    @Deprecated
    public ObjectReader reader(Class<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructType(type), null, null, this._injectableValues);
    }

    @Deprecated
    public ObjectReader reader(TypeReference<?> type) {
        return this._newReader(this.getDeserializationConfig(), this._typeFactory.constructType(type), null, null, this._injectableValues);
    }

    public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
        return (T)this._convert(fromValue, this._typeFactory.constructType(toValueType));
    }

    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) throws IllegalArgumentException {
        return (T)this._convert(fromValue, this._typeFactory.constructType(toValueTypeRef));
    }

    public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
        return (T)this._convert(fromValue, toValueType);
    }

    protected Object _convert(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
        TokenBuffer buf = new TokenBuffer(this, false);
        if (this.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            buf = buf.forceUseOfBigDecimal(true);
        }
        try {
            Object result;
            SerializationConfig config = this.getSerializationConfig().without(SerializationFeature.WRAP_ROOT_VALUE);
            this._serializerProvider(config).serializeValue(buf, fromValue);
            JsonParser p = buf.asParser();
            DeserializationConfig deserConfig = this.getDeserializationConfig();
            JsonToken t = this._initForReading(p, toValueType);
            if (t == JsonToken.VALUE_NULL) {
                DefaultDeserializationContext ctxt = this.createDeserializationContext(p, deserConfig);
                result = this._findRootDeserializer(ctxt, toValueType).getNullValue(ctxt);
            } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = null;
            } else {
                DefaultDeserializationContext ctxt = this.createDeserializationContext(p, deserConfig);
                JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt, toValueType);
                result = deser.deserialize(p, ctxt);
            }
            p.close();
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public <T> T updateValue(T valueToUpdate, Object overrides) throws JsonMappingException {
        T result = valueToUpdate;
        if (valueToUpdate != null && overrides != null) {
            TokenBuffer buf = new TokenBuffer(this, false);
            if (this.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                buf = buf.forceUseOfBigDecimal(true);
            }
            try {
                SerializationConfig config = this.getSerializationConfig().without(SerializationFeature.WRAP_ROOT_VALUE);
                this._serializerProvider(config).serializeValue(buf, overrides);
                JsonParser p = buf.asParser();
                result = this.readerForUpdating(valueToUpdate).readValue(p);
                p.close();
            } catch (IOException e) {
                if (e instanceof JsonMappingException) {
                    throw (JsonMappingException)e;
                }
                throw JsonMappingException.fromUnexpectedIOE(e);
            }
        }
        return result;
    }

    @Deprecated
    public JsonSchema generateJsonSchema(Class<?> t) throws JsonMappingException {
        return this._serializerProvider(this.getSerializationConfig()).generateJsonSchema(t);
    }

    public void acceptJsonFormatVisitor(Class<?> type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        this.acceptJsonFormatVisitor(this._typeFactory.constructType(type), visitor);
    }

    public void acceptJsonFormatVisitor(JavaType type, JsonFormatVisitorWrapper visitor) throws JsonMappingException {
        if (type == null) {
            throw new IllegalArgumentException("type must be provided");
        }
        this._serializerProvider(this.getSerializationConfig()).acceptJsonFormatVisitor(type, visitor);
    }

    protected TypeResolverBuilder<?> _constructDefaultTypeResolverBuilder(DefaultTyping applicability, PolymorphicTypeValidator ptv) {
        return DefaultTypeResolverBuilder.construct(applicability, ptv);
    }

    protected DefaultSerializerProvider _serializerProvider(SerializationConfig config) {
        return this._serializerProvider.createInstance(config, this._serializerFactory);
    }

    protected final void _writeValueAndClose(JsonGenerator g, Object value) throws IOException {
        SerializationConfig cfg = this.getSerializationConfig();
        if (cfg.isEnabled(SerializationFeature.CLOSE_CLOSEABLE) && value instanceof Closeable) {
            this._writeCloseable(g, value, cfg);
            return;
        }
        try {
            this._serializerProvider(cfg).serializeValue(g, value);
        } catch (Exception e) {
            ClassUtil.closeOnFailAndThrowAsIOE(g, e);
            return;
        }
        g.close();
    }

    private final void _writeCloseable(JsonGenerator g, Object value, SerializationConfig cfg) throws IOException {
        Closeable toClose = (Closeable)value;
        try {
            this._serializerProvider(cfg).serializeValue(g, value);
            Closeable tmpToClose = toClose;
            toClose = null;
            tmpToClose.close();
        } catch (Exception e) {
            ClassUtil.closeOnFailAndThrowAsIOE(g, toClose, e);
            return;
        }
        g.close();
    }

    private final void _writeCloseableValue(JsonGenerator g, Object value, SerializationConfig cfg) throws IOException {
        Closeable toClose = (Closeable)value;
        try {
            this._serializerProvider(cfg).serializeValue(g, value);
            if (cfg.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
                g.flush();
            }
        } catch (Exception e) {
            ClassUtil.closeOnFailAndThrowAsIOE(null, toClose, e);
            return;
        }
        toClose.close();
    }

    @Deprecated
    protected final void _configAndWriteValue(JsonGenerator g, Object value) throws IOException {
        this.getSerializationConfig().initialize(g);
        this._writeValueAndClose(g, value);
    }

    protected Object _readValue(DeserializationConfig cfg, JsonParser p, JavaType valueType) throws IOException {
        Object result;
        JsonToken t = this._initForReading(p, valueType);
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p, cfg);
        if (t == JsonToken.VALUE_NULL) {
            result = this._findRootDeserializer(ctxt, valueType).getNullValue(ctxt);
        } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = null;
        } else {
            JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt, valueType);
            result = cfg.useRootWrapping() ? this._unwrapAndDeserialize(p, ctxt, cfg, valueType, deser) : deser.deserialize(p, ctxt);
        }
        p.clearCurrentToken();
        if (cfg.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
            this._verifyNoTrailingTokens(p, ctxt, valueType);
        }
        return result;
    }

    protected Object _readMapAndClose(JsonParser p0, JavaType valueType) throws IOException {
        try (JsonParser p = p0;){
            Object result;
            JsonToken t = this._initForReading(p, valueType);
            DeserializationConfig cfg = this.getDeserializationConfig();
            DefaultDeserializationContext ctxt = this.createDeserializationContext(p, cfg);
            if (t == JsonToken.VALUE_NULL) {
                result = this._findRootDeserializer(ctxt, valueType).getNullValue(ctxt);
            } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = null;
            } else {
                JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt, valueType);
                result = cfg.useRootWrapping() ? this._unwrapAndDeserialize(p, ctxt, cfg, valueType, deser) : deser.deserialize(p, ctxt);
                ((DeserializationContext)ctxt).checkUnresolvedObjectId();
            }
            if (cfg.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                this._verifyNoTrailingTokens(p, ctxt, valueType);
            }
            Object object = result;
            return object;
        }
    }

    protected JsonNode _readTreeAndClose(JsonParser p0) throws IOException {
        try (JsonParser p = p0;){
            DefaultDeserializationContext ctxt;
            JsonNode resultNode;
            JavaType valueType = this.constructType((Type)((Object)JsonNode.class));
            DeserializationConfig cfg = this.getDeserializationConfig();
            cfg.initialize(p);
            JsonToken t = p.getCurrentToken();
            if (t == null && (t = p.nextToken()) == null) {
                JsonNode jsonNode = cfg.getNodeFactory().missingNode();
                return jsonNode;
            }
            boolean checkTrailing = cfg.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
            if (t == JsonToken.VALUE_NULL) {
                resultNode = cfg.getNodeFactory().nullNode();
                if (!checkTrailing) {
                    NullNode nullNode = resultNode;
                    return nullNode;
                }
                ctxt = this.createDeserializationContext(p, cfg);
            } else {
                ctxt = this.createDeserializationContext(p, cfg);
                JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt, valueType);
                resultNode = cfg.useRootWrapping() ? (JsonNode)this._unwrapAndDeserialize(p, ctxt, cfg, valueType, deser) : (JsonNode)deser.deserialize(p, ctxt);
            }
            if (checkTrailing) {
                this._verifyNoTrailingTokens(p, ctxt, valueType);
            }
            NullNode nullNode = resultNode;
            return nullNode;
        }
    }

    protected Object _unwrapAndDeserialize(JsonParser p, DeserializationContext ctxt, DeserializationConfig config, JavaType rootType, JsonDeserializer<Object> deser) throws IOException {
        String actualName;
        PropertyName expRootName = config.findRootName(rootType);
        String expSimpleName = expRootName.getSimpleName();
        if (p.getCurrentToken() != JsonToken.START_OBJECT) {
            ctxt.reportWrongTokenException(rootType, JsonToken.START_OBJECT, "Current token not START_OBJECT (needed to unwrap root name '%s'), but %s", new Object[]{expSimpleName, p.getCurrentToken()});
        }
        if (p.nextToken() != JsonToken.FIELD_NAME) {
            ctxt.reportWrongTokenException(rootType, JsonToken.FIELD_NAME, "Current token not FIELD_NAME (to contain expected root name '%s'), but %s", new Object[]{expSimpleName, p.getCurrentToken()});
        }
        if (!expSimpleName.equals(actualName = p.getCurrentName())) {
            ctxt.reportPropertyInputMismatch(rootType, actualName, "Root name '%s' does not match expected ('%s') for type %s", actualName, expSimpleName, rootType);
        }
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        if (p.nextToken() != JsonToken.END_OBJECT) {
            ctxt.reportWrongTokenException(rootType, JsonToken.END_OBJECT, "Current token not END_OBJECT (to match wrapper object with root name '%s'), but %s", new Object[]{expSimpleName, p.getCurrentToken()});
        }
        if (config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
            this._verifyNoTrailingTokens(p, ctxt, rootType);
        }
        return result;
    }

    protected DefaultDeserializationContext createDeserializationContext(JsonParser p, DeserializationConfig cfg) {
        return this._deserializationContext.createInstance(cfg, p, this._injectableValues);
    }

    protected JsonToken _initForReading(JsonParser p, JavaType targetType) throws IOException {
        this._deserializationConfig.initialize(p);
        JsonToken t = p.getCurrentToken();
        if (t == null && (t = p.nextToken()) == null) {
            throw MismatchedInputException.from(p, targetType, "No content to map due to end-of-input");
        }
        return t;
    }

    @Deprecated
    protected JsonToken _initForReading(JsonParser p) throws IOException {
        return this._initForReading(p, null);
    }

    protected final void _verifyNoTrailingTokens(JsonParser p, DeserializationContext ctxt, JavaType bindType) throws IOException {
        JsonToken t = p.nextToken();
        if (t != null) {
            Class<?> bt = ClassUtil.rawClass(bindType);
            ctxt.reportTrailingTokens(bt, p, t);
        }
    }

    protected JsonDeserializer<Object> _findRootDeserializer(DeserializationContext ctxt, JavaType valueType) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._rootDeserializers.get(valueType);
        if (deser != null) {
            return deser;
        }
        deser = ctxt.findRootValueDeserializer(valueType);
        if (deser == null) {
            return (JsonDeserializer)ctxt.reportBadDefinition(valueType, "Cannot find a deserializer for type " + valueType);
        }
        this._rootDeserializers.put(valueType, deser);
        return deser;
    }

    protected void _verifySchemaType(FormatSchema schema) {
        if (schema != null && !this._jsonFactory.canUseSchema(schema)) {
            throw new IllegalArgumentException("Cannot use FormatSchema of type " + schema.getClass().getName() + " for format " + this._jsonFactory.getFormatName());
        }
    }

    protected final void _assertNotNull(String paramName, Object src) {
        if (src == null) {
            throw new IllegalArgumentException(String.format("argument \"%s\" is null", paramName));
        }
    }

    public static class DefaultTypeResolverBuilder
    extends StdTypeResolverBuilder
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final DefaultTyping _appliesFor;
        protected final PolymorphicTypeValidator _subtypeValidator;

        @Deprecated
        public DefaultTypeResolverBuilder(DefaultTyping t) {
            this(t, LaissezFaireSubTypeValidator.instance);
        }

        public DefaultTypeResolverBuilder(DefaultTyping t, PolymorphicTypeValidator ptv) {
            this._appliesFor = DefaultTypeResolverBuilder._requireNonNull(t, "Can not pass `null` DefaultTyping");
            this._subtypeValidator = DefaultTypeResolverBuilder._requireNonNull(ptv, "Can not pass `null` PolymorphicTypeValidator");
        }

        private static <T> T _requireNonNull(T value, String msg) {
            if (value == null) {
                throw new NullPointerException(msg);
            }
            return value;
        }

        public static DefaultTypeResolverBuilder construct(DefaultTyping t, PolymorphicTypeValidator ptv) {
            return new DefaultTypeResolverBuilder(t, ptv);
        }

        @Override
        public PolymorphicTypeValidator subTypeValidator(MapperConfig<?> config) {
            return this._subtypeValidator;
        }

        @Override
        public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
            return this.useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
        }

        @Override
        public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
            return this.useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes) : null;
        }

        public boolean useForType(JavaType t) {
            if (t.isPrimitive()) {
                return false;
            }
            switch (this._appliesFor) {
                case NON_CONCRETE_AND_ARRAYS: {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                }
                case OBJECT_AND_NON_CONCRETE: {
                    while (t.isReferenceType()) {
                        t = t.getReferencedType();
                    }
                    return t.isJavaLangObject() || !t.isConcrete() && !TreeNode.class.isAssignableFrom(t.getRawClass());
                }
                case NON_FINAL: {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    while (t.isReferenceType()) {
                        t = t.getReferencedType();
                    }
                    return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
                }
                case EVERYTHING: {
                    return true;
                }
            }
            return t.isJavaLangObject();
        }
    }

    public static enum DefaultTyping {
        JAVA_LANG_OBJECT,
        OBJECT_AND_NON_CONCRETE,
        NON_CONCRETE_AND_ARRAYS,
        NON_FINAL,
        EVERYTHING;

    }
}

