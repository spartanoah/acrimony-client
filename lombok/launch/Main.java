/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package lombok.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import lombok.launch.ShadowClassLoader;

class Main {
    private static ShadowClassLoader classLoader;

    Main() {
    }

    static synchronized ClassLoader getShadowClassLoader() {
        if (classLoader == null) {
            classLoader = new ShadowClassLoader(Main.class.getClassLoader(), "lombok", null, Arrays.asList(new String[0]), Arrays.asList("lombok.patcher.Symbols"));
        }
        return classLoader;
    }

    static synchronized void prependClassLoader(ClassLoader loader) {
        Main.getShadowClassLoader();
        classLoader.prependParent(loader);
    }

    public static void main(String[] args) throws Throwable {
        ClassLoader cl = Main.getShadowClassLoader();
        Class<?> mc = cl.loadClass("lombok.core.Main");
        try {
            mc.getMethod("main", String[].class).invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}

