/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.util;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.reflections.ReflectionUtils;
import org.reflections.Store;
import org.reflections.util.QueryFunction;

public interface UtilQueryBuilder<F, E> {
    public QueryFunction<Store, E> get(F var1);

    default public QueryFunction<Store, E> of(F element) {
        return this.of(ReflectionUtils.extendType().get((AnnotatedElement)element));
    }

    default public QueryFunction<Store, E> of(F element, Predicate<? super E> predicate) {
        return this.of(element).filter(predicate);
    }

    default public <T> QueryFunction<Store, E> of(QueryFunction<Store, T> function) {
        return store -> function.apply(store).stream().flatMap(t -> this.get(t).apply(store).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

