/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JDialog;
import javax.swing.JFrame;

class FlatNativeLinuxLibrary {
    static final int MOVE = 8;
    private static Boolean isXWindowSystem;

    FlatNativeLinuxLibrary() {
    }

    static boolean isLoaded() {
        return SystemInfo.isLinux && FlatNativeLibrary.isLoaded();
    }

    private static boolean isXWindowSystem() {
        if (isXWindowSystem == null) {
            isXWindowSystem = Toolkit.getDefaultToolkit().getClass().getName().endsWith(".XToolkit");
        }
        return isXWindowSystem;
    }

    static boolean isWMUtilsSupported(Window window) {
        return FlatNativeLinuxLibrary.hasCustomDecoration(window) && FlatNativeLinuxLibrary.isXWindowSystem() && FlatNativeLinuxLibrary.isLoaded();
    }

    static boolean moveOrResizeWindow(Window window, MouseEvent e, int direction) {
        Point pt = FlatNativeLinuxLibrary.scale(window, e.getLocationOnScreen());
        return FlatNativeLinuxLibrary.xMoveOrResizeWindow(window, pt.x, pt.y, direction);
    }

    static boolean showWindowMenu(Window window, MouseEvent e) {
        Point pt = FlatNativeLinuxLibrary.scale(window, e.getLocationOnScreen());
        return FlatNativeLinuxLibrary.xShowWindowMenu(window, pt.x, pt.y);
    }

    private static Point scale(Window window, Point pt) {
        AffineTransform transform = window.getGraphicsConfiguration().getDefaultTransform();
        int x = (int)Math.round((double)pt.x * transform.getScaleX());
        int y = (int)Math.round((double)pt.y * transform.getScaleY());
        return new Point(x, y);
    }

    private static native boolean xMoveOrResizeWindow(Window var0, int var1, int var2, int var3);

    private static native boolean xShowWindowMenu(Window var0, int var1, int var2);

    private static boolean hasCustomDecoration(Window window) {
        return window instanceof JFrame && JFrame.isDefaultLookAndFeelDecorated() && ((JFrame)window).isUndecorated() || window instanceof JDialog && JDialog.isDefaultLookAndFeelDecorated() && ((JDialog)window).isUndecorated();
    }
}

