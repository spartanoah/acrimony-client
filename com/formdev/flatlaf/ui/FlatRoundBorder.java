/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatSpinnerUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.plaf.SpinnerUI;

public class FlatRoundBorder
extends FlatBorder {
    @FlatStylingSupport.Styleable
    protected int arc = UIManager.getInt("Component.arc");
    @FlatStylingSupport.Styleable
    protected Boolean roundRect;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (this.isMacStyleSpinner(c)) {
            int macStyleButtonsWidth = ((FlatSpinnerUI)((JSpinner)c).getUI()).getMacStyleButtonsWidth();
            width -= macStyleButtonsWidth;
            if (!c.getComponentOrientation().isLeftToRight()) {
                x += macStyleButtonsWidth;
            }
        }
        super.paintBorder(c, g, x, y, width, height);
    }

    @Override
    protected int getArc(Component c) {
        if (this.isCellEditor(c)) {
            return 0;
        }
        Boolean roundRect = FlatUIUtils.isRoundRect(c);
        if (roundRect == null) {
            roundRect = this.roundRect;
        }
        return roundRect != null ? (roundRect.booleanValue() ? Short.MAX_VALUE : 0) : (this.isMacStyleSpinner(c) ? 0 : this.arc);
    }

    private boolean isMacStyleSpinner(Component c) {
        SpinnerUI ui;
        if (c instanceof JSpinner && (ui = ((JSpinner)c).getUI()) instanceof FlatSpinnerUI) {
            return ((FlatSpinnerUI)ui).isMacStyle();
        }
        return false;
    }
}

