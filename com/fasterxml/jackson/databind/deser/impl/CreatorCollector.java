/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.reflect.Member;
import java.util.HashMap;

public class CreatorCollector {
    protected static final int C_DEFAULT = 0;
    protected static final int C_STRING = 1;
    protected static final int C_INT = 2;
    protected static final int C_LONG = 3;
    protected static final int C_DOUBLE = 4;
    protected static final int C_BOOLEAN = 5;
    protected static final int C_DELEGATE = 6;
    protected static final int C_PROPS = 7;
    protected static final int C_ARRAY_DELEGATE = 8;
    protected static final String[] TYPE_DESCS = new String[]{"default", "from-String", "from-int", "from-long", "from-double", "from-boolean", "delegate", "property-based"};
    protected final BeanDescription _beanDesc;
    protected final boolean _canFixAccess;
    protected final boolean _forceAccess;
    protected final AnnotatedWithParams[] _creators = new AnnotatedWithParams[9];
    protected int _explicitCreators = 0;
    protected boolean _hasNonDefaultCreator = false;
    protected SettableBeanProperty[] _delegateArgs;
    protected SettableBeanProperty[] _arrayDelegateArgs;
    protected SettableBeanProperty[] _propertyBasedArgs;

    public CreatorCollector(BeanDescription beanDesc, MapperConfig<?> config) {
        this._beanDesc = beanDesc;
        this._canFixAccess = config.canOverrideAccessModifiers();
        this._forceAccess = config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
    }

