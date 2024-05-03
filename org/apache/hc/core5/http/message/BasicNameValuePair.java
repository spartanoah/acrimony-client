/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.Serializable;
import java.util.Locale;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicNameValuePair
implements NameValuePair,
Serializable {
    private static final long serialVersionUID = -6437800749411518984L;
    private final String name;
    private final String value;

    public BasicNameValuePair(String name, String value) {
        this.name = Args.notNull(name, "Name");
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String toString() {
        if (this.value == null) {
            return this.name;
        }
        int len = this.name.length() + 1 + this.value.length();
        StringBuilder buffer = new StringBuilder(len);
        buffer.append(this.name);
        buffer.append("=");
        buffer.append(this.value);
        return buffer.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BasicNameValuePair) {
            BasicNameValuePair that = (BasicNameValuePair)obj;
            return this.name.equalsIgnoreCase(that.name) && LangUtils.equals(this.value, that.value);
        }
        return false;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.name.toLowerCase(Locale.ROOT));
        hash = LangUtils.hashCode(hash, this.value);
        return hash;
    }
}

