/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.Args;

public class BasicHeaderElement
implements HeaderElement {
    private static final NameValuePair[] EMPTY_NAME_VALUE_PAIR_ARRAY = new NameValuePair[0];
    private final String name;
    private final String value;
    private final NameValuePair[] parameters;

    public BasicHeaderElement(String name, String value, NameValuePair[] parameters) {
        this.name = Args.notNull(name, "Name");
        this.value = value;
        this.parameters = parameters != null ? parameters : EMPTY_NAME_VALUE_PAIR_ARRAY;
    }

    public BasicHeaderElement(String name, String value) {
        this(name, value, null);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public NameValuePair[] getParameters() {
        return (NameValuePair[])this.parameters.clone();
    }

    @Override
    public int getParameterCount() {
        return this.parameters.length;
    }

    @Override
    public NameValuePair getParameter(int index) {
        return this.parameters[index];
    }

    @Override
    public NameValuePair getParameterByName(String name) {
        Args.notNull(name, "Name");
        NameValuePair found = null;
        for (NameValuePair current : this.parameters) {
            if (!current.getName().equalsIgnoreCase(name)) continue;
            found = current;
            break;
        }
        return found;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.name);
        if (this.value != null) {
            buffer.append("=");
            buffer.append(this.value);
        }
        for (NameValuePair parameter : this.parameters) {
            buffer.append("; ");
            buffer.append(parameter);
        }
        return buffer.toString();
    }
}

