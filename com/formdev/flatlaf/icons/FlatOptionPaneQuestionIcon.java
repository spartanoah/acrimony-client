/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatOptionPaneAbstractIcon;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class FlatOptionPaneQuestionIcon
extends FlatOptionPaneAbstractIcon {
    public FlatOptionPaneQuestionIcon() {
        super("OptionPane.icon.questionColor", "Actions.Blue");
    }

    @Override
    protected Shape createOutside() {
        return new Ellipse2D.Float(2.0f, 2.0f, 28.0f, 28.0f);
    }

    @Override
    protected Shape createInside() {
        Path2D.Float q = new Path2D.Float(1, 10);
        ((Path2D)q).moveTo(11.5, 11.75);
        ((Path2D)q).curveTo(11.75, 9.5, 13.75, 8.0, 16.0, 8.0);
        ((Path2D)q).curveTo(18.25, 8.0, 20.5, 9.5, 20.5, 11.75);
        ((Path2D)q).curveTo(20.5, 14.75, 16.0, 15.5, 16.0, 19.0);
        BasicStroke stroke = new BasicStroke(3.0f, 1, 0);
        Path2D.Float inside = new Path2D.Float(0);
        inside.append(new Ellipse2D.Float(14.3f, 22.3f, 3.4f, 3.4f), false);
        inside.append(stroke.createStrokedShape(q), false);
        return inside;
    }
}

