/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public abstract class ConcreteBeanPropertyBase
implements BeanProperty,
Serializable {
    private static final long serialVersionUID = 1L;
    protected final PropertyMetadata _metadata;
    protected transient List<PropertyName> _aliases;

    protected ConcreteBeanPropertyBase(PropertyMetadata md) {
        this._metadata = md == null ? PropertyMetadata.STD_REQUIRED_OR_OPTIONAL : md;
    }

    protected ConcreteBeanPropertyBase(ConcreteBeanPropertyBase src) {
        this._metadata = src._metadata;
    }

    @Override
    public boolean isRequired() {
        return this._metadata.isRequired();
    }

    @Override
    public PropertyMetadata getMetadata() {
        return this._metadata;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    @Deprecated
    public final JsonFormat.Value findFormatOverrides(AnnotationIntrospector intr) {
        AnnotatedMember member;
        JsonFormat.Value f = null;
        if (intr != null && (member = this.getMember()) != null) {
            f = intr.findFormat(member);
        }
        if (f == null) {
            f = EMPTY_FORMAT;
        }
        return f;
    }

    @Override
    public JsonFormat.Value findPropertyFormat(MapperConfig<?> config, Class<?> baseType) {
        AnnotatedMember member;
        JsonFormat.Value v1 = config.getDefaultPropertyFormat(baseType);
        JsonFormat.Value v2 = null;
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        if (intr != null && (member = this.getMember()) != null) {
            v2 = intr.findFormat(member);
        }
        if (v1 == null) {
            return v2 == null ? EMPTY_FORMAT : v2;
        }
        return v2 == null ? v1 : v1.withOverrides(v2);
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(MapperConfig<?> config, Class<?> baseType) {
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedMember member = this.getMember();
        if (member == null) {
            JsonInclude.Value def = config.getDefaultPropertyInclusion(baseType);
            return def;
        }
        JsonInclude.Value v0 = config.getDefaultInclusion(baseType, member.getRawType());
        if (intr == null) {
            return v0;
        }
        JsonInclude.Value v = intr.findPropertyInclusion(member);
        if (v0 == null) {
            return v;
        }
        return v0.withOverrides(v);
    }

    @Override
    public List<PropertyName> findAliases(MapperConfig<?> config) {
        List<PropertyName> aliases = this._aliases;
        if (aliases == null) {
            AnnotatedMember member;
            AnnotationIntrospector intr = config.getAnnotationIntrospector();
            if (intr != null && (member = this.getMember()) != null) {
                aliases = intr.findPropertyAliases(member);
            }
            if (aliases == null) {
                aliases = Collections.emptyList();
            }
            this._aliases = aliases;
        }
        return aliases;
    }
}

