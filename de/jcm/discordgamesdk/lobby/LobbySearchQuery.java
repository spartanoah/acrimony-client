/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.lobby;

import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;

public class LobbySearchQuery {
    private final long pointer;

    LobbySearchQuery(long pointer) {
        this.pointer = pointer;
    }

    public long getPointer() {
        return this.pointer;
    }

    public LobbySearchQuery filter(String key, Comparison comparison, Cast cast, String value) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        if (value.getBytes().length >= 4096) {
            throw new IllegalArgumentException("max value length is 4095");
        }
        Result result = this.filter(this.pointer, key, comparison.nativeValue(), cast.nativeValue(), value);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
        return this;
    }

    public LobbySearchQuery sort(String key, Cast cast, String baseValue) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        Result result = this.sort(this.pointer, key, cast.nativeValue(), baseValue);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
        return this;
    }

    public LobbySearchQuery limit(int limit) {
        Result result = this.limit(this.pointer, limit);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
        return this;
    }

    public LobbySearchQuery distance(Distance distance) {
        Result result = this.distance(this.pointer, distance.nativeValue());
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
        return this;
    }

    private native Result filter(long var1, String var3, int var4, int var5, String var6);

    private native Result sort(long var1, String var3, int var4, String var5);

    private native Result limit(long var1, int var3);

    private native Result distance(long var1, int var3);

    public static enum Distance {
        LOCAL,
        DEFAULT,
        EXTENDED,
        GLOBAL;

        private static final int OFFSET = 0;

        private int nativeValue() {
            return this.ordinal() + 0;
        }
    }

    public static enum Cast {
        STRING,
        NUMBER;

        private static final int OFFSET = 1;

        private int nativeValue() {
            return this.ordinal() + 1;
        }
    }

    public static enum Comparison {
        LESS_THAN_OR_EQUAL,
        LESS_THAN,
        EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        NOT_EQUAL;

        private static final int OFFSET = -2;

        private int nativeValue() {
            return this.ordinal() + -2;
        }
    }
}

