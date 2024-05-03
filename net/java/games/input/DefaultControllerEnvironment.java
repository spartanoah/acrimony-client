/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.util.plugins.Plugins;

class DefaultControllerEnvironment
extends ControllerEnvironment {
    static String libPath;
    private static Logger log;
    private ArrayList controllers;
    private Collection loadedPlugins = new ArrayList();

    static void loadLibrary(final String lib_name) {
        AccessController.doPrivileged(new PrivilegedAction(){

            public final Object run() {
                String lib_path = System.getProperty("net.java.games.input.librarypath");
                if (lib_path != null) {
                    System.load(lib_path + File.separator + System.mapLibraryName(lib_name));
                } else {
                    System.loadLibrary(lib_name);
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

    public Controller[] getControllers() {
        if (this.controllers == null) {
            this.controllers = new ArrayList();
            AccessController.doPrivileged(new PrivilegedAction(){

                public Object run() {
                    DefaultControllerEnvironment.this.scanControllers();
                    return null;
                }
            });
            String pluginClasses = DefaultControllerEnvironment.getPrivilegedProperty("jinput.plugins", "") + " " + DefaultControllerEnvironment.getPrivilegedProperty("net.java.games.input.plugins", "");
            if (!DefaultControllerEnvironment.getPrivilegedProperty("jinput.useDefaultPlugin", "true").toLowerCase().trim().equals("false") && !DefaultControllerEnvironment.getPrivilegedProperty("net.java.games.input.useDefaultPlugin", "true").toLowerCase().trim().equals("false")) {
                String osName = DefaultControllerEnvironment.getPrivilegedProperty("os.name", "").trim();
                if (osName.equals("Linux")) {
                    pluginClasses = pluginClasses + " net.java.games.input.LinuxEnvironmentPlugin";
                } else if (osName.equals("Mac OS X")) {
                    pluginClasses = pluginClasses + " net.java.games.input.OSXEnvironmentPlugin";
                } else if (osName.equals("Windows XP") || osName.equals("Windows Vista") || osName.equals("Windows 7")) {
                    pluginClasses = pluginClasses + " net.java.games.input.DirectAndRawInputEnvironmentPlugin";
                } else if (osName.equals("Windows 98") || osName.equals("Windows 2000")) {
                    pluginClasses = pluginClasses + " net.java.games.input.DirectInputEnvironmentPlugin";
                } else if (osName.startsWith("Windows")) {
                    log.warning("Found unknown Windows version: " + osName);
                    log.info("Attempting to use default windows plug-in.");
                    pluginClasses = pluginClasses + " net.java.games.input.DirectAndRawInputEnvironmentPlugin";
                } else {
                    log.info("Trying to use default plugin, OS name " + osName + " not recognised");
                }
            }
            StringTokenizer pluginClassTok = new StringTokenizer(pluginClasses, " \t\n\r\f,;:");
            while (pluginClassTok.hasMoreTokens()) {
                String className = pluginClassTok.nextToken();
                try {
                    if (this.loadedPlugins.contains(className)) continue;
                    log.info("Loading: " + className);
                    Class<?> ceClass = Class.forName(className);
                    ControllerEnvironment ce = (ControllerEnvironment)ceClass.newInstance();
                    if (ce.isSupported()) {
                        this.addControllers(ce.getControllers());
                        this.loadedPlugins.add(ce.getClass().getName());
                        continue;
                    }
                    DefaultControllerEnvironment.logln(ceClass.getName() + " is not supported");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        Controller[] ret = new Controller[this.controllers.size()];
        Iterator it = this.controllers.iterator();
        int i = 0;
        while (it.hasNext()) {
            ret[i] = (Controller)it.next();
            ++i;
        }
        return ret;
    }

    private void scanControllers() {
        String pluginPathName = DefaultControllerEnvironment.getPrivilegedProperty("jinput.controllerPluginPath");
        if (pluginPathName == null) {
            pluginPathName = "controller";
        }
        this.scanControllersAt(DefaultControllerEnvironment.getPrivilegedProperty("java.home") + File.separator + "lib" + File.separator + pluginPathName);
        this.scanControllersAt(DefaultControllerEnvironment.getPrivilegedProperty("user.dir") + File.separator + pluginPathName);
    }

    private void scanControllersAt(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        try {
            Plugins plugins = new Plugins(file);
            Class[] envClasses = plugins.getExtends(ControllerEnvironment.class);
            for (int i = 0; i < envClasses.length; ++i) {
                try {
                    ControllerEnvironment.logln("ControllerEnvironment " + envClasses[i].getName() + " loaded by " + envClasses[i].getClassLoader());
                    ControllerEnvironment ce = (ControllerEnvironment)envClasses[i].newInstance();
                    if (ce.isSupported()) {
                        this.addControllers(ce.getControllers());
                        this.loadedPlugins.add(ce.getClass().getName());
                        continue;
                    }
                    DefaultControllerEnvironment.logln(envClasses[i].getName() + " is not supported");
                    continue;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addControllers(Controller[] c) {
        for (int i = 0; i < c.length; ++i) {
            this.controllers.add(c[i]);
        }
    }

    public boolean isSupported() {
        return true;
    }

    static {
        log = Logger.getLogger(DefaultControllerEnvironment.class.getName());
    }
}

