/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConfigOverrides
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Map<Class<?>, MutableConfigOverride> _overrides;
    protected JsonInclude.Value _defaultInclusion;
    protected JsonSetter.Value _defaultSetterInfo;
    protected VisibilityChecker<?> _visibilityChecker;
    protected Boolean _defaultMergeable;
    protected Boolean _defaultLeniency;

    public ConfigOverrides() {
        this(null, JsonInclude.Value.empty(), JsonSetter.Value.empty(), VisibilityChecker.Std.defaultInstance(), null, null);
    }

    protected ConfigOverrides(Map<Class<?>, MutableConfigOverride> overrides, JsonInclude.Value defIncl, JsonSetter.Value defSetter, VisibilityChecker<?> defVisibility, Boolean defMergeable, Boolean defLeniency) {
        this._overrides = overrides;
        this._defaultInclusion = defIncl;
        this._defaultSetterInfo = defSetter;
        this._visibilityChecker = defVisibility;
        this._defaultMergeable = defMergeable;
        this._defaultLeniency = defLeniency;
    }

    @Deprecated
    protected ConfigOverrides(Map<Class<?>, MutableConfigOverride> overrides, JsonInclude.Value defIncl, JsonSetter.Value defSetter, VisibilityChecker<?> defVisibility, Boolean defMergeable) {
        this(overrides, defIncl, defSetter, defVisibility, defMergeable, null);
    }

    public ConfigOverrides copy() {
        Map<Class<?>, MutableConfigOverride> newOverrides;
        if (this._overrides == null) {
            newOverrides = null;
        } else {
            newOverrides = this._newMap();
            for (Map.Entry<Class<?>, MutableConfigOverride> entry : this._overrides.entrySet()) {
                newOverrides.put(entry.getKey(), entry.getValue().copy());
            }
        }
        return new ConfigOverrides(newOverrides, this._defaultInclusion, this._defaultSetterInfo, this._visibilityChecker, this._defaultMergeable, this._defaultLeniency);
    }

    public ConfigOverride findOverride(Class<?> type) {
        if (this._overrides == null) {
            return null;
        }
        return this._overrides.get(type);
    }

    public MutableConfigOverride findOrCreateOverride(Class<?> type) {
        MutableConfigOverride override;
        if (this._overrides == null) {
            this._overrides = this._newMap();
        }
        if ((override = this._overrides.get(type)) == null) {
            override = new MutableConfigOverride();
            this._overrides.put(type, override);
        }
        return override;
    }

    public JsonFormat.Value findFormatDefaults(Class<?> type) {
        JsonFormat.Value format;
        ConfigOverride override;
        if (this._overrides != null && (override = (ConfigOverride)this._overrides.get(type)) != null && (format = override.getFormat()) != null) {
            if (!format.hasLenient()) {
                return format.withLenient(this._defaultLeniency);
            }
            return format;
        }
        if (this._defaultLeniency == null) {
            return JsonFormat.Value.empty();
        }
        return JsonFormat.Value.forLeniency(this._defaultLeniency);
    }

    public JsonInclude.Value getDefaultInclusion() {
        return this._defaultInclusion;
    }

    public JsonSetter.Value getDefaultSetterInfo() {
        return this._defaultSetterInfo;
    }

    public Boolean getDefaultMergeable() {
        return this._defaultMergeable;
    }

    public Boolean getDefaultLeniency() {
        return this._defaultLeniency;
    }

    public VisibilityChecker<?> getDefaultVisibility() {
        return this._visibilityChecker;
    }

    public void setDefaultInclusion(JsonInclude.Value v) {
        this._defaultInclusion = v;
    }

    public void setDefaultSetterInfo(JsonSetter.Value v) {
        this._defaultSetterInfo = v;
    }

    public void setDefaultMergeable(Boolean v) {
        this._defaultMergeable = v;
    }

    public void setDefaultLeniency(Boolean v) {
        this._defaultLeniency = v;
    }

    public void setDefaultVisibility(VisibilityChecker<?> v) {
        this._visibilityChecker = v;
    }

    protected Map<Class<?>, MutableConfigOverride> _newMap() {
        return new HashMap();
    }
}

