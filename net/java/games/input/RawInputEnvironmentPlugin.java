/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.RawDevice;
import net.java.games.input.RawDeviceInfo;
import net.java.games.input.RawInputEventQueue;
import net.java.games.input.SetupAPIDevice;
import net.java.games.util.plugins.Plugin;

public final class RawInputEnvironmentPlugin
extends ControllerEnvironment
implements Plugin {
    private static boolean supported = false;
    private final Controller[] controllers;

    static void loadLibrary(final String lib_name) {
        AccessController.doPrivileged(new PrivilegedAction(){

            public final Object run() {
                try {
                    String lib_path = System.getProperty("net.java.games.input.librarypath");
                    if (lib_path != null) {
                        System.load(lib_path + File.separator + System.mapLibraryName(lib_name));
                    } else {
                        System.loadLibrary(lib_name);
                    }
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                    supported = false;
                }
                return null;
            }
        });
    }

    static String getPrivilegedProperty(final String property) {
        return (String)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return System.getProperty(property);
            }
        });
    }

    static String getPrivilegedProperty(final String property, final String default_value) {
        return (String)AccessController.doPrivileged(new PrivilegedAction(){

            public Object run() {
                return System.getProperty(property, default_value);
            }
        });
    }

    public RawInputEnvironmentPlugin() {
        Controller[] controllers = new Controller[]{};
        if (this.isSupported()) {
            try {
                RawInputEventQueue queue = new RawInputEventQueue();
                controllers = this.enumControllers(queue);
            } catch (IOException e) {
                RawInputEnvironmentPlugin.logln("Failed to enumerate devices: " + e.getMessage());
            }
        }
        this.controllers = controllers;
    }

    public final Controller[] getControllers() {
        return this.controllers;
    }

    private static final SetupAPIDevice lookupSetupAPIDevice(String device_name, List setupapi_devices) {
        device_name = device_name.replaceAll("#", "\\\\").toUpperCase();
        for (int i = 0; i < setupapi_devices.size(); ++i) {
            SetupAPIDevice device = (SetupAPIDevice)setupapi_devices.get(i);
            if (device_name.indexOf(device.getInstanceId().toUpperCase()) == -1) continue;
            return device;
        }
        return null;
    }

    private static final void createControllersFromDevices(RawInputEventQueue queue, List controllers, List devices, List setupapi_devices) throws IOException {
        ArrayList<RawDevice> active_devices = new ArrayList<RawDevice>();
        for (int i = 0; i < devices.size(); ++i) {
            RawDeviceInfo info;
            Controller controller;
            RawDevice device = (RawDevice)devices.get(i);
            SetupAPIDevice setupapi_device = RawInputEnvironmentPlugin.lookupSetupAPIDevice(device.getName(), setupapi_devices);
            if (setupapi_device == null || (controller = (info = device.getInfo()).createControllerFromDevice(device, setupapi_device)) == null) continue;
            controllers.add(controller);
            active_devices.add(device);
        }
        queue.start(active_devices);
    }

    private static final native void enumerateDevices(RawInputEventQueue var0, List var1) throws IOException;

    private final Controller[] enumControllers(RawInputEventQueue queue) throws IOException {
        ArrayList controllers = new ArrayList();
        ArrayList devices = new ArrayList();
        RawInputEnvironmentPlugin.enumerateDevices(queue, devices);
        List setupapi_devices = RawInputEnvironmentPlugin.enumSetupAPIDevices();
        RawInputEnvironmentPlugin.createControllersFromDevices(queue, controllers, devices, setupapi_devices);
        Controller[] controllers_array = new Controller[controllers.size()];
        controllers.toArray(controllers_array);
        return controllers_array;
    }

    public boolean isSupported() {
        return supported;
    }

    private static final List enumSetupAPIDevices() throws IOException {
        ArrayList devices = new ArrayList();
        RawInputEnvironmentPlugin.nEnumSetupAPIDevices(RawInputEnvironmentPlugin.getKeyboardClassGUID(), devices);
        RawInputEnvironmentPlugin.nEnumSetupAPIDevices(RawInputEnvironmentPlugin.getMouseClassGUID(), devices);
        return devices;
    }

    private static final native void nEnumSetupAPIDevices(byte[] var0, List var1) throws IOException;

    private static final native byte[] getKeyboardClassGUID();

    private static final native byte[] getMouseClassGUID();

    static {
        String osName = RawInputEnvironmentPlugin.getPrivilegedProperty("os.name", "").trim();
        if (osName.startsWith("Windows")) {
            supported = true;
            if ("x86".equals(RawInputEnvironmentPlugin.getPrivilegedProperty("os.arch"))) {
                RawInputEnvironmentPlugin.loadLibrary("jinput-raw");
            } else {
                RawInputEnvironmentPlugin.loadLibrary("jinput-raw_64");
            }
        }
    }
}

