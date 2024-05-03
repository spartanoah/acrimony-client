/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.HiDPIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public abstract class FlatWindowAbstractIcon
extends FlatAbstractIcon {
    private final int symbolHeight;
    private final Color hoverBackground;
    private final Color pressedBackground;

    protected FlatWindowAbstractIcon(String windowStyle) {
        this(FlatUIUtils.getSubUIDimension("TitlePane.buttonSize", windowStyle), FlatUIUtils.getSubUIInt("TitlePane.buttonSymbolHeight", windowStyle, 10), FlatUIUtils.getSubUIColor("TitlePane.buttonHoverBackground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.buttonPressedBackground", windowStyle));
    }

    protected FlatWindowAbstractIcon(Dimension size, int symbolHeight, Color hoverBackground, Color pressedBackground) {
        super(size.width, size.height, null);
        this.symbolHeight = symbolHeight;
        this.hoverBackground = hoverBackground;
        this.pressedBackground = pressedBackground;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        this.paintBackground(c, g);
        g.setColor(this.getForeground(c));
        HiDPIUtils.paintAtScale1x(g, 0, 0, this.width, this.height, this::paintIconAt1x);
    }

    protected abstract void paintIconAt1x(Graphics2D var1, int var2, int var3, int var4, int var5, double var6);

    protected void paintBackground(Component c, Graphics2D g) {
        Color background = FlatButtonUI.buttonStateColor(c, null, null, null, this.hoverBackground, this.pressedBackground);
        if (background != null) {
            Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(FlatUIUtils.deriveColor(background, c.getBackground()));
            g.fillRect(0, 0, this.width, this.height);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint);
        }
    }

    protected Color getForeground(Component c) {
        return c.getForeground();
    }

    protected int getSymbolHeight() {
        return this.symbolHeight;
    }
}

