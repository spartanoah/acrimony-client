/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl;

import org.lwjgl.SysImplementation;

abstract class DefaultSysImplementation
implements SysImplementation {
    DefaultSysImplementation() {
    }

    public native int getJNIVersion();

    public native int getPointerSize();

    public native void setDebug(boolean var1);

    public long getTimerResolution() {
        return 1000L;
    }

    public boolean has64Bit() {
        return false;
    }

    public abstract long getTime();

    public abstract void alert(String var1, String var2);

    public abstract String getClipboard();
}

