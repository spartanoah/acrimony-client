/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class MappingJsonFactory
extends JsonFactory {
    private static final long serialVersionUID = -1L;

    public MappingJsonFactory() {
        this((ObjectMapper)null);
    }

    public MappingJsonFactory(ObjectMapper mapper) {
        super(mapper);
        if (mapper == null) {
            this.setCodec(new ObjectMapper(this));
        }
    }

    public MappingJsonFactory(JsonFactory src, ObjectMapper mapper) {
        super(src, mapper);
        if (mapper == null) {
            this.setCodec(new ObjectMapper(this));
        }
    }

    @Override
    public final ObjectMapper getCodec() {
        return (ObjectMapper)this._objectCodec;
    }

    @Override
    public JsonFactory copy() {
        this._checkInvalidCopy(MappingJsonFactory.class);
        return new MappingJsonFactory((JsonFactory)this, null);
    }

    @Override
    public String getFormatName() {
        return "JSON";
    }

    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException {
        if (this.getClass() == MappingJsonFactory.class) {
            return this.hasJSONFormat(acc);
        }
        return null;
    }
}

