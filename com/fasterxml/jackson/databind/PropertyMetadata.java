/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import java.io.Serializable;

public class PropertyMetadata
implements Serializable {
    private static final long serialVersionUID = -1L;
    public static final PropertyMetadata STD_REQUIRED = new PropertyMetadata(Boolean.TRUE, null, null, null, null, null, null);
    public static final PropertyMetadata STD_OPTIONAL = new PropertyMetadata(Boolean.FALSE, null, null, null, null, null, null);
    public static final PropertyMetadata STD_REQUIRED_OR_OPTIONAL = new PropertyMetadata(null, null, null, null, null, null, null);
    protected final Boolean _required;
    protected final String _description;
    protected final Integer _index;
    protected final String _defaultValue;
    protected final transient MergeInfo _mergeInfo;
    protected Nulls _valueNulls;
    protected Nulls _contentNulls;

    protected PropertyMetadata(Boolean req, String desc, Integer index, String def, MergeInfo mergeInfo, Nulls valueNulls, Nulls contentNulls) {
        this._required = req;
        this._description = desc;
        this._index = index;
        this._defaultValue = def == null || def.isEmpty() ? null : def;
        this._mergeInfo = mergeInfo;
        this._valueNulls = valueNulls;
        this._contentNulls = contentNulls;
    }

    public static PropertyMetadata construct(Boolean req, String desc, Integer index, String defaultValue) {
        if (desc != null || index != null || defaultValue != null) {
            return new PropertyMetadata(req, desc, index, defaultValue, null, null, null);
        }
        if (req == null) {
            return STD_REQUIRED_OR_OPTIONAL;
        }
        return req != false ? STD_REQUIRED : STD_OPTIONAL;
    }

    @Deprecated
    public static PropertyMetadata construct(boolean req, String desc, Integer index, String defaultValue) {
        if (desc != null || index != null || defaultValue != null) {
            return new PropertyMetadata(req, desc, index, defaultValue, null, null, null);
        }
        return req ? STD_REQUIRED : STD_OPTIONAL;
    }

    protected Object readResolve() {
        if (this._description == null && this._index == null && this._defaultValue == null && this._mergeInfo == null && this._valueNulls == null && this._contentNulls == null) {
            if (this._required == null) {
                return STD_REQUIRED_OR_OPTIONAL;
            }
            return this._required != false ? STD_REQUIRED : STD_OPTIONAL;
        }
        return this;
    }

    public PropertyMetadata withDescription(String desc) {
        return new PropertyMetadata(this._required, desc, this._index, this._defaultValue, this._mergeInfo, this._valueNulls, this._contentNulls);
    }

    public PropertyMetadata withMergeInfo(MergeInfo mergeInfo) {
        return new PropertyMetadata(this._required, this._description, this._index, this._defaultValue, mergeInfo, this._valueNulls, this._contentNulls);
    }

    public PropertyMetadata withNulls(Nulls valueNulls, Nulls contentNulls) {
        return new PropertyMetadata(this._required, this._description, this._index, this._defaultValue, this._mergeInfo, valueNulls, contentNulls);
    }

    public PropertyMetadata withDefaultValue(String def) {
        if (def == null || def.isEmpty()) {
            if (this._defaultValue == null) {
                return this;
            }
            def = null;
        } else if (def.equals(this._defaultValue)) {
            return this;
        }
        return new PropertyMetadata(this._required, this._description, this._index, def, this._mergeInfo, this._valueNulls, this._contentNulls);
    }

    public PropertyMetadata withIndex(Integer index) {
        return new PropertyMetadata(this._required, this._description, index, this._defaultValue, this._mergeInfo, this._valueNulls, this._contentNulls);
    }

    public PropertyMetadata withRequired(Boolean b) {
        if (b == null ? this._required == null : b.equals(this._required)) {
            return this;
        }
        return new PropertyMetadata(b, this._description, this._index, this._defaultValue, this._mergeInfo, this._valueNulls, this._contentNulls);
    }

    public String getDescription() {
        return this._description;
    }

    public String getDefaultValue() {
        return this._defaultValue;
    }

    public boolean hasDefaultValue() {
        return this._defaultValue != null;
    }

    public boolean isRequired() {
        return this._required != null && this._required != false;
    }

    public Boolean getRequired() {
        return this._required;
    }

    public Integer getIndex() {
        return this._index;
    }

    public boolean hasIndex() {
        return this._index != null;
    }

    public MergeInfo getMergeInfo() {
        return this._mergeInfo;
    }

    public Nulls getValueNulls() {
        return this._valueNulls;
    }

    public Nulls getContentNulls() {
        return this._contentNulls;
    }

    public static final class MergeInfo {
        public final AnnotatedMember getter;
        public final boolean fromDefaults;

        protected MergeInfo(AnnotatedMember getter, boolean fromDefaults) {
            this.getter = getter;
            this.fromDefaults = fromDefaults;
        }

        public static MergeInfo createForDefaults(AnnotatedMember getter) {
            return new MergeInfo(getter, true);
        }

        public static MergeInfo createForTypeOverride(AnnotatedMember getter) {
            return new MergeInfo(getter, false);
        }

        public static MergeInfo createForPropertyOverride(AnnotatedMember getter) {
            return new MergeInfo(getter, false);
        }
    }
}

