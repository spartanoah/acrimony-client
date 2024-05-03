/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.MapperConfigBase;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.util.LinkedNode;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import java.io.Serializable;
import java.util.Collection;

public final class DeserializationConfig
extends MapperConfigBase<DeserializationFeature, DeserializationConfig>
implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final int DESER_FEATURE_DEFAULTS = DeserializationConfig.collectFeatureDefaults(DeserializationFeature.class);
    protected final LinkedNode<DeserializationProblemHandler> _problemHandlers;
    protected final JsonNodeFactory _nodeFactory;
    protected final int _deserFeatures;
    protected final int _parserFeatures;
    protected final int _parserFeaturesToChange;
    protected final int _formatReadFeatures;
    protected final int _formatReadFeaturesToChange;

    public DeserializationConfig(BaseSettings base, SubtypeResolver str, SimpleMixInResolver mixins, RootNameLookup rootNames, ConfigOverrides configOverrides) {
        super(base, str, mixins, rootNames, configOverrides);
        this._deserFeatures = DESER_FEATURE_DEFAULTS;
        this._nodeFactory = JsonNodeFactory.instance;
        this._problemHandlers = null;
        this._parserFeatures = 0;
        this._parserFeaturesToChange = 0;
        this._formatReadFeatures = 0;
        this._formatReadFeaturesToChange = 0;
    }

    protected DeserializationConfig(DeserializationConfig src, SubtypeResolver str, SimpleMixInResolver mixins, RootNameLookup rootNames, ConfigOverrides configOverrides) {
        super(src, str, mixins, rootNames, configOverrides);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    @Deprecated
    protected DeserializationConfig(DeserializationConfig src, SimpleMixInResolver mixins, RootNameLookup rootNames, ConfigOverrides configOverrides) {
        this(src, src._subtypeResolver, mixins, rootNames, configOverrides);
    }

    private DeserializationConfig(DeserializationConfig src, int mapperFeatures, int deserFeatures, int parserFeatures, int parserFeatureMask, int formatFeatures, int formatFeatureMask) {
        super(src, mapperFeatures);
        this._deserFeatures = deserFeatures;
        this._nodeFactory = src._nodeFactory;
        this._problemHandlers = src._problemHandlers;
        this._parserFeatures = parserFeatures;
        this._parserFeaturesToChange = parserFeatureMask;
        this._formatReadFeatures = formatFeatures;
        this._formatReadFeaturesToChange = formatFeatureMask;
    }

    private DeserializationConfig(DeserializationConfig src, SubtypeResolver str) {
        super(src, str);
        this._deserFeatures = src._deserFeatures;
        this._nodeFactory = src._nodeFactory;
        this._problemHandlers = src._problemHandlers;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    private DeserializationConfig(DeserializationConfig src, BaseSettings base) {
        super(src, base);
        this._deserFeatures = src._deserFeatures;
        this._nodeFactory = src._nodeFactory;
        this._problemHandlers = src._problemHandlers;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    private DeserializationConfig(DeserializationConfig src, JsonNodeFactory f) {
        super(src);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = f;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    private DeserializationConfig(DeserializationConfig src, LinkedNode<DeserializationProblemHandler> problemHandlers) {
        super(src);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    private DeserializationConfig(DeserializationConfig src, PropertyName rootName) {
        super(src, rootName);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    private DeserializationConfig(DeserializationConfig src, Class<?> view) {
        super(src, view);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    protected DeserializationConfig(DeserializationConfig src, ContextAttributes attrs) {
        super(src, attrs);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    protected DeserializationConfig(DeserializationConfig src, SimpleMixInResolver mixins) {
        super(src, mixins);
        this._deserFeatures = src._deserFeatures;
        this._problemHandlers = src._problemHandlers;
        this._nodeFactory = src._nodeFactory;
        this._parserFeatures = src._parserFeatures;
        this._parserFeaturesToChange = src._parserFeaturesToChange;
        this._formatReadFeatures = src._formatReadFeatures;
        this._formatReadFeaturesToChange = src._formatReadFeaturesToChange;
    }

    protected BaseSettings getBaseSettings() {
        return this._base;
    }

    @Override
    protected final DeserializationConfig _withBase(BaseSettings newBase) {
        return this._base == newBase ? this : new DeserializationConfig(this, newBase);
    }

    @Override
    protected final DeserializationConfig _withMapperFeatures(int mapperFeatures) {
        return new DeserializationConfig(this, mapperFeatures, this._deserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    @Override
    public DeserializationConfig with(SubtypeResolver str) {
        return this._subtypeResolver == str ? this : new DeserializationConfig(this, str);
    }

    @Override
    public DeserializationConfig withRootName(PropertyName rootName) {
        if (rootName == null ? this._rootName == null : rootName.equals(this._rootName)) {
            return this;
        }
        return new DeserializationConfig(this, rootName);
    }

    @Override
    public DeserializationConfig withView(Class<?> view) {
        return this._view == view ? this : new DeserializationConfig(this, view);
    }

    @Override
    public DeserializationConfig with(ContextAttributes attrs) {
        return attrs == this._attributes ? this : new DeserializationConfig(this, attrs);
    }

    public DeserializationConfig with(DeserializationFeature feature) {
        int newDeserFeatures = this._deserFeatures | feature.getMask();
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig with(DeserializationFeature first, DeserializationFeature ... features) {
        int newDeserFeatures = this._deserFeatures | first.getMask();
        for (DeserializationFeature f : features) {
            newDeserFeatures |= f.getMask();
        }
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig withFeatures(DeserializationFeature ... features) {
        int newDeserFeatures = this._deserFeatures;
        for (DeserializationFeature f : features) {
            newDeserFeatures |= f.getMask();
        }
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig without(DeserializationFeature feature) {
        int newDeserFeatures = this._deserFeatures & ~feature.getMask();
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig without(DeserializationFeature first, DeserializationFeature ... features) {
        int newDeserFeatures = this._deserFeatures & ~first.getMask();
        for (DeserializationFeature f : features) {
            newDeserFeatures &= ~f.getMask();
        }
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig withoutFeatures(DeserializationFeature ... features) {
        int newDeserFeatures = this._deserFeatures;
        for (DeserializationFeature f : features) {
            newDeserFeatures &= ~f.getMask();
        }
        return newDeserFeatures == this._deserFeatures ? this : new DeserializationConfig(this, this._mapperFeatures, newDeserFeatures, this._parserFeatures, this._parserFeaturesToChange, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig with(JsonParser.Feature feature) {
        int newSet = this._parserFeatures | feature.getMask();
        int newMask = this._parserFeaturesToChange | feature.getMask();
        return this._parserFeatures == newSet && this._parserFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, newSet, newMask, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig withFeatures(JsonParser.Feature ... features) {
        int newSet = this._parserFeatures;
        int newMask = this._parserFeaturesToChange;
        for (JsonParser.Feature f : features) {
            int mask = f.getMask();
            newSet |= mask;
            newMask |= mask;
        }
        return this._parserFeatures == newSet && this._parserFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, newSet, newMask, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig without(JsonParser.Feature feature) {
        int newSet = this._parserFeatures & ~feature.getMask();
        int newMask = this._parserFeaturesToChange | feature.getMask();
        return this._parserFeatures == newSet && this._parserFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, newSet, newMask, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig withoutFeatures(JsonParser.Feature ... features) {
        int newSet = this._parserFeatures;
        int newMask = this._parserFeaturesToChange;
        for (JsonParser.Feature f : features) {
            int mask = f.getMask();
            newSet &= ~mask;
            newMask |= mask;
        }
        return this._parserFeatures == newSet && this._parserFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, newSet, newMask, this._formatReadFeatures, this._formatReadFeaturesToChange);
    }

    public DeserializationConfig with(FormatFeature feature) {
        if (feature instanceof JsonReadFeature) {
            return this._withJsonReadFeatures(feature);
        }
        int newSet = this._formatReadFeatures | feature.getMask();
        int newMask = this._formatReadFeaturesToChange | feature.getMask();
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, this._parserFeatures, this._parserFeaturesToChange, newSet, newMask);
    }

    public DeserializationConfig withFeatures(FormatFeature ... features) {
        if (features.length > 0 && features[0] instanceof JsonReadFeature) {
            return this._withJsonReadFeatures(features);
        }
        int newSet = this._formatReadFeatures;
        int newMask = this._formatReadFeaturesToChange;
        for (FormatFeature f : features) {
            int mask = f.getMask();
            newSet |= mask;
            newMask |= mask;
        }
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, this._parserFeatures, this._parserFeaturesToChange, newSet, newMask);
    }

    public DeserializationConfig without(FormatFeature feature) {
        if (feature instanceof JsonReadFeature) {
            return this._withoutJsonReadFeatures(feature);
        }
        int newSet = this._formatReadFeatures & ~feature.getMask();
        int newMask = this._formatReadFeaturesToChange | feature.getMask();
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, this._parserFeatures, this._parserFeaturesToChange, newSet, newMask);
    }

    public DeserializationConfig withoutFeatures(FormatFeature ... features) {
        if (features.length > 0 && features[0] instanceof JsonReadFeature) {
            return this._withoutJsonReadFeatures(features);
        }
        int newSet = this._formatReadFeatures;
        int newMask = this._formatReadFeaturesToChange;
        for (FormatFeature f : features) {
            int mask = f.getMask();
            newSet &= ~mask;
            newMask |= mask;
        }
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, this._parserFeatures, this._parserFeaturesToChange, newSet, newMask);
    }

    private DeserializationConfig _withJsonReadFeatures(FormatFeature ... features) {
        int parserSet = this._parserFeatures;
        int parserMask = this._parserFeaturesToChange;
        int newSet = this._formatReadFeatures;
        int newMask = this._formatReadFeaturesToChange;
        for (FormatFeature f : features) {
            JsonParser.Feature oldF;
            int mask = f.getMask();
            newSet |= mask;
            newMask |= mask;
            if (!(f instanceof JsonReadFeature) || (oldF = ((JsonReadFeature)f).mappedFeature()) == null) continue;
            int pmask = oldF.getMask();
            parserSet |= pmask;
            parserMask |= pmask;
        }
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask && this._parserFeatures == parserSet && this._parserFeaturesToChange == parserMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, parserSet, parserMask, newSet, newMask);
    }

    private DeserializationConfig _withoutJsonReadFeatures(FormatFeature ... features) {
        int parserSet = this._parserFeatures;
        int parserMask = this._parserFeaturesToChange;
        int newSet = this._formatReadFeatures;
        int newMask = this._formatReadFeaturesToChange;
        for (FormatFeature f : features) {
            JsonParser.Feature oldF;
            int mask = f.getMask();
            newSet &= ~mask;
            newMask |= mask;
            if (!(f instanceof JsonReadFeature) || (oldF = ((JsonReadFeature)f).mappedFeature()) == null) continue;
            int pmask = oldF.getMask();
            parserSet &= ~pmask;
            parserMask |= pmask;
        }
        return this._formatReadFeatures == newSet && this._formatReadFeaturesToChange == newMask && this._parserFeatures == parserSet && this._parserFeaturesToChange == parserMask ? this : new DeserializationConfig(this, this._mapperFeatures, this._deserFeatures, parserSet, parserMask, newSet, newMask);
    }

    public DeserializationConfig with(JsonNodeFactory f) {
        if (this._nodeFactory == f) {
            return this;
        }
        return new DeserializationConfig(this, f);
    }

    public DeserializationConfig withHandler(DeserializationProblemHandler h) {
        if (LinkedNode.contains(this._problemHandlers, h)) {
            return this;
        }
        return new DeserializationConfig(this, new LinkedNode<DeserializationProblemHandler>(h, this._problemHandlers));
    }

    public DeserializationConfig withNoProblemHandlers() {
        if (this._problemHandlers == null) {
            return this;
        }
        return new DeserializationConfig(this, (LinkedNode<DeserializationProblemHandler>)null);
    }

    public void initialize(JsonParser p) {
        if (this._parserFeaturesToChange != 0) {
            p.overrideStdFeatures(this._parserFeatures, this._parserFeaturesToChange);
        }
        if (this._formatReadFeaturesToChange != 0) {
            p.overrideFormatFeatures(this._formatReadFeatures, this._formatReadFeaturesToChange);
        }
    }

    @Override
    public boolean useRootWrapping() {
        if (this._rootName != null) {
            return !this._rootName.isEmpty();
        }
        return this.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE);
    }

    public final boolean isEnabled(DeserializationFeature f) {
        return (this._deserFeatures & f.getMask()) != 0;
    }

    public final boolean isEnabled(JsonParser.Feature f, JsonFactory factory) {
        int mask = f.getMask();
        if ((this._parserFeaturesToChange & mask) != 0) {
            return (this._parserFeatures & f.getMask()) != 0;
        }
        return factory.isEnabled(f);
    }

    public final boolean hasDeserializationFeatures(int featureMask) {
        return (this._deserFeatures & featureMask) == featureMask;
    }

    public final boolean hasSomeOfFeatures(int featureMask) {
        return (this._deserFeatures & featureMask) != 0;
    }

    public final int getDeserializationFeatures() {
        return this._deserFeatures;
    }

    public final boolean requiresFullValue() {
        return DeserializationFeature.FAIL_ON_TRAILING_TOKENS.enabledIn(this._deserFeatures);
    }

    public LinkedNode<DeserializationProblemHandler> getProblemHandlers() {
        return this._problemHandlers;
    }

    public final JsonNodeFactory getNodeFactory() {
        return this._nodeFactory;
    }

    public <T extends BeanDescription> T introspect(JavaType type) {
        return (T)this.getClassIntrospector().forDeserialization(this, type, this);
    }

    public <T extends BeanDescription> T introspectForCreation(JavaType type) {
        return (T)this.getClassIntrospector().forCreation(this, type, this);
    }

    public <T extends BeanDescription> T introspectForBuilder(JavaType type) {
        return (T)this.getClassIntrospector().forDeserializationWithBuilder(this, type, this);
    }

    public TypeDeserializer findTypeDeserializer(JavaType baseType) throws JsonMappingException {
        BeanDescription bean = this.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        TypeResolverBuilder<?> b = this.getAnnotationIntrospector().findTypeResolver(this, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = this.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = this.getSubtypeResolver().collectAndResolveSubtypesByTypeId(this, ac);
        }
        return b.buildTypeDeserializer(this, baseType, subtypes);
    }
}

