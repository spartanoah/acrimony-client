/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatArrowButton;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class FlatSplitPaneUI
extends BasicSplitPaneUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected String arrowType;
    @FlatStylingSupport.Styleable
    protected Color draggingColor;
    @FlatStylingSupport.Styleable
    protected Color oneTouchArrowColor;
    @FlatStylingSupport.Styleable
    protected Color oneTouchHoverArrowColor;
    @FlatStylingSupport.Styleable
    protected Color oneTouchPressedArrowColor;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatSplitPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        this.arrowType = UIManager.getString("Component.arrowType");
        this.draggingColor = UIManager.getColor("SplitPaneDivider.draggingColor");
        this.oneTouchArrowColor = UIManager.getColor("SplitPaneDivider.oneTouchArrowColor");
        this.oneTouchHoverArrowColor = UIManager.getColor("SplitPaneDivider.oneTouchHoverArrowColor");
        this.oneTouchPressedArrowColor = UIManager.getColor("SplitPaneDivider.oneTouchPressedArrowColor");
        super.installDefaults();
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.draggingColor = null;
        this.oneTouchArrowColor = null;
        this.oneTouchHoverArrowColor = null;
        this.oneTouchPressedArrowColor = null;
        this.oldStyleValues = null;
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return FlatStylingSupport.createPropertyChangeListener(this.splitPane, this::installStyle, super.createPropertyChangeListener());
    }

    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new FlatSplitPaneDivider(this);
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.splitPane, "SplitPane"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        if (this.divider instanceof FlatSplitPaneDivider) {
            ((FlatSplitPaneDivider)this.divider).updateStyle();
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        try {
            if (this.divider instanceof FlatSplitPaneDivider) {
                return ((FlatSplitPaneDivider)this.divider).applyStyleProperty(key, value);
            }
        } catch (FlatStylingSupport.UnknownStyleException unknownStyleException) {
            // empty catch block
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.splitPane, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        Map<String, Class<?>> infos = FlatStylingSupport.getAnnotatedStyleableInfos(this);
        if (this.divider instanceof FlatSplitPaneDivider) {
            infos.putAll(((FlatSplitPaneDivider)this.divider).getStyleableInfos());
        }
        return infos;
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        Object value;
        if (this.divider instanceof FlatSplitPaneDivider && (value = ((FlatSplitPaneDivider)this.divider).getStyleableValue(key)) != null) {
            return value;
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    protected Component createDefaultNonContinuousLayoutDivider() {
        return new Canvas(){

            @Override
            public void paint(Graphics g) {
                if (!FlatSplitPaneUI.this.isContinuousLayout() && FlatSplitPaneUI.this.getLastDragLocation() != -1) {
                    FlatSplitPaneUI.this.paintDragDivider(g, 0);
                }
            }
        };
    }

    @Override
    public void finishedPaintingChildren(JSplitPane sp, Graphics g) {
        if (sp == this.splitPane && this.getLastDragLocation() != -1 && !this.isContinuousLayout() && !this.draggingHW) {
            this.paintDragDivider(g, this.getLastDragLocation());
        }
    }

    private void paintDragDivider(Graphics g, int dividerLocation) {
        boolean horizontal = this.getOrientation() == 1;
        int x = horizontal ? dividerLocation : 0;
        int y = !horizontal ? dividerLocation : 0;
        int width = horizontal ? this.dividerSize : this.splitPane.getWidth();
        int height = !horizontal ? this.dividerSize : this.splitPane.getHeight();
        g.setColor(FlatUIUtils.deriveColor(this.draggingColor, this.splitPane.getBackground()));
        g.fillRect(x, y, width, height);
        if (this.divider instanceof FlatSplitPaneDivider) {
            ((FlatSplitPaneDivider)this.divider).paintStyle(g, x, y, width, height);
        }
    }

    protected class FlatSplitPaneDivider
    extends BasicSplitPaneDivider {
        @FlatStylingSupport.Styleable
        protected String style;
        @FlatStylingSupport.Styleable
        protected Color hoverColor;
        @FlatStylingSupport.Styleable
        protected Color pressedColor;
        @FlatStylingSupport.Styleable
        protected Color gripColor;
        @FlatStylingSupport.Styleable
        protected int gripDotCount;
        @FlatStylingSupport.Styleable
        protected int gripDotSize;
        @FlatStylingSupport.Styleable
        protected int gripGap;

        protected FlatSplitPaneDivider(BasicSplitPaneUI ui) {
            super(ui);
            this.style = UIManager.getString("SplitPaneDivider.style");
            this.hoverColor = UIManager.getColor("SplitPaneDivider.hoverColor");
            this.pressedColor = UIManager.getColor("SplitPaneDivider.pressedColor");
            this.gripColor = UIManager.getColor("SplitPaneDivider.gripColor");
            this.gripDotCount = FlatUIUtils.getUIInt("SplitPaneDivider.gripDotCount", 3);
            this.gripDotSize = FlatUIUtils.getUIInt("SplitPaneDivider.gripDotSize", 3);
            this.gripGap = FlatUIUtils.getUIInt("SplitPaneDivider.gripGap", 2);
            this.setLayout(new FlatDividerLayout());
        }

        protected Object applyStyleProperty(String key, Object value) {
            return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
        }

        public Map<String, Class<?>> getStyleableInfos() {
            return FlatStylingSupport.getAnnotatedStyleableInfos(this);
        }

        public Object getStyleableValue(String key) {
            return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
        }

        void updateStyle() {
            if (this.leftButton instanceof FlatOneTouchButton) {
                ((FlatOneTouchButton)this.leftButton).updateStyle();
            }
            if (this.rightButton instanceof FlatOneTouchButton) {
                ((FlatOneTouchButton)this.rightButton).updateStyle();
            }
        }

        @Override
        public void setDividerSize(int newSize) {
            super.setDividerSize(UIScale.scale(newSize));
        }

        @Override
        protected JButton createLeftOneTouchButton() {
            return new FlatOneTouchButton(true);
        }

        @Override
        protected JButton createRightOneTouchButton() {
            return new FlatOneTouchButton(false);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            switch (e.getPropertyName()) {
                case "dividerLocation": {
                    this.doLayout();
                }
            }
        }

        @Override
        public void paint(Graphics g) {
            Color hoverOrPressedColor;
            Color color = FlatSplitPaneUI.this.isContinuousLayout() && this.dragger != null ? this.pressedColor : (hoverOrPressedColor = this.isMouseOver() && this.dragger == null ? this.hoverColor : null);
            if (hoverOrPressedColor != null) {
                g.setColor(FlatUIUtils.deriveColor(hoverOrPressedColor, this.splitPane.getBackground()));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            super.paint(g);
            this.paintStyle(g, 0, 0, this.getWidth(), this.getHeight());
        }

        protected void paintStyle(Graphics g, int x, int y, int width, int height) {
            if ("plain".equals(this.style)) {
                return;
            }
            Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
            g.setColor(this.gripColor);
            this.paintGrip(g, x, y, width, height);
            FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
        }

        protected void paintGrip(Graphics g, int x, int y, int width, int height) {
            FlatUIUtils.paintGrip(g, x, y, width, height, this.splitPane.getOrientation() == 0, this.gripDotCount, this.gripDotSize, this.gripGap, true);
        }

        protected boolean isLeftCollapsed() {
            int location = this.splitPane.getDividerLocation();
            Insets insets = this.splitPane.getInsets();
            return this.orientation == 0 ? location == insets.top : location == insets.left;
        }

        protected boolean isRightCollapsed() {
            int location = this.splitPane.getDividerLocation();
            Insets insets = this.splitPane.getInsets();
            return this.orientation == 0 ? location == this.splitPane.getHeight() - this.getHeight() - insets.bottom : location == this.splitPane.getWidth() - this.getWidth() - insets.right;
        }

        @Override
        protected void setMouseOver(boolean mouseOver) {
            super.setMouseOver(mouseOver);
            this.repaintIfNecessary();
        }

        @Override
        protected void prepareForDragging() {
            super.prepareForDragging();
            this.repaintIfNecessary();
        }

        @Override
        protected void finishDraggingTo(int location) {
            super.finishDraggingTo(location);
            this.repaintIfNecessary();
        }

        private void repaintIfNecessary() {
            if (this.hoverColor != null || this.pressedColor != null) {
                this.repaint();
            }
        }

        protected class FlatDividerLayout
        extends BasicSplitPaneDivider.DividerLayout {
            protected FlatDividerLayout() {
                super(FlatSplitPaneDivider.this);
            }

            @Override
            public void layoutContainer(Container c) {
                super.layoutContainer(c);
                if (FlatSplitPaneDivider.this.leftButton == null || FlatSplitPaneDivider.this.rightButton == null || !FlatSplitPaneDivider.this.splitPane.isOneTouchExpandable()) {
                    return;
                }
                int extraSize = UIScale.scale(4);
                if (FlatSplitPaneDivider.this.orientation == 0) {
                    FlatSplitPaneDivider.this.leftButton.setSize(FlatSplitPaneDivider.this.leftButton.getWidth() + extraSize, FlatSplitPaneDivider.this.leftButton.getHeight());
                    FlatSplitPaneDivider.this.rightButton.setBounds(FlatSplitPaneDivider.this.leftButton.getX() + FlatSplitPaneDivider.this.leftButton.getWidth(), FlatSplitPaneDivider.this.rightButton.getY(), FlatSplitPaneDivider.this.rightButton.getWidth() + extraSize, FlatSplitPaneDivider.this.rightButton.getHeight());
                } else {
                    FlatSplitPaneDivider.this.leftButton.setSize(FlatSplitPaneDivider.this.leftButton.getWidth(), FlatSplitPaneDivider.this.leftButton.getHeight() + extraSize);
                    FlatSplitPaneDivider.this.rightButton.setBounds(FlatSplitPaneDivider.this.rightButton.getX(), FlatSplitPaneDivider.this.leftButton.getY() + FlatSplitPaneDivider.this.leftButton.getHeight(), FlatSplitPaneDivider.this.rightButton.getWidth(), FlatSplitPaneDivider.this.rightButton.getHeight() + extraSize);
                }
                boolean leftCollapsed = FlatSplitPaneDivider.this.isLeftCollapsed();
                boolean rightCollapsed = FlatSplitPaneDivider.this.isRightCollapsed();
                if (leftCollapsed || rightCollapsed) {
                    FlatSplitPaneDivider.this.leftButton.setVisible(!leftCollapsed);
                    FlatSplitPaneDivider.this.rightButton.setVisible(!rightCollapsed);
                } else {
                    Object expandableSide = FlatSplitPaneDivider.this.splitPane.getClientProperty("JSplitPane.expandableSide");
                    FlatSplitPaneDivider.this.leftButton.setVisible(expandableSide == null || !"left".equals(expandableSide));
                    FlatSplitPaneDivider.this.rightButton.setVisible(expandableSide == null || !"right".equals(expandableSide));
                }
                if (!FlatSplitPaneDivider.this.leftButton.isVisible()) {
                    FlatSplitPaneDivider.this.rightButton.setLocation(FlatSplitPaneDivider.this.leftButton.getLocation());
                }
            }
        }

        protected class FlatOneTouchButton
        extends FlatArrowButton {
            protected final boolean left;

            protected FlatOneTouchButton(boolean left) {
                super(1, FlatSplitPaneUI.this.arrowType, FlatSplitPaneUI.this.oneTouchArrowColor, null, FlatSplitPaneUI.this.oneTouchHoverArrowColor, null, FlatSplitPaneUI.this.oneTouchPressedArrowColor, null);
                this.setCursor(Cursor.getPredefinedCursor(0));
                ToolTipManager.sharedInstance().registerComponent(this);
                this.left = left;
            }

            protected void updateStyle() {
                this.updateStyle(FlatSplitPaneUI.this.arrowType, FlatSplitPaneUI.this.oneTouchArrowColor, null, FlatSplitPaneUI.this.oneTouchHoverArrowColor, null, FlatSplitPaneUI.this.oneTouchPressedArrowColor, null);
            }

            @Override
            public int getDirection() {
                return FlatSplitPaneDivider.this.orientation == 0 ? (this.left ? 1 : 5) : (this.left ? 7 : 3);
            }

            @Override
            public String getToolTipText(MouseEvent e) {
                String key = FlatSplitPaneDivider.this.orientation == 0 ? (this.left ? (FlatSplitPaneDivider.this.isRightCollapsed() ? "SplitPaneDivider.expandBottomToolTipText" : "SplitPaneDivider.collapseTopToolTipText") : (FlatSplitPaneDivider.this.isLeftCollapsed() ? "SplitPaneDivider.expandTopToolTipText" : "SplitPaneDivider.collapseBottomToolTipText")) : (this.left ? (FlatSplitPaneDivider.this.isRightCollapsed() ? "SplitPaneDivider.expandRightToolTipText" : "SplitPaneDivider.collapseLeftToolTipText") : (FlatSplitPaneDivider.this.isLeftCollapsed() ? "SplitPaneDivider.expandLeftToolTipText" : "SplitPaneDivider.collapseRightToolTipText"));
                Object value = FlatSplitPaneDivider.this.splitPane.getClientProperty(key);
                if (value instanceof String) {
                    return (String)value;
                }
                return UIManager.getString((Object)key, this.getLocale());
            }
        }
    }
}

