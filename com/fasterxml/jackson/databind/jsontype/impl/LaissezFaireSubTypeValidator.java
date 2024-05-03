/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public final class LaissezFaireSubTypeValidator
extends PolymorphicTypeValidator.Base {
    private static final long serialVersionUID = 1L;
    public static final LaissezFaireSubTypeValidator instance = new LaissezFaireSubTypeValidator();

    @Override
    public PolymorphicTypeValidator.Validity validateBaseType(MapperConfig<?> ctxt, JavaType baseType) {
        return PolymorphicTypeValidator.Validity.INDETERMINATE;
    }

    @Override
    public PolymorphicTypeValidator.Validity validateSubClassName(MapperConfig<?> ctxt, JavaType baseType, String subClassName) {
        return PolymorphicTypeValidator.Validity.ALLOWED;
    }

    @Override
    public PolymorphicTypeValidator.Validity validateSubType(MapperConfig<?> ctxt, JavaType baseType, JavaType subType) {
        return PolymorphicTypeValidator.Validity.ALLOWED;
    }
}

