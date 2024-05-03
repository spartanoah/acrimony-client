/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Map;
import javax.swing.UIManager;

public class FlatTabbedPaneCloseIcon
extends FlatAbstractIcon {
    @FlatStylingSupport.Styleable
    protected Dimension closeSize = UIManager.getDimension("TabbedPane.closeSize");
    @FlatStylingSupport.Styleable
    protected int closeArc = UIManager.getInt("TabbedPane.closeArc");
    @FlatStylingSupport.Styleable
    protected float closeCrossPlainSize = FlatUIUtils.getUIFloat("TabbedPane.closeCrossPlainSize", 7.5f);
    @FlatStylingSupport.Styleable
    protected float closeCrossFilledSize = FlatUIUtils.getUIFloat("TabbedPane.closeCrossFilledSize", this.closeCrossPlainSize);
    @FlatStylingSupport.Styleable
    protected float closeCrossLineWidth = FlatUIUtils.getUIFloat("TabbedPane.closeCrossLineWidth", 1.0f);
    @FlatStylingSupport.Styleable
    protected Color closeBackground = UIManager.getColor("TabbedPane.closeBackground");
    @FlatStylingSupport.Styleable
    protected Color closeForeground = UIManager.getColor("TabbedPane.closeForeground");
    @FlatStylingSupport.Styleable
    protected Color closeHoverBackground = UIManager.getColor("TabbedPane.closeHoverBackground");
    @FlatStylingSupport.Styleable
    protected Color closeHoverForeground = UIManager.getColor("TabbedPane.closeHoverForeground");
    @FlatStylingSupport.Styleable
    protected Color closePressedBackground = UIManager.getColor("TabbedPane.closePressedBackground");
    @FlatStylingSupport.Styleable
    protected Color closePressedForeground = UIManager.getColor("TabbedPane.closePressedForeground");

    public FlatTabbedPaneCloseIcon() {
        super(16, 16, null);
    }

    public Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
    }

    public Map<String, Class<?>> getStyleableInfos() {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    public Object getStyleableValue(String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        Color bg = FlatButtonUI.buttonStateColor(c, this.closeBackground, null, null, this.closeHoverBackground, this.closePressedBackground);
        if (bg != null) {
            g.setColor(FlatUIUtils.deriveColor(bg, c.getBackground()));
            g.fillRoundRect((this.width - this.closeSize.width) / 2, (this.height - this.closeSize.height) / 2, this.closeSize.width, this.closeSize.height, this.closeArc, this.closeArc);
        }
        Color fg = FlatButtonUI.buttonStateColor(c, this.closeForeground, null, null, this.closeHoverForeground, this.closePressedForeground);
        g.setColor(FlatUIUtils.deriveColor(fg, c.getForeground()));
        float mx = (float)this.width / 2.0f;
        float my = (float)this.height / 2.0f;
        float r = (bg != null ? this.closeCrossFilledSize : this.closeCrossPlainSize) / 2.0f;
        Path2D.Float path = new Path2D.Float(0, 4);
        ((Path2D)path).moveTo(mx - r, my - r);
        ((Path2D)path).lineTo(mx + r, my + r);
        ((Path2D)path).moveTo(mx - r, my + r);
        ((Path2D)path).lineTo(mx + r, my - r);
        g.setStroke(new BasicStroke(this.closeCrossLineWidth));
        g.draw(path);
    }
}

