/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatListUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.function.Function;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ListUI;

public class FlatListCellBorder
extends FlatLineBorder {
    protected boolean showCellFocusIndicator = UIManager.getBoolean("List.showCellFocusIndicator");
    private Component c;

    protected FlatListCellBorder() {
        super(UIManager.getInsets("List.cellMargins"), UIManager.getColor("List.cellFocusColor"));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets m = FlatListCellBorder.getStyleFromListUI(c, ui -> ui.cellMargins);
        if (m != null) {
            return FlatListCellBorder.scaleInsets(c, insets, m.top, m.left, m.bottom, m.right);
        }
        return super.getBorderInsets(c, insets);
    }

    @Override
    public Color getLineColor() {
        Color color;
        if (this.c != null && (color = FlatListCellBorder.getStyleFromListUI(this.c, ui -> ui.cellFocusColor)) != null) {
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

    static <T> T getStyleFromListUI(Component c, Function<FlatListUI, T> f) {
        ListUI ui;
        JList list = (JList)SwingUtilities.getAncestorOfClass(JList.class, c);
        if (list != null && (ui = list.getUI()) instanceof FlatListUI) {
            return f.apply((FlatListUI)ui);
        }
        return null;
    }

    public static class Selected
    extends FlatListCellBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            boolean showCellFocusIndicator;
            Boolean b = Selected.getStyleFromListUI(c, ui -> ui.showCellFocusIndicator);
            boolean bl = showCellFocusIndicator = b != null ? b : this.showCellFocusIndicator;
            if (!showCellFocusIndicator) {
                return;
            }
            JList list = (JList)SwingUtilities.getAncestorOfClass(JList.class, c);
            if (list != null && list.getMinSelectionIndex() == list.getMaxSelectionIndex()) {
                return;
            }
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    public static class Focused
    extends FlatListCellBorder {
    }

    public static class Default
    extends FlatListCellBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        }
    }
}

