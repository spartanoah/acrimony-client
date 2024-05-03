/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class FlatToggleButtonUI
extends FlatButtonUI {
    @FlatStylingSupport.Styleable(dot=true)
    protected int tabUnderlineHeight;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabUnderlineColor;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabDisabledUnderlineColor;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabSelectedBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabSelectedForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabHoverBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabHoverForeground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabFocusBackground;
    @FlatStylingSupport.Styleable(dot=true)
    protected Color tabFocusForeground;
    private boolean defaults_initialized = false;

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatToggleButtonUI.class, () -> new FlatToggleButtonUI(true)) : new FlatToggleButtonUI(false);
    }

    protected FlatToggleButtonUI(boolean shared) {
        super(shared);
    }

    @Override
    String getStyleType() {
        return "ToggleButton";
    }

    @Override
    protected String getPropertyPrefix() {
        return "ToggleButton.";
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if (!this.defaults_initialized) {
            this.tabUnderlineHeight = UIManager.getInt("ToggleButton.tab.underlineHeight");
            this.tabUnderlineColor = UIManager.getColor("ToggleButton.tab.underlineColor");
            this.tabDisabledUnderlineColor = UIManager.getColor("ToggleButton.tab.disabledUnderlineColor");
            this.tabSelectedBackground = UIManager.getColor("ToggleButton.tab.selectedBackground");
            this.tabSelectedForeground = UIManager.getColor("ToggleButton.tab.selectedForeground");
            this.tabHoverBackground = UIManager.getColor("ToggleButton.tab.hoverBackground");
            this.tabHoverForeground = UIManager.getColor("ToggleButton.tab.hoverForeground");
            this.tabFocusBackground = UIManager.getColor("ToggleButton.tab.focusBackground");
            this.tabFocusForeground = UIManager.getColor("ToggleButton.tab.focusForeground");
            this.defaults_initialized = true;
        }
    }

    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        this.defaults_initialized = false;
    }

    @Override
    protected void propertyChange(AbstractButton b, PropertyChangeEvent e) {
        super.propertyChange(b, e);
        switch (e.getPropertyName()) {
            case "JButton.buttonType": {
                if ("tab".equals(e.getOldValue()) || "tab".equals(e.getNewValue())) {
                    MigLayoutVisualPadding.uninstall(b);
                    MigLayoutVisualPadding.install(b);
                    b.revalidate();
                }
                b.repaint();
                break;
            }
            case "JToggleButton.tab.underlinePlacement": 
            case "JToggleButton.tab.underlineHeight": 
            case "JToggleButton.tab.underlineColor": 
            case "JToggleButton.tab.selectedBackground": {
                b.repaint();
            }
        }
    }

    @Override
    protected Object applyStyleProperty(AbstractButton b, String key, Object value) {
        if (key.startsWith("help.")) {
            throw new FlatStylingSupport.UnknownStyleException(key);
        }
        return super.applyStyleProperty(b, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        Map<String, Class<?>> infos = super.getStyleableInfos(c);
        infos.keySet().removeIf(s -> s.startsWith("help."));
        return infos;
    }

    static boolean isTabButton(Component c) {
        return c instanceof JToggleButton && "tab".equals(FlatToggleButtonUI.getButtonTypeStr((JToggleButton)c));
    }

    @Override
    protected void paintBackground(Graphics g, JComponent c) {
        if (FlatToggleButtonUI.isTabButton(c)) {
            Color background;
            Color bg;
            Color enabledColor;
            int height = c.getHeight();
            int width = c.getWidth();
            boolean selected = ((AbstractButton)c).isSelected();
            Color color = enabledColor = selected ? FlatClientProperties.clientPropertyColor(c, "JToggleButton.tab.selectedBackground", this.tabSelectedBackground) : null;
            if (enabledColor == null && this.isCustomBackground(bg = c.getBackground())) {
                enabledColor = bg;
            }
            if ((background = FlatToggleButtonUI.buttonStateColor(c, enabledColor, null, this.tabFocusBackground, this.tabHoverBackground, null)) != null) {
                g.setColor(background);
                g.fillRect(0, 0, width, height);
            }
            if (selected) {
                int underlineThickness = UIScale.scale(FlatClientProperties.clientPropertyInt(c, "JToggleButton.tab.underlineHeight", this.tabUnderlineHeight));
                g.setColor(c.isEnabled() ? FlatClientProperties.clientPropertyColor(c, "JToggleButton.tab.underlineColor", this.tabUnderlineColor) : this.tabDisabledUnderlineColor);
                int placement = FlatClientProperties.clientPropertyInt(c, "JToggleButton.tab.underlinePlacement", 3);
                switch (placement) {
                    case 1: {
                        g.fillRect(0, 0, width, underlineThickness);
                        break;
                    }
                    case 2: {
                        g.fillRect(0, 0, underlineThickness, height);
                        break;
                    }
                    case 4: {
                        g.fillRect(width - underlineThickness, 0, underlineThickness, height);
                        break;
                    }
                    default: {
                        g.fillRect(0, height - underlineThickness, width, underlineThickness);
                    }
                }
            }
        } else {
            super.paintBackground(g, c);
        }
    }

    @Override
    protected Color getForeground(JComponent c) {
        if (FlatToggleButtonUI.isTabButton(c)) {
            if (!c.isEnabled()) {
                return this.disabledText;
            }
            if (this.tabSelectedForeground != null && ((AbstractButton)c).isSelected()) {
                return this.tabSelectedForeground;
            }
            return FlatToggleButtonUI.buttonStateColor(c, c.getForeground(), this.disabledText, this.tabFocusForeground, this.tabHoverForeground, null);
        }
        return super.getForeground(c);
    }
}

