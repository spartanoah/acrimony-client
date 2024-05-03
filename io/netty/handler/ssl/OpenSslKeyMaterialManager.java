/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslKeyMaterial;
import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;

final class OpenSslKeyMaterialManager {
    static final String KEY_TYPE_RSA = "RSA";
    static final String KEY_TYPE_DH_RSA = "DH_RSA";
    static final String KEY_TYPE_EC = "EC";
    static final String KEY_TYPE_EC_EC = "EC_EC";
    static final String KEY_TYPE_EC_RSA = "EC_RSA";
    private static final Map<String, String> KEY_TYPES = new HashMap<String, String>();
    private final OpenSslKeyMaterialProvider provider;

    OpenSslKeyMaterialManager(OpenSslKeyMaterialProvider provider) {
        this.provider = provider;
    }

    void setKeyMaterialServerSide(ReferenceCountedOpenSslEngine engine) throws SSLException {
        Object[] authMethods = engine.authMethods();
        if (authMethods.length == 0) {
            throw new SSLHandshakeException("Unable to find key material");
        }
        HashSet<String> typeSet = new HashSet<String>(KEY_TYPES.size());
        for (String string : authMethods) {
            String alias;
            String type = KEY_TYPES.get(string);
            if (type == null || !typeSet.add(type) || (alias = this.chooseServerAlias(engine, type)) == null) continue;
            this.setKeyMaterial(engine, alias);
            return;
        }
        throw new SSLHandshakeException("Unable to find key material for auth method(s): " + Arrays.toString(authMethods));
    }

    void setKeyMaterialClientSide(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer) throws SSLException {
        String alias = this.chooseClientAlias(engine, keyTypes, issuer);
        if (alias != null) {
            this.setKeyMaterial(engine, alias);
        }
    }

    private void setKeyMaterial(ReferenceCountedOpenSslEngine engine, String alias) throws SSLException {
        OpenSslKeyMaterial keyMaterial = null;
        try {
            keyMaterial = this.provider.chooseKeyMaterial(engine.alloc, alias);
            if (keyMaterial == null) {
                return;
            }
            engine.setKeyMaterial(keyMaterial);
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        } finally {
            if (keyMaterial != null) {
                keyMaterial.release();
            }
        }
    }

    private String chooseClientAlias(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer) {
        X509KeyManager manager = this.provider.keyManager();
        if (manager instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager)manager).chooseEngineClientAlias(keyTypes, issuer, engine);
        }
        return manager.chooseClientAlias(keyTypes, issuer, null);
    }

    private String chooseServerAlias(ReferenceCountedOpenSslEngine engine, String type) {
        X509KeyManager manager = this.provider.keyManager();
        if (manager instanceof X509ExtendedKeyManager) {
            return ((X509ExtendedKeyManager)manager).chooseEngineServerAlias(type, null, engine);
        }
        return manager.chooseServerAlias(type, null, null);
    }

    static {
        KEY_TYPES.put(KEY_TYPE_RSA, KEY_TYPE_RSA);
        KEY_TYPES.put("DHE_RSA", KEY_TYPE_RSA);
        KEY_TYPES.put("ECDHE_RSA", KEY_TYPE_RSA);
        KEY_TYPES.put("ECDHE_ECDSA", KEY_TYPE_EC);
        KEY_TYPES.put("ECDH_RSA", KEY_TYPE_EC_RSA);
        KEY_TYPES.put("ECDH_ECDSA", KEY_TYPE_EC_EC);
        KEY_TYPES.put(KEY_TYPE_DH_RSA, KEY_TYPE_DH_RSA);
    }
}

