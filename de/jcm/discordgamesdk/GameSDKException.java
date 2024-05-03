/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Result;

public class GameSDKException
extends RuntimeException {
    private Result result;

    public GameSDKException(Result result) {
        super("Game SDK operation failed: " + (Object)((Object)result));
    }

    public Result getResult() {
        return this.result;
    }
}

