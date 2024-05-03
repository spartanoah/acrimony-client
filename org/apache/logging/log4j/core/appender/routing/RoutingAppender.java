/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.routing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Bindings;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.appender.routing.PurgePolicy;
import org.apache.logging.log4j.core.appender.routing.Route;
import org.apache.logging.log4j.core.appender.routing.Routes;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.script.ScriptManager;
import org.apache.logging.log4j.core.script.ScriptRef;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="Routing", category="Core", elementType="appender", printObject=true)
public final class RoutingAppender
extends AbstractAppender {
    public static final String STATIC_VARIABLES_KEY = "staticVariables";
    private static final String DEFAULT_KEY = "ROUTING_APPENDER_DEFAULT";
    private final Routes routes;
    private Route defaultRoute;
    private final Configuration configuration;
    private final ConcurrentMap<String, CreatedRouteAppenderControl> createdAppenders = new ConcurrentHashMap<String, CreatedRouteAppenderControl>();
    private final Map<String, AppenderControl> createdAppendersUnmodifiableView = Collections.unmodifiableMap(this.createdAppenders);
    private final ConcurrentMap<String, RouteAppenderControl> referencedAppenders = new ConcurrentHashMap<String, RouteAppenderControl>();
    private final RewritePolicy rewritePolicy;
    private final PurgePolicy purgePolicy;
    private final AbstractScript defaultRouteScript;
    private final ConcurrentMap<Object, Object> scriptStaticVariables = new ConcurrentHashMap<Object, Object>();

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private RoutingAppender(String name, Filter filter, boolean ignoreExceptions, Routes routes, RewritePolicy rewritePolicy, Configuration configuration, PurgePolicy purgePolicy, AbstractScript defaultRouteScript, Property[] properties) {
        super(name, filter, null, ignoreExceptions, properties);
        this.routes = routes;
        this.configuration = configuration;
        this.rewritePolicy = rewritePolicy;
        this.purgePolicy = purgePolicy;
        if (this.purgePolicy != null) {
            this.purgePolicy.initialize(this);
        }
        this.defaultRouteScript = defaultRouteScript;
        Route defRoute = null;
        for (Route route : routes.getRoutes()) {
            if (route.getKey() != null) continue;
            if (defRoute == null) {
                defRoute = route;
                continue;
            }
            this.error("Multiple default routes. Route " + route.toString() + " will be ignored");
        }
        this.defaultRoute = defRoute;
    }

    @Override
    public void start() {
        if (this.defaultRouteScript != null) {
            if (this.configuration == null) {
                this.error("No Configuration defined for RoutingAppender; required for Script element.");
            } else {
                ScriptManager scriptManager = this.configuration.getScriptManager();
                Bindings bindings = scriptManager.createBindings(this.defaultRouteScript);
                bindings.put(STATIC_VARIABLES_KEY, (Object)this.scriptStaticVariables);
                Object object = scriptManager.execute(this.defaultRouteScript.getName(), bindings);
                Route route = this.routes.getRoute(Objects.toString(object, null));
                if (route != null) {
                    this.defaultRoute = route;
                }
            }
        }
        for (Route route : this.routes.getRoutes()) {
            if (route.getAppenderRef() == null) continue;
            Object appender = this.configuration.getAppender(route.getAppenderRef());
            if (appender != null) {
                String key = route == this.defaultRoute ? DEFAULT_KEY : route.getKey();
                this.referencedAppenders.put(key, new ReferencedRouteAppenderControl((Appender)appender));
                continue;
            }
            this.error("Appender " + route.getAppenderRef() + " cannot be located. Route ignored");
        }
        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(timeout, timeUnit, false);
        for (Map.Entry entry : this.createdAppenders.entrySet()) {
            Appender appender = ((CreatedRouteAppenderControl)entry.getValue()).getAppender();
            if (appender instanceof LifeCycle2) {
                ((LifeCycle2)((Object)appender)).stop(timeout, timeUnit);
                continue;
            }
            appender.stop();
        }
        this.setStopped();
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void append(LogEvent event) {
        String pattern;
        String key;
        RouteAppenderControl control;
        if (this.rewritePolicy != null) {
            event = this.rewritePolicy.rewrite(event);
        }
        if ((control = this.getControl(key = (pattern = this.routes.getPattern(event, this.scriptStaticVariables)) != null ? this.configuration.getStrSubstitutor().replace(event, pattern) : (this.defaultRoute.getKey() != null ? this.defaultRoute.getKey() : DEFAULT_KEY), event)) != null) {
            try {
                control.callAppender(event);
            } finally {
                control.release();
            }
        }
        this.updatePurgePolicy(key, event);
    }

    private void updatePurgePolicy(String key, LogEvent event) {
        if (this.purgePolicy != null && !this.referencedAppenders.containsKey(key)) {
            this.purgePolicy.update(key, event);
        }
    }

    private synchronized RouteAppenderControl getControl(String key, LogEvent event) {
        RouteAppenderControl control = this.getAppender(key);
        if (control != null) {
            control.checkout();
            return control;
        }
        Route route = null;
        for (Route r : this.routes.getRoutes()) {
            if (r.getAppenderRef() != null || !key.equals(r.getKey())) continue;
            route = r;
            break;
        }
        if (route == null) {
            route = this.defaultRoute;
            control = this.getAppender(DEFAULT_KEY);
            if (control != null) {
                control.checkout();
                return control;
            }
        }
        if (route != null) {
            Appender app = this.createAppender(route, event);
            if (app == null) {
                return null;
            }
            CreatedRouteAppenderControl created = new CreatedRouteAppenderControl(app);
            control = created;
            this.createdAppenders.put(key, created);
        }
        if (control != null) {
            control.checkout();
        }
        return control;
    }

    private RouteAppenderControl getAppender(String key) {
        RouteAppenderControl result = (RouteAppenderControl)this.referencedAppenders.get(key);
        if (result == null) {
            return (RouteAppenderControl)this.createdAppenders.get(key);
        }
        return result;
    }

    private Appender createAppender(Route route, LogEvent event) {
        Node routeNode = route.getNode();
        for (Node node : routeNode.getChildren()) {
            if (!node.getType().getElementName().equals("appender")) continue;
            Node appNode = new Node(node);
            this.configuration.createConfiguration(appNode, event);
            if (appNode.getObject() instanceof Appender) {
                Appender app = (Appender)appNode.getObject();
                app.start();
                return app;
            }
            this.error("Unable to create Appender of type " + node.getName());
            return null;
        }
        this.error("No Appender was configured for route " + route.getKey());
        return null;
    }

    public Map<String, AppenderControl> getAppenders() {
        return this.createdAppendersUnmodifiableView;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void deleteAppender(String key) {
        LOGGER.debug("Deleting route with {} key ", (Object)key);
        CreatedRouteAppenderControl control = (CreatedRouteAppenderControl)this.createdAppenders.remove(key);
        if (null != control) {
            LOGGER.debug("Stopping route with {} key", (Object)key);
            RoutingAppender routingAppender = this;
            synchronized (routingAppender) {
                control.pendingDeletion = true;
            }
            control.tryStopAppender();
        } else if (this.referencedAppenders.containsKey(key)) {
            LOGGER.debug("Route {} using an appender reference may not be removed because the appender may be used outside of the RoutingAppender", (Object)key);
        } else {
            LOGGER.debug("Route with {} key already deleted", (Object)key);
        }
    }

    @Deprecated
    public static RoutingAppender createAppender(String name, String ignore, Routes routes, Configuration config, RewritePolicy rewritePolicy, PurgePolicy purgePolicy, Filter filter) {
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        if (name == null) {
            LOGGER.error("No name provided for RoutingAppender");
            return null;
        }
        if (routes == null) {
            LOGGER.error("No routes defined for RoutingAppender");
            return null;
        }
        return new RoutingAppender(name, filter, ignoreExceptions, routes, rewritePolicy, config, purgePolicy, null, null);
    }

    public Route getDefaultRoute() {
        return this.defaultRoute;
    }

    public AbstractScript getDefaultRouteScript() {
        return this.defaultRouteScript;
    }

    public PurgePolicy getPurgePolicy() {
        return this.purgePolicy;
    }

    public RewritePolicy getRewritePolicy() {
        return this.rewritePolicy;
    }

    public Routes getRoutes() {
        return this.routes;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public ConcurrentMap<Object, Object> getScriptStaticVariables() {
        return this.scriptStaticVariables;
    }

    private static final class ReferencedRouteAppenderControl
    extends RouteAppenderControl {
        ReferencedRouteAppenderControl(Appender appender) {
            super(appender);
        }

        @Override
        void checkout() {
        }

        @Override
        void release() {
        }
    }

    private static final class CreatedRouteAppenderControl
    extends RouteAppenderControl {
        private volatile boolean pendingDeletion;
        private final AtomicInteger depth = new AtomicInteger();

        CreatedRouteAppenderControl(Appender appender) {
            super(appender);
        }

        @Override
        void checkout() {
            if (this.pendingDeletion) {
                LOGGER.warn("CreatedRouteAppenderControl.checkout invoked on a RouteAppenderControl that is pending deletion");
            }
            this.depth.incrementAndGet();
        }

        @Override
        void release() {
            this.depth.decrementAndGet();
            this.tryStopAppender();
        }

        void tryStopAppender() {
            if (this.pendingDeletion && this.depth.compareAndSet(0, -100000)) {
                Appender appender = this.getAppender();
                LOGGER.debug("Stopping appender {}", (Object)appender);
                appender.stop();
            }
        }
    }

    private static abstract class RouteAppenderControl
    extends AppenderControl {
        RouteAppenderControl(Appender appender) {
            super(appender, null, null);
        }

        abstract void checkout();

        abstract void release();
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<RoutingAppender> {
        @PluginElement(value="Script")
        private AbstractScript defaultRouteScript;
        @PluginElement(value="Routes")
        private Routes routes;
        @PluginElement(value="RewritePolicy")
        private RewritePolicy rewritePolicy;
        @PluginElement(value="PurgePolicy")
        private PurgePolicy purgePolicy;

        @Override
        public RoutingAppender build() {
            String name = this.getName();
            if (name == null) {
                LOGGER.error("No name defined for this RoutingAppender");
                return null;
            }
            if (this.routes == null) {
                LOGGER.error("No routes defined for RoutingAppender {}", (Object)name);
                return null;
            }
            if (this.defaultRouteScript != null) {
                if (this.getConfiguration().getScriptManager() == null) {
                    LOGGER.error("Script support is not enabled");
                    return null;
                }
                if (!(this.defaultRouteScript instanceof ScriptRef) && !this.getConfiguration().getScriptManager().addScript(this.defaultRouteScript)) {
                    return null;
                }
            }
            return new RoutingAppender(name, this.getFilter(), this.isIgnoreExceptions(), this.routes, this.rewritePolicy, this.getConfiguration(), this.purgePolicy, this.defaultRouteScript, this.getPropertyArray());
        }

        public Routes getRoutes() {
            return this.routes;
        }

        public AbstractScript getDefaultRouteScript() {
            return this.defaultRouteScript;
        }

        public RewritePolicy getRewritePolicy() {
            return this.rewritePolicy;
        }

        public PurgePolicy getPurgePolicy() {
            return this.purgePolicy;
        }

        public B withRoutes(Routes routes) {
            this.routes = routes;
            return (B)((Builder)this.asBuilder());
        }

        public B withDefaultRouteScript(AbstractScript defaultRouteScript) {
            this.defaultRouteScript = defaultRouteScript;
            return (B)((Builder)this.asBuilder());
        }

        public B withRewritePolicy(RewritePolicy rewritePolicy) {
            this.rewritePolicy = rewritePolicy;
            return (B)((Builder)this.asBuilder());
        }

        public void withPurgePolicy(PurgePolicy purgePolicy) {
            this.purgePolicy = purgePolicy;
        }
    }
}

