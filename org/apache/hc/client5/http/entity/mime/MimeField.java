/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.hc.core5.http.NameValuePair;

public class MimeField {
    private final String name;
    private final String value;
    private final List<NameValuePair> parameters;

    public MimeField(String name, String value) {
        this.name = name;
        this.value = value;
        this.parameters = Collections.emptyList();
    }

    public MimeField(String name, String value, List<NameValuePair> parameters) {
        this.name = name;
        this.value = value;
        this.parameters = parameters != null ? Collections.unmodifiableList(new ArrayList<NameValuePair>(parameters)) : Collections.emptyList();
    }

    public MimeField(MimeField from) {
        this(from.name, from.value, from.parameters);
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.value);
        for (int i = 0; i < this.parameters.size(); ++i) {
            NameValuePair parameter = this.parameters.get(i);
            sb.append("; ");
            sb.append(parameter.getName());
            sb.append("=\"");
            sb.append(parameter.getValue());
            sb.append("\"");
        }
        return sb.toString();
    }

    public List<NameValuePair> getParameters() {
        return this.parameters;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.name);
        buffer.append(": ");
        buffer.append(this.getBody());
        return buffer.toString();
    }
}

