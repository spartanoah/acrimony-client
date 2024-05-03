/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.AbstractController;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.LinuxAbstractController;
import net.java.games.input.LinuxJoystickAbstractController;

public class LinuxCombinedController
extends AbstractController {
    private LinuxAbstractController eventController;
    private LinuxJoystickAbstractController joystickController;

    LinuxCombinedController(LinuxAbstractController eventController, LinuxJoystickAbstractController joystickController) {
        super(eventController.getName(), joystickController.getComponents(), eventController.getControllers(), eventController.getRumblers());
        this.eventController = eventController;
        this.joystickController = joystickController;
    }

    protected boolean getNextDeviceEvent(Event event) throws IOException {
        return this.joystickController.getNextDeviceEvent(event);
    }

    public final Controller.PortType getPortType() {
        return this.eventController.getPortType();
    }

    public final void pollDevice() throws IOException {
        this.eventController.pollDevice();
        this.joystickController.pollDevice();
    }

    public Controller.Type getType() {
        return this.eventController.getType();
    }
}

