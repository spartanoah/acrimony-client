/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.util.concurrent.TimeoutException;
import org.apache.hc.core5.util.Timeout;

public class TimeoutValueException
extends TimeoutException {
    private static final long serialVersionUID = 1L;
    private final Timeout actual;
    private final Timeout deadline;

    public static TimeoutValueException fromMilliseconds(long timeoutDeadline, long timeoutActual) {
        return new TimeoutValueException(Timeout.ofMilliseconds(TimeoutValueException.min0(timeoutDeadline)), Timeout.ofMilliseconds(TimeoutValueException.min0(timeoutActual)));
    }

    private static long min0(long value) {
        return value < 0L ? 0L : value;
    }

    public TimeoutValueException(Timeout deadline, Timeout actual) {
        super(String.format("Timeout deadline: %s, actual: %s", deadline, actual));
        this.actual = actual;
        this.deadline = deadline;
    }

    public Timeout getActual() {
        return this.actual;
    }

    public Timeout getDeadline() {
        return this.deadline;
    }
}

