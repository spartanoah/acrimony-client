/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class FlatPopupMenuBorder
extends FlatLineBorder
implements FlatStylingSupport.StyleableBorder {
    private Color borderColor;

    public FlatPopupMenuBorder() {
        super(UIManager.getInsets("PopupMenu.borderInsets"), UIManager.getColor("PopupMenu.borderColor"));
    }

    @Override
    public Object applyStyleProperty(String key, Object value) {
        switch (key) {
            case "borderInsets": {
                return this.applyStyleProperty((Insets)value);
            }
            case "borderColor": {
                Color oldValue = this.getLineColor();
                this.borderColor = (Color)value;
                return oldValue;
            }
        }
        throw new FlatStylingSupport.UnknownStyleException(key);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos() {
        FlatStylingSupport.StyleableInfosMap infos = new FlatStylingSupport.StyleableInfosMap();
        infos.put("borderInsets", Insets.class);
        infos.put("borderColor", Color.class);
        return infos;
    }

    @Override
    public Object getStyleableValue(String key) {
        switch (key) {
            case "borderInsets": {
                return this.getStyleableValue();
            }
            case "borderColor": {
                return this.borderColor;
            }
        }
        return null;
    }

    @Override
    public Color getLineColor() {
        return this.borderColor != null ? this.borderColor : super.getLineColor();
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        if (c instanceof Container && ((Container)c).getComponentCount() > 0 && ((Container)c).getComponent(0) instanceof JScrollPane) {
            insets.right = insets.bottom = UIScale.scale(1);
            insets.top = insets.bottom;
            insets.left = insets.bottom;
            return insets;
        }
        return super.getBorderInsets(c, insets);
    }
}

