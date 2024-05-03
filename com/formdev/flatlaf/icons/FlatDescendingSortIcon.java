/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAscendingSortIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

public class FlatDescendingSortIcon
extends FlatAscendingSortIcon {
    @Override
    protected void paintArrow(Component c, Graphics2D g, boolean chevron) {
        if (chevron) {
            Path2D path = FlatUIUtils.createPath(false, 1.0, 0.0, 5.0, 4.0, 9.0, 0.0);
            g.setStroke(new BasicStroke(1.0f));
            g.draw(path);
        } else {
            g.fill(FlatUIUtils.createPath(0.5, 0.0, 5.0, 5.0, 9.5, 0.0));
        }
    }
}

