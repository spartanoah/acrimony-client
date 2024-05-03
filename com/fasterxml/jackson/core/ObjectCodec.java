/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Iterator;

public abstract class ObjectCodec
extends TreeCodec
implements Versioned {
    protected ObjectCodec() {
    }

    @Override
    public abstract Version version();

    public abstract <T> T readValue(JsonParser var1, Class<T> var2) throws IOException;

    public abstract <T> T readValue(JsonParser var1, TypeReference<T> var2) throws IOException;

    public abstract <T> T readValue(JsonParser var1, ResolvedType var2) throws IOException;

    public abstract <T> Iterator<T> readValues(JsonParser var1, Class<T> var2) throws IOException;

    public abstract <T> Iterator<T> readValues(JsonParser var1, TypeReference<T> var2) throws IOException;

    public abstract <T> Iterator<T> readValues(JsonParser var1, ResolvedType var2) throws IOException;

    public abstract void writeValue(JsonGenerator var1, Object var2) throws IOException;

    @Override
    public abstract <T extends TreeNode> T readTree(JsonParser var1) throws IOException;

    @Override
    public abstract void writeTree(JsonGenerator var1, TreeNode var2) throws IOException;

    @Override
    public abstract TreeNode createObjectNode();

    @Override
    public abstract TreeNode createArrayNode();

    @Override
    public abstract JsonParser treeAsTokens(TreeNode var1);

    public abstract <T> T treeToValue(TreeNode var1, Class<T> var2) throws JsonProcessingException;

    @Deprecated
    public JsonFactory getJsonFactory() {
        return this.getFactory();
    }

    public JsonFactory getFactory() {
        return this.getJsonFactory();
    }
}

