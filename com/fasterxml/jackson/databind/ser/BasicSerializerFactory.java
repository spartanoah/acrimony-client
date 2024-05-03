/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ext.OptionalHandlerFactory;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IteratorSerializer;
import com.fasterxml.jackson.databind.ser.impl.MapEntrySerializer;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.AtomicReferenceSerializer;
import com.fasterxml.jackson.databind.ser.std.BooleanSerializer;
import com.fasterxml.jackson.databind.ser.std.ByteBufferSerializer;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import com.fasterxml.jackson.databind.ser.std.EnumSetSerializer;
import com.fasterxml.jackson.databind.ser.std.InetAddressSerializer;
import com.fasterxml.jackson.databind.ser.std.InetSocketAddressSerializer;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.databind.ser.std.ObjectArraySerializer;
import com.fasterxml.jackson.databind.ser.std.SerializableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdArraySerializers;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.ser.std.TimeZoneSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.TokenBufferSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BasicSerializerFactory
extends SerializerFactory
implements Serializable {
    protected static final HashMap<String, JsonSerializer<?>> _concrete;
    protected static final HashMap<String, Class<? extends JsonSerializer<?>>> _concreteLazy;
    protected final SerializerFactoryConfig _factoryConfig;

    protected BasicSerializerFactory(SerializerFactoryConfig config) {
        this._factoryConfig = config == null ? new SerializerFactoryConfig() : config;
    }

    public SerializerFactoryConfig getFactoryConfig() {
        return this._factoryConfig;
    }

    public abstract SerializerFactory withConfig(SerializerFactoryConfig var1);

    @Override
    public final SerializerFactory withAdditionalSerializers(Serializers additional) {
        return this.withConfig(this._factoryConfig.withAdditionalSerializers(additional));
    }

    @Override
    public final SerializerFactory withAdditionalKeySerializers(Serializers additional) {
        return this.withConfig(this._factoryConfig.withAdditionalKeySerializers(additional));
    }

    @Override
    public final SerializerFactory withSerializerModifier(BeanSerializerModifier modifier) {
        return this.withConfig(this._factoryConfig.withSerializerModifier(modifier));
    }

    @Override
    public abstract JsonSerializer<Object> createSerializer(SerializerProvider var1, JavaType var2) throws JsonMappingException;

    @Override
    public JsonSerializer<Object> createKeySerializer(SerializerProvider ctxt, JavaType keyType, JsonSerializer<Object> defaultImpl) throws JsonMappingException {
        SerializationConfig config = ctxt.getConfig();
        Object beanDesc = config.introspect(keyType);
        JsonValueSerializer ser = null;
        if (this._factoryConfig.hasKeySerializers()) {
            Serializers serializers;
            Iterator<Object> iterator = this._factoryConfig.keySerializers().iterator();
            while (iterator.hasNext() && (ser = (serializers = iterator.next()).findSerializer(config, keyType, (BeanDescription)beanDesc)) == null) {
            }
        }
        if (ser == null && (ser = this._findKeySerializer(ctxt, ((BeanDescription)beanDesc).getClassInfo())) == null && (ser = defaultImpl) == null && (ser = StdKeySerializers.getStdKeySerializer(config, keyType.getRawClass(), false)) == null) {
            AnnotatedMember am = ((BeanDescription)beanDesc).findJsonValueAccessor();
            if (am != null) {
                Class<?> rawType = am.getRawType();
                JsonSerializer<Object> delegate = StdKeySerializers.getStdKeySerializer(config, rawType, true);
                if (config.canOverrideAccessModifiers()) {
                    ClassUtil.checkAndFixAccess(am.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                ser = new JsonValueSerializer(am, delegate);
            } else {
                ser = StdKeySerializers.getFallbackKeySerializer(config, keyType.getRawClass());
            }
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyKeySerializer(config, keyType, (BeanDescription)beanDesc, ser);
            }
        }
        return ser;
    }

    @Override
    @Deprecated
    public JsonSerializer<Object> createKeySerializer(SerializationConfig config, JavaType keyType, JsonSerializer<Object> defaultImpl) {
        Object beanDesc = config.introspect(keyType);
        JsonValueSerializer ser = null;
        if (this._factoryConfig.hasKeySerializers()) {
            Serializers serializers;
            Iterator<Object> iterator = this._factoryConfig.keySerializers().iterator();
            while (iterator.hasNext() && (ser = (serializers = iterator.next()).findSerializer(config, keyType, (BeanDescription)beanDesc)) == null) {
            }
        }
        if (ser == null && (ser = defaultImpl) == null && (ser = StdKeySerializers.getStdKeySerializer(config, keyType.getRawClass(), false)) == null) {
            AnnotatedMember am = ((BeanDescription)beanDesc).findJsonValueAccessor();
            if (am != null) {
                Class<?> rawType = am.getRawType();
                JsonSerializer<Object> delegate = StdKeySerializers.getStdKeySerializer(config, rawType, true);
                if (config.canOverrideAccessModifiers()) {
                    ClassUtil.checkAndFixAccess(am.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                }
                ser = new JsonValueSerializer(am, delegate);
            } else {
                ser = StdKeySerializers.getFallbackKeySerializer(config, keyType.getRawClass());
            }
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyKeySerializer(config, keyType, (BeanDescription)beanDesc, ser);
            }
        }
        return ser;
    }

    @Override
    public TypeSerializer createTypeSerializer(SerializationConfig config, JavaType baseType) {
        BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, ac);
        }
        if (b == null) {
            return null;
        }
        return b.buildTypeSerializer(config, baseType, subtypes);
    }

    protected abstract Iterable<Serializers> customSerializers();

    protected final JsonSerializer<?> findSerializerByLookup(JavaType type, SerializationConfig config, BeanDescription beanDesc, boolean staticTyping) {
        Class<? extends JsonSerializer<?>> serClass;
        Class<?> raw = type.getRawClass();
        String clsName = raw.getName();
        JsonSerializer<?> ser = _concrete.get(clsName);
        if (ser == null && (serClass = _concreteLazy.get(clsName)) != null) {
            return ClassUtil.createInstance(serClass, false);
        }
        return ser;
    }

    protected final JsonSerializer<?> findSerializerByAnnotations(SerializerProvider prov, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        Class<?> raw = type.getRawClass();
        if (JsonSerializable.class.isAssignableFrom(raw)) {
            return SerializableSerializer.instance;
        }
        AnnotatedMember valueAccessor = beanDesc.findJsonValueAccessor();
        if (valueAccessor != null) {
            if (prov.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(valueAccessor.getMember(), prov.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            JsonSerializer<Object> ser = this.findSerializerFromAnnotation(prov, valueAccessor);
            return new JsonValueSerializer(valueAccessor, ser);
        }
        return null;
    }

    protected final JsonSerializer<?> findSerializerByPrimaryType(SerializerProvider prov, JavaType type, BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
        if (type.isEnumType()) {
            return this.buildEnumSerializer(prov.getConfig(), type, beanDesc);
        }
        Class<?> raw = type.getRawClass();
        JsonSerializer<?> ser = this.findOptionalStdSerializer(prov, type, beanDesc, staticTyping);
        if (ser != null) {
            return ser;
        }
        if (Calendar.class.isAssignableFrom(raw)) {
            return CalendarSerializer.instance;
        }
        if (Date.class.isAssignableFrom(raw)) {
            return DateSerializer.instance;
        }
        if (Map.Entry.class.isAssignableFrom(raw)) {
            JavaType mapEntryType = type.findSuperType(Map.Entry.class);
            JavaType kt = mapEntryType.containedTypeOrUnknown(0);
            JavaType vt = mapEntryType.containedTypeOrUnknown(1);
            return this.buildMapEntrySerializer(prov, type, beanDesc, staticTyping, kt, vt);
        }
        if (ByteBuffer.class.isAssignableFrom(raw)) {
            return new ByteBufferSerializer();
        }
        if (InetAddress.class.isAssignableFrom(raw)) {
            return new InetAddressSerializer();
        }
        if (InetSocketAddress.class.isAssignableFrom(raw)) {
            return new InetSocketAddressSerializer();
        }
        if (TimeZone.class.isAssignableFrom(raw)) {
            return new TimeZoneSerializer();
        }
        if (Charset.class.isAssignableFrom(raw)) {
            return ToStringSerializer.instance;
        }
        if (Number.class.isAssignableFrom(raw)) {
            JsonFormat.Value format = beanDesc.findExpectedFormat(null);
            if (format != null) {
                switch (format.getShape()) {
                    case STRING: {
                        return ToStringSerializer.instance;
                    }
                    case OBJECT: 
                    case ARRAY: {
                        return null;
                    }
                }
            }
            return NumberSerializer.instance;
        }
        return null;
    }

    protected JsonSerializer<?> findOptionalStdSerializer(SerializerProvider prov, JavaType type, BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
        return OptionalHandlerFactory.instance.findSerializer(prov.getConfig(), type, beanDesc);
    }

    protected final JsonSerializer<?> findSerializerByAddonType(SerializationConfig config, JavaType javaType, BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
        Class<?> rawType = javaType.getRawClass();
        if (Iterator.class.isAssignableFrom(rawType)) {
            JavaType[] params = config.getTypeFactory().findTypeParameters(javaType, Iterator.class);
            JavaType vt = params == null || params.length != 1 ? TypeFactory.unknownType() : params[0];
            return this.buildIteratorSerializer(config, javaType, beanDesc, staticTyping, vt);
        }
        if (Iterable.class.isAssignableFrom(rawType)) {
            JavaType[] params = config.getTypeFactory().findTypeParameters(javaType, Iterable.class);
            JavaType vt = params == null || params.length != 1 ? TypeFactory.unknownType() : params[0];
            return this.buildIterableSerializer(config, javaType, beanDesc, staticTyping, vt);
        }
        if (CharSequence.class.isAssignableFrom(rawType)) {
            return ToStringSerializer.instance;
        }
        return null;
    }

    protected JsonSerializer<Object> findSerializerFromAnnotation(SerializerProvider prov, Annotated a) throws JsonMappingException {
        Object serDef = prov.getAnnotationIntrospector().findSerializer(a);
        if (serDef == null) {
            return null;
        }
        JsonSerializer<Object> ser = prov.serializerInstance(a, serDef);
        return this.findConvertingSerializer(prov, a, ser);
    }

    protected JsonSerializer<?> findConvertingSerializer(SerializerProvider prov, Annotated a, JsonSerializer<?> ser) throws JsonMappingException {
        Converter<Object, Object> conv = this.findConverter(prov, a);
        if (conv == null) {
            return ser;
        }
        JavaType delegateType = conv.getOutputType(prov.getTypeFactory());
        return new StdDelegatingSerializer(conv, delegateType, ser);
    }

    protected Converter<Object, Object> findConverter(SerializerProvider prov, Annotated a) throws JsonMappingException {
        Object convDef = prov.getAnnotationIntrospector().findSerializationConverter(a);
        if (convDef == null) {
            return null;
        }
        return prov.converterInstance(a, convDef);
    }

    protected JsonSerializer<?> buildContainerSerializer(SerializerProvider prov, JavaType type, BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
        JavaType elementType;
        TypeSerializer elementTypeSerializer;
        SerializationConfig config = prov.getConfig();
        if (!(staticTyping || !type.useStaticType() || type.isContainerType() && type.getContentType().isJavaLangObject())) {
            staticTyping = true;
        }
        if ((elementTypeSerializer = this.createTypeSerializer(config, elementType = type.getContentType())) != null) {
            staticTyping = false;
        }
        JsonSerializer<Object> elementValueSerializer = this._findContentSerializer(prov, beanDesc.getClassInfo());
        if (type.isMapLikeType()) {
            Serializers serializers;
            MapLikeType mlt = (MapLikeType)type;
            JsonSerializer<Object> keySerializer = this._findKeySerializer(prov, beanDesc.getClassInfo());
            if (mlt.isTrueMapType()) {
                return this.buildMapSerializer(prov, (MapType)mlt, beanDesc, staticTyping, keySerializer, elementTypeSerializer, elementValueSerializer);
            }
            JsonSerializer<?> ser = null;
            MapLikeType mlType = (MapLikeType)type;
            Iterator<Object> iterator = this.customSerializers().iterator();
            while (iterator.hasNext() && (ser = (serializers = iterator.next()).findMapLikeSerializer(config, mlType, beanDesc, keySerializer, elementTypeSerializer, elementValueSerializer)) == null) {
            }
            if (ser == null) {
                ser = this.findSerializerByAnnotations(prov, type, beanDesc);
            }
            if (ser != null && this._factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                    ser = mod.modifyMapLikeSerializer(config, mlType, beanDesc, ser);
                }
            }
            return ser;
        }
        if (type.isCollectionLikeType()) {
            Serializers serializers;
            CollectionLikeType clt = (CollectionLikeType)type;
            if (clt.isTrueCollectionType()) {
                return this.buildCollectionSerializer(prov, (CollectionType)clt, beanDesc, staticTyping, elementTypeSerializer, elementValueSerializer);
            }
            JsonSerializer<?> ser = null;
            CollectionLikeType clType = (CollectionLikeType)type;
            Iterator<Object> iterator = this.customSerializers().iterator();
            while (iterator.hasNext() && (ser = (serializers = iterator.next()).findCollectionLikeSerializer(config, clType, beanDesc, elementTypeSerializer, elementValueSerializer)) == null) {
            }
            if (ser == null) {
                ser = this.findSerializerByAnnotations(prov, type, beanDesc);
            }
            if (ser != null && this._factoryConfig.hasSerializerModifiers()) {
                for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                    ser = mod.modifyCollectionLikeSerializer(config, clType, beanDesc, ser);
                }
            }
            return ser;
        }
        if (type.isArrayType()) {
            return this.buildArraySerializer(prov, (ArrayType)type, beanDesc, staticTyping, elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }

    protected JsonSerializer<?> buildCollectionSerializer(SerializerProvider prov, CollectionType type, BeanDescription beanDesc, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) throws JsonMappingException {
        Serializers serializers;
        SerializationConfig config = prov.getConfig();
        StdSerializer ser = null;
        Iterator<Object> iterator = this.customSerializers().iterator();
        while (iterator.hasNext() && (ser = (serializers = iterator.next()).findCollectionSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer)) == null) {
        }
        if (ser == null && (ser = this.findSerializerByAnnotations(prov, type, beanDesc)) == null) {
            JsonFormat.Value format = beanDesc.findExpectedFormat(null);
            if (format != null && format.getShape() == JsonFormat.Shape.OBJECT) {
                return null;
            }
            Class<?> raw = type.getRawClass();
            if (EnumSet.class.isAssignableFrom(raw)) {
                JavaType enumType = type.getContentType();
                if (!enumType.isEnumImplType()) {
                    enumType = null;
                }
                ser = this.buildEnumSetSerializer(enumType);
            } else {
                Class<?> elementRaw = type.getContentType().getRawClass();
                if (this.isIndexedList(raw)) {
                    if (elementRaw == String.class) {
                        if (ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                            ser = IndexedStringListSerializer.instance;
                        }
                    } else {
                        ser = this.buildIndexedListSerializer(type.getContentType(), staticTyping, elementTypeSerializer, elementValueSerializer);
                    }
                } else if (elementRaw == String.class && ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                    ser = StringCollectionSerializer.instance;
                }
                if (ser == null) {
                    ser = this.buildCollectionSerializer(type.getContentType(), staticTyping, elementTypeSerializer, elementValueSerializer);
                }
            }
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyCollectionSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    protected boolean isIndexedList(Class<?> cls) {
        return RandomAccess.class.isAssignableFrom(cls);
    }

    public ContainerSerializer<?> buildIndexedListSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> valueSerializer) {
        return new IndexedListSerializer(elemType, staticTyping, vts, valueSerializer);
    }

    public ContainerSerializer<?> buildCollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts, JsonSerializer<Object> valueSerializer) {
        return new CollectionSerializer(elemType, staticTyping, vts, valueSerializer);
    }

    public JsonSerializer<?> buildEnumSetSerializer(JavaType enumType) {
        return new EnumSetSerializer(enumType);
    }

    protected JsonSerializer<?> buildMapSerializer(SerializerProvider prov, MapType type, BeanDescription beanDesc, boolean staticTyping, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) throws JsonMappingException {
        Serializers serializers;
        JsonFormat.Value format = beanDesc.findExpectedFormat(null);
        if (format != null && format.getShape() == JsonFormat.Shape.OBJECT) {
            return null;
        }
        MapSerializer ser = null;
        SerializationConfig config = prov.getConfig();
        Iterator<Object> iterator = this.customSerializers().iterator();
        while (iterator.hasNext() && (ser = (serializers = iterator.next()).findMapSerializer(config, type, beanDesc, keySerializer, elementTypeSerializer, elementValueSerializer)) == null) {
        }
        if (ser == null && (ser = this.findSerializerByAnnotations(prov, type, beanDesc)) == null) {
            Object filterId = this.findFilterId(config, beanDesc);
            JsonIgnoreProperties.Value ignorals = config.getDefaultPropertyIgnorals(Map.class, beanDesc.getClassInfo());
            Set<String> ignored = ignorals == null ? null : ignorals.findIgnoredForSerialization();
            MapSerializer mapSer = MapSerializer.construct(ignored, (JavaType)type, staticTyping, elementTypeSerializer, keySerializer, elementValueSerializer, filterId);
            ser = this._checkMapContentInclusion(prov, beanDesc, mapSer);
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyMapSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    protected MapSerializer _checkMapContentInclusion(SerializerProvider prov, BeanDescription beanDesc, MapSerializer mapSer) throws JsonMappingException {
        Object valueToSuppress;
        JsonInclude.Include incl;
        JavaType contentType = mapSer.getContentType();
        JsonInclude.Value inclV = this._findInclusionWithContent(prov, beanDesc, contentType, Map.class);
        JsonInclude.Include include = incl = inclV == null ? JsonInclude.Include.USE_DEFAULTS : inclV.getContentInclusion();
        if (incl == JsonInclude.Include.USE_DEFAULTS || incl == JsonInclude.Include.ALWAYS) {
            if (!prov.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES)) {
                return mapSer.withContentInclusion(null, true);
            }
            return mapSer;
        }
        boolean suppressNulls = true;
        switch (incl) {
            case NON_DEFAULT: {
                valueToSuppress = BeanUtil.getDefaultValue(contentType);
                if (valueToSuppress == null || !valueToSuppress.getClass().isArray()) break;
                valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                break;
            }
            case NON_ABSENT: {
                valueToSuppress = contentType.isReferenceType() ? MapSerializer.MARKER_FOR_EMPTY : null;
                break;
            }
            case NON_EMPTY: {
                valueToSuppress = MapSerializer.MARKER_FOR_EMPTY;
                break;
            }
            case CUSTOM: {
                valueToSuppress = prov.includeFilterInstance(null, inclV.getContentFilter());
                if (valueToSuppress == null) {
                    suppressNulls = true;
                    break;
                }
                suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
                break;
            }
            default: {
                valueToSuppress = null;
            }
        }
        return mapSer.withContentInclusion(valueToSuppress, suppressNulls);
    }

    protected JsonSerializer<?> buildMapEntrySerializer(SerializerProvider prov, JavaType type, BeanDescription beanDesc, boolean staticTyping, JavaType keyType, JavaType valueType) throws JsonMappingException {
        Object valueToSuppress;
        JsonInclude.Include incl;
        JsonFormat.Value formatOverride = prov.getDefaultPropertyFormat(Map.Entry.class);
        JsonFormat.Value formatFromAnnotation = beanDesc.findExpectedFormat(null);
        JsonFormat.Value format = JsonFormat.Value.merge(formatFromAnnotation, formatOverride);
        if (format.getShape() == JsonFormat.Shape.OBJECT) {
            return null;
        }
        MapEntrySerializer ser = new MapEntrySerializer(valueType, keyType, valueType, staticTyping, this.createTypeSerializer(prov.getConfig(), valueType), null);
        JavaType contentType = ser.getContentType();
        JsonInclude.Value inclV = this._findInclusionWithContent(prov, beanDesc, contentType, Map.Entry.class);
        JsonInclude.Include include = incl = inclV == null ? JsonInclude.Include.USE_DEFAULTS : inclV.getContentInclusion();
        if (incl == JsonInclude.Include.USE_DEFAULTS || incl == JsonInclude.Include.ALWAYS) {
            return ser;
        }
        boolean suppressNulls = true;
        switch (incl) {
            case NON_DEFAULT: {
                valueToSuppress = BeanUtil.getDefaultValue(contentType);
                if (valueToSuppress == null || !valueToSuppress.getClass().isArray()) break;
                valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                break;
            }
            case NON_ABSENT: {
                valueToSuppress = contentType.isReferenceType() ? MapSerializer.MARKER_FOR_EMPTY : null;
                break;
            }
            case NON_EMPTY: {
                valueToSuppress = MapSerializer.MARKER_FOR_EMPTY;
                break;
            }
            case CUSTOM: {
                valueToSuppress = prov.includeFilterInstance(null, inclV.getContentFilter());
                if (valueToSuppress == null) {
                    suppressNulls = true;
                    break;
                }
                suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
                break;
            }
            default: {
                valueToSuppress = null;
            }
        }
        return ser.withContentInclusion(valueToSuppress, suppressNulls);
    }

    protected JsonInclude.Value _findInclusionWithContent(SerializerProvider prov, BeanDescription beanDesc, JavaType contentType, Class<?> configType) throws JsonMappingException {
        SerializationConfig config = prov.getConfig();
        JsonInclude.Value inclV = beanDesc.findPropertyInclusion(config.getDefaultPropertyInclusion());
        inclV = config.getDefaultPropertyInclusion(configType, inclV);
        JsonInclude.Value valueIncl = config.getDefaultPropertyInclusion(contentType.getRawClass(), null);
        if (valueIncl != null) {
            switch (valueIncl.getValueInclusion()) {
                case USE_DEFAULTS: {
                    break;
                }
                case CUSTOM: {
                    inclV = inclV.withContentFilter(valueIncl.getContentFilter());
                    break;
                }
                default: {
                    inclV = inclV.withContentInclusion(valueIncl.getValueInclusion());
                }
            }
        }
        return inclV;
    }

    protected JsonSerializer<?> buildArraySerializer(SerializerProvider prov, ArrayType type, BeanDescription beanDesc, boolean staticTyping, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) throws JsonMappingException {
        Serializers serializers;
        SerializationConfig config = prov.getConfig();
        JsonSerializer ser = null;
        Iterator<Object> iterator = this.customSerializers().iterator();
        while (iterator.hasNext() && (ser = (serializers = iterator.next()).findArraySerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer)) == null) {
        }
        if (ser == null) {
            Class<?> raw = type.getRawClass();
            if (elementValueSerializer == null || ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                ser = String[].class == raw ? StringArraySerializer.instance : StdArraySerializers.findStandardImpl(raw);
            }
            if (ser == null) {
                ser = new ObjectArraySerializer(type.getContentType(), staticTyping, elementTypeSerializer, elementValueSerializer);
            }
        }
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyArraySerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    public JsonSerializer<?> findReferenceSerializer(SerializerProvider prov, ReferenceType refType, BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
        JavaType contentType = refType.getContentType();
        TypeSerializer contentTypeSerializer = (TypeSerializer)contentType.getTypeHandler();
        SerializationConfig config = prov.getConfig();
        if (contentTypeSerializer == null) {
            contentTypeSerializer = this.createTypeSerializer(config, contentType);
        }
        JsonSerializer contentSerializer = (JsonSerializer)contentType.getValueHandler();
        for (Serializers serializers : this.customSerializers()) {
            JsonSerializer<?> ser = serializers.findReferenceSerializer(config, refType, beanDesc, contentTypeSerializer, contentSerializer);
            if (ser == null) continue;
            return ser;
        }
        if (refType.isTypeOrSubTypeOf(AtomicReference.class)) {
            return this.buildAtomicReferenceSerializer(prov, refType, beanDesc, staticTyping, contentTypeSerializer, contentSerializer);
        }
        return null;
    }

    protected JsonSerializer<?> buildAtomicReferenceSerializer(SerializerProvider prov, ReferenceType refType, BeanDescription beanDesc, boolean staticTyping, TypeSerializer contentTypeSerializer, JsonSerializer<Object> contentSerializer) throws JsonMappingException {
        boolean suppressNulls;
        Object valueToSuppress;
        JsonInclude.Include incl;
        JavaType contentType = refType.getReferencedType();
        JsonInclude.Value inclV = this._findInclusionWithContent(prov, beanDesc, contentType, AtomicReference.class);
        JsonInclude.Include include = incl = inclV == null ? JsonInclude.Include.USE_DEFAULTS : inclV.getContentInclusion();
        if (incl == JsonInclude.Include.USE_DEFAULTS || incl == JsonInclude.Include.ALWAYS) {
            valueToSuppress = null;
            suppressNulls = false;
        } else {
            suppressNulls = true;
            switch (incl) {
                case NON_DEFAULT: {
                    valueToSuppress = BeanUtil.getDefaultValue(contentType);
                    if (valueToSuppress == null || !valueToSuppress.getClass().isArray()) break;
                    valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                    break;
                }
                case NON_ABSENT: {
                    valueToSuppress = contentType.isReferenceType() ? MapSerializer.MARKER_FOR_EMPTY : null;
                    break;
                }
                case NON_EMPTY: {
                    valueToSuppress = MapSerializer.MARKER_FOR_EMPTY;
                    break;
                }
                case CUSTOM: {
                    valueToSuppress = prov.includeFilterInstance(null, inclV.getContentFilter());
                    if (valueToSuppress == null) {
                        suppressNulls = true;
                        break;
                    }
                    suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
                    break;
                }
                default: {
                    valueToSuppress = null;
                }
            }
        }
        AtomicReferenceSerializer ser = new AtomicReferenceSerializer(refType, staticTyping, contentTypeSerializer, contentSerializer);
        return ser.withContentInclusion(valueToSuppress, suppressNulls);
    }

    protected JsonSerializer<?> buildIteratorSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc, boolean staticTyping, JavaType valueType) throws JsonMappingException {
        return new IteratorSerializer(valueType, staticTyping, this.createTypeSerializer(config, valueType));
    }

    protected JsonSerializer<?> buildIterableSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc, boolean staticTyping, JavaType valueType) throws JsonMappingException {
        return new IterableSerializer(valueType, staticTyping, this.createTypeSerializer(config, valueType));
    }

    protected JsonSerializer<?> buildEnumSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonFormat.Value format = beanDesc.findExpectedFormat(null);
        if (format != null && format.getShape() == JsonFormat.Shape.OBJECT) {
            ((BasicBeanDescription)beanDesc).removeProperty("declaringClass");
            return null;
        }
        Class<?> enumClass = type.getRawClass();
        JsonSerializer ser = EnumSerializer.construct(enumClass, config, beanDesc, format);
        if (this._factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : this._factoryConfig.serializerModifiers()) {
                ser = mod.modifyEnumSerializer(config, type, beanDesc, ser);
            }
        }
        return ser;
    }

    protected JsonSerializer<Object> _findKeySerializer(SerializerProvider prov, Annotated a) throws JsonMappingException {
        AnnotationIntrospector intr = prov.getAnnotationIntrospector();
        Object serDef = intr.findKeySerializer(a);
        if (serDef != null) {
            return prov.serializerInstance(a, serDef);
        }
        return null;
    }

    protected JsonSerializer<Object> _findContentSerializer(SerializerProvider prov, Annotated a) throws JsonMappingException {
        AnnotationIntrospector intr = prov.getAnnotationIntrospector();
        Object serDef = intr.findContentSerializer(a);
        if (serDef != null) {
            return prov.serializerInstance(a, serDef);
        }
        return null;
    }

    protected Object findFilterId(SerializationConfig config, BeanDescription beanDesc) {
        return config.getAnnotationIntrospector().findFilterId(beanDesc.getClassInfo());
    }

    protected boolean usesStaticTyping(SerializationConfig config, BeanDescription beanDesc, TypeSerializer typeSer) {
        if (typeSer != null) {
            return false;
        }
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        JsonSerialize.Typing t = intr.findSerializationTyping(beanDesc.getClassInfo());
        if (t != null && t != JsonSerialize.Typing.DEFAULT_TYPING) {
            return t == JsonSerialize.Typing.STATIC;
        }
        return config.isEnabled(MapperFeature.USE_STATIC_TYPING);
    }

    static {
        HashMap<String, Class<TokenBufferSerializer>> concLazy = new HashMap<String, Class<TokenBufferSerializer>>();
        HashMap concrete = new HashMap();
        concrete.put(String.class.getName(), new StringSerializer());
        ToStringSerializer sls = ToStringSerializer.instance;
        concrete.put(StringBuffer.class.getName(), sls);
        concrete.put(StringBuilder.class.getName(), sls);
        concrete.put(Character.class.getName(), sls);
        concrete.put(Character.TYPE.getName(), sls);
        NumberSerializers.addAll(concrete);
        concrete.put(Boolean.TYPE.getName(), new BooleanSerializer(true));
        concrete.put(Boolean.class.getName(), new BooleanSerializer(false));
        concrete.put(BigInteger.class.getName(), new NumberSerializer((Class<? extends Number>)BigInteger.class));
        concrete.put(BigDecimal.class.getName(), new NumberSerializer((Class<? extends Number>)BigDecimal.class));
        concrete.put(Calendar.class.getName(), CalendarSerializer.instance);
        concrete.put(Date.class.getName(), DateSerializer.instance);
        for (Map.Entry<Class<?>, Object> en : StdJdkSerializers.all()) {
            Object value = en.getValue();
            if (value instanceof JsonSerializer) {
                concrete.put(en.getKey().getName(), (JsonSerializer)value);
                continue;
            }
            Class cls = (Class)value;
            concLazy.put(en.getKey().getName(), cls);
        }
        concLazy.put(TokenBuffer.class.getName(), TokenBufferSerializer.class);
        _concrete = concrete;
        _concreteLazy = concLazy;
    }
}

