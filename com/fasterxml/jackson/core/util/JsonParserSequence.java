/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParserSequence
extends JsonParserDelegate {
    protected final JsonParser[] _parsers;
    protected final boolean _checkForExistingToken;
    protected int _nextParserIndex;
    protected boolean _hasToken;

    @Deprecated
    protected JsonParserSequence(JsonParser[] parsers) {
        this(false, parsers);
    }

    protected JsonParserSequence(boolean checkForExistingToken, JsonParser[] parsers) {
        super(parsers[0]);
        this._checkForExistingToken = checkForExistingToken;
        this._hasToken = checkForExistingToken && this.delegate.hasCurrentToken();
        this._parsers = parsers;
        this._nextParserIndex = 1;
    }

    public static JsonParserSequence createFlattened(boolean checkForExistingToken, JsonParser first, JsonParser second) {
        if (!(first instanceof JsonParserSequence) && !(second instanceof JsonParserSequence)) {
            return new JsonParserSequence(checkForExistingToken, new JsonParser[]{first, second});
        }
        ArrayList<JsonParser> p = new ArrayList<JsonParser>();
        if (first instanceof JsonParserSequence) {
            ((JsonParserSequence)first).addFlattenedActiveParsers(p);
        } else {
            p.add(first);
        }
        if (second instanceof JsonParserSequence) {
            ((JsonParserSequence)second).addFlattenedActiveParsers(p);
        } else {
            p.add(second);
        }
        return new JsonParserSequence(checkForExistingToken, p.toArray(new JsonParser[p.size()]));
    }

    @Deprecated
    public static JsonParserSequence createFlattened(JsonParser first, JsonParser second) {
        return JsonParserSequence.createFlattened(false, first, second);
    }

    protected void addFlattenedActiveParsers(List<JsonParser> listToAddIn) {
        int len = this._parsers.length;
        for (int i = this._nextParserIndex - 1; i < len; ++i) {
            JsonParser p = this._parsers[i];
            if (p instanceof JsonParserSequence) {
                ((JsonParserSequence)p).addFlattenedActiveParsers(listToAddIn);
                continue;
            }
            listToAddIn.add(p);
        }
    }

    @Override
    public void close() throws IOException {
        do {
            this.delegate.close();
        } while (this.switchToNext());
    }

    @Override
    public JsonToken nextToken() throws IOException {
        if (this.delegate == null) {
            return null;
        }
        if (this._hasToken) {
            this._hasToken = false;
            return this.delegate.currentToken();
        }
        JsonToken t = this.delegate.nextToken();
        if (t == null) {
            return this.switchAndReturnNext();
        }
        return t;
    }

    @Override
    public JsonParser skipChildren() throws IOException {
        if (this.delegate.currentToken() != JsonToken.START_OBJECT && this.delegate.currentToken() != JsonToken.START_ARRAY) {
            return this;
        }
        int open = 1;
        while (true) {
            JsonToken t;
            if ((t = this.nextToken()) == null) {
                return this;
            }
            if (t.isStructStart()) {
                ++open;
                continue;
            }
            if (t.isStructEnd() && --open == 0) break;
        }
        return this;
    }

    public int containedParsersCount() {
        return this._parsers.length;
    }

    protected boolean switchToNext() {
        if (this._nextParserIndex < this._parsers.length) {
            this.delegate = this._parsers[this._nextParserIndex++];
            return true;
        }
        return false;
    }

    protected JsonToken switchAndReturnNext() throws IOException {
        while (this._nextParserIndex < this._parsers.length) {
            this.delegate = this._parsers[this._nextParserIndex++];
            if (this._checkForExistingToken && this.delegate.hasCurrentToken()) {
                return this.delegate.getCurrentToken();
            }
            JsonToken t = this.delegate.nextToken();
            if (t == null) continue;
            return t;
        }
        return null;
    }
}

