/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.plugins.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.EnumConverter;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverter;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.util.TypeUtil;
import org.apache.logging.log4j.status.StatusLogger;

public class TypeConverterRegistry {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static volatile TypeConverterRegistry INSTANCE;
    private static final Object INSTANCE_LOCK;
    private final ConcurrentMap<Type, TypeConverter<?>> registry = new ConcurrentHashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static TypeConverterRegistry getInstance() {
        TypeConverterRegistry result = INSTANCE;
        if (result == null) {
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                result = INSTANCE;
                if (result == null) {
                    INSTANCE = result = new TypeConverterRegistry();
                }
            }
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public TypeConverter<?> findCompatibleConverter(Type type) {
        Class clazz;
        Objects.requireNonNull(type, "No type was provided");
        TypeConverter primary = (TypeConverter)this.registry.get(type);
        if (primary != null) {
            return primary;
        }
        if (type instanceof Class && (clazz = (Class)type).isEnum()) {
            EnumConverter<Enum> converter = new EnumConverter<Enum>(clazz.asSubclass(Enum.class));
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                return this.registerConverter(type, converter);
            }
        }
        for (Map.Entry entry : this.registry.entrySet()) {
            Type key = (Type)entry.getKey();
            if (!TypeUtil.isAssignable(type, key)) continue;
            LOGGER.debug("Found compatible TypeConverter<{}> for type [{}].", (Object)key, (Object)type);
            TypeConverter value = (TypeConverter)entry.getValue();
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                return this.registerConverter(type, value);
            }
        }
        throw new UnknownFormatConversionException(type.toString());
    }

    private TypeConverterRegistry() {
        LOGGER.trace("TypeConverterRegistry initializing.");
        PluginManager manager = new PluginManager("TypeConverter");
        manager.collectPlugins();
        this.loadKnownTypeConverters(manager.getPlugins().values());
        this.registerPrimitiveTypes();
    }

    private void loadKnownTypeConverters(Collection<PluginType<?>> knownTypes) {
        for (PluginType<?> knownType : knownTypes) {
            Class<?> clazz = knownType.getPluginClass();
            if (!TypeConverter.class.isAssignableFrom(clazz)) continue;
            Class<TypeConverter> pluginClass = clazz.asSubclass(TypeConverter.class);
            Type conversionType = TypeConverterRegistry.getTypeConverterSupportedType(pluginClass);
            TypeConverter converter = ReflectionUtil.instantiate(pluginClass);
            this.registerConverter(conversionType, converter);
        }
    }

    private TypeConverter<?> registerConverter(Type conversionType, TypeConverter<?> converter) {
        TypeConverter conflictingConverter = (TypeConverter)this.registry.get(conversionType);
        if (conflictingConverter != null) {
            Comparable comparableConflictingConverter;
            Comparable comparableConverter;
            boolean overridable = converter instanceof Comparable ? (comparableConverter = (Comparable)((Object)converter)).compareTo(conflictingConverter) < 0 : (conflictingConverter instanceof Comparable ? (comparableConflictingConverter = (Comparable)((Object)conflictingConverter)).compareTo(converter) > 0 : false);
            if (overridable) {
                LOGGER.debug("Replacing TypeConverter [{}] for type [{}] with [{}] after comparison.", (Object)conflictingConverter, (Object)conversionType, (Object)converter);
                this.registry.put(conversionType, converter);
                return converter;
            }
            LOGGER.warn("Ignoring TypeConverter [{}] for type [{}] that conflicts with [{}], since they are not comparable.", (Object)converter, (Object)conversionType, (Object)conflictingConverter);
            return conflictingConverter;
        }
        this.registry.put(conversionType, converter);
        return converter;
    }

    private static Type getTypeConverterSupportedType(Class<? extends TypeConverter> typeConverterClass) {
        for (Type type : typeConverterClass.getGenericInterfaces()) {
            ParameterizedType pType;
            if (!(type instanceof ParameterizedType) || !TypeConverter.class.equals((Object)(pType = (ParameterizedType)type).getRawType())) continue;
            return pType.getActualTypeArguments()[0];
        }
        return Void.TYPE;
    }

    private void registerPrimitiveTypes() {
        this.registerTypeAlias((Type)((Object)Boolean.class), Boolean.TYPE);
        this.registerTypeAlias((Type)((Object)Byte.class), Byte.TYPE);
        this.registerTypeAlias((Type)((Object)Character.class), Character.TYPE);
        this.registerTypeAlias((Type)((Object)Double.class), Double.TYPE);
        this.registerTypeAlias((Type)((Object)Float.class), Float.TYPE);
        this.registerTypeAlias((Type)((Object)Integer.class), Integer.TYPE);
        this.registerTypeAlias((Type)((Object)Long.class), Long.TYPE);
        this.registerTypeAlias((Type)((Object)Short.class), Short.TYPE);
    }

    private void registerTypeAlias(Type knownType, Type aliasType) {
        this.registry.putIfAbsent(aliasType, (TypeConverter<?>)this.registry.get(knownType));
    }

    static {
        INSTANCE_LOCK = new Object();
    }
}

