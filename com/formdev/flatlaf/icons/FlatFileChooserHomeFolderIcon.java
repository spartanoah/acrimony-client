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

public class FlatFileChooserHomeFolderIcon
extends FlatAbstractIcon {
    public FlatFileChooserHomeFolderIcon() {
        super(16, 16, UIManager.getColor("Actions.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 1, 1));
        g.draw(FlatUIUtils.createPath(false, 6.5, 13.0, 6.5, 9.5, 9.5, 9.5, 9.5, 13.0));
        g.draw(FlatUIUtils.createPath(false, 3.5, 6.5, 3.5, 12.5, -1.000000000002E12, 3.5, 13.5, 4.5, 13.5, 11.5, 13.5, -1.000000000002E12, 12.5, 13.5, 12.5, 12.5, 12.5, 6.5));
        g.draw(FlatUIUtils.createPath(false, 1.5, 8.5, 8.0, 2.0, 14.5, 8.5));
    }
}

