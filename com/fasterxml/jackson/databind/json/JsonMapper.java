/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.PackageVersion;

public class JsonMapper
extends ObjectMapper {
    private static final long serialVersionUID = 1L;

    public JsonMapper() {
        this(new JsonFactory());
    }

    public JsonMapper(JsonFactory f) {
        super(f);
    }

    protected JsonMapper(JsonMapper src) {
        super(src);
    }

    @Override
    public JsonMapper copy() {
        this._checkInvalidCopy(JsonMapper.class);
        return new JsonMapper(this);
    }

    public static Builder builder() {
        return new Builder(new JsonMapper());
    }

    public static Builder builder(JsonFactory streamFactory) {
        return new Builder(new JsonMapper(streamFactory));
    }

    public Builder rebuild() {
        return new Builder(this.copy());
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public JsonFactory getFactory() {
        return this._jsonFactory;
    }

    public boolean isEnabled(JsonReadFeature f) {
        return this.isEnabled(f.mappedFeature());
    }

    public boolean isEnabled(JsonWriteFeature f) {
        return this.isEnabled(f.mappedFeature());
    }

    public static class Builder
    extends MapperBuilder<JsonMapper, Builder> {
        public Builder(JsonMapper m) {
            super(m);
        }

        public Builder enable(JsonReadFeature ... features) {
            for (JsonReadFeature f : features) {
                ((JsonMapper)this._mapper).enable(f.mappedFeature());
            }
            return this;
        }

        public Builder disable(JsonReadFeature ... features) {
            for (JsonReadFeature f : features) {
                ((JsonMapper)this._mapper).disable(f.mappedFeature());
            }
            return this;
        }

        public Builder configure(JsonReadFeature f, boolean state) {
            if (state) {
                ((JsonMapper)this._mapper).enable(f.mappedFeature());
            } else {
                ((JsonMapper)this._mapper).disable(f.mappedFeature());
            }
            return this;
        }

        public Builder enable(JsonWriteFeature ... features) {
            for (JsonWriteFeature f : features) {
                ((JsonMapper)this._mapper).enable(f.mappedFeature());
            }
            return this;
        }

        public Builder disable(JsonWriteFeature ... features) {
            for (JsonWriteFeature f : features) {
                ((JsonMapper)this._mapper).disable(f.mappedFeature());
            }
            return this;
        }

        public Builder configure(JsonWriteFeature f, boolean state) {
            if (state) {
                ((JsonMapper)this._mapper).enable(f.mappedFeature());
            } else {
                ((JsonMapper)this._mapper).disable(f.mappedFeature());
            }
            return this;
        }
    }
}

