/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.auth.KerberosConfig;
import org.apache.hc.client5.http.impl.auth.GGSSchemeBase;
import org.apache.hc.core5.annotation.Experimental;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

@Experimental
public class KerberosScheme
extends GGSSchemeBase {
    private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";

    public KerberosScheme(KerberosConfig config, DnsResolver dnsResolver) {
        super(config, dnsResolver);
    }

    public KerberosScheme() {
    }

    @Override
    public String getName() {
        return "Kerberos";
    }

    @Override
    protected byte[] generateToken(byte[] input, String serviceName, String authServer) throws GSSException {
        return this.generateGSSToken(input, new Oid(KERBEROS_OID), serviceName, authServer);
    }

    @Override
    public boolean isConnectionBased() {
        return true;
    }
}

