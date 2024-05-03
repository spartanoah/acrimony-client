/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
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
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class POJOPropertiesCollector {
    protected final MapperConfig<?> _config;
    protected final boolean _forSerialization;
    protected final boolean _stdBeanNaming;
    protected final JavaType _type;
    protected final AnnotatedClass _classDef;
    protected final VisibilityChecker<?> _visibilityChecker;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final boolean _useAnnotations;
    protected final String _mutatorPrefix;
    protected boolean _collected;
    protected LinkedHashMap<String, POJOPropertyBuilder> _properties;
    protected LinkedList<POJOPropertyBuilder> _creatorProperties;
    protected Map<PropertyName, PropertyName> _fieldRenameMappings;
    protected LinkedList<AnnotatedMember> _anyGetters;
    protected LinkedList<AnnotatedMethod> _anySetters;
    protected LinkedList<AnnotatedMember> _anySetterField;
    protected LinkedList<AnnotatedMember> _jsonValueAccessors;
    protected HashSet<String> _ignoredPropertyNames;
    protected LinkedHashMap<Object, AnnotatedMember> _injectables;

    protected POJOPropertiesCollector(MapperConfig<?> config, boolean forSerialization, JavaType type, AnnotatedClass classDef, String mutatorPrefix) {
        this._config = config;
        this._stdBeanNaming = config.isEnabled(MapperFeature.USE_STD_BEAN_NAMING);
        this._forSerialization = forSerialization;
        this._type = type;
        this._classDef = classDef;
        String string = this._mutatorPrefix = mutatorPrefix == null ? "set" : mutatorPrefix;
        if (config.isAnnotationProcessingEnabled()) {
            this._useAnnotations = true;
            this._annotationIntrospector = this._config.getAnnotationIntrospector();
        } else {
            this._useAnnotations = false;
            this._annotationIntrospector = AnnotationIntrospector.nopInstance();
        }
        this._visibilityChecker = this._config.getDefaultVisibilityChecker(type.getRawClass(), classDef);
    }

    public MapperConfig<?> getConfig() {
        return this._config;
    }

    public JavaType getType() {
        return this._type;
    }

    public AnnotatedClass getClassDef() {
        return this._classDef;
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return this._annotationIntrospector;
    }

    public List<BeanPropertyDefinition> getProperties() {
        Map<String, POJOPropertyBuilder> props = this.getPropertyMap();
        return new ArrayList<BeanPropertyDefinition>(props.values());
    }

    public Map<Object, AnnotatedMember> getInjectables() {
        if (!this._collected) {
            this.collectAll();
        }
        return this._injectables;
    }

    public AnnotatedMember getJsonValueAccessor() {
        if (!this._collected) {
            this.collectAll();
        }
        if (this._jsonValueAccessors != null) {
            if (this._jsonValueAccessors.size() > 1) {
                this.reportProblem("Multiple 'as-value' properties defined (%s vs %s)", this._jsonValueAccessors.get(0), this._jsonValueAccessors.get(1));
            }
            return this._jsonValueAccessors.get(0);
        }
        return null;
    }

    public AnnotatedMember getAnyGetter() {
        if (!this._collected) {
            this.collectAll();
        }
        if (this._anyGetters != null) {
            if (this._anyGetters.size() > 1) {
                this.reportProblem("Multiple 'any-getters' defined (%s vs %s)", this._anyGetters.get(0), this._anyGetters.get(1));
            }
            return this._anyGetters.getFirst();
        }
        return null;
    }

    public AnnotatedMember getAnySetterField() {
        if (!this._collected) {
            this.collectAll();
        }
        if (this._anySetterField != null) {
            if (this._anySetterField.size() > 1) {
                this.reportProblem("Multiple 'any-setter' fields defined (%s vs %s)", this._anySetterField.get(0), this._anySetterField.get(1));
            }
            return this._anySetterField.getFirst();
        }
        return null;
    }

    public AnnotatedMethod getAnySetterMethod() {
        if (!this._collected) {
            this.collectAll();
        }
        if (this._anySetters != null) {
            if (this._anySetters.size() > 1) {
                this.reportProblem("Multiple 'any-setter' methods defined (%s vs %s)", this._anySetters.get(0), this._anySetters.get(1));
            }
            return this._anySetters.getFirst();
        }
        return null;
    }

    public Set<String> getIgnoredPropertyNames() {
        return this._ignoredPropertyNames;
    }

    public ObjectIdInfo getObjectIdInfo() {
        ObjectIdInfo info = this._annotationIntrospector.findObjectIdInfo(this._classDef);
        if (info != null) {
            info = this._annotationIntrospector.findObjectReferenceInfo(this._classDef, info);
        }
        return info;
    }

    protected Map<String, POJOPropertyBuilder> getPropertyMap() {
        if (!this._collected) {
            this.collectAll();
        }
        return this._properties;
    }

    @Deprecated
    public AnnotatedMethod getJsonValueMethod() {
        AnnotatedMember m = this.getJsonValueAccessor();
        if (m instanceof AnnotatedMethod) {
            return (AnnotatedMethod)m;
        }
        return null;
    }

    @Deprecated
    public Class<?> findPOJOBuilderClass() {
        return this._annotationIntrospector.findPOJOBuilder(this._classDef);
    }

    protected void collectAll() {
        LinkedHashMap<String, POJOPropertyBuilder> props = new LinkedHashMap<String, POJOPropertyBuilder>();
        this._addFields(props);
        this._addMethods(props);
        if (!this._classDef.isNonStaticInnerClass()) {
            this._addCreators(props);
        }
        this._removeUnwantedProperties(props);
        this._removeUnwantedAccessor(props);
        this._renameProperties(props);
        this._addInjectables(props);
        for (POJOPropertyBuilder property : props.values()) {
            property.mergeAnnotations(this._forSerialization);
        }
        PropertyNamingStrategy naming = this._findNamingStrategy();
        if (naming != null) {
            this._renameUsing(props, naming);
        }
        for (POJOPropertyBuilder property : props.values()) {
            property.trimByVisibility();
        }
        if (this._config.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)) {
            this._renameWithWrappers(props);
        }
        this._sortProperties(props);
        this._properties = props;
        this._collected = true;
    }

    protected void _addFields(Map<String, POJOPropertyBuilder> props) {
        AnnotationIntrospector ai = this._annotationIntrospector;
        boolean pruneFinalFields = !this._forSerialization && !this._config.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
        boolean transientAsIgnoral = this._config.isEnabled(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        for (AnnotatedField f : this._classDef.fields()) {
            boolean visible;
            PropertyName pn;
            boolean hasName;
            boolean nameExplicit;
            PropertyName implNameP;
            PropertyName rename;
            if (Boolean.TRUE.equals(ai.hasAsValue(f))) {
                if (this._jsonValueAccessors == null) {
                    this._jsonValueAccessors = new LinkedList();
                }
                this._jsonValueAccessors.add(f);
                continue;
            }
            if (Boolean.TRUE.equals(ai.hasAnySetter(f))) {
                if (this._anySetterField == null) {
                    this._anySetterField = new LinkedList();
                }
                this._anySetterField.add(f);
                continue;
            }
            String implName = ai.findImplicitPropertyName(f);
            if (implName == null) {
                implName = f.getName();
            }
            if ((rename = ai.findRenameByField(this._config, f, implNameP = this._propNameFromSimple(implName))) != null && !rename.equals(implNameP)) {
                if (this._fieldRenameMappings == null) {
                    this._fieldRenameMappings = new HashMap<PropertyName, PropertyName>();
                }
                this._fieldRenameMappings.put(rename, implNameP);
            }
            if ((nameExplicit = (hasName = (pn = this._forSerialization ? ai.findNameForSerialization(f) : ai.findNameForDeserialization(f)) != null)) && pn.isEmpty()) {
                pn = this._propNameFromSimple(implName);
                nameExplicit = false;
            }
            boolean bl = visible = pn != null;
            if (!visible) {
                visible = this._visibilityChecker.isFieldVisible(f);
            }
            boolean ignored = ai.hasIgnoreMarker(f);
            if (f.isTransient() && !hasName) {
                visible = false;
                if (transientAsIgnoral) {
                    ignored = true;
                }
            }
            if (pruneFinalFields && pn == null && !ignored && Modifier.isFinal(f.getModifiers())) continue;
            this._property(props, implName).addField(f, pn, nameExplicit, visible, ignored);
        }
    }

    protected void _addCreators(Map<String, POJOPropertyBuilder> props) {
        int i;
        int len;
        if (!this._useAnnotations) {
            return;
        }
        for (AnnotatedConstructor ctor : this._classDef.getConstructors()) {
            if (this._creatorProperties == null) {
                this._creatorProperties = new LinkedList();
            }
            len = ctor.getParameterCount();
            for (i = 0; i < len; ++i) {
                this._addCreatorParam(props, ctor.getParameter(i));
            }
        }
        for (AnnotatedMethod factory : this._classDef.getFactoryMethods()) {
            if (this._creatorProperties == null) {
                this._creatorProperties = new LinkedList();
            }
            len = factory.getParameterCount();
            for (i = 0; i < len; ++i) {
                this._addCreatorParam(props, factory.getParameter(i));
            }
        }
    }

    protected void _addCreatorParam(Map<String, POJOPropertyBuilder> props, AnnotatedParameter param) {
        PropertyName pn;
        boolean expl;
        String impl = this._annotationIntrospector.findImplicitPropertyName(param);
        if (impl == null) {
            impl = "";
        }
        boolean bl = expl = (pn = this._annotationIntrospector.findNameForDeserialization(param)) != null && !pn.isEmpty();
        if (!expl) {
            if (impl.isEmpty()) {
                return;
            }
            JsonCreator.Mode creatorMode = this._annotationIntrospector.findCreatorAnnotation(this._config, param.getOwner());
            if (creatorMode == null || creatorMode == JsonCreator.Mode.DISABLED) {
                return;
            }
            pn = PropertyName.construct(impl);
        }
        impl = this._checkRenameByField(impl);
        POJOPropertyBuilder prop = expl && impl.isEmpty() ? this._property(props, pn) : this._property(props, impl);
        prop.addCtor(param, pn, expl, true, false);
        this._creatorProperties.add(prop);
    }

    protected void _addMethods(Map<String, POJOPropertyBuilder> props) {
        AnnotationIntrospector ai = this._annotationIntrospector;
        for (AnnotatedMethod m : this._classDef.memberMethods()) {
            int argCount = m.getParameterCount();
            if (argCount == 0) {
                this._addGetterMethod(props, m, ai);
                continue;
            }
            if (argCount == 1) {
                this._addSetterMethod(props, m, ai);
                continue;
            }
            if (argCount != 2 || ai == null || !Boolean.TRUE.equals(ai.hasAnySetter(m))) continue;
            if (this._anySetters == null) {
                this._anySetters = new LinkedList();
            }
            this._anySetters.add(m);
        }
    }

    protected void _addGetterMethod(Map<String, POJOPropertyBuilder> props, AnnotatedMethod m, AnnotationIntrospector ai) {
        boolean visible;
        String implName;
        boolean nameExplicit;
        if (!m.hasReturnType()) {
            return;
        }
        if (Boolean.TRUE.equals(ai.hasAnyGetter(m))) {
            if (this._anyGetters == null) {
                this._anyGetters = new LinkedList();
            }
            this._anyGetters.add(m);
            return;
        }
        if (Boolean.TRUE.equals(ai.hasAsValue(m))) {
            if (this._jsonValueAccessors == null) {
                this._jsonValueAccessors = new LinkedList();
            }
            this._jsonValueAccessors.add(m);
            return;
        }
        PropertyName pn = ai.findNameForSerialization(m);
        boolean bl = nameExplicit = pn != null;
        if (!nameExplicit) {
            implName = ai.findImplicitPropertyName(m);
            if (implName == null) {
                implName = BeanUtil.okNameForRegularGetter(m, m.getName(), this._stdBeanNaming);
            }
            if (implName == null) {
                implName = BeanUtil.okNameForIsGetter(m, m.getName(), this._stdBeanNaming);
                if (implName == null) {
                    return;
                }
                visible = this._visibilityChecker.isIsGetterVisible(m);
            } else {
                visible = this._visibilityChecker.isGetterVisible(m);
            }
        } else {
            implName = ai.findImplicitPropertyName(m);
            if (implName == null) {
                implName = BeanUtil.okNameForGetter(m, this._stdBeanNaming);
            }
            if (implName == null) {
                implName = m.getName();
            }
            if (pn.isEmpty()) {
                pn = this._propNameFromSimple(implName);
                nameExplicit = false;
            }
            visible = true;
        }
        implName = this._checkRenameByField(implName);
        boolean ignore = ai.hasIgnoreMarker(m);
        this._property(props, implName).addGetter(m, pn, nameExplicit, visible, ignore);
    }

    protected void _addSetterMethod(Map<String, POJOPropertyBuilder> props, AnnotatedMethod m, AnnotationIntrospector ai) {
        boolean visible;
        String implName;
        boolean nameExplicit;
        PropertyName pn = ai == null ? null : ai.findNameForDeserialization(m);
        boolean bl = nameExplicit = pn != null;
        if (!nameExplicit) {
            String string = implName = ai == null ? null : ai.findImplicitPropertyName(m);
            if (implName == null) {
                implName = BeanUtil.okNameForMutator(m, this._mutatorPrefix, this._stdBeanNaming);
            }
            if (implName == null) {
                return;
            }
            visible = this._visibilityChecker.isSetterVisible(m);
        } else {
            String string = implName = ai == null ? null : ai.findImplicitPropertyName(m);
            if (implName == null) {
                implName = BeanUtil.okNameForMutator(m, this._mutatorPrefix, this._stdBeanNaming);
            }
            if (implName == null) {
                implName = m.getName();
            }
            if (pn.isEmpty()) {
                pn = this._propNameFromSimple(implName);
                nameExplicit = false;
            }
            visible = true;
        }
        implName = this._checkRenameByField(implName);
        boolean ignore = ai == null ? false : ai.hasIgnoreMarker(m);
        this._property(props, implName).addSetter(m, pn, nameExplicit, visible, ignore);
    }

    protected void _addInjectables(Map<String, POJOPropertyBuilder> props) {
        AnnotationIntrospector ai = this._annotationIntrospector;
        for (AnnotatedField f : this._classDef.fields()) {
            this._doAddInjectable(ai.findInjectableValue(f), f);
        }
        for (AnnotatedMethod m : this._classDef.memberMethods()) {
            if (m.getParameterCount() != 1) continue;
            this._doAddInjectable(ai.findInjectableValue(m), m);
        }
    }

    protected void _doAddInjectable(JacksonInject.Value injectable, AnnotatedMember m) {
        AnnotatedMember prev;
        if (injectable == null) {
            return;
        }
        Object id = injectable.getId();
        if (this._injectables == null) {
            this._injectables = new LinkedHashMap();
        }
        if ((prev = this._injectables.put(id, m)) != null && prev.getClass() == m.getClass()) {
            String type = id.getClass().getName();
            throw new IllegalArgumentException("Duplicate injectable value with id '" + String.valueOf(id) + "' (of type " + type + ")");
        }
    }

    private PropertyName _propNameFromSimple(String simpleName) {
        return PropertyName.construct(simpleName, null);
    }

    private String _checkRenameByField(String implName) {
        PropertyName p;
        if (this._fieldRenameMappings != null && (p = this._fieldRenameMappings.get(this._propNameFromSimple(implName))) != null) {
            implName = p.getSimpleName();
            return implName;
        }
        return implName;
    }

    protected void _removeUnwantedProperties(Map<String, POJOPropertyBuilder> props) {
        Iterator<POJOPropertyBuilder> it = props.values().iterator();
        while (it.hasNext()) {
            POJOPropertyBuilder prop = it.next();
            if (!prop.anyVisible()) {
                it.remove();
                continue;
            }
            if (!prop.anyIgnorals()) continue;
            if (!prop.isExplicitlyIncluded()) {
                it.remove();
                this._collectIgnorals(prop.getName());
                continue;
            }
            prop.removeIgnored();
            if (prop.couldDeserialize()) continue;
            this._collectIgnorals(prop.getName());
        }
    }

    protected void _removeUnwantedAccessor(Map<String, POJOPropertyBuilder> props) {
        boolean inferMutators = this._config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS);
        for (POJOPropertyBuilder prop : props.values()) {
            JsonProperty.Access acc = prop.removeNonVisible(inferMutators);
            if (acc != JsonProperty.Access.READ_ONLY) continue;
            this._collectIgnorals(prop.getName());
        }
    }

    private void _collectIgnorals(String name) {
        if (!this._forSerialization) {
            if (this._ignoredPropertyNames == null) {
                this._ignoredPropertyNames = new HashSet();
            }
            this._ignoredPropertyNames.add(name);
        }
    }

    protected void _renameProperties(Map<String, POJOPropertyBuilder> props) {
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = props.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> renamed = null;
        while (it.hasNext()) {
            Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();
            Set<PropertyName> l = prop.findExplicitNames();
            if (l.isEmpty()) continue;
            it.remove();
            if (renamed == null) {
                renamed = new LinkedList<POJOPropertyBuilder>();
            }
            if (l.size() == 1) {
                PropertyName n = (PropertyName)l.iterator().next();
                renamed.add(prop.withName(n));
                continue;
            }
            renamed.addAll(prop.explode(l));
        }
        if (renamed != null) {
            for (POJOPropertyBuilder prop : renamed) {
                String name = prop.getName();
                POJOPropertyBuilder old = props.get(name);
                if (old == null) {
                    props.put(name, prop);
                } else {
                    old.addAll(prop);
                }
                this._updateCreatorProperty(prop, this._creatorProperties);
                if (this._ignoredPropertyNames == null) continue;
                this._ignoredPropertyNames.remove(name);
            }
        }
    }

    protected void _renameUsing(Map<String, POJOPropertyBuilder> propMap, PropertyNamingStrategy naming) {
        POJOPropertyBuilder[] props = propMap.values().toArray(new POJOPropertyBuilder[propMap.size()]);
        propMap.clear();
        for (POJOPropertyBuilder prop : props) {
            String simpleName;
            PropertyName fullName = prop.getFullName();
            String rename = null;
            if (!prop.isExplicitlyNamed() || this._config.isEnabled(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)) {
                if (this._forSerialization) {
                    if (prop.hasGetter()) {
                        rename = naming.nameForGetterMethod(this._config, prop.getGetter(), fullName.getSimpleName());
                    } else if (prop.hasField()) {
                        rename = naming.nameForField(this._config, prop.getField(), fullName.getSimpleName());
                    }
                } else if (prop.hasSetter()) {
                    rename = naming.nameForSetterMethod(this._config, prop.getSetter(), fullName.getSimpleName());
                } else if (prop.hasConstructorParameter()) {
                    rename = naming.nameForConstructorParameter(this._config, prop.getConstructorParameter(), fullName.getSimpleName());
                } else if (prop.hasField()) {
                    rename = naming.nameForField(this._config, prop.getField(), fullName.getSimpleName());
                } else if (prop.hasGetter()) {
                    rename = naming.nameForGetterMethod(this._config, prop.getGetter(), fullName.getSimpleName());
                }
            }
            if (rename != null && !fullName.hasSimpleName(rename)) {
                prop = prop.withSimpleName(rename);
                simpleName = rename;
            } else {
                simpleName = fullName.getSimpleName();
            }
            POJOPropertyBuilder old = propMap.get(simpleName);
            if (old == null) {
                propMap.put(simpleName, prop);
            } else {
                old.addAll(prop);
            }
            this._updateCreatorProperty(prop, this._creatorProperties);
        }
    }

    protected void _renameWithWrappers(Map<String, POJOPropertyBuilder> props) {
        Iterator<Map.Entry<String, POJOPropertyBuilder>> it = props.entrySet().iterator();
        LinkedList<POJOPropertyBuilder> renamed = null;
        while (it.hasNext()) {
            PropertyName wrapperName;
            Map.Entry<String, POJOPropertyBuilder> entry = it.next();
            POJOPropertyBuilder prop = entry.getValue();
            AnnotatedMember member = prop.getPrimaryMember();
            if (member == null || (wrapperName = this._annotationIntrospector.findWrapperName(member)) == null || !wrapperName.hasSimpleName() || wrapperName.equals(prop.getFullName())) continue;
            if (renamed == null) {
                renamed = new LinkedList<POJOPropertyBuilder>();
            }
            prop = prop.withName(wrapperName);
            renamed.add(prop);
            it.remove();
        }
        if (renamed != null) {
            for (POJOPropertyBuilder prop : renamed) {
                String name = prop.getName();
                POJOPropertyBuilder old = props.get(name);
                if (old == null) {
                    props.put(name, prop);
                    continue;
                }
                old.addAll(prop);
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    protected void _sortProperties(Map<String, POJOPropertyBuilder> props) {
        AnnotationIntrospector intr = this._annotationIntrospector;
        Boolean alpha = intr.findSerializationSortAlphabetically(this._classDef);
        boolean sort = alpha == null ? this._config.shouldSortPropertiesAlphabetically() : alpha.booleanValue();
        boolean indexed = this._anyIndexed(props.values());
        String[] propertyOrder = intr.findSerializationPropertyOrder(this._classDef);
        if (!sort && !indexed && this._creatorProperties == null && propertyOrder == null) {
            return;
        }
        int size = props.size();
        AbstractMap all = sort ? new TreeMap() : new LinkedHashMap(size + size);
        for (POJOPropertyBuilder pOJOPropertyBuilder : props.values()) {
            all.put(pOJOPropertyBuilder.getName(), pOJOPropertyBuilder);
        }
        LinkedHashMap<String, POJOPropertyBuilder> ordered = new LinkedHashMap<String, POJOPropertyBuilder>(size + size);
        if (propertyOrder != null) {
            for (String name : propertyOrder) {
                POJOPropertyBuilder w = (POJOPropertyBuilder)all.remove(name);
                if (w == null) {
                    for (POJOPropertyBuilder prop : props.values()) {
                        if (!name.equals(prop.getInternalName())) continue;
                        w = prop;
                        name = prop.getName();
                        break;
                    }
                }
                if (w == null) continue;
                ordered.put(name, w);
            }
        }
        if (indexed) {
            TreeMap<Integer, POJOPropertyBuilder> treeMap = new TreeMap<Integer, POJOPropertyBuilder>();
            Iterator it = all.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                POJOPropertyBuilder prop = (POJOPropertyBuilder)entry.getValue();
                Integer index = prop.getMetadata().getIndex();
                if (index == null) continue;
                treeMap.put(index, prop);
                it.remove();
            }
            for (POJOPropertyBuilder prop : treeMap.values()) {
                ordered.put(prop.getName(), prop);
            }
        }
        if (this._creatorProperties != null) {
            void var10_17;
            if (sort) {
                TreeMap<String, POJOPropertyBuilder> sorted = new TreeMap<String, POJOPropertyBuilder>();
                for (POJOPropertyBuilder prop : this._creatorProperties) {
                    sorted.put(prop.getName(), prop);
                }
                Collection collection = sorted.values();
            } else {
                LinkedList<POJOPropertyBuilder> linkedList = this._creatorProperties;
            }
            for (POJOPropertyBuilder prop : var10_17) {
                String name;
                name = prop.getName();
                if (!all.containsKey(name)) continue;
                ordered.put(name, prop);
            }
        }
        ordered.putAll(all);
        props.clear();
        props.putAll(ordered);
    }

    private boolean _anyIndexed(Collection<POJOPropertyBuilder> props) {
        for (POJOPropertyBuilder prop : props) {
            if (!prop.getMetadata().hasIndex()) continue;
            return true;
        }
        return false;
    }

    protected void reportProblem(String msg, Object ... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        throw new IllegalArgumentException("Problem with definition of " + this._classDef + ": " + msg);
    }

    protected POJOPropertyBuilder _property(Map<String, POJOPropertyBuilder> props, PropertyName name) {
        String simpleName = name.getSimpleName();
        POJOPropertyBuilder prop = props.get(simpleName);
        if (prop == null) {
            prop = new POJOPropertyBuilder(this._config, this._annotationIntrospector, this._forSerialization, name);
            props.put(simpleName, prop);
        }
        return prop;
    }

    protected POJOPropertyBuilder _property(Map<String, POJOPropertyBuilder> props, String implName) {
        POJOPropertyBuilder prop = props.get(implName);
        if (prop == null) {
            prop = new POJOPropertyBuilder(this._config, this._annotationIntrospector, this._forSerialization, PropertyName.construct(implName));
            props.put(implName, prop);
        }
        return prop;
    }

    private PropertyNamingStrategy _findNamingStrategy() {
        PropertyNamingStrategy pns;
        Object namingDef = this._annotationIntrospector.findNamingStrategy(this._classDef);
        if (namingDef == null) {
            return this._config.getPropertyNamingStrategy();
        }
        if (namingDef instanceof PropertyNamingStrategy) {
            return (PropertyNamingStrategy)namingDef;
        }
        if (!(namingDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned PropertyNamingStrategy definition of type " + namingDef.getClass().getName() + "; expected type PropertyNamingStrategy or Class<PropertyNamingStrategy> instead");
        }
        Class namingClass = (Class)namingDef;
        if (namingClass == PropertyNamingStrategy.class) {
            return null;
        }
        if (!PropertyNamingStrategy.class.isAssignableFrom(namingClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + namingClass.getName() + "; expected Class<PropertyNamingStrategy>");
        }
        HandlerInstantiator hi = this._config.getHandlerInstantiator();
        if (hi != null && (pns = hi.namingStrategyInstance(this._config, this._classDef, namingClass)) != null) {
            return pns;
        }
        return (PropertyNamingStrategy)ClassUtil.createInstance(namingClass, this._config.canOverrideAccessModifiers());
    }

    protected void _updateCreatorProperty(POJOPropertyBuilder prop, List<POJOPropertyBuilder> creatorProperties) {
        if (creatorProperties != null) {
            String intName = prop.getInternalName();
            int len = creatorProperties.size();
            for (int i = 0; i < len; ++i) {
                if (!creatorProperties.get(i).getInternalName().equals(intName)) continue;
                creatorProperties.set(i, prop);
                break;
            }
        }
    }
}

