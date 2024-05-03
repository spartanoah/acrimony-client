/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;

final class WindowsRegistry {
    static final int HKEY_CLASSES_ROOT = 1;
    static final int HKEY_CURRENT_USER = 2;
    static final int HKEY_LOCAL_MACHINE = 3;
    static final int HKEY_USERS = 4;

    WindowsRegistry() {
    }

    static String queryRegistrationKey(int root_key, String subkey, String value) throws LWJGLException {
        switch (root_key) {
            case 1: 
            case 2: 
            case 3: 
            case 4: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid enum: " + root_key);
            }
        }
        return WindowsRegistry.nQueryRegistrationKey(root_key, subkey, value);
    }

    private static native String nQueryRegistrationKey(int var0, String var1, String var2) throws LWJGLException;

    static {
        Sys.initialize();
    }
}

