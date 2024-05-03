/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneLayout;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

public class FlatScrollPaneUI
extends BasicScrollPaneUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected Boolean showButtons;
    private Handler handler;
    private Map<String, Object> oldStyleValues;
    private AtomicBoolean borderShared;

    public static ComponentUI createUI(JComponent c) {
        return new FlatScrollPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        if (FlatUIUtils.needsLightAWTPeer(c)) {
            FlatUIUtils.runWithLightAWTPeerUIDefaults(() -> this.installUIImpl(c));
        } else {
            this.installUIImpl(c);
        }
    }

    private void installUIImpl(JComponent c) {
        super.installUI(c);
        int focusWidth = UIManager.getInt("Component.focusWidth");
        int arc = UIManager.getInt("ScrollPane.arc");
        LookAndFeel.installProperty(c, "opaque", focusWidth == 0 && arc == 0);
        LayoutManager layout = c.getLayout();
        if (layout != null && layout.getClass() == ScrollPaneLayout.UIResource.class) {
            c.setLayout(this.createScrollPaneLayout());
        }
        this.installStyle();
        MigLayoutVisualPadding.install(this.scrollpane);
    }

    @Override
    public void uninstallUI(JComponent c) {
        MigLayoutVisualPadding.uninstall(this.scrollpane);
        if (c.getLayout() instanceof FlatScrollPaneLayout) {
            c.setLayout(new ScrollPaneLayout.UIResource());
        }
        super.uninstallUI(c);
        this.oldStyleValues = null;
        this.borderShared = null;
    }

    @Override
    protected void installListeners(JScrollPane c) {
        super.installListeners(c);
        this.addViewportListeners(this.scrollpane.getViewport());
    }

    @Override
    protected void uninstallListeners(JComponent c) {
        super.uninstallListeners(c);
        this.removeViewportListeners(this.scrollpane.getViewport());
        this.handler = null;
    }

    protected FlatScrollPaneLayout createScrollPaneLayout() {
        return new FlatScrollPaneLayout();
    }

    @Override
    protected MouseWheelListener createMouseWheelListener() {
        MouseWheelListener superListener = super.createMouseWheelListener();
        return e -> {
            if (this.isSmoothScrollingEnabled() && this.scrollpane.isWheelScrollingEnabled() && e.getScrollType() == 0 && e.getPreciseWheelRotation() != 0.0 && e.getPreciseWheelRotation() != (double)e.getWheelRotation()) {
                this.mouseWheelMovedSmooth(e);
            } else {
                superListener.mouseWheelMoved(e);
            }
        };
    }

    protected boolean isSmoothScrollingEnabled() {
        Object smoothScrolling = this.scrollpane.getClientProperty("JScrollPane.smoothScrolling");
        if (smoothScrolling instanceof Boolean) {
            return (Boolean)smoothScrolling;
        }
        return UIManager.getBoolean("ScrollPane.smoothScrolling");
    }

    private void mouseWheelMovedSmooth(MouseWheelEvent e) {
        int maxValue;
        int unitIncrement;
        JViewport viewport = this.scrollpane.getViewport();
        if (viewport == null) {
            return;
        }
        JScrollBar scrollbar = this.scrollpane.getVerticalScrollBar();
        if (!(scrollbar != null && scrollbar.isVisible() && !e.isShiftDown() || (scrollbar = this.scrollpane.getHorizontalScrollBar()) != null && scrollbar.isVisible())) {
            return;
        }
        e.consume();
        double rotation = e.getPreciseWheelRotation();
        int orientation = scrollbar.getOrientation();
        Component view = viewport.getView();
        if (view instanceof Scrollable) {
            Scrollable scrollable = (Scrollable)((Object)view);
            Rectangle visibleRect = new Rectangle(viewport.getViewSize());
            unitIncrement = scrollable.getScrollableUnitIncrement(visibleRect, orientation, 1);
            if (unitIncrement > 0) {
                if (orientation == 1) {
                    visibleRect.y += unitIncrement;
                    visibleRect.height -= unitIncrement;
                } else {
                    visibleRect.x += unitIncrement;
                    visibleRect.width -= unitIncrement;
                }
                int unitIncrement2 = scrollable.getScrollableUnitIncrement(visibleRect, orientation, 1);
                if (unitIncrement2 > 0) {
                    unitIncrement = Math.min(unitIncrement, unitIncrement2);
                }
            }
        } else {
            int direction = rotation < 0.0 ? -1 : 1;
            unitIncrement = scrollbar.getUnitIncrement(direction);
        }
        int viewportWH = orientation == 1 ? viewport.getHeight() : viewport.getWidth();
        int scrollIncrement = Math.min(unitIncrement * e.getScrollAmount(), viewportWH);
        double delta = rotation * (double)scrollIncrement;
        int idelta = (int)Math.round(delta);
        if (idelta == 0) {
            if (rotation > 0.0) {
                idelta = 1;
            } else if (rotation < 0.0) {
                idelta = -1;
            }
        }
        int value = scrollbar.getValue();
        int minValue = scrollbar.getMinimum();
        int newValue = Math.max(minValue, Math.min(value + idelta, maxValue = scrollbar.getMaximum() - scrollbar.getModel().getExtent()));
        if (newValue != value) {
            scrollbar.setValue(newValue);
        }
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        PropertyChangeListener superListener = super.createPropertyChangeListener();
        return e -> {
            superListener.propertyChange(e);
            switch (e.getPropertyName()) {
                case "JScrollBar.showButtons": {
                    JScrollBar vsb = this.scrollpane.getVerticalScrollBar();
                    JScrollBar hsb = this.scrollpane.getHorizontalScrollBar();
                    if (vsb != null) {
                        vsb.revalidate();
                        vsb.repaint();
                    }
                    if (hsb == null) break;
                    hsb.revalidate();
                    hsb.repaint();
                    break;
                }
                case "LOWER_LEFT_CORNER": 
                case "LOWER_RIGHT_CORNER": 
                case "UPPER_LEFT_CORNER": 
                case "UPPER_RIGHT_CORNER": {
                    Object corner = e.getNewValue();
                    if (!(corner instanceof JButton) || !(((JButton)corner).getBorder() instanceof FlatButtonBorder) || !(FlatScrollPaneUI.getView(this.scrollpane) instanceof JTable)) break;
                    ((JButton)corner).setBorder(BorderFactory.createEmptyBorder());
                    ((JButton)corner).setFocusable(false);
                    break;
                }
                case "JComponent.outline": {
                    this.scrollpane.repaint();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    this.installStyle();
                    this.scrollpane.revalidate();
                    this.scrollpane.repaint();
                    break;
                }
                case "border": {
                    Object newBorder = e.getNewValue();
                    if (newBorder == null || newBorder != UIManager.getBorder("Table.scrollPaneBorder")) break;
                    this.borderShared = null;
                    this.installStyle();
                    this.scrollpane.revalidate();
                    this.scrollpane.repaint();
                }
            }
        };
    }

    private Handler getHandler() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        return this.handler;
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.scrollpane, "ScrollPane"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        if (key.equals("focusWidth") || key.equals("arc")) {
            int focusWidth = value instanceof Integer ? (Integer)value : UIManager.getInt("Component.focusWidth");
            int arc = value instanceof Integer ? (Integer)value : UIManager.getInt("ScrollPane.arc");
            LookAndFeel.installProperty(this.scrollpane, "opaque", focusWidth == 0 && arc == 0);
        }
        if (this.borderShared == null) {
            this.borderShared = new AtomicBoolean(true);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrBorder(this, key, value, this.scrollpane, this.borderShared);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this, this.scrollpane.getBorder());
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, this.scrollpane.getBorder(), key);
    }

    @Override
    protected void updateViewport(PropertyChangeEvent e) {
        super.updateViewport(e);
        JViewport oldViewport = (JViewport)e.getOldValue();
        JViewport newViewport = (JViewport)e.getNewValue();
        this.removeViewportListeners(oldViewport);
        this.addViewportListeners(newViewport);
    }

    private void addViewportListeners(JViewport viewport) {
        if (viewport == null) {
            return;
        }
        viewport.addContainerListener(this.getHandler());
        Component view = viewport.getView();
        if (view != null) {
            view.addFocusListener(this.getHandler());
        }
    }

    private void removeViewportListeners(JViewport viewport) {
        if (viewport == null) {
            return;
        }
        viewport.removeContainerListener(this.getHandler());
        Component view = viewport.getView();
        if (view != null) {
            view.removeFocusListener(this.getHandler());
        }
    }

    @Override
    public void update(Graphics g, JComponent c) {
        Component view;
        float arc;
        if (c.isOpaque()) {
            FlatUIUtils.paintParentBackground(g, c);
            Insets insets = c.getInsets();
            g.setColor(c.getBackground());
            g.fillRect(insets.left, insets.top, c.getWidth() - insets.left - insets.right, c.getHeight() - insets.top - insets.bottom);
        }
        if ((arc = FlatScrollPaneUI.getBorderArc(this.scrollpane)) > 0.0f && (view = FlatScrollPaneUI.getView(this.scrollpane)) != null) {
            float focusWidth = FlatUIUtils.getBorderFocusWidth(c);
            g.setColor(view.getBackground());
            Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
            FlatUIUtils.paintComponentBackground((Graphics2D)g, 0, 0, c.getWidth(), c.getHeight(), focusWidth, arc);
            FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
        }
        this.paint(g, c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Border viewportBorder = this.scrollpane.getViewportBorder();
        if (viewportBorder != null) {
            Rectangle r = this.scrollpane.getViewportBorderBounds();
            int padding = FlatScrollPaneUI.getBorderLeftRightPadding(this.scrollpane);
            JScrollBar vsb = this.scrollpane.getVerticalScrollBar();
            if (padding > 0 && vsb != null && vsb.isVisible() && this.scrollpane.getLayout() instanceof FlatScrollPaneLayout && ((FlatScrollPaneLayout)this.scrollpane.getLayout()).canIncreaseViewportWidth(this.scrollpane)) {
                boolean ltr = this.scrollpane.getComponentOrientation().isLeftToRight();
                int extraWidth = Math.min(padding, vsb.getWidth());
                viewportBorder.paintBorder(this.scrollpane, g, r.x - (ltr ? 0 : extraWidth), r.y, r.width + extraWidth, r.height);
            } else {
                viewportBorder.paintBorder(this.scrollpane, g, r.x, r.y, r.width, r.height);
            }
        }
    }

    public static boolean isPermanentFocusOwner(JScrollPane scrollPane) {
        Component focusOwner;
        Component view = FlatScrollPaneUI.getView(scrollPane);
        if (view == null) {
            return false;
        }
        if (FlatUIUtils.isPermanentFocusOwner(view)) {
            return true;
        }
        if ((view instanceof JTable && ((JTable)view).isEditing() || view instanceof JTree && ((JTree)view).isEditing()) && (focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) != null) {
            return SwingUtilities.isDescendingFrom(focusOwner, view);
        }
        return false;
    }

    static Component getView(JScrollPane scrollPane) {
        JViewport viewport = scrollPane.getViewport();
        return viewport != null ? viewport.getView() : null;
    }

    private static float getBorderArc(JScrollPane scrollPane) {
        Border border = scrollPane.getBorder();
        return border instanceof FlatScrollPaneBorder ? UIScale.scale((float)((FlatScrollPaneBorder)border).getArc(scrollPane)) : 0.0f;
    }

    private static int getBorderLeftRightPadding(JScrollPane scrollPane) {
        Border border = scrollPane.getBorder();
        return border instanceof FlatScrollPaneBorder ? ((FlatScrollPaneBorder)border).getLeftRightPadding(scrollPane) : 0;
    }

    protected static class FlatScrollPaneLayout
    extends ScrollPaneLayout.UIResource {
        protected FlatScrollPaneLayout() {
        }

        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            JScrollPane scrollPane = (JScrollPane)parent;
            int padding = FlatScrollPaneUI.getBorderLeftRightPadding(scrollPane);
            if (padding > 0 && this.vsb != null && this.vsb.isVisible()) {
                Insets insets = scrollPane.getInsets();
                Rectangle r = this.vsb.getBounds();
                int y = Math.max(r.y, insets.top + padding);
                int y2 = Math.min(r.y + r.height, scrollPane.getHeight() - insets.bottom - padding);
                boolean ltr = scrollPane.getComponentOrientation().isLeftToRight();
                this.vsb.setBounds(r.x + (ltr ? padding : -padding), y, r.width, y2 - y);
                if (this.canIncreaseViewportWidth(scrollPane)) {
                    int extraWidth = Math.min(padding, this.vsb.getWidth());
                    FlatScrollPaneLayout.resizeViewport(this.viewport, extraWidth, ltr);
                    FlatScrollPaneLayout.resizeViewport(this.colHead, extraWidth, ltr);
                    FlatScrollPaneLayout.resizeViewport(this.hsb, extraWidth, ltr);
                }
            }
        }

        boolean canIncreaseViewportWidth(JScrollPane scrollPane) {
            return scrollPane.getComponentOrientation().isLeftToRight() ? !FlatScrollPaneLayout.isCornerVisible(this.upperRight) && !FlatScrollPaneLayout.isCornerVisible(this.lowerRight) : !FlatScrollPaneLayout.isCornerVisible(this.upperLeft) && !FlatScrollPaneLayout.isCornerVisible(this.lowerLeft);
        }

        private static boolean isCornerVisible(Component corner) {
            return corner != null && corner.getWidth() > 0 && corner.getHeight() > 0 && corner.isVisible();
        }

        private static void resizeViewport(Component c, int extraWidth, boolean ltr) {
            if (c == null) {
                return;
            }
            Rectangle vr = c.getBounds();
            c.setBounds(vr.x - (ltr ? 0 : extraWidth), vr.y, vr.width + extraWidth, vr.height);
        }
    }

    private class Handler
    implements ContainerListener,
    FocusListener {
        private Handler() {
        }

        @Override
        public void componentAdded(ContainerEvent e) {
            e.getChild().addFocusListener(this);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            e.getChild().removeFocusListener(this);
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (FlatScrollPaneUI.this.scrollpane.getBorder() instanceof FlatBorder) {
                FlatScrollPaneUI.this.scrollpane.repaint();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (FlatScrollPaneUI.this.scrollpane.getBorder() instanceof FlatBorder) {
                FlatScrollPaneUI.this.scrollpane.repaint();
            }
        }
    }
}

