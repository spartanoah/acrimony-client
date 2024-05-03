/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.Serializable;
import java.util.Objects;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicHeader
implements Header,
Cloneable,
Serializable {
    private static final long serialVersionUID = -5427236326487562174L;
    private final String name;
    private final boolean sensitive;
    private final String value;

    public BasicHeader(String name, Object value) {
        this(name, value, false);
    }

    public BasicHeader(String name, Object value, boolean sensitive) {
        this.name = Args.notNull(name, "Name");
        this.value = Objects.toString(value, null);
        this.sensitive = sensitive;
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
    public boolean isSensitive() {
        return this.sensitive;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getName()).append(": ");
        if (this.getValue() != null) {
            buf.append(this.getValue());
        }
        return buf.toString();
    }
}

