/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl.util;

import io.netty.handler.ssl.util.X509KeyManagerWrapper;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

public abstract class SimpleKeyManagerFactory
extends KeyManagerFactory {
    private static final Provider PROVIDER = new Provider("", 0.0, ""){
        private static final long serialVersionUID = -2680540247105807895L;
    };
    private static final FastThreadLocal<SimpleKeyManagerFactorySpi> CURRENT_SPI = new FastThreadLocal<SimpleKeyManagerFactorySpi>(){

        @Override
        protected SimpleKeyManagerFactorySpi initialValue() {
            return new SimpleKeyManagerFactorySpi();
        }
    };

    protected SimpleKeyManagerFactory() {
        this("");
    }

    protected SimpleKeyManagerFactory(String name) {
        super(CURRENT_SPI.get(), PROVIDER, ObjectUtil.checkNotNull(name, "name"));
        CURRENT_SPI.get().init(this);
        CURRENT_SPI.remove();
    }

    protected abstract void engineInit(KeyStore var1, char[] var2) throws Exception;

    protected abstract void engineInit(ManagerFactoryParameters var1) throws Exception;

    protected abstract KeyManager[] engineGetKeyManagers();

    private static final class SimpleKeyManagerFactorySpi
    extends KeyManagerFactorySpi {
        private SimpleKeyManagerFactory parent;
        private volatile KeyManager[] keyManagers;

        private SimpleKeyManagerFactorySpi() {
        }

        void init(SimpleKeyManagerFactory parent) {
            this.parent = parent;
        }

        @Override
        protected void engineInit(KeyStore keyStore, char[] pwd) throws KeyStoreException {
            try {
                this.parent.engineInit(keyStore, pwd);
            } catch (KeyStoreException e) {
                throw e;
            } catch (Exception e) {
                throw new KeyStoreException(e);
            }
        }

        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
            try {
                this.parent.engineInit(managerFactoryParameters);
            } catch (InvalidAlgorithmParameterException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidAlgorithmParameterException(e);
            }
        }

        @Override
        protected KeyManager[] engineGetKeyManagers() {
            KeyManager[] keyManagers = this.keyManagers;
            if (keyManagers == null) {
                keyManagers = this.parent.engineGetKeyManagers();
                if (PlatformDependent.javaVersion() >= 7) {
                    SimpleKeyManagerFactorySpi.wrapIfNeeded(keyManagers);
                }
                this.keyManagers = keyManagers;
            }
            return (KeyManager[])keyManagers.clone();
        }

        @SuppressJava6Requirement(reason="Usage guarded by java version check")
        private static void wrapIfNeeded(KeyManager[] keyManagers) {
            for (int i = 0; i < keyManagers.length; ++i) {
                KeyManager tm = keyManagers[i];
                if (!(tm instanceof X509KeyManager) || tm instanceof X509ExtendedKeyManager) continue;
                keyManagers[i] = new X509KeyManagerWrapper((X509KeyManager)tm);
            }
        }
    }
}

