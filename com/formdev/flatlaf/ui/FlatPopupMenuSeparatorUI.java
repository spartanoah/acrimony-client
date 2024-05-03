/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatSeparatorUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class FlatPopupMenuSeparatorUI
extends FlatSeparatorUI {
    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatPopupMenuSeparatorUI.class, () -> new FlatPopupMenuSeparatorUI(true)) : new FlatPopupMenuSeparatorUI(false);
    }

    protected FlatPopupMenuSeparatorUI(boolean shared) {
        super(shared);
    }

    @Override
    protected String getPropertyPrefix() {
        return "PopupMenuSeparator";
    }

    @Override
    String getStyleType() {
        return "PopupMenuSeparator";
    }
}

