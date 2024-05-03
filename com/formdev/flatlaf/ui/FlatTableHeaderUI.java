/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FlatTableHeaderUI
extends BasicTableHeaderUI
implements FlatStylingSupport.StyleableUI {
    @FlatStylingSupport.Styleable
    protected Color hoverBackground;
    @FlatStylingSupport.Styleable
    protected Color hoverForeground;
    @FlatStylingSupport.Styleable
    protected Color pressedBackground;
    @FlatStylingSupport.Styleable
    protected Color pressedForeground;
    @FlatStylingSupport.Styleable
    protected Color bottomSeparatorColor;
    @FlatStylingSupport.Styleable
    protected int height;
    @FlatStylingSupport.Styleable(type=String.class)
    protected int sortIconPosition;
    @FlatStylingSupport.Styleable
    protected Insets cellMargins;
    @FlatStylingSupport.Styleable
    protected Color separatorColor;
    @FlatStylingSupport.Styleable
    protected Boolean showTrailingVerticalLine;
    @FlatStylingSupport.Styleable
    public String arrowType;
    @FlatStylingSupport.Styleable
    public Color sortIconColor;
    private PropertyChangeListener propertyChangeListener;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTableHeaderUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.header.remove(this.rendererPane);
        this.rendererPane = new FlatTableHeaderCellRendererPane();
        this.header.add(this.rendererPane);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        this.hoverBackground = UIManager.getColor("TableHeader.hoverBackground");
        this.hoverForeground = UIManager.getColor("TableHeader.hoverForeground");
        this.pressedBackground = UIManager.getColor("TableHeader.pressedBackground");
        this.pressedForeground = UIManager.getColor("TableHeader.pressedForeground");
        this.bottomSeparatorColor = UIManager.getColor("TableHeader.bottomSeparatorColor");
        this.height = UIManager.getInt("TableHeader.height");
        this.sortIconPosition = FlatTableHeaderUI.parseSortIconPosition(UIManager.getString("TableHeader.sortIconPosition"));
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.hoverBackground = null;
        this.hoverForeground = null;
        this.pressedBackground = null;
        this.pressedForeground = null;
        this.bottomSeparatorColor = null;
        this.oldStyleValues = null;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.propertyChangeListener = FlatStylingSupport.createPropertyChangeListener(this.header, this::installStyle, null);
        this.header.addPropertyChangeListener(this.propertyChangeListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.header.removePropertyChangeListener(this.propertyChangeListener);
        this.propertyChangeListener = null;
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.header, "TableHeader"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, this::applyStyleProperty);
    }

    protected Object applyStyleProperty(String key, Object value) {
        if (key.equals("sortIconPosition") && value instanceof String) {
            value = FlatTableHeaderUI.parseSortIconPosition((String)value);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.header, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        if (key.equals("sortIconPosition")) {
            switch (this.sortIconPosition) {
                default: {
                    return "right";
                }
                case 2: {
                    return "left";
                }
                case 1: {
                    return "top";
                }
                case 3: 
            }
            return "bottom";
        }
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    private static int parseSortIconPosition(String str) {
        if (str == null) {
            str = "";
        }
        switch (str) {
            default: {
                return 4;
            }
            case "left": {
                return 2;
            }
            case "top": {
                return 1;
            }
            case "bottom": 
        }
        return 3;
    }

    @Override
    protected MouseInputListener createMouseInputListener() {
        return new FlatMouseInputHandler();
    }

    @Override
    public int getRolloverColumn() {
        return super.getRolloverColumn();
    }

    @Override
    protected void rolloverColumnUpdated(int oldColumn, int newColumn) {
        this.header.repaint(this.header.getHeaderRect(oldColumn));
        this.header.repaint(this.header.getHeaderRect(newColumn));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        FlatTableHeaderUI.fixDraggedAndResizingColumns(this.header);
        TableColumnModel columnModel = this.header.getColumnModel();
        if (columnModel.getColumnCount() <= 0) {
            return;
        }
        int columnCount = columnModel.getColumnCount();
        int totalWidth = 0;
        for (int i = 0; i < columnCount; ++i) {
            totalWidth += columnModel.getColumn(i).getWidth();
        }
        if (totalWidth < this.header.getWidth()) {
            TableCellRenderer defaultRenderer = this.header.getDefaultRenderer();
            boolean paintBottomSeparator = this.isSystemDefaultRenderer(defaultRenderer);
            if (!paintBottomSeparator && this.header.getTable() != null) {
                Component rendererComponent = defaultRenderer.getTableCellRendererComponent(this.header.getTable(), "", false, false, -1, 0);
                paintBottomSeparator = this.isSystemDefaultRenderer(rendererComponent);
            }
            if (paintBottomSeparator) {
                int w = c.getWidth() - totalWidth;
                int x = this.header.getComponentOrientation().isLeftToRight() ? c.getWidth() - w : 0;
                this.paintBottomSeparator(g, c, x, w);
            }
        }
        super.paint(g, c);
    }

    private boolean isSystemDefaultRenderer(Object headerRenderer) {
        String rendererClassName = headerRenderer.getClass().getName();
        return rendererClassName.equals("sun.swing.table.DefaultTableCellHeaderRenderer") || rendererClassName.equals("sun.swing.FilePane$AlignableTableHeaderRenderer");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void paintBottomSeparator(Graphics g, JComponent c, int x, int w) {
        float lineWidth = UIScale.scale(1.0f);
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setColor(this.bottomSeparatorColor);
            g2.fill(new Rectangle2D.Float(x, (float)c.getHeight() - lineWidth, w, lineWidth));
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        if (size.height > 0) {
            size.height = Math.max(size.height, UIScale.scale(this.height));
        }
        return size;
    }

    static void fixDraggedAndResizingColumns(JTableHeader header) {
        TableColumn resizingColumn;
        if (header == null) {
            return;
        }
        TableColumn draggedColumn = header.getDraggedColumn();
        if (draggedColumn != null && !FlatTableHeaderUI.isValidColumn(header.getColumnModel(), draggedColumn)) {
            header.setDraggedColumn(null);
        }
        if ((resizingColumn = header.getResizingColumn()) != null && !FlatTableHeaderUI.isValidColumn(header.getColumnModel(), resizingColumn)) {
            header.setResizingColumn(null);
        }
    }

    private static boolean isValidColumn(TableColumnModel cm, TableColumn column) {
        int count = cm.getColumnCount();
        for (int i = 0; i < count; ++i) {
            if (cm.getColumn(i) != column) continue;
            return true;
        }
        return false;
    }

    protected class FlatMouseInputHandler
    extends BasicTableHeaderUI.MouseInputHandler {
        Cursor oldCursor;

        protected FlatMouseInputHandler() {
            super(FlatTableHeaderUI.this);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int column;
            JTable table;
            if (this.oldCursor != null) {
                FlatTableHeaderUI.this.header.setCursor(this.oldCursor);
                this.oldCursor = null;
            }
            super.mouseMoved(e);
            if (FlatTableHeaderUI.this.header.isEnabled() && (table = FlatTableHeaderUI.this.header.getTable()) != null && table.getAutoResizeMode() != 0 && FlatTableHeaderUI.this.header.getCursor() == Cursor.getPredefinedCursor(11) && (column = FlatTableHeaderUI.this.header.columnAtPoint(e.getPoint())) >= 0 && column == FlatTableHeaderUI.this.header.getColumnModel().getColumnCount() - 1) {
                Rectangle r = FlatTableHeaderUI.this.header.getHeaderRect(column);
                r.grow(-3, 0);
                if (!r.contains(e.getX(), e.getY())) {
                    boolean isResizeLastColumn;
                    boolean bl = isResizeLastColumn = e.getX() >= r.x + r.width / 2;
                    if (!FlatTableHeaderUI.this.header.getComponentOrientation().isLeftToRight()) {
                        boolean bl2 = isResizeLastColumn = !isResizeLastColumn;
                    }
                    if (isResizeLastColumn) {
                        this.oldCursor = FlatTableHeaderUI.this.header.getCursor();
                        FlatTableHeaderUI.this.header.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }
    }

    private class FlatTableHeaderCellRendererPane
    extends CellRendererPane {
        private final Icon ascendingSortIcon = UIManager.getIcon("Table.ascendingSortIcon");
        private final Icon descendingSortIcon = UIManager.getIcon("Table.descendingSortIcon");

        @Override
        public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h, boolean shouldValidate) {
            Icon icon;
            boolean isSortIcon;
            if (!(c instanceof JLabel)) {
                super.paintComponent(g, c, p, x, y, w, h, shouldValidate);
                return;
            }
            JLabel l = (JLabel)c;
            Color oldBackground = null;
            Color oldForeground = null;
            boolean oldOpaque = false;
            Icon oldIcon = null;
            int oldHorizontalTextPosition = -1;
            TableColumn draggedColumn = FlatTableHeaderUI.this.header.getDraggedColumn();
            Color background = null;
            Color foreground = null;
            if (draggedColumn != null && FlatTableHeaderUI.this.header.getTable().convertColumnIndexToView(draggedColumn.getModelIndex()) == this.getColumn(x - FlatTableHeaderUI.this.header.getDraggedDistance(), w)) {
                background = FlatTableHeaderUI.this.pressedBackground;
                foreground = FlatTableHeaderUI.this.pressedForeground;
            } else if (FlatTableHeaderUI.this.getRolloverColumn() >= 0 && FlatTableHeaderUI.this.getRolloverColumn() == this.getColumn(x, w)) {
                background = FlatTableHeaderUI.this.hoverBackground;
                foreground = FlatTableHeaderUI.this.hoverForeground;
            }
            if (background != null) {
                oldBackground = l.getBackground();
                oldOpaque = l.isOpaque();
                l.setBackground(FlatUIUtils.deriveColor(background, FlatTableHeaderUI.this.header.getBackground()));
                l.setOpaque(true);
            }
            if (foreground != null) {
                oldForeground = l.getForeground();
                l.setForeground(FlatUIUtils.deriveColor(foreground, FlatTableHeaderUI.this.header.getForeground()));
            }
            boolean bl = isSortIcon = (icon = l.getIcon()) != null && (icon == this.ascendingSortIcon || icon == this.descendingSortIcon);
            if (isSortIcon) {
                if (FlatTableHeaderUI.this.sortIconPosition == 2) {
                    oldHorizontalTextPosition = l.getHorizontalTextPosition();
                    l.setHorizontalTextPosition(4);
                } else if (FlatTableHeaderUI.this.sortIconPosition == 1 || FlatTableHeaderUI.this.sortIconPosition == 3) {
                    oldIcon = icon;
                    l.setIcon(null);
                }
            }
            super.paintComponent(g, c, p, x, y, w, h, shouldValidate);
            if (isSortIcon && (FlatTableHeaderUI.this.sortIconPosition == 1 || FlatTableHeaderUI.this.sortIconPosition == 3)) {
                int xi = x + (w - icon.getIconWidth()) / 2;
                int yi = FlatTableHeaderUI.this.sortIconPosition == 1 ? y + UIScale.scale(1) : y + FlatTableHeaderUI.this.height - icon.getIconHeight() - 1 - (int)(1.0f * UIScale.getUserScaleFactor());
                icon.paintIcon(c, g, xi, yi);
            }
            if (background != null) {
                l.setBackground(oldBackground);
                l.setOpaque(oldOpaque);
            }
            if (foreground != null) {
                l.setForeground(oldForeground);
            }
            if (oldIcon != null) {
                l.setIcon(oldIcon);
            }
            if (oldHorizontalTextPosition >= 0) {
                l.setHorizontalTextPosition(oldHorizontalTextPosition);
            }
        }

        private int getColumn(int x, int width) {
            TableColumnModel columnModel = FlatTableHeaderUI.this.header.getColumnModel();
            int columnCount = columnModel.getColumnCount();
            boolean ltr = FlatTableHeaderUI.this.header.getComponentOrientation().isLeftToRight();
            int cx = ltr ? 0 : this.getWidthInRightToLef();
            for (int i = 0; i < columnCount; ++i) {
                int cw = columnModel.getColumn(i).getWidth();
                if (x == cx - (ltr ? 0 : cw) && width == cw) {
                    return i;
                }
                cx += ltr ? cw : -cw;
            }
            return -1;
        }

        private int getWidthInRightToLef() {
            JTable table = FlatTableHeaderUI.this.header.getTable();
            return table != null && table.getAutoResizeMode() != 0 ? table.getWidth() : FlatTableHeaderUI.this.header.getWidth();
        }
    }
}

