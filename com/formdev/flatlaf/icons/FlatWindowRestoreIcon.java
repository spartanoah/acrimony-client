/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class FlatWindowRestoreIcon
extends FlatWindowAbstractIcon {
    public FlatWindowRestoreIcon() {
        this(null);
    }

    public FlatWindowRestoreIcon(String windowStyle) {
        super(windowStyle);
    }

    @Override
    protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        int iwh = (int)((double)this.getSymbolHeight() * scaleFactor);
        int ix = x + (width - iwh) / 2;
        int iy = y + (height - iwh) / 2;
        float thickness = SystemInfo.isWindows_11_orLater ? (float)scaleFactor : (float)((int)scaleFactor);
        int arc = Math.max((int)(1.5 * scaleFactor), 2);
        int arcOuter = (int)((double)arc + 1.5 * scaleFactor);
        int rwh = (int)((double)(this.getSymbolHeight() - 2) * scaleFactor);
        int ro2 = iwh - rwh;
        Path2D r1 = SystemInfo.isWindows_11_orLater ? FlatUIUtils.createRoundRectangle(ix + ro2, iy, rwh, rwh, thickness, arc, arcOuter, arc, arc) : FlatUIUtils.createRectangle(ix + ro2, iy, rwh, rwh, thickness);
        Path2D r2 = SystemInfo.isWindows_11_orLater ? FlatUIUtils.createRoundRectangle(ix, iy + ro2, rwh, rwh, thickness, arc, arc, arc, arc) : FlatUIUtils.createRectangle(ix, iy + ro2, rwh, rwh, thickness);
        Area area = new Area(r1);
        if (SystemInfo.isWindows_11_orLater) {
            area.subtract(new Area(new Rectangle2D.Float(ix, (float)((double)iy + scaleFactor), rwh, rwh)));
            area.subtract(new Area(new Rectangle2D.Float((float)((double)ix + scaleFactor), iy + ro2, rwh, rwh)));
        } else {
            area.subtract(new Area(new Rectangle2D.Float(ix, iy + ro2, rwh, rwh)));
        }
        g.fill(area);
        g.fill(r2);
    }
}

