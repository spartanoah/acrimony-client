/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import java.util.Iterator;

public interface TreeNode {
    public JsonToken asToken();

    public JsonParser.NumberType numberType();

    public int size();

    public boolean isValueNode();

    public boolean isContainerNode();

    public boolean isMissingNode();

    public boolean isArray();

    public boolean isObject();

    public TreeNode get(String var1);

    public TreeNode get(int var1);

    public TreeNode path(String var1);

    public TreeNode path(int var1);

    public Iterator<String> fieldNames();

    public TreeNode at(JsonPointer var1);

    public TreeNode at(String var1) throws IllegalArgumentException;

    public JsonParser traverse();

    public JsonParser traverse(ObjectCodec var1);
}

