/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.AbstractDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.impl.JDKValueInstantiators;
import com.fasterxml.jackson.databind.deser.impl.JavaUtilCollectionsDeserializers;
import com.fasterxml.jackson.databind.deser.std.ArrayBlockingQueueDeserializer;
import com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumMapDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumSetDeserializer;
import com.fasterxml.jackson.databind.deser.std.JdkDeserializers;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapEntryDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.PrimitiveArrayDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializers;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.deser.std.TokenBufferDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ext.OptionalHandlerFactory;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BasicDeserializerFactory
extends DeserializerFactory
implements Serializable {
    private static final Class<?> CLASS_OBJECT = Object.class;
    private static final Class<?> CLASS_STRING = String.class;
    private static final Class<?> CLASS_CHAR_SEQUENCE = CharSequence.class;
    private static final Class<?> CLASS_ITERABLE = Iterable.class;
    private static final Class<?> CLASS_MAP_ENTRY = Map.Entry.class;
    private static final Class<?> CLASS_SERIALIZABLE = Serializable.class;
    protected static final PropertyName UNWRAPPED_CREATOR_PARAM_NAME = new PropertyName("@JsonUnwrapped");
    protected final DeserializerFactoryConfig _factoryConfig;

    protected BasicDeserializerFactory(DeserializerFactoryConfig config) {
        this._factoryConfig = config;
    }

    public DeserializerFactoryConfig getFactoryConfig() {
        return this._factoryConfig;
    }

    protected abstract DeserializerFactory withConfig(DeserializerFactoryConfig var1);

    @Override
    public final DeserializerFactory withAdditionalDeserializers(Deserializers additional) {
        return this.withConfig(this._factoryConfig.withAdditionalDeserializers(additional));
    }

    @Override
    public final DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers additional) {
        return this.withConfig(this._factoryConfig.withAdditionalKeyDeserializers(additional));
    }

    @Override
    public final DeserializerFactory withDeserializerModifier(BeanDeserializerModifier modifier) {
        return this.withConfig(this._factoryConfig.withDeserializerModifier(modifier));
    }

    @Override
    public final DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver resolver) {
        return this.withConfig(this._factoryConfig.withAbstractTypeResolver(resolver));
    }

    @Override
    public final DeserializerFactory withValueInstantiators(ValueInstantiators instantiators) {
        return this.withConfig(this._factoryConfig.withValueInstantiators(instantiators));
    }

    @Override
    public JavaType mapAbstractType(DeserializationConfig config, JavaType type) throws JsonMappingException {
        JavaType next;
        while ((next = this._mapAbstractType2(config, type)) != null) {
            Class<?> nextCls;
            Class<?> prevCls = type.getRawClass();
            if (prevCls == (nextCls = next.getRawClass()) || !prevCls.isAssignableFrom(nextCls)) {
                throw new IllegalArgumentException("Invalid abstract type resolution from " + type + " to " + next + ": latter is not a subtype of former");
            }
            type = next;
        }
        return type;
    }

    private JavaType _mapAbstractType2(DeserializationConfig config, JavaType type) throws JsonMappingException {
        Class<?> currClass = type.getRawClass();
        if (this._factoryConfig.hasAbstractTypeResolvers()) {
            for (AbstractTypeResolver resolver : this._factoryConfig.abstractTypeResolvers()) {
                JavaType concrete = resolver.findTypeMapping(config, type);
                if (concrete == null || concrete.hasRawClass(currClass)) continue;
                return concrete;
            }
        }
        return null;
    }

    @Override
    public ValueInstantiator findValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        ValueInstantiator instantiator = null;
        AnnotatedClass ac = beanDesc.getClassInfo();
        Object instDef = ctxt.getAnnotationIntrospector().findValueInstantiator(ac);
        if (instDef != null) {
            instantiator = this._valueInstantiatorInstance(config, ac, instDef);
        }
        if (instantiator == null && (instantiator = JDKValueInstantiators.findStdValueInstantiator(config, beanDesc.getBeanClass())) == null) {
            instantiator = this._constructDefaultValueInstantiator(ctxt, beanDesc);
        }
        if (this._factoryConfig.hasValueInstantiators()) {
            for (ValueInstantiators insts : this._factoryConfig.valueInstantiators()) {
                instantiator = insts.findValueInstantiator(config, beanDesc, instantiator);
                if (instantiator != null) continue;
                ctxt.reportBadTypeDefinition(beanDesc, "Broken registered ValueInstantiators (of type %s): returned null ValueInstantiator", insts.getClass().getName());
            }
        }
        if (instantiator.getIncompleteParameter() != null) {
            AnnotatedParameter nonAnnotatedParam = instantiator.getIncompleteParameter();
            AnnotatedWithParams ctor = nonAnnotatedParam.getOwner();
            throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of constructor " + ctor + " has no property name annotation; must have name when multiple-parameter constructor annotated as Creator");
        }
        return instantiator;
    }

    protected ValueInstantiator _constructDefaultValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        CreatorCollector creators = new CreatorCollector(beanDesc, ctxt.getConfig());
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        DeserializationConfig config = ctxt.getConfig();
        VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker(beanDesc.getBeanClass(), beanDesc.getClassInfo());
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorDefs = this._findCreatorsFromProperties(ctxt, beanDesc);
        this._addDeserializerFactoryMethods(ctxt, beanDesc, vchecker, intr, creators, creatorDefs);
        if (beanDesc.getType().isConcrete()) {
            this._addDeserializerConstructors(ctxt, beanDesc, vchecker, intr, creators, creatorDefs);
        }
        return creators.constructValueInstantiator(ctxt);
    }

    protected Map<AnnotatedWithParams, BeanPropertyDefinition[]> _findCreatorsFromProperties(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> result = Collections.emptyMap();
        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            Iterator<AnnotatedParameter> it = propDef.getConstructorParameters();
            while (it.hasNext()) {
                AnnotatedParameter param = it.next();
                AnnotatedWithParams owner = param.getOwner();
                BeanPropertyDefinition[] defs = result.get(owner);
                int index = param.getIndex();
                if (defs == null) {
                    if (result.isEmpty()) {
                        result = new LinkedHashMap<AnnotatedWithParams, BeanPropertyDefinition[]>();
                    }
                    defs = new BeanPropertyDefinition[owner.getParameterCount()];
                    result.put(owner, defs);
                } else if (defs[index] != null) {
                    ctxt.reportBadTypeDefinition(beanDesc, "Conflict: parameter #%d of %s bound to more than one property; %s vs %s", index, owner, defs[index], propDef);
                }
                defs[index] = propDef;
            }
        }
        return result;
    }

    public ValueInstantiator _valueInstantiatorInstance(DeserializationConfig config, Annotated annotated, Object instDef) throws JsonMappingException {
        ValueInstantiator inst;
        if (instDef == null) {
            return null;
        }
        if (instDef instanceof ValueInstantiator) {
            return (ValueInstantiator)instDef;
        }
        if (!(instDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned key deserializer definition of type " + instDef.getClass().getName() + "; expected type KeyDeserializer or Class<KeyDeserializer> instead");
        }
        Class instClass = (Class)instDef;
        if (ClassUtil.isBogusClass(instClass)) {
            return null;
        }
        if (!ValueInstantiator.class.isAssignableFrom(instClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + instClass.getName() + "; expected Class<ValueInstantiator>");
        }
        HandlerInstantiator hi = config.getHandlerInstantiator();
        if (hi != null && (inst = hi.valueInstantiatorInstance(config, annotated, instClass)) != null) {
            return inst;
        }
        return (ValueInstantiator)ClassUtil.createInstance(instClass, config.canOverrideAccessModifiers());
    }

    protected void _addDeserializerConstructors(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams) throws JsonMappingException {
        boolean isNonStaticInnerClass = beanDesc.isNonStaticInnerClass();
        if (isNonStaticInnerClass) {
            return;
        }
        AnnotatedConstructor defaultCtor = beanDesc.findDefaultConstructor();
        if (defaultCtor != null && (!creators.hasDefaultCreator() || this._hasCreatorAnnotation(ctxt, defaultCtor))) {
            creators.setDefaultCreator(defaultCtor);
        }
        LinkedList<CreatorCandidate> nonAnnotated = new LinkedList<CreatorCandidate>();
        int explCount = 0;
        for (AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            JsonCreator.Mode creatorMode = intr.findCreatorAnnotation(ctxt.getConfig(), ctor);
            if (JsonCreator.Mode.DISABLED == creatorMode) continue;
            if (creatorMode == null) {
                if (!vchecker.isCreatorVisible(ctor)) continue;
                nonAnnotated.add(CreatorCandidate.construct(intr, ctor, creatorParams.get(ctor)));
                continue;
            }
            switch (creatorMode) {
                case DELEGATING: {
                    this._addExplicitDelegatingCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, ctor, null));
                    break;
                }
                case PROPERTIES: {
                    this._addExplicitPropertyCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, ctor, creatorParams.get(ctor)));
                    break;
                }
                default: {
                    this._addExplicitAnyCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, ctor, creatorParams.get(ctor)));
                }
            }
            ++explCount;
        }
        if (explCount > 0) {
            return;
        }
        LinkedList<AnnotatedWithParams> implicitCtors = null;
        for (CreatorCandidate candidate : nonAnnotated) {
            int argCount = candidate.paramCount();
            AnnotatedWithParams ctor = candidate.creator();
            if (argCount == 1) {
                BeanPropertyDefinition propDef = candidate.propertyDef(0);
                boolean useProps = this._checkIfCreatorPropertyBased(intr, ctor, propDef);
                if (useProps) {
                    SettableBeanProperty[] properties = new SettableBeanProperty[1];
                    PropertyName name = candidate.paramName(0);
                    properties[0] = this.constructCreatorProperty(ctxt, beanDesc, name, 0, candidate.parameter(0), candidate.injection(0));
                    creators.addPropertyCreator(ctor, false, properties);
                    continue;
                }
                this._handleSingleArgumentCreator(creators, ctor, false, vchecker.isCreatorVisible(ctor));
                if (propDef == null) continue;
                ((POJOPropertyBuilder)propDef).removeConstructors();
                continue;
            }
            int nonAnnotatedParamIndex = -1;
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            int explicitNameCount = 0;
            int implicitWithCreatorCount = 0;
            int injectCount = 0;
            for (int i = 0; i < argCount; ++i) {
                PropertyName name;
                AnnotatedParameter param = ctor.getParameter(i);
                BeanPropertyDefinition propDef = candidate.propertyDef(i);
                JacksonInject.Value injectable = intr.findInjectableValue(param);
                PropertyName propertyName = name = propDef == null ? null : propDef.getFullName();
                if (propDef != null && propDef.isExplicitlyNamed()) {
                    ++explicitNameCount;
                    properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, i, param, injectable);
                    continue;
                }
                if (injectable != null) {
                    ++injectCount;
                    properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, i, param, injectable);
                    continue;
                }
                NameTransformer unwrapper = intr.findUnwrappingNameTransformer(param);
                if (unwrapper != null) {
                    this._reportUnwrappedCreatorProperty(ctxt, beanDesc, param);
                    continue;
                }
                if (nonAnnotatedParamIndex >= 0) continue;
                nonAnnotatedParamIndex = i;
            }
            int namedCount = explicitNameCount + implicitWithCreatorCount;
            if (explicitNameCount > 0 || injectCount > 0) {
                if (namedCount + injectCount == argCount) {
                    creators.addPropertyCreator(ctor, false, properties);
                    continue;
                }
                if (explicitNameCount == 0 && injectCount + 1 == argCount) {
                    creators.addDelegatingCreator(ctor, false, properties, 0);
                    continue;
                }
                PropertyName impl = candidate.findImplicitParamName(nonAnnotatedParamIndex);
                if (impl == null || impl.isEmpty()) {
                    ctxt.reportBadTypeDefinition(beanDesc, "Argument #%d of constructor %s has no property name annotation; must have name when multiple-parameter constructor annotated as Creator", nonAnnotatedParamIndex, ctor);
                }
            }
            if (creators.hasDefaultCreator()) continue;
            if (implicitCtors == null) {
                implicitCtors = new LinkedList<AnnotatedWithParams>();
            }
            implicitCtors.add(ctor);
        }
        if (implicitCtors != null && !creators.hasDelegatingCreator() && !creators.hasPropertyBasedCreator()) {
            this._checkImplicitlyNamedConstructors(ctxt, beanDesc, vchecker, intr, creators, implicitCtors);
        }
    }

    protected void _addExplicitDelegatingCreator(DeserializationContext ctxt, BeanDescription beanDesc, CreatorCollector creators, CreatorCandidate candidate) throws JsonMappingException {
        int ix = -1;
        int argCount = candidate.paramCount();
        SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
        for (int i = 0; i < argCount; ++i) {
            AnnotatedParameter param = candidate.parameter(i);
            JacksonInject.Value injectId = candidate.injection(i);
            if (injectId != null) {
                properties[i] = this.constructCreatorProperty(ctxt, beanDesc, null, i, param, injectId);
                continue;
            }
            if (ix < 0) {
                ix = i;
                continue;
            }
            ctxt.reportBadTypeDefinition(beanDesc, "More than one argument (#%d and #%d) left as delegating for Creator %s: only one allowed", ix, i, candidate);
        }
        if (ix < 0) {
            ctxt.reportBadTypeDefinition(beanDesc, "No argument left as delegating for Creator %s: exactly one required", candidate);
        }
        if (argCount == 1) {
            this._handleSingleArgumentCreator(creators, candidate.creator(), true, true);
            BeanPropertyDefinition paramDef = candidate.propertyDef(0);
            if (paramDef != null) {
                ((POJOPropertyBuilder)paramDef).removeConstructors();
            }
            return;
        }
        creators.addDelegatingCreator(candidate.creator(), true, properties, ix);
    }

    protected void _addExplicitPropertyCreator(DeserializationContext ctxt, BeanDescription beanDesc, CreatorCollector creators, CreatorCandidate candidate) throws JsonMappingException {
        int paramCount = candidate.paramCount();
        SettableBeanProperty[] properties = new SettableBeanProperty[paramCount];
        for (int i = 0; i < paramCount; ++i) {
            JacksonInject.Value injectId = candidate.injection(i);
            AnnotatedParameter param = candidate.parameter(i);
            PropertyName name = candidate.paramName(i);
            if (name == null) {
                NameTransformer unwrapper = ctxt.getAnnotationIntrospector().findUnwrappingNameTransformer(param);
                if (unwrapper != null) {
                    this._reportUnwrappedCreatorProperty(ctxt, beanDesc, param);
                }
                if ((name = candidate.findImplicitParamName(i)) == null && injectId == null) {
                    ctxt.reportBadTypeDefinition(beanDesc, "Argument #%d has no property name, is not Injectable: can not use as Creator %s", i, candidate);
                }
            }
            properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
        }
        creators.addPropertyCreator(candidate.creator(), true, properties);
    }

    protected void _addExplicitAnyCreator(DeserializationContext ctxt, BeanDescription beanDesc, CreatorCollector creators, CreatorCandidate candidate) throws JsonMappingException {
        boolean useProps;
        if (1 != candidate.paramCount()) {
            int oneNotInjected = candidate.findOnlyParamWithoutInjection();
            if (oneNotInjected >= 0 && candidate.paramName(oneNotInjected) == null) {
                this._addExplicitDelegatingCreator(ctxt, beanDesc, creators, candidate);
                return;
            }
            this._addExplicitPropertyCreator(ctxt, beanDesc, creators, candidate);
            return;
        }
        AnnotatedParameter param = candidate.parameter(0);
        JacksonInject.Value injectId = candidate.injection(0);
        PropertyName paramName = candidate.explicitParamName(0);
        BeanPropertyDefinition paramDef = candidate.propertyDef(0);
        boolean bl = useProps = paramName != null || injectId != null;
        if (!useProps && paramDef != null) {
            paramName = candidate.paramName(0);
            boolean bl2 = useProps = paramName != null && paramDef.couldSerialize();
        }
        if (useProps) {
            SettableBeanProperty[] properties = new SettableBeanProperty[]{this.constructCreatorProperty(ctxt, beanDesc, paramName, 0, param, injectId)};
            creators.addPropertyCreator(candidate.creator(), true, properties);
            return;
        }
        this._handleSingleArgumentCreator(creators, candidate.creator(), true, true);
        if (paramDef != null) {
            ((POJOPropertyBuilder)paramDef).removeConstructors();
        }
    }

    private boolean _checkIfCreatorPropertyBased(AnnotationIntrospector intr, AnnotatedWithParams creator, BeanPropertyDefinition propDef) {
        String implName;
        if (propDef != null && propDef.isExplicitlyNamed() || intr.findInjectableValue(creator.getParameter(0)) != null) {
            return true;
        }
        return propDef != null && (implName = propDef.getName()) != null && !implName.isEmpty() && propDef.couldSerialize();
    }

    private void _checkImplicitlyNamedConstructors(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, List<AnnotatedWithParams> implicitCtors) throws JsonMappingException {
        AnnotatedWithParams found = null;
        SettableBeanProperty[] foundProps = null;
        block0: for (AnnotatedWithParams ctor : implicitCtors) {
            if (!vchecker.isCreatorVisible(ctor)) continue;
            int argCount = ctor.getParameterCount();
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                AnnotatedParameter param = ctor.getParameter(i);
                PropertyName name = this._findParamName(param, intr);
                if (name == null || name.isEmpty()) continue block0;
                properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, param.getIndex(), param, null);
            }
            if (found != null) {
                found = null;
                break;
            }
            found = ctor;
            foundProps = properties;
        }
        if (found != null) {
            creators.addPropertyCreator(found, false, foundProps);
            BasicBeanDescription bbd = (BasicBeanDescription)beanDesc;
            for (void var13_16 : foundProps) {
                PropertyName pn = var13_16.getFullName();
                if (bbd.hasProperty(pn)) continue;
                SimpleBeanPropertyDefinition newDef = SimpleBeanPropertyDefinition.construct(ctxt.getConfig(), var13_16.getMember(), pn);
                bbd.addProperty(newDef);
            }
        }
    }

    protected void _addDeserializerFactoryMethods(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams) throws JsonMappingException {
        LinkedList<CreatorCandidate> nonAnnotated = new LinkedList<CreatorCandidate>();
        int explCount = 0;
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            JsonCreator.Mode creatorMode = intr.findCreatorAnnotation(ctxt.getConfig(), factory);
            int argCount = factory.getParameterCount();
            if (creatorMode == null) {
                if (argCount != 1 || !vchecker.isCreatorVisible(factory)) continue;
                nonAnnotated.add(CreatorCandidate.construct(intr, factory, null));
                continue;
            }
            if (creatorMode == JsonCreator.Mode.DISABLED) continue;
            if (argCount == 0) {
                creators.setDefaultCreator(factory);
                continue;
            }
            switch (creatorMode) {
                case DELEGATING: {
                    this._addExplicitDelegatingCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, factory, null));
                    break;
                }
                case PROPERTIES: {
                    this._addExplicitPropertyCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, factory, creatorParams.get(factory)));
                    break;
                }
                default: {
                    this._addExplicitAnyCreator(ctxt, beanDesc, creators, CreatorCandidate.construct(intr, factory, creatorParams.get(factory)));
                }
            }
            ++explCount;
        }
        if (explCount > 0) {
            return;
        }
        for (CreatorCandidate candidate : nonAnnotated) {
            int argCount = candidate.paramCount();
            AnnotatedWithParams factory = candidate.creator();
            BeanPropertyDefinition[] propDefs = creatorParams.get(factory);
            if (argCount != 1) continue;
            BeanPropertyDefinition argDef = candidate.propertyDef(0);
            boolean useProps = this._checkIfCreatorPropertyBased(intr, factory, argDef);
            if (!useProps) {
                this._handleSingleArgumentCreator(creators, factory, false, vchecker.isCreatorVisible(factory));
                if (argDef == null) continue;
                ((POJOPropertyBuilder)argDef).removeConstructors();
                continue;
            }
            AnnotatedParameter nonAnnotatedParam = null;
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            int implicitNameCount = 0;
            int explicitNameCount = 0;
            int injectCount = 0;
            for (int i = 0; i < argCount; ++i) {
                PropertyName name;
                AnnotatedParameter param = factory.getParameter(i);
                BeanPropertyDefinition propDef = propDefs == null ? null : propDefs[i];
                JacksonInject.Value injectable = intr.findInjectableValue(param);
                PropertyName propertyName = name = propDef == null ? null : propDef.getFullName();
                if (propDef != null && propDef.isExplicitlyNamed()) {
                    ++explicitNameCount;
                    properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, i, param, injectable);
                    continue;
                }
                if (injectable != null) {
                    ++injectCount;
                    properties[i] = this.constructCreatorProperty(ctxt, beanDesc, name, i, param, injectable);
                    continue;
                }
                NameTransformer unwrapper = intr.findUnwrappingNameTransformer(param);
                if (unwrapper != null) {
                    this._reportUnwrappedCreatorProperty(ctxt, beanDesc, param);
                    continue;
                }
                if (nonAnnotatedParam != null) continue;
                nonAnnotatedParam = param;
            }
            int namedCount = explicitNameCount + implicitNameCount;
            if (explicitNameCount <= 0 && injectCount <= 0) continue;
            if (namedCount + injectCount == argCount) {
                creators.addPropertyCreator(factory, false, properties);
                continue;
            }
            if (explicitNameCount == 0 && injectCount + 1 == argCount) {
                creators.addDelegatingCreator(factory, false, properties, 0);
                continue;
            }
            ctxt.reportBadTypeDefinition(beanDesc, "Argument #%d of factory method %s has no property name annotation; must have name when multiple-parameter constructor annotated as Creator", nonAnnotatedParam.getIndex(), factory);
        }
    }

    protected boolean _handleSingleArgumentCreator(CreatorCollector creators, AnnotatedWithParams ctor, boolean isCreator, boolean isVisible) {
        Class<?> type = ctor.getRawParameterType(0);
        if (type == String.class || type == CLASS_CHAR_SEQUENCE) {
            if (isCreator || isVisible) {
                creators.addStringCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == Integer.TYPE || type == Integer.class) {
            if (isCreator || isVisible) {
                creators.addIntCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == Long.TYPE || type == Long.class) {
            if (isCreator || isVisible) {
                creators.addLongCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == Double.TYPE || type == Double.class) {
            if (isCreator || isVisible) {
                creators.addDoubleCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == Boolean.TYPE || type == Boolean.class) {
            if (isCreator || isVisible) {
                creators.addBooleanCreator(ctor, isCreator);
            }
            return true;
        }
        if (isCreator) {
            creators.addDelegatingCreator(ctor, isCreator, null, 0);
            return true;
        }
        return false;
    }

    protected void _reportUnwrappedCreatorProperty(DeserializationContext ctxt, BeanDescription beanDesc, AnnotatedParameter param) throws JsonMappingException {
        ctxt.reportBadDefinition(beanDesc.getType(), String.format("Cannot define Creator parameter %d as `@JsonUnwrapped`: combination not yet supported", param.getIndex()));
    }

    protected SettableBeanProperty constructCreatorProperty(DeserializationContext ctxt, BeanDescription beanDesc, PropertyName name, int index, AnnotatedParameter param, JacksonInject.Value injectable) throws JsonMappingException {
        PropertyMetadata metadata;
        DeserializationConfig config = ctxt.getConfig();
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            metadata = PropertyMetadata.STD_REQUIRED_OR_OPTIONAL;
        } else {
            Boolean b = intr.hasRequiredMarker(param);
            String desc = intr.findPropertyDescription(param);
            Integer idx = intr.findPropertyIndex(param);
            String def = intr.findPropertyDefaultValue(param);
            metadata = PropertyMetadata.construct(b, desc, idx, def);
        }
        JavaType type = this.resolveMemberAndTypeAnnotations(ctxt, param, param.getType());
        BeanProperty.Std property = new BeanProperty.Std(name, type, intr.findWrapperName(param), param, metadata);
        TypeDeserializer typeDeser = (TypeDeserializer)type.getTypeHandler();
        if (typeDeser == null) {
            typeDeser = this.findTypeDeserializer(config, type);
        }
        metadata = this._getSetterInfo(ctxt, property, metadata);
        SettableBeanProperty prop = CreatorProperty.construct(name, type, property.getWrapperName(), typeDeser, beanDesc.getClassAnnotations(), param, index, injectable, metadata);
        JsonDeserializer<?> deser = this.findDeserializerFromAnnotation(ctxt, param);
        if (deser == null) {
            deser = (JsonDeserializer<?>)type.getValueHandler();
        }
        if (deser != null) {
            deser = ctxt.handlePrimaryContextualization(deser, prop, type);
            prop = ((SettableBeanProperty)prop).withValueDeserializer(deser);
        }
        return prop;
    }

    private PropertyName _findParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
        if (param != null && intr != null) {
            PropertyName name = intr.findNameForDeserialization(param);
            if (name != null) {
                return name;
            }
            String str = intr.findImplicitPropertyName(param);
            if (str != null && !str.isEmpty()) {
                return PropertyName.construct(str);
            }
        }
        return null;
    }

    protected PropertyMetadata _getSetterInfo(DeserializationContext ctxt, BeanProperty prop, PropertyMetadata metadata) {
        JsonSetter.Value setterInfo;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        DeserializationConfig config = ctxt.getConfig();
        boolean needMerge = true;
        Nulls valueNulls = null;
        Nulls contentNulls = null;
        AnnotatedMember prim = prop.getMember();
        if (prim != null) {
            ConfigOverride co;
            JsonSetter.Value setterInfo2;
            if (intr != null && (setterInfo = intr.findSetterInfo(prim)) != null) {
                valueNulls = setterInfo.nonDefaultValueNulls();
                contentNulls = setterInfo.nonDefaultContentNulls();
            }
            if ((needMerge || valueNulls == null || contentNulls == null) && (setterInfo2 = (co = config.getConfigOverride(prop.getType().getRawClass())).getSetterInfo()) != null) {
                if (valueNulls == null) {
                    valueNulls = setterInfo2.nonDefaultValueNulls();
                }
                if (contentNulls == null) {
                    contentNulls = setterInfo2.nonDefaultContentNulls();
                }
            }
        }
        if (needMerge || valueNulls == null || contentNulls == null) {
            setterInfo = config.getDefaultSetterInfo();
            if (valueNulls == null) {
                valueNulls = setterInfo.nonDefaultValueNulls();
            }
            if (contentNulls == null) {
                contentNulls = setterInfo.nonDefaultContentNulls();
            }
        }
        if (valueNulls != null || contentNulls != null) {
            metadata = metadata.withNulls(valueNulls, contentNulls);
        }
        return metadata;
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext ctxt, ArrayType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer deser;
        DeserializationConfig config = ctxt.getConfig();
        JavaType elemType = type.getContentType();
        JsonDeserializer contentDeser = (JsonDeserializer)elemType.getValueHandler();
        TypeDeserializer elemTypeDeser = (TypeDeserializer)elemType.getTypeHandler();
        if (elemTypeDeser == null) {
            elemTypeDeser = this.findTypeDeserializer(config, elemType);
        }
        if ((deser = this._findCustomArrayDeserializer(type, config, beanDesc, elemTypeDeser, contentDeser)) == null) {
            if (contentDeser == null) {
                Class<?> raw = elemType.getRawClass();
                if (elemType.isPrimitive()) {
                    return PrimitiveArrayDeserializers.forType(raw);
                }
                if (raw == String.class) {
                    return StringArrayDeserializer.instance;
                }
            }
            deser = new ObjectArrayDeserializer((JavaType)type, contentDeser, elemTypeDeser);
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyArrayDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext ctxt, CollectionType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer deser;
        JavaType contentType = type.getContentType();
        JsonDeserializer contentDeser = (JsonDeserializer)contentType.getValueHandler();
        DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType);
        }
        if ((deser = this._findCustomCollectionDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser)) == null) {
            Class<?> collectionClass = type.getRawClass();
            if (contentDeser == null && EnumSet.class.isAssignableFrom(collectionClass)) {
                deser = new EnumSetDeserializer(contentType, null);
            }
        }
        if (deser == null) {
            if (type.isInterface() || type.isAbstract()) {
                CollectionType implType = this._mapAbstractCollectionType(type, config);
                if (implType == null) {
                    if (type.getTypeHandler() == null) {
                        throw new IllegalArgumentException("Cannot find a deserializer for non-concrete Collection type " + type);
                    }
                    deser = AbstractDeserializer.constructForNonPOJO(beanDesc);
                } else {
                    type = implType;
                    beanDesc = config.introspectForCreation(type);
                }
            }
            if (deser == null) {
                ValueInstantiator inst = this.findValueInstantiator(ctxt, beanDesc);
                if (!inst.canCreateUsingDefault()) {
                    if (type.hasRawClass(ArrayBlockingQueue.class)) {
                        return new ArrayBlockingQueueDeserializer(type, contentDeser, contentTypeDeser, inst);
                    }
                    deser = JavaUtilCollectionsDeserializers.findForCollection(ctxt, type);
                    if (deser != null) {
                        return deser;
                    }
                }
                deser = contentType.hasRawClass(String.class) ? new StringCollectionDeserializer((JavaType)type, contentDeser, inst) : new CollectionDeserializer(type, contentDeser, contentTypeDeser, inst);
            }
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyCollectionDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    protected CollectionType _mapAbstractCollectionType(JavaType type, DeserializationConfig config) {
        Class<?> collectionClass = ContainerDefaultMappings.findCollectionFallback(type);
        if (collectionClass != null) {
            return (CollectionType)config.getTypeFactory().constructSpecializedType(type, collectionClass, true);
        }
        return null;
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext ctxt, CollectionLikeType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser;
        JavaType contentType = type.getContentType();
        JsonDeserializer contentDeser = (JsonDeserializer)contentType.getValueHandler();
        DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType);
        }
        if ((deser = this._findCustomCollectionLikeDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser)) != null && this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyCollectionLikeDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer deser;
        DeserializationConfig config = ctxt.getConfig();
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        JsonDeserializer contentDeser = (JsonDeserializer)contentType.getValueHandler();
        KeyDeserializer keyDes = (KeyDeserializer)keyType.getValueHandler();
        TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType);
        }
        if ((deser = this._findCustomMapDeserializer(type, config, beanDesc, keyDes, contentTypeDeser, contentDeser)) == null) {
            ValueInstantiator inst;
            Class<?> mapClass = type.getRawClass();
            if (EnumMap.class.isAssignableFrom(mapClass)) {
                inst = mapClass == EnumMap.class ? null : this.findValueInstantiator(ctxt, beanDesc);
                if (!keyType.isEnumImplType()) {
                    throw new IllegalArgumentException("Cannot construct EnumMap; generic (key) type not available");
                }
                deser = new EnumMapDeserializer(type, inst, null, contentDeser, contentTypeDeser, null);
            }
            if (deser == null) {
                if (type.isInterface() || type.isAbstract()) {
                    MapType fallback = this._mapAbstractMapType(type, config);
                    if (fallback != null) {
                        type = fallback;
                        mapClass = type.getRawClass();
                        beanDesc = config.introspectForCreation(type);
                    } else {
                        if (type.getTypeHandler() == null) {
                            throw new IllegalArgumentException("Cannot find a deserializer for non-concrete Map type " + type);
                        }
                        deser = AbstractDeserializer.constructForNonPOJO(beanDesc);
                    }
                } else {
                    deser = JavaUtilCollectionsDeserializers.findForMap(ctxt, type);
                    if (deser != null) {
                        return deser;
                    }
                }
                if (deser == null) {
                    inst = this.findValueInstantiator(ctxt, beanDesc);
                    MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
                    JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class, beanDesc.getClassInfo());
                    Set<String> ignored = ignorals == null ? null : ignorals.findIgnoredForDeserialization();
                    md.setIgnorableProperties(ignored);
                    deser = md;
                }
            }
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyMapDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    protected MapType _mapAbstractMapType(JavaType type, DeserializationConfig config) {
        Class<?> mapClass = ContainerDefaultMappings.findMapFallback(type);
        if (mapClass != null) {
            return (MapType)config.getTypeFactory().constructSpecializedType(type, mapClass, true);
        }
        return null;
    }

    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext ctxt, MapLikeType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser;
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        DeserializationConfig config = ctxt.getConfig();
        JsonDeserializer contentDeser = (JsonDeserializer)contentType.getValueHandler();
        KeyDeserializer keyDes = (KeyDeserializer)keyType.getValueHandler();
        TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType);
        }
        if ((deser = this._findCustomMapLikeDeserializer(type, config, beanDesc, keyDes, contentTypeDeser, contentDeser)) != null && this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyMapLikeDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createEnumDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        Class<?> enumClass = type.getRawClass();
        JsonDeserializer deser = this._findCustomEnumDeserializer(enumClass, config, beanDesc);
        if (deser == null) {
            if (enumClass == Enum.class) {
                return AbstractDeserializer.constructForNonPOJO(beanDesc);
            }
            ValueInstantiator valueInstantiator = this._constructDefaultValueInstantiator(ctxt, beanDesc);
            SettableBeanProperty[] creatorProps = valueInstantiator == null ? null : valueInstantiator.getFromObjectArguments(ctxt.getConfig());
            for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
                if (!this._hasCreatorAnnotation(ctxt, factory)) continue;
                if (factory.getParameterCount() == 0) {
                    deser = EnumDeserializer.deserializerForNoArgsCreator(config, enumClass, factory);
                    break;
                }
                Class<?> returnType = factory.getRawReturnType();
                if (!returnType.isAssignableFrom(enumClass)) {
                    ctxt.reportBadDefinition(type, String.format("Invalid `@JsonCreator` annotated Enum factory method [%s]: needs to return compatible type", factory.toString()));
                }
                deser = EnumDeserializer.deserializerForCreator(config, enumClass, factory, valueInstantiator, creatorProps);
                break;
            }
            if (deser == null) {
                deser = new EnumDeserializer(this.constructEnumResolver(enumClass, config, beanDesc.findJsonValueAccessor()), (Boolean)config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS));
            }
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyEnumDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, JavaType nodeType, BeanDescription beanDesc) throws JsonMappingException {
        Class<?> nodeClass = nodeType.getRawClass();
        JsonDeserializer<?> custom = this._findCustomTreeNodeDeserializer(nodeClass, config, beanDesc);
        if (custom != null) {
            return custom;
        }
        return JsonNodeDeserializer.getDeserializer(nodeClass);
    }

    @Override
    public JsonDeserializer<?> createReferenceDeserializer(DeserializationContext ctxt, ReferenceType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser;
        JavaType contentType = type.getContentType();
        JsonDeserializer contentDeser = (JsonDeserializer)contentType.getValueHandler();
        DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = (TypeDeserializer)contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = this.findTypeDeserializer(config, contentType);
        }
        if ((deser = this._findCustomReferenceDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser)) == null && type.isTypeOrSubTypeOf(AtomicReference.class)) {
            Class<?> rawType = type.getRawClass();
            ValueInstantiator inst = rawType == AtomicReference.class ? null : this.findValueInstantiator(ctxt, beanDesc);
            return new AtomicReferenceDeserializer(type, inst, contentTypeDeser, contentDeser);
        }
        if (deser != null && this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyReferenceDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType) throws JsonMappingException {
        JavaType defaultType;
        BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, ac);
        }
        if (b.getDefaultImpl() == null && baseType.isAbstract() && (defaultType = this.mapAbstractType(config, baseType)) != null && !defaultType.hasRawClass(baseType.getRawClass())) {
            b = b.defaultImpl(defaultType.getRawClass());
        }
        try {
            return b.buildTypeDeserializer(config, baseType, subtypes);
        } catch (IllegalArgumentException e0) {
            InvalidDefinitionException e = InvalidDefinitionException.from((JsonParser)null, ClassUtil.exceptionMessage(e0), baseType);
            e.initCause(e0);
            throw e;
        }
    }

    protected JsonDeserializer<?> findOptionalStdDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        return OptionalHandlerFactory.instance.findDeserializer(type, ctxt.getConfig(), beanDesc);
    }

    @Override
    public KeyDeserializer createKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        BeanDescription beanDesc = null;
        KeyDeserializer deser = null;
        if (this._factoryConfig.hasKeyDeserializers()) {
            KeyDeserializers d;
            beanDesc = config.introspectClassAnnotations(type);
            Iterator<Object> iterator = this._factoryConfig.keyDeserializers().iterator();
            while (iterator.hasNext() && (deser = (d = iterator.next()).findKeyDeserializer(type, config, beanDesc)) == null) {
            }
        }
        if (deser == null) {
            if (beanDesc == null) {
                beanDesc = config.introspectClassAnnotations(type.getRawClass());
            }
            if ((deser = this.findKeyDeserializerFromAnnotation(ctxt, beanDesc.getClassInfo())) == null) {
                deser = type.isEnumType() ? this._createEnumKeyDeserializer(ctxt, type) : StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
            }
        }
        if (deser != null && this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : this._factoryConfig.deserializerModifiers()) {
                deser = mod.modifyKeyDeserializer(config, type, deser);
            }
        }
        return deser;
    }

    private KeyDeserializer _createEnumKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        Class<?> enumClass = type.getRawClass();
        Object beanDesc = config.introspect(type);
        KeyDeserializer des = this.findKeyDeserializerFromAnnotation(ctxt, ((BeanDescription)beanDesc).getClassInfo());
        if (des != null) {
            return des;
        }
        JsonDeserializer<?> custom = this._findCustomEnumDeserializer(enumClass, config, (BeanDescription)beanDesc);
        if (custom != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, custom);
        }
        JsonDeserializer<Object> valueDesForKey = this.findDeserializerFromAnnotation(ctxt, ((BeanDescription)beanDesc).getClassInfo());
        if (valueDesForKey != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, valueDesForKey);
        }
        EnumResolver enumRes = this.constructEnumResolver(enumClass, config, ((BeanDescription)beanDesc).findJsonValueAccessor());
        for (AnnotatedMethod factory : ((BeanDescription)beanDesc).getFactoryMethods()) {
            Class<?> returnType;
            if (!this._hasCreatorAnnotation(ctxt, factory)) continue;
            int argCount = factory.getParameterCount();
            if (argCount == 1 && (returnType = factory.getRawReturnType()).isAssignableFrom(enumClass)) {
                if (factory.getRawParameterType(0) != String.class) continue;
                if (config.canOverrideAccessModifiers()) {
                    ClassUtil.checkAndFixAccess(factory.getMember(), ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes, factory);
            }
            throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
        }
        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes);
    }

    @Override
    public boolean hasExplicitDeserializerFor(DeserializationConfig config, Class<?> valueType) {
        while (valueType.isArray()) {
            valueType = valueType.getComponentType();
        }
        if (Enum.class.isAssignableFrom(valueType)) {
            return true;
        }
        String clsName = valueType.getName();
        if (clsName.startsWith("java.")) {
            if (Collection.class.isAssignableFrom(valueType)) {
                return true;
            }
            if (Map.class.isAssignableFrom(valueType)) {
                return true;
            }
            if (Number.class.isAssignableFrom(valueType)) {
                return NumberDeserializers.find(valueType, clsName) != null;
            }
            if (JdkDeserializers.hasDeserializerFor(valueType) || valueType == CLASS_STRING || valueType == Boolean.class || valueType == EnumMap.class || valueType == AtomicReference.class) {
                return true;
            }
            return DateDeserializers.hasDeserializerFor(valueType);
        }
        if (clsName.startsWith("com.fasterxml.")) {
            return JsonNode.class.isAssignableFrom(valueType) || valueType == TokenBuffer.class;
        }
        return OptionalHandlerFactory.instance.hasDeserializerFor(valueType);
    }

    public TypeDeserializer findPropertyTypeDeserializer(DeserializationConfig config, JavaType baseType, AnnotatedMember annotated) throws JsonMappingException {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, annotated, baseType);
        if (b == null) {
            return this.findTypeDeserializer(config, baseType);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, annotated, baseType);
        try {
            return b.buildTypeDeserializer(config, baseType, subtypes);
        } catch (IllegalArgumentException e0) {
            InvalidDefinitionException e = InvalidDefinitionException.from((JsonParser)null, ClassUtil.exceptionMessage(e0), baseType);
            e.initCause(e0);
            throw e;
        }
    }

    public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig config, JavaType containerType, AnnotatedMember propertyEntity) throws JsonMappingException {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);
        JavaType contentType = containerType.getContentType();
        if (b == null) {
            return this.findTypeDeserializer(config, contentType);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, propertyEntity, contentType);
        return b.buildTypeDeserializer(config, contentType, subtypes);
    }

    public JsonDeserializer<?> findDefaultDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser;
        Class<?> rawType = type.getRawClass();
        if (rawType == CLASS_OBJECT || rawType == CLASS_SERIALIZABLE) {
            JavaType mt;
            JavaType lt;
            DeserializationConfig config = ctxt.getConfig();
            if (this._factoryConfig.hasAbstractTypeResolvers()) {
                lt = this._findRemappedType(config, List.class);
                mt = this._findRemappedType(config, Map.class);
            } else {
                mt = null;
                lt = null;
            }
            return new UntypedObjectDeserializer(lt, mt);
        }
        if (rawType == CLASS_STRING || rawType == CLASS_CHAR_SEQUENCE) {
            return StringDeserializer.instance;
        }
        if (rawType == CLASS_ITERABLE) {
            TypeFactory tf = ctxt.getTypeFactory();
            JavaType[] tps = tf.findTypeParameters(type, CLASS_ITERABLE);
            JavaType elemType = tps == null || tps.length != 1 ? TypeFactory.unknownType() : tps[0];
            CollectionType ct = tf.constructCollectionType(Collection.class, elemType);
            return this.createCollectionDeserializer(ctxt, ct, beanDesc);
        }
        if (rawType == CLASS_MAP_ENTRY) {
            JavaType kt = type.containedTypeOrUnknown(0);
            JavaType vt = type.containedTypeOrUnknown(1);
            TypeDeserializer vts = (TypeDeserializer)vt.getTypeHandler();
            if (vts == null) {
                vts = this.findTypeDeserializer(ctxt.getConfig(), vt);
            }
            JsonDeserializer valueDeser = (JsonDeserializer)vt.getValueHandler();
            KeyDeserializer keyDes = (KeyDeserializer)kt.getValueHandler();
            return new MapEntryDeserializer(type, keyDes, (JsonDeserializer<Object>)valueDeser, vts);
        }
        String clsName = rawType.getName();
        if (rawType.isPrimitive() || clsName.startsWith("java.")) {
            deser = NumberDeserializers.find(rawType, clsName);
            if (deser == null) {
                deser = DateDeserializers.find(rawType, clsName);
            }
            if (deser != null) {
                return deser;
            }
        }
        if (rawType == TokenBuffer.class) {
            return new TokenBufferDeserializer();
        }
        deser = this.findOptionalStdDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            return deser;
        }
        return JdkDeserializers.find(rawType, clsName);
    }

    protected JavaType _findRemappedType(DeserializationConfig config, Class<?> rawType) throws JsonMappingException {
        JavaType type = this.mapAbstractType(config, config.constructType(rawType));
        return type == null || type.hasRawClass(rawType) ? null : type;
    }

    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findTreeNodeDeserializer(type, config, beanDesc);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomReferenceDeserializer(ReferenceType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findReferenceDeserializer(type, config, beanDesc, contentTypeDeserializer, contentDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<Object> _findCustomBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<Object> deser = d.findBeanDeserializer(type, config, beanDesc);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findArrayDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionLikeDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findEnumDeserializer(type, config, beanDesc);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomMapDeserializer(MapType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapLikeDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser == null) continue;
            return deser;
        }
        return null;
    }

    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr != null && (deserDef = intr.findDeserializer(ann)) != null) {
            return ctxt.deserializerInstance(ann, deserDef);
        }
        return null;
    }

    protected KeyDeserializer findKeyDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr != null && (deserDef = intr.findKeyDeserializer(ann)) != null) {
            return ctxt.keyDeserializerInstance(ann, deserDef);
        }
        return null;
    }

    protected JsonDeserializer<Object> findContentDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr != null && (deserDef = intr.findContentDeserializer(ann)) != null) {
            return ctxt.deserializerInstance(ann, deserDef);
        }
        return null;
    }

    protected JavaType resolveMemberAndTypeAnnotations(DeserializationContext ctxt, AnnotatedMember member, JavaType type) throws JsonMappingException {
        TypeDeserializer valueTypeDeser;
        Object kdDef;
        KeyDeserializer kd;
        JavaType keyType;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            return type;
        }
        if (type.isMapLikeType() && (keyType = type.getKeyType()) != null && (kd = ctxt.keyDeserializerInstance(member, kdDef = intr.findKeyDeserializer(member))) != null) {
            type = ((MapLikeType)type).withKeyValueHandler(kd);
            keyType = type.getKeyType();
        }
        if (type.hasContentType()) {
            TypeDeserializer contentTypeDeser;
            Object cdDef = intr.findContentDeserializer(member);
            JsonDeserializer<Object> cd = ctxt.deserializerInstance(member, cdDef);
            if (cd != null) {
                type = type.withContentValueHandler(cd);
            }
            if ((contentTypeDeser = this.findPropertyContentTypeDeserializer(ctxt.getConfig(), type, member)) != null) {
                type = type.withContentTypeHandler(contentTypeDeser);
            }
        }
        if ((valueTypeDeser = this.findPropertyTypeDeserializer(ctxt.getConfig(), type, member)) != null) {
            type = type.withTypeHandler(valueTypeDeser);
        }
        type = intr.refineDeserializationType(ctxt.getConfig(), member, type);
        return type;
    }

    protected EnumResolver constructEnumResolver(Class<?> enumClass, DeserializationConfig config, AnnotatedMember jsonValueAccessor) {
        if (jsonValueAccessor != null) {
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(jsonValueAccessor.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            return EnumResolver.constructUnsafeUsingMethod(enumClass, jsonValueAccessor, config.getAnnotationIntrospector());
        }
        return EnumResolver.constructUnsafe(enumClass, config.getAnnotationIntrospector());
    }

    protected boolean _hasCreatorAnnotation(DeserializationContext ctxt, Annotated ann) {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr != null) {
            JsonCreator.Mode mode = intr.findCreatorAnnotation(ctxt.getConfig(), ann);
            return mode != null && mode != JsonCreator.Mode.DISABLED;
        }
        return false;
    }

    @Deprecated
    protected JavaType modifyTypeByAnnotation(DeserializationContext ctxt, Annotated a, JavaType type) throws JsonMappingException {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            return type;
        }
        return intr.refineDeserializationType(ctxt.getConfig(), a, type);
    }

    @Deprecated
    protected JavaType resolveType(DeserializationContext ctxt, BeanDescription beanDesc, JavaType type, AnnotatedMember member) throws JsonMappingException {
        return this.resolveMemberAndTypeAnnotations(ctxt, member, type);
    }

    @Deprecated
    protected AnnotatedMethod _findJsonValueFor(DeserializationConfig config, JavaType enumType) {
        if (enumType == null) {
            return null;
        }
        Object beanDesc = config.introspect(enumType);
        return ((BeanDescription)beanDesc).findJsonValueMethod();
    }

    protected static class ContainerDefaultMappings {
        static final HashMap<String, Class<? extends Collection>> _collectionFallbacks;
        static final HashMap<String, Class<? extends Map>> _mapFallbacks;

        protected ContainerDefaultMappings() {
        }

        public static Class<?> findCollectionFallback(JavaType type) {
            return _collectionFallbacks.get(type.getRawClass().getName());
        }

        public static Class<?> findMapFallback(JavaType type) {
            return _mapFallbacks.get(type.getRawClass().getName());
        }

        static {
            HashMap<String, Class> fallbacks = new HashMap<String, Class>();
            Class<ArrayList> DEFAULT_LIST = ArrayList.class;
            Class<HashSet> DEFAULT_SET = HashSet.class;
            fallbacks.put(Collection.class.getName(), DEFAULT_LIST);
            fallbacks.put(List.class.getName(), DEFAULT_LIST);
            fallbacks.put(Set.class.getName(), DEFAULT_SET);
            fallbacks.put(SortedSet.class.getName(), TreeSet.class);
            fallbacks.put(Queue.class.getName(), LinkedList.class);
            fallbacks.put(AbstractList.class.getName(), DEFAULT_LIST);
            fallbacks.put(AbstractSet.class.getName(), DEFAULT_SET);
            fallbacks.put(Deque.class.getName(), LinkedList.class);
            fallbacks.put(NavigableSet.class.getName(), TreeSet.class);
            _collectionFallbacks = fallbacks;
            fallbacks = new HashMap();
            Class<LinkedHashMap> DEFAULT_MAP = LinkedHashMap.class;
            fallbacks.put(Map.class.getName(), DEFAULT_MAP);
            fallbacks.put(AbstractMap.class.getName(), DEFAULT_MAP);
            fallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
            fallbacks.put(SortedMap.class.getName(), TreeMap.class);
            fallbacks.put(NavigableMap.class.getName(), TreeMap.class);
            fallbacks.put(ConcurrentNavigableMap.class.getName(), ConcurrentSkipListMap.class);
            _mapFallbacks = fallbacks;
        }
    }
}

