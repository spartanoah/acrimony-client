/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import java.io.IOException;

public class XmlDeserializationContext
extends DefaultDeserializationContext {
    private static final long serialVersionUID = 1L;

    public XmlDeserializationContext(DeserializerFactory df) {
        super(df, null);
    }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializationConfig config, JsonParser p, InjectableValues values) {
        super(src, config, p, values);
    }

    private XmlDeserializationContext(XmlDeserializationContext src) {
        super(src);
    }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializerFactory factory) {
        super(src, factory);
    }

    private XmlDeserializationContext(XmlDeserializationContext src, DeserializationConfig config) {
        super((DefaultDeserializationContext)src, config);
    }

    @Override
    public XmlDeserializationContext copy() {
        return new XmlDeserializationContext(this);
    }

    @Override
    public DefaultDeserializationContext createInstance(DeserializationConfig config, JsonParser p, InjectableValues values) {
        return new XmlDeserializationContext(this, config, p, values);
    }

    public DefaultDeserializationContext createDummyInstance(DeserializationConfig config) {
        return new XmlDeserializationContext(this, config);
    }

    @Override
    public DefaultDeserializationContext with(DeserializerFactory factory) {
        return new XmlDeserializationContext(this, factory);
    }

    public Object readRootValue(JsonParser p, JavaType valueType, JsonDeserializer<Object> deser, Object valueToUpdate) throws IOException {
        if (this._config.useRootWrapping()) {
            return this._unwrapAndDeserialize(p, valueType, deser, valueToUpdate);
        }
        if (valueToUpdate == null) {
            return deser.deserialize(p, this);
        }
        return deser.deserialize(p, this, valueToUpdate);
    }

    public String extractScalarFromObject(JsonParser p, JsonDeserializer<?> deser, Class<?> scalarType) throws IOException {
        String text = "";
        while (p.nextToken() == JsonToken.FIELD_NAME) {
            String propName = p.currentName();
            JsonToken t = p.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                if (!propName.equals("")) continue;
                text = p.getText();
                continue;
            }
            p.skipChildren();
        }
        return text;
    }
}

