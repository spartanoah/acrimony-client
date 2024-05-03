/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicListUI;

public class FlatListUI
extends BasicListUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected Color selectionBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionForeground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveForeground;
    @FlatStylingSupport.Styleable
    protected Insets selectionInsets;
    @FlatStylingSupport.Styleable
    protected int selectionArc;
    @FlatStylingSupport.Styleable
    protected Insets cellMargins;
    @FlatStylingSupport.Styleable
    protected Color cellFocusColor;
    @FlatStylingSupport.Styleable
    protected Boolean showCellFocusIndicator;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatListUI();
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
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.selectionBackground = UIManager.getColor("List.selectionBackground");
        this.selectionForeground = UIManager.getColor("List.selectionForeground");
        this.selectionInactiveBackground = UIManager.getColor("List.selectionInactiveBackground");
        this.selectionInactiveForeground = UIManager.getColor("List.selectionInactiveForeground");
        this.selectionInsets = UIManager.getInsets("List.selectionInsets");
        this.selectionArc = UIManager.getInt("List.selectionArc");
        this.toggleSelectionColors();
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.selectionBackground = null;
        this.selectionForeground = null;
        this.selectionInactiveBackground = null;
        this.selectionInactiveForeground = null;
        this.oldStyleValues = null;
    }

    @Override
    protected FocusListener createFocusListener() {
        return new BasicListUI.FocusHandler(){

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                FlatListUI.this.toggleSelectionColors();
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                EventQueue.invokeLater(() -> FlatListUI.this.toggleSelectionColors());
            }
        };
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        PropertyChangeListener superListener = super.createPropertyChangeListener();
        return e -> {
            superListener.propertyChange(e);
            switch (e.getPropertyName()) {
                case "JComponent.focusOwner": {
                    this.toggleSelectionColors();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    this.installStyle();
                    this.list.revalidate();
                    this.list.repaint();
                }
            }
        };
    }

    @Override
    protected ListSelectionListener createListSelectionListener() {
        ListSelectionListener superListener = super.createListSelectionListener();
        return e -> {
            superListener.valueChanged(e);
            if (this.useUnitedRoundedSelection(true, true) && !this.list.isSelectionEmpty() && this.list.getMaxSelectionIndex() - this.list.getMinSelectionIndex() >= 1) {
                int lastIndex;
                int size = this.list.getModel().getSize();
                int firstIndex = Math.min(Math.max(e.getFirstIndex(), 0), size - 1);
                Rectangle r = this.getCellBounds(this.list, firstIndex, lastIndex = Math.min(Math.max(e.getLastIndex(), 0), size - 1));
                if (r != null) {
                    int arc = (int)Math.ceil(UIScale.scale((float)this.selectionArc / 2.0f));
                    this.list.repaint(r.x - arc, r.y - arc, r.width + arc * 2, r.height + arc * 2);
                }
            }
        };
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.list, "List"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        Color oldSelectionBackground = this.selectionBackground;
        Color oldSelectionForeground = this.selectionForeground;
        Color oldSelectionInactiveBackground = this.selectionInactiveBackground;
        Color oldSelectionInactiveForeground = this.selectionInactiveForeground;
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
        if (this.selectionBackground != oldSelectionBackground) {
            Color selBg = this.list.getSelectionBackground();
            if (selBg == oldSelectionBackground) {
                this.list.setSelectionBackground(this.selectionBackground);
            } else if (selBg == oldSelectionInactiveBackground) {
                this.list.setSelectionBackground(this.selectionInactiveBackground);
            }
        }
        if (this.selectionForeground != oldSelectionForeground) {
            Color selFg = this.list.getSelectionForeground();
            if (selFg == oldSelectionForeground) {
                this.list.setSelectionForeground(this.selectionForeground);
            } else if (selFg == oldSelectionInactiveForeground) {
                this.list.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.list, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    private void toggleSelectionColors() {
        if (this.list == null) {
            return;
        }
        if (FlatUIUtils.isPermanentFocusOwner(this.list)) {
            if (this.list.getSelectionBackground() == this.selectionInactiveBackground) {
                this.list.setSelectionBackground(this.selectionBackground);
            }
            if (this.list.getSelectionForeground() == this.selectionInactiveForeground) {
                this.list.setSelectionForeground(this.selectionForeground);
            }
        } else {
            if (this.list.getSelectionBackground() == this.selectionBackground) {
                this.list.setSelectionBackground(this.selectionInactiveBackground);
            }
            if (this.list.getSelectionForeground() == this.selectionForeground) {
                this.list.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    @Override
    protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer cellRenderer, ListModel dataModel, ListSelectionModel selModel, int leadIndex) {
        int cx;
        int cw;
        boolean isSelected = selModel.isSelectedIndex(row);
        Component rendererComponent = cellRenderer.getListCellRendererComponent(this.list, dataModel.getElementAt(row), row, isSelected, FlatUIUtils.isPermanentFocusOwner(this.list) && row == leadIndex);
        boolean isFileList = Boolean.TRUE.equals(this.list.getClientProperty("List.isFileList"));
        if (isFileList) {
            cw = Math.min(rowBounds.width, rendererComponent.getPreferredSize().width + 4);
            cx = this.list.getComponentOrientation().isLeftToRight() ? rowBounds.x : rowBounds.x + (rowBounds.width - cw);
        } else {
            cx = rowBounds.x;
            cw = rowBounds.width;
        }
        if (isSelected && !isFileList && (rendererComponent instanceof DefaultListCellRenderer || rendererComponent instanceof BasicComboBoxRenderer) && (this.selectionArc > 0 || this.selectionInsets != null && (this.selectionInsets.top != 0 || this.selectionInsets.left != 0 || this.selectionInsets.bottom != 0 || this.selectionInsets.right != 0))) {
            class RoundedSelectionGraphics
            extends Graphics2DProxy {
                private boolean inPaintSelection;
                final /* synthetic */ Rectangle val$rowBounds;
                final /* synthetic */ Component val$rendererComponent;
                final /* synthetic */ int val$row;

                RoundedSelectionGraphics(Graphics delegate) {
                    this.val$rowBounds = rectangle;
                    this.val$rendererComponent = component;
                    this.val$row = n;
                    super((Graphics2D)delegate);
                }

                @Override
                public Graphics create() {
                    return new RoundedSelectionGraphics(super.create());
                }

                @Override
                public Graphics create(int x, int y, int width, int height) {
                    return new RoundedSelectionGraphics(super.create(x, y, width, height));
                }

                @Override
                public void fillRect(int x, int y, int width, int height) {
                    if (!this.inPaintSelection && x == 0 && y == 0 && width == this.val$rowBounds.width && height == this.val$rowBounds.height && this.getColor() == this.val$rendererComponent.getBackground()) {
                        this.inPaintSelection = true;
                        FlatListUI.this.paintCellSelection(this, this.val$row, x, y, width, height);
                        this.inPaintSelection = false;
                    } else {
                        super.fillRect(x, y, width, height);
                    }
                }
            }
            g = new RoundedSelectionGraphics(g);
        }
        this.rendererPane.paintComponent(g, rendererComponent, this.list, cx, rowBounds.y, cw, rowBounds.height, true);
    }

    protected void paintCellSelection(Graphics g, int row, int x, int y, int width, int height) {
        float arcBottomRight;
        float arcBottomLeft = arcBottomRight = UIScale.scale((float)this.selectionArc / 2.0f);
        float arcTopRight = arcBottomRight;
        float arcTopLeft = arcBottomRight;
        if (this.list.getLayoutOrientation() == 0) {
            if (this.useUnitedRoundedSelection(true, false)) {
                if (row > 0 && this.list.isSelectedIndex(row - 1)) {
                    arcTopRight = 0.0f;
                    arcTopLeft = 0.0f;
                }
                if (row < this.list.getModel().getSize() - 1 && this.list.isSelectedIndex(row + 1)) {
                    arcBottomRight = 0.0f;
                    arcBottomLeft = 0.0f;
                }
            }
        } else {
            Rectangle r = null;
            if (this.useUnitedRoundedSelection(true, false)) {
                r = this.getCellBounds(this.list, row, row);
                int topIndex = this.locationToIndex(this.list, new Point(r.x, r.y - 1));
                int bottomIndex = this.locationToIndex(this.list, new Point(r.x, r.y + r.height));
                if (topIndex >= 0 && topIndex != row && this.list.isSelectedIndex(topIndex)) {
                    arcTopRight = 0.0f;
                    arcTopLeft = 0.0f;
                }
                if (bottomIndex >= 0 && bottomIndex != row && this.list.isSelectedIndex(bottomIndex)) {
                    arcBottomRight = 0.0f;
                    arcBottomLeft = 0.0f;
                }
            }
            if (this.useUnitedRoundedSelection(false, true)) {
                if (r == null) {
                    r = this.getCellBounds(this.list, row, row);
                }
                int leftIndex = this.locationToIndex(this.list, new Point(r.x - 1, r.y));
                int rightIndex = this.locationToIndex(this.list, new Point(r.x + r.width, r.y));
                boolean ltr = this.list.getComponentOrientation().isLeftToRight();
                if (!ltr && leftIndex >= 0 && leftIndex != row && leftIndex == this.locationToIndex(this.list, new Point(r.x - 1, r.y - 1))) {
                    leftIndex = -1;
                }
                if (ltr && rightIndex >= 0 && rightIndex != row && rightIndex == this.locationToIndex(this.list, new Point(r.x + r.width, r.y - 1))) {
                    rightIndex = -1;
                }
                if (leftIndex >= 0 && leftIndex != row && this.list.isSelectedIndex(leftIndex)) {
                    arcBottomLeft = 0.0f;
                    arcTopLeft = 0.0f;
                }
                if (rightIndex >= 0 && rightIndex != row && this.list.isSelectedIndex(rightIndex)) {
                    arcBottomRight = 0.0f;
                    arcTopRight = 0.0f;
                }
            }
        }
        FlatUIUtils.paintSelection((Graphics2D)g, x, y, width, height, UIScale.scale(this.selectionInsets), arcTopLeft, arcTopRight, arcBottomLeft, arcBottomRight, 0);
    }

    private boolean useUnitedRoundedSelection(boolean vertical, boolean horizontal) {
        return this.selectionArc > 0 && (this.selectionInsets == null || vertical && this.selectionInsets.top == 0 && this.selectionInsets.bottom == 0 || horizontal && this.selectionInsets.left == 0 && this.selectionInsets.right == 0);
    }

    public static void paintCellSelection(JList<?> list, Graphics g, int row, int x, int y, int width, int height) {
        if (!(list.getUI() instanceof FlatListUI)) {
            return;
        }
        FlatListUI ui = (FlatListUI)list.getUI();
        ui.paintCellSelection(g, row, x, y, width, height);
    }
}

