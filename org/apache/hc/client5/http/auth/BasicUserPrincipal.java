/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.io.Serializable;
import java.security.Principal;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class BasicUserPrincipal
implements Principal,
Serializable {
    private static final long serialVersionUID = -2266305184969850467L;
    private final String username;

    public BasicUserPrincipal(String username) {
        Args.notNull(username, "User name");
        this.username = username;
    }

    @Override
    public String getName() {
        return this.username;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.username);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BasicUserPrincipal) {
            BasicUserPrincipal that = (BasicUserPrincipal)o;
            if (LangUtils.equals(this.username, that.username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[principal: ");
        buffer.append(this.username);
        buffer.append("]");
        return buffer.toString();
    }
}

