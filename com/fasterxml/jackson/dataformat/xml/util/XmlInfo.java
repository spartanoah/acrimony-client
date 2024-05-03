/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.util;

public class XmlInfo {
    protected final String _namespace;
    protected final boolean _isAttribute;
    protected final boolean _isText;
    protected final boolean _isCData;

    public XmlInfo(Boolean isAttribute, String ns, Boolean isText, Boolean isCData) {
        this._isAttribute = isAttribute == null ? false : isAttribute;
        this._namespace = ns == null ? "" : ns;
        this._isText = isText == null ? false : isText;
        this._isCData = isCData == null ? false : isCData;
    }

    public String getNamespace() {
        return this._namespace;
    }

    public boolean isAttribute() {
        return this._isAttribute;
    }

    public boolean isText() {
        return this._isText;
    }

    public boolean isCData() {
        return this._isCData;
    }
}

