/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;
import java.awt.Graphics2D;

public class FlatWindowIconifyIcon
extends FlatWindowAbstractIcon {
    public FlatWindowIconifyIcon() {
        this(null);
    }

    public FlatWindowIconifyIcon(String windowStyle) {
        super(windowStyle);
    }

    @Override
    protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        int iw = (int)((double)this.getSymbolHeight() * scaleFactor);
        int ih = (int)scaleFactor;
        int ix = x + (width - iw) / 2;
        int iy = y + (height - ih) / 2;
        g.fillRect(ix, iy, iw, ih);
    }
}

