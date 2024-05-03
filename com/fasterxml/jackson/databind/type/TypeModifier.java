/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Type;

public abstract class TypeModifier {
    public abstract JavaType modifyType(JavaType var1, Type var2, TypeBindings var3, TypeFactory var4);
}

