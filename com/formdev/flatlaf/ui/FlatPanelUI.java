/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

public class FlatPanelUI
extends BasicPanelUI
implements FlatStylingSupport.StyleableUI,
PropertyChangeListener {
    @FlatStylingSupport.Styleable
    protected int arc = -1;
    private final boolean shared;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatPanelUI.class, () -> new FlatPanelUI(true)) : new FlatPanelUI(false);
    }

    protected FlatPanelUI(boolean shared) {
        this.shared = shared;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.addPropertyChangeListener(this);
        this.installStyle((JPanel)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.removePropertyChangeListener(this);
        this.oldStyleValues = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "FlatLaf.style": 
            case "FlatLaf.styleClass": {
                JPanel c = (JPanel)e.getSource();
                if (this.shared && FlatStylingSupport.hasStyleProperty(c)) {
                    c.updateUI();
                } else {
                    this.installStyle(c);
                }
                c.revalidate();
                c.repaint();
            }
        }
    }

    protected void installStyle(JPanel c) {
        try {
            this.applyStyle(c, FlatStylingSupport.getResolvedStyle(c, "Panel"));
        } catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(JPanel c, Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, (key, value) -> this.applyStyleProperty(c, (String)key, value));
    }

    protected Object applyStyleProperty(JPanel c, String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, c, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            int arc;
            int width = c.getWidth();
            int height = c.getHeight();
            int n = this.arc >= 0 ? this.arc : (arc = c.getBorder() instanceof FlatLineBorder ? ((FlatLineBorder)c.getBorder()).getArc() : 0);
            if (arc > 0) {
                FlatUIUtils.paintParentBackground(g, c);
            }
            g.setColor(c.getBackground());
            if (arc > 0) {
                Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
                FlatUIUtils.paintComponentBackground((Graphics2D)g, 0, 0, width, height, 0.0f, UIScale.scale(arc));
                FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
            } else {
                g.fillRect(0, 0, width, height);
            }
        }
        this.paint(g, c);
    }
}

