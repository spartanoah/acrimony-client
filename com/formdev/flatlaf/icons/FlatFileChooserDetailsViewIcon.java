/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import java.awt.Component;
import java.awt.Graphics2D;
import javax.swing.UIManager;

public class FlatFileChooserDetailsViewIcon
extends FlatAbstractIcon {
    public FlatFileChooserDetailsViewIcon() {
        super(16, 16, UIManager.getColor("Actions.Grey"));
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        g.fillRoundRect(2, 3, 2, 1, 1, 1);
        g.fillRoundRect(2, 6, 2, 1, 1, 1);
        g.fillRoundRect(2, 9, 2, 1, 1, 1);
        g.fillRoundRect(2, 12, 2, 1, 1, 1);
        g.fillRoundRect(6, 3, 8, 1, 1, 1);
        g.fillRoundRect(6, 6, 8, 1, 1, 1);
        g.fillRoundRect(6, 9, 8, 1, 1, 1);
        g.fillRoundRect(6, 12, 8, 1, 1, 1);
    }
}

