/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatMarginBorder;
import com.formdev.flatlaf.ui.FlatMenuBarUI;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.plaf.MenuBarUI;

public class FlatMenuItemBorder
extends FlatMarginBorder {
    private final Insets menuBarItemMargins = UIManager.getInsets("MenuBar.itemMargins");

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Container parent = c.getParent();
        if (parent instanceof JMenuBar) {
            MenuBarUI ui = ((JMenuBar)parent).getUI();
            Insets margins = ui instanceof FlatMenuBarUI && ((FlatMenuBarUI)ui).itemMargins != null ? ((FlatMenuBarUI)ui).itemMargins : this.menuBarItemMargins;
            insets.top = UIScale.scale(margins.top);
            insets.left = UIScale.scale(margins.left);
            insets.bottom = UIScale.scale(margins.bottom);
            insets.right = UIScale.scale(margins.right);
            return insets;
        }
        return super.getBorderInsets(c, insets);
    }
}

