/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ArrayIterator;
import java.io.Serializable;

public final class SerializerFactoryConfig
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static final Serializers[] NO_SERIALIZERS = new Serializers[0];
    protected static final BeanSerializerModifier[] NO_MODIFIERS = new BeanSerializerModifier[0];
    protected final Serializers[] _additionalSerializers;
    protected final Serializers[] _additionalKeySerializers;
    protected final BeanSerializerModifier[] _modifiers;

    public SerializerFactoryConfig() {
        this(null, null, null);
    }

    protected SerializerFactoryConfig(Serializers[] allAdditionalSerializers, Serializers[] allAdditionalKeySerializers, BeanSerializerModifier[] modifiers) {
        this._additionalSerializers = allAdditionalSerializers == null ? NO_SERIALIZERS : allAdditionalSerializers;
        this._additionalKeySerializers = allAdditionalKeySerializers == null ? NO_SERIALIZERS : allAdditionalKeySerializers;
        this._modifiers = modifiers == null ? NO_MODIFIERS : modifiers;
    }

    public SerializerFactoryConfig withAdditionalSerializers(Serializers additional) {
        if (additional == null) {
            throw new IllegalArgumentException("Cannot pass null Serializers");
        }
        Serializers[] all = ArrayBuilders.insertInListNoDup(this._additionalSerializers, additional);
        return new SerializerFactoryConfig(all, this._additionalKeySerializers, this._modifiers);
    }

    public SerializerFactoryConfig withAdditionalKeySerializers(Serializers additional) {
        if (additional == null) {
            throw new IllegalArgumentException("Cannot pass null Serializers");
        }
        Serializers[] all = ArrayBuilders.insertInListNoDup(this._additionalKeySerializers, additional);
        return new SerializerFactoryConfig(this._additionalSerializers, all, this._modifiers);
    }

    public SerializerFactoryConfig withSerializerModifier(BeanSerializerModifier modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("Cannot pass null modifier");
        }
        BeanSerializerModifier[] modifiers = ArrayBuilders.insertInListNoDup(this._modifiers, modifier);
        return new SerializerFactoryConfig(this._additionalSerializers, this._additionalKeySerializers, modifiers);
    }

    public boolean hasSerializers() {
        return this._additionalSerializers.length > 0;
    }

    public boolean hasKeySerializers() {
        return this._additionalKeySerializers.length > 0;
    }

    public boolean hasSerializerModifiers() {
        return this._modifiers.length > 0;
    }

    public Iterable<Serializers> serializers() {
        return new ArrayIterator<Serializers>(this._additionalSerializers);
    }

    public Iterable<Serializers> keySerializers() {
        return new ArrayIterator<Serializers>(this._additionalKeySerializers);
    }

    public Iterable<BeanSerializerModifier> serializerModifiers() {
        return new ArrayIterator<BeanSerializerModifier>(this._modifiers);
    }
}

