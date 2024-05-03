/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DiscordUtils {
    private DiscordUtils() {
        throw new RuntimeException("DiscordUtils is a static class and no instance of it can be obtained.");
    }

    public static Consumer<Result> completer(CompletableFuture<Void> future) {
        return result -> {
            if (result == Result.OK) {
                future.complete(null);
            } else {
                future.completeExceptionally(new GameSDKException((Result)((Object)result)));
            }
        };
    }

    public static <T> BiConsumer<Result, T> returningCompleter(CompletableFuture<T> future) {
        return (result, t) -> {
            if (result == Result.OK) {
                future.complete(t);
            } else {
                future.completeExceptionally(new GameSDKException((Result)((Object)result)));
            }
        };
    }

    public static Instant dateTimeFromSnowflake(long snowflake) {
        long discordTime = snowflake >> 22;
        long unixTime = discordTime + 1420070400000L;
        return Instant.ofEpochMilli(unixTime);
    }
}

