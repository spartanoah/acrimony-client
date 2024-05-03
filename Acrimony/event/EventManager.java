/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event;

import Acrimony.Acrimony;
import Acrimony.event.Event;
import Acrimony.event.Listener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final ArrayList<Object> listeningObjects = new ArrayList();
    private final CopyOnWriteArrayList<ListeningMethod> listeningMethods = new CopyOnWriteArrayList();

    public void register(Object o) {
        if (!this.listeningObjects.contains(o)) {
            this.listeningObjects.add(o);
        }
        this.updateListeningMethods();
    }

    public void unregister(Object o) {
        if (this.listeningObjects.contains(o)) {
            this.listeningObjects.remove(o);
        }
        this.updateListeningMethods();
    }

    private void updateListeningMethods() {
        this.listeningMethods.clear();
        this.listeningObjects.forEach(o -> Arrays.stream(o.getClass().getMethods()).filter(m -> m.isAnnotationPresent(Listener.class) && m.getParameters().length == 1).forEach(m -> this.listeningMethods.add(new ListeningMethod((Method)m, o))));
        this.listeningMethods.sort(Comparator.comparingInt(m -> ((ListeningMethod)m).method.getAnnotation(Listener.class).value()));
    }

    public void post(Event e) {
        if (Acrimony.instance.isDestructed()) {
            return;
        }
        this.listeningMethods.forEach(m -> Arrays.stream(((ListeningMethod)m).method.getParameters()).filter(p -> p.getType().equals(e.getClass())).forEach(p -> {
            try {
                ((ListeningMethod)m).method.invoke(((ListeningMethod)m).instance, e);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    private class ListeningMethod {
        private final Method method;
        private final Object instance;

        private ListeningMethod(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }
    }
}

