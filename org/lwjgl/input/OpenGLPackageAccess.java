/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.input;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.InputImplementation;

final class OpenGLPackageAccess {
    static final Object global_lock;

    OpenGLPackageAccess() {
    }

    static InputImplementation createImplementation() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InputImplementation>(){

                @Override
                public InputImplementation run() throws Exception {
                    Method getImplementation_method = Display.class.getDeclaredMethod("getImplementation", new Class[0]);
                    getImplementation_method.setAccessible(true);
                    return (InputImplementation)getImplementation_method.invoke(null, new Object[0]);
                }
            });
        } catch (PrivilegedActionException e) {
            throw new Error(e);
        }
    }

    static {
        try {
            global_lock = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                @Override
                public Object run() throws Exception {
                    Field lock_field = Class.forName("org.lwjgl.opengl.GlobalLock").getDeclaredField("lock");
                    lock_field.setAccessible(true);
                    return lock_field.get(null);
                }
            });
        } catch (PrivilegedActionException e) {
            throw new Error(e);
        }
    }
}

