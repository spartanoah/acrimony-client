/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.LoggerFields;
import org.apache.logging.log4j.core.layout.Rfc5424Layout;
import org.apache.logging.log4j.core.layout.SyslogLayout;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.util.EnglishEnums;

@Plugin(name="Syslog", category="Core", elementType="appender", printObject=true)
public class SyslogAppender
extends SocketAppender {
    protected static final String RFC5424 = "RFC5424";

    protected SyslogAppender(String name, Layout<? extends Serializable> layout, Filter filter, boolean ignoreExceptions, boolean immediateFlush, AbstractSocketManager manager, Advertiser advertiser, Property[] properties) {
        super(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertiser, properties);
    }

    @Deprecated
    protected SyslogAppender(String name, Layout<? extends Serializable> layout, Filter filter, boolean ignoreExceptions, boolean immediateFlush, AbstractSocketManager manager, Advertiser advertiser) {
        super(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertiser, Property.EMPTY_ARRAY);
    }

    @Deprecated
    public static <B extends Builder<B>> SyslogAppender createAppender(String host, int port, String protocolStr, SslConfiguration sslConfiguration, int connectTimeoutMillis, int reconnectDelayMillis, boolean immediateFail, String name, boolean immediateFlush, boolean ignoreExceptions, Facility facility, String id, int enterpriseNumber, boolean includeMdc, String mdcId, String mdcPrefix, String eventPrefix, boolean newLine, String escapeNL, String appName, String msgId, String excludes, String includes, String required, String format, Filter filter, Configuration configuration, Charset charset, String exceptionPattern, LoggerFields[] loggerFields, boolean advertise) {
        return ((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((SocketAppender.AbstractBuilder)SyslogAppender.newSyslogAppenderBuilder()).setHost(host)).setPort(port)).setProtocol(EnglishEnums.valueOf(Protocol.class, protocolStr))).setSslConfiguration(sslConfiguration)).setConnectTimeoutMillis(connectTimeoutMillis)).setReconnectDelayMillis(reconnectDelayMillis)).setImmediateFail(immediateFail)).setName(appName)).setImmediateFlush(immediateFlush)).setIgnoreExceptions(ignoreExceptions)).setFilter(filter)).setConfiguration(configuration)).setAdvertise(advertise)).setFacility(facility)).setId(id)).setEnterpriseNumber(enterpriseNumber)).setIncludeMdc(includeMdc)).setMdcId(mdcId)).setMdcPrefix(mdcPrefix)).setEventPrefix(eventPrefix)).setNewLine(newLine)).setAppName(appName)).setMsgId(msgId)).setExcludes(excludes)).setIncludeMdc(includeMdc)).setRequired(required)).setFormat(format)).setCharsetName(charset)).setExceptionPattern(exceptionPattern)).setLoggerFields(loggerFields)).build();
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newSyslogAppenderBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static class Builder<B extends Builder<B>>
    extends SocketAppender.AbstractBuilder<B>
    implements org.apache.logging.log4j.core.util.Builder<SocketAppender> {
        @PluginBuilderAttribute(value="facility")
        private Facility facility = Facility.LOCAL0;
        @PluginBuilderAttribute(value="id")
        private String id;
        @PluginBuilderAttribute(value="enterpriseNumber")
        private String enterpriseNumber = "32473";
        @PluginBuilderAttribute(value="includeMdc")
        private boolean includeMdc = true;
        @PluginBuilderAttribute(value="mdcId")
        private String mdcId;
        @PluginBuilderAttribute(value="mdcPrefix")
        private String mdcPrefix;
        @PluginBuilderAttribute(value="eventPrefix")
        private String eventPrefix;
        @PluginBuilderAttribute(value="newLine")
        private boolean newLine;
        @PluginBuilderAttribute(value="newLineEscape")
        private String escapeNL;
        @PluginBuilderAttribute(value="appName")
        private String appName;
        @PluginBuilderAttribute(value="messageId")
        private String msgId;
        @PluginBuilderAttribute(value="mdcExcludes")
        private String excludes;
        @PluginBuilderAttribute(value="mdcIncludes")
        private String includes;
        @PluginBuilderAttribute(value="mdcRequired")
        private String required;
        @PluginBuilderAttribute(value="format")
        private String format;
        @PluginBuilderAttribute(value="charset")
        private Charset charsetName = StandardCharsets.UTF_8;
        @PluginBuilderAttribute(value="exceptionPattern")
        private String exceptionPattern;
        @PluginElement(value="LoggerFields")
        private LoggerFields[] loggerFields;

        @Override
        public SyslogAppender build() {
            String name;
            Protocol protocol = this.getProtocol();
            SslConfiguration sslConfiguration = this.getSslConfiguration();
            boolean useTlsMessageFormat = sslConfiguration != null || protocol == Protocol.SSL;
            Configuration configuration = this.getConfiguration();
            SyslogLayout layout = this.getLayout();
            if (layout == null) {
                AbstractStringLayout abstractStringLayout = layout = SyslogAppender.RFC5424.equalsIgnoreCase(this.format) ? new Rfc5424Layout.Rfc5424LayoutBuilder().setFacility(this.facility).setId(this.id).setEin(this.enterpriseNumber).setIncludeMDC(this.includeMdc).setMdcId(this.mdcId).setMdcPrefix(this.mdcPrefix).setEventPrefix(this.eventPrefix).setIncludeNL(this.newLine).setEscapeNL(this.escapeNL).setAppName(this.appName).setMessageId(this.msgId).setExcludes(this.excludes).setIncludes(this.includes).setRequired(this.required).setExceptionPattern(this.exceptionPattern).setUseTLSMessageFormat(useTlsMessageFormat).setLoggerFields(this.loggerFields).setConfig(configuration).build() : ((SyslogLayout.Builder)((AbstractStringLayout.Builder)((SyslogLayout.Builder)((SyslogLayout.Builder)((SyslogLayout.Builder)SyslogLayout.newBuilder()).setFacility(this.facility)).setIncludeNewLine(this.newLine)).setEscapeNL(this.escapeNL)).setCharset(this.charsetName)).build();
            }
            if ((name = this.getName()) == null) {
                LOGGER.error("No name provided for SyslogAppender");
                return null;
            }
            AbstractSocketManager manager = SocketAppender.createSocketManager(name, protocol, this.getHost(), this.getPort(), this.getConnectTimeoutMillis(), sslConfiguration, this.getReconnectDelayMillis(), this.getImmediateFail(), layout, Constants.ENCODER_BYTE_BUFFER_SIZE, this.getSocketOptions());
            return new SyslogAppender(name, (Layout<? extends Serializable>)layout, this.getFilter(), this.isIgnoreExceptions(), this.isImmediateFlush(), manager, this.getAdvertise() ? configuration.getAdvertiser() : null, null);
        }

        public Facility getFacility() {
            return this.facility;
        }

        public String getId() {
            return this.id;
        }

        public String getEnterpriseNumber() {
            return this.enterpriseNumber;
        }

        public boolean isIncludeMdc() {
            return this.includeMdc;
        }

        public String getMdcId() {
            return this.mdcId;
        }

        public String getMdcPrefix() {
            return this.mdcPrefix;
        }

        public String getEventPrefix() {
            return this.eventPrefix;
        }

        public boolean isNewLine() {
            return this.newLine;
        }

        public String getEscapeNL() {
            return this.escapeNL;
        }

        public String getAppName() {
            return this.appName;
        }

        public String getMsgId() {
            return this.msgId;
        }

        public String getExcludes() {
            return this.excludes;
        }

        public String getIncludes() {
            return this.includes;
        }

        public String getRequired() {
            return this.required;
        }

        public String getFormat() {
            return this.format;
        }

        public Charset getCharsetName() {
            return this.charsetName;
        }

        public String getExceptionPattern() {
            return this.exceptionPattern;
        }

        public LoggerFields[] getLoggerFields() {
            return this.loggerFields;
        }

        public B setFacility(Facility facility) {
            this.facility = facility;
            return (B)((Builder)this.asBuilder());
        }

        public B setId(String id) {
            this.id = id;
            return (B)((Builder)this.asBuilder());
        }

        public B setEnterpriseNumber(String enterpriseNumber) {
            this.enterpriseNumber = enterpriseNumber;
            return (B)((Builder)this.asBuilder());
        }

        public B setEnterpriseNumber(int enterpriseNumber) {
            this.enterpriseNumber = String.valueOf(enterpriseNumber);
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeMdc(boolean includeMdc) {
            this.includeMdc = includeMdc;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcId(String mdcId) {
            this.mdcId = mdcId;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcPrefix(String mdcPrefix) {
            this.mdcPrefix = mdcPrefix;
            return (B)((Builder)this.asBuilder());
        }

        public B setEventPrefix(String eventPrefix) {
            this.eventPrefix = eventPrefix;
            return (B)((Builder)this.asBuilder());
        }

        public B setNewLine(boolean newLine) {
            this.newLine = newLine;
            return (B)((Builder)this.asBuilder());
        }

        public B setEscapeNL(String escapeNL) {
            this.escapeNL = escapeNL;
            return (B)((Builder)this.asBuilder());
        }

        public B setAppName(String appName) {
            this.appName = appName;
            return (B)((Builder)this.asBuilder());
        }

        public B setMsgId(String msgId) {
            this.msgId = msgId;
            return (B)((Builder)this.asBuilder());
        }

        public B setExcludes(String excludes) {
            this.excludes = excludes;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludes(String includes) {
            this.includes = includes;
            return (B)((Builder)this.asBuilder());
        }

        public B setRequired(String required) {
            this.required = required;
            return (B)((Builder)this.asBuilder());
        }

        public B setFormat(String format) {
            this.format = format;
            return (B)((Builder)this.asBuilder());
        }

        public B setCharsetName(Charset charset) {
            this.charsetName = charset;
            return (B)((Builder)this.asBuilder());
        }

        public B setExceptionPattern(String exceptionPattern) {
            this.exceptionPattern = exceptionPattern;
            return (B)((Builder)this.asBuilder());
        }

        public B setLoggerFields(LoggerFields[] loggerFields) {
            this.loggerFields = loggerFields;
            return (B)((Builder)this.asBuilder());
        }
    }
}

