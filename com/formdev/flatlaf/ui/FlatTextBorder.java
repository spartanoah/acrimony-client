/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Component;
import javax.swing.UIManager;

public class FlatTextBorder
extends FlatBorder {
    @FlatStylingSupport.Styleable
    protected int arc = UIManager.getInt("TextComponent.arc");
    @FlatStylingSupport.Styleable
    protected Boolean roundRect;

    @Override
    protected int getArc(Component c) {
        if (this.isCellEditor(c)) {
            return 0;
        }
        Boolean roundRect = FlatUIUtils.isRoundRect(c);
        if (roundRect == null) {
            roundRect = this.roundRect;
        }
        return roundRect != null ? (roundRect.booleanValue() ? Short.MAX_VALUE : 0) : this.arc;
    }
}

