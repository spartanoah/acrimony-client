/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

public class JsonLocationInstantiator
extends ValueInstantiator.Base {
    private static final long serialVersionUID = 1L;

    public JsonLocationInstantiator() {
        super(JsonLocation.class);
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return true;
    }

    @Override
    public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
        JavaType intType = config.constructType(Integer.TYPE);
        JavaType longType = config.constructType(Long.TYPE);
        return new SettableBeanProperty[]{JsonLocationInstantiator.creatorProp("sourceRef", config.constructType(Object.class), 0), JsonLocationInstantiator.creatorProp("byteOffset", longType, 1), JsonLocationInstantiator.creatorProp("charOffset", longType, 2), JsonLocationInstantiator.creatorProp("lineNr", intType, 3), JsonLocationInstantiator.creatorProp("columnNr", intType, 4)};
    }

    private static CreatorProperty creatorProp(String name, JavaType type, int index) {
        return CreatorProperty.construct(PropertyName.construct(name), type, null, null, null, null, index, null, PropertyMetadata.STD_REQUIRED);
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) {
        return new JsonLocation(args[0], JsonLocationInstantiator._long(args[1]), JsonLocationInstantiator._long(args[2]), JsonLocationInstantiator._int(args[3]), JsonLocationInstantiator._int(args[4]));
    }

    private static final long _long(Object o) {
        return o == null ? 0L : ((Number)o).longValue();
    }

    private static final int _int(Object o) {
        return o == null ? 0 : ((Number)o).intValue();
    }
}

