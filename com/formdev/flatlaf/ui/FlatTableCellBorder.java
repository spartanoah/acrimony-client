/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatTableUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.Function;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.TableUI;

public class FlatTableCellBorder
extends FlatLineBorder {
    protected boolean showCellFocusIndicator = UIManager.getBoolean("Table.showCellFocusIndicator");
    private Component c;

    protected FlatTableCellBorder() {
        super(UIManager.getInsets("Table.cellMargins"), UIManager.getColor("Table.cellFocusColor"));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets m = FlatTableCellBorder.getStyleFromTableUI(c, ui -> ui.cellMargins);
        if (m != null) {
            return FlatTableCellBorder.scaleInsets(c, insets, m.top, m.left, m.bottom, m.right);
        }
        return super.getBorderInsets(c, insets);
    }

    @Override
    public Color getLineColor() {
        Color color;
        if (this.c != null && (color = FlatTableCellBorder.getStyleFromTableUI(this.c, ui -> ui.cellFocusColor)) != null) {
            return color;
        }
        return super.getLineColor();
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        this.c = c;
        super.paintBorder(c, g, x, y, width, height);
        this.c = null;
    }

    static <T> T getStyleFromTableUI(Component c, Function<FlatTableUI, T> f) {
        TableUI ui;
        JTable table = (JTable)SwingUtilities.getAncestorOfClass(JTable.class, c);
        if (table != null && (ui = table.getUI()) instanceof FlatTableUI) {
            return f.apply((FlatTableUI)ui);
        }
        return null;
    }

    public static class Selected
    extends FlatTableCellBorder {
        public int maxCheckCellsEditable = 50;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            JTable table;
            boolean showCellFocusIndicator;
            Boolean b = Selected.getStyleFromTableUI(c, ui -> ui.showCellFocusIndicator);
            boolean bl = showCellFocusIndicator = b != null ? b : this.showCellFocusIndicator;
            if (!showCellFocusIndicator && (table = (JTable)SwingUtilities.getAncestorOfClass(JTable.class, c)) != null && !this.shouldShowCellFocusIndicator(table)) {
                return;
            }
            super.paintBorder(c, g, x, y, width, height);
        }

        protected boolean shouldShowCellFocusIndicator(JTable table) {
            block8: {
                boolean columnSelectionAllowed;
                block7: {
                    boolean rowSelectionAllowed = table.getRowSelectionAllowed();
                    columnSelectionAllowed = table.getColumnSelectionAllowed();
                    if (rowSelectionAllowed && columnSelectionAllowed) {
                        return false;
                    }
                    if (!rowSelectionAllowed) break block7;
                    if (table.getSelectedRowCount() != 1) {
                        return false;
                    }
                    int columnCount = table.getColumnCount();
                    if (columnCount > this.maxCheckCellsEditable) {
                        return true;
                    }
                    int selectedRow = table.getSelectedRow();
                    for (int column = 0; column < columnCount; ++column) {
                        if (!table.isCellEditable(selectedRow, column)) continue;
                        return true;
                    }
                    break block8;
                }
                if (!columnSelectionAllowed) break block8;
                if (table.getSelectedColumnCount() != 1) {
                    return false;
                }
                int rowCount = table.getRowCount();
                if (rowCount > this.maxCheckCellsEditable) {
                    return true;
                }
                int selectedColumn = table.getSelectedColumn();
                for (int row = 0; row < rowCount; ++row) {
                    if (!table.isCellEditable(row, selectedColumn)) continue;
                    return true;
                }
            }
            return false;
        }
    }

    public static class Focused
    extends FlatTableCellBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Border border;
            JTable table;
            if (c != null && c.getClass().getName().equals("javax.swing.JTable$BooleanRenderer") && (table = (JTable)SwingUtilities.getAncestorOfClass(JTable.class, c)) != null && c.getForeground() == table.getSelectionForeground() && c.getBackground() == table.getSelectionBackground() && (border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder")) != null) {
                border.paintBorder(c, g, x, y, width, height);
                return;
            }
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    public static class Default
    extends FlatTableCellBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        }
    }
}

