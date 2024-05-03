/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatScrollPaneUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

public class FlatScrollPaneBorder
extends FlatBorder {
    @FlatStylingSupport.Styleable
    protected int arc = UIManager.getInt("ScrollPane.arc");
    private boolean isArcStyled;
    private final int listArc = FlatUIUtils.getUIInt("ScrollPane.List.arc", -1);
    private final int tableArc = FlatUIUtils.getUIInt("ScrollPane.Table.arc", -1);
    private final int textComponentArc = FlatUIUtils.getUIInt("ScrollPane.TextComponent.arc", -1);
    private final int treeArc = FlatUIUtils.getUIInt("ScrollPane.Tree.arc", -1);

    @Override
    public Object applyStyleProperty(String key, Object value) {
        Object oldValue = super.applyStyleProperty(key, value);
        if ("arc".equals(key)) {
            this.isArcStyled = true;
        }
        return oldValue;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets = super.getBorderInsets(c, insets);
        int padding = this.getLeftRightPadding(c);
        if (padding > 0) {
            insets.left += padding;
            insets.right += padding;
        }
        return insets;
    }

    @Override
    protected int getArc(Component c) {
        if (this.isCellEditor(c)) {
            return 0;
        }
        if (this.isArcStyled) {
            return this.arc;
        }
        if (c instanceof JScrollPane) {
            Component view = FlatScrollPaneUI.getView((JScrollPane)c);
            if (this.listArc >= 0 && view instanceof JList) {
                return this.listArc;
            }
            if (this.tableArc >= 0 && view instanceof JTable) {
                return this.tableArc;
            }
            if (this.textComponentArc >= 0 && view instanceof JTextComponent) {
                return this.textComponentArc;
            }
            if (this.treeArc >= 0 && view instanceof JTree) {
                return this.treeArc;
            }
        }
        return this.arc;
    }

    public int getLeftRightPadding(Component c) {
        int arc = this.getArc(c);
        return arc > 0 ? Math.max(Math.round(UIScale.scale(((float)arc / 2.0f - (float)this.getLineWidth(c)) * 0.9f)), 0) : 0;
    }
}

