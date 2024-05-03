/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.NativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;
import java.net.URL;
import java.security.CodeSource;

class FlatNativeLibrary {
    private static boolean initialized;
    private static NativeLibrary nativeLibrary;

    FlatNativeLibrary() {
    }

    static synchronized boolean isLoaded() {
        FlatNativeLibrary.initialize();
        return nativeLibrary != null ? nativeLibrary.isLoaded() : false;
    }

    private static void initialize() {
        String ext;
        String classifier;
        if (initialized) {
            return;
        }
        initialized = true;
        if (!FlatSystemProperties.getBoolean("flatlaf.useNativeLibrary", true)) {
            return;
        }
        if (SystemInfo.isWindows_10_orLater && (SystemInfo.isX86 || SystemInfo.isX86_64 || SystemInfo.isAARCH64)) {
            classifier = SystemInfo.isAARCH64 ? "windows-arm64" : (SystemInfo.isX86_64 ? "windows-x86_64" : "windows-x86");
            ext = "dll";
        } else if (SystemInfo.isMacOS_10_14_Mojave_orLater && (SystemInfo.isAARCH64 || SystemInfo.isX86_64)) {
            classifier = SystemInfo.isAARCH64 ? "macos-arm64" : "macos-x86_64";
            ext = "dylib";
        } else if (SystemInfo.isLinux && SystemInfo.isX86_64) {
            classifier = "linux-x86_64";
            ext = "so";
            FlatNativeLibrary.loadJAWT();
        } else {
            return;
        }
        nativeLibrary = FlatNativeLibrary.createNativeLibrary(classifier, ext);
    }

    private static NativeLibrary createNativeLibrary(String classifier, String ext) {
        File libraryFile;
        String libraryName = "flatlaf-" + classifier;
        String libraryPath = System.getProperty("flatlaf.nativeLibraryPath");
        if (libraryPath != null) {
            if ("system".equals(libraryPath)) {
                NativeLibrary library = new NativeLibrary(libraryName, true);
                if (library.isLoaded()) {
                    return library;
                }
                LoggingFacade.INSTANCE.logSevere("Did not find library " + libraryName + " in java.library.path, using extracted library instead", null);
            } else {
                libraryFile = new File(libraryPath, System.mapLibraryName(libraryName));
                if (libraryFile.exists()) {
                    return new NativeLibrary(libraryFile, true);
                }
                LoggingFacade.INSTANCE.logSevere("Did not find external library " + libraryFile + ", using extracted library instead", null);
            }
        }
        if ((libraryFile = FlatNativeLibrary.findLibraryBesideJar(classifier, ext)) != null) {
            return new NativeLibrary(libraryFile, true);
        }
        return new NativeLibrary("com/formdev/flatlaf/natives/" + libraryName, null, true);
    }

    private static File findLibraryBesideJar(String classifier, String ext) {
        try {
            String libraryName;
            URL jarUrl;
            CodeSource codeSource = FlatNativeLibrary.class.getProtectionDomain().getCodeSource();
            URL uRL = jarUrl = codeSource != null ? codeSource.getLocation() : null;
            if (jarUrl == null) {
                return null;
            }
            if (!"file".equals(jarUrl.getProtocol())) {
                return null;
            }
            File jarFile = new File(jarUrl.toURI());
            if (!jarFile.isFile()) {
                return null;
            }
            String jarName = jarFile.getName();
            String jarBasename = jarName.substring(0, jarName.lastIndexOf(46));
            File parent = jarFile.getParentFile();
            File libraryFile = new File(parent, libraryName = jarBasename + (jarBasename.contains("flatlaf") ? "" : "-flatlaf") + '-' + classifier + '.' + ext);
            if (libraryFile.isFile()) {
                return libraryFile;
            }
            if (parent.getName().equalsIgnoreCase("lib") && (libraryFile = new File(parent.getParentFile(), "bin/" + libraryName)).isFile()) {
                return libraryFile;
            }
        } catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(ex.getMessage(), ex);
        }
        return null;
    }

    private static void loadJAWT() {
        try {
            System.loadLibrary("jawt");
        } catch (UnsatisfiedLinkError ex) {
            String message = ex.getMessage();
            if (message == null || !message.contains("already loaded in another classloader")) {
                LoggingFacade.INSTANCE.logSevere(message, ex);
            }
        } catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(ex.getMessage(), ex);
        }
    }
}

