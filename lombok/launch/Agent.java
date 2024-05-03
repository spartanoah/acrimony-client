/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package lombok.launch;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.launch.Main;

final class Agent {
    Agent() {
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Throwable {
        Agent.runLauncher(agentArgs, instrumentation, true);
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Throwable {
        Agent.runLauncher(agentArgs, instrumentation, false);
    }

    private static void runLauncher(String agentArgs, Instrumentation instrumentation, boolean injected) throws Throwable {
        ClassLoader cl = Main.getShadowClassLoader();
        try {
            Class<?> c = cl.loadClass("lombok.core.AgentLauncher");
            Method m = c.getDeclaredMethod("runAgents", String.class, Instrumentation.class, Boolean.TYPE, Class.class);
            m.invoke(null, agentArgs, instrumentation, injected, Agent.class);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}

