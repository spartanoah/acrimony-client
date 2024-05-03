/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.IOException;
import net.java.games.input.AbstractComponent;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Mouse;
import net.java.games.input.RawDevice;
import net.java.games.input.RawMouseEvent;
import net.java.games.input.Rumbler;

final class RawMouse
extends Mouse {
    private static final int EVENT_DONE = 1;
    private static final int EVENT_X = 2;
    private static final int EVENT_Y = 3;
    private static final int EVENT_Z = 4;
    private static final int EVENT_BUTTON_0 = 5;
    private static final int EVENT_BUTTON_1 = 6;
    private static final int EVENT_BUTTON_2 = 7;
    private static final int EVENT_BUTTON_3 = 8;
    private static final int EVENT_BUTTON_4 = 9;
    private final RawDevice device;
    private final RawMouseEvent current_event = new RawMouseEvent();
    private int event_state = 1;

    protected RawMouse(String name, RawDevice device, Component[] components, Controller[] children, Rumbler[] rumblers) throws IOException {
        super(name, components, children, rumblers);
        this.device = device;
    }

    public final void pollDevice() throws IOException {
        this.device.pollMouse();
    }

    private static final boolean makeButtonEvent(RawMouseEvent mouse_event, Event event, Component button_component, int down_flag, int up_flag) {
        if ((mouse_event.getButtonFlags() & down_flag) != 0) {
            event.set(button_component, 1.0f, mouse_event.getNanos());
            return true;
        }
        if ((mouse_event.getButtonFlags() & up_flag) != 0) {
            event.set(button_component, 0.0f, mouse_event.getNanos());
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected final synchronized boolean getNextDeviceEvent(Event event) throws IOException {
        block11: while (true) {
            switch (this.event_state) {
                case 1: {
                    if (!this.device.getNextMouseEvent(this.current_event)) {
                        return false;
                    }
                    this.event_state = 2;
                    continue block11;
                }
                case 2: {
                    int rel_x = this.device.getEventRelativeX();
                    this.event_state = 3;
                    if (rel_x == 0) continue block11;
                    event.set(this.getX(), rel_x, this.current_event.getNanos());
                    return true;
                }
                case 3: {
                    int rel_y = this.device.getEventRelativeY();
                    this.event_state = 4;
                    if (rel_y == 0) continue block11;
                    event.set(this.getY(), rel_y, this.current_event.getNanos());
                    return true;
                }
                case 4: {
                    int wheel = this.current_event.getWheelDelta();
                    this.event_state = 5;
                    if (wheel == 0) continue block11;
                    event.set(this.getWheel(), wheel, this.current_event.getNanos());
                    return true;
                }
                case 5: {
                    this.event_state = 6;
                    if (!RawMouse.makeButtonEvent(this.current_event, event, this.getPrimaryButton(), 1, 2)) continue block11;
                    return true;
                }
                case 6: {
                    this.event_state = 7;
                    if (!RawMouse.makeButtonEvent(this.current_event, event, this.getSecondaryButton(), 4, 8)) continue block11;
                    return true;
                }
                case 7: {
                    this.event_state = 8;
                    if (!RawMouse.makeButtonEvent(this.current_event, event, this.getTertiaryButton(), 16, 32)) continue block11;
                    return true;
                }
                case 8: {
                    this.event_state = 9;
                    if (!RawMouse.makeButtonEvent(this.current_event, event, this.getButton3(), 64, 128)) continue block11;
                    return true;
                }
                case 9: {
                    this.event_state = 1;
                    if (RawMouse.makeButtonEvent(this.current_event, event, this.getButton4(), 256, 512)) return true;
                    continue block11;
                }
            }
            break;
        }
        throw new RuntimeException("Unknown event state: " + this.event_state);
    }

    protected final void setDeviceEventQueueSize(int size) throws IOException {
        this.device.setBufferSize(size);
    }

    static final class Button
    extends AbstractComponent {
        private final RawDevice device;
        private final int button_id;

        public Button(RawDevice device, Component.Identifier.Button id, int button_id) {
            super(id.getName(), id);
            this.device = device;
            this.button_id = button_id;
        }

        protected final float poll() throws IOException {
            return this.device.getButtonState(this.button_id) ? 1.0f : 0.0f;
        }

        public final boolean isAnalog() {
            return false;
        }

        public final boolean isRelative() {
            return false;
        }
    }

    static final class Axis
    extends AbstractComponent {
        private final RawDevice device;

        public Axis(RawDevice device, Component.Identifier.Axis axis) {
            super(axis.getName(), axis);
            this.device = device;
        }

        public final boolean isRelative() {
            return true;
        }

        public final boolean isAnalog() {
            return true;
        }

        protected final float poll() throws IOException {
            if (this.getIdentifier() == Component.Identifier.Axis.X) {
                return this.device.getRelativeX();
            }
            if (this.getIdentifier() == Component.Identifier.Axis.Y) {
                return this.device.getRelativeY();
            }
            if (this.getIdentifier() == Component.Identifier.Axis.Z) {
                return this.device.getWheel();
            }
            throw new RuntimeException("Unknown raw axis: " + this.getIdentifier());
        }
    }
}

