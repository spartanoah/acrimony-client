/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import java.awt.Graphics2D;

public class FlatSearchWithHistoryIcon
extends FlatSearchIcon {
    public FlatSearchWithHistoryIcon() {
        this(false);
    }

    public FlatSearchWithHistoryIcon(boolean ignoreButtonState) {
        super(ignoreButtonState);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.translate(-2, 0);
        super.paintIcon(c, g);
        g.translate(2, 0);
        g.fill(FlatUIUtils.createPath(11.0, 7.0, 16.0, 7.0, 13.5, 10.0));
    }
}

