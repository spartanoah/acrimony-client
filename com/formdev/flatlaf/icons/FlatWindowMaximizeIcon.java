/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Graphics2D;

public class FlatWindowMaximizeIcon
extends FlatWindowAbstractIcon {
    public FlatWindowMaximizeIcon() {
        this(null);
    }

    public FlatWindowMaximizeIcon(String windowStyle) {
        super(windowStyle);
    }

    @Override
    protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        int iwh = (int)((double)this.getSymbolHeight() * scaleFactor);
        int ix = x + (width - iwh) / 2;
        int iy = y + (height - iwh) / 2;
        float thickness = SystemInfo.isWindows_11_orLater ? (float)scaleFactor : (float)((int)scaleFactor);
        int arc = Math.max((int)(1.5 * scaleFactor), 2);
        g.fill(SystemInfo.isWindows_11_orLater ? FlatUIUtils.createRoundRectangle(ix, iy, iwh, iwh, thickness, arc, arc, arc, arc) : FlatUIUtils.createRectangle(ix, iy, iwh, iwh, thickness));
    }
}

