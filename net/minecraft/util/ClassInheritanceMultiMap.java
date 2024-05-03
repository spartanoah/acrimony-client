/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.optifine.util.IteratorCache;

public class ClassInheritanceMultiMap<T>
extends AbstractSet<T> {
    private static final Set<Class<?>> field_181158_a = Collections.newSetFromMap(new ConcurrentHashMap());
    private final Map<Class<?>, List<T>> map = Maps.newHashMap();
    private final Set<Class<?>> knownKeys = Sets.newIdentityHashSet();
    private final Class<T> baseClass;
    private final List<T> field_181745_e = Lists.newArrayList();
    public boolean empty;

    public ClassInheritanceMultiMap(Class<T> baseClassIn) {
        this.baseClass = baseClassIn;
        this.knownKeys.add(baseClassIn);
        this.map.put(baseClassIn, this.field_181745_e);
        for (Class<?> oclass : field_181158_a) {
            this.createLookup(oclass);
        }
        this.empty = this.field_181745_e.size() == 0;
    }

    protected void createLookup(Class<?> clazz) {
        field_181158_a.add(clazz);
        int i = this.field_181745_e.size();
        for (int j = 0; j < i; ++j) {
            T t = this.field_181745_e.get(j);
            if (!clazz.isAssignableFrom(t.getClass())) continue;
            this.func_181743_a(t, clazz);
        }
        this.knownKeys.add(clazz);
    }

    protected Class<?> func_181157_b(Class<?> p_181157_1_) {
        if (this.baseClass.isAssignableFrom(p_181157_1_)) {
            if (!this.knownKeys.contains(p_181157_1_)) {
                this.createLookup(p_181157_1_);
            }
            return p_181157_1_;
        }
        throw new IllegalArgumentException("Don't know how to search for " + p_181157_1_);
    }

    @Override
    public boolean add(T p_add_1_) {
        for (Class<?> oclass : this.knownKeys) {
            if (!oclass.isAssignableFrom(p_add_1_.getClass())) continue;
            this.func_181743_a(p_add_1_, oclass);
        }
        this.empty = this.field_181745_e.size() == 0;
        return true;
    }

    private void func_181743_a(T p_181743_1_, Class<?> p_181743_2_) {
        List<T> list = this.map.get(p_181743_2_);
        if (list == null) {
            this.map.put(p_181743_2_, Lists.newArrayList(p_181743_1_));
        } else {
            list.add(p_181743_1_);
        }
        this.empty = this.field_181745_e.size() == 0;
    }

    @Override
    public boolean remove(Object p_remove_1_) {
        Object t = p_remove_1_;
        boolean flag = false;
        for (Class<?> oclass : this.knownKeys) {
            List<T> list;
            if (!oclass.isAssignableFrom(t.getClass()) || (list = this.map.get(oclass)) == null || !list.remove(t)) continue;
            flag = true;
        }
        this.empty = this.field_181745_e.size() == 0;
        return flag;
    }

    @Override
    public boolean contains(Object p_contains_1_) {
        return Iterators.contains(this.getByClass(p_contains_1_.getClass()).iterator(), p_contains_1_);
    }

    public <S> Iterable<S> getByClass(final Class<S> clazz) {
        return new Iterable<S>(){

            @Override
            public Iterator<S> iterator() {
                List list = (List)ClassInheritanceMultiMap.this.map.get(ClassInheritanceMultiMap.this.func_181157_b(clazz));
                if (list == null) {
                    return Iterators.emptyIterator();
                }
                Iterator iterator = list.iterator();
                return Iterators.filter(iterator, clazz);
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return this.field_181745_e.isEmpty() ? Iterators.emptyIterator() : IteratorCache.getReadOnly(this.field_181745_e);
    }

    @Override
    public int size() {
        return this.field_181745_e.size();
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }
}

