/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net.ssl;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.AbstractKeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.EnvironmentPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.FilePasswordProvider;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;

@Plugin(name="TrustStore", category="Core", printObject=true)
public class TrustStoreConfiguration
extends AbstractKeyStoreConfiguration {
    private final String trustManagerFactoryAlgorithm;

    public TrustStoreConfiguration(String location, PasswordProvider passwordProvider, String keyStoreType, String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        super(location, passwordProvider, keyStoreType);
        this.trustManagerFactoryAlgorithm = trustManagerFactoryAlgorithm == null ? TrustManagerFactory.getDefaultAlgorithm() : trustManagerFactoryAlgorithm;
    }

    @Deprecated
    public TrustStoreConfiguration(String location, char[] password, String keyStoreType, String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password), keyStoreType, trustManagerFactoryAlgorithm);
        if (password != null) {
            Arrays.fill(password, '\u0000');
        }
    }

    @Deprecated
    public TrustStoreConfiguration(String location, String password, String keyStoreType, String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password == null ? null : password.toCharArray()), keyStoreType, trustManagerFactoryAlgorithm);
    }

    @PluginFactory
    public static TrustStoreConfiguration createKeyStoreConfiguration(@PluginAttribute(value="location") String location, @PluginAttribute(value="password", sensitive=true) char[] password, @PluginAttribute(value="passwordEnvironmentVariable") String passwordEnvironmentVariable, @PluginAttribute(value="passwordFile") String passwordFile, @PluginAttribute(value="type") String keyStoreType, @PluginAttribute(value="trustManagerFactoryAlgorithm") String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        if (password != null && passwordEnvironmentVariable != null && passwordFile != null) {
            throw new IllegalStateException("You MUST set only one of 'password', 'passwordEnvironmentVariable' or 'passwordFile'.");
        }
        try {
            PasswordProvider provider;
            PasswordProvider passwordProvider = passwordFile != null ? new FilePasswordProvider(passwordFile) : (provider = passwordEnvironmentVariable != null ? new EnvironmentPasswordProvider(passwordEnvironmentVariable) : new MemoryPasswordProvider(password));
            if (password != null) {
                Arrays.fill(password, '\u0000');
            }
            return new TrustStoreConfiguration(location, provider, keyStoreType, trustManagerFactoryAlgorithm);
        } catch (Exception ex) {
            throw new StoreConfigurationException("Could not configure TrustStore", ex);
        }
    }

    @Deprecated
    public static TrustStoreConfiguration createKeyStoreConfiguration(String location, char[] password, String keyStoreType, String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        return TrustStoreConfiguration.createKeyStoreConfiguration(location, password, null, null, keyStoreType, trustManagerFactoryAlgorithm);
    }

    @Deprecated
    public static TrustStoreConfiguration createKeyStoreConfiguration(String location, String password, String keyStoreType, String trustManagerFactoryAlgorithm) throws StoreConfigurationException {
        return TrustStoreConfiguration.createKeyStoreConfiguration(location, password == null ? null : password.toCharArray(), null, null, keyStoreType, trustManagerFactoryAlgorithm);
    }

    public TrustManagerFactory initTrustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(this.trustManagerFactoryAlgorithm);
        tmFactory.init(this.getKeyStore());
        return tmFactory;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.trustManagerFactoryAlgorithm == null ? 0 : this.trustManagerFactoryAlgorithm.hashCode());
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
        TrustStoreConfiguration other = (TrustStoreConfiguration)obj;
        return Objects.equals(this.trustManagerFactoryAlgorithm, other.trustManagerFactoryAlgorithm);
    }

    public String getTrustManagerFactoryAlgorithm() {
        return this.trustManagerFactoryAlgorithm;
    }
}

