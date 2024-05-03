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
import javax.swing.UIManager;

public class FlatFileViewFloppyDriveIcon
extends FlatAbstractIcon {
    public FlatFileViewFloppyDriveIcon() {
        super(16, 16, UIManager.getColor("Objects.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 1, 1));
        g.draw(FlatUIUtils.createPath(3.5, 2.5, 11.5, 2.5, 11.5, 2.5, 13.5, 4.5, 13.5, 12.5, -1.000000000002E12, 13.5, 13.5, 12.5, 13.5, 3.5, 13.5, -1.000000000002E12, 2.5, 13.5, 2.5, 12.5, 2.5, 3.5, -1.000000000002E12, 2.5, 2.5, 3.5, 2.5));
        g.draw(FlatUIUtils.createPath(false, 4.5, 13.0, 4.5, 9.5, 11.5, 9.5, 11.5, 13.0));
        g.draw(FlatUIUtils.createPath(false, 5.5, 3.0, 5.5, 5.5, 10.5, 5.5, 10.5, 3.0));
    }
}

