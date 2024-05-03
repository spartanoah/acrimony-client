/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface JsonNodeCreator {
    public ValueNode booleanNode(boolean var1);

    public ValueNode nullNode();

    public ValueNode numberNode(byte var1);

    public ValueNode numberNode(Byte var1);

    public ValueNode numberNode(short var1);

    public ValueNode numberNode(Short var1);

    public ValueNode numberNode(int var1);

    public ValueNode numberNode(Integer var1);

    public ValueNode numberNode(long var1);

    public ValueNode numberNode(Long var1);

    public ValueNode numberNode(BigInteger var1);

    public ValueNode numberNode(float var1);

    public ValueNode numberNode(Float var1);

    public ValueNode numberNode(double var1);

    public ValueNode numberNode(Double var1);

    public ValueNode numberNode(BigDecimal var1);

    public ValueNode textNode(String var1);

    public ValueNode binaryNode(byte[] var1);

    public ValueNode binaryNode(byte[] var1, int var2, int var3);

    public ValueNode pojoNode(Object var1);

    public ValueNode rawValueNode(RawValue var1);

    public ArrayNode arrayNode();

    public ArrayNode arrayNode(int var1);

    public ObjectNode objectNode();
}

