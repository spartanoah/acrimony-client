/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class POJOPropertyBuilder
extends BeanPropertyDefinition
implements Comparable<POJOPropertyBuilder> {
    private static final AnnotationIntrospector.ReferenceProperty NOT_REFEFERENCE_PROP = AnnotationIntrospector.ReferenceProperty.managed("");
    protected final boolean _forSerialization;
    protected final MapperConfig<?> _config;
    protected final AnnotationIntrospector _annotationIntrospector;
    protected final PropertyName _name;
    protected final PropertyName _internalName;
    protected Linked<AnnotatedField> _fields;
    protected Linked<AnnotatedParameter> _ctorParameters;
    protected Linked<AnnotatedMethod> _getters;
    protected Linked<AnnotatedMethod> _setters;
    protected transient PropertyMetadata _metadata;
    protected transient AnnotationIntrospector.ReferenceProperty _referenceInfo;

    public POJOPropertyBuilder(MapperConfig<?> config, AnnotationIntrospector ai, boolean forSerialization, PropertyName internalName) {
        this(config, ai, forSerialization, internalName, internalName);
    }

    protected POJOPropertyBuilder(MapperConfig<?> config, AnnotationIntrospector ai, boolean forSerialization, PropertyName internalName, PropertyName name) {
        this._config = config;
        this._annotationIntrospector = ai;
        this._internalName = internalName;
        this._name = name;
        this._forSerialization = forSerialization;
    }

    protected POJOPropertyBuilder(POJOPropertyBuilder src, PropertyName newName) {
        this._config = src._config;
        this._annotationIntrospector = src._annotationIntrospector;
        this._internalName = src._internalName;
        this._name = newName;
        this._fields = src._fields;
        this._ctorParameters = src._ctorParameters;
        this._getters = src._getters;
        this._setters = src._setters;
        this._forSerialization = src._forSerialization;
    }

    @Override
    public POJOPropertyBuilder withName(PropertyName newName) {
        return new POJOPropertyBuilder(this, newName);
    }

    @Override
    public POJOPropertyBuilder withSimpleName(String newSimpleName) {
        PropertyName newName = this._name.withSimpleName(newSimpleName);
        return newName == this._name ? this : new POJOPropertyBuilder(this, newName);
    }

    @Override
    public int compareTo(POJOPropertyBuilder other) {
        if (this._ctorParameters != null) {
            if (other._ctorParameters == null) {
                return -1;
            }
        } else if (other._ctorParameters != null) {
            return 1;
        }
        return this.getName().compareTo(other.getName());
    }

    @Override
    public String getName() {
        return this._name == null ? null : this._name.getSimpleName();
    }

    @Override
    public PropertyName getFullName() {
        return this._name;
    }

    @Override
    public boolean hasName(PropertyName name) {
        return this._name.equals(name);
    }

    @Override
    public String getInternalName() {
        return this._internalName.getSimpleName();
    }

    @Override
    public PropertyName getWrapperName() {
        AnnotatedMember member = this.getPrimaryMember();
        return member == null || this._annotationIntrospector == null ? null : this._annotationIntrospector.findWrapperName(member);
    }

    @Override
    public boolean isExplicitlyIncluded() {
        return this._anyExplicits(this._fields) || this._anyExplicits(this._getters) || this._anyExplicits(this._setters) || this._anyExplicitNames(this._ctorParameters);
    }

    @Override
    public boolean isExplicitlyNamed() {
        return this._anyExplicitNames(this._fields) || this._anyExplicitNames(this._getters) || this._anyExplicitNames(this._setters) || this._anyExplicitNames(this._ctorParameters);
    }

    @Override
    public PropertyMetadata getMetadata() {
        if (this._metadata == null) {
            Boolean b = this._findRequired();
            String desc = this._findDescription();
            Integer idx = this._findIndex();
            String def = this._findDefaultValue();
            this._metadata = b == null && idx == null && def == null ? (desc == null ? PropertyMetadata.STD_REQUIRED_OR_OPTIONAL : PropertyMetadata.STD_REQUIRED_OR_OPTIONAL.withDescription(desc)) : PropertyMetadata.construct(b, desc, idx, def);
            if (!this._forSerialization) {
                this._metadata = this._getSetterInfo(this._metadata);
            }
        }
        return this._metadata;
    }

    protected PropertyMetadata _getSetterInfo(PropertyMetadata metadata) {
        JsonSetter.Value setterInfo;
        boolean needMerge = true;
        Nulls valueNulls = null;
        Nulls contentNulls = null;
        AnnotatedMember prim = this.getPrimaryMemberUnchecked();
        AnnotatedMember acc = this.getAccessor();
        if (prim != null) {
            if (this._annotationIntrospector != null) {
                Boolean b;
                if (acc != null && (b = this._annotationIntrospector.findMergeInfo(prim)) != null) {
                    needMerge = false;
                    if (b.booleanValue()) {
                        metadata = metadata.withMergeInfo(PropertyMetadata.MergeInfo.createForPropertyOverride(acc));
                    }
                }
                if ((setterInfo = this._annotationIntrospector.findSetterInfo(prim)) != null) {
                    valueNulls = setterInfo.nonDefaultValueNulls();
                    contentNulls = setterInfo.nonDefaultContentNulls();
                }
            }
            if (needMerge || valueNulls == null || contentNulls == null) {
                Boolean b;
                Class<?> rawType = this._rawTypeOf(prim);
                ConfigOverride co = this._config.getConfigOverride(rawType);
                JsonSetter.Value setterInfo2 = co.getSetterInfo();
                if (setterInfo2 != null) {
                    if (valueNulls == null) {
                        valueNulls = setterInfo2.nonDefaultValueNulls();
                    }
                    if (contentNulls == null) {
                        contentNulls = setterInfo2.nonDefaultContentNulls();
                    }
                }
                if (needMerge && acc != null && (b = co.getMergeable()) != null) {
                    needMerge = false;
                    if (b.booleanValue()) {
                        metadata = metadata.withMergeInfo(PropertyMetadata.MergeInfo.createForTypeOverride(acc));
                    }
                }
            }
        }
        if (needMerge || valueNulls == null || contentNulls == null) {
            Boolean b;
            setterInfo = this._config.getDefaultSetterInfo();
            if (valueNulls == null) {
                valueNulls = setterInfo.nonDefaultValueNulls();
            }
            if (contentNulls == null) {
                contentNulls = setterInfo.nonDefaultContentNulls();
            }
            if (needMerge && Boolean.TRUE.equals(b = this._config.getDefaultMergeable()) && acc != null) {
                metadata = metadata.withMergeInfo(PropertyMetadata.MergeInfo.createForDefaults(acc));
            }
        }
        if (valueNulls != null || contentNulls != null) {
            metadata = metadata.withNulls(valueNulls, contentNulls);
        }
        return metadata;
    }

    @Override
    public JavaType getPrimaryType() {
        if (this._forSerialization) {
            AnnotatedMember m = this.getGetter();
            if (m == null && (m = this.getField()) == null) {
                return TypeFactory.unknownType();
            }
            return ((Annotated)m).getType();
        }
        AnnotatedMember m = this.getConstructorParameter();
        if (m == null) {
            m = this.getSetter();
            if (m != null) {
                return ((AnnotatedMethod)m).getParameterType(0);
            }
            m = this.getField();
        }
        if (m == null && (m = this.getGetter()) == null) {
            return TypeFactory.unknownType();
        }
        return m.getType();
    }

    @Override
    public Class<?> getRawPrimaryType() {
        return this.getPrimaryType().getRawClass();
    }

    @Override
    public boolean hasGetter() {
        return this._getters != null;
    }

    @Override
    public boolean hasSetter() {
        return this._setters != null;
    }

    @Override
    public boolean hasField() {
        return this._fields != null;
    }

    @Override
    public boolean hasConstructorParameter() {
        return this._ctorParameters != null;
    }

    @Override
    public boolean couldDeserialize() {
        return this._ctorParameters != null || this._setters != null || this._fields != null;
    }

    @Override
    public boolean couldSerialize() {
        return this._getters != null || this._fields != null;
    }

    @Override
    public AnnotatedMethod getGetter() {
        Linked<AnnotatedMethod> curr = this._getters;
        if (curr == null) {
            return null;
        }
        Linked next = curr.next;
        if (next == null) {
            return (AnnotatedMethod)curr.value;
        }
        while (next != null) {
            block11: {
                int priCurr;
                int priNext;
                block9: {
                    Class<?> nextClass;
                    Class<?> currClass;
                    block10: {
                        currClass = ((AnnotatedMethod)curr.value).getDeclaringClass();
                        if (currClass == (nextClass = ((AnnotatedMethod)next.value).getDeclaringClass())) break block9;
                        if (!currClass.isAssignableFrom(nextClass)) break block10;
                        curr = next;
                        break block11;
                    }
                    if (nextClass.isAssignableFrom(currClass)) break block11;
                }
                if ((priNext = this._getterPriority((AnnotatedMethod)next.value)) != (priCurr = this._getterPriority((AnnotatedMethod)curr.value))) {
                    if (priNext < priCurr) {
                        curr = next;
                    }
                } else {
                    throw new IllegalArgumentException("Conflicting getter definitions for property \"" + this.getName() + "\": " + ((AnnotatedMethod)curr.value).getFullName() + " vs " + ((AnnotatedMethod)next.value).getFullName());
                }
            }
            next = next.next;
        }
        this._getters = curr.withoutNext();
        return (AnnotatedMethod)curr.value;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public AnnotatedMethod getSetter() {
        Linked<AnnotatedMethod> curr = this._setters;
        if (curr == null) {
            return null;
        }
        Linked next = curr.next;
        if (next == null) {
            return (AnnotatedMethod)curr.value;
        }
        while (next != null) {
            block12: {
                int priCurr;
                block10: {
                    Class<?> nextClass;
                    Class<?> currClass;
                    block11: {
                        currClass = ((AnnotatedMethod)curr.value).getDeclaringClass();
                        if (currClass == (nextClass = ((AnnotatedMethod)next.value).getDeclaringClass())) break block10;
                        if (!currClass.isAssignableFrom(nextClass)) break block11;
                        curr = next;
                        break block12;
                    }
                    if (nextClass.isAssignableFrom(currClass)) break block12;
                }
                AnnotatedMethod nextM = (AnnotatedMethod)next.value;
                AnnotatedMethod currM = (AnnotatedMethod)curr.value;
                int priNext = this._setterPriority(nextM);
                if (priNext != (priCurr = this._setterPriority(currM))) {
                    if (priNext < priCurr) {
                        curr = next;
                    }
                } else {
                    if (this._annotationIntrospector == null) throw new IllegalArgumentException(String.format("Conflicting setter definitions for property \"%s\": %s vs %s", this.getName(), ((AnnotatedMethod)curr.value).getFullName(), ((AnnotatedMethod)next.value).getFullName()));
                    AnnotatedMethod pref = this._annotationIntrospector.resolveSetterConflict(this._config, currM, nextM);
                    if (pref != currM) {
                        if (pref != nextM) throw new IllegalArgumentException(String.format("Conflicting setter definitions for property \"%s\": %s vs %s", this.getName(), ((AnnotatedMethod)curr.value).getFullName(), ((AnnotatedMethod)next.value).getFullName()));
                        curr = next;
                    }
                }
            }
            next = next.next;
        }
        this._setters = curr.withoutNext();
        return (AnnotatedMethod)curr.value;
    }

    @Override
    public AnnotatedField getField() {
        if (this._fields == null) {
            return null;
        }
        AnnotatedField field = (AnnotatedField)this._fields.value;
        Linked next = this._fields.next;
        while (next != null) {
            block7: {
                AnnotatedField nextField;
                block5: {
                    Class<?> nextClass;
                    Class<?> fieldClass;
                    block6: {
                        nextField = (AnnotatedField)next.value;
                        fieldClass = field.getDeclaringClass();
                        if (fieldClass == (nextClass = nextField.getDeclaringClass())) break block5;
                        if (!fieldClass.isAssignableFrom(nextClass)) break block6;
                        field = nextField;
                        break block7;
                    }
                    if (nextClass.isAssignableFrom(fieldClass)) break block7;
                }
                throw new IllegalArgumentException("Multiple fields representing property \"" + this.getName() + "\": " + field.getFullName() + " vs " + nextField.getFullName());
            }
            next = next.next;
        }
        return field;
    }

    @Override
    public AnnotatedParameter getConstructorParameter() {
        if (this._ctorParameters == null) {
            return null;
        }
        Linked<AnnotatedParameter> curr = this._ctorParameters;
        do {
            if (!(((AnnotatedParameter)curr.value).getOwner() instanceof AnnotatedConstructor)) continue;
            return (AnnotatedParameter)curr.value;
        } while ((curr = curr.next) != null);
        return (AnnotatedParameter)this._ctorParameters.value;
    }

    @Override
    public Iterator<AnnotatedParameter> getConstructorParameters() {
        if (this._ctorParameters == null) {
            return ClassUtil.emptyIterator();
        }
        return new MemberIterator<AnnotatedParameter>(this._ctorParameters);
    }

    @Override
    public AnnotatedMember getPrimaryMember() {
        if (this._forSerialization) {
            return this.getAccessor();
        }
        AnnotatedMember m = this.getMutator();
        if (m == null) {
            m = this.getAccessor();
        }
        return m;
    }

    protected AnnotatedMember getPrimaryMemberUnchecked() {
        if (this._forSerialization) {
            if (this._getters != null) {
                return (AnnotatedMember)this._getters.value;
            }
            if (this._fields != null) {
                return (AnnotatedMember)this._fields.value;
            }
            return null;
        }
        if (this._ctorParameters != null) {
            return (AnnotatedMember)this._ctorParameters.value;
        }
        if (this._setters != null) {
            return (AnnotatedMember)this._setters.value;
        }
        if (this._fields != null) {
            return (AnnotatedMember)this._fields.value;
        }
        if (this._getters != null) {
            return (AnnotatedMember)this._getters.value;
        }
        return null;
    }

    protected int _getterPriority(AnnotatedMethod m) {
        String name = m.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return 1;
        }
        if (name.startsWith("is") && name.length() > 2) {
            return 2;
        }
        return 3;
    }

    protected int _setterPriority(AnnotatedMethod m) {
        String name = m.getName();
        if (name.startsWith("set") && name.length() > 3) {
            return 1;
        }
        return 2;
    }

    @Override
    public Class<?>[] findViews() {
        return this.fromMemberAnnotations(new WithMember<Class<?>[]>(){

            @Override
            public Class<?>[] withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findViews(member);
            }
        });
    }

    @Override
    public AnnotationIntrospector.ReferenceProperty findReferenceType() {
        AnnotationIntrospector.ReferenceProperty result = this._referenceInfo;
        if (result != null) {
            if (result == NOT_REFEFERENCE_PROP) {
                return null;
            }
            return result;
        }
        result = this.fromMemberAnnotations(new WithMember<AnnotationIntrospector.ReferenceProperty>(){

            @Override
            public AnnotationIntrospector.ReferenceProperty withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findReferenceType(member);
            }
        });
        this._referenceInfo = result == null ? NOT_REFEFERENCE_PROP : result;
        return result;
    }

    @Override
    public boolean isTypeId() {
        Boolean b = this.fromMemberAnnotations(new WithMember<Boolean>(){

            @Override
            public Boolean withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.isTypeId(member);
            }
        });
        return b != null && b != false;
    }

    protected Boolean _findRequired() {
        return this.fromMemberAnnotations(new WithMember<Boolean>(){

            @Override
            public Boolean withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.hasRequiredMarker(member);
            }
        });
    }

    protected String _findDescription() {
        return this.fromMemberAnnotations(new WithMember<String>(){

            @Override
            public String withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findPropertyDescription(member);
            }
        });
    }

    protected Integer _findIndex() {
        return this.fromMemberAnnotations(new WithMember<Integer>(){

            @Override
            public Integer withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findPropertyIndex(member);
            }
        });
    }

    protected String _findDefaultValue() {
        return this.fromMemberAnnotations(new WithMember<String>(){

            @Override
            public String withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findPropertyDefaultValue(member);
            }
        });
    }

    @Override
    public ObjectIdInfo findObjectIdInfo() {
        return this.fromMemberAnnotations(new WithMember<ObjectIdInfo>(){

            @Override
            public ObjectIdInfo withMember(AnnotatedMember member) {
                ObjectIdInfo info = POJOPropertyBuilder.this._annotationIntrospector.findObjectIdInfo(member);
                if (info != null) {
                    info = POJOPropertyBuilder.this._annotationIntrospector.findObjectReferenceInfo(member, info);
                }
                return info;
            }
        });
    }

    @Override
    public JsonInclude.Value findInclusion() {
        AnnotatedMember a = this.getAccessor();
        JsonInclude.Value v = this._annotationIntrospector == null ? null : this._annotationIntrospector.findPropertyInclusion(a);
        return v == null ? JsonInclude.Value.empty() : v;
    }

    public JsonProperty.Access findAccess() {
        return this.fromMemberAnnotationsExcept(new WithMember<JsonProperty.Access>(){

            @Override
            public JsonProperty.Access withMember(AnnotatedMember member) {
                return POJOPropertyBuilder.this._annotationIntrospector.findPropertyAccess(member);
            }
        }, JsonProperty.Access.AUTO);
    }

    public void addField(AnnotatedField a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        this._fields = new Linked<AnnotatedField>(a, this._fields, name, explName, visible, ignored);
    }

    public void addCtor(AnnotatedParameter a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        this._ctorParameters = new Linked<AnnotatedParameter>(a, this._ctorParameters, name, explName, visible, ignored);
    }

    public void addGetter(AnnotatedMethod a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        this._getters = new Linked<AnnotatedMethod>(a, this._getters, name, explName, visible, ignored);
    }

    public void addSetter(AnnotatedMethod a, PropertyName name, boolean explName, boolean visible, boolean ignored) {
        this._setters = new Linked<AnnotatedMethod>(a, this._setters, name, explName, visible, ignored);
    }

    public void addAll(POJOPropertyBuilder src) {
        this._fields = POJOPropertyBuilder.merge(this._fields, src._fields);
        this._ctorParameters = POJOPropertyBuilder.merge(this._ctorParameters, src._ctorParameters);
        this._getters = POJOPropertyBuilder.merge(this._getters, src._getters);
        this._setters = POJOPropertyBuilder.merge(this._setters, src._setters);
    }

    private static <T> Linked<T> merge(Linked<T> chain1, Linked<T> chain2) {
        if (chain1 == null) {
            return chain2;
        }
        if (chain2 == null) {
            return chain1;
        }
        return chain1.append(chain2);
    }

    public void removeIgnored() {
        this._fields = this._removeIgnored(this._fields);
        this._getters = this._removeIgnored(this._getters);
        this._setters = this._removeIgnored(this._setters);
        this._ctorParameters = this._removeIgnored(this._ctorParameters);
    }

    public JsonProperty.Access removeNonVisible(boolean inferMutators) {
        JsonProperty.Access acc = this.findAccess();
        if (acc == null) {
            acc = JsonProperty.Access.AUTO;
        }
        switch (acc) {
            case READ_ONLY: {
                this._setters = null;
                this._ctorParameters = null;
                if (this._forSerialization) break;
                this._fields = null;
                break;
            }
            case READ_WRITE: {
                break;
            }
            case WRITE_ONLY: {
                this._getters = null;
                if (!this._forSerialization) break;
                this._fields = null;
                break;
            }
            default: {
                this._getters = this._removeNonVisible(this._getters);
                this._ctorParameters = this._removeNonVisible(this._ctorParameters);
                if (inferMutators && this._getters != null) break;
                this._fields = this._removeNonVisible(this._fields);
                this._setters = this._removeNonVisible(this._setters);
            }
        }
        return acc;
    }

    public void removeConstructors() {
        this._ctorParameters = null;
    }

    public void trimByVisibility() {
        this._fields = this._trimByVisibility(this._fields);
        this._getters = this._trimByVisibility(this._getters);
        this._setters = this._trimByVisibility(this._setters);
        this._ctorParameters = this._trimByVisibility(this._ctorParameters);
    }

    public void mergeAnnotations(boolean forSerialization) {
        if (forSerialization) {
            if (this._getters != null) {
                AnnotationMap ann = this._mergeAnnotations(0, this._getters, this._fields, this._ctorParameters, this._setters);
                this._getters = this._applyAnnotations(this._getters, ann);
            } else if (this._fields != null) {
                AnnotationMap ann = this._mergeAnnotations(0, this._fields, this._ctorParameters, this._setters);
                this._fields = this._applyAnnotations(this._fields, ann);
            }
        } else if (this._ctorParameters != null) {
            AnnotationMap ann = this._mergeAnnotations(0, this._ctorParameters, this._setters, this._fields, this._getters);
            this._ctorParameters = this._applyAnnotations(this._ctorParameters, ann);
        } else if (this._setters != null) {
            AnnotationMap ann = this._mergeAnnotations(0, this._setters, this._fields, this._getters);
            this._setters = this._applyAnnotations(this._setters, ann);
        } else if (this._fields != null) {
            AnnotationMap ann = this._mergeAnnotations(0, this._fields, this._getters);
            this._fields = this._applyAnnotations(this._fields, ann);
        }
    }

    private AnnotationMap _mergeAnnotations(int index, Linked<? extends AnnotatedMember> ... nodes) {
        AnnotationMap ann = this._getAllAnnotations(nodes[index]);
        while (++index < nodes.length) {
            if (nodes[index] == null) continue;
            return AnnotationMap.merge(ann, this._mergeAnnotations(index, nodes));
        }
        return ann;
    }

    private <T extends AnnotatedMember> AnnotationMap _getAllAnnotations(Linked<T> node) {
        AnnotationMap ann = ((AnnotatedMember)node.value).getAllAnnotations();
        if (node.next != null) {
            ann = AnnotationMap.merge(ann, this._getAllAnnotations(node.next));
        }
        return ann;
    }

    private <T extends AnnotatedMember> Linked<T> _applyAnnotations(Linked<T> node, AnnotationMap ann) {
        AnnotatedMember value = (AnnotatedMember)((AnnotatedMember)node.value).withAnnotations(ann);
        if (node.next != null) {
            node = node.withNext(this._applyAnnotations(node.next, ann));
        }
        return node.withValue(value);
    }

    private <T> Linked<T> _removeIgnored(Linked<T> node) {
        if (node == null) {
            return node;
        }
        return node.withoutIgnored();
    }

    private <T> Linked<T> _removeNonVisible(Linked<T> node) {
        if (node == null) {
            return node;
        }
        return node.withoutNonVisible();
    }

    private <T> Linked<T> _trimByVisibility(Linked<T> node) {
        if (node == null) {
            return node;
        }
        return node.trimByVisibility();
    }

    private <T> boolean _anyExplicits(Linked<T> n) {
        while (n != null) {
            if (n.name != null && n.name.hasSimpleName()) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    private <T> boolean _anyExplicitNames(Linked<T> n) {
        while (n != null) {
            if (n.name != null && n.isNameExplicit) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    public boolean anyVisible() {
        return this._anyVisible(this._fields) || this._anyVisible(this._getters) || this._anyVisible(this._setters) || this._anyVisible(this._ctorParameters);
    }

    private <T> boolean _anyVisible(Linked<T> n) {
        while (n != null) {
            if (n.isVisible) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    public boolean anyIgnorals() {
        return this._anyIgnorals(this._fields) || this._anyIgnorals(this._getters) || this._anyIgnorals(this._setters) || this._anyIgnorals(this._ctorParameters);
    }

    private <T> boolean _anyIgnorals(Linked<T> n) {
        while (n != null) {
            if (n.isMarkedIgnored) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    public Set<PropertyName> findExplicitNames() {
        Set<PropertyName> renamed = null;
        renamed = this._findExplicitNames(this._fields, renamed);
        renamed = this._findExplicitNames(this._getters, renamed);
        renamed = this._findExplicitNames(this._setters, renamed);
        if ((renamed = this._findExplicitNames(this._ctorParameters, renamed)) == null) {
            return Collections.emptySet();
        }
        return renamed;
    }

    public Collection<POJOPropertyBuilder> explode(Collection<PropertyName> newNames) {
        HashMap<PropertyName, POJOPropertyBuilder> props = new HashMap<PropertyName, POJOPropertyBuilder>();
        this._explode(newNames, props, this._fields);
        this._explode(newNames, props, this._getters);
        this._explode(newNames, props, this._setters);
        this._explode(newNames, props, this._ctorParameters);
        return props.values();
    }

    private void _explode(Collection<PropertyName> newNames, Map<PropertyName, POJOPropertyBuilder> props, Linked<?> accessors) {
        Linked<?> firstAcc = accessors;
        Linked<Object> node = accessors;
        while (node != null) {
            PropertyName name = node.name;
            if (!node.isNameExplicit || name == null) {
                if (node.isVisible) {
                    throw new IllegalStateException("Conflicting/ambiguous property name definitions (implicit name '" + this._name + "'): found multiple explicit names: " + newNames + ", but also implicit accessor: " + node);
                }
            } else {
                Linked<Object> n2;
                POJOPropertyBuilder prop = props.get(name);
                if (prop == null) {
                    prop = new POJOPropertyBuilder(this._config, this._annotationIntrospector, this._forSerialization, this._internalName, name);
                    props.put(name, prop);
                }
                if (firstAcc == this._fields) {
                    n2 = node;
                    prop._fields = n2.withNext(prop._fields);
                } else if (firstAcc == this._getters) {
                    n2 = node;
                    prop._getters = n2.withNext(prop._getters);
                } else if (firstAcc == this._setters) {
                    n2 = node;
                    prop._setters = n2.withNext(prop._setters);
                } else if (firstAcc == this._ctorParameters) {
                    n2 = node;
                    prop._ctorParameters = n2.withNext(prop._ctorParameters);
                } else {
                    throw new IllegalStateException("Internal error: mismatched accessors, property: " + this);
                }
            }
            node = node.next;
        }
    }

    private Set<PropertyName> _findExplicitNames(Linked<? extends AnnotatedMember> node, Set<PropertyName> renamed) {
        while (node != null) {
            if (node.isNameExplicit && node.name != null) {
                if (renamed == null) {
                    renamed = new HashSet<PropertyName>();
                }
                renamed.add(node.name);
            }
            node = node.next;
        }
        return renamed;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Property '").append(this._name).append("'; ctors: ").append(this._ctorParameters).append(", field(s): ").append(this._fields).append(", getter(s): ").append(this._getters).append(", setter(s): ").append(this._setters);
        sb.append("]");
        return sb.toString();
    }

    protected <T> T fromMemberAnnotations(WithMember<T> func) {
        T result = null;
        if (this._annotationIntrospector != null) {
            if (this._forSerialization) {
                if (this._getters != null) {
                    result = func.withMember((AnnotatedMember)this._getters.value);
                }
            } else {
                if (this._ctorParameters != null) {
                    result = func.withMember((AnnotatedMember)this._ctorParameters.value);
                }
                if (result == null && this._setters != null) {
                    result = func.withMember((AnnotatedMember)this._setters.value);
                }
            }
            if (result == null && this._fields != null) {
                result = func.withMember((AnnotatedMember)this._fields.value);
            }
        }
        return result;
    }

    protected <T> T fromMemberAnnotationsExcept(WithMember<T> func, T defaultValue) {
        T result;
        if (this._annotationIntrospector == null) {
            return null;
        }
        if (this._forSerialization) {
            T result2;
            if (this._getters != null && (result2 = func.withMember((AnnotatedMember)this._getters.value)) != null && result2 != defaultValue) {
                return result2;
            }
            if (this._fields != null && (result2 = func.withMember((AnnotatedMember)this._fields.value)) != null && result2 != defaultValue) {
                return result2;
            }
            if (this._ctorParameters != null && (result2 = func.withMember((AnnotatedMember)this._ctorParameters.value)) != null && result2 != defaultValue) {
                return result2;
            }
            if (this._setters != null && (result2 = func.withMember((AnnotatedMember)this._setters.value)) != null && result2 != defaultValue) {
                return result2;
            }
            return null;
        }
        if (this._ctorParameters != null && (result = func.withMember((AnnotatedMember)this._ctorParameters.value)) != null && result != defaultValue) {
            return result;
        }
        if (this._setters != null && (result = func.withMember((AnnotatedMember)this._setters.value)) != null && result != defaultValue) {
            return result;
        }
        if (this._fields != null && (result = func.withMember((AnnotatedMember)this._fields.value)) != null && result != defaultValue) {
            return result;
        }
        if (this._getters != null && (result = func.withMember((AnnotatedMember)this._getters.value)) != null && result != defaultValue) {
            return result;
        }
        return null;
    }

    protected Class<?> _rawTypeOf(AnnotatedMember m) {
        AnnotatedMethod meh;
        if (m instanceof AnnotatedMethod && (meh = (AnnotatedMethod)m).getParameterCount() > 0) {
            return meh.getParameterType(0).getRawClass();
        }
        return m.getType().getRawClass();
    }

    protected static final class Linked<T> {
        public final T value;
        public final Linked<T> next;
        public final PropertyName name;
        public final boolean isNameExplicit;
        public final boolean isVisible;
        public final boolean isMarkedIgnored;

        public Linked(T v, Linked<T> n, PropertyName name, boolean explName, boolean visible, boolean ignored) {
            this.value = v;
            this.next = n;
            PropertyName propertyName = this.name = name == null || name.isEmpty() ? null : name;
            if (explName) {
                if (this.name == null) {
                    throw new IllegalArgumentException("Cannot pass true for 'explName' if name is null/empty");
                }
                if (!name.hasSimpleName()) {
                    explName = false;
                }
            }
            this.isNameExplicit = explName;
            this.isVisible = visible;
            this.isMarkedIgnored = ignored;
        }

        public Linked<T> withoutNext() {
            if (this.next == null) {
                return this;
            }
            return new Linked<T>(this.value, null, this.name, this.isNameExplicit, this.isVisible, this.isMarkedIgnored);
        }

        public Linked<T> withValue(T newValue) {
            if (newValue == this.value) {
                return this;
            }
            return new Linked<T>(newValue, this.next, this.name, this.isNameExplicit, this.isVisible, this.isMarkedIgnored);
        }

        public Linked<T> withNext(Linked<T> newNext) {
            if (newNext == this.next) {
                return this;
            }
            return new Linked<T>(this.value, newNext, this.name, this.isNameExplicit, this.isVisible, this.isMarkedIgnored);
        }

        public Linked<T> withoutIgnored() {
            Linked<T> newNext;
            if (this.isMarkedIgnored) {
                return this.next == null ? null : this.next.withoutIgnored();
            }
            if (this.next != null && (newNext = this.next.withoutIgnored()) != this.next) {
                return this.withNext(newNext);
            }
            return this;
        }

        public Linked<T> withoutNonVisible() {
            Linked<T> newNext = this.next == null ? null : this.next.withoutNonVisible();
            return this.isVisible ? this.withNext(newNext) : newNext;
        }

        protected Linked<T> append(Linked<T> appendable) {
            if (this.next == null) {
                return this.withNext(appendable);
            }
            return this.withNext(this.next.append(appendable));
        }

        public Linked<T> trimByVisibility() {
            if (this.next == null) {
                return this;
            }
            Linked<T> newNext = this.next.trimByVisibility();
            if (this.name != null) {
                if (newNext.name == null) {
                    return this.withNext(null);
                }
                return this.withNext(newNext);
            }
            if (newNext.name != null) {
                return newNext;
            }
            if (this.isVisible == newNext.isVisible) {
                return this.withNext(newNext);
            }
            return this.isVisible ? this.withNext(null) : newNext;
        }

        public String toString() {
            String msg = String.format("%s[visible=%b,ignore=%b,explicitName=%b]", this.value.toString(), this.isVisible, this.isMarkedIgnored, this.isNameExplicit);
            if (this.next != null) {
                msg = msg + ", " + this.next.toString();
            }
            return msg;
        }
    }

    protected static class MemberIterator<T extends AnnotatedMember>
    implements Iterator<T> {
        private Linked<T> next;

        public MemberIterator(Linked<T> first) {
            this.next = first;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public T next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            AnnotatedMember result = (AnnotatedMember)this.next.value;
            this.next = this.next.next;
            return (T)result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static interface WithMember<T> {
        public T withMember(AnnotatedMember var1);
    }
}

