/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.util.JndiCloser;
import org.apache.logging.log4j.util.PropertiesUtil;

public class JndiManager
extends AbstractManager {
    private static final JndiManagerFactory FACTORY = new JndiManagerFactory();
    private static final String PREFIX = "log4j2.enableJndi";
    private static final String JAVA_SCHEME = "java";
    private final InitialContext context;

    private static boolean isJndiEnabled(String subKey) {
        return PropertiesUtil.getProperties().getBooleanProperty(PREFIX + subKey, false);
    }

    public static boolean isJndiEnabled() {
        return JndiManager.isJndiContextSelectorEnabled() || JndiManager.isJndiJdbcEnabled() || JndiManager.isJndiJmsEnabled() || JndiManager.isJndiLookupEnabled();
    }

    public static boolean isJndiContextSelectorEnabled() {
        return JndiManager.isJndiEnabled("ContextSelector");
    }

    public static boolean isJndiJdbcEnabled() {
        return JndiManager.isJndiEnabled("Jdbc");
    }

    public static boolean isJndiJmsEnabled() {
        return JndiManager.isJndiEnabled("Jms");
    }

    public static boolean isJndiLookupEnabled() {
        return JndiManager.isJndiEnabled("Lookup");
    }

    private JndiManager(String name, InitialContext context) {
        super(null, name);
        this.context = context;
    }

    public static JndiManager getDefaultManager() {
        return JndiManager.getManager(JndiManager.class.getName(), FACTORY, null);
    }

    public static JndiManager getDefaultManager(String name) {
        return JndiManager.getManager(name, FACTORY, null);
    }

    public static JndiManager getJndiManager(String initialContextFactoryName, String providerURL, String urlPkgPrefixes, String securityPrincipal, String securityCredentials, Properties additionalProperties) {
        Properties properties = JndiManager.createProperties(initialContextFactoryName, providerURL, urlPkgPrefixes, securityPrincipal, securityCredentials, additionalProperties);
        return JndiManager.getManager(JndiManager.createManagerName(), FACTORY, properties);
    }

    public static JndiManager getJndiManager(Properties properties) {
        return JndiManager.getManager(JndiManager.createManagerName(), FACTORY, properties);
    }

    private static String createManagerName() {
        return JndiManager.class.getName() + '@' + JndiManager.class.hashCode();
    }

    public static Properties createProperties(String initialContextFactoryName, String providerURL, String urlPkgPrefixes, String securityPrincipal, String securityCredentials, Properties additionalProperties) {
        if (initialContextFactoryName == null) {
            return null;
        }
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial", initialContextFactoryName);
        if (providerURL != null) {
            properties.setProperty("java.naming.provider.url", providerURL);
        } else {
            LOGGER.warn("The JNDI InitialContextFactory class name [{}] was provided, but there was no associated provider URL. This is likely to cause problems.", (Object)initialContextFactoryName);
        }
        if (urlPkgPrefixes != null) {
            properties.setProperty("java.naming.factory.url.pkgs", urlPkgPrefixes);
        }
        if (securityPrincipal != null) {
            properties.setProperty("java.naming.security.principal", securityPrincipal);
            if (securityCredentials != null) {
                properties.setProperty("java.naming.security.credentials", securityCredentials);
            } else {
                LOGGER.warn("A security principal [{}] was provided, but with no corresponding security credentials.", (Object)securityPrincipal);
            }
        }
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }
        return properties;
    }

    @Override
    protected boolean releaseSub(long timeout, TimeUnit timeUnit) {
        return JndiCloser.closeSilently(this.context);
    }

    public <T> T lookup(String name) throws NamingException {
        if (this.context == null) {
            return null;
        }
        try {
            URI uri = new URI(name);
            if (uri.getScheme() == null || uri.getScheme().equals(JAVA_SCHEME)) {
                return (T)this.context.lookup(name);
            }
            LOGGER.warn("Unsupported JNDI URI - {}", (Object)name);
        } catch (URISyntaxException ex) {
            LOGGER.warn("Invalid JNDI URI - {}", (Object)name);
        }
        return null;
    }

    public String toString() {
        return "JndiManager [context=" + this.context + ", count=" + this.count + "]";
    }

    private static class JndiManagerFactory
    implements ManagerFactory<JndiManager, Properties> {
        private JndiManagerFactory() {
        }

        @Override
        public JndiManager createManager(String name, Properties data) {
            if (!JndiManager.isJndiEnabled()) {
                throw new IllegalStateException(String.format("JNDI must be enabled by setting one of the %s* properties to true", JndiManager.PREFIX));
            }
            try {
                return new JndiManager(name, new InitialContext(data));
            } catch (NamingException e) {
                LOGGER.error("Error creating JNDI InitialContext for '{}'.", (Object)name, (Object)e);
                return null;
            }
        }
    }
}

