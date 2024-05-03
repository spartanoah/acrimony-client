/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultHttpRequestRetryStrategy
implements HttpRequestRetryStrategy {
    public static final DefaultHttpRequestRetryStrategy INSTANCE = new DefaultHttpRequestRetryStrategy();
    private final int maxRetries;
    private final TimeValue defaultRetryInterval;
    private final Set<Class<? extends IOException>> nonRetriableIOExceptionClasses;
    private final Set<Integer> retriableCodes;

    protected DefaultHttpRequestRetryStrategy(int maxRetries, TimeValue defaultRetryInterval, Collection<Class<? extends IOException>> clazzes, Collection<Integer> codes) {
        Args.notNegative(maxRetries, "maxRetries");
        Args.notNegative(defaultRetryInterval.getDuration(), "defaultRetryInterval");
        this.maxRetries = maxRetries;
        this.defaultRetryInterval = defaultRetryInterval;
        this.nonRetriableIOExceptionClasses = new HashSet<Class<? extends IOException>>(clazzes);
        this.retriableCodes = new HashSet<Integer>(codes);
    }

    public DefaultHttpRequestRetryStrategy(int maxRetries, TimeValue defaultRetryInterval) {
        this(maxRetries, defaultRetryInterval, Arrays.asList(InterruptedIOException.class, UnknownHostException.class, ConnectException.class, ConnectionClosedException.class, SSLException.class), Arrays.asList(429, 503));
    }

    public DefaultHttpRequestRetryStrategy() {
        this(1, TimeValue.ofSeconds(1L));
    }

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        Args.notNull(request, "request");
        Args.notNull(exception, "exception");
        if (execCount > this.maxRetries) {
            return false;
        }
        if (this.nonRetriableIOExceptionClasses.contains(exception.getClass())) {
            return false;
        }
        for (Class<? extends IOException> rejectException : this.nonRetriableIOExceptionClasses) {
            if (!rejectException.isInstance(exception)) continue;
            return false;
        }
        if (request instanceof CancellableDependency && ((CancellableDependency)((Object)request)).isCancelled()) {
            return false;
        }
        return this.handleAsIdempotent(request);
    }

    @Override
    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        Args.notNull(response, "response");
        return execCount <= this.maxRetries && this.retriableCodes.contains(response.getCode());
    }

    @Override
    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
        Args.notNull(response, "response");
        Header header = response.getFirstHeader("Retry-After");
        TimeValue retryAfter = null;
        if (header != null) {
            block4: {
                String value = header.getValue();
                try {
                    retryAfter = TimeValue.ofSeconds(Long.parseLong(value));
                } catch (NumberFormatException ignore) {
                    Date retryAfterDate = DateUtils.parseDate(value);
                    if (retryAfterDate == null) break block4;
                    retryAfter = TimeValue.ofMilliseconds(retryAfterDate.getTime() - System.currentTimeMillis());
                }
            }
            if (TimeValue.isPositive(retryAfter)) {
                return retryAfter;
            }
        }
        return this.defaultRetryInterval;
    }

    protected boolean handleAsIdempotent(HttpRequest request) {
        return Method.isIdempotent(request.getMethod());
    }
}

