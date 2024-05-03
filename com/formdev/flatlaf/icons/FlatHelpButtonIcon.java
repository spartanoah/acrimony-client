/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Map;
import javax.swing.UIManager;

public class FlatHelpButtonIcon
extends FlatAbstractIcon {
    @FlatStylingSupport.Styleable
    protected int focusWidth = UIManager.getInt("Component.focusWidth");
    @FlatStylingSupport.Styleable
    protected Color focusColor = UIManager.getColor("Component.focusColor");
    @FlatStylingSupport.Styleable
    protected float innerFocusWidth = FlatUIUtils.getUIFloat("HelpButton.innerFocusWidth", FlatUIUtils.getUIFloat("Component.innerFocusWidth", 0.0f));
    @FlatStylingSupport.Styleable
    protected int borderWidth = FlatUIUtils.getUIInt("HelpButton.borderWidth", 1);
    @FlatStylingSupport.Styleable
    protected Color borderColor = UIManager.getColor("HelpButton.borderColor");
    @FlatStylingSupport.Styleable
    protected Color disabledBorderColor = UIManager.getColor("HelpButton.disabledBorderColor");
    @FlatStylingSupport.Styleable
    protected Color focusedBorderColor = UIManager.getColor("HelpButton.focusedBorderColor");
    @FlatStylingSupport.Styleable
    protected Color hoverBorderColor = UIManager.getColor("HelpButton.hoverBorderColor");
    @FlatStylingSupport.Styleable
    protected Color background = UIManager.getColor("HelpButton.background");
    @FlatStylingSupport.Styleable
    protected Color disabledBackground = UIManager.getColor("HelpButton.disabledBackground");
    @FlatStylingSupport.Styleable
    protected Color focusedBackground = UIManager.getColor("HelpButton.focusedBackground");
    @FlatStylingSupport.Styleable
    protected Color hoverBackground = UIManager.getColor("HelpButton.hoverBackground");
    @FlatStylingSupport.Styleable
    protected Color pressedBackground = UIManager.getColor("HelpButton.pressedBackground");
    @FlatStylingSupport.Styleable
    protected Color questionMarkColor = UIManager.getColor("HelpButton.questionMarkColor");
    @FlatStylingSupport.Styleable
    protected Color disabledQuestionMarkColor = UIManager.getColor("HelpButton.disabledQuestionMarkColor");

    public FlatHelpButtonIcon() {
        super(0, 0, null);
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
    protected void paintIcon(Component c, Graphics2D g2) {
        boolean enabled = c == null || c.isEnabled();
        boolean focused = c != null && FlatUIUtils.isPermanentFocusOwner(c);
        float xy = 0.5f;
        float wh = this.iconSize() - 1;
        if (focused && FlatButtonUI.isFocusPainted(c)) {
            g2.setColor(this.focusColor);
            g2.fill(new Ellipse2D.Float(xy, xy, wh, wh));
        }
        g2.setColor(FlatButtonUI.buttonStateColor(c, this.borderColor, this.disabledBorderColor, this.focusedBorderColor, this.hoverBorderColor, null));
        g2.fill(new Ellipse2D.Float(xy += (float)this.focusWidth, xy, wh -= (float)(this.focusWidth * 2), wh));
        xy += (float)this.borderWidth;
        wh -= (float)(this.borderWidth * 2);
        if (this.innerFocusWidth > 0.0f && focused && FlatButtonUI.isFocusPainted(c)) {
            g2.setColor(this.focusColor);
            g2.fill(new Ellipse2D.Float(xy, xy, wh, wh));
            xy += this.innerFocusWidth;
            wh -= this.innerFocusWidth * 2.0f;
        }
        g2.setColor(FlatUIUtils.deriveColor(FlatButtonUI.buttonStateColor(c, this.background, this.disabledBackground, this.focusedBackground, this.hoverBackground, this.pressedBackground), this.background));
        g2.fill(new Ellipse2D.Float(xy, xy, wh, wh));
        Path2D.Float q = new Path2D.Float(1, 10);
        ((Path2D)q).moveTo(8.0, 8.5);
        ((Path2D)q).curveTo(8.25, 7.0, 9.66585007, 6.0, 11.0, 6.0);
        ((Path2D)q).curveTo(12.5, 6.0, 14.0, 7.0, 14.0, 8.5);
        ((Path2D)q).curveTo(14.0, 10.5, 11.0, 11.0, 11.0, 13.0);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setStroke(new BasicStroke(2.0f, 1, 1));
        g2.translate(this.focusWidth, this.focusWidth);
        g2.setColor(enabled ? this.questionMarkColor : this.disabledQuestionMarkColor);
        g2.draw(q);
        g2.fill(new Ellipse2D.Float(9.8f, 14.8f, 2.4f, 2.4f));
    }

    @Override
    public int getIconWidth() {
        return UIScale.scale(this.iconSize());
    }

    @Override
    public int getIconHeight() {
        return UIScale.scale(this.iconSize());
    }

    private int iconSize() {
        return 22 + this.focusWidth * 2;
    }
}

