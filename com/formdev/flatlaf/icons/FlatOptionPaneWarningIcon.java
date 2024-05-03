/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatOptionPaneAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class FlatOptionPaneWarningIcon
extends FlatOptionPaneAbstractIcon {
    public FlatOptionPaneWarningIcon() {
        super("OptionPane.icon.warningColor", "Actions.Yellow");
    }

    @Override
    protected Shape createOutside() {
        return FlatUIUtils.createRoundTrianglePath(16.0f, 0.0f, 32.0f, 28.0f, 0.0f, 28.0f, 4.0f);
    }

    @Override
    protected Shape createInside() {
        Path2D.Float inside = new Path2D.Float(0);
        inside.append(new RoundRectangle2D.Float(14.0f, 8.0f, 4.0f, 11.0f, 4.0f, 4.0f), false);
        inside.append(new Ellipse2D.Float(14.0f, 21.0f, 4.0f, 4.0f), false);
        return inside;
    }
}

