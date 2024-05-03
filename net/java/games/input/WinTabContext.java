/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.util.ArrayList;
import net.java.games.input.Controller;
import net.java.games.input.DummyWindow;
import net.java.games.input.WinTabDevice;
import net.java.games.input.WinTabPacket;

public class WinTabContext {
    private DummyWindow window;
    private long hCTX;
    private Controller[] controllers;

    public WinTabContext(DummyWindow window) {
        this.window = window;
    }

    public Controller[] getControllers() {
        if (this.hCTX == 0L) {
            throw new IllegalStateException("Context must be open before getting the controllers");
        }
        return this.controllers;
    }

    public synchronized void open() {
        this.hCTX = WinTabContext.nOpen(this.window.getHwnd());
        ArrayList<WinTabDevice> devices = new ArrayList<WinTabDevice>();
        int numSupportedDevices = WinTabContext.nGetNumberOfSupportedDevices();
        for (int i = 0; i < numSupportedDevices; ++i) {
            WinTabDevice newDevice = WinTabDevice.createDevice(this, i);
            if (newDevice == null) continue;
            devices.add(newDevice);
        }
        this.controllers = devices.toArray(new Controller[0]);
    }

    public synchronized void close() {
        WinTabContext.nClose(this.hCTX);
    }

    public synchronized void processEvents() {
        WinTabPacket[] packets = WinTabContext.nGetPackets(this.hCTX);
        for (int i = 0; i < packets.length; ++i) {
            ((WinTabDevice)this.getControllers()[0]).processPacket(packets[i]);
        }
    }

    private static final native int nGetNumberOfSupportedDevices();

    private static final native long nOpen(long var0);

    private static final native void nClose(long var0);

    private static final native WinTabPacket[] nGetPackets(long var0);
}

