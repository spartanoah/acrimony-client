/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.JavaCompatibility2;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicViewportUI;

public class FlatViewportUI
extends BasicViewportUI {
    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.createSharedUI(FlatViewportUI.class, FlatViewportUI::new);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ComponentUI ui;
        super.paint(g, c);
        Component view = ((JViewport)c).getView();
        if (view instanceof JComponent && (ui = JavaCompatibility2.getUI((JComponent)view)) instanceof ViewportPainter) {
            ((ViewportPainter)((Object)ui)).paintViewport(g, (JComponent)view, (JViewport)c);
        }
    }

    public static interface ViewportPainter {
        public void paintViewport(Graphics var1, JComponent var2, JViewport var3);
    }
}

