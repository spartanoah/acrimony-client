/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatWindowsNativeWindowBorder;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;

public class FlatNativeWindowBorder {
    private static final boolean canUseWindowDecorations = SystemInfo.isWindows_10_orLater && (SystemInfo.isWindows_11_orLater || !FlatSystemProperties.getBoolean("sun.java2d.opengl", false)) && !SystemInfo.isProjector && !SystemInfo.isWebswing && !SystemInfo.isWinPE && FlatSystemProperties.getBoolean("flatlaf.useWindowDecorations", true);
    private static Boolean supported;
    private static Provider nativeProvider;

    public static boolean isSupported() {
        FlatNativeWindowBorder.initialize();
        return supported;
    }

    static Object install(JRootPane rootPane) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return null;
        }
        Container parent = rootPane.getParent();
        if (parent != null && !(parent instanceof Window)) {
            return null;
        }
        if (parent instanceof Window && parent.isDisplayable()) {
            FlatNativeWindowBorder.install((Window)parent);
        }
        PropertyChangeListener ancestorListener = e -> {
            Object newValue = e.getNewValue();
            if (newValue instanceof Window) {
                FlatNativeWindowBorder.install((Window)newValue);
            } else if (newValue == null && e.getOldValue() instanceof Window) {
                FlatNativeWindowBorder.uninstall((Window)e.getOldValue());
            }
        };
        rootPane.addPropertyChangeListener("ancestor", ancestorListener);
        return ancestorListener;
    }

    static void install(Window window) {
        if (FlatNativeWindowBorder.hasCustomDecoration(window)) {
            return;
        }
        if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
            return;
        }
        if (window instanceof JFrame) {
            JFrame frame = (JFrame)window;
            JRootPane rootPane = frame.getRootPane();
            if (!FlatNativeWindowBorder.useWindowDecorations(rootPane)) {
                return;
            }
            if (frame.isUndecorated()) {
                return;
            }
            FlatNativeWindowBorder.setHasCustomDecoration(frame, true);
            if (!FlatNativeWindowBorder.hasCustomDecoration(frame)) {
                return;
            }
            rootPane.setWindowDecorationStyle(1);
        } else if (window instanceof JDialog) {
            JDialog dialog = (JDialog)window;
            JRootPane rootPane = dialog.getRootPane();
            if (!FlatNativeWindowBorder.useWindowDecorations(rootPane)) {
                return;
            }
            if (dialog.isUndecorated()) {
                return;
            }
            FlatNativeWindowBorder.setHasCustomDecoration(dialog, true);
            if (!FlatNativeWindowBorder.hasCustomDecoration(dialog)) {
                return;
            }
            rootPane.setWindowDecorationStyle(2);
        }
    }

    static void uninstall(JRootPane rootPane, Object data) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return;
        }
        if (data instanceof PropertyChangeListener) {
            rootPane.removePropertyChangeListener("ancestor", (PropertyChangeListener)data);
        }
        if (UIManager.getLookAndFeel() instanceof FlatLaf && FlatNativeWindowBorder.useWindowDecorations(rootPane)) {
            return;
        }
        Container parent = rootPane.getParent();
        if (parent instanceof Window) {
            FlatNativeWindowBorder.uninstall((Window)parent);
        }
    }

    private static void uninstall(Window window) {
        if (!FlatNativeWindowBorder.hasCustomDecoration(window)) {
            return;
        }
        FlatNativeWindowBorder.setHasCustomDecoration(window, false);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame)window;
            frame.getRootPane().setWindowDecorationStyle(0);
        } else if (window instanceof JDialog) {
            JDialog dialog = (JDialog)window;
            dialog.getRootPane().setWindowDecorationStyle(0);
        }
    }

    private static boolean useWindowDecorations(JRootPane rootPane) {
        return FlatUIUtils.getBoolean(rootPane, "flatlaf.useWindowDecorations", "JRootPane.useWindowDecorations", "TitlePane.useWindowDecorations", false);
    }

    public static boolean hasCustomDecoration(Window window) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return false;
        }
        return nativeProvider.hasCustomDecoration(window);
    }

    public static void setHasCustomDecoration(Window window, boolean hasCustomDecoration) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return;
        }
        nativeProvider.setHasCustomDecoration(window, hasCustomDecoration);
    }

    static void setTitleBarHeightAndHitTestSpots(Window window, int titleBarHeight, List<Rectangle> hitTestSpots, Rectangle appIconBounds, Rectangle minimizeButtonBounds, Rectangle maximizeButtonBounds, Rectangle closeButtonBounds) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return;
        }
        nativeProvider.updateTitleBarInfo(window, titleBarHeight, hitTestSpots, appIconBounds, minimizeButtonBounds, maximizeButtonBounds, closeButtonBounds);
    }

    static boolean showWindow(Window window, int cmd) {
        if (!FlatNativeWindowBorder.isSupported()) {
            return false;
        }
        return nativeProvider.showWindow(window, cmd);
    }

    private static void initialize() {
        if (supported != null) {
            return;
        }
        supported = false;
        if (!canUseWindowDecorations) {
            return;
        }
        try {
            FlatNativeWindowBorder.setNativeProvider(FlatWindowsNativeWindowBorder.getInstance());
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public static void setNativeProvider(Provider provider) {
        if (nativeProvider != null) {
            throw new IllegalStateException();
        }
        nativeProvider = provider;
        supported = nativeProvider != null;
    }

    static class WindowTopBorder
    extends BorderUIResource.EmptyBorderUIResource {
        private static WindowTopBorder instance;
        private final Color activeLightColor = new Color(0x707070);
        private final Color activeDarkColor = new Color(2960943);
        private final Color inactiveLightColor = new Color(0xAAAAAA);
        private final Color inactiveDarkColor = new Color(4803147);
        private boolean colorizationAffectsBorders;
        private Color activeColor;

        static WindowTopBorder getInstance() {
            if (instance == null) {
                instance = new WindowTopBorder();
            }
            return instance;
        }

        WindowTopBorder() {
            super(1, 0, 0, 0);
            this.update();
            this.installListeners();
        }

        void update() {
            this.colorizationAffectsBorders = this.isColorizationColorAffectsBorders();
            this.activeColor = this.calculateActiveBorderColor();
        }

        void installListeners() {
            nativeProvider.addChangeListener(e -> {
                this.update();
                for (Window window : Window.getWindows()) {
                    if (!window.isDisplayable()) continue;
                    window.repaint(0, 0, window.getWidth(), 1);
                }
            });
        }

        boolean isColorizationColorAffectsBorders() {
            return nativeProvider.isColorizationColorAffectsBorders();
        }

        Color getColorizationColor() {
            return nativeProvider.getColorizationColor();
        }

        int getColorizationColorBalance() {
            return nativeProvider.getColorizationColorBalance();
        }

        private Color calculateActiveBorderColor() {
            if (!this.colorizationAffectsBorders) {
                return null;
            }
            Color colorizationColor = this.getColorizationColor();
            if (colorizationColor != null) {
                int colorizationColorBalance = this.getColorizationColorBalance();
                if (colorizationColorBalance < 0 || colorizationColorBalance > 100) {
                    colorizationColorBalance = 100;
                }
                if (colorizationColorBalance == 0) {
                    return new Color(0xD9D9D9);
                }
                if (colorizationColorBalance == 100) {
                    return colorizationColor;
                }
                float alpha = (float)colorizationColorBalance / 100.0f;
                float remainder = 1.0f - alpha;
                int r = Math.round((float)colorizationColor.getRed() * alpha + 217.0f * remainder);
                int g = Math.round((float)colorizationColor.getGreen() * alpha + 217.0f * remainder);
                int b = Math.round((float)colorizationColor.getBlue() * alpha + 217.0f * remainder);
                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);
                return new Color(r, g, b);
            }
            Color activeBorderColor = (Color)Toolkit.getDefaultToolkit().getDesktopProperty("win.frame.activeBorderColor");
            return activeBorderColor != null ? activeBorderColor : UIManager.getColor("MenuBar.borderColor");
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Window window = SwingUtilities.windowForComponent(c);
            boolean active = window != null && window.isActive();
            boolean dark = FlatLaf.isLafDark();
            g.setColor(active ? (this.activeColor != null ? this.activeColor : (dark ? this.activeDarkColor : this.activeLightColor)) : (dark ? this.inactiveDarkColor : this.inactiveLightColor));
            HiDPIUtils.paintAtScale1x((Graphics2D)g, x, y, width, height, this::paintImpl);
        }

        private void paintImpl(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
            g.fillRect(x, y, width, 1);
        }

        void repaintBorder(Component c) {
            c.repaint(0, 0, c.getWidth(), 1);
        }
    }

    public static interface Provider {
        public static final int SW_MAXIMIZE = 3;
        public static final int SW_MINIMIZE = 6;
        public static final int SW_RESTORE = 9;

        public boolean hasCustomDecoration(Window var1);

        public void setHasCustomDecoration(Window var1, boolean var2);

        public void updateTitleBarInfo(Window var1, int var2, List<Rectangle> var3, Rectangle var4, Rectangle var5, Rectangle var6, Rectangle var7);

        public boolean showWindow(Window var1, int var2);

        public boolean isColorizationColorAffectsBorders();

        public Color getColorizationColor();

        public int getColorizationColorBalance();

        public void addChangeListener(ChangeListener var1);

        public void removeChangeListener(ChangeListener var1);
    }
}

