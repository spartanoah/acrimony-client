/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatInternalFrameAbstractIcon;
import com.formdev.flatlaf.ui.FlatButtonUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.UIManager;

public class FlatInternalFrameCloseIcon
extends FlatInternalFrameAbstractIcon {
    private final Color hoverForeground = UIManager.getColor("InternalFrame.closeHoverForeground");
    private final Color pressedForeground = UIManager.getColor("InternalFrame.closePressedForeground");

    public FlatInternalFrameCloseIcon() {
        super(UIManager.getDimension("InternalFrame.buttonSize"), UIManager.getColor("InternalFrame.closeHoverBackground"), UIManager.getColor("InternalFrame.closePressedBackground"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        this.paintBackground(c, g);
        g.setColor(FlatButtonUI.buttonStateColor(c, c.getForeground(), null, null, this.hoverForeground, this.pressedForeground));
        float mx = (float)this.width / 2.0f;
        float my = (float)this.height / 2.0f;
        float r = 3.25f;
        Path2D.Float path = new Path2D.Float(0, 4);
        ((Path2D)path).moveTo(mx - r, my - r);
        ((Path2D)path).lineTo(mx + r, my + r);
        ((Path2D)path).moveTo(mx - r, my + r);
        ((Path2D)path).lineTo(mx + r, my - r);
        g.setStroke(new BasicStroke(1.0f));
        g.draw(path);
    }
}

