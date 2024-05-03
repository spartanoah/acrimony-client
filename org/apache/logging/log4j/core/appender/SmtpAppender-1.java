/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.MailManager;
import org.apache.logging.log4j.core.net.MailManagerFactory;
import org.apache.logging.log4j.core.net.SmtpManager;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.util.ServiceLoaderUtil;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="SMTP", category="Core", elementType="appender", printObject=true)
public final class SmtpAppender
extends AbstractAppender {
    private static final int DEFAULT_BUFFER_SIZE = 512;
    private final MailManager manager;

    private SmtpAppender(String name, Filter filter, Layout<? extends Serializable> layout, MailManager manager, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.manager = manager;
    }

    public MailManager getManager() {
        return this.manager;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Deprecated
    public static SmtpAppender createAppender(@PluginConfiguration Configuration config, @PluginAttribute(value="name") @Required String name, @PluginAttribute(value="to") String to, @PluginAttribute(value="cc") String cc, @PluginAttribute(value="bcc") String bcc, @PluginAttribute(value="from") String from, @PluginAttribute(value="replyTo") String replyTo, @PluginAttribute(value="subject") String subject, @PluginAttribute(value="smtpProtocol") String smtpProtocol, @PluginAttribute(value="smtpHost") String smtpHost, @PluginAttribute(value="smtpPort", defaultString="0") @ValidPort String smtpPortStr, @PluginAttribute(value="smtpUsername") String smtpUsername, @PluginAttribute(value="smtpPassword", sensitive=true) String smtpPassword, @PluginAttribute(value="smtpDebug") String smtpDebug, @PluginAttribute(value="bufferSize") String bufferSizeStr, @PluginElement(value="Layout") Layout<? extends Serializable> layout, @PluginElement(value="Filter") Filter filter, @PluginAttribute(value="ignoreExceptions") String ignore) {
        if (name == null) {
            LOGGER.error("No name provided for SmtpAppender");
            return null;
        }
        return ((Builder)((Builder)((Builder)SmtpAppender.newBuilder().setIgnoreExceptions(Booleans.parseBoolean(ignore, true))).setSmtpPort(AbstractAppender.parseInt(smtpPortStr, 0)).setSmtpDebug(Boolean.parseBoolean(smtpDebug)).setBufferSize(bufferSizeStr == null ? 512 : Integers.parseInt(bufferSizeStr)).setLayout((Layout)layout)).setFilter(filter).setConfiguration(config != null ? config : new DefaultConfiguration())).build();
    }

    @Override
    public boolean isFiltered(LogEvent event) {
        boolean filtered = super.isFiltered(event);
        if (filtered) {
            this.manager.add(event);
        }
        return filtered;
    }

    @Override
    public void append(LogEvent event) {
        this.manager.sendEvents(this.getLayout(), event);
    }

    public static class Builder
    extends AbstractAppender.Builder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<SmtpAppender> {
        @PluginBuilderAttribute
        private String to;
        @PluginBuilderAttribute
        private String cc;
        @PluginBuilderAttribute
        private String bcc;
        @PluginBuilderAttribute
        private String from;
        @PluginBuilderAttribute
        private String replyTo;
        @PluginBuilderAttribute
        private String subject;
        @PluginBuilderAttribute
        private String smtpProtocol = "smtp";
        @PluginBuilderAttribute
        private String smtpHost;
        @PluginBuilderAttribute
        @ValidPort
        private int smtpPort;
        @PluginBuilderAttribute
        private String smtpUsername;
        @PluginBuilderAttribute(sensitive=true)
        private String smtpPassword;
        @PluginBuilderAttribute
        private boolean smtpDebug;
        @PluginBuilderAttribute
        private int bufferSize = 512;
        @PluginElement(value="SSL")
        private SslConfiguration sslConfiguration;

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setCc(String cc) {
            this.cc = cc;
            return this;
        }

        public Builder setBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setReplyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setSmtpProtocol(String smtpProtocol) {
            this.smtpProtocol = smtpProtocol;
            return this;
        }

        public Builder setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
            return this;
        }

        public Builder setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }

        public Builder setSmtpUsername(String smtpUsername) {
            this.smtpUsername = smtpUsername;
            return this;
        }

        public Builder setSmtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
            return this;
        }

        public Builder setSmtpDebug(boolean smtpDebug) {
            this.smtpDebug = smtpDebug;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setSslConfiguration(SslConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        @Override
        public Builder setLayout(Layout<? extends Serializable> layout) {
            return (Builder)super.setLayout(layout);
        }

        @Override
        public Builder setFilter(Filter filter) {
            return (Builder)super.setFilter(filter);
        }

        @Override
        public SmtpAppender build() {
            if (this.getLayout() == null) {
                this.setLayout((Layout)HtmlLayout.createDefaultLayout());
            }
            if (this.getFilter() == null) {
                this.setFilter(ThresholdFilter.createFilter(null, null, null));
            }
            if (Strings.isEmpty(this.smtpProtocol)) {
                this.smtpProtocol = "smtp";
            }
            AbstractStringLayout.Serializer subjectSerializer = PatternLayout.newSerializerBuilder().setConfiguration(this.getConfiguration()).setPattern(this.subject).build();
            MailManager.FactoryData data = new MailManager.FactoryData(this.to, this.cc, this.bcc, this.from, this.replyTo, this.subject, subjectSerializer, this.smtpProtocol, this.smtpHost, this.smtpPort, this.smtpUsername, this.smtpPassword, this.smtpDebug, this.bufferSize, this.sslConfiguration, this.getFilter().toString());
            MailManagerFactory factory = ServiceLoaderUtil.loadServices(MailManagerFactory.class, MethodHandles.lookup()).findAny().orElseGet(() -> SmtpManager.FACTORY);
            MailManager smtpManager = AbstractManager.getManager(data.getManagerName(), factory, data);
            if (smtpManager == null) {
                LOGGER.error("Unabled to instantiate SmtpAppender named {}", (Object)this.getName());
                return null;
            }
            return new SmtpAppender(this.getName(), this.getFilter(), this.getLayout(), smtpManager, this.isIgnoreExceptions(), this.getPropertyArray());
        }
    }
}

