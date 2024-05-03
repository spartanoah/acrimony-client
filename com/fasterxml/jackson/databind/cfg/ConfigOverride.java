/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

public abstract class ConfigOverride {
    protected JsonFormat.Value _format;
    protected JsonInclude.Value _include;
    protected JsonInclude.Value _includeAsProperty;
    protected JsonIgnoreProperties.Value _ignorals;
    protected JsonSetter.Value _setterInfo;
    protected JsonAutoDetect.Value _visibility;
    protected Boolean _isIgnoredType;
    protected Boolean _mergeable;

    protected ConfigOverride() {
    }

    protected ConfigOverride(ConfigOverride src) {
        this._format = src._format;
        this._include = src._include;
        this._includeAsProperty = src._includeAsProperty;
        this._ignorals = src._ignorals;
        this._setterInfo = src._setterInfo;
        this._visibility = src._visibility;
        this._isIgnoredType = src._isIgnoredType;
        this._mergeable = src._mergeable;
    }

    public static ConfigOverride empty() {
        return Empty.INSTANCE;
    }

    public JsonFormat.Value getFormat() {
        return this._format;
    }

    public JsonInclude.Value getInclude() {
        return this._include;
    }

    public JsonInclude.Value getIncludeAsProperty() {
        return this._includeAsProperty;
    }

    public JsonIgnoreProperties.Value getIgnorals() {
        return this._ignorals;
    }

    public Boolean getIsIgnoredType() {
        return this._isIgnoredType;
    }

    public JsonSetter.Value getSetterInfo() {
        return this._setterInfo;
    }

    public JsonAutoDetect.Value getVisibility() {
        return this._visibility;
    }

    public Boolean getMergeable() {
        return this._mergeable;
    }

    static final class Empty
    extends ConfigOverride {
        static final Empty INSTANCE = new Empty();

        private Empty() {
        }
    }
}

