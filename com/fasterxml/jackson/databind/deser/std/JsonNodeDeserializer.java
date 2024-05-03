/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.BaseNodeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class JsonNodeDeserializer
extends BaseNodeDeserializer<JsonNode> {
    private static final JsonNodeDeserializer instance = new JsonNodeDeserializer();

    protected JsonNodeDeserializer() {
        super(JsonNode.class, null);
    }

    public static JsonDeserializer<? extends JsonNode> getDeserializer(Class<?> nodeClass) {
        if (nodeClass == ObjectNode.class) {
            return ObjectDeserializer.getInstance();
        }
        if (nodeClass == ArrayNode.class) {
            return ArrayDeserializer.getInstance();
        }
        return instance;
    }

    @Override
    public JsonNode getNullValue(DeserializationContext ctxt) {
        return ctxt.getNodeFactory().nullNode();
    }

    @Override
    public JsonNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.currentTokenId()) {
            case 1: {
                return this.deserializeObject(p, ctxt, ctxt.getNodeFactory());
            }
            case 3: {
                return this.deserializeArray(p, ctxt, ctxt.getNodeFactory());
            }
        }
        return this.deserializeAny(p, ctxt, ctxt.getNodeFactory());
    }

    static final class ArrayDeserializer
    extends BaseNodeDeserializer<ArrayNode> {
        private static final long serialVersionUID = 1L;
        protected static final ArrayDeserializer _instance = new ArrayDeserializer();

        protected ArrayDeserializer() {
            super(ArrayNode.class, true);
        }

        public static ArrayDeserializer getInstance() {
            return _instance;
        }

        @Override
        public ArrayNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedStartArrayToken()) {
                return this.deserializeArray(p, ctxt, ctxt.getNodeFactory());
            }
            return (ArrayNode)ctxt.handleUnexpectedToken(ArrayNode.class, p);
        }

        @Override
        public ArrayNode deserialize(JsonParser p, DeserializationContext ctxt, ArrayNode node) throws IOException {
            if (p.isExpectedStartArrayToken()) {
                return (ArrayNode)this.updateArray(p, ctxt, node);
            }
            return (ArrayNode)ctxt.handleUnexpectedToken(ArrayNode.class, p);
        }
    }

    static final class ObjectDeserializer
    extends BaseNodeDeserializer<ObjectNode> {
        private static final long serialVersionUID = 1L;
        protected static final ObjectDeserializer _instance = new ObjectDeserializer();

        protected ObjectDeserializer() {
            super(ObjectNode.class, true);
        }

        public static ObjectDeserializer getInstance() {
            return _instance;
        }

        @Override
        public ObjectNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedStartObjectToken()) {
                return this.deserializeObject(p, ctxt, ctxt.getNodeFactory());
            }
            if (p.hasToken(JsonToken.FIELD_NAME)) {
                return this.deserializeObjectAtName(p, ctxt, ctxt.getNodeFactory());
            }
            if (p.hasToken(JsonToken.END_OBJECT)) {
                return ctxt.getNodeFactory().objectNode();
            }
            return (ObjectNode)ctxt.handleUnexpectedToken(ObjectNode.class, p);
        }

        @Override
        public ObjectNode deserialize(JsonParser p, DeserializationContext ctxt, ObjectNode node) throws IOException {
            if (p.isExpectedStartObjectToken() || p.hasToken(JsonToken.FIELD_NAME)) {
                return (ObjectNode)this.updateObject(p, ctxt, node);
            }
            return (ObjectNode)ctxt.handleUnexpectedToken(ObjectNode.class, p);
        }
    }
}

