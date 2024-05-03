/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import java.util.HashSet;

public class DupDetector {
    protected final Object _source;
    protected String _firstName;
    protected String _secondName;
    protected HashSet<String> _seen;

    private DupDetector(Object src) {
        this._source = src;
    }

    public static DupDetector rootDetector(JsonParser p) {
        return new DupDetector(p);
    }

    public static DupDetector rootDetector(JsonGenerator g) {
        return new DupDetector(g);
    }

    public DupDetector child() {
        return new DupDetector(this._source);
    }

    public void reset() {
        this._firstName = null;
        this._secondName = null;
        this._seen = null;
    }

    public JsonLocation findLocation() {
        if (this._source instanceof JsonParser) {
            return ((JsonParser)this._source).getCurrentLocation();
        }
        return null;
    }

    public Object getSource() {
        return this._source;
    }

    public boolean isDup(String name) throws JsonParseException {
        if (this._firstName == null) {
            this._firstName = name;
            return false;
        }
        if (name.equals(this._firstName)) {
            return true;
        }
        if (this._secondName == null) {
            this._secondName = name;
            return false;
        }
        if (name.equals(this._secondName)) {
            return true;
        }
        if (this._seen == null) {
            this._seen = new HashSet(16);
            this._seen.add(this._firstName);
            this._seen.add(this._secondName);
        }
        return !this._seen.add(name);
    }
}

