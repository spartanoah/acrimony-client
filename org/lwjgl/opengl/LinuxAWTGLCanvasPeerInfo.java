/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.awt.Canvas;
import java.nio.ByteBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.AWTSurfaceLock;
import org.lwjgl.opengl.LinuxPeerInfo;

final class LinuxAWTGLCanvasPeerInfo
extends LinuxPeerInfo {
    private final Canvas component;
    private final AWTSurfaceLock awt_surface = new AWTSurfaceLock();
    private int screen = -1;

    LinuxAWTGLCanvasPeerInfo(Canvas component) {
        this.component = component;
    }

    protected void doLockAndInitHandle() throws LWJGLException {
        ByteBuffer surface_handle = this.awt_surface.lockAndGetHandle(this.component);
        if (this.screen == -1) {
            try {
                this.screen = LinuxAWTGLCanvasPeerInfo.getScreenFromSurfaceInfo(surface_handle);
            } catch (LWJGLException e) {
                LWJGLUtil.log("Got exception while trying to determine screen: " + e);
                this.screen = 0;
            }
        }
        LinuxAWTGLCanvasPeerInfo.nInitHandle(this.screen, surface_handle, this.getHandle());
    }

    private static native int getScreenFromSurfaceInfo(ByteBuffer var0) throws LWJGLException;

    private static native void nInitHandle(int var0, ByteBuffer var1, ByteBuffer var2) throws LWJGLException;

    protected void doUnlock() throws LWJGLException {
        this.awt_surface.unlock();
    }
}

