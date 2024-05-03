/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import com.formdev.flatlaf.icons.FlatTreeCollapsedIcon;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.UIManager;

public class FlatTreeClosedIcon
extends FlatAbstractIcon {
    private Path2D path;

    public FlatTreeClosedIcon() {
        super(16, 16, UIManager.getColor("Tree.icon.closedColor"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        FlatTreeCollapsedIcon.setStyleColorFromTreeUI(c, g, ui -> ui.iconClosedColor);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (this.path == null) {
            this.path = FlatFileViewDirectoryIcon.createFolderPath();
        }
        g.draw(this.path);
    }
}

