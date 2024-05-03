/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatMarginBorder;
import com.formdev.flatlaf.ui.FlatMenuBarUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

public class FlatMenuBarBorder
extends FlatMarginBorder
implements FlatStylingSupport.StyleableBorder {
    @FlatStylingSupport.Styleable
    protected Color borderColor = UIManager.getColor("MenuBar.borderColor");

    @Override
    public Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos() {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (!this.showBottomSeparator(c)) {
            return;
        }
        float lineHeight = UIScale.scale(1.0f);
        FlatUIUtils.paintFilledRectangle(g, this.borderColor, x, (float)(y + height) - lineHeight, width, lineHeight);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets margin = c instanceof JMenuBar ? ((JMenuBar)c).getMargin() : new Insets(0, 0, 0, 0);
        insets.top = UIScale.scale(margin.top);
        insets.left = UIScale.scale(margin.left);
        insets.bottom = UIScale.scale(margin.bottom + 1);
        insets.right = UIScale.scale(margin.right);
        return insets;
    }

    protected boolean showBottomSeparator(Component c) {
        return !FlatMenuBarUI.useUnifiedBackground(c);
    }
}

