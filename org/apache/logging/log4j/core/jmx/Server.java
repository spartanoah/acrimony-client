/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jmx;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.jmx.AppenderAdmin;
import org.apache.logging.log4j.core.jmx.AsyncAppenderAdmin;
import org.apache.logging.log4j.core.jmx.ContextSelectorAdmin;
import org.apache.logging.log4j.core.jmx.LoggerConfigAdmin;
import org.apache.logging.log4j.core.jmx.LoggerContextAdmin;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.jmx.StatusLoggerAdmin;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Server {
    private static final String CONTEXT_NAME_ALL = "*";
    public static final String DOMAIN = "org.apache.logging.log4j2";
    private static final String PROPERTY_DISABLE_JMX = "log4j2.disable.jmx";
    private static final String PROPERTY_ASYNC_NOTIF = "log4j2.jmx.notify.async";
    private static final String THREAD_NAME_PREFIX = "jmx.notif";
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    static final Executor executor = Server.isJmxDisabled() ? null : Server.createExecutor();

    private Server() {
    }

    private static ExecutorService createExecutor() {
        boolean defaultAsync = !Constants.IS_WEB_APP;
        boolean async = PropertiesUtil.getProperties().getBooleanProperty(PROPERTY_ASYNC_NOTIF, defaultAsync);
        return async ? Executors.newFixedThreadPool(1, Log4jThreadFactory.createDaemonThreadFactory(THREAD_NAME_PREFIX)) : null;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static String escape(String name) {
        StringBuilder sb = new StringBuilder(name.length() * 2);
        boolean needsQuotes = false;
        block6: for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            switch (c) {
                case '\"': 
                case '*': 
                case '?': 
                case '\\': {
                    sb.append('\\');
                    needsQuotes = true;
                    break;
                }
                case ',': 
                case ':': 
                case '=': {
                    needsQuotes = true;
                    break;
                }
                case '\r': {
                    continue block6;
                }
                case '\n': {
                    sb.append("\\n");
                    needsQuotes = true;
                    continue block6;
                }
            }
            sb.append(c);
        }
        if (needsQuotes) {
            sb.insert(0, '\"');
            sb.append('\"');
        }
        return sb.toString();
    }

    private static boolean isJmxDisabled() {
        return PropertiesUtil.getProperties().getBooleanProperty(PROPERTY_DISABLE_JMX);
    }

    public static void reregisterMBeansAfterReconfigure() {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not registering MBeans.");
            return;
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Server.reregisterMBeansAfterReconfigure(mbs);
    }

    public static void reregisterMBeansAfterReconfigure(MBeanServer mbs) {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not registering MBeans.");
            return;
        }
        try {
            ContextSelector selector = Server.getContextSelector();
            if (selector == null) {
                LOGGER.debug("Could not register MBeans: no ContextSelector found.");
                return;
            }
            LOGGER.trace("Reregistering MBeans after reconfigure. Selector={}", (Object)selector);
            List<LoggerContext> contexts = selector.getLoggerContexts();
            int i = 0;
            for (LoggerContext ctx : contexts) {
                RingBufferAdmin rbmbean;
                LOGGER.trace("Reregistering context ({}/{}): '{}' {}", (Object)(++i), (Object)contexts.size(), (Object)ctx.getName(), (Object)ctx);
                Server.unregisterLoggerContext(ctx.getName(), mbs);
                LoggerContextAdmin mbean = new LoggerContextAdmin(ctx, executor);
                Server.register(mbs, mbean, mbean.getObjectName());
                if (ctx instanceof AsyncLoggerContext && (rbmbean = ((AsyncLoggerContext)ctx).createRingBufferAdmin()).getBufferSize() > 0L) {
                    Server.register(mbs, rbmbean, rbmbean.getObjectName());
                }
                Server.registerStatusLogger(ctx.getName(), mbs, executor);
                Server.registerContextSelector(ctx.getName(), selector, mbs, executor);
                Server.registerLoggerConfigs(ctx, mbs, executor);
                Server.registerAppenders(ctx, mbs, executor);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not register mbeans", (Throwable)ex);
        }
    }

    public static void unregisterMBeans() {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not unregistering MBeans.");
            return;
        }
        Server.unregisterMBeans(ManagementFactory.getPlatformMBeanServer());
    }

    public static void unregisterMBeans(MBeanServer mbs) {
        if (mbs != null) {
            Server.unregisterStatusLogger(CONTEXT_NAME_ALL, mbs);
            Server.unregisterContextSelector(CONTEXT_NAME_ALL, mbs);
            Server.unregisterContexts(mbs);
            Server.unregisterLoggerConfigs(CONTEXT_NAME_ALL, mbs);
            Server.unregisterAsyncLoggerRingBufferAdmins(CONTEXT_NAME_ALL, mbs);
            Server.unregisterAsyncLoggerConfigRingBufferAdmins(CONTEXT_NAME_ALL, mbs);
            Server.unregisterAppenders(CONTEXT_NAME_ALL, mbs);
            Server.unregisterAsyncAppenders(CONTEXT_NAME_ALL, mbs);
        }
    }

    private static ContextSelector getContextSelector() {
        LoggerContextFactory factory = LogManager.getFactory();
        if (factory instanceof Log4jContextFactory) {
            ContextSelector selector = ((Log4jContextFactory)factory).getSelector();
            return selector;
        }
        return null;
    }

    public static void unregisterLoggerContext(String loggerContextName) {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not unregistering MBeans.");
            return;
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Server.unregisterLoggerContext(loggerContextName, mbs);
    }

    public static void unregisterLoggerContext(String contextName, MBeanServer mbs) {
        String search = String.format("org.apache.logging.log4j2:type=%s", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
        Server.unregisterStatusLogger(contextName, mbs);
        Server.unregisterContextSelector(contextName, mbs);
        Server.unregisterLoggerConfigs(contextName, mbs);
        Server.unregisterAppenders(contextName, mbs);
        Server.unregisterAsyncAppenders(contextName, mbs);
        Server.unregisterAsyncLoggerRingBufferAdmins(contextName, mbs);
        Server.unregisterAsyncLoggerConfigRingBufferAdmins(contextName, mbs);
    }

    private static void registerStatusLogger(String contextName, MBeanServer mbs, Executor executor) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        StatusLoggerAdmin mbean = new StatusLoggerAdmin(contextName, executor);
        Server.register(mbs, mbean, mbean.getObjectName());
    }

    private static void registerContextSelector(String contextName, ContextSelector selector, MBeanServer mbs, Executor executor) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        ContextSelectorAdmin mbean = new ContextSelectorAdmin(contextName, selector);
        Server.register(mbs, mbean, mbean.getObjectName());
    }

    private static void unregisterStatusLogger(String contextName, MBeanServer mbs) {
        String search = String.format("org.apache.logging.log4j2:type=%s,component=StatusLogger", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterContextSelector(String contextName, MBeanServer mbs) {
        String search = String.format("org.apache.logging.log4j2:type=%s,component=ContextSelector", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterLoggerConfigs(String contextName, MBeanServer mbs) {
        String pattern = "org.apache.logging.log4j2:type=%s,component=Loggers,name=%s";
        String search = String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterContexts(MBeanServer mbs) {
        String pattern = "org.apache.logging.log4j2:type=%s";
        String search = String.format("org.apache.logging.log4j2:type=%s", CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterAppenders(String contextName, MBeanServer mbs) {
        String pattern = "org.apache.logging.log4j2:type=%s,component=Appenders,name=%s";
        String search = String.format("org.apache.logging.log4j2:type=%s,component=Appenders,name=%s", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterAsyncAppenders(String contextName, MBeanServer mbs) {
        String pattern = "org.apache.logging.log4j2:type=%s,component=AsyncAppenders,name=%s";
        String search = String.format("org.apache.logging.log4j2:type=%s,component=AsyncAppenders,name=%s", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search, mbs);
    }

    private static void unregisterAsyncLoggerRingBufferAdmins(String contextName, MBeanServer mbs) {
        String pattern1 = "org.apache.logging.log4j2:type=%s,component=AsyncLoggerRingBuffer";
        String search1 = String.format("org.apache.logging.log4j2:type=%s,component=AsyncLoggerRingBuffer", Server.escape(contextName));
        Server.unregisterAllMatching(search1, mbs);
    }

    private static void unregisterAsyncLoggerConfigRingBufferAdmins(String contextName, MBeanServer mbs) {
        String pattern2 = "org.apache.logging.log4j2:type=%s,component=Loggers,name=%s,subtype=RingBuffer";
        String search2 = String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s,subtype=RingBuffer", Server.escape(contextName), CONTEXT_NAME_ALL);
        Server.unregisterAllMatching(search2, mbs);
    }

    private static void unregisterAllMatching(String search, MBeanServer mbs) {
        try {
            ObjectName pattern = new ObjectName(search);
            Set<ObjectName> found = mbs.queryNames(pattern, null);
            if (found == null || found.isEmpty()) {
                LOGGER.trace("Unregistering but no MBeans found matching '{}'", (Object)search);
            } else {
                LOGGER.trace("Unregistering {} MBeans: {}", (Object)found.size(), (Object)found);
            }
            if (found != null) {
                for (ObjectName objectName : found) {
                    mbs.unregisterMBean(objectName);
                }
            }
        } catch (InstanceNotFoundException ex) {
            LOGGER.debug("Could not unregister MBeans for " + search + ". Ignoring " + ex);
        } catch (Exception ex) {
            LOGGER.error("Could not unregister MBeans for " + search, (Throwable)ex);
        }
    }

    private static void registerLoggerConfigs(LoggerContext ctx, MBeanServer mbs, Executor executor) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        Map<String, LoggerConfig> map = ctx.getConfiguration().getLoggers();
        for (String name : map.keySet()) {
            LoggerConfig cfg = map.get(name);
            LoggerConfigAdmin mbean = new LoggerConfigAdmin(ctx, cfg);
            Server.register(mbs, mbean, mbean.getObjectName());
            if (!(cfg instanceof AsyncLoggerConfig)) continue;
            AsyncLoggerConfig async = (AsyncLoggerConfig)cfg;
            RingBufferAdmin rbmbean = async.createRingBufferAdmin(ctx.getName());
            Server.register(mbs, rbmbean, rbmbean.getObjectName());
        }
    }

    private static void registerAppenders(LoggerContext ctx, MBeanServer mbs, Executor executor) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        Map<String, Appender> map = ctx.getConfiguration().getAppenders();
        for (String name : map.keySet()) {
            Appender appender = map.get(name);
            if (appender instanceof AsyncAppender) {
                AsyncAppender async = (AsyncAppender)appender;
                AsyncAppenderAdmin mbean = new AsyncAppenderAdmin(ctx.getName(), async);
                Server.register(mbs, mbean, mbean.getObjectName());
                continue;
            }
            AppenderAdmin mbean = new AppenderAdmin(ctx.getName(), appender);
            Server.register(mbs, mbean, mbean.getObjectName());
        }
    }

    private static void register(MBeanServer mbs, Object mbean, ObjectName objectName) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        if (mbs.isRegistered(objectName)) {
            try {
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException | MBeanRegistrationException ex) {
                LOGGER.trace("Failed to unregister MBean {}", (Object)objectName);
            }
        }
        LOGGER.debug("Registering MBean {}", (Object)objectName);
        mbs.registerMBean(mbean, objectName);
    }
}

