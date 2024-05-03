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
public class SPNegoScheme
extends GGSSchemeBase {
    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    public SPNegoScheme(KerberosConfig config, DnsResolver dnsResolver) {
        super(config, dnsResolver);
    }

    public SPNegoScheme() {
    }

    @Override
    public String getName() {
        return "Negotiate";
    }

    @Override
    protected byte[] generateToken(byte[] input, String serviceName, String authServer) throws GSSException {
        return this.generateGSSToken(input, new Oid(SPNEGO_OID), serviceName, authServer);
    }

    @Override
    public boolean isConnectionBased() {
        return true;
    }
}

