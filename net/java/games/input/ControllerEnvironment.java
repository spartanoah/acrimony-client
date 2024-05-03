/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.DefaultControllerEnvironment;

public abstract class ControllerEnvironment {
    private static ControllerEnvironment defaultEnvironment;
    protected final ArrayList controllerListeners = new ArrayList();
    static final /* synthetic */ boolean $assertionsDisabled;

    static void logln(String msg) {
        ControllerEnvironment.log(msg + "\n");
    }

    static void log(String msg) {
        Logger.getLogger(ControllerEnvironment.class.getName()).info(msg);
    }

    protected ControllerEnvironment() {
    }

    public abstract Controller[] getControllers();

    public void addControllerListener(ControllerListener l) {
        if (!$assertionsDisabled && l == null) {
            throw new AssertionError();
        }
        this.controllerListeners.add(l);
    }

    public abstract boolean isSupported();

    public void removeControllerListener(ControllerListener l) {
        if (!$assertionsDisabled && l == null) {
            throw new AssertionError();
        }
        this.controllerListeners.remove(l);
    }

    protected void fireControllerAdded(Controller c) {
        ControllerEvent ev = new ControllerEvent(c);
        Iterator it = this.controllerListeners.iterator();
        while (it.hasNext()) {
            ((ControllerListener)it.next()).controllerAdded(ev);
        }
    }

    protected void fireControllerRemoved(Controller c) {
        ControllerEvent ev = new ControllerEvent(c);
        Iterator it = this.controllerListeners.iterator();
        while (it.hasNext()) {
            ((ControllerListener)it.next()).controllerRemoved(ev);
        }
    }

    public static ControllerEnvironment getDefaultEnvironment() {
        return defaultEnvironment;
    }

    static {
        $assertionsDisabled = !ControllerEnvironment.class.desiredAssertionStatus();
        defaultEnvironment = new DefaultControllerEnvironment();
    }
}

