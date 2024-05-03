/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.UIManager;

public class FlatFileViewFileIcon
extends FlatAbstractIcon {
    private Path2D path;

    public FlatFileViewFileIcon() {
        super(16, 16, UIManager.getColor("Objects.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 1, 1));
        if (this.path == null) {
            double arc = 1.5;
            this.path = FlatUIUtils.createPath(false, 2.5, 1.5 + arc, -1.000000000002E12, 2.5, 1.5, 2.5 + arc, 1.5, 8.8, 1.5, 13.5, 6.2, 13.5, 14.5 - arc, -1.000000000002E12, 13.5, 14.5, 13.5 - arc, 14.5, 2.5 + arc, 14.5, -1.000000000002E12, 2.5, 14.5, 2.5, 14.5 - arc, -1.000000000005E12, -1.000000000001E12, 8.5, 2.0, 8.5, 6.5 - arc, -1.000000000002E12, 8.5, 6.5, 8.5 + arc, 6.5, 13.0, 6.5);
        }
        g.draw(this.path);
    }
}

