/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.UIManager;

public class FlatFileViewHardDriveIcon
extends FlatAbstractIcon {
    public FlatFileViewHardDriveIcon() {
        super(16, 16, UIManager.getColor("Objects.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 1, 1));
        g.draw(new RoundRectangle2D.Float(2.5f, 5.5f, 11.0f, 5.0f, 2.0f, 2.0f));
        g.fill(new Ellipse2D.Float(10.8f, 7.8f, 1.4f, 1.4f));
        g.fill(new Ellipse2D.Float(8.8f, 7.8f, 1.4f, 1.4f));
    }
}

