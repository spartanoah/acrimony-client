/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatTableHeaderUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatViewportUI;
import com.formdev.flatlaf.ui.StackUtils;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class FlatTableUI
extends BasicTableUI
implements FlatStylingSupport.StyleableUI,
FlatViewportUI.ViewportPainter {
    protected boolean showHorizontalLines;
    protected boolean showVerticalLines;
    @FlatStylingSupport.Styleable
    protected boolean showTrailingVerticalLine;
    protected Dimension intercellSpacing;
    @FlatStylingSupport.Styleable
    protected Color selectionBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionForeground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveBackground;
    @FlatStylingSupport.Styleable
    protected Color selectionInactiveForeground;
    @FlatStylingSupport.Styleable
    protected Insets cellMargins;
    @FlatStylingSupport.Styleable
    protected Color cellFocusColor;
    @FlatStylingSupport.Styleable
    protected Boolean showCellFocusIndicator;
    private boolean oldShowHorizontalLines;
    private boolean oldShowVerticalLines;
    private Dimension oldIntercellSpacing;
    private TableCellRenderer oldBooleanRenderer;
    private PropertyChangeListener propertyChangeListener;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return new FlatTableUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.installStyle();
    }

    @Override
    protected void installDefaults() {
        FlatTablePropertyWatcher watcher;
        super.installDefaults();
        this.showHorizontalLines = UIManager.getBoolean("Table.showHorizontalLines");
        this.showVerticalLines = UIManager.getBoolean("Table.showVerticalLines");
        this.showTrailingVerticalLine = UIManager.getBoolean("Table.showTrailingVerticalLine");
        this.intercellSpacing = UIManager.getDimension("Table.intercellSpacing");
        this.selectionBackground = UIManager.getColor("Table.selectionBackground");
        this.selectionForeground = UIManager.getColor("Table.selectionForeground");
        this.selectionInactiveBackground = UIManager.getColor("Table.selectionInactiveBackground");
        this.selectionInactiveForeground = UIManager.getColor("Table.selectionInactiveForeground");
        this.toggleSelectionColors();
        int rowHeight = FlatUIUtils.getUIInt("Table.rowHeight", 16);
        if (rowHeight > 0) {
            LookAndFeel.installProperty(this.table, "rowHeight", UIScale.scale(rowHeight));
        }
        if ((watcher = FlatTablePropertyWatcher.get(this.table)) != null) {
            watcher.enabled = false;
        }
        if (!(this.showHorizontalLines || watcher != null && watcher.showHorizontalLinesChanged)) {
            this.oldShowHorizontalLines = this.table.getShowHorizontalLines();
            this.table.setShowHorizontalLines(false);
        }
        if (!(this.showVerticalLines || watcher != null && watcher.showVerticalLinesChanged)) {
            this.oldShowVerticalLines = this.table.getShowVerticalLines();
            this.table.setShowVerticalLines(false);
        }
        if (!(this.intercellSpacing == null || watcher != null && watcher.intercellSpacingChanged)) {
            this.oldIntercellSpacing = this.table.getIntercellSpacing();
            this.table.setIntercellSpacing(this.intercellSpacing);
        }
        if (watcher != null) {
            watcher.enabled = true;
        } else {
            this.table.addPropertyChangeListener(new FlatTablePropertyWatcher());
        }
        this.oldBooleanRenderer = this.table.getDefaultRenderer(Boolean.class);
        if (this.oldBooleanRenderer instanceof UIResource) {
            this.table.setDefaultRenderer(Boolean.class, new FlatBooleanRenderer());
        } else {
            this.oldBooleanRenderer = null;
        }
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.selectionBackground = null;
        this.selectionForeground = null;
        this.selectionInactiveBackground = null;
        this.selectionInactiveForeground = null;
        this.oldStyleValues = null;
        FlatTablePropertyWatcher watcher = FlatTablePropertyWatcher.get(this.table);
        if (watcher != null) {
            watcher.enabled = false;
        }
        if (!(this.showHorizontalLines || !this.oldShowHorizontalLines || this.table.getShowHorizontalLines() || watcher != null && watcher.showHorizontalLinesChanged)) {
            this.table.setShowHorizontalLines(true);
        }
        if (!(this.showVerticalLines || !this.oldShowVerticalLines || this.table.getShowVerticalLines() || watcher != null && watcher.showVerticalLinesChanged)) {
            this.table.setShowVerticalLines(true);
        }
        if (this.intercellSpacing != null && this.table.getIntercellSpacing().equals(this.intercellSpacing) && (watcher == null || !watcher.intercellSpacingChanged)) {
            this.table.setIntercellSpacing(this.oldIntercellSpacing);
        }
        if (watcher != null) {
            watcher.enabled = true;
        }
        if (this.table.getDefaultRenderer(Boolean.class) instanceof FlatBooleanRenderer) {
            if (this.oldBooleanRenderer instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component)((Object)this.oldBooleanRenderer));
            }
            this.table.setDefaultRenderer(Boolean.class, this.oldBooleanRenderer);
        }
        this.oldBooleanRenderer = null;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.propertyChangeListener = e -> {
            switch (e.getPropertyName()) {
                case "JComponent.focusOwner": {
                    this.toggleSelectionColors();
                    break;
                }
                case "FlatLaf.style": 
                case "FlatLaf.styleClass": {
                    this.installStyle();
                    this.table.revalidate();
                    this.table.repaint();
                }
            }
        };
        this.table.addPropertyChangeListener(this.propertyChangeListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.table.removePropertyChangeListener(this.propertyChangeListener);
        this.propertyChangeListener = null;
    }

    @Override
    protected FocusListener createFocusListener() {
        return new BasicTableUI.FocusHandler(){

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                FlatTableUI.this.toggleSelectionColors();
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                EventQueue.invokeLater(() -> FlatTableUI.this.toggleSelectionColors());
            }
        };
    }

    protected void installStyle() {
        try {
            this.applyStyle(FlatStylingSupport.getResolvedStyle(this.table, "Table"));
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
            Color selBg = this.table.getSelectionBackground();
            if (selBg == oldSelectionBackground) {
                this.table.setSelectionBackground(this.selectionBackground);
            } else if (selBg == oldSelectionInactiveBackground) {
                this.table.setSelectionBackground(this.selectionInactiveBackground);
            }
        }
        if (this.selectionForeground != oldSelectionForeground) {
            Color selFg = this.table.getSelectionForeground();
            if (selFg == oldSelectionForeground) {
                this.table.setSelectionForeground(this.selectionForeground);
            } else if (selFg == oldSelectionInactiveForeground) {
                this.table.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    protected Object applyStyleProperty(String key, Object value) {
        if ("rowHeight".equals(key) && value instanceof Integer) {
            value = UIScale.scale((Integer)value);
        }
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, this.table, key, value);
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
        if (this.table == null) {
            return;
        }
        if (FlatUIUtils.isPermanentFocusOwner(this.table)) {
            if (this.table.getSelectionBackground() == this.selectionInactiveBackground) {
                this.table.setSelectionBackground(this.selectionBackground);
            }
            if (this.table.getSelectionForeground() == this.selectionInactiveForeground) {
                this.table.setSelectionForeground(this.selectionForeground);
            }
        } else {
            if (this.table.getSelectionBackground() == this.selectionBackground) {
                this.table.setSelectionBackground(this.selectionInactiveBackground);
            }
            if (this.table.getSelectionForeground() == this.selectionForeground) {
                this.table.setSelectionForeground(this.selectionInactiveForeground);
            }
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        FlatTableHeaderUI.fixDraggedAndResizingColumns(this.table.getTableHeader());
        final boolean horizontalLines = this.table.getShowHorizontalLines();
        final boolean verticalLines = this.table.getShowVerticalLines();
        if (horizontalLines || verticalLines) {
            final boolean hideLastVerticalLine = this.hideLastVerticalLine();
            final int tableWidth = this.table.getWidth();
            JTableHeader header = this.table.getTableHeader();
            final boolean isDragging = header != null && header.getDraggedColumn() != null;
            double systemScaleFactor = UIScale.getSystemScaleFactor((Graphics2D)g);
            final double lineThickness = 1.0 / systemScaleFactor * (double)((int)systemScaleFactor);
            g = new Graphics2DProxy((Graphics2D)g){

                @Override
                public void drawLine(int x1, int y1, int x2, int y2) {
                    if (hideLastVerticalLine && verticalLines && x1 == x2 && y1 == 0 && x1 == tableWidth - 1 && this.wasInvokedFromPaintGrid()) {
                        return;
                    }
                    if (isDragging && SystemInfo.isJava_9_orLater && (horizontalLines && y1 == y2 || verticalLines && x1 == x2) && this.wasInvokedFromMethod("paintDraggedArea")) {
                        if (y1 == y2) {
                            super.fill(new Rectangle2D.Double(x1, y1, x2 - x1 + 1, lineThickness));
                        } else if (x1 == x2) {
                            super.fill(new Rectangle2D.Double(x1, y1, lineThickness, y2 - y1 + 1));
                        }
                        return;
                    }
                    super.drawLine(x1, y1, x2, y2);
                }

                @Override
                public void fillRect(int x, int y, int width, int height) {
                    if (hideLastVerticalLine && verticalLines && width == 1 && y == 0 && x == tableWidth - 1 && this.wasInvokedFromPaintGrid()) {
                        return;
                    }
                    if (lineThickness != 1.0) {
                        if (horizontalLines && height == 1 && this.wasInvokedFromPaintGrid()) {
                            super.fill(new Rectangle2D.Double(x, y, width, lineThickness));
                            return;
                        }
                        if (verticalLines && width == 1 && y == 0 && this.wasInvokedFromPaintGrid()) {
                            super.fill(new Rectangle2D.Double(x, y, lineThickness, height));
                            return;
                        }
                    }
                    super.fillRect(x, y, width, height);
                }

                private boolean wasInvokedFromPaintGrid() {
                    return this.wasInvokedFromMethod("paintGrid");
                }

                private boolean wasInvokedFromMethod(String methodName) {
                    return StackUtils.wasInvokedFrom(BasicTableUI.class.getName(), methodName, 8);
                }
            };
        }
        super.paint(g, c);
    }

    protected boolean hideLastVerticalLine() {
        Container viewportParent;
        if (this.showTrailingVerticalLine) {
            return false;
        }
        Container viewport = SwingUtilities.getUnwrappedParent(this.table);
        Container container = viewportParent = viewport != null ? viewport.getParent() : null;
        if (!(viewportParent instanceof JScrollPane)) {
            return false;
        }
        if (this.table.getX() + this.table.getWidth() < viewport.getWidth()) {
            return false;
        }
        JScrollPane scrollPane = (JScrollPane)viewportParent;
        JViewport rowHeader = scrollPane.getRowHeader();
        return scrollPane.getComponentOrientation().isLeftToRight() ? viewport != rowHeader : viewport == rowHeader || rowHeader == null;
    }

    @Override
    public void paintViewport(Graphics g, JComponent c, JViewport viewport) {
        Color alternateColor;
        boolean paintOutside;
        int viewportWidth = viewport.getWidth();
        int viewportHeight = viewport.getHeight();
        if (viewport.isOpaque()) {
            g.setColor(this.table.getBackground());
            g.fillRect(0, 0, viewportWidth, viewportHeight);
        }
        if ((paintOutside = UIManager.getBoolean("Table.paintOutsideAlternateRows")) && (alternateColor = UIManager.getColor("Table.alternateRowColor")) != null) {
            g.setColor(alternateColor);
            int rowCount = this.table.getRowCount();
            int tableHeight = this.table.getHeight();
            if (tableHeight < viewportHeight) {
                int tableWidth = this.table.getWidth();
                int rowHeight = this.table.getRowHeight();
                int y = tableHeight;
                int row = rowCount;
                while (y < viewportHeight) {
                    if (row % 2 != 0) {
                        g.fillRect(0, y, tableWidth, rowHeight);
                    }
                    y += rowHeight;
                    ++row;
                }
            }
        }
    }

    private static class FlatBooleanRenderer
    extends DefaultTableCellRenderer
    implements UIResource {
        private boolean selected;

        FlatBooleanRenderer() {
            this.setHorizontalAlignment(0);
            this.setIcon(new FlatCheckBoxIcon(){

                @Override
                protected boolean isSelected(Component c) {
                    return selected;
                }
            });
        }

        @Override
        protected void setValue(Object value) {
            this.selected = value != null && (Boolean)value != false;
        }
    }

    private static class FlatTablePropertyWatcher
    implements PropertyChangeListener {
        boolean enabled = true;
        boolean showHorizontalLinesChanged;
        boolean showVerticalLinesChanged;
        boolean intercellSpacingChanged;

        private FlatTablePropertyWatcher() {
        }

        static FlatTablePropertyWatcher get(JTable table) {
            for (PropertyChangeListener l : table.getPropertyChangeListeners()) {
                if (!(l instanceof FlatTablePropertyWatcher)) continue;
                return (FlatTablePropertyWatcher)l;
            }
            return null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (!this.enabled) {
                return;
            }
            switch (e.getPropertyName()) {
                case "showHorizontalLines": {
                    this.showHorizontalLinesChanged = true;
                    break;
                }
                case "showVerticalLines": {
                    this.showVerticalLinesChanged = true;
                    break;
                }
                case "rowMargin": {
                    this.intercellSpacingChanged = true;
                }
            }
        }
    }
}

