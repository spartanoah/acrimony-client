/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class FlatRadioButtonIcon
extends FlatCheckBoxIcon {
    @FlatStylingSupport.Styleable
    protected float centerDiameter;

    public FlatRadioButtonIcon() {
        this.centerDiameter = FlatRadioButtonIcon.getUIFloat("RadioButton.icon.centerDiameter", 8.0f, this.style);
    }

    @Override
    protected String getPropertyPrefix() {
        return "RadioButton.";
    }

    @Override
    protected void paintFocusBorder(Component c, Graphics2D g) {
        float wh = 15.0f + this.focusWidth * 2.0f;
        g.fill(new Ellipse2D.Float(-this.focusWidth, -this.focusWidth, wh, wh));
    }

    @Override
    protected void paintBorder(Component c, Graphics2D g, float borderWidth) {
        if (borderWidth == 0.0f) {
            return;
        }
        g.fillOval(0, 0, 15, 15);
    }

    @Override
    protected void paintBackground(Component c, Graphics2D g, float borderWidth) {
        float xy = borderWidth;
        float wh = 15.0f - borderWidth * 2.0f;
        g.fill(new Ellipse2D.Float(xy, xy, wh, wh));
    }

    @Override
    protected void paintCheckmark(Component c, Graphics2D g) {
        float xy = (15.0f - this.centerDiameter) / 2.0f;
        g.fill(new Ellipse2D.Float(xy, xy, this.centerDiameter, this.centerDiameter));
    }
}

