/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.Locale;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.NTUserPrincipal;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class NTCredentials
implements Credentials,
Serializable {
    private static final long serialVersionUID = -7385699315228907265L;
    private final NTUserPrincipal principal;
    private final char[] password;
    private final String workstation;
    private final String netbiosDomain;

    public NTCredentials(String userName, char[] password, String workstation, String domain) {
        this(userName, password, NTCredentials.convertHost(workstation), domain, NTCredentials.convertDomain(domain));
    }

    public NTCredentials(String userName, char[] password, String workstation, String domain, String netbiosDomain) {
        Args.notNull(userName, "User name");
        this.principal = new NTUserPrincipal(domain, userName);
        this.password = password;
        this.workstation = workstation != null ? workstation.toUpperCase(Locale.ROOT) : null;
        this.netbiosDomain = netbiosDomain;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getUserName() {
        return this.principal.getUsername();
    }

    @Override
    public char[] getPassword() {
        return this.password;
    }

    public String getDomain() {
        return this.principal.getDomain();
    }

    public String getNetbiosDomain() {
        return this.netbiosDomain;
    }

    public String getWorkstation() {
        return this.workstation;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.principal);
        hash = LangUtils.hashCode(hash, this.workstation);
        hash = LangUtils.hashCode(hash, this.netbiosDomain);
        return hash;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NTCredentials) {
            NTCredentials that = (NTCredentials)o;
            if (LangUtils.equals(this.principal, that.principal) && LangUtils.equals(this.workstation, that.workstation) && LangUtils.equals(this.netbiosDomain, that.netbiosDomain)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[principal: ");
        buffer.append(this.principal);
        buffer.append("][workstation: ");
        buffer.append(this.workstation);
        buffer.append("][netbiosDomain: ");
        buffer.append(this.netbiosDomain);
        buffer.append("]");
        return buffer.toString();
    }

    private static String stripDotSuffix(String value) {
        if (value == null) {
            return null;
        }
        int index = value.indexOf(46);
        if (index != -1) {
            return value.substring(0, index);
        }
        return value;
    }

    private static String convertHost(String host) {
        return NTCredentials.stripDotSuffix(host);
    }

    private static String convertDomain(String domain) {
        String returnString = NTCredentials.stripDotSuffix(domain);
        return returnString == null ? returnString : returnString.toUpperCase(Locale.ROOT);
    }
}

