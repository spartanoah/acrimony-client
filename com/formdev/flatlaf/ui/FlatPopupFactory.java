/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatDropShadowBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowsLibrary;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.StackUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowFocusListener;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboPopup;

public class FlatPopupFactory
extends PopupFactory {
    static final String KEY_POPUP_USES_NATIVE_BORDER = "FlatLaf.internal.FlatPopupFactory.popupUsesNativeBorder";
    private MethodHandle java8getPopupMethod;
    private MethodHandle java9getPopupMethod;

    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        Point pt = this.fixToolTipLocation(owner, contents, x, y);
        if (pt != null) {
            x = pt.x;
            y = pt.y;
        }
        FlatPopupFactory.fixLinuxWaylandJava21focusIssue(owner);
        boolean forceHeavyWeight = FlatPopupFactory.isOptionEnabled(owner, contents, "Popup.forceHeavyWeight", "Popup.forceHeavyWeight");
        if (!FlatPopupFactory.isOptionEnabled(owner, contents, "Popup.dropShadowPainted", "Popup.dropShadowPainted") || SystemInfo.isProjector || SystemInfo.isWebswing) {
            return new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, forceHeavyWeight), contents);
        }
        if (SystemInfo.isMacOS || SystemInfo.isLinux) {
            NonFlashingPopup popup = new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, true), contents);
            if (popup.popupWindow != null && SystemInfo.isMacOS && FlatNativeMacLibrary.isLoaded()) {
                FlatPopupFactory.setupRoundedBorder(popup.popupWindow, owner, contents);
            }
            return popup;
        }
        if (FlatPopupFactory.isWindows11BorderSupported() && FlatPopupFactory.getBorderCornerRadius(owner, contents) > 0) {
            NonFlashingPopup popup = new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, true), contents);
            if (popup.popupWindow != null) {
                FlatPopupFactory.setupRoundedBorder(popup.popupWindow, owner, contents);
            }
            return popup;
        }
        if (!forceHeavyWeight && FlatPopupFactory.overlapsHeavyWeightComponent(owner, contents, x, y)) {
            forceHeavyWeight = true;
        }
        return new DropShadowPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, forceHeavyWeight), owner, contents);
    }

    private Popup getPopupForScreenOfOwner(Component owner, Component contents, int x, int y, boolean forceHeavyWeight) throws IllegalArgumentException {
        int count = 0;
        while (true) {
            Popup popup = forceHeavyWeight ? this.getHeavyWeightPopup(owner, contents, x, y) : super.getPopup(owner, contents, x, y);
            Window popupWindow = SwingUtilities.windowForComponent(contents);
            if (popupWindow == null || owner == null || popupWindow.getGraphicsConfiguration() == owner.getGraphicsConfiguration()) {
                return popup;
            }
            if (++count > 10) {
                return popup;
            }
            if (popupWindow instanceof JWindow) {
                ((JWindow)popupWindow).getContentPane().removeAll();
            }
            popupWindow.dispose();
        }
    }

    private Popup getHeavyWeightPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        try {
            if (SystemInfo.isJava_9_orLater) {
                if (this.java9getPopupMethod == null) {
                    MethodType mt = MethodType.methodType(Popup.class, Component.class, Component.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
                    this.java9getPopupMethod = MethodHandles.lookup().findVirtual(PopupFactory.class, "getPopup", mt);
                }
                return this.java9getPopupMethod.invoke(this, owner, contents, x, y, true);
            }
            if (this.java8getPopupMethod == null) {
                Method m = PopupFactory.class.getDeclaredMethod("getPopup", Component.class, Component.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                m.setAccessible(true);
                this.java8getPopupMethod = MethodHandles.lookup().unreflect(m);
            }
            return this.java8getPopupMethod.invoke(this, owner, contents, x, y, 2);
        } catch (Throwable ex) {
            return super.getPopup(owner, contents, x, y);
        }
    }

    private static boolean isOptionEnabled(Component owner, Component contents, String clientKey, String uiKey) {
        Object value = FlatPopupFactory.getOption(owner, contents, clientKey, uiKey);
        return value instanceof Boolean ? (Boolean)value : false;
    }

    private static Object getOption(Component owner, Component contents, String clientKey, String uiKey) {
        for (Component c : new Component[]{owner, contents}) {
            Object value;
            if (!(c instanceof JComponent) || (value = ((JComponent)c).getClientProperty(clientKey)) == null) continue;
            return value;
        }
        return UIManager.get(uiKey);
    }

    private Point fixToolTipLocation(Component owner, Component contents, int x, int y) {
        if (!(contents instanceof JToolTip) || !this.wasInvokedFromToolTipManager() || this.hasTipLocation(owner)) {
            return null;
        }
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return null;
        }
        Point mouseLocation = pointerInfo.getLocation();
        Dimension tipSize = contents.getPreferredSize();
        Rectangle tipBounds = new Rectangle(x, y, tipSize.width, tipSize.height);
        if (!tipBounds.contains(mouseLocation)) {
            return null;
        }
        GraphicsConfiguration gc = null;
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            GraphicsConfiguration dgc = device.getDefaultConfiguration();
            if (!dgc.getBounds().contains(mouseLocation)) continue;
            gc = dgc;
            break;
        }
        if (gc == null) {
            gc = owner.getGraphicsConfiguration();
        }
        if (gc == null) {
            return null;
        }
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int screenTop = screenBounds.y + screenInsets.top;
        int newY = mouseLocation.y - tipSize.height - UIScale.scale(20);
        if (newY < screenTop) {
            return null;
        }
        return new Point(x, newY);
    }

    private boolean wasInvokedFromToolTipManager() {
        return StackUtils.wasInvokedFrom(ToolTipManager.class.getName(), "showTipWindow", 8);
    }

    private boolean hasTipLocation(Component owner) {
        MouseEvent me;
        if (!(owner instanceof JComponent)) {
            return false;
        }
        AWTEvent e = EventQueue.getCurrentEvent();
        if (e instanceof MouseEvent) {
            me = (MouseEvent)e;
        } else {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                return false;
            }
            Point location = new Point(pointerInfo.getLocation());
            SwingUtilities.convertPointFromScreen(location, owner);
            me = new MouseEvent(owner, 503, System.currentTimeMillis(), 0, location.x, location.y, 0, false);
        }
        return me.getSource() == owner && ((JComponent)owner).getToolTipLocation(me) != null;
    }

    private static boolean isWindows11BorderSupported() {
        return SystemInfo.isWindows_11_orLater && FlatNativeWindowsLibrary.isLoaded();
    }

    private static void setupRoundedBorder(Window popupWindow, Component owner, Component contents) {
        if (!popupWindow.isDisplayable()) {
            popupWindow.addNotify();
        }
        int borderCornerRadius = FlatPopupFactory.getBorderCornerRadius(owner, contents);
        float borderWidth = FlatPopupFactory.getRoundedBorderWidth(owner, contents);
        Color borderColor = null;
        if (contents instanceof JComponent) {
            Border border = ((JComponent)contents).getBorder();
            if ((border = FlatUIUtils.unwrapNonUIResourceBorder(border)) instanceof FlatLineBorder) {
                borderColor = ((FlatLineBorder)border).getLineColor();
            } else if (border instanceof LineBorder) {
                borderColor = ((LineBorder)border).getLineColor();
            } else if (border instanceof EmptyBorder) {
                borderColor = FlatNativeWindowsLibrary.COLOR_NONE;
            }
            ((JComponent)contents).putClientProperty(KEY_POPUP_USES_NATIVE_BORDER, true);
        }
        if (SystemInfo.isWindows) {
            long hwnd = FlatNativeWindowsLibrary.getHWND(popupWindow);
            int cornerPreference = borderCornerRadius <= 4 ? 3 : 2;
            FlatNativeWindowsLibrary.setWindowCornerPreference(hwnd, cornerPreference);
            FlatNativeWindowsLibrary.dwmSetWindowAttributeCOLORREF(hwnd, 34, borderColor);
        } else if (SystemInfo.isMacOS) {
            if (borderColor == null || borderColor == FlatNativeWindowsLibrary.COLOR_NONE) {
                borderWidth = 0.0f;
            }
            FlatNativeMacLibrary.setWindowRoundedBorder(popupWindow, borderCornerRadius, borderWidth, borderColor != null ? borderColor.getRGB() : 0);
        }
    }

    private static void resetWindows11Border(Window popupWindow) {
        long hwnd = FlatNativeWindowsLibrary.getHWND(popupWindow);
        if (hwnd == 0L) {
            return;
        }
        FlatNativeWindowsLibrary.setWindowCornerPreference(hwnd, 1);
    }

    private static int getBorderCornerRadius(Component owner, Component contents) {
        String uiKey = contents instanceof BasicComboPopup ? "ComboBox.borderCornerRadius" : (contents instanceof JPopupMenu ? "PopupMenu.borderCornerRadius" : (contents instanceof JToolTip ? "ToolTip.borderCornerRadius" : "Popup.borderCornerRadius"));
        Object value = FlatPopupFactory.getOption(owner, contents, "Popup.borderCornerRadius", uiKey);
        return value instanceof Integer ? (Integer)value : 0;
    }

    private static float getRoundedBorderWidth(Component owner, Component contents) {
        String uiKey = contents instanceof BasicComboPopup ? "ComboBox.roundedBorderWidth" : (contents instanceof JPopupMenu ? "PopupMenu.roundedBorderWidth" : (contents instanceof JToolTip ? "ToolTip.roundedBorderWidth" : "Popup.roundedBorderWidth"));
        Object value = FlatPopupFactory.getOption(owner, contents, "Popup.roundedBorderWidth", uiKey);
        return value instanceof Number ? ((Number)value).floatValue() : 0.0f;
    }

    private static boolean overlapsHeavyWeightComponent(Component owner, Component contents, int x, int y) {
        if (owner == null) {
            return false;
        }
        Window window = SwingUtilities.getWindowAncestor(owner);
        if (window == null) {
            return false;
        }
        Rectangle r = new Rectangle(new Point(x, y), contents.getPreferredSize());
        return FlatPopupFactory.overlapsHeavyWeightComponent(window, r);
    }

    private static boolean overlapsHeavyWeightComponent(Component parent, Rectangle r) {
        if (!parent.isVisible() || !r.intersects(parent.getBounds())) {
            return false;
        }
        if (!parent.isLightweight() && !(parent instanceof Window)) {
            return true;
        }
        if (parent instanceof Container) {
            Rectangle r2 = new Rectangle(r.x - parent.getX(), r.y - parent.getY(), r.width, r.height);
            for (Component c : ((Container)parent).getComponents()) {
                if (!FlatPopupFactory.overlapsHeavyWeightComponent(c, r2)) continue;
                return true;
            }
        }
        return false;
    }

    private static void fixLinuxWaylandJava21focusIssue(Component owner) {
        if (owner == null || !SystemInfo.isLinux || SystemInfo.javaVersion < SystemInfo.toVersion(21, 0, 0, 0)) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(owner);
        if (window == null) {
            return;
        }
        for (WindowFocusListener l : window.getWindowFocusListeners()) {
            if (!"sun.awt.UNIXToolkit$1".equals(l.getClass().getName())) continue;
            window.removeWindowFocusListener(l);
            break;
        }
    }

    private static void showPopupAndFixLocation(Popup popup, Window popupWindow) {
        if (popupWindow != null) {
            int x = popupWindow.getX();
            int y = popupWindow.getY();
            popup.show();
            if (popupWindow.getX() != x || popupWindow.getY() != y) {
                popupWindow.setLocation(x, y);
            }
        } else {
            popup.show();
        }
    }

    private class DropShadowPopup
    extends NonFlashingPopup {
        private final Component owner;
        private JComponent lightComp;
        private Border oldBorder;
        private boolean oldOpaque;
        private boolean mediumWeightShown;
        private Panel mediumWeightPanel;
        private JPanel dropShadowPanel;
        private ComponentListener mediumPanelListener;
        private Popup dropShadowDelegate;
        private Window dropShadowWindow;
        private Color oldDropShadowWindowBackground;

        DropShadowPopup(Popup delegate, Component owner, Component contents) {
            super(delegate, contents);
            this.owner = owner;
            Dimension size = contents.getPreferredSize();
            if (size.width <= 0 || size.height <= 0) {
                return;
            }
            if (this.popupWindow != null) {
                JPanel dropShadowPanel = new JPanel();
                dropShadowPanel.setBorder(this.createDropShadowBorder());
                dropShadowPanel.setOpaque(false);
                Dimension prefSize = this.popupWindow.getPreferredSize();
                Insets insets = dropShadowPanel.getInsets();
                dropShadowPanel.setPreferredSize(new Dimension(prefSize.width + insets.left + insets.right, prefSize.height + insets.top + insets.bottom));
                int x = this.popupWindow.getX() - insets.left;
                int y = this.popupWindow.getY() - insets.top;
                this.dropShadowDelegate = FlatPopupFactory.this.getPopupForScreenOfOwner(owner, dropShadowPanel, x, y, true);
                this.dropShadowWindow = SwingUtilities.windowForComponent(dropShadowPanel);
                if (this.dropShadowWindow != null) {
                    this.oldDropShadowWindowBackground = this.dropShadowWindow.getBackground();
                    this.dropShadowWindow.setBackground(new Color(0, true));
                }
                if (FlatPopupFactory.isWindows11BorderSupported()) {
                    FlatPopupFactory.resetWindows11Border(this.popupWindow);
                    if (this.dropShadowWindow != null) {
                        FlatPopupFactory.resetWindows11Border(this.dropShadowWindow);
                    }
                }
            } else {
                this.mediumWeightPanel = (Panel)SwingUtilities.getAncestorOfClass(Panel.class, contents);
                if (this.mediumWeightPanel != null) {
                    this.dropShadowPanel = new JPanel();
                    this.dropShadowPanel.setBorder(this.createDropShadowBorder());
                    this.dropShadowPanel.setOpaque(false);
                    this.dropShadowPanel.setSize(FlatUIUtils.addInsets(this.mediumWeightPanel.getSize(), this.dropShadowPanel.getInsets()));
                } else {
                    Container p = contents.getParent();
                    if (!(p instanceof JComponent)) {
                        return;
                    }
                    this.lightComp = (JComponent)p;
                    this.oldBorder = this.lightComp.getBorder();
                    this.oldOpaque = this.lightComp.isOpaque();
                    this.lightComp.setBorder(this.createDropShadowBorder());
                    this.lightComp.setOpaque(false);
                    this.lightComp.setSize(this.lightComp.getPreferredSize());
                }
            }
        }

        private Border createDropShadowBorder() {
            return new FlatDropShadowBorder(UIManager.getColor("Popup.dropShadowColor"), UIManager.getInsets("Popup.dropShadowInsets"), FlatUIUtils.getUIFloat("Popup.dropShadowOpacity", 0.5f));
        }

        @Override
        public void show() {
            if (this.dropShadowDelegate != null) {
                FlatPopupFactory.showPopupAndFixLocation(this.dropShadowDelegate, this.dropShadowWindow);
            }
            if (this.mediumWeightPanel != null) {
                this.showMediumWeightDropShadow();
            }
            super.show();
            if (this.lightComp != null) {
                Insets insets = this.lightComp.getInsets();
                if (insets.left != 0 || insets.top != 0) {
                    this.lightComp.setLocation(this.lightComp.getX() - insets.left, this.lightComp.getY() - insets.top);
                }
            }
        }

        @Override
        public void hide() {
            if (this.dropShadowDelegate != null) {
                this.dropShadowDelegate.hide();
                this.dropShadowDelegate = null;
            }
            if (this.mediumWeightPanel != null) {
                this.hideMediumWeightDropShadow();
                this.dropShadowPanel = null;
                this.mediumWeightPanel = null;
            }
            super.hide();
            if (this.dropShadowWindow != null) {
                this.dropShadowWindow.setBackground(this.oldDropShadowWindowBackground);
                this.dropShadowWindow = null;
            }
            if (this.lightComp != null) {
                this.lightComp.setBorder(this.oldBorder);
                this.lightComp.setOpaque(this.oldOpaque);
                this.lightComp = null;
            }
        }

        private void showMediumWeightDropShadow() {
            if (this.mediumWeightShown) {
                return;
            }
            this.mediumWeightShown = true;
            if (this.owner == null) {
                return;
            }
            Window window = SwingUtilities.windowForComponent(this.owner);
            if (!(window instanceof RootPaneContainer)) {
                return;
            }
            this.dropShadowPanel.setVisible(false);
            JLayeredPane layeredPane = ((RootPaneContainer)((Object)window)).getLayeredPane();
            layeredPane.add(this.dropShadowPanel, JLayeredPane.POPUP_LAYER, 0);
            this.moveMediumWeightDropShadow();
            this.resizeMediumWeightDropShadow();
            this.mediumPanelListener = new ComponentListener(){

                @Override
                public void componentShown(ComponentEvent e) {
                    if (DropShadowPopup.this.dropShadowPanel != null) {
                        DropShadowPopup.this.dropShadowPanel.setVisible(true);
                    }
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    if (DropShadowPopup.this.dropShadowPanel != null) {
                        DropShadowPopup.this.dropShadowPanel.setVisible(false);
                    }
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    DropShadowPopup.this.moveMediumWeightDropShadow();
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    DropShadowPopup.this.resizeMediumWeightDropShadow();
                }
            };
            this.mediumWeightPanel.addComponentListener(this.mediumPanelListener);
        }

        private void hideMediumWeightDropShadow() {
            this.mediumWeightPanel.removeComponentListener(this.mediumPanelListener);
            Container parent = this.dropShadowPanel.getParent();
            if (parent != null) {
                Rectangle bounds = this.dropShadowPanel.getBounds();
                parent.remove(this.dropShadowPanel);
                parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        private void moveMediumWeightDropShadow() {
            if (this.dropShadowPanel != null && this.mediumWeightPanel != null) {
                Point location = this.mediumWeightPanel.getLocation();
                Insets insets = this.dropShadowPanel.getInsets();
                this.dropShadowPanel.setLocation(location.x - insets.left, location.y - insets.top);
            }
        }

        private void resizeMediumWeightDropShadow() {
            if (this.dropShadowPanel != null && this.mediumWeightPanel != null) {
                this.dropShadowPanel.setSize(FlatUIUtils.addInsets(this.mediumWeightPanel.getSize(), this.dropShadowPanel.getInsets()));
            }
        }
    }

    private static class NonFlashingPopup
    extends Popup {
        private Popup delegate;
        private Component contents;
        protected Window popupWindow;
        private Color oldPopupWindowBackground;

        NonFlashingPopup(Popup delegate, Component contents) {
            this.delegate = delegate;
            this.contents = contents;
            this.popupWindow = SwingUtilities.windowForComponent(contents);
            if (this.popupWindow != null) {
                this.oldPopupWindowBackground = this.popupWindow.getBackground();
                this.popupWindow.setBackground(contents.getBackground());
            }
        }

        @Override
        public void show() {
            if (this.delegate != null) {
                Dimension prefSize;
                Container parent;
                FlatPopupFactory.showPopupAndFixLocation(this.delegate, this.popupWindow);
                if (this.contents instanceof JToolTip && this.popupWindow == null && (parent = this.contents.getParent()) instanceof JPanel && !(prefSize = parent.getPreferredSize()).equals(parent.getSize())) {
                    Container mediumWeightPanel = SwingUtilities.getAncestorOfClass(Panel.class, parent);
                    Container c = mediumWeightPanel != null ? mediumWeightPanel : parent;
                    c.setSize(prefSize);
                    c.validate();
                }
            }
        }

        @Override
        public void hide() {
            if (this.contents instanceof JComponent) {
                ((JComponent)this.contents).putClientProperty(FlatPopupFactory.KEY_POPUP_USES_NATIVE_BORDER, null);
            }
            if (this.delegate != null) {
                this.delegate.hide();
                this.delegate = null;
                this.contents = null;
            }
            if (this.popupWindow != null) {
                this.popupWindow.setBackground(this.oldPopupWindowBackground);
                this.popupWindow = null;
            }
        }
    }
}

