/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.gson.internal.bind;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.libs.gson.stream.JsonToken;
import com.viaversion.viaversion.libs.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public final class JsonTreeReader
extends JsonReader {
    private static final Reader UNREADABLE_READER = new Reader(){

        @Override
        public int read(char[] buffer, int offset, int count) {
            throw new AssertionError();
        }

        @Override
        public void close() {
            throw new AssertionError();
        }
    };
    private static final Object SENTINEL_CLOSED = new Object();
    private Object[] stack = new Object[32];
    private int stackSize = 0;
    private String[] pathNames = new String[32];
    private int[] pathIndices = new int[32];

    public JsonTreeReader(JsonElement element) {
        super(UNREADABLE_READER);
        this.push(element);
    }

    @Override
    public void beginArray() throws IOException {
        this.expect(JsonToken.BEGIN_ARRAY);
        JsonArray array = (JsonArray)this.peekStack();
        this.push(array.iterator());
        this.pathIndices[this.stackSize - 1] = 0;
    }

    @Override
    public void endArray() throws IOException {
        this.expect(JsonToken.END_ARRAY);
        this.popStack();
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
    }

    @Override
    public void beginObject() throws IOException {
        this.expect(JsonToken.BEGIN_OBJECT);
        JsonObject object = (JsonObject)this.peekStack();
        this.push(object.entrySet().iterator());
    }

    @Override
    public void endObject() throws IOException {
        this.expect(JsonToken.END_OBJECT);
        this.pathNames[this.stackSize - 1] = null;
        this.popStack();
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        JsonToken token = this.peek();
        return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY && token != JsonToken.END_DOCUMENT;
    }

    @Override
    public JsonToken peek() throws IOException {
        if (this.stackSize == 0) {
            return JsonToken.END_DOCUMENT;
        }
        Object o = this.peekStack();
        if (o instanceof Iterator) {
            boolean isObject = this.stack[this.stackSize - 2] instanceof JsonObject;
            Iterator iterator = (Iterator)o;
            if (iterator.hasNext()) {
                if (isObject) {
                    return JsonToken.NAME;
                }
                this.push(iterator.next());
                return this.peek();
            }
            return isObject ? JsonToken.END_OBJECT : JsonToken.END_ARRAY;
        }
        if (o instanceof JsonObject) {
            return JsonToken.BEGIN_OBJECT;
        }
        if (o instanceof JsonArray) {
            return JsonToken.BEGIN_ARRAY;
        }
        if (o instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive)o;
            if (primitive.isString()) {
                return JsonToken.STRING;
            }
            if (primitive.isBoolean()) {
                return JsonToken.BOOLEAN;
            }
            if (primitive.isNumber()) {
                return JsonToken.NUMBER;
            }
            throw new AssertionError();
        }
        if (o instanceof JsonNull) {
            return JsonToken.NULL;
        }
        if (o == SENTINEL_CLOSED) {
            throw new IllegalStateException("JsonReader is closed");
        }
        throw new MalformedJsonException("Custom JsonElement subclass " + o.getClass().getName() + " is not supported");
    }

    private Object peekStack() {
        return this.stack[this.stackSize - 1];
    }

    private Object popStack() {
        Object result = this.stack[--this.stackSize];
        this.stack[this.stackSize] = null;
        return result;
    }

    private void expect(JsonToken expected) throws IOException {
        if (this.peek() != expected) {
            throw new IllegalStateException("Expected " + (Object)((Object)expected) + " but was " + (Object)((Object)this.peek()) + this.locationString());
        }
    }

    private String nextName(boolean skipName) throws IOException {
        this.expect(JsonToken.NAME);
        Iterator i = (Iterator)this.peekStack();
        Map.Entry entry = (Map.Entry)i.next();
        String result = (String)entry.getKey();
        this.pathNames[this.stackSize - 1] = skipName ? "<skipped>" : result;
        this.push(entry.getValue());
        return result;
    }

    @Override
    public String nextName() throws IOException {
        return this.nextName(false);
    }

    @Override
    public String nextString() throws IOException {
        JsonToken token = this.peek();
        if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
            throw new IllegalStateException("Expected " + (Object)((Object)JsonToken.STRING) + " but was " + (Object)((Object)token) + this.locationString());
        }
        String result = ((JsonPrimitive)this.popStack()).getAsString();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
        return result;
    }

    @Override
    public boolean nextBoolean() throws IOException {
        this.expect(JsonToken.BOOLEAN);
        boolean result = ((JsonPrimitive)this.popStack()).getAsBoolean();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
        return result;
    }

    @Override
    public void nextNull() throws IOException {
        this.expect(JsonToken.NULL);
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
    }

    @Override
    public double nextDouble() throws IOException {
        JsonToken token = this.peek();
        if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
            throw new IllegalStateException("Expected " + (Object)((Object)JsonToken.NUMBER) + " but was " + (Object)((Object)token) + this.locationString());
        }
        double result = ((JsonPrimitive)this.peekStack()).getAsDouble();
        if (!this.isLenient() && (Double.isNaN(result) || Double.isInfinite(result))) {
            throw new MalformedJsonException("JSON forbids NaN and infinities: " + result);
        }
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
        return result;
    }

    @Override
    public long nextLong() throws IOException {
        JsonToken token = this.peek();
        if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
            throw new IllegalStateException("Expected " + (Object)((Object)JsonToken.NUMBER) + " but was " + (Object)((Object)token) + this.locationString());
        }
        long result = ((JsonPrimitive)this.peekStack()).getAsLong();
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
        return result;
    }

    @Override
    public int nextInt() throws IOException {
        JsonToken token = this.peek();
        if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
            throw new IllegalStateException("Expected " + (Object)((Object)JsonToken.NUMBER) + " but was " + (Object)((Object)token) + this.locationString());
        }
        int result = ((JsonPrimitive)this.peekStack()).getAsInt();
        this.popStack();
        if (this.stackSize > 0) {
            int n = this.stackSize - 1;
            this.pathIndices[n] = this.pathIndices[n] + 1;
        }
        return result;
    }

    JsonElement nextJsonElement() throws IOException {
        JsonToken peeked = this.peek();
        if (peeked == JsonToken.NAME || peeked == JsonToken.END_ARRAY || peeked == JsonToken.END_OBJECT || peeked == JsonToken.END_DOCUMENT) {
            throw new IllegalStateException("Unexpected " + (Object)((Object)peeked) + " when reading a JsonElement.");
        }
        JsonElement element = (JsonElement)this.peekStack();
        this.skipValue();
        return element;
    }

    @Override
    public void close() throws IOException {
        this.stack = new Object[]{SENTINEL_CLOSED};
        this.stackSize = 1;
    }

    @Override
    public void skipValue() throws IOException {
        JsonToken peeked = this.peek();
        switch (peeked) {
            case NAME: {
                String unused = this.nextName(true);
                break;
            }
            case END_ARRAY: {
                this.endArray();
                break;
            }
            case END_OBJECT: {
                this.endObject();
                break;
            }
            case END_DOCUMENT: {
                break;
            }
            default: {
                this.popStack();
                if (this.stackSize <= 0) break;
                int n = this.stackSize - 1;
                this.pathIndices[n] = this.pathIndices[n] + 1;
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + this.locationString();
    }

    public void promoteNameToValue() throws IOException {
        this.expect(JsonToken.NAME);
        Iterator i = (Iterator)this.peekStack();
        Map.Entry entry = (Map.Entry)i.next();
        this.push(entry.getValue());
        this.push(new JsonPrimitive((String)entry.getKey()));
    }

    private void push(Object newTop) {
        if (this.stackSize == this.stack.length) {
            int newLength = this.stackSize * 2;
            this.stack = Arrays.copyOf(this.stack, newLength);
            this.pathIndices = Arrays.copyOf(this.pathIndices, newLength);
            this.pathNames = Arrays.copyOf(this.pathNames, newLength);
        }
        this.stack[this.stackSize++] = newTop;
    }

    private String getPath(boolean usePreviousPath) {
        StringBuilder result = new StringBuilder().append('$');
        for (int i = 0; i < this.stackSize; ++i) {
            if (this.stack[i] instanceof JsonArray) {
                if (++i >= this.stackSize || !(this.stack[i] instanceof Iterator)) continue;
                int pathIndex = this.pathIndices[i];
                if (usePreviousPath && pathIndex > 0 && (i == this.stackSize - 1 || i == this.stackSize - 2)) {
                    --pathIndex;
                }
                result.append('[').append(pathIndex).append(']');
                continue;
            }
            if (!(this.stack[i] instanceof JsonObject) || ++i >= this.stackSize || !(this.stack[i] instanceof Iterator)) continue;
            result.append('.');
            if (this.pathNames[i] == null) continue;
            result.append(this.pathNames[i]);
        }
        return result.toString();
    }

    @Override
    public String getPreviousPath() {
        return this.getPath(true);
    }

    @Override
    public String getPath() {
        return this.getPath(false);
    }

    private String locationString() {
        return " at path " + this.getPath();
    }
}

