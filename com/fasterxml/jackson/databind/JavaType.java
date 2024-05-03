/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public abstract class JavaType
extends ResolvedType
implements Serializable,
Type {
    private static final long serialVersionUID = 1L;
    protected final Class<?> _class;
    protected final int _hash;
    protected final Object _valueHandler;
    protected final Object _typeHandler;
    protected final boolean _asStatic;

    protected JavaType(Class<?> raw, int additionalHash, Object valueHandler, Object typeHandler, boolean asStatic) {
        this._class = raw;
        this._hash = raw.getName().hashCode() + additionalHash;
        this._valueHandler = valueHandler;
        this._typeHandler = typeHandler;
        this._asStatic = asStatic;
    }

    protected JavaType(JavaType base) {
        this._class = base._class;
        this._hash = base._hash;
        this._valueHandler = base._valueHandler;
        this._typeHandler = base._typeHandler;
        this._asStatic = base._asStatic;
    }

    public abstract JavaType withTypeHandler(Object var1);

    public abstract JavaType withContentTypeHandler(Object var1);

    public abstract JavaType withValueHandler(Object var1);

    public abstract JavaType withContentValueHandler(Object var1);

    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = this;
        Object h = src.getTypeHandler();
        if (h != this._typeHandler) {
            type = type.withTypeHandler(h);
        }
        if ((h = src.getValueHandler()) != this._valueHandler) {
            type = type.withValueHandler(h);
        }
        return type;
    }

    public abstract JavaType withContentType(JavaType var1);

    public abstract JavaType withStaticTyping();

    public abstract JavaType refine(Class<?> var1, TypeBindings var2, JavaType var3, JavaType[] var4);

    @Deprecated
    public JavaType forcedNarrowBy(Class<?> subclass) {
        if (subclass == this._class) {
            return this;
        }
        return this._narrow(subclass);
    }

    @Deprecated
    protected abstract JavaType _narrow(Class<?> var1);

    @Override
    public final Class<?> getRawClass() {
        return this._class;
    }

    @Override
    public final boolean hasRawClass(Class<?> clz) {
        return this._class == clz;
    }

    public boolean hasContentType() {
        return true;
    }

    public final boolean isTypeOrSubTypeOf(Class<?> clz) {
        return this._class == clz || clz.isAssignableFrom(this._class);
    }

    public final boolean isTypeOrSuperTypeOf(Class<?> clz) {
        return this._class == clz || this._class.isAssignableFrom(clz);
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this._class.getModifiers());
    }

    @Override
    public boolean isConcrete() {
        int mod = this._class.getModifiers();
        if ((mod & 0x600) == 0) {
            return true;
        }
        return this._class.isPrimitive();
    }

    @Override
    public boolean isThrowable() {
        return Throwable.class.isAssignableFrom(this._class);
    }

    @Override
    public boolean isArrayType() {
        return false;
    }

    @Override
    public final boolean isEnumType() {
        return ClassUtil.isEnumType(this._class);
    }

    public final boolean isEnumImplType() {
        return ClassUtil.isEnumType(this._class) && this._class != Enum.class;
    }

    @Override
    public final boolean isInterface() {
        return this._class.isInterface();
    }

    @Override
    public final boolean isPrimitive() {
        return this._class.isPrimitive();
    }

    @Override
    public final boolean isFinal() {
        return Modifier.isFinal(this._class.getModifiers());
    }

    @Override
    public abstract boolean isContainerType();

    @Override
    public boolean isCollectionLikeType() {
        return false;
    }

    @Override
    public boolean isMapLikeType() {
        return false;
    }

    public final boolean isJavaLangObject() {
        return this._class == Object.class;
    }

    public final boolean useStaticType() {
        return this._asStatic;
    }

    @Override
    public boolean hasGenericTypes() {
        return this.containedTypeCount() > 0;
    }

    @Override
    public JavaType getKeyType() {
        return null;
    }

    @Override
    public JavaType getContentType() {
        return null;
    }

    @Override
    public JavaType getReferencedType() {
        return null;
    }

    @Override
    public abstract int containedTypeCount();

    @Override
    public abstract JavaType containedType(int var1);

    @Override
    @Deprecated
    public abstract String containedTypeName(int var1);

    @Override
    @Deprecated
    public Class<?> getParameterSource() {
        return null;
    }

    public JavaType containedTypeOrUnknown(int index) {
        JavaType t = this.containedType(index);
        return t == null ? TypeFactory.unknownType() : t;
    }

    public abstract TypeBindings getBindings();

    public abstract JavaType findSuperType(Class<?> var1);

    public abstract JavaType getSuperClass();

    public abstract List<JavaType> getInterfaces();

    public abstract JavaType[] findTypeParameters(Class<?> var1);

    public <T> T getValueHandler() {
        return (T)this._valueHandler;
    }

    public <T> T getTypeHandler() {
        return (T)this._typeHandler;
    }

    public Object getContentValueHandler() {
        return null;
    }

    public Object getContentTypeHandler() {
        return null;
    }

    public boolean hasValueHandler() {
        return this._valueHandler != null;
    }

    public boolean hasHandlers() {
        return this._typeHandler != null || this._valueHandler != null;
    }

    public String getGenericSignature() {
        StringBuilder sb = new StringBuilder(40);
        this.getGenericSignature(sb);
        return sb.toString();
    }

    public abstract StringBuilder getGenericSignature(StringBuilder var1);

    public String getErasedSignature() {
        StringBuilder sb = new StringBuilder(40);
        this.getErasedSignature(sb);
        return sb.toString();
    }

    public abstract StringBuilder getErasedSignature(StringBuilder var1);

    public abstract String toString();

    public abstract boolean equals(Object var1);

    public final int hashCode() {
        return this._hash;
    }
}

