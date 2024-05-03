/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.UIManager;

public class FlatFileViewDirectoryIcon
extends FlatAbstractIcon {
    private Path2D path;

    public FlatFileViewDirectoryIcon() {
        super(16, 16, UIManager.getColor("Objects.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (this.path == null) {
            this.path = FlatFileViewDirectoryIcon.createFolderPath();
        }
        g.draw(this.path);
    }

    static Path2D createFolderPath() {
        double arc = 1.5;
        double arc2 = 0.5;
        return FlatUIUtils.createPath(14.5, 13.5 - arc, -1.000000000002E12, 14.5, 13.5, 14.5 - arc, 13.5, 1.5 + arc, 13.5, -1.000000000002E12, 1.5, 13.5, 1.5, 13.5 - arc, 1.5, 2.5 + arc, -1.000000000002E12, 1.5, 2.5, 1.5 + arc, 2.5, 6.5 - arc2, 2.5, -1.000000000002E12, 6.5, 2.5, 6.5 + arc2, 2.5 + arc2, 8.5, 4.5, 14.5 - arc, 4.5, -1.000000000002E12, 14.5, 4.5, 14.5, 4.5 + arc);
    }
}

