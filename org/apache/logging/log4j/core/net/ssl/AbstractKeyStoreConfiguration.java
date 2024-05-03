/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationDefaults;
import org.apache.logging.log4j.core.net.ssl.StoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;
import org.apache.logging.log4j.core.util.NetUtils;

public class AbstractKeyStoreConfiguration
extends StoreConfiguration<KeyStore> {
    static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();
    private final KeyStore keyStore;
    private final String keyStoreType;

    public AbstractKeyStoreConfiguration(String location, PasswordProvider passwordProvider, String keyStoreType) throws StoreConfigurationException {
        super(location, passwordProvider);
        this.keyStoreType = keyStoreType == null ? SslConfigurationDefaults.KEYSTORE_TYPE : keyStoreType;
        this.keyStore = this.load();
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String location, char[] password, String keyStoreType) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password), keyStoreType);
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String location, String password, String keyStoreType) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password == null ? null : password.toCharArray()), keyStoreType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected KeyStore load() throws StoreConfigurationException {
        String loadLocation = this.getLocation();
        LOGGER.debug("Loading keystore from location {}", (Object)loadLocation);
        try {
            if (loadLocation == null) {
                throw new IOException("The location is null");
            }
            try (InputStream fin = this.openInputStream(loadLocation);){
                KeyStore ks = KeyStore.getInstance(this.keyStoreType);
                char[] password = this.getPasswordAsCharArray();
                try {
                    ks.load(fin, password != null ? password : DEFAULT_PASSWORD);
                } finally {
                    if (password != null) {
                        Arrays.fill(password, '\u0000');
                    }
                }
                LOGGER.debug("KeyStore successfully loaded from location {}", (Object)loadLocation);
                KeyStore keyStore = ks;
                return keyStore;
            }
        } catch (CertificateException e) {
            LOGGER.error("No Provider supports a KeyStoreSpi implementation for the specified type {} for location {}", (Object)this.keyStoreType, (Object)loadLocation, (Object)e);
            throw new StoreConfigurationException(loadLocation, e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("The algorithm used to check the integrity of the keystore cannot be found for location {}", (Object)loadLocation, (Object)e);
            throw new StoreConfigurationException(loadLocation, e);
        } catch (KeyStoreException e) {
            LOGGER.error("KeyStoreException for location {}", (Object)loadLocation, (Object)e);
            throw new StoreConfigurationException(loadLocation, e);
        } catch (FileNotFoundException e) {
            LOGGER.error("The keystore file {} is not found", (Object)loadLocation, (Object)e);
            throw new StoreConfigurationException(loadLocation, e);
        } catch (IOException e) {
            LOGGER.error("Something is wrong with the format of the keystore or the given password for location {}", (Object)loadLocation, (Object)e);
            throw new StoreConfigurationException(loadLocation, e);
        }
    }

    private InputStream openInputStream(String filePathOrUri) {
        return ConfigurationSource.fromUri(NetUtils.toURI(filePathOrUri)).getInputStream();
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.keyStore == null ? 0 : this.keyStore.hashCode());
        result = 31 * result + (this.keyStoreType == null ? 0 : this.keyStoreType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AbstractKeyStoreConfiguration other = (AbstractKeyStoreConfiguration)obj;
        if (!Objects.equals(this.keyStore, other.keyStore)) {
            return false;
        }
        return Objects.equals(this.keyStoreType, other.keyStoreType);
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }
}

