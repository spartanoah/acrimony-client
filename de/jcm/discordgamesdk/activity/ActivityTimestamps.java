/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

import java.time.Instant;

public class ActivityTimestamps {
    private final long pointer;

    ActivityTimestamps(long pointer) {
        this.pointer = pointer;
    }

    public void setStart(Instant start) {
        this.setStart(this.pointer, start.getEpochSecond());
    }

    public Instant getStart() {
        return Instant.ofEpochSecond(this.getStart(this.pointer));
    }

    public void setEnd(Instant end) {
        this.setEnd(this.pointer, end.getEpochSecond());
    }

    public Instant getEnd() {
        return Instant.ofEpochSecond(this.getEnd(this.pointer));
    }

    private native void setStart(long var1, long var3);

    private native long getStart(long var1);

    private native void setEnd(long var1, long var3);

    private native long getEnd(long var1);
}

