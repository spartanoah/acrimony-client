/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.LinuxDevice;
import net.java.games.input.LinuxJoystickAxis;
import net.java.games.input.LinuxJoystickButton;
import net.java.games.input.LinuxJoystickEvent;
import net.java.games.input.LinuxJoystickPOV;

final class LinuxJoystickDevice
implements LinuxDevice {
    public static final int JS_EVENT_BUTTON = 1;
    public static final int JS_EVENT_AXIS = 2;
    public static final int JS_EVENT_INIT = 128;
    public static final int AXIS_MAX_VALUE = Short.MAX_VALUE;
    private final long fd;
    private final String name;
    private final LinuxJoystickEvent joystick_event = new LinuxJoystickEvent();
    private final Event event = new Event();
    private final LinuxJoystickButton[] buttons;
    private final LinuxJoystickAxis[] axes;
    private final Map povXs = new HashMap();
    private final Map povYs = new HashMap();
    private final byte[] axisMap;
    private final char[] buttonMap;
    private EventQueue event_queue;
    private boolean closed;

    public LinuxJoystickDevice(String filename) throws IOException {
        this.fd = LinuxJoystickDevice.nOpen(filename);
        try {
            this.name = this.getDeviceName();
            this.setBufferSize(32);
            this.buttons = new LinuxJoystickButton[this.getNumDeviceButtons()];
            this.axes = new LinuxJoystickAxis[this.getNumDeviceAxes()];
            this.axisMap = this.getDeviceAxisMap();
            this.buttonMap = this.getDeviceButtonMap();
        } catch (IOException e) {
            this.close();
            throw e;
        }
    }

    private static final native long nOpen(String var0) throws IOException;

    public final synchronized void setBufferSize(int size) {
        this.event_queue = new EventQueue(size);
    }

    private final void processEvent(LinuxJoystickEvent joystick_event) {
        int index = joystick_event.getNumber();
        int type = joystick_event.getType() & 0xFFFFFF7F;
        switch (type) {
            case 1: {
                LinuxJoystickButton button;
                if (index < this.getNumButtons() && (button = this.buttons[index]) != null) {
                    float value = joystick_event.getValue();
                    button.setValue(value);
                    this.event.set(button, value, joystick_event.getNanos());
                    break;
                }
                return;
            }
            case 2: {
                LinuxJoystickAxis axis;
                if (index < this.getNumAxes() && (axis = this.axes[index]) != null) {
                    float value = (float)joystick_event.getValue() / 32767.0f;
                    axis.setValue(value);
                    if (this.povXs.containsKey(new Integer(index))) {
                        LinuxJoystickPOV pov = (LinuxJoystickPOV)this.povXs.get(new Integer(index));
                        pov.updateValue();
                        this.event.set(pov, pov.getPollData(), joystick_event.getNanos());
                        break;
                    }
                    if (this.povYs.containsKey(new Integer(index))) {
                        LinuxJoystickPOV pov = (LinuxJoystickPOV)this.povYs.get(new Integer(index));
                        pov.updateValue();
                        this.event.set(pov, pov.getPollData(), joystick_event.getNanos());
                        break;
                    }
                    this.event.set(axis, value, joystick_event.getNanos());
                    break;
                }
                return;
            }
            default: {
                return;
            }
        }
        if (!this.event_queue.isFull()) {
            this.event_queue.add(this.event);
        }
    }

    public final void registerAxis(int index, LinuxJoystickAxis axis) {
        this.axes[index] = axis;
    }

    public final void registerButton(int index, LinuxJoystickButton button) {
        this.buttons[index] = button;
    }

    public final void registerPOV(LinuxJoystickPOV pov) {
        int yIndex;
        int xIndex;
        LinuxJoystickAxis xAxis = pov.getYAxis();
        LinuxJoystickAxis yAxis = pov.getXAxis();
        for (xIndex = 0; xIndex < this.axes.length && this.axes[xIndex] != xAxis; ++xIndex) {
        }
        for (yIndex = 0; yIndex < this.axes.length && this.axes[yIndex] != yAxis; ++yIndex) {
        }
        this.povXs.put(new Integer(xIndex), pov);
        this.povYs.put(new Integer(yIndex), pov);
    }

    public final synchronized boolean getNextEvent(Event event) throws IOException {
        return this.event_queue.getNextEvent(event);
    }

    public final synchronized void poll() throws IOException {
        this.checkClosed();
        while (this.getNextDeviceEvent(this.joystick_event)) {
            this.processEvent(this.joystick_event);
        }
    }

    private final boolean getNextDeviceEvent(LinuxJoystickEvent joystick_event) throws IOException {
        return LinuxJoystickDevice.nGetNextEvent(this.fd, joystick_event);
    }

    private static final native boolean nGetNextEvent(long var0, LinuxJoystickEvent var2) throws IOException;

    public final int getNumAxes() {
        return this.axes.length;
    }

    public final int getNumButtons() {
        return this.buttons.length;
    }

    public final byte[] getAxisMap() {
        return this.axisMap;
    }

    public final char[] getButtonMap() {
        return this.buttonMap;
    }

    private final int getNumDeviceButtons() throws IOException {
        return LinuxJoystickDevice.nGetNumButtons(this.fd);
    }

    private static final native int nGetNumButtons(long var0) throws IOException;

    private final int getNumDeviceAxes() throws IOException {
        return LinuxJoystickDevice.nGetNumAxes(this.fd);
    }

    private static final native int nGetNumAxes(long var0) throws IOException;

    private final byte[] getDeviceAxisMap() throws IOException {
        return LinuxJoystickDevice.nGetAxisMap(this.fd);
    }

    private static final native byte[] nGetAxisMap(long var0) throws IOException;

    private final char[] getDeviceButtonMap() throws IOException {
        return LinuxJoystickDevice.nGetButtonMap(this.fd);
    }

    private static final native char[] nGetButtonMap(long var0) throws IOException;

    private final int getVersion() throws IOException {
        return LinuxJoystickDevice.nGetVersion(this.fd);
    }

    private static final native int nGetVersion(long var0) throws IOException;

    public final String getName() {
        return this.name;
    }

    private final String getDeviceName() throws IOException {
        return LinuxJoystickDevice.nGetName(this.fd);
    }

    private static final native String nGetName(long var0) throws IOException;

    public final synchronized void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            LinuxJoystickDevice.nClose(this.fd);
        }
    }

    private static final native void nClose(long var0) throws IOException;

    private final void checkClosed() throws IOException {
        if (this.closed) {
            throw new IOException("Device is closed");
        }
    }

    protected void finalize() throws IOException {
        this.close();
    }
}

