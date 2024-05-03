/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubTypeValidator {
    protected static final String PREFIX_SPRING = "org.springframework.";
    protected static final String PREFIX_C3P0 = "com.mchange.v2.c3p0.";
    protected static final Set<String> DEFAULT_NO_DESER_CLASS_NAMES;
    protected Set<String> _cfgIllegalClassNames = DEFAULT_NO_DESER_CLASS_NAMES;
    private static final SubTypeValidator instance;

    protected SubTypeValidator() {
    }

    public static SubTypeValidator instance() {
        return instance;
    }

    public void validateSubType(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        String full;
        block6: {
            block7: {
                block8: {
                    Class<?> raw = type.getRawClass();
                    full = raw.getName();
                    if (this._cfgIllegalClassNames.contains(full)) break block6;
                    if (raw.isInterface()) break block7;
                    if (!full.startsWith(PREFIX_SPRING)) break block8;
                    for (Class<?> cls = raw; cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                        String name = cls.getSimpleName();
                        if (!"AbstractPointcutAdvisor".equals(name) && !"AbstractApplicationContext".equals(name)) {
                            continue;
                        }
                        break block6;
                    }
                    break block7;
                }
                if (full.startsWith(PREFIX_C3P0) && full.endsWith("DataSource")) break block6;
            }
            return;
        }
        ctxt.reportBadTypeDefinition(beanDesc, "Illegal type (%s) to deserialize: prevented for security reasons", full);
    }

    static {
        HashSet<String> s = new HashSet<String>();
        s.add("org.apache.commons.collections.functors.InvokerTransformer");
        s.add("org.apache.commons.collections.functors.InstantiateTransformer");
        s.add("org.apache.commons.collections4.functors.InvokerTransformer");
        s.add("org.apache.commons.collections4.functors.InstantiateTransformer");
        s.add("org.codehaus.groovy.runtime.ConvertedClosure");
        s.add("org.codehaus.groovy.runtime.MethodClosure");
        s.add("org.springframework.beans.factory.ObjectFactory");
        s.add("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
        s.add("org.apache.xalan.xsltc.trax.TemplatesImpl");
        s.add("com.sun.rowset.JdbcRowSetImpl");
        s.add("java.util.logging.FileHandler");
        s.add("java.rmi.server.UnicastRemoteObject");
        s.add("org.springframework.beans.factory.config.PropertyPathFactoryBean");
        s.add("org.springframework.aop.config.MethodLocatingFactoryBean");
        s.add("org.springframework.beans.factory.config.BeanReferenceFactoryBean");
        s.add("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
        s.add("com.sun.org.apache.bcel.internal.util.ClassLoader");
        s.add("org.hibernate.jmx.StatisticsService");
        s.add("org.apache.ibatis.datasource.jndi.JndiDataSourceFactory");
        s.add("org.apache.ibatis.parsing.XPathParser");
        s.add("jodd.db.connection.DataSourceConnectionProvider");
        s.add("oracle.jdbc.connector.OracleManagedConnectionFactory");
        s.add("oracle.jdbc.rowset.OracleJDBCRowSet");
        s.add("org.slf4j.ext.EventData");
        s.add("flex.messaging.util.concurrent.AsynchBeansWorkManagerExecutor");
        s.add("com.sun.deploy.security.ruleset.DRSHelper");
        s.add("org.apache.axis2.jaxws.spi.handler.HandlerResolverImpl");
        s.add("org.jboss.util.propertyeditor.DocumentEditor");
        s.add("org.apache.openjpa.ee.RegistryManagedRuntime");
        s.add("org.apache.openjpa.ee.JNDIManagedRuntime");
        s.add("org.apache.openjpa.ee.WASRegistryManagedRuntime");
        s.add("org.apache.axis2.transport.jms.JMSOutTransportInfo");
        s.add("com.mysql.cj.jdbc.admin.MiniAdmin");
        s.add("ch.qos.logback.core.db.DriverManagerConnectionSource");
        s.add("org.jdom.transform.XSLTransformer");
        s.add("org.jdom2.transform.XSLTransformer");
        s.add("net.sf.ehcache.transaction.manager.DefaultTransactionManagerLookup");
        s.add("net.sf.ehcache.hibernate.EhcacheJtaTransactionManagerLookup");
        s.add("ch.qos.logback.core.db.JNDIConnectionSource");
        s.add("com.zaxxer.hikari.HikariConfig");
        s.add("com.zaxxer.hikari.HikariDataSource");
        s.add("org.apache.cxf.jaxrs.provider.XSLTJaxbProvider");
        s.add("org.apache.commons.configuration.JNDIConfiguration");
        s.add("org.apache.commons.configuration2.JNDIConfiguration");
        s.add("org.apache.xalan.lib.sql.JNDIConnectionPool");
        s.add("com.sun.org.apache.xalan.internal.lib.sql.JNDIConnectionPool");
        s.add("org.apache.commons.dbcp.datasources.PerUserPoolDataSource");
        s.add("org.apache.commons.dbcp.datasources.SharedPoolDataSource");
        s.add("com.p6spy.engine.spy.P6DataSource");
        s.add("org.apache.log4j.receivers.db.DriverManagerConnectionSource");
        s.add("org.apache.log4j.receivers.db.JNDIConnectionSource");
        s.add("net.sf.ehcache.transaction.manager.selector.GenericJndiSelector");
        s.add("net.sf.ehcache.transaction.manager.selector.GlassfishSelector");
        s.add("org.apache.xbean.propertyeditor.JndiConverter");
        s.add("org.apache.hadoop.shaded.com.zaxxer.hikari.HikariConfig");
        s.add("com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig");
        s.add("br.com.anteros.dbcp.AnterosDBCPConfig");
        s.add("javax.swing.JEditorPane");
        s.add("org.apache.shiro.realm.jndi.JndiRealmFactory");
        s.add("org.apache.shiro.jndi.JndiObjectFactory");
        s.add("org.apache.ignite.cache.jta.jndi.CacheJndiTmLookup");
        s.add("org.apache.ignite.cache.jta.jndi.CacheJndiTmFactory");
        s.add("org.quartz.utils.JNDIConnectionProvider");
        s.add("org.apache.aries.transaction.jms.internal.XaPooledConnectionFactory");
        s.add("org.apache.aries.transaction.jms.RecoverablePooledConnectionFactory");
        s.add("com.caucho.config.types.ResourceRef");
        s.add("org.aoju.bus.proxy.provider.RmiProvider");
        s.add("org.aoju.bus.proxy.provider.remoting.RmiProvider");
        s.add("org.apache.activemq.ActiveMQConnectionFactory");
        s.add("org.apache.activemq.ActiveMQXAConnectionFactory");
        s.add("org.apache.activemq.spring.ActiveMQConnectionFactory");
        s.add("org.apache.activemq.spring.ActiveMQXAConnectionFactory");
        s.add("org.apache.activemq.pool.JcaPooledConnectionFactory");
        s.add("org.apache.activemq.pool.PooledConnectionFactory");
        s.add("org.apache.activemq.pool.XaPooledConnectionFactory");
        s.add("org.apache.activemq.jms.pool.XaPooledConnectionFactory");
        s.add("org.apache.activemq.jms.pool.JcaPooledConnectionFactory");
        s.add("org.apache.commons.proxy.provider.remoting.RmiProvider");
        s.add("org.apache.commons.jelly.impl.Embedded");
        s.add("oadd.org.apache.xalan.lib.sql.JNDIConnectionPool");
        s.add("oracle.jms.AQjmsQueueConnectionFactory");
        s.add("oracle.jms.AQjmsXATopicConnectionFactory");
        s.add("oracle.jms.AQjmsTopicConnectionFactory");
        s.add("oracle.jms.AQjmsXAQueueConnectionFactory");
        s.add("oracle.jms.AQjmsXAConnectionFactory");
        s.add("org.jsecurity.realm.jndi.JndiRealmFactory");
        s.add("com.pastdev.httpcomponents.configuration.JndiConfiguration");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet(s);
        instance = new SubTypeValidator();
    }
}

