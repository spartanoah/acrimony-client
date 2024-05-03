/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.hc.core5.util.Deadline;

public class DeadlineTimeoutException
extends TimeoutException {
    private static final long serialVersionUID = 1L;
    private final Deadline deadline;

    public static DeadlineTimeoutException from(Deadline deadline) {
        return new DeadlineTimeoutException(deadline);
    }

    private DeadlineTimeoutException(Deadline deadline) {
        super(deadline.format(TimeUnit.MILLISECONDS));
        this.deadline = deadline;
    }

    public Deadline getDeadline() {
        return this.deadline;
    }
}

