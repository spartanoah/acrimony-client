/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.io.IOException;

abstract class BaseNodeDeserializer<T extends JsonNode>
extends StdDeserializer<T> {
    protected final Boolean _supportsUpdates;

    public BaseNodeDeserializer(Class<T> vc, Boolean supportsUpdates) {
        super(vc);
        this._supportsUpdates = supportsUpdates;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return this._supportsUpdates;
    }

    protected void _handleDuplicateField(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory, String fieldName, ObjectNode objectNode, JsonNode oldValue, JsonNode newValue) throws JsonProcessingException {
        if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)) {
            ctxt.reportInputMismatch(JsonNode.class, "Duplicate field '%s' for `ObjectNode`: not allowed when `DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY` enabled", fieldName);
        }
    }

    protected final ObjectNode deserializeObject(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        ObjectNode node = nodeFactory.objectNode();
        String key = p.nextFieldName();
        while (key != null) {
            JsonNode value;
            JsonToken t = p.nextToken();
            if (t == null) {
                t = JsonToken.NOT_AVAILABLE;
            }
            switch (t.id()) {
                case 1: {
                    value = this.deserializeObject(p, ctxt, nodeFactory);
                    break;
                }
                case 3: {
                    value = this.deserializeArray(p, ctxt, nodeFactory);
                    break;
                }
                case 12: {
                    value = this._fromEmbedded(p, ctxt, nodeFactory);
                    break;
                }
                case 6: {
                    value = nodeFactory.textNode(p.getText());
                    break;
                }
                case 7: {
                    value = this._fromInt(p, ctxt, nodeFactory);
                    break;
                }
                case 9: {
                    value = nodeFactory.booleanNode(true);
                    break;
                }
                case 10: {
                    value = nodeFactory.booleanNode(false);
                    break;
                }
                case 11: {
                    value = nodeFactory.nullNode();
                    break;
                }
                default: {
                    value = this.deserializeAny(p, ctxt, nodeFactory);
                }
            }
            JsonNode old = node.replace(key, value);
            if (old != null) {
                this._handleDuplicateField(p, ctxt, nodeFactory, key, node, old, value);
            }
            key = p.nextFieldName();
        }
        return node;
    }

    protected final ObjectNode deserializeObjectAtName(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        ObjectNode node = nodeFactory.objectNode();
        String key = p.getCurrentName();
        while (key != null) {
            JsonNode value;
            JsonToken t = p.nextToken();
            if (t == null) {
                t = JsonToken.NOT_AVAILABLE;
            }
            switch (t.id()) {
                case 1: {
                    value = this.deserializeObject(p, ctxt, nodeFactory);
                    break;
                }
                case 3: {
                    value = this.deserializeArray(p, ctxt, nodeFactory);
                    break;
                }
                case 12: {
                    value = this._fromEmbedded(p, ctxt, nodeFactory);
                    break;
                }
                case 6: {
                    value = nodeFactory.textNode(p.getText());
                    break;
                }
                case 7: {
                    value = this._fromInt(p, ctxt, nodeFactory);
                    break;
                }
                case 9: {
                    value = nodeFactory.booleanNode(true);
                    break;
                }
                case 10: {
                    value = nodeFactory.booleanNode(false);
                    break;
                }
                case 11: {
                    value = nodeFactory.nullNode();
                    break;
                }
                default: {
                    value = this.deserializeAny(p, ctxt, nodeFactory);
                }
            }
            JsonNode old = node.replace(key, value);
            if (old != null) {
                this._handleDuplicateField(p, ctxt, nodeFactory, key, node, old, value);
            }
            key = p.nextFieldName();
        }
        return node;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    protected final JsonNode updateObject(JsonParser p, DeserializationContext ctxt, ObjectNode node) throws IOException {
        if (p.isExpectedStartObjectToken()) {
            key = p.nextFieldName();
        } else {
            if (!p.hasToken(JsonToken.FIELD_NAME)) {
                return (JsonNode)this.deserialize(p, ctxt);
            }
            key = p.getCurrentName();
        }
        while (key != null) {
            t = p.nextToken();
            old = node.get(key);
            if (old == null) ** GOTO lbl-1000
            if (old instanceof ObjectNode) {
                newValue = this.updateObject(p, ctxt, (ObjectNode)old);
                if (newValue != old) {
                    node.set(key, newValue);
                }
            } else if (old instanceof ArrayNode) {
                newValue = this.updateArray(p, ctxt, (ArrayNode)old);
                if (newValue != old) {
                    node.set(key, newValue);
                }
            } else lbl-1000:
            // 2 sources

            {
                if (t == null) {
                    t = JsonToken.NOT_AVAILABLE;
                }
                nodeFactory = ctxt.getNodeFactory();
                switch (t.id()) {
                    case 1: {
                        value /* !! */  = this.deserializeObject(p, ctxt, nodeFactory);
                        break;
                    }
                    case 3: {
                        value /* !! */  = this.deserializeArray(p, ctxt, nodeFactory);
                        break;
                    }
                    case 12: {
                        value /* !! */  = this._fromEmbedded(p, ctxt, nodeFactory);
                        break;
                    }
                    case 6: {
                        value /* !! */  = nodeFactory.textNode(p.getText());
                        break;
                    }
                    case 7: {
                        value /* !! */  = this._fromInt(p, ctxt, nodeFactory);
                        break;
                    }
                    case 9: {
                        value /* !! */  = nodeFactory.booleanNode(true);
                        break;
                    }
                    case 10: {
                        value /* !! */  = nodeFactory.booleanNode(false);
                        break;
                    }
                    case 11: {
                        value /* !! */  = nodeFactory.nullNode();
                        break;
                    }
                    default: {
                        value /* !! */  = this.deserializeAny(p, ctxt, nodeFactory);
                    }
                }
                if (old != null) {
                    this._handleDuplicateField(p, ctxt, nodeFactory, key, node, old, value /* !! */ );
                }
                node.set(key, value /* !! */ );
            }
            key = p.nextFieldName();
        }
        return node;
    }

    protected final ArrayNode deserializeArray(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        ArrayNode node = nodeFactory.arrayNode();
        block11: while (true) {
            JsonToken t = p.nextToken();
            switch (t.id()) {
                case 1: {
                    node.add(this.deserializeObject(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 3: {
                    node.add(this.deserializeArray(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 4: {
                    return node;
                }
                case 12: {
                    node.add(this._fromEmbedded(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 6: {
                    node.add(nodeFactory.textNode(p.getText()));
                    continue block11;
                }
                case 7: {
                    node.add(this._fromInt(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 9: {
                    node.add(nodeFactory.booleanNode(true));
                    continue block11;
                }
                case 10: {
                    node.add(nodeFactory.booleanNode(false));
                    continue block11;
                }
                case 11: {
                    node.add(nodeFactory.nullNode());
                    continue block11;
                }
            }
            node.add(this.deserializeAny(p, ctxt, nodeFactory));
        }
    }

    protected final JsonNode updateArray(JsonParser p, DeserializationContext ctxt, ArrayNode node) throws IOException {
        JsonNodeFactory nodeFactory = ctxt.getNodeFactory();
        block11: while (true) {
            JsonToken t = p.nextToken();
            switch (t.id()) {
                case 1: {
                    node.add(this.deserializeObject(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 3: {
                    node.add(this.deserializeArray(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 4: {
                    return node;
                }
                case 12: {
                    node.add(this._fromEmbedded(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 6: {
                    node.add(nodeFactory.textNode(p.getText()));
                    continue block11;
                }
                case 7: {
                    node.add(this._fromInt(p, ctxt, nodeFactory));
                    continue block11;
                }
                case 9: {
                    node.add(nodeFactory.booleanNode(true));
                    continue block11;
                }
                case 10: {
                    node.add(nodeFactory.booleanNode(false));
                    continue block11;
                }
                case 11: {
                    node.add(nodeFactory.nullNode());
                    continue block11;
                }
            }
            node.add(this.deserializeAny(p, ctxt, nodeFactory));
        }
    }

    protected final JsonNode deserializeAny(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        switch (p.currentTokenId()) {
            case 2: {
                return nodeFactory.objectNode();
            }
            case 5: {
                return this.deserializeObjectAtName(p, ctxt, nodeFactory);
            }
            case 12: {
                return this._fromEmbedded(p, ctxt, nodeFactory);
            }
            case 6: {
                return nodeFactory.textNode(p.getText());
            }
            case 7: {
                return this._fromInt(p, ctxt, nodeFactory);
            }
            case 8: {
                return this._fromFloat(p, ctxt, nodeFactory);
            }
            case 9: {
                return nodeFactory.booleanNode(true);
            }
            case 10: {
                return nodeFactory.booleanNode(false);
            }
            case 11: {
                return nodeFactory.nullNode();
            }
        }
        return (JsonNode)ctxt.handleUnexpectedToken(this.handledType(), p);
    }

    protected final JsonNode _fromInt(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        int feats = ctxt.getDeserializationFeatures();
        JsonParser.NumberType nt = (feats & F_MASK_INT_COERCIONS) != 0 ? (DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.enabledIn(feats) ? JsonParser.NumberType.BIG_INTEGER : (DeserializationFeature.USE_LONG_FOR_INTS.enabledIn(feats) ? JsonParser.NumberType.LONG : p.getNumberType())) : p.getNumberType();
        if (nt == JsonParser.NumberType.INT) {
            return nodeFactory.numberNode(p.getIntValue());
        }
        if (nt == JsonParser.NumberType.LONG) {
            return nodeFactory.numberNode(p.getLongValue());
        }
        return nodeFactory.numberNode(p.getBigIntegerValue());
    }

    protected final JsonNode _fromFloat(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        JsonParser.NumberType nt = p.getNumberType();
        if (nt == JsonParser.NumberType.BIG_DECIMAL) {
            return nodeFactory.numberNode(p.getDecimalValue());
        }
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            if (p.isNaN()) {
                return nodeFactory.numberNode(p.getDoubleValue());
            }
            return nodeFactory.numberNode(p.getDecimalValue());
        }
        if (nt == JsonParser.NumberType.FLOAT) {
            return nodeFactory.numberNode(p.getFloatValue());
        }
        return nodeFactory.numberNode(p.getDoubleValue());
    }

    protected final JsonNode _fromEmbedded(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        Object ob = p.getEmbeddedObject();
        if (ob == null) {
            return nodeFactory.nullNode();
        }
        Class<?> type = ob.getClass();
        if (type == byte[].class) {
            return nodeFactory.binaryNode((byte[])ob);
        }
        if (ob instanceof RawValue) {
            return nodeFactory.rawValueNode((RawValue)ob);
        }
        if (ob instanceof JsonNode) {
            return (JsonNode)ob;
        }
        return nodeFactory.pojoNode(ob);
    }
}

