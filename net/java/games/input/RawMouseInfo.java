/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.DIIdentifierMap;
import net.java.games.input.RawDevice;
import net.java.games.input.RawDeviceInfo;
import net.java.games.input.RawMouse;
import net.java.games.input.Rumbler;
import net.java.games.input.SetupAPIDevice;

class RawMouseInfo
extends RawDeviceInfo {
    private final RawDevice device;
    private final int id;
    private final int num_buttons;
    private final int sample_rate;

    public RawMouseInfo(RawDevice device, int id, int num_buttons, int sample_rate) {
        this.device = device;
        this.id = id;
        this.num_buttons = num_buttons;
        this.sample_rate = sample_rate;
    }

    public final int getUsage() {
        return 2;
    }

    public final int getUsagePage() {
        return 1;
    }

    public final long getHandle() {
        return this.device.getHandle();
    }

    public final Controller createControllerFromDevice(RawDevice device, SetupAPIDevice setupapi_device) throws IOException {
        if (this.num_buttons == 0) {
            return null;
        }
        Component[] components = new Component[3 + this.num_buttons];
        int index = 0;
        components[index++] = new RawMouse.Axis(device, Component.Identifier.Axis.X);
        components[index++] = new RawMouse.Axis(device, Component.Identifier.Axis.Y);
        components[index++] = new RawMouse.Axis(device, Component.Identifier.Axis.Z);
        for (int i = 0; i < this.num_buttons; ++i) {
            Component.Identifier.Button id = DIIdentifierMap.mapMouseButtonIdentifier(DIIdentifierMap.getButtonIdentifier(i));
            components[index++] = new RawMouse.Button(device, id, i);
        }
        RawMouse mouse = new RawMouse(setupapi_device.getName(), device, components, new Controller[0], new Rumbler[0]);
        return mouse;
    }
}

