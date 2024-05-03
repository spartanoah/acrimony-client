/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatNativeLinuxLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.ui.FlatTitlePaneIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public class FlatTitlePane
extends JComponent {
    private static final String KEY_DEBUG_SHOW_RECTANGLES = "FlatLaf.debug.titlebar.showRectangles";
    protected final Font titleFont;
    protected final Color activeBackground;
    protected final Color inactiveBackground;
    protected final Color activeForeground;
    protected final Color inactiveForeground;
    protected final Color embeddedForeground;
    protected final Color borderColor;
    protected final boolean showIcon;
    protected final boolean showIconInDialogs;
    protected final int noIconLeftGap;
    protected final Dimension iconSize;
    protected final int titleMinimumWidth;
    protected final int buttonMinimumWidth;
    protected final int buttonMaximizedHeight;
    protected final boolean centerTitle;
    protected final boolean centerTitleIfMenuBarEmbedded;
    protected final boolean showIconBesideTitle;
    protected final int menuBarTitleGap;
    protected final int menuBarTitleMinimumGap;
    protected final int menuBarResizeHeight;
    protected final JRootPane rootPane;
    protected final String windowStyle;
    protected JPanel leftPanel;
    protected JLabel iconLabel;
    protected JComponent menuBarPlaceholder;
    protected JLabel titleLabel;
    protected JPanel buttonPanel;
    protected JButton iconifyButton;
    protected JButton maximizeButton;
    protected JButton restoreButton;
    protected JButton closeButton;
    protected Window window;
    private final Handler handler;
    private int laterCounter;
    private int debugTitleBarHeight;
    private List<Rectangle> debugHitTestSpots;
    private Rectangle debugAppIconBounds;
    private Rectangle debugMinimizeButtonBounds;
    private Rectangle debugMaximizeButtonBounds;
    private Rectangle debugCloseButtonBounds;

    public FlatTitlePane(JRootPane rootPane) {
        this.rootPane = rootPane;
        Window w = SwingUtilities.getWindowAncestor(rootPane);
        String defaultWindowStyle = w != null && w.getType() == Window.Type.UTILITY ? "small" : null;
        this.windowStyle = FlatClientProperties.clientProperty(rootPane, "Window.style", defaultWindowStyle, String.class);
        this.titleFont = FlatUIUtils.getSubUIFont("TitlePane.font", this.windowStyle);
        this.activeBackground = FlatUIUtils.getSubUIColor("TitlePane.background", this.windowStyle);
        this.inactiveBackground = FlatUIUtils.getSubUIColor("TitlePane.inactiveBackground", this.windowStyle);
        this.activeForeground = FlatUIUtils.getSubUIColor("TitlePane.foreground", this.windowStyle);
        this.inactiveForeground = FlatUIUtils.getSubUIColor("TitlePane.inactiveForeground", this.windowStyle);
        this.embeddedForeground = FlatUIUtils.getSubUIColor("TitlePane.embeddedForeground", this.windowStyle);
        this.borderColor = UIManager.getColor("TitlePane.borderColor");
        this.showIcon = FlatUIUtils.getSubUIBoolean("TitlePane.showIcon", this.windowStyle, true);
        this.showIconInDialogs = FlatUIUtils.getSubUIBoolean("TitlePane.showIconInDialogs", this.windowStyle, true);
        this.noIconLeftGap = FlatUIUtils.getSubUIInt("TitlePane.noIconLeftGap", this.windowStyle, 8);
        this.iconSize = FlatUIUtils.getSubUIDimension("TitlePane.iconSize", this.windowStyle);
        this.titleMinimumWidth = FlatUIUtils.getSubUIInt("TitlePane.titleMinimumWidth", this.windowStyle, 60);
        this.buttonMinimumWidth = FlatUIUtils.getSubUIInt("TitlePane.buttonMinimumWidth", this.windowStyle, 30);
        this.buttonMaximizedHeight = FlatUIUtils.getSubUIInt("TitlePane.buttonMaximizedHeight", this.windowStyle, 0);
        this.centerTitle = FlatUIUtils.getSubUIBoolean("TitlePane.centerTitle", this.windowStyle, false);
        this.centerTitleIfMenuBarEmbedded = FlatUIUtils.getSubUIBoolean("TitlePane.centerTitleIfMenuBarEmbedded", this.windowStyle, true);
        this.showIconBesideTitle = FlatUIUtils.getSubUIBoolean("TitlePane.showIconBesideTitle", this.windowStyle, false);
        this.menuBarTitleGap = FlatUIUtils.getSubUIInt("TitlePane.menuBarTitleGap", this.windowStyle, 40);
        this.menuBarTitleMinimumGap = FlatUIUtils.getSubUIInt("TitlePane.menuBarTitleMinimumGap", this.windowStyle, 12);
        this.menuBarResizeHeight = FlatUIUtils.getSubUIInt("TitlePane.menuBarResizeHeight", this.windowStyle, 4);
        this.handler = this.createHandler();
        this.setBorder(this.createTitlePaneBorder());
        this.addSubComponents();
        this.activeChanged(true);
        this.addMouseListener(this.handler);
        this.addMouseMotionListener(this.handler);
        this.iconLabel.addMouseListener(this.handler);
        this.applyComponentOrientation(rootPane.getComponentOrientation());
    }

    protected FlatTitlePaneBorder createTitlePaneBorder() {
        return new FlatTitlePaneBorder();
    }

    protected Handler createHandler() {
        return new Handler();
    }

    protected void addSubComponents() {
        this.leftPanel = new JPanel();
        this.iconLabel = new JLabel();
        this.titleLabel = new JLabel(){

            @Override
            public void updateUI() {
                this.setUI(new FlatTitleLabelUI());
            }
        };
        this.iconLabel.setBorder(new FlatEmptyBorder(FlatUIUtils.getSubUIInsets("TitlePane.iconMargins", this.windowStyle)));
        this.titleLabel.setBorder(new FlatEmptyBorder(FlatUIUtils.getSubUIInsets("TitlePane.titleMargins", this.windowStyle)));
        this.leftPanel.setLayout(new BoxLayout(this.leftPanel, 2));
        this.leftPanel.setOpaque(false);
        this.leftPanel.add(this.iconLabel);
        this.menuBarPlaceholder = new JComponent(){

            @Override
            public Dimension getPreferredSize() {
                JMenuBar menuBar = FlatTitlePane.this.rootPane.getJMenuBar();
                return FlatTitlePane.this.hasVisibleEmbeddedMenuBar(menuBar) ? menuBar.getPreferredSize() : new Dimension();
            }
        };
        this.leftPanel.add(this.menuBarPlaceholder);
        this.createButtons();
        this.setLayout(new BorderLayout(){

            @Override
            public void layoutContainer(Container target) {
                Component horizontalGlue;
                Insets insets = target.getInsets();
                int x = insets.left;
                int y = insets.top;
                int w = target.getWidth() - insets.left - insets.right;
                int h = target.getHeight() - insets.top - insets.bottom;
                int leftWidth = FlatTitlePane.this.leftPanel.getPreferredSize().width;
                int buttonsWidth = FlatTitlePane.this.buttonPanel.getPreferredSize().width;
                int titleWidth = w - leftWidth - buttonsWidth;
                int minTitleWidth = UIScale.scale(FlatTitlePane.this.titleMinimumWidth);
                Icon icon = FlatTitlePane.this.titleLabel.getIcon();
                if (icon != null) {
                    Insets iconInsets = FlatTitlePane.this.iconLabel.getInsets();
                    int iconTextGap = FlatTitlePane.this.titleLabel.getComponentOrientation().isLeftToRight() ? iconInsets.right : iconInsets.left;
                    minTitleWidth += icon.getIconWidth() + iconTextGap;
                }
                if (titleWidth < minTitleWidth) {
                    buttonsWidth = Math.max(buttonsWidth - (minTitleWidth - titleWidth), FlatTitlePane.this.buttonPanel.getMinimumSize().width);
                    titleWidth = w - leftWidth - buttonsWidth;
                }
                if (titleWidth < minTitleWidth) {
                    int minLeftWidth = FlatTitlePane.this.iconLabel.isVisible() ? FlatTitlePane.this.iconLabel.getWidth() - FlatTitlePane.this.iconLabel.getInsets().right : UIScale.scale(FlatTitlePane.this.noIconLeftGap);
                    leftWidth = Math.max(leftWidth - (minTitleWidth - titleWidth), minLeftWidth);
                    titleWidth = w - leftWidth - buttonsWidth;
                }
                if (target.getComponentOrientation().isLeftToRight()) {
                    FlatTitlePane.this.leftPanel.setBounds(x, y, leftWidth, h);
                    FlatTitlePane.this.titleLabel.setBounds(x + leftWidth, y, titleWidth, h);
                    FlatTitlePane.this.buttonPanel.setBounds(x + leftWidth + titleWidth, y, buttonsWidth, h);
                } else {
                    FlatTitlePane.this.buttonPanel.setBounds(x, y, buttonsWidth, h);
                    FlatTitlePane.this.titleLabel.setBounds(x + buttonsWidth, y, titleWidth, h);
                    FlatTitlePane.this.leftPanel.setBounds(x + buttonsWidth + titleWidth, y, leftWidth, h);
                }
                JMenuBar menuBar = FlatTitlePane.this.rootPane.getJMenuBar();
                if (FlatTitlePane.this.hasVisibleEmbeddedMenuBar(menuBar) && (horizontalGlue = FlatTitlePane.this.findHorizontalGlue(menuBar)) != null) {
                    Point glueLocation = SwingUtilities.convertPoint(horizontalGlue, 0, 0, FlatTitlePane.this.titleLabel);
                    FlatTitlePane.this.titleLabel.setBounds(FlatTitlePane.this.titleLabel.getX() + glueLocation.x, FlatTitlePane.this.titleLabel.getY(), horizontalGlue.getWidth(), FlatTitlePane.this.titleLabel.getHeight());
                }
            }
        });
        this.add((Component)this.leftPanel, "Before");
        this.add((Component)this.titleLabel, "Center");
        this.add((Component)this.buttonPanel, "After");
    }

    protected void createButtons() {
        this.iconifyButton = this.createButton("TitlePane.iconifyIcon", "Iconify", e -> this.iconify());
        this.maximizeButton = this.createButton("TitlePane.maximizeIcon", "Maximize", e -> this.maximize());
        this.restoreButton = this.createButton("TitlePane.restoreIcon", "Restore", e -> this.restore());
        this.closeButton = this.createButton("TitlePane.closeIcon", "Close", e -> this.close());
        this.iconifyButton.setVisible(false);
        this.maximizeButton.setVisible(false);
        this.restoreButton.setVisible(false);
        this.buttonPanel = new JPanel(){

            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                if (FlatTitlePane.this.buttonMaximizedHeight > 0 && FlatTitlePane.this.isWindowMaximized() && !FlatTitlePane.this.hasVisibleEmbeddedMenuBar(FlatTitlePane.this.rootPane.getJMenuBar())) {
                    size = new Dimension(size.width, Math.min(size.height, UIScale.scale(FlatTitlePane.this.buttonMaximizedHeight)));
                }
                return size;
            }
        };
        this.buttonPanel.setOpaque(false);
        this.buttonPanel.setLayout(new BoxLayout(this.buttonPanel, 2));
        if (this.rootPane.getWindowDecorationStyle() == 1) {
            this.buttonPanel.add(this.iconifyButton);
            this.buttonPanel.add(this.maximizeButton);
            this.buttonPanel.add(this.restoreButton);
        }
        this.buttonPanel.add(this.closeButton);
    }

    protected JButton createButton(String iconKey, String accessibleName, ActionListener action) {
        JButton button = new JButton(FlatUIUtils.getSubUIIcon(iconKey, this.windowStyle)){

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(UIScale.scale(FlatTitlePane.this.buttonMinimumWidth), super.getMinimumSize().height);
            }
        };
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.putClientProperty("AccessibleName", accessibleName);
        button.addActionListener(action);
        return button;
    }

    protected void activeChanged(boolean active) {
        Color foreground;
        Color background = FlatClientProperties.clientPropertyColor(this.rootPane, "JRootPane.titleBarBackground", null);
        Color titleForeground = foreground = FlatClientProperties.clientPropertyColor(this.rootPane, "JRootPane.titleBarForeground", null);
        if (background == null) {
            background = FlatUIUtils.nonUIResource(active ? this.activeBackground : this.inactiveBackground);
        }
        if (foreground == null) {
            foreground = FlatUIUtils.nonUIResource(active ? this.activeForeground : this.inactiveForeground);
            titleForeground = active && this.hasVisibleEmbeddedMenuBar(this.rootPane.getJMenuBar()) ? FlatUIUtils.nonUIResource(this.embeddedForeground) : foreground;
        }
        this.setBackground(background);
        this.titleLabel.setForeground(titleForeground);
        this.iconifyButton.setForeground(foreground);
        this.maximizeButton.setForeground(foreground);
        this.restoreButton.setForeground(foreground);
        this.closeButton.setForeground(foreground);
        this.iconifyButton.setBackground(background);
        this.maximizeButton.setBackground(background);
        this.restoreButton.setBackground(background);
        this.closeButton.setBackground(background);
    }

    protected void frameStateChanged() {
        if (this.window == null || this.rootPane.getWindowDecorationStyle() != 1) {
            return;
        }
        this.updateVisibility();
        if (this.window instanceof Frame) {
            Frame frame = (Frame)this.window;
            if (!(!this.isWindowMaximized() || SystemInfo.isLinux && FlatNativeLinuxLibrary.isWMUtilsSupported(this.window) || this.rootPane.getClientProperty("_flatlaf.maximizedBoundsUpToDate") != null)) {
                this.rootPane.putClientProperty("_flatlaf.maximizedBoundsUpToDate", null);
                Rectangle oldMaximizedBounds = frame.getMaximizedBounds();
                this.updateMaximizedBounds();
                Rectangle newMaximizedBounds = frame.getMaximizedBounds();
                if (newMaximizedBounds != null && !newMaximizedBounds.equals(oldMaximizedBounds)) {
                    int oldExtendedState = frame.getExtendedState();
                    frame.setExtendedState(oldExtendedState & 0xFFFFFFF9);
                    frame.setExtendedState(oldExtendedState);
                }
            }
        }
    }

    protected void updateVisibility() {
        this.titleLabel.setVisible(FlatClientProperties.clientPropertyBoolean(this.rootPane, "JRootPane.titleBarShowTitle", true));
        this.closeButton.setVisible(FlatClientProperties.clientPropertyBoolean(this.rootPane, "JRootPane.titleBarShowClose", true));
        if (this.window instanceof Frame) {
            Frame frame = (Frame)this.window;
            boolean maximizable = frame.isResizable() && FlatClientProperties.clientPropertyBoolean(this.rootPane, "JRootPane.titleBarShowMaximize", true);
            boolean maximized = this.isWindowMaximized();
            this.iconifyButton.setVisible(FlatClientProperties.clientPropertyBoolean(this.rootPane, "JRootPane.titleBarShowIconify", true));
            this.maximizeButton.setVisible(maximizable && !maximized);
            this.restoreButton.setVisible(maximizable && maximized);
        } else {
            this.iconifyButton.setVisible(false);
            this.maximizeButton.setVisible(false);
            this.restoreButton.setVisible(false);
        }
    }

    protected void updateIcon() {
        boolean defaultShowIcon = this.showIcon;
        if (!this.showIconInDialogs && this.rootPane.getParent() instanceof JDialog) {
            defaultShowIcon = false;
        }
        List<Image> images = null;
        if (FlatClientProperties.clientPropertyBoolean(this.rootPane, "JRootPane.titleBarShowIcon", defaultShowIcon) && (images = this.window.getIconImages()).isEmpty()) {
            for (Window owner = this.window.getOwner(); owner != null && (images = owner.getIconImages()).isEmpty(); owner = owner.getOwner()) {
            }
        }
        boolean hasIcon = images != null && !images.isEmpty();
        this.iconLabel.setIcon(hasIcon && !this.showIconBesideTitle ? new FlatTitlePaneIcon(images, this.iconSize) : null);
        this.titleLabel.setIcon(hasIcon && this.showIconBesideTitle ? new FlatTitlePaneIcon(images, this.iconSize) : null);
        this.iconLabel.setVisible(hasIcon && !this.showIconBesideTitle);
        this.leftPanel.setBorder(hasIcon && !this.showIconBesideTitle ? null : FlatUIUtils.nonUIResource(new FlatEmptyBorder(0, this.noIconLeftGap, 0, 0)));
        this.updateNativeTitleBarHeightAndHitTestSpotsLater();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        this.uninstallWindowListeners();
        this.window = SwingUtilities.getWindowAncestor(this);
        if (this.window != null) {
            this.frameStateChanged();
            this.activeChanged(this.window.isActive());
            this.updateIcon();
            this.titleLabel.setText(this.getWindowTitle());
            this.installWindowListeners();
        }
        this.updateNativeTitleBarHeightAndHitTestSpotsLater();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.uninstallWindowListeners();
        this.window = null;
    }

    protected String getWindowTitle() {
        if (this.window instanceof Frame) {
            return ((Frame)this.window).getTitle();
        }
        if (this.window instanceof Dialog) {
            return ((Dialog)this.window).getTitle();
        }
        return null;
    }

    protected void installWindowListeners() {
        if (this.window == null) {
            return;
        }
        this.window.addPropertyChangeListener(this.handler);
        this.window.addWindowListener(this.handler);
        this.window.addWindowStateListener(this.handler);
        this.window.addComponentListener(this.handler);
    }

    protected void uninstallWindowListeners() {
        if (this.window == null) {
            return;
        }
        this.window.removePropertyChangeListener(this.handler);
        this.window.removeWindowListener(this.handler);
        this.window.removeWindowStateListener(this.handler);
        this.window.removeComponentListener(this.handler);
    }

    protected boolean hasVisibleEmbeddedMenuBar(JMenuBar menuBar) {
        return menuBar != null && menuBar.isVisible() && this.isMenuBarEmbedded();
    }

    protected boolean isMenuBarEmbedded() {
        return FlatUIUtils.getBoolean(this.rootPane, "flatlaf.menuBarEmbedded", "JRootPane.menuBarEmbedded", "TitlePane.menuBarEmbedded", false);
    }

    protected Rectangle getMenuBarBounds() {
        Insets insets = this.rootPane.getInsets();
        Rectangle bounds = new Rectangle(SwingUtilities.convertPoint(this.menuBarPlaceholder, -insets.left, -insets.top, this.rootPane), this.menuBarPlaceholder.getSize());
        Insets borderInsets = this.getBorder().getBorderInsets(this);
        bounds.height += borderInsets.bottom;
        Component horizontalGlue = this.findHorizontalGlue(this.rootPane.getJMenuBar());
        if (horizontalGlue != null) {
            boolean leftToRight = this.getComponentOrientation().isLeftToRight();
            int titleWidth = leftToRight ? this.buttonPanel.getX() - (this.leftPanel.getX() + this.leftPanel.getWidth()) : this.leftPanel.getX() - (this.buttonPanel.getX() + this.buttonPanel.getWidth());
            titleWidth = Math.max(titleWidth, 0);
            bounds.width += titleWidth;
            if (!leftToRight) {
                bounds.x -= titleWidth;
            }
        }
        return bounds;
    }

    protected Component findHorizontalGlue(JMenuBar menuBar) {
        if (menuBar == null) {
            return null;
        }
        int count = menuBar.getComponentCount();
        for (int i = count - 1; i >= 0; --i) {
            Component c = menuBar.getComponent(i);
            if (!(c instanceof Box.Filler) || c.getMaximumSize().width < Short.MAX_VALUE) continue;
            return c;
        }
        return null;
    }

    protected void titleBarColorsChanged() {
        this.activeChanged(this.window == null || this.window.isActive());
        this.repaint();
    }

    protected void menuBarChanged() {
        this.menuBarPlaceholder.invalidate();
        this.repaint();
        EventQueue.invokeLater(() -> this.activeChanged(this.window == null || this.window.isActive()));
    }

    protected void menuBarLayouted() {
        this.updateNativeTitleBarHeightAndHitTestSpotsLater();
        this.doLayout();
    }

    void menuBarInvalidate() {
        this.menuBarPlaceholder.invalidate();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!UIManager.getBoolean(KEY_DEBUG_SHOW_RECTANGLES)) {
            return;
        }
        if (this.debugTitleBarHeight > 0) {
            g.setColor(Color.green);
            g.drawLine(0, this.debugTitleBarHeight, this.getWidth(), this.debugTitleBarHeight);
        }
        if (this.debugHitTestSpots != null) {
            for (Rectangle r : this.debugHitTestSpots) {
                this.paintRect(g, Color.red, r);
            }
        }
        this.paintRect(g, Color.cyan, this.debugCloseButtonBounds);
        this.paintRect(g, Color.blue, this.debugAppIconBounds);
        this.paintRect(g, Color.blue, this.debugMinimizeButtonBounds);
        this.paintRect(g, Color.magenta, this.debugMaximizeButtonBounds);
        this.paintRect(g, Color.cyan, this.debugCloseButtonBounds);
    }

    private void paintRect(Graphics g, Color color, Rectangle r) {
        if (r == null) {
            return;
        }
        g.setColor(color);
        Point offset = SwingUtilities.convertPoint(this, 0, 0, this.window);
        g.drawRect(r.x - offset.x, r.y - offset.y, r.width - 1, r.height - 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(UIManager.getBoolean("TitlePane.unifiedBackground") && FlatClientProperties.clientPropertyColor(this.rootPane, "JRootPane.titleBarBackground", null) == null ? FlatUIUtils.getParentBackground(this) : this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    protected void repaintWindowBorder() {
        int width = this.rootPane.getWidth();
        int height = this.rootPane.getHeight();
        Insets insets = this.rootPane.getInsets();
        this.rootPane.repaint(0, 0, width, insets.top);
        this.rootPane.repaint(0, 0, insets.left, height);
        this.rootPane.repaint(0, height - insets.bottom, width, insets.bottom);
        this.rootPane.repaint(width - insets.right, 0, insets.right, height);
    }

    protected void iconify() {
        if (!(this.window instanceof Frame)) {
            return;
        }
        Frame frame = (Frame)this.window;
        if (!FlatNativeWindowBorder.showWindow(this.window, 6)) {
            frame.setExtendedState(frame.getExtendedState() | 1);
        }
    }

    protected boolean isWindowMaximized() {
        return this.window instanceof Frame && (((Frame)this.window).getExtendedState() & 6) == 6;
    }

    protected void maximize() {
        if (!(this.window instanceof Frame)) {
            return;
        }
        Frame frame = (Frame)this.window;
        this.updateMaximizedBounds();
        this.rootPane.putClientProperty("_flatlaf.maximizedBoundsUpToDate", true);
        if (!FlatNativeWindowBorder.showWindow(frame, 3)) {
            int oldState = frame.getExtendedState();
            int newState = oldState | 6;
            if (SystemInfo.isLinux && (oldState & 6) == 4) {
                newState = oldState & 0xFFFFFFF9 | 2;
            }
            frame.setExtendedState(newState);
        }
    }

    protected void updateMaximizedBounds() {
        Frame frame = (Frame)this.window;
        Rectangle oldMaximizedBounds = frame.getMaximizedBounds();
        if (!this.hasNativeCustomDecoration() && (oldMaximizedBounds == null || Objects.equals(oldMaximizedBounds, this.rootPane.getClientProperty("_flatlaf.maximizedBounds")))) {
            GraphicsConfiguration gc = this.window.getGraphicsConfiguration();
            Rectangle screenBounds = gc.getBounds();
            int maximizedX = screenBounds.x;
            int maximizedY = screenBounds.y;
            int maximizedWidth = screenBounds.width;
            int maximizedHeight = screenBounds.height;
            if (SystemInfo.isWindows && !this.isMaximizedBoundsFixed()) {
                maximizedX = 0;
                maximizedY = 0;
                AffineTransform defaultTransform = gc.getDefaultTransform();
                maximizedWidth = (int)((double)maximizedWidth * defaultTransform.getScaleX());
                maximizedHeight = (int)((double)maximizedHeight * defaultTransform.getScaleY());
            }
            Insets screenInsets = this.window.getToolkit().getScreenInsets(gc);
            Rectangle newMaximizedBounds = new Rectangle(maximizedX + screenInsets.left, maximizedY + screenInsets.top, maximizedWidth - screenInsets.left - screenInsets.right, maximizedHeight - screenInsets.top - screenInsets.bottom);
            if (!Objects.equals(oldMaximizedBounds, newMaximizedBounds)) {
                frame.setMaximizedBounds(newMaximizedBounds);
                this.rootPane.putClientProperty("_flatlaf.maximizedBounds", newMaximizedBounds);
            }
        }
    }

    private boolean isMaximizedBoundsFixed() {
        return SystemInfo.isJava_15_orLater || SystemInfo.javaVersion >= SystemInfo.toVersion(11, 0, 8, 0) && SystemInfo.javaVersion < SystemInfo.toVersion(12, 0, 0, 0) || SystemInfo.javaVersion >= SystemInfo.toVersion(13, 0, 4, 0) && SystemInfo.javaVersion < SystemInfo.toVersion(14, 0, 0, 0);
    }

    protected void restore() {
        if (!(this.window instanceof Frame)) {
            return;
        }
        Frame frame = (Frame)this.window;
        if (!FlatNativeWindowBorder.showWindow(this.window, 9)) {
            int state = frame.getExtendedState();
            frame.setExtendedState((state & 1) != 0 ? state & 0xFFFFFFFE : state & 0xFFFFFFF9);
        }
    }

    private void maximizeOrRestore() {
        if (!(this.window instanceof Frame) || !((Frame)this.window).isResizable()) {
            return;
        }
        if (this.isWindowMaximized()) {
            this.restore();
        } else {
            this.maximize();
        }
    }

    protected void close() {
        if (this.window != null) {
            this.window.dispatchEvent(new WindowEvent(this.window, 201));
        }
    }

    protected boolean hasNativeCustomDecoration() {
        return this.window != null && FlatNativeWindowBorder.hasCustomDecoration(this.window);
    }

    protected void updateNativeTitleBarHeightAndHitTestSpotsLater() {
        ++this.laterCounter;
        EventQueue.invokeLater(() -> {
            --this.laterCounter;
            if (this.laterCounter == 0) {
                this.updateNativeTitleBarHeightAndHitTestSpots();
            }
        });
    }

    protected void updateNativeTitleBarHeightAndHitTestSpots() {
        JMenuBar menuBar;
        if (!this.isDisplayable()) {
            return;
        }
        if (!this.hasNativeCustomDecoration()) {
            return;
        }
        int titleBarHeight = this.getHeight();
        if (titleBarHeight > 0) {
            --titleBarHeight;
        }
        ArrayList<Rectangle> hitTestSpots = new ArrayList<Rectangle>();
        Rectangle appIconBounds = null;
        if (!this.showIconBesideTitle && this.iconLabel.isVisible()) {
            Point location = SwingUtilities.convertPoint(this.iconLabel, 0, 0, this.window);
            Insets iconInsets = this.iconLabel.getInsets();
            Rectangle iconBounds = new Rectangle(location.x + iconInsets.left - 1, location.y + iconInsets.top - 1, this.iconLabel.getWidth() - iconInsets.left - iconInsets.right + 2, this.iconLabel.getHeight() - iconInsets.top - iconInsets.bottom + 2);
            if (this.isWindowMaximized()) {
                iconBounds.height += iconBounds.y;
                iconBounds.y = 0;
                if (this.window.getComponentOrientation().isLeftToRight()) {
                    iconBounds.width += iconBounds.x;
                    iconBounds.x = 0;
                } else {
                    iconBounds.width += iconInsets.right;
                }
            }
            appIconBounds = iconBounds;
        } else if (this.showIconBesideTitle && this.titleLabel.getIcon() != null && this.titleLabel.getUI() instanceof FlatTitleLabelUI) {
            FlatTitleLabelUI ui = (FlatTitleLabelUI)this.titleLabel.getUI();
            Insets insets = this.titleLabel.getInsets();
            Rectangle viewR = new Rectangle(insets.left, insets.top, this.titleLabel.getWidth() - insets.left - insets.right, this.titleLabel.getHeight() - insets.top - insets.bottom);
            Rectangle iconR = new Rectangle();
            Rectangle textR = new Rectangle();
            ui.layoutCL(this.titleLabel, this.titleLabel.getFontMetrics(this.titleLabel.getFont()), this.titleLabel.getText(), this.titleLabel.getIcon(), viewR, iconR, textR);
            if (iconR.x == 0) {
                Point location = SwingUtilities.convertPoint(this.titleLabel, 0, 0, this.window);
                iconR.x += location.x;
                iconR.y += location.y;
                --iconR.x;
                --iconR.y;
                iconR.width += 2;
                iconR.height += 2;
                appIconBounds = iconR;
            }
        }
        Rectangle r = this.getNativeHitTestSpot(this.buttonPanel);
        if (r != null) {
            hitTestSpots.add(r);
        }
        if (this.hasVisibleEmbeddedMenuBar(menuBar = this.rootPane.getJMenuBar()) && (r = this.getNativeHitTestSpot(menuBar)) != null) {
            if (this.window instanceof Frame && ((Frame)this.window).isResizable() && !this.isWindowMaximized()) {
                int resizeHeight = UIScale.scale(Math.min(this.menuBarResizeHeight, 8));
                r.y += resizeHeight;
                r.height -= resizeHeight;
            }
            int count = menuBar.getComponentCount();
            for (int i = count - 1; i >= 0; --i) {
                Rectangle r2;
                Component c = menuBar.getComponent(i);
                if (!(c instanceof Box.Filler) && (!(c instanceof JComponent) || !FlatClientProperties.clientPropertyBoolean((JComponent)c, "JComponent.titleBarCaption", false))) continue;
                Point glueLocation = SwingUtilities.convertPoint(c, 0, 0, this.window);
                int x2 = glueLocation.x + c.getWidth();
                if (this.getComponentOrientation().isLeftToRight()) {
                    r2 = new Rectangle(x2, r.y, r.x + r.width - x2, r.height);
                    r.width = glueLocation.x - r.x;
                } else {
                    r2 = new Rectangle(r.x, r.y, glueLocation.x - r.x, r.height);
                    r.width = r.x + r.width - x2;
                    r.x = x2;
                }
                if (r2.width <= 0) continue;
                hitTestSpots.add(r2);
            }
            hitTestSpots.add(r);
        }
        for (Component c : this.rootPane.getLayeredPane().getComponents()) {
            Rectangle rectangle = r = c instanceof JInternalFrame ? this.getNativeHitTestSpot((JInternalFrame)c) : null;
            if (r == null) continue;
            hitTestSpots.add(r);
        }
        Rectangle minimizeButtonBounds = this.boundsInWindow(this.iconifyButton);
        Rectangle maximizeButtonBounds = this.boundsInWindow(this.maximizeButton.isVisible() ? this.maximizeButton : this.restoreButton);
        Rectangle closeButtonBounds = this.boundsInWindow(this.closeButton);
        FlatNativeWindowBorder.setTitleBarHeightAndHitTestSpots(this.window, titleBarHeight, hitTestSpots, appIconBounds, minimizeButtonBounds, maximizeButtonBounds, closeButtonBounds);
        this.debugTitleBarHeight = titleBarHeight;
        this.debugHitTestSpots = hitTestSpots;
        this.debugAppIconBounds = appIconBounds;
        this.debugMinimizeButtonBounds = minimizeButtonBounds;
        this.debugMaximizeButtonBounds = maximizeButtonBounds;
        this.debugCloseButtonBounds = closeButtonBounds;
        if (UIManager.getBoolean(KEY_DEBUG_SHOW_RECTANGLES)) {
            this.repaint();
        }
    }

    private Rectangle boundsInWindow(JComponent c) {
        return c.isShowing() ? SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), this.window) : null;
    }

    protected Rectangle getNativeHitTestSpot(JComponent c) {
        Dimension size = c.getSize();
        if (size.width <= 0 || size.height <= 0) {
            return null;
        }
        Point location = SwingUtilities.convertPoint(c, 0, 0, this.window);
        Rectangle r = new Rectangle(location, size);
        return r;
    }

    protected class Handler
    extends WindowAdapter
    implements PropertyChangeListener,
    MouseListener,
    MouseMotionListener,
    ComponentListener {
        private Point dragOffset;
        private boolean linuxNativeMove;
        private long lastSingleClickWhen;

        protected Handler() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "title": {
                    FlatTitlePane.this.titleLabel.setText(FlatTitlePane.this.getWindowTitle());
                    break;
                }
                case "resizable": {
                    if (!(FlatTitlePane.this.window instanceof Frame)) break;
                    FlatTitlePane.this.frameStateChanged();
                    break;
                }
                case "iconImage": {
                    FlatTitlePane.this.updateIcon();
                    break;
                }
                case "componentOrientation": {
                    FlatTitlePane.this.updateNativeTitleBarHeightAndHitTestSpotsLater();
                }
            }
        }

        @Override
        public void windowActivated(WindowEvent e) {
            FlatTitlePane.this.activeChanged(true);
            FlatTitlePane.this.updateNativeTitleBarHeightAndHitTestSpots();
            if (!SystemInfo.isWindows_11_orLater && FlatTitlePane.this.hasNativeCustomDecoration()) {
                FlatNativeWindowBorder.WindowTopBorder.getInstance().repaintBorder(FlatTitlePane.this);
            }
            FlatTitlePane.this.repaintWindowBorder();
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            FlatTitlePane.this.activeChanged(false);
            FlatTitlePane.this.updateNativeTitleBarHeightAndHitTestSpots();
            if (!SystemInfo.isWindows_11_orLater && FlatTitlePane.this.hasNativeCustomDecoration()) {
                FlatNativeWindowBorder.WindowTopBorder.getInstance().repaintBorder(FlatTitlePane.this);
            }
            FlatTitlePane.this.repaintWindowBorder();
        }

        @Override
        public void windowStateChanged(WindowEvent e) {
            FlatTitlePane.this.frameStateChanged();
            FlatTitlePane.this.updateNativeTitleBarHeightAndHitTestSpots();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (this.linuxNativeMove && SystemInfo.isLinux && FlatNativeLinuxLibrary.isWMUtilsSupported(FlatTitlePane.this.window)) {
                if (this.lastSingleClickWhen != 0L && e.getWhen() - this.lastSingleClickWhen <= (long)this.getMultiClickInterval()) {
                    this.lastSingleClickWhen = 0L;
                    FlatTitlePane.this.maximizeOrRestore();
                }
                return;
            }
            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                if (e.getSource() == FlatTitlePane.this.iconLabel) {
                    FlatTitlePane.this.close();
                } else if (!FlatTitlePane.this.hasNativeCustomDecoration()) {
                    FlatTitlePane.this.maximizeOrRestore();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (FlatTitlePane.this.window == null) {
                return;
            }
            if (SwingUtilities.isRightMouseButton(e) && SystemInfo.isLinux && FlatNativeLinuxLibrary.isWMUtilsSupported(FlatTitlePane.this.window)) {
                e.consume();
                FlatNativeLinuxLibrary.showWindowMenu(FlatTitlePane.this.window, e);
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            this.dragOffset = SwingUtilities.convertPoint(FlatTitlePane.this, e.getPoint(), FlatTitlePane.this.window);
            this.linuxNativeMove = false;
            if (SystemInfo.isLinux && FlatNativeLinuxLibrary.isWMUtilsSupported(FlatTitlePane.this.window)) {
                int clickCount = e.getClickCount();
                if (clickCount == 1 && this.lastSingleClickWhen != 0L && e.getWhen() - this.lastSingleClickWhen <= (long)this.getMultiClickInterval()) {
                    clickCount = 2;
                }
                switch (clickCount) {
                    case 1: {
                        e.consume();
                        this.linuxNativeMove = FlatNativeLinuxLibrary.moveOrResizeWindow(FlatTitlePane.this.window, e, 8);
                        this.lastSingleClickWhen = e.getWhen();
                        break;
                    }
                    case 2: {
                        this.lastSingleClickWhen = 0L;
                        FlatTitlePane.this.maximizeOrRestore();
                    }
                }
            }
        }

        private int getMultiClickInterval() {
            Object value = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
            return value instanceof Integer ? (Integer)value : 500;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Frame frame;
            int state;
            if (FlatTitlePane.this.window == null || this.dragOffset == null) {
                return;
            }
            if (this.linuxNativeMove) {
                return;
            }
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            if (FlatTitlePane.this.hasNativeCustomDecoration()) {
                return;
            }
            if (FlatTitlePane.this.window instanceof Frame && ((state = (frame = (Frame)FlatTitlePane.this.window).getExtendedState()) & 6) != 0) {
                int maximizedWidth = FlatTitlePane.this.window.getWidth();
                frame.setExtendedState(state & 0xFFFFFFF9);
                int restoredWidth = FlatTitlePane.this.window.getWidth();
                int center = restoredWidth / 2;
                if (this.dragOffset.x > center) {
                    this.dragOffset.x = this.dragOffset.x > maximizedWidth - center ? restoredWidth - (maximizedWidth - this.dragOffset.x) : center;
                }
            }
            int newX = e.getXOnScreen() - this.dragOffset.x;
            int newY = e.getYOnScreen() - this.dragOffset.y;
            if (newX == FlatTitlePane.this.window.getX() && newY == FlatTitlePane.this.window.getY()) {
                return;
            }
            FlatTitlePane.this.window.setLocation(newX, newY);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            FlatTitlePane.this.updateNativeTitleBarHeightAndHitTestSpotsLater();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            FlatTitlePane.this.frameStateChanged();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    protected class FlatTitleLabelUI
    extends FlatLabelUI {
        protected FlatTitleLabelUI() {
            super(false);
        }

        @Override
        protected void installDefaults(JLabel c) {
            super.installDefaults(c);
            if (FlatTitlePane.this.titleFont != null) {
                c.setFont(FlatTitlePane.this.titleFont);
            }
        }

        @Override
        protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
            String clippedText;
            JMenuBar menuBar = FlatTitlePane.this.rootPane.getJMenuBar();
            boolean hasEmbeddedMenuBar = FlatTitlePane.this.hasVisibleEmbeddedMenuBar(menuBar);
            boolean hasEmbeddedLeadingMenus = hasEmbeddedMenuBar && this.hasLeadingMenus(menuBar);
            boolean leftToRight = FlatTitlePane.this.getComponentOrientation().isLeftToRight();
            if (hasEmbeddedMenuBar) {
                Component horizontalGlue;
                int minGap = UIScale.scale(FlatTitlePane.this.menuBarTitleMinimumGap);
                if (hasEmbeddedLeadingMenus) {
                    if (leftToRight) {
                        viewR.x += minGap;
                    }
                    viewR.width -= minGap;
                }
                if ((horizontalGlue = FlatTitlePane.this.findHorizontalGlue(menuBar)) != null && menuBar.getComponent(menuBar.getComponentCount() - 1) != horizontalGlue) {
                    if (!leftToRight) {
                        viewR.x += minGap;
                    }
                    viewR.width -= minGap;
                }
            }
            int iconTextGap = 0;
            int iconWidthAndGap = 0;
            if (icon != null) {
                Insets iconInsets = FlatTitlePane.this.iconLabel.getInsets();
                iconTextGap = leftToRight ? iconInsets.right : iconInsets.left;
                iconWidthAndGap = icon.getIconWidth() + iconTextGap;
            }
            if (!(clippedText = SwingUtilities.layoutCompoundLabel(label, fontMetrics, text, icon, label.getVerticalAlignment(), label.getHorizontalAlignment(), label.getVerticalTextPosition(), label.getHorizontalTextPosition(), viewR, iconR, textR, iconTextGap)).equals(text)) {
                textR.x = leftToRight ? viewR.x + iconWidthAndGap : viewR.x + viewR.width - iconWidthAndGap - textR.width;
            } else {
                boolean center;
                int leadingGap = hasEmbeddedLeadingMenus ? UIScale.scale(FlatTitlePane.this.menuBarTitleGap - FlatTitlePane.this.menuBarTitleMinimumGap) : 0;
                boolean bl = center = hasEmbeddedLeadingMenus ? FlatTitlePane.this.centerTitleIfMenuBarEmbedded : FlatTitlePane.this.centerTitle;
                if (center) {
                    Container parent = label.getParent();
                    int centeredTextX = parent != null ? (parent.getWidth() - textR.width - iconWidthAndGap) / 2 + iconWidthAndGap - label.getX() : -1;
                    textR.x = centeredTextX >= viewR.x + leadingGap && centeredTextX + textR.width <= viewR.x + viewR.width - leadingGap ? centeredTextX : viewR.x + (viewR.width - textR.width - iconWidthAndGap) / 2 + iconWidthAndGap;
                } else {
                    int n = textR.x = leftToRight ? Math.min(viewR.x + leadingGap + iconWidthAndGap, viewR.x + viewR.width - textR.width) : Math.max(viewR.x + viewR.width - leadingGap - iconWidthAndGap - textR.width, viewR.x);
                }
            }
            if (icon != null) {
                iconR.x = leftToRight ? textR.x - iconWidthAndGap : textR.x + textR.width + iconTextGap;
            }
            return clippedText;
        }

        private boolean hasLeadingMenus(JMenuBar menuBar) {
            boolean leftToRight;
            if (menuBar.getComponentCount() == 0 || menuBar.getWidth() == 0) {
                return false;
            }
            Component horizontalGlue = FlatTitlePane.this.findHorizontalGlue(menuBar);
            return horizontalGlue == null || (!(leftToRight = FlatTitlePane.this.getComponentOrientation().isLeftToRight()) || horizontalGlue.getX() != 0) && (leftToRight || horizontalGlue.getX() + horizontalGlue.getWidth() != menuBar.getWidth());
        }
    }

    protected class FlatTitlePaneBorder
    extends AbstractBorder {
        protected FlatTitlePaneBorder() {
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            super.getBorderInsets(c, insets);
            Border menuBarBorder = this.getMenuBarBorder();
            if (menuBarBorder != null) {
                Insets menuBarInsets = menuBarBorder.getBorderInsets(c);
                insets.bottom += menuBarInsets.bottom;
            } else if (!(FlatTitlePane.this.borderColor == null || FlatTitlePane.this.rootPane.getJMenuBar() != null && FlatTitlePane.this.rootPane.getJMenuBar().isVisible())) {
                insets.bottom += UIScale.scale(1);
            }
            if (!SystemInfo.isWindows_11_orLater && FlatTitlePane.this.hasNativeCustomDecoration() && !FlatTitlePane.this.isWindowMaximized()) {
                insets = FlatUIUtils.addInsets(insets, FlatNativeWindowBorder.WindowTopBorder.getInstance().getBorderInsets());
            }
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Border menuBarBorder = this.getMenuBarBorder();
            if (menuBarBorder != null) {
                menuBarBorder.paintBorder(FlatTitlePane.this.rootPane.getJMenuBar(), g, x, y, width, height);
            } else if (!(FlatTitlePane.this.borderColor == null || FlatTitlePane.this.rootPane.getJMenuBar() != null && FlatTitlePane.this.rootPane.getJMenuBar().isVisible())) {
                float lineHeight = UIScale.scale(1.0f);
                FlatUIUtils.paintFilledRectangle(g, FlatTitlePane.this.borderColor, x, (float)(y + height) - lineHeight, width, lineHeight);
            }
            if (!SystemInfo.isWindows_11_orLater && FlatTitlePane.this.hasNativeCustomDecoration() && !FlatTitlePane.this.isWindowMaximized()) {
                FlatNativeWindowBorder.WindowTopBorder.getInstance().paintBorder(c, g, x, y, width, height);
            }
        }

        protected Border getMenuBarBorder() {
            JMenuBar menuBar = FlatTitlePane.this.rootPane.getJMenuBar();
            return FlatTitlePane.this.hasVisibleEmbeddedMenuBar(menuBar) ? menuBar.getBorder() : null;
        }
    }
}

