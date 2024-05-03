/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import java.io.IOException;

public abstract class TreeCodec {
    public abstract <T extends TreeNode> T readTree(JsonParser var1) throws IOException, JsonProcessingException;

    public abstract void writeTree(JsonGenerator var1, TreeNode var2) throws IOException, JsonProcessingException;

    public TreeNode missingNode() {
        return null;
    }

    public TreeNode nullNode() {
        return null;
    }

    public abstract TreeNode createArrayNode();

    public abstract TreeNode createObjectNode();

    public abstract JsonParser treeAsTokens(TreeNode var1);
}

