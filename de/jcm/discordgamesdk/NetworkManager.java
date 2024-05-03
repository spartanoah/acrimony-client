/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;

public class NetworkManager {
    private final long pointer;
    private final Core core;

    NetworkManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public long getPeerId() {
        return this.core.execute(() -> this.getPeerId(this.pointer));
    }

    public void flush() {
        Result result = this.core.execute(() -> this.flush(this.pointer));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void openPeer(long peerId, String routeData) {
        Result result = this.core.execute(() -> this.openPeer(this.pointer, peerId, routeData));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void updatePeer(long peerId, String routeData) {
        Result result = this.core.execute(() -> this.updatePeer(this.pointer, peerId, routeData));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void closePeer(long peerId) {
        Result result = this.core.execute(() -> this.closePeer(this.pointer, peerId));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void openChannel(long peerId, byte channelId, boolean reliable) {
        Result result = this.core.execute(() -> this.openChannel(this.pointer, peerId, channelId, reliable));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void closeChannel(long peerId, byte channelId) {
        Result result = this.core.execute(() -> this.closeChannel(this.pointer, peerId, channelId));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void sendMessage(long peerId, byte channelId, byte[] data) {
        Result result = this.core.execute(() -> this.sendMessage(this.pointer, peerId, channelId, data, 0, data.length));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    private native long getPeerId(long var1);

    private native Result flush(long var1);

    private native Result openPeer(long var1, long var3, String var5);

    private native Result updatePeer(long var1, long var3, String var5);

    private native Result closePeer(long var1, long var3);

    private native Result openChannel(long var1, long var3, byte var5, boolean var6);

    private native Result closeChannel(long var1, long var3, byte var5);

    private native Result sendMessage(long var1, long var3, byte var5, byte[] var6, int var7, int var8);
}

