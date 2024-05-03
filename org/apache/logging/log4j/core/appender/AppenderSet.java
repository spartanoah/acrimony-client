/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginNode;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="AppenderSet", category="Core", printObject=true, deferChildren=true)
public class AppenderSet {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private final Configuration configuration;
    private final Map<String, Node> nodeMap;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private AppenderSet(Configuration configuration, Map<String, Node> appenders) {
        this.configuration = configuration;
        this.nodeMap = appenders;
    }

    public Appender createAppender(String actualAppenderName, String sourceAppenderName) {
        Node node = this.nodeMap.get(actualAppenderName);
        if (node == null) {
            LOGGER.error("No node named {} in {}", (Object)actualAppenderName, (Object)this);
            return null;
        }
        node.getAttributes().put("name", sourceAppenderName);
        if (node.getType().getElementName().equals("appender")) {
            Node appNode = new Node(node);
            this.configuration.createConfiguration(appNode, null);
            if (appNode.getObject() instanceof Appender) {
                Appender app = (Appender)appNode.getObject();
                app.start();
                return app;
            }
            LOGGER.error("Unable to create Appender of type " + node.getName());
            return null;
        }
        LOGGER.error("No Appender was configured for name {} " + actualAppenderName);
        return null;
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<AppenderSet> {
        @PluginNode
        private Node node;
        @PluginConfiguration
        @Required
        private Configuration configuration;

        @Override
        public AppenderSet build() {
            if (this.configuration == null) {
                LOGGER.error("Configuration is missing from AppenderSet {}", (Object)this);
                return null;
            }
            if (this.node == null) {
                LOGGER.error("No node in AppenderSet {}", (Object)this);
                return null;
            }
            List<Node> children = this.node.getChildren();
            if (children == null) {
                LOGGER.error("No children node in AppenderSet {}", (Object)this);
                return null;
            }
            HashMap<String, Node> map = new HashMap<String, Node>(children.size());
            for (Node childNode : children) {
                String key = childNode.getAttributes().get("name");
                if (key == null) {
                    LOGGER.error("The attribute 'name' is missing from from the node {} in AppenderSet {}", (Object)childNode, (Object)children);
                    continue;
                }
                map.put(key, childNode);
            }
            return new AppenderSet(this.configuration, map);
        }

        public Node getNode() {
            return this.node;
        }

        public Configuration getConfiguration() {
            return this.configuration;
        }

        public Builder withNode(Node node) {
            this.node = node;
            return this;
        }

        public Builder withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public String toString() {
            return this.getClass().getName() + " [node=" + this.node + ", configuration=" + this.configuration + "]";
        }
    }
}

