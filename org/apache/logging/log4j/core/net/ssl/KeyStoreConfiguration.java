/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net.ssl;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.Objects;
import javax.net.ssl.KeyManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.AbstractKeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.EnvironmentPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.FilePasswordProvider;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;

@Plugin(name="KeyStore", category="Core", printObject=true)
public class KeyStoreConfiguration
extends AbstractKeyStoreConfiguration {
    private final String keyManagerFactoryAlgorithm;

    public KeyStoreConfiguration(String location, PasswordProvider passwordProvider, String keyStoreType, String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        super(location, passwordProvider, keyStoreType);
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm == null ? KeyManagerFactory.getDefaultAlgorithm() : keyManagerFactoryAlgorithm;
    }

    @Deprecated
    public KeyStoreConfiguration(String location, char[] password, String keyStoreType, String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password), keyStoreType, keyManagerFactoryAlgorithm);
        if (password != null) {
            Arrays.fill(password, '\u0000');
        }
    }

    @Deprecated
    public KeyStoreConfiguration(String location, String password, String keyStoreType, String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password == null ? null : password.toCharArray()), keyStoreType, keyManagerFactoryAlgorithm);
    }

    @PluginFactory
    public static KeyStoreConfiguration createKeyStoreConfiguration(@PluginAttribute(value="location") String location, @PluginAttribute(value="password", sensitive=true) char[] password, @PluginAttribute(value="passwordEnvironmentVariable") String passwordEnvironmentVariable, @PluginAttribute(value="passwordFile") String passwordFile, @PluginAttribute(value="type") String keyStoreType, @PluginAttribute(value="keyManagerFactoryAlgorithm") String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        if (password != null && passwordEnvironmentVariable != null && passwordFile != null) {
            throw new StoreConfigurationException("You MUST set only one of 'password', 'passwordEnvironmentVariable' or 'passwordFile'.");
        }
        try {
            PasswordProvider provider;
            PasswordProvider passwordProvider = passwordFile != null ? new FilePasswordProvider(passwordFile) : (provider = passwordEnvironmentVariable != null ? new EnvironmentPasswordProvider(passwordEnvironmentVariable) : new MemoryPasswordProvider(password));
            if (password != null) {
                Arrays.fill(password, '\u0000');
            }
            return new KeyStoreConfiguration(location, provider, keyStoreType, keyManagerFactoryAlgorithm);
        } catch (Exception ex) {
            throw new StoreConfigurationException("Could not configure KeyStore", ex);
        }
    }

    @Deprecated
    public static KeyStoreConfiguration createKeyStoreConfiguration(String location, char[] password, String keyStoreType, String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        return KeyStoreConfiguration.createKeyStoreConfiguration(location, password, null, null, keyStoreType, keyManagerFactoryAlgorithm);
    }

    @Deprecated
    public static KeyStoreConfiguration createKeyStoreConfiguration(String location, String password, String keyStoreType, String keyManagerFactoryAlgorithm) throws StoreConfigurationException {
        return KeyStoreConfiguration.createKeyStoreConfiguration(location, password == null ? null : password.toCharArray(), keyStoreType, keyManagerFactoryAlgorithm);
    }

    public KeyManagerFactory initKeyManagerFactory() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(this.keyManagerFactoryAlgorithm);
        char[] password = this.getPasswordAsCharArray();
        try {
            kmFactory.init(this.getKeyStore(), password != null ? password : DEFAULT_PASSWORD);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\u0000');
            }
        }
        return kmFactory;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.keyManagerFactoryAlgorithm == null ? 0 : this.keyManagerFactoryAlgorithm.hashCode());
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
        KeyStoreConfiguration other = (KeyStoreConfiguration)obj;
        return Objects.equals(this.keyManagerFactoryAlgorithm, other.keyManagerFactoryAlgorithm);
    }

    public String getKeyManagerFactoryAlgorithm() {
        return this.keyManagerFactoryAlgorithm;
    }
}

