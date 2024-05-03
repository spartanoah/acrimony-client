/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.UIManager;

public class FlatClearIcon
extends FlatAbstractIcon {
    @FlatStylingSupport.Styleable
    protected Color clearIconColor = UIManager.getColor("SearchField.clearIconColor");
    @FlatStylingSupport.Styleable
    protected Color clearIconHoverColor = UIManager.getColor("SearchField.clearIconHoverColor");
    @FlatStylingSupport.Styleable
    protected Color clearIconPressedColor = UIManager.getColor("SearchField.clearIconPressedColor");
    private final boolean ignoreButtonState;

    public FlatClearIcon() {
        this(false);
    }

    public FlatClearIcon(boolean ignoreButtonState) {
        super(16, 16, null);
        this.ignoreButtonState = ignoreButtonState;
    }

    public Object applyStyleProperty(String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObject(this, key, value);
    }

    public Map<String, Class<?>> getStyleableInfos() {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    public Object getStyleableValue(String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g) {
        ButtonModel model;
        if (!this.ignoreButtonState && c instanceof AbstractButton && ((model = ((AbstractButton)c).getModel()).isPressed() || model.isRollover())) {
            g.setColor(model.isPressed() ? this.clearIconPressedColor : this.clearIconHoverColor);
            Path2D.Float path = new Path2D.Float(0);
            path.append(new Ellipse2D.Float(1.75f, 1.75f, 12.5f, 12.5f), false);
            path.append(FlatUIUtils.createPath(4.5, 5.5, 5.5, 4.5, 8.0, 7.0, 10.5, 4.5, 11.5, 5.5, 9.0, 8.0, 11.5, 10.5, 10.5, 11.5, 8.0, 9.0, 5.5, 11.5, 4.5, 10.5, 7.0, 8.0), false);
            g.fill(path);
            return;
        }
        g.setColor(this.clearIconColor);
        Path2D.Float path = new Path2D.Float(0, 4);
        ((Path2D)path).moveTo(5.0, 5.0);
        ((Path2D)path).lineTo(11.0, 11.0);
        ((Path2D)path).moveTo(5.0, 11.0);
        ((Path2D)path).lineTo(11.0, 5.0);
        g.draw(path);
    }
}

