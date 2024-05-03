/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.io.CharTypes;

public abstract class JsonStreamContext {
    protected static final int TYPE_ROOT = 0;
    protected static final int TYPE_ARRAY = 1;
    protected static final int TYPE_OBJECT = 2;
    protected int _type;
    protected int _index;

    protected JsonStreamContext() {
    }

    protected JsonStreamContext(JsonStreamContext base) {
        this._type = base._type;
        this._index = base._index;
    }

    protected JsonStreamContext(int type, int index) {
        this._type = type;
        this._index = index;
    }

    public abstract JsonStreamContext getParent();

    public final boolean inArray() {
        return this._type == 1;
    }

    public final boolean inRoot() {
        return this._type == 0;
    }

    public final boolean inObject() {
        return this._type == 2;
    }

    @Deprecated
    public final String getTypeDesc() {
        switch (this._type) {
            case 0: {
                return "ROOT";
            }
            case 1: {
                return "ARRAY";
            }
            case 2: {
                return "OBJECT";
            }
        }
        return "?";
    }

    public String typeDesc() {
        switch (this._type) {
            case 0: {
                return "root";
            }
            case 1: {
                return "Array";
            }
            case 2: {
                return "Object";
            }
        }
        return "?";
    }

    public final int getEntryCount() {
        return this._index + 1;
    }

    public final int getCurrentIndex() {
        return this._index < 0 ? 0 : this._index;
    }

    public boolean hasCurrentIndex() {
        return this._index >= 0;
    }

    public boolean hasPathSegment() {
        if (this._type == 2) {
            return this.hasCurrentName();
        }
        if (this._type == 1) {
            return this.hasCurrentIndex();
        }
        return false;
    }

    public abstract String getCurrentName();

    public boolean hasCurrentName() {
        return this.getCurrentName() != null;
    }

    public Object getCurrentValue() {
        return null;
    }

    public void setCurrentValue(Object v) {
    }

    public JsonPointer pathAsPointer() {
        return JsonPointer.forPath(this, false);
    }

    public JsonPointer pathAsPointer(boolean includeRoot) {
        return JsonPointer.forPath(this, includeRoot);
    }

    public JsonLocation getStartLocation(Object srcRef) {
        return JsonLocation.NA;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        switch (this._type) {
            case 0: {
                sb.append("/");
                break;
            }
            case 1: {
                sb.append('[');
                sb.append(this.getCurrentIndex());
                sb.append(']');
                break;
            }
            default: {
                sb.append('{');
                String currentName = this.getCurrentName();
                if (currentName != null) {
                    sb.append('\"');
                    CharTypes.appendQuoted(sb, currentName);
                    sb.append('\"');
                } else {
                    sb.append('?');
                }
                sb.append('}');
            }
        }
        return sb.toString();
    }
}

