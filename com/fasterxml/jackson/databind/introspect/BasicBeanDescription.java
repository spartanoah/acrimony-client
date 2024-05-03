/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BasicBeanDescription
extends BeanDescription {
    private static final Class<?>[] NO_VIEWS = new Class[0];
    protected final POJOPropertiesCollector _propCollector;
    protected final MapperConfig<?> _config;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final AnnotatedClass _classInfo;
    protected Class<?>[] _defaultViews;
    protected boolean _defaultViewsResolved;
    protected List<BeanPropertyDefinition> _properties;
    protected ObjectIdInfo _objectIdInfo;

    protected BasicBeanDescription(POJOPropertiesCollector coll, JavaType type, AnnotatedClass classDef) {
        super(type);
        this._propCollector = coll;
        this._config = coll.getConfig();
        this._annotationIntrospector = this._config == null ? null : this._config.getAnnotationIntrospector();
        this._classInfo = classDef;
    }

    protected BasicBeanDescription(MapperConfig<?> config, JavaType type, AnnotatedClass classDef, List<BeanPropertyDefinition> props) {
        super(type);
        this._propCollector = null;
        this._config = config;
        this._annotationIntrospector = this._config == null ? null : this._config.getAnnotationIntrospector();
        this._classInfo = classDef;
        this._properties = props;
    }

    protected BasicBeanDescription(POJOPropertiesCollector coll) {
        this(coll, coll.getType(), coll.getClassDef());
        this._objectIdInfo = coll.getObjectIdInfo();
    }

    public static BasicBeanDescription forDeserialization(POJOPropertiesCollector coll) {
        return new BasicBeanDescription(coll);
    }

    public static BasicBeanDescription forSerialization(POJOPropertiesCollector coll) {
        return new BasicBeanDescription(coll);
    }

    public static BasicBeanDescription forOtherUse(MapperConfig<?> config, JavaType type, AnnotatedClass ac) {
        return new BasicBeanDescription(config, type, ac, Collections.emptyList());
    }

    protected List<BeanPropertyDefinition> _properties() {
        if (this._properties == null) {
            this._properties = this._propCollector.getProperties();
        }
        return this._properties;
    }

    public boolean removeProperty(String propName) {
        Iterator<BeanPropertyDefinition> it = this._properties().iterator();
        while (it.hasNext()) {
            BeanPropertyDefinition prop = it.next();
            if (!prop.getName().equals(propName)) continue;
            it.remove();
            return true;
        }
        return false;
    }

    public boolean addProperty(BeanPropertyDefinition def) {
        if (this.hasProperty(def.getFullName())) {
            return false;
        }
        this._properties().add(def);
        return true;
    }

    public boolean hasProperty(PropertyName name) {
        return this.findProperty(name) != null;
    }

    public BeanPropertyDefinition findProperty(PropertyName name) {
        for (BeanPropertyDefinition prop : this._properties()) {
            if (!prop.hasName(name)) continue;
            return prop;
        }
        return null;
    }

    @Override
    public AnnotatedClass getClassInfo() {
        return this._classInfo;
    }

    @Override
    public ObjectIdInfo getObjectIdInfo() {
        return this._objectIdInfo;
    }

    @Override
    public List<BeanPropertyDefinition> findProperties() {
        return this._properties();
    }

    @Override
    @Deprecated
    public AnnotatedMethod findJsonValueMethod() {
        return this._propCollector == null ? null : this._propCollector.getJsonValueMethod();
    }

    @Override
    public AnnotatedMember findJsonValueAccessor() {
        return this._propCollector == null ? null : this._propCollector.getJsonValueAccessor();
    }

    @Override
    public Set<String> getIgnoredPropertyNames() {
        Set<String> ign;
        Set<String> set = ign = this._propCollector == null ? null : this._propCollector.getIgnoredPropertyNames();
        if (ign == null) {
            return Collections.emptySet();
        }
        return ign;
    }

    @Override
    public boolean hasKnownClassAnnotations() {
        return this._classInfo.hasAnnotations();
    }

    @Override
    public Annotations getClassAnnotations() {
        return this._classInfo.getAnnotations();
    }

    @Override
    @Deprecated
    public TypeBindings bindingsForBeanType() {
        return this._type.getBindings();
    }

    @Override
    @Deprecated
    public JavaType resolveType(Type jdkType) {
        if (jdkType == null) {
            return null;
        }
        return this._config.getTypeFactory().constructType(jdkType, this._type.getBindings());
    }

    @Override
    public AnnotatedConstructor findDefaultConstructor() {
        return this._classInfo.getDefaultConstructor();
    }

    @Override
    public AnnotatedMember findAnySetterAccessor() throws IllegalArgumentException {
        if (this._propCollector != null) {
            AnnotatedMethod anyMethod = this._propCollector.getAnySetterMethod();
            if (anyMethod != null) {
                Class<?> type = anyMethod.getRawParameterType(0);
                if (type != String.class && type != Object.class) {
                    throw new IllegalArgumentException(String.format("Invalid 'any-setter' annotation on method '%s()': first argument not of type String or Object, but %s", anyMethod.getName(), type.getName()));
                }
                return anyMethod;
            }
            AnnotatedMember anyField = this._propCollector.getAnySetterField();
            if (anyField != null) {
                Class<?> type = anyField.getRawType();
                if (!Map.class.isAssignableFrom(type)) {
                    throw new IllegalArgumentException(String.format("Invalid 'any-setter' annotation on field '%s': type is not instance of java.util.Map", anyField.getName()));
                }
                return anyField;
            }
        }
        return null;
    }

    @Override
    public Map<Object, AnnotatedMember> findInjectables() {
        if (this._propCollector != null) {
            return this._propCollector.getInjectables();
        }
        return Collections.emptyMap();
    }

    @Override
    public List<AnnotatedConstructor> getConstructors() {
        return this._classInfo.getConstructors();
    }

    @Override
    public Object instantiateBean(boolean fixAccess) {
        AnnotatedConstructor ac = this._classInfo.getDefaultConstructor();
        if (ac == null) {
            return null;
        }
        if (fixAccess) {
            ac.fixAccess(this._config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
        }
        try {
            return ((Constructor)ac.getAnnotated()).newInstance(new Object[0]);
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            ClassUtil.throwIfError(t);
            ClassUtil.throwIfRTE(t);
            throw new IllegalArgumentException("Failed to instantiate bean of type " + ((Class)this._classInfo.getAnnotated()).getName() + ": (" + t.getClass().getName() + ") " + ClassUtil.exceptionMessage(t), t);
        }
    }

    @Override
    public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
        return this._classInfo.findMethod(name, paramTypes);
    }

    @Override
    public JsonFormat.Value findExpectedFormat(JsonFormat.Value defValue) {
        JsonFormat.Value v;
        if (this._annotationIntrospector != null && (v = this._annotationIntrospector.findFormat(this._classInfo)) != null) {
            defValue = defValue == null ? v : defValue.withOverrides(v);
        }
        if ((v = this._config.getDefaultPropertyFormat(this._classInfo.getRawType())) != null) {
            defValue = defValue == null ? v : defValue.withOverrides(v);
        }
        return defValue;
    }

    @Override
    public Class<?>[] findDefaultViews() {
        if (!this._defaultViewsResolved) {
            Class<?>[] def;
            this._defaultViewsResolved = true;
            Class<?>[] classArray = def = this._annotationIntrospector == null ? null : this._annotationIntrospector.findViews(this._classInfo);
            if (def == null && !this._config.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION)) {
                def = NO_VIEWS;
            }
            this._defaultViews = def;
        }
        return this._defaultViews;
    }

    @Override
    public Converter<Object, Object> findSerializationConverter() {
        if (this._annotationIntrospector == null) {
            return null;
        }
        return this._createConverter(this._annotationIntrospector.findSerializationConverter(this._classInfo));
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(JsonInclude.Value defValue) {
        JsonInclude.Value incl;
        if (this._annotationIntrospector != null && (incl = this._annotationIntrospector.findPropertyInclusion(this._classInfo)) != null) {
            return defValue == null ? incl : defValue.withOverrides(incl);
        }
        return defValue;
    }

    @Override
    public AnnotatedMember findAnyGetter() throws IllegalArgumentException {
        Class<?> type;
        AnnotatedMember anyGetter;
        AnnotatedMember annotatedMember = anyGetter = this._propCollector == null ? null : this._propCollector.getAnyGetter();
        if (anyGetter != null && !Map.class.isAssignableFrom(type = anyGetter.getRawType())) {
            throw new IllegalArgumentException("Invalid 'any-getter' annotation on method " + anyGetter.getName() + "(): return type is not instance of java.util.Map");
        }
        return anyGetter;
    }

    @Override
    public List<BeanPropertyDefinition> findBackReferences() {
        ArrayList<BeanPropertyDefinition> result = null;
        HashSet<String> names = null;
        for (BeanPropertyDefinition property : this._properties()) {
            AnnotationIntrospector.ReferenceProperty refDef = property.findReferenceType();
            if (refDef == null || !refDef.isBackReference()) continue;
            String refName = refDef.getName();
            if (result == null) {
                result = new ArrayList<BeanPropertyDefinition>();
                names = new HashSet<String>();
                names.add(refName);
            } else if (!names.add(refName)) {
                throw new IllegalArgumentException("Multiple back-reference properties with name '" + refName + "'");
            }
            result.add(property);
        }
        return result;
    }

    @Override
    @Deprecated
    public Map<String, AnnotatedMember> findBackReferenceProperties() {
        List<BeanPropertyDefinition> props = this.findBackReferences();
        if (props == null) {
            return null;
        }
        HashMap<String, AnnotatedMember> result = new HashMap<String, AnnotatedMember>();
        for (BeanPropertyDefinition prop : props) {
            result.put(prop.getName(), prop.getMutator());
        }
        return result;
    }

    @Override
    public List<AnnotatedMethod> getFactoryMethods() {
        List<AnnotatedMethod> candidates = this._classInfo.getFactoryMethods();
        if (candidates.isEmpty()) {
            return candidates;
        }
        ArrayList<AnnotatedMethod> result = null;
        for (AnnotatedMethod am : candidates) {
            if (!this.isFactoryMethod(am)) continue;
            if (result == null) {
                result = new ArrayList<AnnotatedMethod>();
            }
            result.add(am);
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    @Override
    public Constructor<?> findSingleArgConstructor(Class<?> ... argTypes) {
        for (AnnotatedConstructor ac : this._classInfo.getConstructors()) {
            if (ac.getParameterCount() != 1) continue;
            Class<?> actArg = ac.getRawParameterType(0);
            for (Class<?> expArg : argTypes) {
                if (expArg != actArg) continue;
                return ac.getAnnotated();
            }
        }
        return null;
    }

    @Override
    public Method findFactoryMethod(Class<?> ... expArgTypes) {
        for (AnnotatedMethod am : this._classInfo.getFactoryMethods()) {
            if (!this.isFactoryMethod(am) || am.getParameterCount() != 1) continue;
            Class<?> actualArgType = am.getRawParameterType(0);
            for (Class<?> expArgType : expArgTypes) {
                if (!actualArgType.isAssignableFrom(expArgType)) continue;
                return am.getAnnotated();
            }
        }
        return null;
    }

    protected boolean isFactoryMethod(AnnotatedMethod am) {
        Class<?> cls;
        Class<?> rt = am.getRawReturnType();
        if (!this.getBeanClass().isAssignableFrom(rt)) {
            return false;
        }
        JsonCreator.Mode mode = this._annotationIntrospector.findCreatorAnnotation(this._config, am);
        if (mode != null && mode != JsonCreator.Mode.DISABLED) {
            return true;
        }
        String name = am.getName();
        if ("valueOf".equals(name) && am.getParameterCount() == 1) {
            return true;
        }
        return "fromString".equals(name) && am.getParameterCount() == 1 && ((cls = am.getRawParameterType(0)) == String.class || CharSequence.class.isAssignableFrom(cls));
    }

    @Deprecated
    protected PropertyName _findCreatorPropertyName(AnnotatedParameter param) {
        String str;
        PropertyName name = this._annotationIntrospector.findNameForDeserialization(param);
        if ((name == null || name.isEmpty()) && (str = this._annotationIntrospector.findImplicitPropertyName(param)) != null && !str.isEmpty()) {
            name = PropertyName.construct(str);
        }
        return name;
    }

    @Override
    public Class<?> findPOJOBuilder() {
        return this._annotationIntrospector == null ? null : this._annotationIntrospector.findPOJOBuilder(this._classInfo);
    }

    @Override
    public JsonPOJOBuilder.Value findPOJOBuilderConfig() {
        return this._annotationIntrospector == null ? null : this._annotationIntrospector.findPOJOBuilderConfig(this._classInfo);
    }

    @Override
    public Converter<Object, Object> findDeserializationConverter() {
        if (this._annotationIntrospector == null) {
            return null;
        }
        return this._createConverter(this._annotationIntrospector.findDeserializationConverter(this._classInfo));
    }

    @Override
    public String findClassDescription() {
        return this._annotationIntrospector == null ? null : this._annotationIntrospector.findClassDescription(this._classInfo);
    }

    @Deprecated
    public LinkedHashMap<String, AnnotatedField> _findPropertyFields(Collection<String> ignoredProperties, boolean forSerialization) {
        LinkedHashMap<String, AnnotatedField> results = new LinkedHashMap<String, AnnotatedField>();
        for (BeanPropertyDefinition property : this._properties()) {
            AnnotatedField f = property.getField();
            if (f == null) continue;
            String name = property.getName();
            if (ignoredProperties != null && ignoredProperties.contains(name)) continue;
            results.put(name, f);
        }
        return results;
    }

    protected Converter<Object, Object> _createConverter(Object converterDef) {
        Converter conv;
        if (converterDef == null) {
            return null;
        }
        if (converterDef instanceof Converter) {
            return (Converter)converterDef;
        }
        if (!(converterDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned Converter definition of type " + converterDef.getClass().getName() + "; expected type Converter or Class<Converter> instead");
        }
        Class converterClass = (Class)converterDef;
        if (converterClass == Converter.None.class || ClassUtil.isBogusClass(converterClass)) {
            return null;
        }
        if (!Converter.class.isAssignableFrom(converterClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + converterClass.getName() + "; expected Class<Converter>");
        }
        HandlerInstantiator hi = this._config.getHandlerInstantiator();
        Converter converter = conv = hi == null ? null : hi.converterInstance(this._config, this._classInfo, converterClass);
        if (conv == null) {
            conv = (Converter)ClassUtil.createInstance(converterClass, this._config.canOverrideAccessModifiers());
        }
        return conv;
    }
}

