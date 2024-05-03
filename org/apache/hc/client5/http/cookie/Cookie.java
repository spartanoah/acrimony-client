/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cookie;

import java.util.Date;

public interface Cookie {
    public static final String PATH_ATTR = "path";
    public static final String DOMAIN_ATTR = "domain";
    public static final String MAX_AGE_ATTR = "max-age";
    public static final String SECURE_ATTR = "secure";
    public static final String EXPIRES_ATTR = "expires";

    public String getAttribute(String var1);

    public boolean containsAttribute(String var1);

    public String getName();

    public String getValue();

    public Date getExpiryDate();

    public boolean isPersistent();

    public String getDomain();

    public String getPath();

    public boolean isSecure();

    public boolean isExpired(Date var1);

    public Date getCreationDate();
}

