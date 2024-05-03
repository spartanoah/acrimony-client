/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxyHelper;
import org.apache.logging.log4j.core.impl.ThrowableProxyRenderer;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class ThrowableProxy
implements Serializable {
    private static final char EOL = '\n';
    private static final String EOL_STR = String.valueOf('\n');
    private static final long serialVersionUID = -2752771578252251910L;
    private final ThrowableProxy causeProxy;
    private int commonElementCount;
    private final ExtendedStackTraceElement[] extendedStackTrace;
    private final String localizedMessage;
    private final String message;
    private final String name;
    private final ThrowableProxy[] suppressedProxies;
    private final transient Throwable throwable;
    static final ThrowableProxy[] EMPTY_ARRAY = new ThrowableProxy[0];

    ThrowableProxy() {
        this.throwable = null;
        this.name = null;
        this.extendedStackTrace = ExtendedStackTraceElement.EMPTY_ARRAY;
        this.causeProxy = null;
        this.message = null;
        this.localizedMessage = null;
        this.suppressedProxies = EMPTY_ARRAY;
    }

    public ThrowableProxy(Throwable throwable) {
        this(throwable, null);
    }

    ThrowableProxy(Throwable throwable, Set<Throwable> visited) {
        this.throwable = throwable;
        this.name = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.localizedMessage = throwable.getLocalizedMessage();
        HashMap<String, ThrowableProxyHelper.CacheEntry> map = new HashMap<String, ThrowableProxyHelper.CacheEntry>();
        Deque<Class<?>> stack = StackLocatorUtil.getCurrentStackTrace();
        this.extendedStackTrace = ThrowableProxyHelper.toExtendedStackTrace(this, stack, map, null, throwable.getStackTrace());
        Throwable throwableCause = throwable.getCause();
        HashSet<Throwable> causeVisited = new HashSet<Throwable>(1);
        this.causeProxy = throwableCause == null ? null : new ThrowableProxy(throwable, stack, map, throwableCause, visited, causeVisited);
        this.suppressedProxies = ThrowableProxyHelper.toSuppressedProxies(throwable, visited);
    }

    private ThrowableProxy(Throwable parent, Deque<Class<?>> stack, Map<String, ThrowableProxyHelper.CacheEntry> map, Throwable cause, Set<Throwable> suppressedVisited, Set<Throwable> causeVisited) {
        causeVisited.add(cause);
        this.throwable = cause;
        this.name = cause.getClass().getName();
        this.message = this.throwable.getMessage();
        this.localizedMessage = this.throwable.getLocalizedMessage();
        this.extendedStackTrace = ThrowableProxyHelper.toExtendedStackTrace(this, stack, map, parent.getStackTrace(), cause.getStackTrace());
        Throwable causeCause = cause.getCause();
        this.causeProxy = causeCause == null || causeVisited.contains(causeCause) ? null : new ThrowableProxy(parent, stack, map, causeCause, suppressedVisited, causeVisited);
        this.suppressedProxies = ThrowableProxyHelper.toSuppressedProxies(cause, suppressedVisited);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ThrowableProxy other = (ThrowableProxy)obj;
        if (!Objects.equals(this.causeProxy, other.causeProxy)) {
            return false;
        }
        if (this.commonElementCount != other.commonElementCount) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Arrays.equals(this.extendedStackTrace, other.extendedStackTrace)) {
            return false;
        }
        return Arrays.equals(this.suppressedProxies, other.suppressedProxies);
    }

    public void formatWrapper(StringBuilder sb, ThrowableProxy cause, String suffix) {
        this.formatWrapper(sb, cause, null, PlainTextRenderer.getInstance(), suffix);
    }

    public void formatWrapper(StringBuilder sb, ThrowableProxy cause, List<String> ignorePackages, String suffix) {
        this.formatWrapper(sb, cause, ignorePackages, PlainTextRenderer.getInstance(), suffix);
    }

    public void formatWrapper(StringBuilder sb, ThrowableProxy cause, List<String> ignorePackages, TextRenderer textRenderer, String suffix) {
        this.formatWrapper(sb, cause, ignorePackages, textRenderer, suffix, EOL_STR);
    }

    public void formatWrapper(StringBuilder sb, ThrowableProxy cause, List<String> ignorePackages, TextRenderer textRenderer, String suffix, String lineSeparator) {
        ThrowableProxyRenderer.formatWrapper(sb, cause, ignorePackages, textRenderer, suffix, lineSeparator);
    }

    public ThrowableProxy getCauseProxy() {
        return this.causeProxy;
    }

    public String getCauseStackTraceAsString(String suffix) {
        return this.getCauseStackTraceAsString(null, PlainTextRenderer.getInstance(), suffix, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> packages, String suffix) {
        return this.getCauseStackTraceAsString(packages, PlainTextRenderer.getInstance(), suffix, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> ignorePackages, TextRenderer textRenderer, String suffix) {
        return this.getCauseStackTraceAsString(ignorePackages, textRenderer, suffix, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> ignorePackages, TextRenderer textRenderer, String suffix, String lineSeparator) {
        StringBuilder sb = new StringBuilder();
        ThrowableProxyRenderer.formatCauseStackTrace(this, sb, ignorePackages, textRenderer, suffix, lineSeparator);
        return sb.toString();
    }

    public int getCommonElementCount() {
        return this.commonElementCount;
    }

    void setCommonElementCount(int value) {
        this.commonElementCount = value;
    }

    public ExtendedStackTraceElement[] getExtendedStackTrace() {
        return this.extendedStackTrace;
    }

    public String getExtendedStackTraceAsString() {
        return this.getExtendedStackTraceAsString(null, PlainTextRenderer.getInstance(), "", EOL_STR);
    }

    public String getExtendedStackTraceAsString(String suffix) {
        return this.getExtendedStackTraceAsString(null, PlainTextRenderer.getInstance(), suffix, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> ignorePackages, String suffix) {
        return this.getExtendedStackTraceAsString(ignorePackages, PlainTextRenderer.getInstance(), suffix, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> ignorePackages, TextRenderer textRenderer, String suffix) {
        return this.getExtendedStackTraceAsString(ignorePackages, textRenderer, suffix, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> ignorePackages, TextRenderer textRenderer, String suffix, String lineSeparator) {
        StringBuilder sb = new StringBuilder(1024);
        this.formatExtendedStackTraceTo(sb, ignorePackages, textRenderer, suffix, lineSeparator);
        return sb.toString();
    }

    public void formatExtendedStackTraceTo(StringBuilder sb, List<String> ignorePackages, TextRenderer textRenderer, String suffix, String lineSeparator) {
        ThrowableProxyRenderer.formatExtendedStackTraceTo(this, sb, ignorePackages, textRenderer, suffix, lineSeparator);
    }

    public String getLocalizedMessage() {
        return this.localizedMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public String getName() {
        return this.name;
    }

    public StackTraceElement[] getStackTrace() {
        return this.throwable == null ? null : this.throwable.getStackTrace();
    }

    public ThrowableProxy[] getSuppressedProxies() {
        return this.suppressedProxies;
    }

    public String getSuppressedStackTrace(String suffix) {
        ThrowableProxy[] suppressed = this.getSuppressedProxies();
        if (suppressed == null || suppressed.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder("Suppressed Stack Trace Elements:").append('\n');
        for (ThrowableProxy proxy : suppressed) {
            sb.append(proxy.getExtendedStackTraceAsString(suffix));
        }
        return sb.toString();
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.causeProxy == null ? 0 : this.causeProxy.hashCode());
        result = 31 * result + this.commonElementCount;
        result = 31 * result + (this.extendedStackTrace == null ? 0 : Arrays.hashCode(this.extendedStackTrace));
        result = 31 * result + (this.suppressedProxies == null ? 0 : Arrays.hashCode(this.suppressedProxies));
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    public String toString() {
        String msg = this.message;
        return msg != null ? this.name + ": " + msg : this.name;
    }
}

