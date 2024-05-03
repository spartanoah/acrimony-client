/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache.memcached;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hc.client5.http.impl.cache.memcached.KeyHashingScheme;
import org.apache.hc.client5.http.impl.cache.memcached.MemcachedKeyHashingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SHA256KeyHashingScheme
implements KeyHashingScheme {
    public static final SHA256KeyHashingScheme INSTANCE = new SHA256KeyHashingScheme();
    private static final Logger LOG = LoggerFactory.getLogger(SHA256KeyHashingScheme.class);

    @Override
    public String hash(String key) {
        MessageDigest md = this.getDigest();
        md.update(key.getBytes());
        return Hex.encodeHexString(md.digest());
    }

    private MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("can't find SHA-256 implementation for cache key hashing");
            throw new MemcachedKeyHashingException(nsae);
        }
    }
}

