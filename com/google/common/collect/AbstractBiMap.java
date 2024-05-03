/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible(emulated=true)
abstract class AbstractBiMap<K, V>
extends ForwardingMap<K, V>
implements BiMap<K, V>,
Serializable {
    private transient Map<K, V> delegate;
    transient AbstractBiMap<V, K> inverse;
    private transient Set<K> keySet;
    private transient Set<V> valueSet;
    private transient Set<Map.Entry<K, V>> entrySet;
    @GwtIncompatible(value="Not needed in emulated source.")
    private static final long serialVersionUID = 0L;

    AbstractBiMap(Map<K, V> forward, Map<V, K> backward) {
        this.setDelegates(forward, backward);
    }

    private AbstractBiMap(Map<K, V> backward, AbstractBiMap<V, K> forward) {
        this.delegate = backward;
        this.inverse = forward;
    }

    @Override
    protected Map<K, V> delegate() {
        return this.delegate;
    }

    K checkKey(@Nullable K key) {
        return key;
    }

    V checkValue(@Nullable V value) {
        return value;
    }

    void setDelegates(Map<K, V> forward, Map<V, K> backward) {
        Preconditions.checkState(this.delegate == null);
        Preconditions.checkState(this.inverse == null);
        Preconditions.checkArgument(forward.isEmpty());
        Preconditions.checkArgument(backward.isEmpty());
        Preconditions.checkArgument(forward != backward);
        this.delegate = forward;
        this.inverse = new Inverse<V, K>(backward, this);
    }

    void setInverse(AbstractBiMap<V, K> inverse) {
        this.inverse = inverse;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return this.inverse.containsKey(value);
    }

    @Override
    public V put(@Nullable K key, @Nullable V value) {
        return this.putInBothMaps(key, value, false);
    }

    @Override
    public V forcePut(@Nullable K key, @Nullable V value) {
        return this.putInBothMaps(key, value, true);
    }

    private V putInBothMaps(@Nullable K key, @Nullable V value, boolean force) {
        this.checkKey(key);
        this.checkValue(value);
        boolean containedKey = this.containsKey(key);
        if (containedKey && Objects.equal(value, this.get(key))) {
            return value;
        }
        if (force) {
            this.inverse().remove(value);
        } else {
            Preconditions.checkArgument(!this.containsValue(value), "value already present: %s", value);
        }
        V oldValue = this.delegate.put(key, value);
        this.updateInverseMap(key, containedKey, oldValue, value);
        return oldValue;
    }

    private void updateInverseMap(K key, boolean containedKey, V oldValue, V newValue) {
        if (containedKey) {
            this.removeFromInverseMap(oldValue);
        }
        this.inverse.delegate.put(newValue, key);
    }

    @Override
    public V remove(@Nullable Object key) {
        return this.containsKey(key) ? (V)this.removeFromBothMaps(key) : null;
    }

    private V removeFromBothMaps(Object key) {
        V oldValue = this.delegate.remove(key);
        this.removeFromInverseMap(oldValue);
        return oldValue;
    }

    private void removeFromInverseMap(V oldValue) {
        this.inverse.delegate.remove(oldValue);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.delegate.clear();
        this.inverse.delegate.clear();
    }

    @Override
    public BiMap<V, K> inverse() {
        return this.inverse;
    }

    @Override
    public Set<K> keySet() {
        KeySet result = this.keySet;
        return result == null ? (this.keySet = new KeySet()) : result;
    }

    @Override
    public Set<V> values() {
        ValueSet result = this.valueSet;
        return result == null ? (this.valueSet = new ValueSet()) : result;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet result = this.entrySet;
        return result == null ? (this.entrySet = new EntrySet()) : result;
    }

    private static class Inverse<K, V>
    extends AbstractBiMap<K, V> {
        @GwtIncompatible(value="Not needed in emulated source.")
        private static final long serialVersionUID = 0L;

        private Inverse(Map<K, V> backward, AbstractBiMap<V, K> forward) {
            super(backward, forward);
        }

        @Override
        K checkKey(K key) {
            return this.inverse.checkValue(key);
        }

        @Override
        V checkValue(V value) {
            return this.inverse.checkKey(value);
        }

        @GwtIncompatible(value="java.io.ObjectOuputStream")
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            stream.writeObject(this.inverse());
        }

        @GwtIncompatible(value="java.io.ObjectInputStream")
        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.setInverse((AbstractBiMap)stream.readObject());
        }

        @GwtIncompatible(value="Not needed in the emulated source.")
        Object readResolve() {
            return this.inverse().inverse();
        }
    }

    private class EntrySet
    extends ForwardingSet<Map.Entry<K, V>> {
        final Set<Map.Entry<K, V>> esDelegate;

        private EntrySet() {
            this.esDelegate = AbstractBiMap.this.delegate.entrySet();
        }

        @Override
        protected Set<Map.Entry<K, V>> delegate() {
            return this.esDelegate;
        }

        @Override
        public void clear() {
            AbstractBiMap.this.clear();
        }

        @Override
        public boolean remove(Object object) {
            if (!this.esDelegate.contains(object)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)object;
            AbstractBiMap.this.inverse.delegate.remove(entry.getValue());
            this.esDelegate.remove(entry);
            return true;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            final Iterator iterator = this.esDelegate.iterator();
            return new Iterator<Map.Entry<K, V>>(){
                Map.Entry<K, V> entry;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Map.Entry<K, V> next() {
                    this.entry = (Map.Entry)iterator.next();
                    final Map.Entry finalEntry = this.entry;
                    return new ForwardingMapEntry<K, V>(){

                        @Override
                        protected Map.Entry<K, V> delegate() {
                            return finalEntry;
                        }

                        @Override
                        public V setValue(V value) {
                            Preconditions.checkState(EntrySet.this.contains(this), "entry no longer in map");
                            if (Objects.equal(value, this.getValue())) {
                                return value;
                            }
                            Preconditions.checkArgument(!AbstractBiMap.this.containsValue(value), "value already present: %s", value);
                            Object oldValue = finalEntry.setValue(value);
                            Preconditions.checkState(Objects.equal(value, AbstractBiMap.this.get(this.getKey())), "entry no longer in map");
                            AbstractBiMap.this.updateInverseMap(this.getKey(), true, oldValue, value);
                            return oldValue;
                        }
                    };
                }

                @Override
                public void remove() {
                    CollectPreconditions.checkRemove(this.entry != null);
                    Object value = this.entry.getValue();
                    iterator.remove();
                    AbstractBiMap.this.removeFromInverseMap(value);
                }
            };
        }

        @Override
        public Object[] toArray() {
            return this.standardToArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return this.standardToArray(array);
        }

        @Override
        public boolean contains(Object o) {
            return Maps.containsEntryImpl(this.delegate(), o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.standardContainsAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.standardRemoveAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.standardRetainAll(c);
        }
    }

    private class ValueSet
    extends ForwardingSet<V> {
        final Set<V> valuesDelegate;

        private ValueSet() {
            this.valuesDelegate = AbstractBiMap.this.inverse.keySet();
        }

        @Override
        protected Set<V> delegate() {
            return this.valuesDelegate;
        }

        @Override
        public Iterator<V> iterator() {
            return Maps.valueIterator(AbstractBiMap.this.entrySet().iterator());
        }

        @Override
        public Object[] toArray() {
            return this.standardToArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return this.standardToArray(array);
        }

        @Override
        public String toString() {
            return this.standardToString();
        }
    }

    private class KeySet
    extends ForwardingSet<K> {
        private KeySet() {
        }

        @Override
        protected Set<K> delegate() {
            return AbstractBiMap.this.delegate.keySet();
        }

        @Override
        public void clear() {
            AbstractBiMap.this.clear();
        }

        @Override
        public boolean remove(Object key) {
            if (!this.contains(key)) {
                return false;
            }
            AbstractBiMap.this.removeFromBothMaps(key);
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> keysToRemove) {
            return this.standardRemoveAll(keysToRemove);
        }

        @Override
        public boolean retainAll(Collection<?> keysToRetain) {
            return this.standardRetainAll(keysToRetain);
        }

        @Override
        public Iterator<K> iterator() {
            return Maps.keyIterator(AbstractBiMap.this.entrySet().iterator());
        }
    }
}

