/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.ui.FlatTitlePane;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatWindowResizer;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Function;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.RootPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicRootPaneUI;

public class FlatRootPaneUI
extends BasicRootPaneUI {
    protected final Color borderColor = UIManager.getColor("TitlePane.borderColor");
    protected JRootPane rootPane;
    protected FlatTitlePane titlePane;
    protected FlatWindowResizer windowResizer;
    private Object nativeWindowBorderData;
    private LayoutManager oldLayout;
    private PropertyChangeListener ancestorListener;
    private ComponentListener componentListener;
    protected static final Integer TITLE_PANE_LAYER = JLayeredPane.FRAME_CONTENT_LAYER - 1;

    public static ComponentUI createUI(JComponent c) {
        return new FlatRootPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.rootPane = (JRootPane)c;
        if (this.rootPane.getWindowDecorationStyle() != 0) {
            this.installClientDecorations();
        } else {
            this.installBorder();
        }
        this.installNativeWindowBorder();
    }

    protected void installBorder() {
        Border b;
        if (this.borderColor != null && ((b = this.rootPane.getBorder()) == null || b instanceof UIResource)) {
            this.rootPane.setBorder(new FlatWindowTitleBorder(this.borderColor));
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        this.uninstallNativeWindowBorder();
        this.uninstallClientDecorations();
        this.rootPane = null;
    }

    @Override
    protected void installDefaults(JRootPane c) {
        Color background;
        Container parent;
        super.installDefaults(c);
        if (!c.isBackgroundSet() || c.getBackground() instanceof UIResource) {
            c.setBackground(UIManager.getColor("RootPane.background"));
        }
        if (!c.isForegroundSet() || c.getForeground() instanceof UIResource) {
            c.setForeground(UIManager.getColor("RootPane.foreground"));
        }
        if (!c.isFontSet() || c.getFont() instanceof UIResource) {
            c.setFont(UIManager.getFont("RootPane.font"));
        }
        if (((parent = c.getParent()) instanceof JFrame || parent instanceof JDialog) && ((background = parent.getBackground()) == null || background instanceof UIResource)) {
            parent.setBackground(UIManager.getColor("control"));
        }
    }

    @Override
    protected void uninstallDefaults(JRootPane c) {
        super.uninstallDefaults(c);
        if (c.isBackgroundSet() && c.getBackground() instanceof UIResource) {
            c.setBackground(null);
        }
        if (c.isForegroundSet() && c.getForeground() instanceof UIResource) {
            c.setForeground(null);
        }
        if (c.isFontSet() && c.getFont() instanceof UIResource) {
            c.setFont(null);
        }
    }

    @Override
    protected void installListeners(final JRootPane root) {
        super.installListeners(root);
        if (SystemInfo.isJava_9_orLater) {
            this.ancestorListener = e -> {
                Object oldValue = e.getOldValue();
                Object newValue = e.getNewValue();
                if (newValue instanceof Window) {
                    if (this.componentListener == null) {
                        this.componentListener = new ComponentAdapter(){

                            @Override
                            public void componentShown(ComponentEvent e) {
                                root.getParent().repaint(root.getX(), root.getY(), root.getWidth(), root.getHeight());
                            }
                        };
                    }
                    ((Window)newValue).addComponentListener(this.componentListener);
                } else if (newValue == null && oldValue instanceof Window && this.componentListener != null) {
                    ((Window)oldValue).removeComponentListener(this.componentListener);
                }
            };
            root.addPropertyChangeListener("ancestor", this.ancestorListener);
        }
    }

    @Override
    protected void uninstallListeners(JRootPane root) {
        super.uninstallListeners(root);
        if (SystemInfo.isJava_9_orLater) {
            if (this.componentListener != null) {
                Window window = SwingUtilities.windowForComponent(root);
                if (window != null) {
                    window.removeComponentListener(this.componentListener);
                }
                this.componentListener = null;
            }
            root.removePropertyChangeListener("ancestor", this.ancestorListener);
            this.ancestorListener = null;
        }
    }

    protected void installNativeWindowBorder() {
        this.nativeWindowBorderData = FlatNativeWindowBorder.install(this.rootPane);
    }

    protected void uninstallNativeWindowBorder() {
        FlatNativeWindowBorder.uninstall(this.rootPane, this.nativeWindowBorderData);
        this.nativeWindowBorderData = null;
    }

    public static void updateNativeWindowBorder(JRootPane rootPane) {
        RootPaneUI rui = rootPane.getUI();
        if (!(rui instanceof FlatRootPaneUI)) {
            return;
        }
        FlatRootPaneUI ui = (FlatRootPaneUI)rui;
        ui.uninstallNativeWindowBorder();
        ui.installNativeWindowBorder();
    }

    protected void installClientDecorations() {
        boolean isNativeWindowBorderSupported = FlatNativeWindowBorder.isSupported();
        if (this.rootPane.getWindowDecorationStyle() != 0 && !isNativeWindowBorderSupported) {
            LookAndFeel.installBorder(this.rootPane, "RootPane.border");
        } else {
            LookAndFeel.uninstallBorder(this.rootPane);
        }
        this.setTitlePane(this.createTitlePane());
        this.oldLayout = this.rootPane.getLayout();
        this.rootPane.setLayout(this.createRootLayout());
        if (!isNativeWindowBorderSupported) {
            this.windowResizer = this.createWindowResizer();
        }
    }

    protected void uninstallClientDecorations() {
        LookAndFeel.uninstallBorder(this.rootPane);
        this.setTitlePane(null);
        if (this.windowResizer != null) {
            this.windowResizer.uninstall();
            this.windowResizer = null;
        }
        if (this.oldLayout != null) {
            this.rootPane.setLayout(this.oldLayout);
            this.oldLayout = null;
        }
        if (this.rootPane.getWindowDecorationStyle() == 0) {
            this.rootPane.revalidate();
            this.rootPane.repaint();
        }
    }

    protected FlatRootLayout createRootLayout() {
        return new FlatRootLayout();
    }

    protected FlatWindowResizer createWindowResizer() {
        return new FlatWindowResizer.WindowResizer(this.rootPane);
    }

    protected FlatTitlePane createTitlePane() {
        return new FlatTitlePane(this.rootPane);
    }

    protected void setTitlePane(FlatTitlePane newTitlePane) {
        JLayeredPane layeredPane = this.rootPane.getLayeredPane();
        if (this.titlePane != null) {
            layeredPane.remove(this.titlePane);
        }
        if (newTitlePane != null) {
            layeredPane.add((Component)newTitlePane, TITLE_PANE_LAYER);
        }
        this.titlePane = newTitlePane;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        switch (e.getPropertyName()) {
            case "windowDecorationStyle": {
                this.uninstallClientDecorations();
                if (this.rootPane.getWindowDecorationStyle() != 0) {
                    this.installClientDecorations();
                    break;
                }
                this.installBorder();
                break;
            }
            case "JRootPane.useWindowDecorations": {
                FlatRootPaneUI.updateNativeWindowBorder(this.rootPane);
                break;
            }
            case "JRootPane.menuBarEmbedded": {
                if (this.titlePane == null) break;
                this.titlePane.menuBarChanged();
                this.rootPane.revalidate();
                this.rootPane.repaint();
                break;
            }
            case "JRootPane.titleBarShowIcon": {
                if (this.titlePane == null) break;
                this.titlePane.updateIcon();
                break;
            }
            case "JRootPane.titleBarShowTitle": 
            case "JRootPane.titleBarShowIconify": 
            case "JRootPane.titleBarShowMaximize": 
            case "JRootPane.titleBarShowClose": {
                if (this.titlePane == null) break;
                this.titlePane.updateVisibility();
                break;
            }
            case "JRootPane.titleBarBackground": 
            case "JRootPane.titleBarForeground": {
                if (this.titlePane == null) break;
                this.titlePane.titleBarColorsChanged();
                break;
            }
            case "JRootPane.glassPaneFullHeight": {
                this.rootPane.revalidate();
                break;
            }
            case "Window.style": {
                if (!this.rootPane.isDisplayable()) break;
                throw new IllegalComponentStateException("The client property 'Window.style' must be set before the window becomes displayable.");
            }
        }
    }

    protected static boolean isMenuBarEmbedded(JRootPane rootPane) {
        RootPaneUI ui = rootPane.getUI();
        return ui instanceof FlatRootPaneUI && ((FlatRootPaneUI)ui).titlePane != null && ((FlatRootPaneUI)ui).titlePane.isMenuBarEmbedded();
    }

    protected static FlatTitlePane getTitlePane(JRootPane rootPane) {
        RootPaneUI ui = rootPane.getUI();
        return ui instanceof FlatRootPaneUI ? ((FlatRootPaneUI)ui).titlePane : null;
    }

    private static class FlatWindowTitleBorder
    extends BorderUIResource.EmptyBorderUIResource {
        private final Color borderColor;

        FlatWindowTitleBorder(Color borderColor) {
            super(0, 0, 0, 0);
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (this.showBorder(c)) {
                float lineHeight = UIScale.scale(1.0f);
                FlatUIUtils.paintFilledRectangle(g, this.borderColor, x, y, width, lineHeight);
            }
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(this.showBorder(c) ? 1 : 0, 0, 0, 0);
            return insets;
        }

        private boolean showBorder(Component c) {
            Container parent = c.getParent();
            return parent instanceof JFrame && (((JFrame)parent).getJMenuBar() == null || !((JFrame)parent).getJMenuBar().isVisible()) || parent instanceof JDialog && (((JDialog)parent).getJMenuBar() == null || !((JDialog)parent).getJMenuBar().isVisible());
        }
    }

    public static class FlatWindowBorder
    extends BorderUIResource.EmptyBorderUIResource {
        protected final Color activeBorderColor = UIManager.getColor("RootPane.activeBorderColor");
        protected final Color inactiveBorderColor = UIManager.getColor("RootPane.inactiveBorderColor");
        protected final Color baseBorderColor = UIManager.getColor("Panel.background");

        public FlatWindowBorder() {
            super(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            if (this.isWindowMaximized(c) || FlatUIUtils.isFullScreen(c)) {
                insets.right = 0;
                insets.bottom = 0;
                insets.left = 0;
                insets.top = 0;
                return insets;
            }
            return super.getBorderInsets(c, insets);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (this.isWindowMaximized(c) || FlatUIUtils.isFullScreen(c)) {
                return;
            }
            Container parent = c.getParent();
            boolean active = parent instanceof Window && ((Window)parent).isActive();
            g.setColor(FlatUIUtils.deriveColor(active ? this.activeBorderColor : this.inactiveBorderColor, this.baseBorderColor));
            HiDPIUtils.paintAtScale1x((Graphics2D)g, x, y, width, height, this::paintImpl);
        }

        private void paintImpl(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
            g.drawRect(x, y, width - 1, height - 1);
        }

        protected boolean isWindowMaximized(Component c) {
            Container parent = c.getParent();
            return parent instanceof Frame && (((Frame)parent).getExtendedState() & 6) == 6;
        }
    }

    protected class FlatRootLayout
    implements LayoutManager2 {
        protected FlatRootLayout() {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return this.computeLayoutSize(parent, c -> c.getPreferredSize());
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return this.computeLayoutSize(parent, c -> c.getMinimumSize());
        }

        @Override
        public Dimension maximumLayoutSize(Container parent) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        private Dimension computeLayoutSize(Container parent, Function<Component, Dimension> getSizeFunc) {
            JRootPane rootPane = (JRootPane)parent;
            Dimension titlePaneSize = FlatRootPaneUI.this.titlePane != null ? getSizeFunc.apply(FlatRootPaneUI.this.titlePane) : new Dimension();
            Dimension contentSize = rootPane.getContentPane() != null ? getSizeFunc.apply(rootPane.getContentPane()) : rootPane.getSize();
            int width = contentSize.width;
            int height = titlePaneSize.height + contentSize.height;
            if (FlatRootPaneUI.this.titlePane == null || !FlatRootPaneUI.this.titlePane.isMenuBarEmbedded()) {
                JMenuBar menuBar = rootPane.getJMenuBar();
                Dimension menuBarSize = menuBar != null && menuBar.isVisible() ? getSizeFunc.apply(menuBar) : new Dimension();
                width = Math.max(width, menuBarSize.width);
                height += menuBarSize.height;
            }
            Insets insets = rootPane.getInsets();
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }

        @Override
        public void layoutContainer(Container parent) {
            Container contentPane;
            JMenuBar menuBar;
            JRootPane rootPane = (JRootPane)parent;
            boolean isFullScreen = FlatUIUtils.isFullScreen(rootPane);
            Insets insets = rootPane.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = rootPane.getWidth() - insets.left - insets.right;
            int height = rootPane.getHeight() - insets.top - insets.bottom;
            if (rootPane.getLayeredPane() != null) {
                rootPane.getLayeredPane().setBounds(x, y, width, height);
            }
            int nextY = 0;
            if (FlatRootPaneUI.this.titlePane != null) {
                int prefHeight = !isFullScreen ? FlatRootPaneUI.this.titlePane.getPreferredSize().height : 0;
                FlatRootPaneUI.this.titlePane.setBounds(0, 0, width, prefHeight);
                nextY += prefHeight;
            }
            if (rootPane.getGlassPane() != null) {
                boolean fullHeight = FlatClientProperties.clientPropertyBoolean(rootPane, "JRootPane.glassPaneFullHeight", false);
                int offset = fullHeight ? 0 : nextY;
                rootPane.getGlassPane().setBounds(x, y + offset, width, height - offset);
            }
            if ((menuBar = rootPane.getJMenuBar()) != null && menuBar.isVisible()) {
                boolean embedded;
                boolean bl = embedded = !isFullScreen && FlatRootPaneUI.this.titlePane != null && FlatRootPaneUI.this.titlePane.isMenuBarEmbedded();
                if (embedded) {
                    FlatRootPaneUI.this.titlePane.validate();
                    menuBar.setBounds(FlatRootPaneUI.this.titlePane.getMenuBarBounds());
                } else {
                    Dimension prefSize = menuBar.getPreferredSize();
                    menuBar.setBounds(0, nextY, width, prefSize.height);
                    nextY += prefSize.height;
                }
            }
            if ((contentPane = rootPane.getContentPane()) != null) {
                contentPane.setBounds(0, nextY, width, Math.max(height - nextY, 0));
            }
            if (FlatRootPaneUI.this.titlePane != null) {
                FlatRootPaneUI.this.titlePane.menuBarLayouted();
            }
        }

        @Override
        public void invalidateLayout(Container parent) {
            if (FlatRootPaneUI.this.titlePane != null) {
                FlatRootPaneUI.this.titlePane.menuBarInvalidate();
            }
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }
    }
}

