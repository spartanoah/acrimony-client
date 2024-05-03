/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.Event;
import net.java.games.input.OSXComponent;
import net.java.games.input.OSXEvent;
import net.java.games.input.OSXHIDElement;
import net.java.games.input.OSXHIDQueue;

final class OSXControllers {
    private static final OSXEvent osx_event = new OSXEvent();

    OSXControllers() {
    }

    public static final synchronized float poll(OSXHIDElement element) throws IOException {
        element.getElementValue(osx_event);
        return element.convertValue(osx_event.getValue());
    }

    public static final synchronized boolean getNextDeviceEvent(Event event, OSXHIDQueue queue) throws IOException {
        if (queue.getNextEvent(osx_event)) {
            OSXComponent component = queue.mapEvent(osx_event);
            event.set(component, component.getElement().convertValue(osx_event.getValue()), osx_event.getNanos());
            return true;
        }
        return false;
    }
}