    public ValueInstantiator constructValueInstantiator(DeserializationContext ctxt) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        JavaType delegateType = this._computeDelegateType(ctxt, this._creators[6], this._delegateArgs);
        JavaType arrayDelegateType = this._computeDelegateType(ctxt, this._creators[8], this._arrayDelegateArgs);
        JavaType type = this._beanDesc.getType();
        StdValueInstantiator inst = new StdValueInstantiator(config, type);
        inst.configureFromObjectSettings(this._creators[0], this._creators[6], delegateType, this._delegateArgs, this._creators[7], this._propertyBasedArgs);
        inst.configureFromArraySettings(this._creators[8], arrayDelegateType, this._arrayDelegateArgs);
        inst.configureFromStringCreator(this._creators[1]);
        inst.configureFromIntCreator(this._creators[2]);
        inst.configureFromLongCreator(this._creators[3]);
        inst.configureFromDoubleCreator(this._creators[4]);
        inst.configureFromBooleanCreator(this._creators[5]);
        return inst;
    }

    public void setDefaultCreator(AnnotatedWithParams creator) {
        this._creators[0] = this._fixAccess(creator);
    }

    public void addStringCreator(AnnotatedWithParams creator, boolean explicit) {
        this.verifyNonDup(creator, 1, explicit);
    }

    public void addIntCreator(AnnotatedWithParams creator, boolean explicit) {
        this.verifyNonDup(creator, 2, explicit);
    }

    public void addLongCreator(AnnotatedWithParams creator, boolean explicit) {
        this.verifyNonDup(creator, 3, explicit);
    }

    public void addDoubleCreator(AnnotatedWithParams creator, boolean explicit) {
        this.verifyNonDup(creator, 4, explicit);
    }

    public void addBooleanCreator(AnnotatedWithParams creator, boolean explicit) {
        this.verifyNonDup(creator, 5, explicit);
    }

    public void addDelegatingCreator(AnnotatedWithParams creator, boolean explicit, SettableBeanProperty[] injectables, int delegateeIndex) {
        if (creator.getParameterType(delegateeIndex).isCollectionLikeType()) {
            if (this.verifyNonDup(creator, 8, explicit)) {
                this._arrayDelegateArgs = injectables;
            }
        } else if (this.verifyNonDup(creator, 6, explicit)) {
            this._delegateArgs = injectables;
        }
    }

    public void addPropertyCreator(AnnotatedWithParams creator, boolean explicit, SettableBeanProperty[] properties) {
        if (this.verifyNonDup(creator, 7, explicit)) {
            if (properties.length > 1) {
                HashMap<String, Integer> names = new HashMap<String, Integer>();
                int len = properties.length;
                for (int i = 0; i < len; ++i) {
                    Integer old;
                    String name = properties[i].getName();
                    if (name.isEmpty() && properties[i].getInjectableValueId() != null || (old = names.put(name, i)) == null) continue;
                    throw new IllegalArgumentException(String.format("Duplicate creator property \"%s\" (index %s vs %d) for type %s ", name, old, i, ClassUtil.nameOf(this._beanDesc.getBeanClass())));
                }
            }
            this._propertyBasedArgs = properties;
        }
    }

    public boolean hasDefaultCreator() {
        return this._creators[0] != null;
    }

    public boolean hasDelegatingCreator() {
        return this._creators[6] != null;
    }

    public boolean hasPropertyBasedCreator() {
        return this._creators[7] != null;
    }

    private JavaType _computeDelegateType(DeserializationContext ctxt, AnnotatedWithParams creator, SettableBeanProperty[] delegateArgs) throws JsonMappingException {
        if (!this._hasNonDefaultCreator || creator == null) {
            return null;
        }
        int ix = 0;
        if (delegateArgs != null) {
            int len = delegateArgs.length;
            for (int i = 0; i < len; ++i) {
                if (delegateArgs[i] != null) continue;
                ix = i;
                break;
            }
        }
        DeserializationConfig config = ctxt.getConfig();
        JavaType baseType = creator.getParameterType(ix);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        if (intr != null) {
            AnnotatedParameter delegate = creator.getParameter(ix);
            Object deserDef = intr.findDeserializer(delegate);
            if (deserDef != null) {
                JsonDeserializer<Object> deser = ctxt.deserializerInstance(delegate, deserDef);
                baseType = baseType.withValueHandler(deser);
            } else {
                baseType = intr.refineDeserializationType(config, delegate, baseType);
            }
        }
        return baseType;
    }

    private <T extends AnnotatedMember> T _fixAccess(T member) {
        if (member != null && this._canFixAccess) {
            ClassUtil.checkAndFixAccess((Member)((Object)member.getAnnotated()), this._forceAccess);
        }
        return member;
    }

    protected boolean verifyNonDup(AnnotatedWithParams newOne, int typeIndex, boolean explicit) {
        int mask = 1 << typeIndex;
        this._hasNonDefaultCreator = true;
        AnnotatedWithParams oldOne = this._creators[typeIndex];
        if (oldOne != null) {
            boolean verify;
            if ((this._explicitCreators & mask) != 0) {
                if (!explicit) {
                    return false;
                }
                verify = true;
            } else {
                boolean bl = verify = !explicit;
            }
            if (verify && oldOne.getClass() == newOne.getClass()) {
                Class<?> newType;
                Class<?> oldType = oldOne.getRawParameterType(0);
                if (oldType == (newType = newOne.getRawParameterType(0))) {
                    if (this._isEnumValueOf(newOne)) {
                        return false;
                    }
                    if (!this._isEnumValueOf(oldOne)) {
                        throw new IllegalArgumentException(String.format("Conflicting %s creators: already had %s creator %s, encountered another: %s", TYPE_DESCS[typeIndex], explicit ? "explicitly marked" : "implicitly discovered", oldOne, newOne));
                    }
                } else if (newType.isAssignableFrom(oldType)) {
                    return false;
                }
            }
        }
        if (explicit) {
            this._explicitCreators |= mask;
        }
        this._creators[typeIndex] = this._fixAccess(newOne);
        return true;
    }

    protected boolean _isEnumValueOf(AnnotatedWithParams creator) {
        return ClassUtil.isEnumType(creator.getDeclaringClass()) && "valueOf".equals(creator.getName());
    }
}

