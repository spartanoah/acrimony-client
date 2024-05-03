/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.DiscordEventAdapter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class CreateParams
implements AutoCloseable {
    private final long pointer;
    private final AtomicBoolean open = new AtomicBoolean(true);

    public CreateParams() {
        this.pointer = this.allocate();
    }

    public void setClientID(long id) {
        this.setClientID(this.pointer, id);
    }

    public long getClientID() {
        return this.getClientID(this.pointer);
    }

    public void setFlags(Flags ... flags) {
        this.setFlags(this.pointer, Flags.toLong(flags));
    }

    public void setFlags(long flags) {
        this.setFlags(this.pointer, flags);
    }

    public long getFlags() {
        return this.getFlags(this.pointer);
    }

    public void registerEventHandler(DiscordEventAdapter eventHandler) {
        this.registerEventHandler(this.pointer, Objects.requireNonNull(eventHandler));
    }

    private native long allocate();

    private native void free(long var1);

    private native void setClientID(long var1, long var3);

    private native long getClientID(long var1);

    private native void setFlags(long var1, long var3);

    private native long getFlags(long var1);

    private native void registerEventHandler(long var1, DiscordEventAdapter var3);

    public static native long getDefaultFlags();

    @Override
    public void close() {
        if (this.open.compareAndSet(true, false)) {
            this.free(this.pointer);
        }
    }

    public long getPointer() {
        return this.pointer;
    }

    public static enum Flags {
        DEFAULT(0L),
        NO_REQUIRE_DISCORD(1L);

        private final long value;

        private Flags(long value) {
            this.value = value;
        }

        public static long toLong(Flags ... flags) {
            long l = 0L;
            for (Flags f : flags) {
                l |= f.value;
            }
            return l;
        }

        public static Flags[] fromLong(long l) {
            return (Flags[])Stream.of(Flags.values()).filter(f -> (l & f.value) != 0L).toArray(Flags[]::new);
        }
    }
}

