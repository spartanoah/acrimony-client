/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatNativeLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

class FlatWindowsNativeWindowBorder
implements FlatNativeWindowBorder.Provider {
    private final Map<Window, WndProc> windowsMap = Collections.synchronizedMap(new IdentityHashMap());
    private final EventListenerList listenerList = new EventListenerList();
    private Timer fireStateChangedTimer;
    private boolean colorizationUpToDate;
    private boolean colorizationColorAffectsBorders;
    private Color colorizationColor;
    private int colorizationColorBalance;
    private static FlatWindowsNativeWindowBorder instance;

    static FlatNativeWindowBorder.Provider getInstance() {
        if (!SystemInfo.isWindows_10_orLater) {
            return null;
        }
        if (!FlatNativeLibrary.isLoaded()) {
            return null;
        }
        if (instance == null) {
            instance = new FlatWindowsNativeWindowBorder();
        }
        return instance;
    }

    private FlatWindowsNativeWindowBorder() {
    }

    @Override
    public boolean hasCustomDecoration(Window window) {
        return this.windowsMap.containsKey(window);
    }

    @Override
    public void setHasCustomDecoration(Window window, boolean hasCustomDecoration) {
        if (hasCustomDecoration) {
            this.install(window);
        } else {
            this.uninstall(window);
        }
    }

    private void install(Window window) {
        if (!SystemInfo.isWindows_10_orLater) {
            return;
        }
        if (!(window instanceof JFrame) && !(window instanceof JDialog)) {
            return;
        }
        if (window instanceof Frame && ((Frame)window).isUndecorated() || window instanceof Dialog && ((Dialog)window).isUndecorated()) {
            return;
        }
        if (this.windowsMap.containsKey(window)) {
            return;
        }
        try {
            WndProc wndProc = new WndProc(window);
            if (wndProc.hwnd == 0L) {
                return;
            }
            this.windowsMap.put(window, wndProc);
        } catch (UnsatisfiedLinkError ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    private void uninstall(Window window) {
        WndProc wndProc = this.windowsMap.remove(window);
        if (wndProc != null) {
            wndProc.uninstall();
        }
    }

    @Override
    public void updateTitleBarInfo(Window window, int titleBarHeight, List<Rectangle> hitTestSpots, Rectangle appIconBounds, Rectangle minimizeButtonBounds, Rectangle maximizeButtonBounds, Rectangle closeButtonBounds) {
        WndProc wndProc = this.windowsMap.get(window);
        if (wndProc == null) {
            return;
        }
        wndProc.titleBarHeight = titleBarHeight;
        WndProc.access$202(wndProc, hitTestSpots.toArray(new Rectangle[hitTestSpots.size()]));
        wndProc.appIconBounds = FlatWindowsNativeWindowBorder.cloneRectange(appIconBounds);
        wndProc.minimizeButtonBounds = FlatWindowsNativeWindowBorder.cloneRectange(minimizeButtonBounds);
        wndProc.maximizeButtonBounds = FlatWindowsNativeWindowBorder.cloneRectange(maximizeButtonBounds);
        wndProc.closeButtonBounds = FlatWindowsNativeWindowBorder.cloneRectange(closeButtonBounds);
    }

    private static Rectangle cloneRectange(Rectangle rect) {
        return rect != null ? new Rectangle(rect) : null;
    }

    @Override
    public boolean showWindow(Window window, int cmd) {
        WndProc wndProc = this.windowsMap.get(window);
        if (wndProc == null) {
            return false;
        }
        wndProc.showWindow(wndProc.hwnd, cmd);
        return true;
    }

    @Override
    public boolean isColorizationColorAffectsBorders() {
        this.updateColorization();
        return this.colorizationColorAffectsBorders;
    }

    @Override
    public Color getColorizationColor() {
        this.updateColorization();
        return this.colorizationColor;
    }

    @Override
    public int getColorizationColorBalance() {
        this.updateColorization();
        return this.colorizationColorBalance;
    }

    private void updateColorization() {
        if (this.colorizationUpToDate) {
            return;
        }
        this.colorizationUpToDate = true;
        String subKey = "SOFTWARE\\Microsoft\\Windows\\DWM";
        int value = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorPrevalence", -1);
        this.colorizationColorAffectsBorders = value > 0;
        value = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorizationColor", -1);
        this.colorizationColor = value != -1 ? new Color(value) : null;
        this.colorizationColorBalance = FlatWindowsNativeWindowBorder.registryGetIntValue(subKey, "ColorizationColorBalance", -1);
    }

    private static native int registryGetIntValue(String var0, String var1, int var2);

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    private void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        if (listeners.length == 0) {
            return;
        }
        ChangeEvent e = new ChangeEvent(this);
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] != ChangeListener.class) continue;
            ((ChangeListener)listeners[i + 1]).stateChanged(e);
        }
    }

    void fireStateChangedLaterOnce() {
        EventQueue.invokeLater(() -> {
            if (this.fireStateChangedTimer != null) {
                this.fireStateChangedTimer.restart();
                return;
            }
            this.fireStateChangedTimer = new Timer(300, e -> {
                this.fireStateChangedTimer = null;
                this.colorizationUpToDate = false;
                this.fireStateChanged();
            });
            this.fireStateChangedTimer.setRepeats(false);
            this.fireStateChangedTimer.start();
        });
    }

    private class WndProc
    implements PropertyChangeListener {
        private static final int HTCLIENT = 1;
        private static final int HTCAPTION = 2;
        private static final int HTSYSMENU = 3;
        private static final int HTMINBUTTON = 8;
        private static final int HTMAXBUTTON = 9;
        private static final int HTTOP = 12;
        private static final int HTCLOSE = 20;
        private Window window;
        private final long hwnd;
        private int titleBarHeight;
        private Rectangle[] hitTestSpots;
        private Rectangle appIconBounds;
        private Rectangle minimizeButtonBounds;
        private Rectangle maximizeButtonBounds;
        private Rectangle closeButtonBounds;

        WndProc(Window window) {
            this.window = window;
            this.hwnd = this.installImpl(window);
            if (this.hwnd == 0L) {
                return;
            }
            this.updateFrame(this.hwnd, window instanceof JFrame ? ((JFrame)window).getExtendedState() : 0);
            this.updateWindowBackground();
            window.addPropertyChangeListener("background", this);
        }

        void uninstall() {
            this.window.removePropertyChangeListener("background", this);
            this.uninstallImpl(this.hwnd);
            this.window = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            this.updateWindowBackground();
        }

        private void updateWindowBackground() {
            Color bg = this.window.getBackground();
            if (bg != null) {
                this.setWindowBackground(this.hwnd, bg.getRed(), bg.getGreen(), bg.getBlue());
            }
        }

        private native long installImpl(Window var1);

        private native void uninstallImpl(long var1);

        private native void updateFrame(long var1, int var3);

        private native void setWindowBackground(long var1, int var3, int var4, int var5);

        private native void showWindow(long var1, int var3);

        private int onNcHitTest(int x, int y, boolean isOnResizeBorder) {
            boolean isOnTitleBar;
            Point pt = this.scaleDown(x, y);
            int sx = pt.x;
            int sy = pt.y;
            if (this.contains(this.appIconBounds, sx, sy)) {
                return 3;
            }
            if (this.contains(this.minimizeButtonBounds, sx, sy)) {
                return 8;
            }
            if (this.contains(this.maximizeButtonBounds, sx, sy)) {
                return 9;
            }
            if (this.contains(this.closeButtonBounds, sx, sy)) {
                return 20;
            }
            boolean bl = isOnTitleBar = sy < this.titleBarHeight;
            if (isOnTitleBar) {
                Rectangle[] hitTestSpots2;
                for (Rectangle spot : hitTestSpots2 = this.hitTestSpots) {
                    if (!spot.contains(sx, sy)) continue;
                    return 1;
                }
                return isOnResizeBorder ? 12 : 2;
            }
            return isOnResizeBorder ? 12 : 1;
        }

        private boolean contains(Rectangle rect, int x, int y) {
            return rect != null && rect.contains(x, y);
        }

        private Point scaleDown(int x, int y) {
            GraphicsConfiguration gc = this.window.getGraphicsConfiguration();
            if (gc == null) {
                return new Point(x, y);
            }
            AffineTransform t = gc.getDefaultTransform();
            return new Point(this.clipRound((double)x / t.getScaleX()), this.clipRound((double)y / t.getScaleY()));
        }

        private int clipRound(double value) {
            if ((value -= 0.5) < -2.147483648E9) {
                return Integer.MIN_VALUE;
            }
            if (value > 2.147483647E9) {
                return Integer.MAX_VALUE;
            }
            return (int)Math.ceil(value);
        }

        private boolean isFullscreen() {
            GraphicsConfiguration gc = this.window.getGraphicsConfiguration();
            if (gc == null) {
                return false;
            }
            return gc.getDevice().getFullScreenWindow() == this.window;
        }

        private void fireStateChangedLaterOnce() {
            FlatWindowsNativeWindowBorder.this.fireStateChangedLaterOnce();
        }

        static /* synthetic */ Rectangle[] access$202(WndProc x0, Rectangle[] x1) {
            x0.hitTestSpots = x1;
            return x1;
        }
    }
}

