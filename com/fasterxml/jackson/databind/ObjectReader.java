/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.filter.FilteringParserDelegate;
import com.fasterxml.jackson.core.filter.JsonPointerBasedFilter;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectReader
extends ObjectCodec
implements Versioned,
Serializable {
    private static final long serialVersionUID = 2L;
    protected final DeserializationConfig _config;
    protected final DefaultDeserializationContext _context;
    protected final JsonFactory _parserFactory;
    protected final boolean _unwrapRoot;
    private final TokenFilter _filter;
    protected final JavaType _valueType;
    protected final JsonDeserializer<Object> _rootDeserializer;
    protected final Object _valueToUpdate;
    protected final FormatSchema _schema;
    protected final InjectableValues _injectableValues;
    protected final DataFormatReaders _dataFormatReaders;
    protected final ConcurrentHashMap<JavaType, JsonDeserializer<Object>> _rootDeserializers;
    protected transient JavaType _jsonNodeType;

    protected ObjectReader(ObjectMapper mapper, DeserializationConfig config) {
        this(mapper, config, null, null, null, null);
    }

    protected ObjectReader(ObjectMapper mapper, DeserializationConfig config, JavaType valueType, Object valueToUpdate, FormatSchema schema, InjectableValues injectableValues) {
        this._config = config;
        this._context = mapper._deserializationContext;
        this._rootDeserializers = mapper._rootDeserializers;
        this._parserFactory = mapper._jsonFactory;
        this._valueType = valueType;
        this._valueToUpdate = valueToUpdate;
        this._schema = schema;
        this._injectableValues = injectableValues;
        this._unwrapRoot = config.useRootWrapping();
        this._rootDeserializer = this._prefetchRootDeserializer(valueType);
        this._dataFormatReaders = null;
        this._filter = null;
    }

    protected ObjectReader(ObjectReader base, DeserializationConfig config, JavaType valueType, JsonDeserializer<Object> rootDeser, Object valueToUpdate, FormatSchema schema, InjectableValues injectableValues, DataFormatReaders dataFormatReaders) {
        this._config = config;
        this._context = base._context;
        this._rootDeserializers = base._rootDeserializers;
        this._parserFactory = base._parserFactory;
        this._valueType = valueType;
        this._rootDeserializer = rootDeser;
        this._valueToUpdate = valueToUpdate;
        this._schema = schema;
        this._injectableValues = injectableValues;
        this._unwrapRoot = config.useRootWrapping();
        this._dataFormatReaders = dataFormatReaders;
        this._filter = base._filter;
    }

    protected ObjectReader(ObjectReader base, DeserializationConfig config) {
        this._config = config;
        this._context = base._context;
        this._rootDeserializers = base._rootDeserializers;
        this._parserFactory = base._parserFactory;
        this._valueType = base._valueType;
        this._rootDeserializer = base._rootDeserializer;
        this._valueToUpdate = base._valueToUpdate;
        this._schema = base._schema;
        this._injectableValues = base._injectableValues;
        this._unwrapRoot = config.useRootWrapping();
        this._dataFormatReaders = base._dataFormatReaders;
        this._filter = base._filter;
    }

    protected ObjectReader(ObjectReader base, JsonFactory f) {
        this._config = (DeserializationConfig)base._config.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, f.requiresPropertyOrdering());
        this._context = base._context;
        this._rootDeserializers = base._rootDeserializers;
        this._parserFactory = f;
        this._valueType = base._valueType;
        this._rootDeserializer = base._rootDeserializer;
        this._valueToUpdate = base._valueToUpdate;
        this._schema = base._schema;
        this._injectableValues = base._injectableValues;
        this._unwrapRoot = base._unwrapRoot;
        this._dataFormatReaders = base._dataFormatReaders;
        this._filter = base._filter;
    }

    protected ObjectReader(ObjectReader base, TokenFilter filter) {
        this._config = base._config;
        this._context = base._context;
        this._rootDeserializers = base._rootDeserializers;
        this._parserFactory = base._parserFactory;
        this._valueType = base._valueType;
        this._rootDeserializer = base._rootDeserializer;
        this._valueToUpdate = base._valueToUpdate;
        this._schema = base._schema;
        this._injectableValues = base._injectableValues;
        this._unwrapRoot = base._unwrapRoot;
        this._dataFormatReaders = base._dataFormatReaders;
        this._filter = filter;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    protected ObjectReader _new(ObjectReader base, JsonFactory f) {
        return new ObjectReader(base, f);
    }

    protected ObjectReader _new(ObjectReader base, DeserializationConfig config) {
        return new ObjectReader(base, config);
    }

    protected ObjectReader _new(ObjectReader base, DeserializationConfig config, JavaType valueType, JsonDeserializer<Object> rootDeser, Object valueToUpdate, FormatSchema schema, InjectableValues injectableValues, DataFormatReaders dataFormatReaders) {
        return new ObjectReader(base, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
    }

    protected <T> MappingIterator<T> _newIterator(JsonParser p, DeserializationContext ctxt, JsonDeserializer<?> deser, boolean parserManaged) {
        return new MappingIterator(this._valueType, p, ctxt, deser, parserManaged, this._valueToUpdate);
    }

    protected JsonToken _initForReading(DeserializationContext ctxt, JsonParser p) throws IOException {
        if (this._schema != null) {
            p.setSchema(this._schema);
        }
        this._config.initialize(p);
        JsonToken t = p.getCurrentToken();
        if (t == null && (t = p.nextToken()) == null) {
            ctxt.reportInputMismatch(this._valueType, "No content to map due to end-of-input", new Object[0]);
        }
        return t;
    }

    protected void _initForMultiRead(DeserializationContext ctxt, JsonParser p) throws IOException {
        if (this._schema != null) {
            p.setSchema(this._schema);
        }
        this._config.initialize(p);
    }

    public ObjectReader with(DeserializationFeature feature) {
        return this._with(this._config.with(feature));
    }

    public ObjectReader with(DeserializationFeature first, DeserializationFeature ... other) {
        return this._with(this._config.with(first, other));
    }

    public ObjectReader withFeatures(DeserializationFeature ... features) {
        return this._with(this._config.withFeatures(features));
    }

    public ObjectReader without(DeserializationFeature feature) {
        return this._with(this._config.without(feature));
    }

    public ObjectReader without(DeserializationFeature first, DeserializationFeature ... other) {
        return this._with(this._config.without(first, other));
    }

    public ObjectReader withoutFeatures(DeserializationFeature ... features) {
        return this._with(this._config.withoutFeatures(features));
    }

    public ObjectReader with(JsonParser.Feature feature) {
        return this._with(this._config.with(feature));
    }

    public ObjectReader withFeatures(JsonParser.Feature ... features) {
        return this._with(this._config.withFeatures(features));
    }

    public ObjectReader without(JsonParser.Feature feature) {
        return this._with(this._config.without(feature));
    }

    public ObjectReader withoutFeatures(JsonParser.Feature ... features) {
        return this._with(this._config.withoutFeatures(features));
    }

    public ObjectReader with(StreamReadFeature feature) {
        return this._with(this._config.with(feature.mappedFeature()));
    }

    public ObjectReader without(StreamReadFeature feature) {
        return this._with(this._config.without(feature.mappedFeature()));
    }

    public ObjectReader with(FormatFeature feature) {
        return this._with(this._config.with(feature));
    }

    public ObjectReader withFeatures(FormatFeature ... features) {
        return this._with(this._config.withFeatures(features));
    }

    public ObjectReader without(FormatFeature feature) {
        return this._with(this._config.without(feature));
    }

    public ObjectReader withoutFeatures(FormatFeature ... features) {
        return this._with(this._config.withoutFeatures(features));
    }

    public ObjectReader at(String pointerExpr) {
        this._assertNotNull("pointerExpr", pointerExpr);
        return new ObjectReader(this, new JsonPointerBasedFilter(pointerExpr));
    }

    public ObjectReader at(JsonPointer pointer) {
        this._assertNotNull("pointer", pointer);
        return new ObjectReader(this, new JsonPointerBasedFilter(pointer));
    }

    public ObjectReader with(DeserializationConfig config) {
        return this._with(config);
    }

    public ObjectReader with(InjectableValues injectableValues) {
        if (this._injectableValues == injectableValues) {
            return this;
        }
        return this._new(this, this._config, this._valueType, this._rootDeserializer, this._valueToUpdate, this._schema, injectableValues, this._dataFormatReaders);
    }

    public ObjectReader with(JsonNodeFactory f) {
        return this._with(this._config.with(f));
    }

    public ObjectReader with(JsonFactory f) {
        if (f == this._parserFactory) {
            return this;
        }
        ObjectReader r = this._new(this, f);
        if (f.getCodec() == null) {
            f.setCodec(r);
        }
        return r;
    }

    public ObjectReader withRootName(String rootName) {
        return this._with((DeserializationConfig)this._config.withRootName(rootName));
    }

    public ObjectReader withRootName(PropertyName rootName) {
        return this._with(this._config.withRootName(rootName));
    }

    public ObjectReader withoutRootName() {
        return this._with(this._config.withRootName(PropertyName.NO_NAME));
    }

    public ObjectReader with(FormatSchema schema) {
        if (this._schema == schema) {
            return this;
        }
        this._verifySchemaType(schema);
        return this._new(this, this._config, this._valueType, this._rootDeserializer, this._valueToUpdate, schema, this._injectableValues, this._dataFormatReaders);
    }

    public ObjectReader forType(JavaType valueType) {
        if (valueType != null && valueType.equals(this._valueType)) {
            return this;
        }
        JsonDeserializer<Object> rootDeser = this._prefetchRootDeserializer(valueType);
        DataFormatReaders det = this._dataFormatReaders;
        if (det != null) {
            det = det.withType(valueType);
        }
        return this._new(this, this._config, valueType, rootDeser, this._valueToUpdate, this._schema, this._injectableValues, det);
    }

    public ObjectReader forType(Class<?> valueType) {
        return this.forType(this._config.constructType(valueType));
    }

    public ObjectReader forType(TypeReference<?> valueTypeRef) {
        return this.forType(this._config.getTypeFactory().constructType(valueTypeRef.getType()));
    }

    @Deprecated
    public ObjectReader withType(JavaType valueType) {
        return this.forType(valueType);
    }

    @Deprecated
    public ObjectReader withType(Class<?> valueType) {
        return this.forType(this._config.constructType(valueType));
    }

    @Deprecated
    public ObjectReader withType(Type valueType) {
        return this.forType(this._config.getTypeFactory().constructType(valueType));
    }

    @Deprecated
    public ObjectReader withType(TypeReference<?> valueTypeRef) {
        return this.forType(this._config.getTypeFactory().constructType(valueTypeRef.getType()));
    }

    public ObjectReader withValueToUpdate(Object value) {
        if (value == this._valueToUpdate) {
            return this;
        }
        if (value == null) {
            return this._new(this, this._config, this._valueType, this._rootDeserializer, null, this._schema, this._injectableValues, this._dataFormatReaders);
        }
        JavaType t = this._valueType == null ? this._config.constructType(value.getClass()) : this._valueType;
        return this._new(this, this._config, t, this._rootDeserializer, value, this._schema, this._injectableValues, this._dataFormatReaders);
    }

    public ObjectReader withView(Class<?> activeView) {
        return this._with((DeserializationConfig)this._config.withView((Class)activeView));
    }

    public ObjectReader with(Locale l) {
        return this._with((DeserializationConfig)this._config.with(l));
    }

    public ObjectReader with(TimeZone tz) {
        return this._with((DeserializationConfig)this._config.with(tz));
    }

    public ObjectReader withHandler(DeserializationProblemHandler h) {
        return this._with(this._config.withHandler(h));
    }

    public ObjectReader with(Base64Variant defaultBase64) {
        return this._with((DeserializationConfig)this._config.with(defaultBase64));
    }

    public ObjectReader withFormatDetection(ObjectReader ... readers) {
        return this.withFormatDetection(new DataFormatReaders(readers));
    }

    public ObjectReader withFormatDetection(DataFormatReaders readers) {
        return this._new(this, this._config, this._valueType, this._rootDeserializer, this._valueToUpdate, this._schema, this._injectableValues, readers);
    }

    public ObjectReader with(ContextAttributes attrs) {
        return this._with(this._config.with(attrs));
    }

    public ObjectReader withAttributes(Map<?, ?> attrs) {
        return this._with((DeserializationConfig)this._config.withAttributes(attrs));
    }

    public ObjectReader withAttribute(Object key, Object value) {
        return this._with((DeserializationConfig)this._config.withAttribute(key, value));
    }

    public ObjectReader withoutAttribute(Object key) {
        return this._with((DeserializationConfig)this._config.withoutAttribute(key));
    }

    protected ObjectReader _with(DeserializationConfig newConfig) {
        if (newConfig == this._config) {
            return this;
        }
        ObjectReader r = this._new(this, newConfig);
        if (this._dataFormatReaders != null) {
            r = r.withFormatDetection(this._dataFormatReaders.with(newConfig));
        }
        return r;
    }

    public boolean isEnabled(DeserializationFeature f) {
        return this._config.isEnabled(f);
    }

    public boolean isEnabled(MapperFeature f) {
        return this._config.isEnabled(f);
    }

    public boolean isEnabled(JsonParser.Feature f) {
        return this._config.isEnabled(f, this._parserFactory);
    }

    public boolean isEnabled(StreamReadFeature f) {
        return this._config.isEnabled(f.mappedFeature(), this._parserFactory);
    }

    public DeserializationConfig getConfig() {
        return this._config;
    }

    @Override
    public JsonFactory getFactory() {
        return this._parserFactory;
    }

    public TypeFactory getTypeFactory() {
        return this._config.getTypeFactory();
    }

    public ContextAttributes getAttributes() {
        return this._config.getAttributes();
    }

    public InjectableValues getInjectableValues() {
        return this._injectableValues;
    }

    public JavaType getValueType() {
        return this._valueType;
    }

    public JsonParser createParser(File src) throws IOException {
        this._assertNotNull("src", src);
        JsonParser p = this._parserFactory.createParser(src);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(URL src) throws IOException {
        this._assertNotNull("src", src);
        JsonParser p = this._parserFactory.createParser(src);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(InputStream in) throws IOException {
        this._assertNotNull("in", in);
        JsonParser p = this._parserFactory.createParser(in);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(Reader r) throws IOException {
        this._assertNotNull("r", r);
        JsonParser p = this._parserFactory.createParser(r);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(byte[] content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(byte[] content, int offset, int len) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content, offset, len);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(String content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(char[] content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(char[] content, int offset, int len) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content, offset, len);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createParser(DataInput content) throws IOException {
        this._assertNotNull("content", content);
        JsonParser p = this._parserFactory.createParser(content);
        this._config.initialize(p);
        return p;
    }

    public JsonParser createNonBlockingByteArrayParser() throws IOException {
        JsonParser p = this._parserFactory.createNonBlockingByteArrayParser();
        this._config.initialize(p);
        return p;
    }

    public <T> T readValue(JsonParser p) throws IOException {
        this._assertNotNull("p", p);
        return (T)this._bind(p, this._valueToUpdate);
    }

    @Override
    public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueType).readValue(p);
    }

    @Override
    public <T> T readValue(JsonParser p, TypeReference<T> valueTypeRef) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueTypeRef).readValue(p);
    }

    @Override
    public <T> T readValue(JsonParser p, ResolvedType valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.forType((JavaType)valueType).readValue(p);
    }

    public <T> T readValue(JsonParser p, JavaType valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueType).readValue(p);
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueType).readValues(p);
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser p, TypeReference<T> valueTypeRef) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueTypeRef).readValues(p);
    }

    @Override
    public <T> Iterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.readValues(p, (JavaType)valueType);
    }

    public <T> Iterator<T> readValues(JsonParser p, JavaType valueType) throws IOException {
        this._assertNotNull("p", p);
        return this.forType(valueType).readValues(p);
    }

    @Override
    public JsonNode createArrayNode() {
        return this._config.getNodeFactory().arrayNode();
    }

    @Override
    public JsonNode createObjectNode() {
        return this._config.getNodeFactory().objectNode();
    }

    @Override
    public JsonNode missingNode() {
        return this._config.getNodeFactory().missingNode();
    }

    @Override
    public JsonNode nullNode() {
        return this._config.getNodeFactory().nullNode();
    }

    @Override
    public JsonParser treeAsTokens(TreeNode n) {
        this._assertNotNull("n", n);
        ObjectReader codec = this.withValueToUpdate(null);
        return new TreeTraversingParser((JsonNode)n, codec);
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        this._assertNotNull("p", p);
        return (T)this._bindAsTreeOrNull(p);
    }

    @Override
    public void writeTree(JsonGenerator g, TreeNode rootNode) {
        throw new UnsupportedOperationException();
    }

    public <T> T readValue(InputStream src) throws IOException {
        if (this._dataFormatReaders != null) {
            return (T)this._detectBindAndClose(this._dataFormatReaders.findFormat(src), false);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
    }

    public <T> T readValue(InputStream src, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(src);
    }

    public <T> T readValue(Reader src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
    }

    public <T> T readValue(Reader src, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(src);
    }

    public <T> T readValue(String src) throws JsonProcessingException, JsonMappingException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        try {
            return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    public <T> T readValue(String src, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(src);
    }

    public <T> T readValue(byte[] content) throws IOException {
        if (this._dataFormatReaders != null) {
            return (T)this._detectBindAndClose(content, 0, content.length);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(content), false));
    }

    public <T> T readValue(byte[] content, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(content);
    }

    public <T> T readValue(byte[] buffer, int offset, int length) throws IOException {
        if (this._dataFormatReaders != null) {
            return (T)this._detectBindAndClose(buffer, offset, length);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(buffer, offset, length), false));
    }

    public <T> T readValue(byte[] buffer, int offset, int length, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(buffer, offset, length);
    }

    public <T> T readValue(File src) throws IOException {
        if (this._dataFormatReaders != null) {
            return (T)this._detectBindAndClose(this._dataFormatReaders.findFormat(this._inputStream(src)), true);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
    }

    public <T> T readValue(File src, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(src);
    }

    public <T> T readValue(URL src) throws IOException {
        if (this._dataFormatReaders != null) {
            return (T)this._detectBindAndClose(this._dataFormatReaders.findFormat(this._inputStream(src)), true);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
    }

    public <T> T readValue(URL src, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(src);
    }

    public <T> T readValue(JsonNode content) throws IOException {
        this._assertNotNull("content", content);
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(content);
        }
        return (T)this._bindAndClose(this._considerFilter(this.treeAsTokens(content), false));
    }

    public <T> T readValue(JsonNode content, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(content);
    }

    public <T> T readValue(DataInput src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        return (T)this._bindAndClose(this._considerFilter(this.createParser(src), false));
    }

    public <T> T readValue(DataInput content, Class<T> valueType) throws IOException {
        return this.forType(valueType).readValue(content);
    }

    public JsonNode readTree(InputStream src) throws IOException {
        if (this._dataFormatReaders != null) {
            return this._detectBindAndCloseAsTree(src);
        }
        return this._bindAndCloseAsTree(this._considerFilter(this.createParser(src), false));
    }

    public JsonNode readTree(Reader src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        return this._bindAndCloseAsTree(this._considerFilter(this.createParser(src), false));
    }

    public JsonNode readTree(String json) throws JsonProcessingException, JsonMappingException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(json);
        }
        try {
            return this._bindAndCloseAsTree(this._considerFilter(this.createParser(json), false));
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    public JsonNode readTree(byte[] json) throws IOException {
        this._assertNotNull("json", json);
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(json);
        }
        return this._bindAndCloseAsTree(this._considerFilter(this.createParser(json), false));
    }

    public JsonNode readTree(byte[] json, int offset, int len) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(json);
        }
        return this._bindAndCloseAsTree(this._considerFilter(this.createParser(json, offset, len), false));
    }

    public JsonNode readTree(DataInput src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        return this._bindAndCloseAsTree(this._considerFilter(this.createParser(src), false));
    }

    public <T> MappingIterator<T> readValues(JsonParser p) throws IOException {
        this._assertNotNull("p", p);
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
        return this._newIterator(p, ctxt, this._findRootDeserializer(ctxt), false);
    }

    public <T> MappingIterator<T> readValues(InputStream src) throws IOException {
        if (this._dataFormatReaders != null) {
            return this._detectBindAndReadValues(this._dataFormatReaders.findFormat(src), false);
        }
        return this._bindAndReadValues(this._considerFilter(this.createParser(src), true));
    }

    public <T> MappingIterator<T> readValues(Reader src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        JsonParser p = this._considerFilter(this.createParser(src), true);
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
        this._initForMultiRead(ctxt, p);
        p.nextToken();
        return this._newIterator(p, ctxt, this._findRootDeserializer(ctxt), true);
    }

    public <T> MappingIterator<T> readValues(String json) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(json);
        }
        JsonParser p = this._considerFilter(this.createParser(json), true);
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
        this._initForMultiRead(ctxt, p);
        p.nextToken();
        return this._newIterator(p, ctxt, this._findRootDeserializer(ctxt), true);
    }

    public <T> MappingIterator<T> readValues(byte[] src, int offset, int length) throws IOException {
        if (this._dataFormatReaders != null) {
            return this._detectBindAndReadValues(this._dataFormatReaders.findFormat(src, offset, length), false);
        }
        return this._bindAndReadValues(this._considerFilter(this.createParser(src, offset, length), true));
    }

    public final <T> MappingIterator<T> readValues(byte[] src) throws IOException {
        this._assertNotNull("src", src);
        return this.readValues(src, 0, src.length);
    }

    public <T> MappingIterator<T> readValues(File src) throws IOException {
        if (this._dataFormatReaders != null) {
            return this._detectBindAndReadValues(this._dataFormatReaders.findFormat(this._inputStream(src)), false);
        }
        return this._bindAndReadValues(this._considerFilter(this.createParser(src), true));
    }

    public <T> MappingIterator<T> readValues(URL src) throws IOException {
        if (this._dataFormatReaders != null) {
            return this._detectBindAndReadValues(this._dataFormatReaders.findFormat(this._inputStream(src)), true);
        }
        return this._bindAndReadValues(this._considerFilter(this.createParser(src), true));
    }

    public <T> MappingIterator<T> readValues(DataInput src) throws IOException {
        if (this._dataFormatReaders != null) {
            this._reportUndetectableSource(src);
        }
        return this._bindAndReadValues(this._considerFilter(this.createParser(src), true));
    }

    @Override
    public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException {
        this._assertNotNull("n", n);
        try {
            return this.readValue(this.treeAsTokens(n), valueType);
        } catch (JsonProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    @Override
    public void writeValue(JsonGenerator gen, Object value) throws IOException {
        throw new UnsupportedOperationException("Not implemented for ObjectReader");
    }

    protected Object _bind(JsonParser p, Object valueToUpdate) throws IOException {
        Object result;
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
        JsonToken t = this._initForReading(ctxt, p);
        if (t == JsonToken.VALUE_NULL) {
            result = valueToUpdate == null ? this._findRootDeserializer(ctxt).getNullValue(ctxt) : valueToUpdate;
        } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
            result = valueToUpdate;
        } else {
            JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt);
            result = this._unwrapRoot ? this._unwrapAndDeserialize(p, ctxt, this._valueType, deser) : (valueToUpdate == null ? deser.deserialize(p, ctxt) : deser.deserialize(p, ctxt, valueToUpdate));
        }
        p.clearCurrentToken();
        if (this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
            this._verifyNoTrailingTokens(p, ctxt, this._valueType);
        }
        return result;
    }

    protected Object _bindAndClose(JsonParser p0) throws IOException {
        try (JsonParser p = p0;){
            Object result;
            DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
            JsonToken t = this._initForReading(ctxt, p);
            if (t == JsonToken.VALUE_NULL) {
                result = this._valueToUpdate == null ? this._findRootDeserializer(ctxt).getNullValue(ctxt) : this._valueToUpdate;
            } else if (t == JsonToken.END_ARRAY || t == JsonToken.END_OBJECT) {
                result = this._valueToUpdate;
            } else {
                JsonDeserializer<Object> deser = this._findRootDeserializer(ctxt);
                if (this._unwrapRoot) {
                    result = this._unwrapAndDeserialize(p, ctxt, this._valueType, deser);
                } else if (this._valueToUpdate == null) {
                    result = deser.deserialize(p, ctxt);
                } else {
                    deser.deserialize(p, ctxt, this._valueToUpdate);
                    result = this._valueToUpdate;
                }
            }
            if (this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                this._verifyNoTrailingTokens(p, ctxt, this._valueType);
            }
            Object object = result;
            return object;
        }
    }

    protected final JsonNode _bindAndCloseAsTree(JsonParser p0) throws IOException {
        try (JsonParser p = p0;){
            JsonNode jsonNode = this._bindAsTree(p);
            return jsonNode;
        }
    }

    protected final JsonNode _bindAsTree(JsonParser p) throws IOException {
        DefaultDeserializationContext ctxt;
        JsonNode resultNode;
        JsonToken t;
        this._config.initialize(p);
        if (this._schema != null) {
            p.setSchema(this._schema);
        }
        if ((t = p.getCurrentToken()) == null && (t = p.nextToken()) == null) {
            return this._config.getNodeFactory().missingNode();
        }
        boolean checkTrailing = this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        if (t == JsonToken.VALUE_NULL) {
            resultNode = this._config.getNodeFactory().nullNode();
            if (!checkTrailing) {
                return resultNode;
            }
            ctxt = this.createDeserializationContext(p);
        } else {
            ctxt = this.createDeserializationContext(p);
            JsonDeserializer<Object> deser = this._findTreeDeserializer(ctxt);
            resultNode = this._unwrapRoot ? (JsonNode)this._unwrapAndDeserialize(p, ctxt, this._jsonNodeType(), deser) : (JsonNode)deser.deserialize(p, ctxt);
        }
        if (this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
            this._verifyNoTrailingTokens(p, ctxt, this._jsonNodeType());
        }
        return resultNode;
    }

    protected final JsonNode _bindAsTreeOrNull(JsonParser p) throws IOException {
        DefaultDeserializationContext ctxt;
        JsonNode resultNode;
        JsonToken t;
        this._config.initialize(p);
        if (this._schema != null) {
            p.setSchema(this._schema);
        }
        if ((t = p.getCurrentToken()) == null && (t = p.nextToken()) == null) {
            return null;
        }
        boolean checkTrailing = this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        if (t == JsonToken.VALUE_NULL) {
            resultNode = this._config.getNodeFactory().nullNode();
            if (!checkTrailing) {
                return resultNode;
            }
            ctxt = this.createDeserializationContext(p);
        } else {
            ctxt = this.createDeserializationContext(p);
            JsonDeserializer<Object> deser = this._findTreeDeserializer(ctxt);
            resultNode = this._unwrapRoot ? (JsonNode)this._unwrapAndDeserialize(p, ctxt, this._jsonNodeType(), deser) : (JsonNode)deser.deserialize(p, ctxt);
        }
        if (checkTrailing) {
            this._verifyNoTrailingTokens(p, ctxt, this._jsonNodeType());
        }
        return resultNode;
    }

    protected <T> MappingIterator<T> _bindAndReadValues(JsonParser p) throws IOException {
        DefaultDeserializationContext ctxt = this.createDeserializationContext(p);
        this._initForMultiRead(ctxt, p);
        p.nextToken();
        return this._newIterator(p, ctxt, this._findRootDeserializer(ctxt), true);
    }

    protected Object _unwrapAndDeserialize(JsonParser p, DeserializationContext ctxt, JavaType rootType, JsonDeserializer<Object> deser) throws IOException {
        Object result;
        String actualName;
        PropertyName expRootName = this._config.findRootName(rootType);
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
        if (this._valueToUpdate == null) {
            result = deser.deserialize(p, ctxt);
        } else {
            deser.deserialize(p, ctxt, this._valueToUpdate);
            result = this._valueToUpdate;
        }
        if (p.nextToken() != JsonToken.END_OBJECT) {
            ctxt.reportWrongTokenException(rootType, JsonToken.END_OBJECT, "Current token not END_OBJECT (to match wrapper object with root name '%s'), but %s", new Object[]{expSimpleName, p.getCurrentToken()});
        }
        if (this._config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
            this._verifyNoTrailingTokens(p, ctxt, this._valueType);
        }
        return result;
    }

    protected JsonParser _considerFilter(JsonParser p, boolean multiValue) {
        return this._filter == null || FilteringParserDelegate.class.isInstance(p) ? p : new FilteringParserDelegate(p, this._filter, false, multiValue);
    }

    protected final void _verifyNoTrailingTokens(JsonParser p, DeserializationContext ctxt, JavaType bindType) throws IOException {
        JsonToken t = p.nextToken();
        if (t != null) {
            Class<?> bt = ClassUtil.rawClass(bindType);
            if (bt == null && this._valueToUpdate != null) {
                bt = this._valueToUpdate.getClass();
            }
            ctxt.reportTrailingTokens(bt, p, t);
        }
    }

    protected Object _detectBindAndClose(byte[] src, int offset, int length) throws IOException {
        DataFormatReaders.Match match = this._dataFormatReaders.findFormat(src, offset, length);
        if (!match.hasMatch()) {
            this._reportUnkownFormat(this._dataFormatReaders, match);
        }
        JsonParser p = match.createParserWithMatch();
        return match.getReader()._bindAndClose(p);
    }

    protected Object _detectBindAndClose(DataFormatReaders.Match match, boolean forceClosing) throws IOException {
        if (!match.hasMatch()) {
            this._reportUnkownFormat(this._dataFormatReaders, match);
        }
        JsonParser p = match.createParserWithMatch();
        if (forceClosing) {
            p.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        }
        return match.getReader()._bindAndClose(p);
    }

    protected <T> MappingIterator<T> _detectBindAndReadValues(DataFormatReaders.Match match, boolean forceClosing) throws IOException {
        if (!match.hasMatch()) {
            this._reportUnkownFormat(this._dataFormatReaders, match);
        }
        JsonParser p = match.createParserWithMatch();
        if (forceClosing) {
            p.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        }
        return match.getReader()._bindAndReadValues(p);
    }

    protected JsonNode _detectBindAndCloseAsTree(InputStream in) throws IOException {
        DataFormatReaders.Match match = this._dataFormatReaders.findFormat(in);
        if (!match.hasMatch()) {
            this._reportUnkownFormat(this._dataFormatReaders, match);
        }
        JsonParser p = match.createParserWithMatch();
        p.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        return match.getReader()._bindAndCloseAsTree(p);
    }

    protected void _reportUnkownFormat(DataFormatReaders detector, DataFormatReaders.Match match) throws JsonProcessingException {
        throw new JsonParseException(null, "Cannot detect format from input, does not look like any of detectable formats " + detector.toString());
    }

    protected void _verifySchemaType(FormatSchema schema) {
        if (schema != null && !this._parserFactory.canUseSchema(schema)) {
            throw new IllegalArgumentException("Cannot use FormatSchema of type " + schema.getClass().getName() + " for format " + this._parserFactory.getFormatName());
        }
    }

    protected DefaultDeserializationContext createDeserializationContext(JsonParser p) {
        return this._context.createInstance(this._config, p, this._injectableValues);
    }

    protected InputStream _inputStream(URL src) throws IOException {
        return src.openStream();
    }

    protected InputStream _inputStream(File f) throws IOException {
        return new FileInputStream(f);
    }

    protected void _reportUndetectableSource(Object src) throws JsonParseException {
        throw new JsonParseException(null, "Cannot use source of type " + src.getClass().getName() + " with format auto-detection: must be byte- not char-based");
    }

    protected JsonDeserializer<Object> _findRootDeserializer(DeserializationContext ctxt) throws JsonMappingException {
        JsonDeserializer<Object> deser;
        if (this._rootDeserializer != null) {
            return this._rootDeserializer;
        }
        JavaType t = this._valueType;
        if (t == null) {
            ctxt.reportBadDefinition((JavaType)null, "No value type configured for ObjectReader");
        }
        if ((deser = this._rootDeserializers.get(t)) != null) {
            return deser;
        }
        deser = ctxt.findRootValueDeserializer(t);
        if (deser == null) {
            ctxt.reportBadDefinition(t, "Cannot find a deserializer for type " + t);
        }
        this._rootDeserializers.put(t, deser);
        return deser;
    }

    protected JsonDeserializer<Object> _findTreeDeserializer(DeserializationContext ctxt) throws JsonMappingException {
        JavaType nodeType = this._jsonNodeType();
        JsonDeserializer<Object> deser = this._rootDeserializers.get(nodeType);
        if (deser == null) {
            deser = ctxt.findRootValueDeserializer(nodeType);
            if (deser == null) {
                ctxt.reportBadDefinition(nodeType, "Cannot find a deserializer for type " + nodeType);
            }
            this._rootDeserializers.put(nodeType, deser);
        }
        return deser;
    }

    protected JsonDeserializer<Object> _prefetchRootDeserializer(JavaType valueType) {
        if (valueType == null || !this._config.isEnabled(DeserializationFeature.EAGER_DESERIALIZER_FETCH)) {
            return null;
        }
        JsonDeserializer<Object> deser = this._rootDeserializers.get(valueType);
        if (deser == null) {
            try {
                DefaultDeserializationContext ctxt = this.createDeserializationContext(null);
                deser = ctxt.findRootValueDeserializer(valueType);
                if (deser != null) {
                    this._rootDeserializers.put(valueType, deser);
                }
                return deser;
            } catch (JsonProcessingException jsonProcessingException) {
                // empty catch block
            }
        }
        return deser;
    }

    protected final JavaType _jsonNodeType() {
        JavaType t = this._jsonNodeType;
        if (t == null) {
            this._jsonNodeType = t = this.getTypeFactory().constructType((Type)((Object)JsonNode.class));
        }
        return t;
    }

    protected final void _assertNotNull(String paramName, Object src) {
        if (src == null) {
            throw new IllegalArgumentException(String.format("argument \"%s\" is null", paramName));
        }
    }
}

