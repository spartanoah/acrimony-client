/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.PeerInfo;

abstract class LinuxPeerInfo
extends PeerInfo {
    LinuxPeerInfo() {
        super(LinuxPeerInfo.createHandle());
    }

    private static native ByteBuffer createHandle();

    public final long getDisplay() {
        return LinuxPeerInfo.nGetDisplay(this.getHandle());
    }

    private static native long nGetDisplay(ByteBuffer var0);

    public final long getDrawable() {
        return LinuxPeerInfo.nGetDrawable(this.getHandle());
    }

    private static native long nGetDrawable(ByteBuffer var0);
}

