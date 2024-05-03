/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

import java.io.Serializable;

public abstract class ObjectIdGenerator<T>
implements Serializable {
    public abstract Class<?> getScope();

    public abstract boolean canUseFor(ObjectIdGenerator<?> var1);

    public boolean maySerializeAsObject() {
        return false;
    }

    public boolean isValidReferencePropertyName(String name, Object parser) {
        return false;
    }

    public abstract ObjectIdGenerator<T> forScope(Class<?> var1);

    public abstract ObjectIdGenerator<T> newForSerialization(Object var1);

    public abstract IdKey key(Object var1);

    public abstract T generateId(Object var1);

    public static final class IdKey
    implements Serializable {
        private static final long serialVersionUID = 1L;
        public final Class<?> type;
        public final Class<?> scope;
        public final Object key;
        private final int hashCode;

        public IdKey(Class<?> type, Class<?> scope, Object key) {
            if (key == null) {
                throw new IllegalArgumentException("Can not construct IdKey for null key");
            }
            this.type = type;
            this.scope = scope;
            this.key = key;
            int h = key.hashCode() + type.getName().hashCode();
            if (scope != null) {
                h ^= scope.getName().hashCode();
            }
            this.hashCode = h;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (o.getClass() != this.getClass()) {
                return false;
            }
            IdKey other = (IdKey)o;
            return other.key.equals(this.key) && other.type == this.type && other.scope == this.scope;
        }

        public String toString() {
            return String.format("[ObjectId: key=%s, type=%s, scope=%s]", this.key, this.type == null ? "NONE" : this.type.getName(), this.scope == null ? "NONE" : this.scope.getName());
        }
    }
}

