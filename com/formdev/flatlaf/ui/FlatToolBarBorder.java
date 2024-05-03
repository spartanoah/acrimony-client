/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatMarginBorder;
import com.formdev.flatlaf.ui.FlatToolBarUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.function.Function;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.ToolBarUI;

public class FlatToolBarBorder
extends FlatMarginBorder {
    private static final int DOT_COUNT = 4;
    private static final int DOT_SIZE = 2;
    private static final int GRIP_SIZE = 6;
    protected Color gripColor = UIManager.getColor("ToolBar.gripColor");

    public FlatToolBarBorder() {
        super(UIManager.getInsets("ToolBar.borderMargins"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c instanceof JToolBar && ((JToolBar)c).isFloatable()) {
            Graphics2D g2 = (Graphics2D)g.create();
            try {
                FlatUIUtils.setRenderingHints(g2);
                Color color = FlatToolBarBorder.getStyleFromToolBarUI(c, ui -> ui.gripColor);
                g2.setColor(color != null ? color : this.gripColor);
                this.paintGrip(c, g2, x, y, width, height);
            } finally {
                g2.dispose();
            }
        }
    }

    protected void paintGrip(Component c, Graphics g, int x, int y, int width, int height) {
        Rectangle r = this.calculateGripBounds(c, x, y, width, height);
        FlatUIUtils.paintGrip(g, r.x, r.y, r.width, r.height, ((JToolBar)c).getOrientation() == 1, 4, 2, 2, false);
    }

    protected Rectangle calculateGripBounds(Component c, int x, int y, int width, int height) {
        Insets insets = super.getBorderInsets(c, new Insets(0, 0, 0, 0));
        Rectangle r = FlatUIUtils.subtractInsets(new Rectangle(x, y, width, height), insets);
        int gripSize = UIScale.scale(6);
        if (((JToolBar)c).getOrientation() == 0) {
            if (!c.getComponentOrientation().isLeftToRight()) {
                r.x = r.x + r.width - gripSize;
            }
            r.width = gripSize;
        } else {
            r.height = gripSize;
        }
        return r;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets m = FlatToolBarBorder.getStyleFromToolBarUI(c, ui -> ui.borderMargins);
        if (m != null) {
            int t = this.top;
            int l = this.left;
            int b = this.bottom;
            int r = this.right;
            this.top = m.top;
            this.left = m.left;
            this.bottom = m.bottom;
            this.right = m.right;
            insets = super.getBorderInsets(c, insets);
            this.top = t;
            this.left = l;
            this.bottom = b;
            this.right = r;
        } else {
            insets = super.getBorderInsets(c, insets);
        }
        if (c instanceof JToolBar && ((JToolBar)c).isFloatable()) {
            int gripInset = UIScale.scale(6);
            if (((JToolBar)c).getOrientation() == 0) {
                if (c.getComponentOrientation().isLeftToRight()) {
                    insets.left += gripInset;
                } else {
                    insets.right += gripInset;
                }
            } else {
                insets.top += gripInset;
            }
        }
        return insets;
    }

    static <T> T getStyleFromToolBarUI(Component c, Function<FlatToolBarUI, T> f) {
        ToolBarUI ui;
        if (c instanceof JToolBar && (ui = ((JToolBar)c).getUI()) instanceof FlatToolBarUI) {
            return f.apply((FlatToolBarUI)ui);
        }
        return null;
    }
}

