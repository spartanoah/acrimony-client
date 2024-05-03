/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.icons.FlatTreeCollapsedIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.UIManager;

public class FlatTreeOpenIcon
extends FlatAbstractIcon {
    private Path2D path;

    public FlatTreeOpenIcon() {
        super(16, 16, UIManager.getColor("Tree.icon.openColor"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        FlatTreeCollapsedIcon.setStyleColorFromTreeUI(c, g, ui -> ui.iconOpenColor);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 0, 0));
        if (this.path == null) {
            double arc = 1.5;
            double arc2 = 0.5;
            this.path = FlatUIUtils.createPath(false, 2.0, 13.5, -1.000000000004E12, 4.5, 7.5, arc, -1.000000000004E12, 15.5, 7.5, arc2, -1.000000000004E12, 13.0, 13.5, arc, 1.5 + arc, 13.5, -1.000000000002E12, 1.5, 13.5, 1.5, 13.5 - arc, 1.5, 2.5 + arc, -1.000000000002E12, 1.5, 2.5, 1.5 + arc, 2.5, 6.5 - arc2, 2.5, -1.000000000002E12, 6.5, 2.5, 6.5 + arc2, 2.5 + arc2, 8.5, 4.5, 13.5 - arc, 4.5, -1.000000000002E12, 13.5, 4.5, 13.5, 4.5 + arc, 13.5, 6.5);
        }
        g.draw(this.path);
    }
}

