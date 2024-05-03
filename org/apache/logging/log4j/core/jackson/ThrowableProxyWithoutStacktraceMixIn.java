/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

abstract class ThrowableProxyWithoutStacktraceMixIn {
    @JsonProperty(value="cause")
    @JacksonXmlProperty(namespace="http://logging.apache.org/log4j/2.0/events", localName="Cause")
    private ThrowableProxyWithoutStacktraceMixIn causeProxy;
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private int commonElementCount;
    @JsonIgnore
    private ExtendedStackTraceElement[] extendedStackTrace;
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private String localizedMessage;
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private String message;
    @JsonProperty
    @JacksonXmlProperty(isAttribute=true)
    private String name;
    @JsonIgnore
    private transient Throwable throwable;

    ThrowableProxyWithoutStacktraceMixIn() {
    }

    @JsonIgnore
    public abstract String getCauseStackTraceAsString();

    @JsonIgnore
    public abstract String getExtendedStackTraceAsString();

    @JsonIgnore
    public abstract StackTraceElement[] getStackTrace();

    @JsonProperty(value="suppressed")
    @JacksonXmlElementWrapper(namespace="http://logging.apache.org/log4j/2.0/events", localName="Suppressed")
    @JacksonXmlProperty(namespace="http://logging.apache.org/log4j/2.0/events", localName="SuppressedItem")
    public abstract ThrowableProxy[] getSuppressedProxies();

    @JsonIgnore
    public abstract String getSuppressedStackTrace();

    @JsonIgnore
    public abstract Throwable getThrowable();
}

