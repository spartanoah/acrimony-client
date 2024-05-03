/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import javax.swing.UIManager;

public class FlatFileChooserUpFolderIcon
extends FlatAbstractIcon {
    private final Color blueColor = UIManager.getColor("Actions.Blue");

    public FlatFileChooserUpFolderIcon() {
        super(16, 16, UIManager.getColor("Actions.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setStroke(new BasicStroke(1.0f, 1, 1));
        g.draw(FlatFileViewDirectoryIcon.createFolderPath());
        g.setColor(this.blueColor);
        g.draw(new Line2D.Float(8.0f, 6.5f, 8.0f, 11.5f));
        g.draw(FlatUIUtils.createPath(false, 5.5, 9.0, 8.0, 6.5, 10.5, 9.0));
    }
}

