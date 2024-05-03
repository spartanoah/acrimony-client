/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Keyboard;
import net.java.games.input.LinuxControllers;
import net.java.games.input.LinuxEventDevice;
import net.java.games.input.Rumbler;

final class LinuxKeyboard
extends Keyboard {
    private final Controller.PortType port;
    private final LinuxEventDevice device;

    protected LinuxKeyboard(LinuxEventDevice device, Component[] components, Controller[] children, Rumbler[] rumblers) throws IOException {
        super(device.getName(), components, children, rumblers);
        this.device = device;
        this.port = device.getPortType();
    }

    public final Controller.PortType getPortType() {
        return this.port;
    }

    protected final boolean getNextDeviceEvent(Event event) throws IOException {
        return LinuxControllers.getNextDeviceEvent(event, this.device);
    }

    public final void pollDevice() throws IOException {
        this.device.pollKeyStates();
    }
}

