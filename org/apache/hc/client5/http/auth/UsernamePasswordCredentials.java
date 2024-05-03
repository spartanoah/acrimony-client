/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.io.Serializable;
import java.security.Principal;
import org.apache.hc.client5.http.auth.BasicUserPrincipal;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class UsernamePasswordCredentials
implements Credentials,
Serializable {
    private static final long serialVersionUID = 243343858802739403L;
    private final BasicUserPrincipal principal;
    private final char[] password;

    public UsernamePasswordCredentials(String userName, char[] password) {
        Args.notNull(userName, "Username");
        this.principal = new BasicUserPrincipal(userName);
        this.password = password;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getUserName() {
        return this.principal.getName();
    }

    @Override
    public char[] getPassword() {
        return this.password;
    }

    public int hashCode() {
        return this.principal.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials that = (UsernamePasswordCredentials)o;
            if (LangUtils.equals(this.principal, that.principal)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.principal.toString();
    }
}

