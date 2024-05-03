/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import java.io.IOException;

public abstract class TypeSerializer {
    public abstract TypeSerializer forProperty(BeanProperty var1);

    public abstract JsonTypeInfo.As getTypeInclusion();

    public abstract String getPropertyName();

    public abstract TypeIdResolver getTypeIdResolver();

    public WritableTypeId typeId(Object value, JsonToken valueShape) {
        WritableTypeId typeIdDef = new WritableTypeId(value, valueShape);
        switch (this.getTypeInclusion()) {
            case EXISTING_PROPERTY: {
                typeIdDef.include = WritableTypeId.Inclusion.PAYLOAD_PROPERTY;
                typeIdDef.asProperty = this.getPropertyName();
                break;
            }
            case EXTERNAL_PROPERTY: {
                typeIdDef.include = WritableTypeId.Inclusion.PARENT_PROPERTY;
                typeIdDef.asProperty = this.getPropertyName();
                break;
            }
            case PROPERTY: {
                typeIdDef.include = WritableTypeId.Inclusion.METADATA_PROPERTY;
                typeIdDef.asProperty = this.getPropertyName();
                break;
            }
            case WRAPPER_ARRAY: {
                typeIdDef.include = WritableTypeId.Inclusion.WRAPPER_ARRAY;
                break;
            }
            case WRAPPER_OBJECT: {
                typeIdDef.include = WritableTypeId.Inclusion.WRAPPER_OBJECT;
                break;
            }
            default: {
                VersionUtil.throwInternal();
            }
        }
        return typeIdDef;
    }

    public WritableTypeId typeId(Object value, JsonToken valueShape, Object id) {
        WritableTypeId typeId = this.typeId(value, valueShape);
        typeId.id = id;
        return typeId;
    }

    public WritableTypeId typeId(Object value, Class<?> typeForId, JsonToken valueShape) {
        WritableTypeId typeId = this.typeId(value, valueShape);
        typeId.forValueType = typeForId;
        return typeId;
    }

    public abstract WritableTypeId writeTypePrefix(JsonGenerator var1, WritableTypeId var2) throws IOException;

    public abstract WritableTypeId writeTypeSuffix(JsonGenerator var1, WritableTypeId var2) throws IOException;

    @Deprecated
    public void writeTypePrefixForScalar(Object value, JsonGenerator g) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.VALUE_STRING));
    }

    @Deprecated
    public void writeTypePrefixForObject(Object value, JsonGenerator g) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.START_OBJECT));
    }

    @Deprecated
    public void writeTypePrefixForArray(Object value, JsonGenerator g) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.START_ARRAY));
    }

    @Deprecated
    public void writeTypeSuffixForScalar(Object value, JsonGenerator g) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.VALUE_STRING));
    }

    @Deprecated
    public void writeTypeSuffixForObject(Object value, JsonGenerator g) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.START_OBJECT));
    }

    @Deprecated
    public void writeTypeSuffixForArray(Object value, JsonGenerator g) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.START_ARRAY));
    }

    @Deprecated
    public void writeTypePrefixForScalar(Object value, JsonGenerator g, Class<?> type) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, type, JsonToken.VALUE_STRING));
    }

    @Deprecated
    public void writeTypePrefixForObject(Object value, JsonGenerator g, Class<?> type) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, type, JsonToken.START_OBJECT));
    }

    @Deprecated
    public void writeTypePrefixForArray(Object value, JsonGenerator g, Class<?> type) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, type, JsonToken.START_ARRAY));
    }

    @Deprecated
    public void writeCustomTypePrefixForScalar(Object value, JsonGenerator g, String typeId) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.VALUE_STRING, typeId));
    }

    @Deprecated
    public void writeCustomTypePrefixForObject(Object value, JsonGenerator g, String typeId) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.START_OBJECT, typeId));
    }

    @Deprecated
    public void writeCustomTypePrefixForArray(Object value, JsonGenerator g, String typeId) throws IOException {
        this.writeTypePrefix(g, this.typeId(value, JsonToken.START_ARRAY, typeId));
    }

    @Deprecated
    public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator g, String typeId) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.VALUE_STRING, typeId));
    }

    @Deprecated
    public void writeCustomTypeSuffixForObject(Object value, JsonGenerator g, String typeId) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.START_OBJECT, typeId));
    }

    @Deprecated
    public void writeCustomTypeSuffixForArray(Object value, JsonGenerator g, String typeId) throws IOException {
        this._writeLegacySuffix(g, this.typeId(value, JsonToken.START_ARRAY, typeId));
    }

    protected final void _writeLegacySuffix(JsonGenerator g, WritableTypeId typeId) throws IOException {
        typeId.wrapperWritten = !g.canWriteTypeId();
        this.writeTypeSuffix(g, typeId);
    }
}

