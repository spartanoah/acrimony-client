/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class PropertyBuilder {
    private static final Object NO_DEFAULT_MARKER = Boolean.FALSE;
    protected final SerializationConfig _config;
    protected final BeanDescription _beanDesc;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected Object _defaultBean;
    protected final JsonInclude.Value _defaultInclusion;
    protected final boolean _useRealPropertyDefaults;

    public PropertyBuilder(SerializationConfig config, BeanDescription beanDesc) {
        this._config = config;
        this._beanDesc = beanDesc;
        JsonInclude.Value inclPerType = JsonInclude.Value.merge(beanDesc.findPropertyInclusion(JsonInclude.Value.empty()), config.getDefaultPropertyInclusion(beanDesc.getBeanClass(), JsonInclude.Value.empty()));
        this._defaultInclusion = JsonInclude.Value.merge(config.getDefaultPropertyInclusion(), inclPerType);
        this._useRealPropertyDefaults = inclPerType.getValueInclusion() == JsonInclude.Include.NON_DEFAULT;
        this._annotationIntrospector = this._config.getAnnotationIntrospector();
    }

    public Annotations getClassAnnotations() {
        return this._beanDesc.getClassAnnotations();
    }

    protected BeanPropertyWriter buildWriter(SerializerProvider prov, BeanPropertyDefinition propDef, JavaType declaredType, JsonSerializer<?> ser, TypeSerializer typeSer, TypeSerializer contentTypeSer, AnnotatedMember am, boolean defaultUseStaticTyping) throws JsonMappingException {
        NameTransformer unwrapper;
        JavaType serializationType;
        try {
            serializationType = this.findSerializationType(am, defaultUseStaticTyping, declaredType);
        } catch (JsonMappingException e) {
            if (propDef == null) {
                return (BeanPropertyWriter)prov.reportBadDefinition(declaredType, ClassUtil.exceptionMessage(e));
            }
            return (BeanPropertyWriter)prov.reportBadPropertyDefinition(this._beanDesc, propDef, ClassUtil.exceptionMessage(e), new Object[0]);
        }
        if (contentTypeSer != null) {
            JavaType ct;
            if (serializationType == null) {
                serializationType = declaredType;
            }
            if ((ct = serializationType.getContentType()) == null) {
                prov.reportBadPropertyDefinition(this._beanDesc, propDef, "serialization type " + serializationType + " has no content", new Object[0]);
            }
            serializationType = serializationType.withContentTypeHandler(contentTypeSer);
            ct = serializationType.getContentType();
        }
        Object valueToSuppress = null;
        boolean suppressNulls = false;
        JavaType actualType = serializationType == null ? declaredType : serializationType;
        AnnotatedMember accessor = propDef.getAccessor();
        if (accessor == null) {
            return (BeanPropertyWriter)prov.reportBadPropertyDefinition(this._beanDesc, propDef, "could not determine property type", new Object[0]);
        }
        Class<?> rawPropertyType = accessor.getRawType();
        JsonInclude.Value inclV = this._config.getDefaultInclusion(actualType.getRawClass(), rawPropertyType, this._defaultInclusion);
        JsonInclude.Include inclusion = (inclV = inclV.withOverrides(propDef.findInclusion())).getValueInclusion();
        if (inclusion == JsonInclude.Include.USE_DEFAULTS) {
            inclusion = JsonInclude.Include.ALWAYS;
        }
        switch (inclusion) {
            case NON_DEFAULT: {
                Object defaultBean;
                if (this._useRealPropertyDefaults && (defaultBean = this.getDefaultBean()) != null) {
                    if (prov.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                        am.fixAccess(this._config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                    }
                    try {
                        valueToSuppress = am.getValue(defaultBean);
                    } catch (Exception e) {
                        this._throwWrapped(e, propDef.getName(), defaultBean);
                    }
                } else {
                    valueToSuppress = BeanUtil.getDefaultValue(actualType);
                    suppressNulls = true;
                }
                if (valueToSuppress == null) {
                    suppressNulls = true;
                    break;
                }
                if (!valueToSuppress.getClass().isArray()) break;
                valueToSuppress = ArrayBuilders.getArrayComparator(valueToSuppress);
                break;
            }
            case NON_ABSENT: {
                suppressNulls = true;
                if (!actualType.isReferenceType()) break;
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
                break;
            }
            case NON_EMPTY: {
                suppressNulls = true;
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
                break;
            }
            case CUSTOM: {
                valueToSuppress = prov.includeFilterInstance(propDef, inclV.getValueFilter());
                if (valueToSuppress == null) {
                    suppressNulls = true;
                    break;
                }
                suppressNulls = prov.includeFilterSuppressNulls(valueToSuppress);
                break;
            }
            case NON_NULL: {
                suppressNulls = true;
            }
            default: {
                if (!actualType.isContainerType() || this._config.isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)) break;
                valueToSuppress = BeanPropertyWriter.MARKER_FOR_EMPTY;
            }
        }
        Class<?>[] views = propDef.findViews();
        if (views == null) {
            views = this._beanDesc.findDefaultViews();
        }
        BeanPropertyWriter bpw = new BeanPropertyWriter(propDef, am, this._beanDesc.getClassAnnotations(), declaredType, ser, typeSer, serializationType, suppressNulls, valueToSuppress, views);
        Object serDef = this._annotationIntrospector.findNullSerializer(am);
        if (serDef != null) {
            bpw.assignNullSerializer(prov.serializerInstance(am, serDef));
        }
        if ((unwrapper = this._annotationIntrospector.findUnwrappingNameTransformer(am)) != null) {
            bpw = bpw.unwrappingWriter(unwrapper);
        }
        return bpw;
    }

    protected JavaType findSerializationType(Annotated a, boolean useStaticTyping, JavaType declaredType) throws JsonMappingException {
        JsonSerialize.Typing typing;
        JavaType secondary = this._annotationIntrospector.refineSerializationType(this._config, a, declaredType);
        if (secondary != declaredType) {
            Class<?> rawDeclared;
            Class<?> serClass = secondary.getRawClass();
            if (!serClass.isAssignableFrom(rawDeclared = declaredType.getRawClass()) && !rawDeclared.isAssignableFrom(serClass)) {
                throw new IllegalArgumentException("Illegal concrete-type annotation for method '" + a.getName() + "': class " + serClass.getName() + " not a super-type of (declared) class " + rawDeclared.getName());
            }
            useStaticTyping = true;
            declaredType = secondary;
        }
        if ((typing = this._annotationIntrospector.findSerializationTyping(a)) != null && typing != JsonSerialize.Typing.DEFAULT_TYPING) {
            boolean bl = useStaticTyping = typing == JsonSerialize.Typing.STATIC;
        }
        if (useStaticTyping) {
            return declaredType.withStaticTyping();
        }
        return null;
    }

    protected Object getDefaultBean() {
        Object def = this._defaultBean;
        if (def == null) {
            def = this._beanDesc.instantiateBean(this._config.canOverrideAccessModifiers());
            if (def == null) {
                def = NO_DEFAULT_MARKER;
            }
            this._defaultBean = def;
        }
        return def == NO_DEFAULT_MARKER ? null : this._defaultBean;
    }

    @Deprecated
    protected Object getPropertyDefaultValue(String name, AnnotatedMember member, JavaType type) {
        Object defaultBean = this.getDefaultBean();
        if (defaultBean == null) {
            return this.getDefaultValue(type);
        }
        try {
            return member.getValue(defaultBean);
        } catch (Exception e) {
            return this._throwWrapped(e, name, defaultBean);
        }
    }

    @Deprecated
    protected Object getDefaultValue(JavaType type) {
        return BeanUtil.getDefaultValue(type);
    }

    protected Object _throwWrapped(Exception e, String propName, Object defaultBean) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        ClassUtil.throwIfError(t);
        ClassUtil.throwIfRTE(t);
        throw new IllegalArgumentException("Failed to get property '" + propName + "' of default " + defaultBean.getClass().getName() + " instance");
    }
}

