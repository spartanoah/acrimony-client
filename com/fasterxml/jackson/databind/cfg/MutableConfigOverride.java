/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import java.io.Serializable;

public class MutableConfigOverride
extends ConfigOverride
implements Serializable {
    private static final long serialVersionUID = 1L;

    public MutableConfigOverride() {
    }

    protected MutableConfigOverride(MutableConfigOverride src) {
        super(src);
    }

    public MutableConfigOverride copy() {
        return new MutableConfigOverride(this);
    }

    public MutableConfigOverride setFormat(JsonFormat.Value v) {
        this._format = v;
        return this;
    }

    public MutableConfigOverride setInclude(JsonInclude.Value v) {
        this._include = v;
        return this;
    }

    public MutableConfigOverride setIncludeAsProperty(JsonInclude.Value v) {
        this._includeAsProperty = v;
        return this;
    }

    public MutableConfigOverride setIgnorals(JsonIgnoreProperties.Value v) {
        this._ignorals = v;
        return this;
    }

    public MutableConfigOverride setIsIgnoredType(Boolean v) {
        this._isIgnoredType = v;
        return this;
    }

    public MutableConfigOverride setSetterInfo(JsonSetter.Value v) {
        this._setterInfo = v;
        return this;
    }

    public MutableConfigOverride setVisibility(JsonAutoDetect.Value v) {
        this._visibility = v;
        return this;
    }

    public MutableConfigOverride setMergeable(Boolean v) {
        this._mergeable = v;
        return this;
    }
}

