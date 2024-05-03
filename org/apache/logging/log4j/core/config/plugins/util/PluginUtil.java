/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;

public final class PluginUtil {
    private PluginUtil() {
    }

    public static Map<String, PluginType<?>> collectPluginsByCategory(String category) {
        Objects.requireNonNull(category, "category");
        return PluginUtil.collectPluginsByCategoryAndPackage(category, Collections.emptyList());
    }

    public static Map<String, PluginType<?>> collectPluginsByCategoryAndPackage(String category, List<String> packages) {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(packages, "packages");
        PluginManager pluginManager = new PluginManager(category);
        pluginManager.collectPlugins(packages);
        return pluginManager.getPlugins();
    }

    public static <V> V instantiatePlugin(Class<V> pluginClass) {
        Objects.requireNonNull(pluginClass, "pluginClass");
        Method pluginFactoryMethod = PluginUtil.findPluginFactoryMethod(pluginClass);
        try {
            Object instance = pluginFactoryMethod.invoke(null, new Object[0]);
            return (V)instance;
        } catch (IllegalAccessException | InvocationTargetException error) {
            String message = String.format("failed to instantiate plugin of type %s using the factory method %s", pluginClass, pluginFactoryMethod);
            throw new IllegalStateException(message, error);
        }
    }

    public static Method findPluginFactoryMethod(Class<?> pluginClass) {
        Objects.requireNonNull(pluginClass, "pluginClass");
        for (Method method : pluginClass.getDeclaredMethods()) {
            boolean methodStatic;
            boolean methodAnnotated = method.isAnnotationPresent(PluginFactory.class);
            if (!methodAnnotated || !(methodStatic = Modifier.isStatic(method.getModifiers()))) continue;
            return method;
        }
        throw new IllegalStateException("no factory method found for class " + pluginClass);
    }
}

