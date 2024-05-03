/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;

final class InternalNodeMapper {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();
    private static final ObjectWriter STD_WRITER = JSON_MAPPER.writer();
    private static final ObjectWriter PRETTY_WRITER = JSON_MAPPER.writer().withDefaultPrettyPrinter();
    private static final ObjectReader NODE_READER = JSON_MAPPER.readerFor(JsonNode.class);

    InternalNodeMapper() {
    }

    public static String nodeToString(JsonNode n) {
        try {
            return STD_WRITER.writeValueAsString(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String nodeToPrettyString(JsonNode n) {
        try {
            return PRETTY_WRITER.writeValueAsString(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] valueToBytes(Object value) throws IOException {
        return JSON_MAPPER.writeValueAsBytes(value);
    }

    public static JsonNode bytesToNode(byte[] json) throws IOException {
        return (JsonNode)NODE_READER.readValue(json);
    }
}

