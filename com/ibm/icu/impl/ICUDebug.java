/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.util.VersionInfo;

public final class ICUDebug {
    private static String params;
    private static boolean debug;
    private static boolean help;
    public static final String javaVersionString;
    public static final boolean isJDK14OrHigher;
    public static final VersionInfo javaVersion;

    public static VersionInfo getInstanceLenient(String s) {
        int[] ver = new int[4];
        boolean numeric = false;
        int i = 0;
        int vidx = 0;
        while (i < s.length()) {
            char c;
            if ((c = s.charAt(i++)) < '0' || c > '9') {
                if (!numeric) continue;
                if (vidx == 3) break;
                numeric = false;
                ++vidx;
                continue;
            }
            if (numeric) {
                ver[vidx] = ver[vidx] * 10 + (c - 48);
                if (ver[vidx] <= 255) continue;
                ver[vidx] = 0;
                break;
            }
            numeric = true;
            ver[vidx] = c - 48;
        }
        return VersionInfo.getInstance(ver[0], ver[1], ver[2], ver[3]);
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        if (debug) {
            boolean result;
            boolean bl = result = params.indexOf(arg) != -1;
            if (help) {
                System.out.println("\nICUDebug.enabled(" + arg + ") = " + result);
            }
            return result;
        }
        return false;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                int limit;
                result = params.length() > (index += arg.length()) && params.charAt(index) == '=' ? params.substring(index, (limit = params.indexOf(",", ++index)) == -1 ? params.length() : limit) : "true";
            }
            if (help) {
                System.out.println("\nICUDebug.value(" + arg + ") = " + result);
            }
        }
        return result;
    }

    static {
        VersionInfo java14Version;
        try {
            params = System.getProperty("ICUDebug");
        } catch (SecurityException e) {
            // empty catch block
        }
        debug = params != null;
        boolean bl = help = debug && (params.equals("") || params.indexOf("help") != -1);
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
        isJDK14OrHigher = (javaVersion = ICUDebug.getInstanceLenient(javaVersionString = System.getProperty("java.version", "0"))).compareTo(java14Version = VersionInfo.getInstance("1.4.0")) >= 0;
    }
}

